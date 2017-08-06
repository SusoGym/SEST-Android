package de.konstanz.schulen.suso.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import org.json.JSONObject;

import de.konstanz.schulen.suso.data.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import lombok.Getter;

import static de.konstanz.schulen.suso.util.SharedPreferencesManager.*;
import static de.konstanz.schulen.suso.data.SubstitutionplanFetcher.SubstitutionplanResponse.*;

public class AccountManager {

    private static final String TAG = AccountManager.class.getSimpleName();

    private static AccountManager instance;

    public static AccountManager getInstance() {
        return instance == null ? instance = new AccountManager() : instance;
    }

    @Getter()
    private String username, password, name, surname, className;

    @Getter
    private int accountId, accountType;

    public void loadFromSharedPreferences(Context ctx) {
        username = SharedPreferencesManager.getSharedPreferences().getString(SHR_USERNAME, null);
        password = SharedPreferencesManager.getSharedPreferences().getString(SHR_PASSWORD, null);

        if (username != null && password != null) {
            loadFromOnline(ctx, null, false);
        }

    }

    public void loadFromOnline(Context ctx, @Nullable Callback<String> callback) {
        loadFromOnline(ctx, callback, false);
    }

    public void loadFromOnline(final Context ctx, @Nullable final Callback<String> callback, final boolean rw) {

        new Thread() {
            @Override
            public void run() {

                final String resp = checkLogin(ctx, rw);

                ThreadHandler.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.callback(resp);
                        }
                    }
                }, ctx);

            }
        }.start();

    }

    private String checkLogin(Context ctx, boolean rw) {

        SubstitutionplanFetcher.SubstitutionplanResponse data = SubstitutionplanFetcher.fetchSync(username, password, ctx);

        boolean success = false;
        String response = null;

        switch (data.getStatusCode()) {
            case STATUS_OK:
                response = "OK";
                success = true;
                break;
            case STATUS_INVALID_DATA:
                response = "INVALID_DATA";
                break;
            case STATUS_INVALID_USER:
                response = "INVALID_USER";
                break;
            case STATUS_NETWORK_ERROR:
                response = "NETWORK_ERROR";
                break;
        }

        if (!success) {
            if (rw) {
                username = null;
                password = null;
            }
        } else {
            try {
                JSONObject userData = new JSONObject(data.getPayload()).getJSONObject("user").getJSONObject("data");

                accountType = userData.getInt("type");
                accountId = userData.getInt("id");
                name = userData.getString("name");
                surname = userData.getString("surname");

                if (accountType == 3) { // is student
                    className = userData.getString("class");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return response;

    }

    public boolean isValidLogin(Context ctx) {

        return !(username == null || password == null) && checkLogin(ctx, false).equals("OK");

    }

    public void saveToSharedPreferences() {
        SharedPreferencesManager.getSharedPreferences().edit()
                .putString(SHR_USERNAME, username)
                .putString(SHR_PASSWORD, password)
                .apply();
    }

    public AccountManager setUsername(@NonNull String username) {
        this.username = username;
        return this;
    }

    public AccountManager setPassword(@NonNull String password) {
        this.password = password;
        return this;
    }

    public void logout() {
        Log.i(TAG, "Logging out...");
        username = null;
        password = null;
        FirebaseHandler.getInstance().deleteToken();
    }

}
