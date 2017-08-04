package de.konstanz.schulen.suso.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseHandler
{

    private static final String TAG = FirebaseHandler.class.getSimpleName();
    private static FirebaseHandler instance;

    public static FirebaseHandler getInstance(){
        return instance == null ? instance = new FirebaseHandler() : instance;
    }

    private FirebaseHandler(){}

    public void sendRegistrationToServer(String token)
    {
        Log.d(TAG, "Refreshed token: " + token);
    }

    public void sendMessageReceived(RemoteMessage msg)
    {

        Log.d(TAG, "Incoming message from " + msg.getFrom() + ", payload: " + msg.getData() + (msg.getNotification() != null ? ", title: " + msg.getNotification().getTitle() + ", body: " + msg.getNotification().getBody() : ""));

        if(msg.getNotification() == null)
        { // data incoming

        } else {
            //notification incoming
        }

    }


    public void startup()
    {
        if(FirebaseInstanceId.getInstance().getToken() != null){
            Log.d(TAG, "Firebase-Token is: " + FirebaseInstanceId.getInstance().getToken());
        }
    }

}
