package de.konstanz.schulen.suso.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.data.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;
import io.fabric.sdk.android.Fabric;

import static de.konstanz.schulen.suso.util.SharedPreferencesManager.*;

public class LoadingActivity extends AppCompatActivity implements
        Runnable
{
    private static final String TAG = LoadingActivity.class.getSimpleName();

    private ProgressBar spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Thread welcomeThread = new Thread(this);
        checkForPlayServices(welcomeThread);

        spinner = (ProgressBar)findViewById(R.id.loading_progressbar);
        spinner.setVisibility(View.VISIBLE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForPlayServices(null);
    }

    @Override
    public void run() {

        Class targetClass;

        // Login is not saved in SharedPrefs or login is wrong -> no direct login
        targetClass = LoginActivity.class;

        Log.d(TAG, "Checking login...");
        if(AccountManager.getInstance().isValidLogin(this))
        {
            // Login is saved in SharedPrefs -> direct login
            targetClass = MainActivity.class;
        }

        Log.d(TAG, "Starting " + targetClass.getSimpleName());
        Intent i = new Intent(LoadingActivity.this, targetClass);
        startActivity(i);
        finish();

    }

    private void checkForPlayServices(final Thread thread){
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(thread != null)
                    thread.start();
            }
        });
    }

}
