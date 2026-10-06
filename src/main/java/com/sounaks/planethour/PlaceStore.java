/*
 * File: PlaceStore.java in java package com.sounaks.planethour is part of application
 * PlanetHour - Planetary hour calculation software
 * Copyright (C) 2026 Sounak Choudhury
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sounaks.planethour;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The places (cities) and countries the user can choose from.
 * <p>
 * The bundled list is read from the jar and never changes. The user's own changes are kept apart in
 * ~/.planethour/places.csv: "+" lines add or replace a place, "-" lines remove a bundled one. So a newer
 * bundled list still reaches users who have edited places.
 *
 * @author Sounak Choudhury
 */
final class PlaceStore {

    static final String USER_FILE = "places.csv";
    private static final String SEPARATOR = ";";

    /**
     * One place, in the stored formats: coordinates as DDDMM, N or S, E or W, and a GMT offset such as +05:30.
     */
    record Place(String country, String city, String latitude, String longitude,
                 String northSouth, String eastWest, String gmtOffset) {

        Place {
            if (country == null || country.isBlank() || city == null || city.isBlank()) {
                throw new IllegalArgumentException("Country and city must not be blank");
            }
            for (String field : new String[]{country, city, latitude, longitude, northSouth, eastWest, gmtOffset}) {
                if (field == null || field.contains(SEPARATOR) || field.contains("\n") || field.contains("\r")) {
                    throw new IllegalArgumentException("Place fields must not be null or contain ';' or line breaks");
                }
            }
        }

        PlaceRecord toPlaceRecord() {
            return new PlaceRecord(city, latitude, northSouth.equalsIgnoreCase("N"),
                    longitude, eastWest.equalsIgnoreCase("E"), gmtOffset);
        }

        private String key() {
            return PlaceStore.key(country, city);
        }
    }

    private final Map<String, String> countryOffsets = new LinkedHashMap<>();
    private final Map<String, Place> bundled = new LinkedHashMap<>();
    private final Map<String, Place> added = new LinkedHashMap<>();
    private final Set<String> removed = new LinkedHashSet<>();

    private PlaceStore() {
    }

    /**
     * Loads the bundled places and countries, then the user's changes (or their backup).
     */
    static PlaceStore load() {
        PlaceStore store = new PlaceStore();
        try {
            store.readBundled();
        } catch (IOException | RuntimeException e) {
            Logger.getLogger(PlaceStore.class.getName()).log(Level.SEVERE, "Cannot read the bundled places", e);
        }
        PlaceStore userChanges = UserFiles.load(UserFiles.file(USER_FILE), PlaceStore::readUserChanges);
        if (userChanges != null) {
            store.added.putAll(userChanges.added);
            store.removed.addAll(userChanges.removed);
        }
        return store;
    }

    Place find(String country, String city) {
        String key = key(country, city);
        Place place = added.get(key);
        if (place == null && !removed.contains(key)) {
            place = bundled.get(key);
        }
        return place;
    }

    /**
     * The first place in country and city order, for when no saved or default place exists.
     */
    Place first() {
        for (String country : countries()) {
            List<String> cities = cities(country);
            if (!cities.isEmpty()) {
                return find(country, cities.get(0));
            }
        }
        return null;
    }

    /**
     * All countries, including those that have no places and those that only the user's places use.
     */
    List<String> countries() {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(countryOffsets.keySet());
        for (Place place : places()) {
            names.add(place.country());
        }
        return new ArrayList<>(names);
    }

    List<String> cities(String country) {
        Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Place place : places()) {
            if (place.country().equals(country)) {
                names.add(place.city());
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * The country's usual GMT offset, such as +05.30, or null when the country is unknown.
     */
    String countryOffset(String country) {
        return countryOffsets.get(country);
    }

    /**
     * Adds a place, or replaces the one with the same country and city, and saves the user's changes.
     */
    void put(Place place) throws IOException {
        added.put(place.key(), place);
        removed.remove(place.key());
        save();
    }

    /**
     * Removes a place and saves the user's changes.
     * @return false when there was no such place
     */
    boolean remove(String country, String city) throws IOException {
        if (find(country, city) == null) {
            return false;
        }
        String key = key(country, city);
        added.remove(key);
        if (bundled.containsKey(key)) {
            removed.add(key);
        }
        save();
        return true;
    }

    private List<Place> places() {
        List<Place> places = new ArrayList<>(bundled.size() + added.size());
        for (Map.Entry<String, Place> entry : bundled.entrySet()) {
            if (!removed.contains(entry.getKey()) && !added.containsKey(entry.getKey())) {
                places.add(entry.getValue());
            }
        }
        places.addAll(added.values());
        return places;
    }

    private static String key(String country, String city) {
        return country + '\n' + city;
    }

    private void readBundled() throws IOException {
        for (String[] fields : readLines(PlaceStore.class.getResourceAsStream("data/countries.csv"), "data/countries.csv")) {
            expectFields(fields, 2, "data/countries.csv");
            countryOffsets.put(fields[0], fields[1]);
        }
        for (String[] fields : readLines(PlaceStore.class.getResourceAsStream("data/cities.csv"), "data/cities.csv")) {
            expectFields(fields, 7, "data/cities.csv");
            Place place = new Place(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
            bundled.put(place.key(), place);
        }
    }

    private static PlaceStore readUserChanges(InputStream in) throws IOException {
        PlaceStore changes = new PlaceStore();
        for (String[] fields : readLines(in, USER_FILE)) {
            if (fields[0].equals("+") && fields.length == 8) {
                Place place = new Place(fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
                changes.added.put(place.key(), place);
            } else if (fields[0].equals("-") && fields.length == 3) {
                changes.removed.add(key(fields[1], fields[2]));
            } else {
                throw new IOException("Unexpected line in " + USER_FILE + ": " + String.join(SEPARATOR, fields));
            }
        }
        return changes;
    }

    private void save() throws IOException {
        UserFiles.save(UserFiles.file(USER_FILE), this::writeUserChanges);
    }

    private void writeUserChanges(OutputStream out) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, UTF_8));
        writer.println("# PlanetHour places: '+' adds or replaces a place, '-' removes a bundled place");
        for (Place place : added.values()) {
            writer.println(String.join(SEPARATOR, "+", place.country(), place.city(), place.latitude(),
                    place.longitude(), place.northSouth(), place.eastWest(), place.gmtOffset()));
        }
        for (String key : removed) {
            writer.println("-" + SEPARATOR + key.replace("\n", SEPARATOR));
        }
        writer.flush();
    }

    // Blank lines and lines starting with '#' are skipped.
    private static List<String[]> readLines(InputStream in, String name) throws IOException {
        if (in == null) {
            throw new IOException("Missing " + name);
        }
        List<String[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    lines.add(line.split(SEPARATOR, -1));
                }
            }
        }
        return lines;
    }

    private static void expectFields(String[] fields, int count, String name) throws IOException {
        if (fields.length != count) {
            throw new IOException("Expected " + count + " fields in " + name + ": " + String.join(SEPARATOR, fields));
        }
    }
}
