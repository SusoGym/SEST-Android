package de.konstanz.schulen.suso.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.Fabric;


public class FabricHandler {

    public static void initialize(Context ctx) {

        final Fabric fabric = new Fabric.Builder(ctx)
                .kits(new Crashlytics())
                .debuggable(BuildConfig.DEBUG)
                .build();

        Fabric.with(fabric);
    }

    public static void logCustomEvent(CustomEvent event) {
        Answers.getInstance().logCustom(event);
    }

    public static void logLoginEvent(LoginEvent event) {
        Answers.getInstance().logLogin(event);
    }

}
