package com.thomaskuenneth;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WindowsRegistry {

    private static final Logger LOGGER = Logger.getLogger(WindowsRegistry.class.getPackageName());

    public static String getEntry(String key, String value, String type) {
        StringBuilder sbIS = new StringBuilder();
        StringBuilder sbES = new StringBuilder();
        String result = null;
        try {
            String cmd = String.format("reg query \"%s\" /v %s", key, value);
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            }
            boolean hasData = true;
            int isData;
            int esData;
            while (hasData) {
                if (is.available() > 0) {
                    isData = is.read();
                } else {
                    isData = -1;
                }
                if (es.available() > 0) {
                    esData = es.read();
                } else {
                    esData = -1;
                }
                if (isData != -1) {
                    sbIS.append((char) isData);
                }
                if (esData != -1) {
                    sbES.append((char) esData);
                }
                hasData = isData != -1 || esData != -1;
            }
            result = sbIS.toString();
            int pos = result.indexOf(type);
            if (pos >= 0) {
                result = result.substring(pos + type.length()).trim();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
        if (sbES.length() > 0) {
            LOGGER.log(Level.SEVERE, sbES.toString());
        }
        return result;
    }
}