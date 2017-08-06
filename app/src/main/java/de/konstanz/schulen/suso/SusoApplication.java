package de.konstanz.schulen.suso;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;
import io.fabric.sdk.android.Fabric;

public class SusoApplication extends Application {
    @Override
    public void onCreate() {

        SharedPreferencesManager.initialize(this);


        if(!BuildConfig.DEBUG_MODE) {
            Fabric.with(this, new Crashlytics());
        }

        if(BuildConfig.DEBUG_MODE)
        {
            Context context = getApplicationContext();
            CharSequence text = "Version: " + BuildConfig.VERSION_NAME + "/" + BuildConfig.GIT_HASH + "(" + BuildConfig.GIT_COMMITS + ")";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
        }

        AccountManager.getInstance().loadFromSharedPreferences(this);

        FirebaseHandler.getInstance().setEndPoint(getString(R.string.base_url));
        FirebaseHandler.getInstance().startup(this);

        super.onCreate();
    }
}
