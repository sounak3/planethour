/*
 * File: Settings.java in java package com.sounaks.planethour is part of application
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * User settings, kept in ~/.planethour/settings.properties.
 *
 * @author Sounak Choudhury
 */
final class Settings {

    static final String FILE_NAME = "settings.properties";
    static final String DEFAULT_COUNTRY = "India";
    static final String DEFAULT_CITY = "Pune";

    int x;
    int y;
    boolean blackBackground;
    boolean symbols;
    boolean twelveHourClock;
    String country = DEFAULT_COUNTRY;
    String city = DEFAULT_CITY;

    /**
     * Loads the saved settings, or their backup, or the defaults when neither can be read.
     */
    static Settings load() {
        Settings loaded = UserFiles.load(UserFiles.file(FILE_NAME), Settings::read);
        return loaded != null ? loaded : new Settings();
    }

    void save() throws IOException {
        UserFiles.save(UserFiles.file(FILE_NAME), this::write);
    }

    private static Settings read(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(new InputStreamReader(in, UTF_8));
        Settings settings = new Settings();
        try {
            settings.x = Integer.parseInt(required(props, "window.x"));
            settings.y = Integer.parseInt(required(props, "window.y"));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid window position", e);
        }
        settings.blackBackground = flag(props, "background.black");
        settings.symbols = flag(props, "planets.symbols");
        settings.twelveHourClock = flag(props, "clock.12hour");
        settings.country = required(props, "place.country");
        settings.city = required(props, "place.city");
        return settings;
    }

    private void write(OutputStream out) throws IOException {
        Properties props = new Properties();
        props.setProperty("window.x", Integer.toString(x));
        props.setProperty("window.y", Integer.toString(y));
        props.setProperty("background.black", Boolean.toString(blackBackground));
        props.setProperty("planets.symbols", Boolean.toString(symbols));
        props.setProperty("clock.12hour", Boolean.toString(twelveHourClock));
        props.setProperty("place.country", country);
        props.setProperty("place.city", city);
        Writer writer = new OutputStreamWriter(out, UTF_8);
        props.store(writer, "PlanetHour settings");
        writer.flush();
    }

    // A missing or malformed value means the file is damaged, so loading falls back to the backup.
    private static String required(Properties props, String key) throws IOException {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IOException("Missing setting " + key);
        }
        return value.trim();
    }

    private static boolean flag(Properties props, String key) throws IOException {
        String value = required(props, key);
        if (!value.equals("true") && !value.equals("false")) {
            throw new IOException("Invalid setting " + key + "=" + value);
        }
        return Boolean.parseBoolean(value);
    }
}
