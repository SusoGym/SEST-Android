package de.konstanz.schulen.suso.util;

import android.util.Log;

import de.konstanz.schulen.suso.BuildConfig;

public class DebugUtil {

    public static void infoLog(String tag, String msg) {

        if (BuildConfig.DEBUG_MODE) {
            Log.i(tag, msg);
        }

    }

    public static void errorLog(String tag, String msg) {

        if (BuildConfig.DEBUG_MODE) {
            Log.e(tag, msg);
        }

    }

}
