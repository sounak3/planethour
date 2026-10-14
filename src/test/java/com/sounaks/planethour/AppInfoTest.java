package com.sounaks.planethour;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class AppInfoTest
{
	private static String pomVersion() throws Exception
	{
		Matcher m = Pattern.compile("<artifactId>planethour</artifactId>\\s*(?:<!--.*?-->\\s*)?<version>([^<]+)</version>", Pattern.DOTALL)
				.matcher(Files.readString(Path.of("pom.xml")));
		assertTrue(m.find(), "project version in pom.xml");
		return m.group(1);
	}

	@Test
	void versionComesFromThePomWithoutSnapshot() throws Exception
	{
		assertEquals(pomVersion().replace("-SNAPSHOT", ""), AppInfo.version());
		assertTrue(AppInfo.version().matches("\\d+(\\.\\d+)+"), AppInfo.version());
	}

	@Test
	void aboutTextShowsVersionAndCopyrightYears()
	{
		String about = AppInfo.aboutText();
		assertTrue(about.contains("PlanetHour v" + AppInfo.version() + " - Planetary hour calculation software"), about);
		assertTrue(about.contains("Copyright (C) 2014 - 2026 Sounak Choudhury"), about);
	}
}
