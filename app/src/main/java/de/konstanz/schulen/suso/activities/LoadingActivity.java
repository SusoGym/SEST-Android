package de.konstanz.schulen.suso.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.SusoApplication;
import de.konstanz.schulen.suso.data.fetch.DownloadManager;
import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.DebugUtil;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;

public class LoadingActivity extends AppCompatActivity implements
        Runnable {
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


        de.konstanz.schulen.suso.data.fetch.DownloadManager.initializeInstance(this);

        FirebaseHandler.getInstance().setEndPoint(SusoApplication.API_ENDPOINT);
        FirebaseHandler.getInstance().startup();

        Class targetClass;

        // Login is not saved in SharedPrefs or login is wrong -> no direct login
        targetClass = LoginActivity.class;

        DebugUtil.infoLog(TAG, "Checking login...");
        if(DownloadManager.getInstance().isLoggedIn())
        {
            // Login is saved in SharedPrefs -> direct login
            targetClass = MainActivity.class;
        }else{
            //Enable offline viewing of substitution plan
            if(SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_SUBSITUTIONPLAN_DATA, null)!=null
                    && SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_USERNAME, null)!=null){
                targetClass = MainActivity.class;
            }
        }

        DebugUtil.infoLog(TAG, "Starting " + targetClass.getSimpleName());
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
