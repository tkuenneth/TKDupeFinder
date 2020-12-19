/*
 * @(#)AbstractDupeFinder.java	0.01a, 2019/02/17
 * Copyright 2008 Thomas Kuenneth
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.thomaskuenneth.TKJMD5;

/**
 * An abstract base class, used to find duplicate and orphans files
 *
 * @author Thomas Kuenneth
 * @version 0.02, 2020/12/19
 */
public abstract class AbstractDupeFinder {

    // maps MD5 digests to lists of files
    final HashMap<String, List<File>> mapChecksumFiles;

    // used to compute MD5 checksums
    final TKJMD5 md;

    AbstractDupeFinder() throws NoSuchAlgorithmException {
        mapChecksumFiles = new HashMap<>();
        md = new TKJMD5(8192 * 1024);
    }

    /**
     * Scans a directory and, optionally, its subdirectories. For all normal
     * files an MD5 digest is computed. Files with an equal digest are grouped.
     *
     * @param basedir base directory
     * @param recursive scan subdirectories
     * @see #scanFile(File)
     */
    void scanDir(String basedir, boolean recursive) {
        scanDir(new File(basedir), recursive);
    }

    /**
     * Scans a directory and, optionally, its subdirectories. For all normal
     * files an MD5 digest is computed. Files with an equal digest are grouped.
     *
     * @param basedir base directory
     * @param recursive scan subdirectories
     * @see #scanFile(File)
     */
    public void scanDir(File basedir, boolean recursive) {
        File[] files = basedir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (recursive && f.isDirectory()) {
                    scanDir(f, recursive);
                } else if (f.isFile()) {
                    scanFile(f);
                }
            }
        }
    }

    /**
     * Calculates the MD5 digest of a file.
     *
     * @param file the file
     */
    void scanFile(File file) {
        if (file.length() == 0) {
            System.err.println(String.format("%s is 0 bytes", file.getAbsolutePath()));
            return;
        }
        byte[] digest = md.getChecksum(file);
        if (digest != null) {
            String key = TKJMD5.toString(digest);
            List<File> value;
            if (mapChecksumFiles.containsKey(key)) {
                value = mapChecksumFiles.get(key);
            } else {
                value = new ArrayList<>();
                mapChecksumFiles.put(key, value);
            }
            value.add(file);
        } else {
            System.err.println("error while calculationg hash for "
                    + file.getName());
        }
    }

    /**
     * The checksums of all files found while traversing the base directory
     *
     * @return array of strings with checksums
     */
    String[] getChecksums() {
        String[] checksums = new String[mapChecksumFiles.size()];
		Set<String> set = mapChecksumFiles.keySet();
        set.toArray(checksums);
        return checksums;
    }

    /**
     * Get a list of files with the same checksum.
     *
     * @param key checksum
     * @return list of files
     */
    List<File> getFiles(String key) {
        return mapChecksumFiles.get(key);
    }

    /**
     * Clears the map that maps checksums to files.
     */
    public void clear() {
        mapChecksumFiles.clear();
    }
}
