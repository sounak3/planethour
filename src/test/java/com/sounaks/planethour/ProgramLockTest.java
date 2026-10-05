package com.sounaks.planethour;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProgramLockTest
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

	@Test
	void secondInstanceSeesTheFirstAndTheLockLivesInTheUserFolder()
	{
		ProgramLock first = new ProgramLock();
		ProgramLock second = new ProgramLock();
		try {
			assertFalse(first.isAppActive());
			assertTrue(second.isAppActive());
			assertTrue(Files.isRegularFile(home.resolve(UserFiles.DIR_NAME).resolve(ProgramLock.FILE_NAME)));
			assertFalse(Files.exists(home.resolve("PlanetHour.tmp")), "no lock file in the home folder itself");
		} finally {
			first.closeLock();
		}
	}

	@Test
	void freeAgainAfterTheFirstInstanceReleases()
	{
		ProgramLock first = new ProgramLock();
		assertFalse(first.isAppActive());
		first.closeLock();
		ProgramLock next = new ProgramLock();
		try {
			assertFalse(next.isAppActive());
		} finally {
			next.closeLock();
		}
	}
}
