/*
 * File: ProgramLock.java in java package com.sounaks.planethour is part of application
 * PlanetHour v1.0 - Planetary hour calculation software
 * Copyright (C) 2014 Sounak Choudhury
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
 * 
 * Contact E-mail: sounak_s@rediffmail.com
 */
package com.sounaks.planethour;

/**
 *
 * @author Sounak Choudhury
 */
import java.io.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgramLock {
    static final String FILE_NAME = "planethour.lock";
    private File file;
    private FileChannel channel;
    private FileLock lock;

    /**
     * Checks whether another PlanetHour runs for this user, and if not, holds the lock in
     * ~/.planethour/ until this program exits.
     * @return true if another instance holds the lock
     */
    public boolean isAppActive() {
        try {
            Files.createDirectories(UserFiles.dir());
            file = UserFiles.file(FILE_NAME).toFile();
            channel = new RandomAccessFile(file, "rw").getChannel();

            try {
                lock = channel.tryLock();
            }
            catch (OverlappingFileLockException e) {
                // already locked
                closeLock();
//                System.out.println("Closing lock! Overlapping file lock.");
                return true;
            }

            if (lock == null) {
                closeLock();
//                System.out.println("Closing lock! Null lock.");
                return true;
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                    // release the lock when the JVM is closing; the file stays, so no other instance can lock a deleted file
                    @Override
                    public void run() {
                        closeLock();
                    }
                });
            return false;
        }
        catch (Exception e) {
            // Without a usable lock file, starting is better than refusing to run.
            closeLock();
            Logger.getLogger(ProgramLock.class.getName()).log(Level.WARNING, "Cannot check for another running PlanetHour", e);
            return false;
        }
    }

    void closeLock() {
        try { lock.release();  }
        catch (Exception e) {  }
        try { channel.close(); }
        catch (Exception e) {  }
    }
}