/*
 * @(#)TKJMD5.java	0.01b, 2016/06/25
 * Copyright 2007 - 2016 Thomas Kuenneth
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
package com.thomaskuenneth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calculates the MD5 hash for a given file.
 *
 * @author Thomas Kuenneth
 * @version 0.01b, 2016/06/25
 */
public class TKJMD5 {

    // determines the message digest algorithm
    private static final String MD5 = "MD5";

    // default buffer length
    private static final int DEFAULT_BUFFER_LENGTH = 32768;

    // size of the buffer used for file operations
    private final int buflen;

    // the file buffer
    private final byte[] buffer;

    // used to compute the checksum
    private final MessageDigest md;

    public TKJMD5() throws NoSuchAlgorithmException {
        this(DEFAULT_BUFFER_LENGTH);
    }

    public TKJMD5(int len) throws NoSuchAlgorithmException {
        buffer = new byte[len];
        buflen = len;
        md = MessageDigest.getInstance(MD5);
    }

    public byte[] getChecksum(String filename) {
        return getChecksum(new File(filename));
    }

    public byte[] getChecksum(File file) {
        byte[] result = null;
        if (file.isFile()) {
            int filelen = (int) file.length();
            int read = 0;
            int num;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                while ((num = (filelen - read)) > 0) {
                    if (num > buflen) {
                        num = buflen;
                    }
                    num = fis.read(buffer, 0, num);
                    md.update(buffer, 0, num);
                    read += num;
                }
                result = md.digest();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
                md.reset();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        System.err.println(e.getLocalizedMessage());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates the String representation of a given digest.
     *
     * @param digest an already computed digest
     * @return its String representation
     */
    public static String toString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        if (digest != null) {
            for (int i = 0; i < digest.length; i++) {
                long l = digest[i] & 0xff;
                if (l < 16) {
                    sb.append('0');
                }
                sb.append(Long.toHexString(l));
            }
        }
        return sb.toString();
    }

    /**
     * This is the obligatory entry point if this class is used as a stand-alone
     * program. Computes and prints the MD5 checksums of all files that are
     * passed via the command line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            TKJMD5 md = new TKJMD5();
            if (args.length == 0) {
                System.out.println("Usage: TKJMD5 file1 [file2] [...]");
                System.exit(1);
            } else {
                byte[] digest;
                for (String arg : args) {
                    digest = md.getChecksum(arg);
                    System.out.println(arg + ": " + toString(digest));
                }
                System.exit(0);
            }
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.getLocalizedMessage());
            System.exit(1);
        }
    }
}
