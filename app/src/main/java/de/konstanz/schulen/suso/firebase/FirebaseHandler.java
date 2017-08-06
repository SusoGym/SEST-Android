package de.konstanz.schulen.suso.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.IOUtility;

public class FirebaseHandler {

    private static final String TAG = FirebaseHandler.class.getSimpleName();
    private static FirebaseHandler instance;
    private String endPoint;
    private String token;

    private String registerVerification2;
    private String deleteVerification2;

    public static FirebaseHandler getInstance() {
        return instance == null ? instance = new FirebaseHandler() : instance;
    }

    public void setEndPoint(String base) {
        this.endPoint = base + "api/index.php";
    }

    private FirebaseHandler() {
    }

    public void sendRegistrationToServer(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        this.token = token;
        registerToken();

    }

    public void sendMessageReceived(RemoteMessage msg) {

        Log.d(TAG, "Incoming message from " + msg.getFrom() + ", payload: " + msg.getData() + (msg.getNotification() != null ? ", title: " + msg.getNotification().getTitle() + ", body: " + msg.getNotification().getBody() : ""));

        if (msg.getNotification() == null) { // data incoming

            Map<String, String> data = msg.getData();

            if (data.containsKey("event") && data.get("event").equals("verify")) {
                if (data.get("type").equals("register")) {
                    registerVerification2 = data.get("verification");
                } else if (data.get("type").equals("delete")) {
                    deleteVerification2 = data.get("verification");
                }

            }

        } else {
            //notification incoming
        }

    }


    public void startup(final Context ctx) {
        if (FirebaseInstanceId.getInstance().getToken() != null) {
            Log.d(TAG, "Firebase-Token is: " + FirebaseInstanceId.getInstance().getToken());
            token = FirebaseInstanceId.getInstance().getToken();

            new Thread()
            {
                @Override
                public void run() {
                    try {
                        boolean registered = isRegistered(token);
                        boolean validLogin = AccountManager.getInstance().isValidLogin(ctx);
                        Log.d(TAG, "Token registered: " + registered + "; Valid login: " + validLogin);
                        if (!registered && validLogin) {
                            registerToken();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
    }


    public void registerToken() {

        Log.i(TAG, "Trying to register this device / account combination to the main server");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    int userId = AccountManager.getInstance().getAccountId();
                    int userType = AccountManager.getInstance().getAccountType();

                    boolean success = false;

                    while (!success) {

                        registerVerification2 = null;

                        String verification1 = sendRegisterRequest(userId, userType, token);
                        Log.d(TAG, "Verification Code 1: " + verification1);
                        while (registerVerification2 == null) ;
                        Log.d(TAG, "Verification Code 2: " + registerVerification2);

                        Log.d(TAG, "Sending verification request with verify1: " + verification1 + " / verify2: " + registerVerification2);
                        success = sendRegisterToken(userId, userType, token, verification1, registerVerification2);

                    }

                    Log.i(TAG, "Successfully registered this device with userId " + userId + " of type " + userType);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        t.start();

    }

    public void deleteToken() {

        Log.i(TAG, "Trying to delete this device / account combination on the main server");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    if (!isRegistered(token))
                        return;

                    boolean success = false;

                    while (!success) {

                        deleteVerification2 = null;

                        String verification1 = sendDeletionRequest(token);
                        Log.d(TAG, "Verification Code 1: " + verification1);
                        while (deleteVerification2 == null) ;
                        Log.d(TAG, "Verification Code 2: " + deleteVerification2);

                        Log.d(TAG, "Sending verification request with verify1: " + verification1 + " / verify2: " + deleteVerification2);
                        success = sendDeletionToken(token, verification1, deleteVerification2);

                    }


                    Log.i(TAG, "Successfully deleted this devices' fcm token on the main server");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        t.start();

    }

    public boolean isRegistered(String token) throws IOException, JSONException {

        URL url = new URL(endPoint + "?action=checkRegistered&fcm_token=" + token);
        String resp = IOUtility.readFromURL(url);

        JSONObject obj = new JSONObject(resp);
        return obj.getJSONObject("payload").getBoolean("isExisting");
    }

    private String sendRegisterRequest(int userId, int userType, String token) throws IOException, JSONException {
        URL url = new URL(endPoint + "?action=requestregistration&userId=" + userId + "&userType=" + userType + "&fcm_token=" + token);
        String resp = IOUtility.readFromURL(url);
        JSONObject o = new JSONObject(resp);
        return o.getJSONObject("payload").getString("verification");
    }

    private boolean sendRegisterToken(int userId, int userType, String token, String verify1, String verify2) throws IOException, JSONException {
        URL url = new URL(endPoint + "?action=verifyregistration&userId=" + userId + "&userType=" + userType + "&fcm_token=" + token + "&verification1=" + verify1 + "&verification2=" + verify2);
        String resp = IOUtility.readFromURL(url);
        JSONObject o = new JSONObject(resp);
        boolean success = o.getJSONObject("payload").getBoolean("success");

        Log.d(TAG, "Success of verification: " + success);

        if (!success) {
            Log.e(TAG, "Not successful! \n" + resp);
        }
        return success;
    }

    private String sendDeletionRequest(String token) throws IOException, JSONException {
        URL url = new URL(endPoint + "?action=requestDelete&fcm_token=" + token);
        String resp = IOUtility.readFromURL(url);
        JSONObject o = new JSONObject(resp);
        return o.getJSONObject("payload").getString("verification");
    }

    private boolean sendDeletionToken(String token, String verify1, String verify2) throws IOException, JSONException {
        URL url = new URL(endPoint + "?action=verifyDelete&fcm_token=" + token + "&verification1=" + verify1 + "&verification2=" + verify2);
        String resp = IOUtility.readFromURL(url);
        JSONObject o = new JSONObject(resp);
        boolean success = o.getJSONObject("payload").getBoolean("success");

        Log.d(TAG, "Success of verification: " + success);

        if (!success) {
            Log.e(TAG, "Not successful! \n" + resp);
        }
        return success;
    }


}
