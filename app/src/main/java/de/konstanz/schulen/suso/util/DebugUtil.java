package de.konstanz.schulen.suso.util;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.data.fetch.DownloadManager;
import io.fabric.sdk.android.Fabric;

public class DebugUtil {

    private static final boolean DEBUG = BuildConfig.DEBUG && false;
    private static final String TAG = DebugUtil.class.getSimpleName();

    public static void infoLog(String tag, String msg) {
        msg = censor(msg);
        if (BuildConfig.DEBUG) {
            logPublic(Log.INFO, "d_" + tag, msg);
        } else {
            logSilent(msg);
        }
    }

    public static void debugLog(String tag, String msg) {
        msg = censor(msg);
        if (BuildConfig.DEBUG) {
            logPublic(Log.DEBUG, "d_" + tag, msg);
        } else {
            logSilent(msg);
        }
    }

    public static void errorLog(String tag, String msg) {
        msg = censor(msg);
        if (BuildConfig.DEBUG) {
            logPublic(Log.ERROR, "d_" + tag, msg);
        } else {
            logSilent(msg);
        }
    }

    public static String censor(String msg) {
        if(DownloadManager.getInstance() != null) {
            String pwd = DownloadManager.getInstance().getPassword();
            if (pwd == null) {
                pwd = SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_PASSWORD, null);
            }
            if (pwd == null) {
                return msg;
            }

            try {
                msg = msg.replaceAll(Pattern.quote(pwd), Matcher.quoteReplacement("<pwd>")).replaceAll(URLEncoder.encode(pwd, "UTF-8"), Matcher.quoteReplacement("<pwd>"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return msg;
    }


    public static void logException(String tag, Throwable able, String desc) {
        desc = censor(desc);
        String message = able.getMessage();
        if (message != null) {
            message = censor(message);
        }
        if (Fabric.isInitialized()) {
            Crashlytics.logException(able);
        }
        errorLog(tag, "Encountered exception ( " + desc + " ): " + able.getClass().getSimpleName() + (message == null ? "" : (": " + message)));
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
