package com.sounaks.planethour;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlaceStoreTest
{
	@TempDir Path home;
	private String originalHome;

	@BeforeEach
	void isolateFromRealUser()
	{
		originalHome = System.getProperty("user.home");
		System.setProperty("user.home", home.toString());
	}

	@AfterEach
	void restore()
	{
		System.setProperty("user.home", originalHome);
	}

	private Path userFile()
	{
		return home.resolve(UserFiles.DIR_NAME).resolve(PlaceStore.USER_FILE);
	}

	private static PlaceStore.Place testville()
	{
		return new PlaceStore.Place("India", "Testville", "01200", "07700", "N", "E", "+05:30");
	}

	@Test
	void loadsBundledPlacesWithNoUserFiles()
	{
		PlaceStore store = PlaceStore.load();
		assertEquals(185, store.countries().size());
		assertFalse(store.countries().contains("Name"), "CSV header row must not be a country");
		assertTrue(store.countries().contains("Seychelles"), "countries without places stay selectable");
		assertEquals(new PlaceStore.Place("India", "Pune", "01834", "07358", "N", "E", "+05:30"), store.find("India", "Pune"));
		assertEquals("+05.30", store.countryOffset("India"));
		assertFalse(Files.exists(home.resolve(UserFiles.DIR_NAME)), "loading must not write anything");
	}

	@Test
	void defaultPlaceGivesAUsablePlaceRecord()
	{
		PlaceRecord pune = PlaceStore.load().find(Settings.DEFAULT_COUNTRY, Settings.DEFAULT_CITY).toPlaceRecord();
		assertEquals("Pune", pune.place_name);
		assertTrue(pune.north_south);
		assertTrue(pune.east_west);
		assertEquals("GMT+05:30", pune.getTimezone().getID());
	}

	@Test
	void citiesAreSortedAndBelongToTheirCountry()
	{
		PlaceStore store = PlaceStore.load();
		var cities = store.cities("Denmark");
		assertTrue(cities.contains("Randers"));
		for (int i = 1; i < cities.size(); i++) {
			assertTrue(cities.get(i - 1).compareToIgnoreCase(cities.get(i)) <= 0, "not sorted at " + i);
		}
		assertTrue(store.cities("No such country").isEmpty());
	}

	@Test
	void addedPlaceSurvivesAReload() throws IOException
	{
		PlaceStore.load().put(testville());
		assertEquals(testville(), PlaceStore.load().find("India", "Testville"));
	}

	@Test
	void editedBundledPlaceOverridesTheBundledValues() throws IOException
	{
		PlaceStore.Place edited = new PlaceStore.Place("India", "Pune", "01831", "07351", "N", "E", "+05:30");
		PlaceStore.load().put(edited);
		PlaceStore reloaded = PlaceStore.load();
		assertEquals(edited, reloaded.find("India", "Pune"));
		assertEquals(1, reloaded.cities("India").stream().filter("Pune"::equals).count());
	}

	@Test
	void removedBundledPlaceStaysRemovedAndCanBeAddedBack() throws IOException
	{
		assertTrue(PlaceStore.load().remove("India", "Pune"));
		PlaceStore reloaded = PlaceStore.load();
		assertNull(reloaded.find("India", "Pune"));
		assertFalse(reloaded.cities("India").contains("Pune"));

		reloaded.put(new PlaceStore.Place("India", "Pune", "01834", "07358", "N", "E", "+05:30"));
		assertNotNull(PlaceStore.load().find("India", "Pune"));
	}

	@Test
	void removingAnUnknownPlaceChangesNothing() throws IOException
	{
		assertFalse(PlaceStore.load().remove("India", "Nowhere"));
		assertFalse(Files.exists(userFile()));
	}

	@Test
	void placeInANewCountryAddsThatCountry() throws IOException
	{
		PlaceStore.load().put(new PlaceStore.Place("Atlantis", "Poseidonia", "03000", "02000", "N", "W", "-01:00"));
		assertTrue(PlaceStore.load().countries().contains("Atlantis"));
	}

	@Test
	void keepsPreviousVersionAsBackupAndRecoversFromCorruptFile() throws IOException
	{
		PlaceStore.load().put(testville());
		PlaceStore.load().put(new PlaceStore.Place("India", "Othertown", "01300", "07800", "N", "E", "+05:30"));
		assertTrue(Files.isRegularFile(UserFiles.backupOf(userFile())));

		Files.writeString(userFile(), "+;India;broken line\n", StandardCharsets.UTF_8);
		PlaceStore recovered = PlaceStore.load();
		assertEquals(testville(), recovered.find("India", "Testville"));
		assertNull(recovered.find("India", "Othertown"));
	}

	@Test
	void rejectsNamesThatWouldBreakTheFile()
	{
		assertThrows(IllegalArgumentException.class,
				() -> new PlaceStore.Place("India", "Bad;Name", "01200", "07700", "N", "E", "+05:30"));
		assertThrows(IllegalArgumentException.class,
				() -> new PlaceStore.Place("India", "Bad\nName", "01200", "07700", "N", "E", "+05:30"));
		assertThrows(IllegalArgumentException.class,
				() -> new PlaceStore.Place("", "Tahiti", "01737", "14927", "S", "W", "-10:00"));
	}

	@Test
	void keepsNamesWithPunctuation() throws IOException
	{
		PlaceStore.Place place = new PlaceStore.Place("St. Kitts & Nevis", "Basseterre (Old Town), W.I.", "01718", "06243", "N", "W", "-04:00");
		PlaceStore.load().put(place);
		assertEquals(place, PlaceStore.load().find("St. Kitts & Nevis", "Basseterre (Old Town), W.I."));
	}
}
