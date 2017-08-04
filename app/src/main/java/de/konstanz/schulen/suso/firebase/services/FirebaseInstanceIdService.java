package de.konstanz.schulen.suso.firebase.services;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import de.konstanz.schulen.suso.firebase.FirebaseHandler;

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService
{

    private static final String TAG = FirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseHandler.getInstance().sendRegistrationToServer(refreshedToken);

    }
}
