package de.konstanz.schulen.suso.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class IOUtility {

    private static final String TAG = IOUtility.class.getSimpleName();

    public static String readFromURL(URL url) throws IOException {

        DebugUtil.infoLog(TAG, "Reading content from " + url);
        BufferedReader is = new BufferedReader(new InputStreamReader(url.openStream()));

        String temp;
        StringBuilder data = new StringBuilder();
        while ((temp = is.readLine()) != null) {
            data.append(temp);
        }
        DebugUtil.debugLog(TAG, "Response: " + data.toString());
        return data.toString();
    }

}
