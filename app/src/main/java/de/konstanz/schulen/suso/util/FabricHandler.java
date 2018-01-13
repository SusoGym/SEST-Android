package de.konstanz.schulen.suso.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

import io.fabric.sdk.android.Fabric;


public class FabricHandler {

    public static boolean USE_FABRIC = true;

    public static void initialize(Context ctx) {
        if (USE_FABRIC) {
            Fabric.with(ctx, new Crashlytics());
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

}
