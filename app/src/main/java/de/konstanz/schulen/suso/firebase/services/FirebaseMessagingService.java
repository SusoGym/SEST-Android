package de.konstanz.schulen.suso.firebase.services;


import com.google.firebase.messaging.RemoteMessage;

import de.konstanz.schulen.suso.firebase.FirebaseHandler;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService
{

    private static final String TAG = FirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        FirebaseHandler.getInstance().sendMessageReceived(remoteMessage);
    }
}
