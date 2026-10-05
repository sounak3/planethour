# PlanetHour

PlanetHour calculates the **planetary hours** (Chaldean order) and the Hindu special periods Rahu Kalam, Gulika Kalam and Yamaganda for any place and date, from the place's latitude, longitude and time zone. It shows the hour that is running now, the next one, and a table for the whole day and night, and can sit in the system tray.

It comes with about 10,900 cities in 185 countries. You can add your own places or correct existing ones.

## Install

Download the installer for your system from the [Releases](https://github.com/sounak3/planetHour/releases) page:

| System | File |
|---|---|
| Windows 10/11 | `PlanetHour-<version>.msi` |
| Ubuntu / Debian | `planethour_<version>-release_amd64.deb` (`sudo apt install ./planethour_*.deb`) |
| macOS | `PlanetHour-<version>.dmg` (Intel; runs on Apple Silicon through Rosetta) |

The installers include their own Java runtime.

## Run from the jar

With Java 21 or newer installed:

```bash
java -jar planethour.jar
```

It works from any folder. Your settings and places are kept in `~/.planethour/` (`%USERPROFILE%\.planethour\` on Windows).

## Build

```bash
mvn clean verify     # compiles, runs the tests and writes target/planethour.jar
```

Requires JDK 21 and Maven. How the Jenkins pipelines build the installers is described in [BUILD.md](BUILD.md).

## License

PlanetHour is free software under the [GNU General Public License v3](LICENSE) or later.

Copyright (C) 2013-2026 Sounak Choudhury
