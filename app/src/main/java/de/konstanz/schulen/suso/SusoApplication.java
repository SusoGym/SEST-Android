package de.konstanz.schulen.suso;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;
import io.fabric.sdk.android.Fabric;

public class SusoApplication extends Application {

    public static final String TAG = SusoApplication.class.getSimpleName();

    public static boolean USE_FABRIC = !BuildConfig.DEBUG_MODE;

    @Override
    public void onCreate() {

        Log.i(TAG, "Starting Suso App...");

        SharedPreferencesManager.initialize(this);

        String testLab = Settings.System.getString(getContentResolver(), "firebase.test.lab");

        if(testLab != null && testLab.equals("true"))
        {
            USE_FABRIC = false; // we are in an testing environment
            Log.i(TAG, "Welcome Testing!");
        }

        if(USE_FABRIC) {
            Fabric.with(this, new Crashlytics());
        }

        CharSequence text = "Version: " + BuildConfig.VERSION_NAME + "/" + BuildConfig.GIT_HASH + "(" + BuildConfig.GIT_COMMITS + ")";

        if(BuildConfig.DEBUG_MODE)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
        }

        Log.i(TAG, text.toString());

        AccountManager.getInstance().loadFromSharedPreferences(this);

        FirebaseHandler.getInstance().setEndPoint(getString(R.string.base_url));
        FirebaseHandler.getInstance().startup(this);

        Log.i(TAG, "Successfully started Suso App");
        super.onCreate();
    }
}
