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

public class ProgramLock {
    private String appName;
    private File file;
    private FileChannel channel;
    private FileLock lock;

    public ProgramLock(String appName) {
        this.appName = appName;
    }

    public boolean isAppActive() {
        try {
            file = new File(System.getProperty("user.home"), appName + ".tmp");
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
                    // destroy the lock when the JVM is closing
                    @Override
                    public void run() {
                        closeLock();
//                        System.out.println("Closing lock! Program shutdown.");
                        deleteFile();
                    }
                });
            return false;
        }
        catch (Exception e) {
            closeLock();
//            System.out.println("Closing lock! Cannot open file.");
            return true;
        }
    }

    private void closeLock() {
        try { lock.release();  }
        catch (Exception e) {  }
        try { channel.close(); }
        catch (Exception e) {  }
    }

    private void deleteFile() {
        try { file.delete(); }
        catch (Exception e) { }
    }
}