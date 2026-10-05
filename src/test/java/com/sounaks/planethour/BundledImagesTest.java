package com.sounaks.planethour;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Image;

import org.junit.jupiter.api.Test;

class BundledImagesTest
{
	private static void assertLoaded(Image image, String what)
	{
		assertNotNull(image, what);
		assertTrue(image.getWidth(null) > 0, what + " has no pixels");
	}

	@Test
	void loadsFixedImagesFromTheJarOnJava9AndLater()
	{
		assertLoaded(Planet.getEarthImage(PlanetHour.class), "earth");
		assertLoaded(Planet.getEarthSunImage(PlanetHour.class), "earthsun");
		assertLoaded(Planet.getEarthMoonImage(PlanetHour.class), "earthmoon");
		assertLoaded(Planet.getSearchIcon(PlanetHour.class), "search");
		assertLoaded(Planet.getImageNA(PlanetHour.class), "na");
		assertLoaded(Planet.getImageSun(PlanetHour.class), "sun");
		assertLoaded(Planet.getImageMoon(PlanetHour.class), "moon");
		assertLoaded(Planet.getImageStar(PlanetHour.class), "star");
	}

	@Test
	void loadsEveryPlanetImageAndSymbol()
	{
		for (String name : Planet.chaldean) {
			Planet planet = new Planet(name, 0, 1, false);
			assertLoaded(planet.getImage(false, PlanetHour.class), name);
			assertLoaded(planet.getImage(true, PlanetHour.class), name + " symbol");
		}
	}
}
