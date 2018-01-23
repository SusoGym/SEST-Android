package de.konstanz.schulen.suso.util;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

import java.net.InetAddress;

import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.Fabric;


public class FabricHandler {

    public static boolean USE_FABRIC = true;
    public static final String TAG = FabricHandler.class.getSimpleName();

    public static void initialize(Context ctx) {
        if (isInternetAvailable()) {
            final Fabric fabric = new Fabric.Builder(ctx)
                    .kits(new Crashlytics())
                    .debuggable(BuildConfig.DEBUG)
                    .build();
            Fabric.with(fabric);
        } else {
            Log.e(TAG, "Could not connect to fabric as there is not internet connection!");
        }

    }

    public static void logCustomEvent(CustomEvent event) {
        if (!USE_FABRIC) {
            return;
        }

        Answers.getInstance().logCustom(event);
    }

    public static void logLoginEvent(LoginEvent event) {
        if (!USE_FABRIC) {
            return;
        }

        Answers.getInstance().logLogin(event);
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

}
