package de.konstanz.schulen.suso.util;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import de.konstanz.schulen.suso.BuildConfig;
import io.fabric.sdk.android.Fabric;

public class DebugUtil {

    public static void infoLog(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            logPublic(Log.INFO, tag, msg);
        } else {
            logSilent(msg);
        }
    }

    public static void errorLog(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            logPublic(Log.ERROR, tag, msg);
        } else {
            logSilent(msg);
        }
    }


    public static void logException(String tag, Throwable able, String desc)
    {
        if(Fabric.isInitialized())
        {
            Crashlytics.logException(able);
        }
        errorLog(tag, "Encountered exception ( " + desc +" ): " + able.getMessage());
    }

    public static void logSilent(String msg) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(msg);
        }

    }

    public static void logPublic(int priority, String tag, String msg) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(priority, tag, msg);
        } else {
            switch (priority) {
                case Log.ERROR:
                    Log.e(tag, msg);
                    break;
                case Log.INFO:
                    Log.i(tag, msg);
                    break;
                case Log.WARN:
                    Log.w(tag, msg);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg);
                    break;
                case Log.VERBOSE:
                    Log.v(tag, msg);
                    break;
            }
        }
    }

}
