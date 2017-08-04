package de.konstanz.schulen.suso;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class SusoApplication extends Application {
    @Override
    public void onCreate() {

        if(BuildConfig.DEBUG_MODE)
        {
            Context context = getApplicationContext();
            CharSequence text = "Version: " + BuildConfig.VERSION_NAME + "/" + BuildConfig.GIT_HASH + "(" + BuildConfig.GIT_COMMITS + ")";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
        }

        super.onCreate();
    }
}
