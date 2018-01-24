package de.konstanz.schulen.suso;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import de.konstanz.schulen.suso.util.FabricHandler;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;

public class SusoApplication extends Application {

    public static final String TAG = SusoApplication.class.getSimpleName();
    public static String API_ENDPOINT = null;



    @Override
    public void onCreate() {

        Log.i(TAG, "Starting Suso App...");

        API_ENDPOINT = getString(R.string.base_url);

        SharedPreferencesManager.initialize(this);

        String testLab = Settings.System.getString(getContentResolver(), "firebase.test.lab");

        if(testLab != null && testLab.equals("true") || BuildConfig.DEBUG_MODE)
        {
            Log.i(TAG, "Welcome Testing! Fabric is disabled...");
        }

        FabricHandler.initialize(this);

        CharSequence text = "Version: " + BuildConfig.VERSION_NAME + "/" + BuildConfig.GIT_HASH + "(" + BuildConfig.GIT_COMMITS + ")";

        if(BuildConfig.DEBUG_MODE)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
        }

        Log.i(TAG, text.toString());

        Log.i(TAG, "Successfully started Suso App");
        super.onCreate();
    }
}
