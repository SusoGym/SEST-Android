package de.konstanz.schulen.suso;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class SusoApplication extends Application {
    @Override
    public void onCreate() {
        Context context = getApplicationContext();
        CharSequence text = "Version: " + BuildConfig.VERSION_NAME + "/" + BuildConfig.GIT_HASH;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);


        if(BuildConfig.DEBUG_MODE)
        {
            toast.show();
        }

        super.onCreate();
    }
}
