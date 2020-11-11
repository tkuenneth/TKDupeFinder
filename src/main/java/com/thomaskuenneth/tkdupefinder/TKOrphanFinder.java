/*
 * @(#)TKOrphanFinder.java	0.02a, 2019/02/17
 * Copyright 2011 - 2016 Thomas Kuenneth
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
 * Compares two directory trees and lists orphan files.
 *
 * @author Thomas Kuenneth
 * @version 0.02a, 2019/02/17
 */
public class TKOrphanFinder extends AbstractDupeFinder {

    /**
     * Constructs and initializes a TKOrphanFinder instance.
     *
     * @throws NoSuchAlgorithmException
     */
    public TKOrphanFinder() throws NoSuchAlgorithmException {
        super();
    }

    /**
     * Removes all entries from the internal list that have exactly two
     * occurrences.
     */
    public void removePairs() {
        String[] checksums = getChecksums();
        for (String checksum : checksums) {
            List<File> files = mapChecksumFiles.get(checksum);
            if (files.size() == 2) {
                mapChecksumFiles.remove(checksum);
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: TKOrphanFinder <dir1> <dir2>");
                System.exit(1);
            }
            TKOrphanFinder of = new TKOrphanFinder();
            System.out.println("scanning " + args[0]);
            of.scanDir(args[0], true);
            System.out.println("scanning " + args[1]);
            of.scanDir(args[1], true);
            System.out.println("finding orphanss");
            of.removePairs();
            String[] checksums = of.getChecksums();
            for (String checksum : checksums) {
                List<File> files = of.getFiles(checksum);
                files.stream().forEach((f) -> {
                    System.out.println(f.getAbsolutePath());
                });
            }
            System.out.println("finished");
        } catch (NoSuchAlgorithmException thr) {
            System.err.println(thr.getLocalizedMessage());
        }
    }
}
