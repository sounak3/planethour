/*
 * File: UserFiles.java in java package com.sounaks.planethour is part of application
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * The per-user data folder ~/.planethour/, with safe saves and loading that falls back to a backup.
 * Nothing is ever written next to the jar or into the folder the app was started from.
 *
 * @author Sounak Choudhury
 */
final class UserFiles {

    static final String DIR_NAME = ".planethour";

    interface Writer {
        void write(OutputStream out) throws IOException;
    }

    interface Reader<T> {
        T read(InputStream in) throws IOException;
    }

    private UserFiles() {
    }

    /**
     * The per-user data folder. Reads user.home on every call, so tests can point it at a temporary folder.
     */
    static Path dir() {
        return Paths.get(System.getProperty("user.home"), DIR_NAME);
    }

    static Path file(String name) {
        return dir().resolve(name);
    }

    static Path backupOf(Path file) {
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    /**
     * Writes to a temporary file next to the target, keeps the current target as .bak and then moves
     * the temporary file into place, so a failed or interrupted save never damages the existing file.
     */
    static void save(Path target, Writer writer) throws IOException {
        Path folder = target.toAbsolutePath().getParent();
        Files.createDirectories(folder);
        Path temp = Files.createTempFile(folder, target.getFileName().toString(), ".tmp");
        try {
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(temp))) {
                writer.write(out);
            }
            if (Files.isRegularFile(target)) {
                Files.copy(target, backupOf(target), REPLACE_EXISTING);
            }
            try {
                Files.move(temp, target, REPLACE_EXISTING, ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Reads the file, or its .bak when the file is missing or cannot be read.
     * @return what the reader returned, or null when neither file could be read
     */
    static <T> T load(Path file, Reader<T> reader) {
        for (Path candidate : new Path[]{file, backupOf(file)}) {
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try (InputStream in = new BufferedInputStream(Files.newInputStream(candidate))) {
                return reader.read(in);
            } catch (IOException | RuntimeException e) {
                Logger.getLogger(UserFiles.class.getName()).log(Level.WARNING, "Cannot read " + candidate, e);
            }
        }
        return null;
    }
}
