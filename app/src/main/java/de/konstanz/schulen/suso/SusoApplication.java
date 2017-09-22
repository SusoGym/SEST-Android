package de.konstanz.schulen.suso;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.AccountManager;
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
            FabricHandler.USE_FABRIC = false; // we are in an testing environment
            Log.i(TAG, "Welcome Testing!");
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

        AccountManager.getInstance().loadFromSharedPreferences(this);

        FirebaseHandler.getInstance().setEndPoint(API_ENDPOINT);
        FirebaseHandler.getInstance().startup(this);

        Log.i(TAG, "Successfully started Suso App");
        super.onCreate();
    }
}
