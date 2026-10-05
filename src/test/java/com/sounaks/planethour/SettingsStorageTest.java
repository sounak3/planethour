package com.sounaks.planethour;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsStorageTest
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

	private Path settingsFile()
	{
		return home.resolve(UserFiles.DIR_NAME).resolve(Settings.FILE_NAME);
	}

	private static Settings sample(String city, int x)
	{
		Settings settings = new Settings();
		settings.x = x;
		settings.y = 175;
		settings.blackBackground = true;
		settings.symbols = true;
		settings.twelveHourClock = true;
		settings.country = "Japan";
		settings.city = city;
		return settings;
	}

	@Test
	void startsWithPuneWhenNothingIsSaved()
	{
		Settings settings = Settings.load();
		assertEquals("India", settings.country);
		assertEquals("Pune", settings.city);
		assertFalse(settings.blackBackground);
		assertFalse(settings.twelveHourClock);
	}

	@Test
	void savesIntoTheUserFolderWhenItDoesNotExistYet() throws IOException
	{
		assertFalse(Files.exists(home.resolve(UserFiles.DIR_NAME)));
		sample("Tokyo", 430).save();
		assertTrue(Files.isRegularFile(settingsFile()));
	}

	@Test
	void roundTripsEverySetting() throws IOException
	{
		sample("Tokyo", 430).save();
		Settings loaded = Settings.load();
		assertEquals(430, loaded.x);
		assertEquals(175, loaded.y);
		assertTrue(loaded.blackBackground);
		assertTrue(loaded.symbols);
		assertTrue(loaded.twelveHourClock);
		assertEquals("Japan", loaded.country);
		assertEquals("Tokyo", loaded.city);
	}

	@Test
	void keepsPreviousVersionAsBackupAndRecoversFromCorruptFile() throws IOException
	{
		sample("Osaka", 10).save();
		sample("Tokyo", 20).save();
		assertTrue(Files.isRegularFile(UserFiles.backupOf(settingsFile())));

		Files.writeString(settingsFile(), "window.x=garbage\n", StandardCharsets.UTF_8);
		Settings loaded = Settings.load();
		assertEquals("Osaka", loaded.city);
		assertEquals(10, loaded.x);
	}

	@Test
	void usesDefaultsWhenFileAndBackupAreUnreadable() throws IOException
	{
		Files.createDirectories(settingsFile().getParent());
		Files.writeString(settingsFile(), "", StandardCharsets.UTF_8);
		Files.writeString(UserFiles.backupOf(settingsFile()), "place.city=\n", StandardCharsets.UTF_8);
		assertEquals("Pune", Settings.load().city);
	}

	@Test
	void leavesNoTempFilesBehind() throws IOException
	{
		sample("Tokyo", 1).save();
		sample("Kyoto", 2).save();
		File[] files = settingsFile().getParent().toFile().listFiles((dir, name) -> name.endsWith(".tmp"));
		assertEquals(0, files.length);
	}
}
