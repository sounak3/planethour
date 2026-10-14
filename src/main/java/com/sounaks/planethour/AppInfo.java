/*
 * File: AppInfo.java in java package com.sounaks.planethour is part of application
 * PlanetHour - Planetary hour calculation software
 * Copyright (C) 2014 - 2026 Sounak Choudhury
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
import java.util.Properties;

/** Application name, version and copyright for the About tab. The version comes from pom.xml at build time. */
final class AppInfo
{
	static final String COPYRIGHT = "Copyright (C) 2014 - 2026 Sounak Choudhury";

	private static final String VERSION = loadVersion();

	private AppInfo()
	{
	}

	private static String loadVersion()
	{
		Properties props = new Properties();
		try (InputStream in = AppInfo.class.getResourceAsStream("version.properties")) {
			if (in != null) props.load(in);
		} catch (IOException e) {
			// fall through to the empty version
		}
		String version = props.getProperty("version", "");
		if (version.startsWith("${")) version = "";   // resource was not filtered (built outside Maven)
		return version.replace("-SNAPSHOT", "");
	}

	/** The version as installers show it, e.g. "2.0"; empty if unknown. */
	static String version()
	{
		return VERSION;
	}

	static String aboutText()
	{
		String name = VERSION.isEmpty() ? "PlanetHour" : "PlanetHour v" + VERSION;
		return "<html>" + name + " - Planetary hour calculation software<br>" + COPYRIGHT + "</html>";
	}
}
