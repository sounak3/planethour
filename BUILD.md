# Building PlanetHour

This document describes how PlanetHour is built, tested and packaged with Jenkins: the two pipelines, the build agents, what each agent needs installed, how the app stores its data, and the known issues.

PlanetHour shares its Jenkins controller, agents and backups with DeskStop. Those are documented once, in [desktime's BUILD.md](https://github.com/sounak3/desktime/blob/main/BUILD.md) (sections *Build infrastructure*, *Rotating inbound agent secrets* and *Backups*). This file covers what is specific to PlanetHour and summarises the rest.

## Pipelines at a glance

| Jenkins job | Pipeline file | Purpose | Trigger | Output |
|---|---|---|---|---|
| `planethour` | [`Jenkinsfile`](Jenkinsfile) | **Release.** Builds and tests the jar from source, then packages native installers on each OS. | Manual: *Build Now* | `PlanetHour-<ver>.msi`, `planethour_<ver>-release_amd64.deb`, `PlanetHour-<ver>.dmg`, `planethour.jar` (archived in Jenkins) |
| `planethour dev` | [`Jenkinsfile.dev`](Jenkinsfile.dev) | **CI / testing.** Copies the jar your IDE just built onto all three test machines, then runs the unit tests. No installers. | Automatic, whenever `target/planethour.jar` changes on dell5558 | `planethour_latest.jar` on the Desktop of lin, mac and win (also archived in Jenkins) |

Typical workflow:

1. Build in the IDE (`mvn package`). This writes `target/planethour.jar`.
2. `planethour dev` runs automatically. It drops `planethour_latest.jar` on the Desktop of all three machines, prints its SHA-256 on each, and runs the unit tests on a snapshot of your working copy.
3. Test the jar on Linux, macOS and Windows (`java -jar ~/Desktop/planethour_latest.jar`).
4. To release a new version, set `<version>` in `pom.xml` (see *Version*). Commit and push to `main`.
5. Run `planethour` (release). Download the installers from the build page and upload them to GitHub Releases.

Each build's description shows what it was built from:

- **Release:** `v2.0 @ 1a2b3c4d`, meaning the version and the commit it was built from.
- **Dev:** `1a2b3c4`, or `1a2b3c4 + uncommitted changes` if the IDE build included work that wasn't committed yet.

### Version

`pom.xml` is the only place the version is set. The release pipeline reads `project.version`, removes `-SNAPSHOT`, and uses the result as the installer version. The MSI format accepts only numeric versions (`2.0`, `2.0.1`), and the pipeline stops with an error otherwise.

PlanetHour uses **incremental major versions**: each release raises the major number (`2.0-SNAPSHOT` → installers `2.0`, then `3.0-SNAPSHOT` → `3.0`, and so on). Version 2.0 is the first release built by these pipelines; earlier versions were NetBeans/Ant builds.

### Release pipeline flow

```
Build jar (lin)                         Package (parallel)
  checkout scm                     ┌──> Windows (win): jlink → jpackage --type msi (WiX 3.14)
  version from pom.xml             │
  mvn clean verify (tests) ─stash─>├──> Ubuntu  (lin): jlink → jpackage --type deb
  app/ + extras/ icons             └──> Mac OS  (mac): jlink → jpackage --type dmg
```

Only `lin` checks out the repository. `win` and `mac` receive the jar, license and icons through `stash`/`unstash`, so they need neither git nor GitHub access. If a unit test fails, the release stops before packaging.

The `app/` folder is jpackage's `--input`, and everything in it ships inside the installer:

| File in installer | Source in repo |
|---|---|
| `planethour.jar` | `target/planethour.jar` (built by Maven; the places list and images are inside it) |
| `LICENSE.txt` | `LICENSE` (GPL-3.0, renamed, as jpackage's license file) |

There are no default data files next to the jar: the app needs nothing but the jar.

The bundled runtime contains only the modules `jdeps` reports (`java.base`, `java.desktop`, `java.logging`), about 54 MB after `jlink --compress=zip-6`.

Installer settings: name `PlanetHour`, vendor *Sounak Choudhury*, description *Planetary hour calculator*. Windows: directory chooser, Start menu group `PlanetHour`, desktop shortcut. Linux: category and menu group `Utility`, shortcut. macOS: category `utilities`.

### Icons

| File | Used for | Made by |
|---|---|---|
| [`art/PlanetHour-icon.png`](art/PlanetHour-icon.png) | Source image (256×256, the original PlanetHour icon) | |
| `extras/PlanetHour.png` | Linux (`--icon`) | `extras/create-lin-icon.sh` |
| `extras/PlanetHour.ico` | Windows (`--icon`); 16, 32, 48, 128 and 256 px | `extras/Create-WinIcon.ps1` on Windows, or `convert art/PlanetHour-icon.png -define icon:auto-resize=256,128,48,32,16 extras/PlanetHour.ico` |
| `extras/PlanetHour.icns` | macOS (`--icon`) | `extras/create-mac-icon.sh` on a Mac, or Pillow: `Image.open('art/PlanetHour-icon.png').save('extras/PlanetHour.icns')` |

The scripts are the ones from desktime with the names changed. They find the source image by name anywhere in the repository except `extras/`, because the Windows script deletes its working copy of the source when it finishes. The icons in the repository were made on 2026-10-05 with ImageMagick and Pillow on dell5558. Icons are passed with `--icon`, so they are not copied into `app/`.

The Jenkinsfile is the only packaging definition. Don't add a jpackage plugin to `pom.xml`.

## Where the app stores its data

Everything the app writes is per user, in `~/.planethour/` (`%USERPROFILE%\.planethour\` on Windows). Nothing is written next to the jar or into the folder the app was started from.

| File | Contents |
|---|---|
| `settings.properties`, `settings.properties.bak` | Window position, black background, planet symbols, 12-hour clock, selected country and city; and the previous version |
| `places.csv`, `places.csv.bak` | Your own changes to the places list: `+` lines add or replace a place, `-` lines remove a bundled one; and the previous version. Only exists after you add, edit or delete a place. |
| `planethour.lock` | Stops the same user from running two copies at once. It is checked before any data is loaded. |

The places list itself (about 10,900 cities in 185 countries) is bundled in the jar as `data/cities.csv` and `data/countries.csv`. Your changes are kept separately, so a newer bundled list in a later version still reaches you.

Every save writes a temporary file in the same folder, copies the current file to `.bak`, then moves the temporary file into place, so a crash can't leave a truncated file. On start, each file is loaded from the first of these that can be read: the file → its `.bak` → built-in defaults (Pune, India; window placed by the system). A damaged file never makes the app start empty while a readable backup exists. Settings are saved as soon as they change, and the window position a second after the window stops moving, so nothing is lost on logoff or if the process is killed.

Versions before 2.0 kept everything in a Derby database `cities.db` in whatever folder the app was started from. That data is not migrated: version 2.0 starts with the bundled list and default settings.

## Parameters

| Job | Parameter | Default | Notes |
|---|---|---|---|
| `planethour dev` | `JAR_PATH` | `/home/sounak/Documents/NetBeansProjects/PlanetHour/target/planethour.jar` | The IDE-built jar on the lin host. The working copy two folders above it is used for the commit info and the unit tests. On the job's first run, before Jenkins has registered the parameter, the same path is used as a fallback. |

The release job has no parameters. Its version comes from `pom.xml`.

## Build infrastructure

Shared with DeskStop; full details, the `docker run` command, secret rotation and backups are in [desktime's BUILD.md](https://github.com/sounak3/desktime/blob/main/BUILD.md).

| Item | Value |
|---|---|
| Controller | Docker container `jenkins` on dell5558, `jenkins/jenkins:2.580.1-lts`, `http://192.168.1.14:8080/`, `JENKINS_HOME` = `/home/sounak/container/jenkins_home` |
| `lin` | dell5558 itself (Ubuntu 24.04), inbound agent run by the systemd user service `jenkins-agent-lin` |
| `mac` | `macos.local`, macOS 13 on Intel, SSH launcher. DMGs are Intel-only. |
| `win` | `winos.local`, Windows 7 VM, inbound agent started by the scheduled task `JenkinsWinAgent`. Comodo Internet Security runs there. |
| Backups | Nightly `jenkins-backup.timer` on dell5558 covers both projects' jobs and archived installers |

## Software required on each agent

All three agents need **JDK 21 or newer** (`maven.compiler.release=21`, and `jlink --compress=zip-6` only exists from JDK 21).

| Agent | Needs | Used by |
|---|---|---|
| `lin` | JDK 21 (`java`, `jdeps`, `jlink`, `jpackage`), Maven, git, `dpkg-deb` and `fakeroot` (for `--type deb`), `unzip`, `sha256sum`, `rsync`, `~/Desktop` | release: Build jar, Ubuntu; dev: Collect jar, lin, Unit tests |
| `win` | JDK 21 with `JAVA_HOME` set system-wide, `java` on `PATH`, .NET Framework 4.8 (for WiX 3.14), access to `github.com` (WiX is downloaded on every release), `certutil` (built in), `%USERPROFILE%\Desktop` | release: Windows; dev: win |
| `mac` | JDK 21 on the non-interactive SSH `PATH`, `hdiutil` and `shasum` (built in), no idle system sleep, `~/Desktop` | release: Mac OS; dev: mac |

> **Comodo on `win`:** a Windows step that hangs with no output, or a file that vanishes, is usually Comodo blocking a new program or `.bat` file. Allow folders, not single files: `c:\jenkins\` (including `workspace`) and the JDK folder.

## Jenkins configuration

### Plugins the pipelines depend on

Pipeline and Pipeline: Declarative, Git, JUnit, Workspace Cleanup (`cleanWs`), File Operations (WiX download on Windows), Pipeline: Basic Steps and Nodes and Processes (`stash`, `archiveArtifacts`, `timeout`), SSH Build Agents (the `mac` agent). These are the same plugins DeskStop uses.

### Job setup

Both jobs read their pipeline from the repository:

*Configure → Pipeline → Definition: **Pipeline script from SCM*** → SCM: Git → Repository URL `https://github.com/sounak3/planethour.git`, Credentials: *none* (public repository) → Branch `*/main` → Script Path `Jenkinsfile` (release) or `Jenkinsfile.dev` (dev) → *Lightweight checkout* ✓.

`planethour dev` keeps 30 builds, never runs two at once and has a 15-second quiet period; these come from its `options { }` block.

### Dev trigger (on dell5558)

A systemd path unit watches the jar and asks Jenkins to start `planethour dev`:

| File | Role |
|---|---|
| `~/.config/systemd/user/planethour-dev-trigger.path` | watches `target/planethour.jar` |
| `~/.config/systemd/user/planethour-dev-trigger.service` | runs `curl -X POST …/job/planethour%20dev/buildWithParameters` (a job with parameters rejects plain `/build` with HTTP 400); skipped if the jar doesn't exist, for example right after `mvn clean` |
| `~/.config/desktime-dev-trigger.env` (mode 600) | `JENKINS_USER` and `JENKINS_TOKEN` (a Jenkins API token), shared with DeskStop's trigger |

To check that it works: `systemctl --user start planethour-dev-trigger.service`, then `journalctl --user -u planethour-dev-trigger.service -n 20`

A single Maven build writes the jar more than once (the shade plugin replaces it). The job's 15-second quiet period merges those triggers into one build. Each deploy stage times out after 5 minutes, so a powered-off VM only fails its own branch while the other machines still receive the jar.

**While the trigger is enabled, every `mvn package` in this working copy pushes a jar to all three test machines.** Experiment in a scratch copy (`rsync -a --exclude /target --exclude /.git --exclude /archive`) instead.

## Known issues and recommendations

1. **11 bundled places have minute values of 60 or more** in their latitude or longitude (for example `04189`). The editor rejects such values, so those places can't be saved again unchanged. The hour calculation still works, slightly off. Correct them from a reliable source.
2. **The UI is updated from a background thread.** The one-second updater thread changes Swing components outside the event dispatch thread. It has worked for years but can cause rare repaint glitches; move those updates into `SwingUtilities.invokeLater`.
3. **Publishing is manual.** Jenkins only archives the installers. Uploading them to GitHub Releases happens outside Jenkins.
4. **Installers are unsigned.** The MSI has no Authenticode signature (SmartScreen will warn). The DMG is not notarized: open it with right-click → *Open*.
5. **The DEB's maintainer field reads `Sounak Choudhury <Unknown>`.** Add `--linux-deb-maintainer <email>` to the Ubuntu stage if you want a contact address in the package.
6. **The icon source is 256×256.** The 512 and 1024 px macOS sizes are upscaled and slightly soft. Replace `art/PlanetHour-icon.png` with larger art and regenerate when available.
7. **Test machines need Java 21**, and **Windows 7 is unsupported by Java 21.** See desktime's BUILD.md.
8. **A dev build started while a release is packaging can end ABORTED.** Each agent has one executor, and each deploy stage's 5-minute timeout includes waiting for it (that is what lets a powered-off VM fail only its own copy). If the release holds `mac` or `win` for longer, the dev build times out before its unit tests. Rerun it when the release has finished.
9. **What you test isn't exactly what you ship.** `planethour dev` tests your IDE build, which may include uncommitted changes; the release rebuilds from `main`. The build descriptions record the commit.
