/*
 * @(#)TKDupeFinder.java	0.02, 2016/06/26
 * Copyright 2008 - 2016 Thomas Kuenneth
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thomaskuenneth.tkdupefinder;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Finds duplicate files.
 *
 * @author Thomas Kuenneth
 * @version 0.02, 2016/06/26
 */
public class TKDupeFinder extends AbstractDupeFinder {

    /**
     * Constructs and initializes a TKDupeFinder instance.
     *
     * @throws NoSuchAlgorithmException
     */
    public TKDupeFinder() throws NoSuchAlgorithmException {
        super();
    }

    /**
     * Removes all files from the internal list that occur once.
     */
    public void removeSingles() {
        String[] checksums = getChecksums();
        for (String checksum : checksums) {
            List<File> files = mapChecksumFiles.get(checksum);
            if (files.size() < 2) {
                mapChecksumFiles.remove(checksum);
            }
        }
    }

    public boolean deleteFile(String key, File file) {
        List<File> v = getFiles(key);
        if (v != null) {
            if (v.remove(file)) {
                return file.delete();
            }
        }
        return false;
    }

    public static void main(String[] args) {
        TKDupeFinder df;
        try {
            df = new TKDupeFinder();
            System.out.println("scanning " + args[0]);
            df.scanDir(args[0], true);
            System.out.println("finding duplicates");
            df.removeSingles();
            String[] checksums = df.getChecksums();
            for (String checksum : checksums) {
                List<File> v = df.getFiles(checksum);
                System.out.println(checksum + ": " + v.size());
                v.stream().forEach((f) -> {
                    System.out.println(f.getAbsolutePath());
                });
            }
        } catch (Throwable thr) {
            System.err.println(thr.getLocalizedMessage());
        }
    }
}
