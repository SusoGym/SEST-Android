package de.konstanz.schulen.suso.data.fetch;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.credentials.Credential;

import org.json.JSONException;
import org.json.JSONObject;

import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.DebugUtil;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;
import de.konstanz.schulen.suso.util.ThreadHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

;

public class DownloadManager {
    private static final String TAG = DownloadManager.class.getSimpleName();

    private static DownloadManager instance;

    public static DownloadManager getInstance() {
        return instance;
    }

    public static DownloadManager initializeInstance(Context context) {
        return new DownloadManager(context);
    }


    @Getter
    private String username = null, password = null;
    @Getter
    private AccountInformation accountInformation = null;


    private DownloadManager(Context context) {

        if (instance == null) {
            instance = this;
        }

        String username = SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_USERNAME, null);
        String password = SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_PASSWORD, null);
        if (username != null && password != null) {
            loginSync(context, username, password);
        }
    }


    /**
     * Checks the given user credentials and fetches the substitution plan as well as general account information
     * The login is performed in a separate thread
     *
     * @param context
     * @param username
     * @param password
     * @param callback The callback for when the login has completed. The parameter is true if the login was successful
     */
    public void login(final Context context, @NonNull final String username, @NonNull final String password, @Nullable final Callback<Integer> callback) {
        (new Thread() {
            @Override
            public void run() {
                final int errorCode = loginSync(context, username, password);
                if (callback != null) {
                    ThreadHandler.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(errorCode);
                        }
                    }, context);
                }
            }
        }).start();
    }


    private int loginSync(Context context, @NonNull final String username, @NonNull final String password) {
        SubstitutionplanFetcher.SubstitutionplanResponse response = SubstitutionplanFetcher.fetch(context, username, password);

        if (response.getErrorCode() == SubstitutionplanFetcher.SubstitutionplanResponse.NO_ERROR) {
            this.username = username;
            this.password = password;
            saveToSharedPreferences();
            SharedPreferencesManager.getSharedPreferences().edit().putString(SharedPreferencesManager.SHR_SUBSITUTIONPLAN_DATA, response.getData()).commit();


            try {
                JSONObject userData = new JSONObject(response.getData()).getJSONObject("user").getJSONObject("data");
                int accountType = userData.getInt("type");
                int accountId = userData.getInt("id");
                String name = userData.getString("name");
                String surname = userData.getString("surname");
                String className = "-";
                if (accountType == 3) { // is student
                    className = userData.getString("class");
                }
                this.accountInformation = new AccountInformation(accountType, accountId, name, surname, className);


            } catch (JSONException e) {
                DebugUtil.errorLog(TAG, "Invalid substitution data Json (should be fine)");
            }


        }
        return response.getErrorCode();
    }


    public boolean isLoggedIn() {
        return username != null;
    }

    public Credential getCredentials() {
        return new Credential.Builder(this.username).setPassword(this.password).build();
    }

    public void logout() {
        Log.i(TAG, "Logging out...");
        username = null;
        password = null;

        saveToSharedPreferences();
        FirebaseHandler.getInstance().deleteToken();
    }


    public void updateSubstitutionplanData(final Context context, final Callback<SubstitutionplanFetcher.SubstitutionplanResponse> callback) {

        String usernameTemp, passwordTemp;
        if (this.username == null || this.password == null) {
            usernameTemp = SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_USERNAME, null);
            passwordTemp = SharedPreferencesManager.getSharedPreferences().getString(SharedPreferencesManager.SHR_PASSWORD, null);
        } else {
            usernameTemp = this.username;
            passwordTemp = this.password;
        }


        final String username = usernameTemp;
        final String password = passwordTemp;
        (new Thread() {
            @Override
            public void run() {
                final SubstitutionplanFetcher.SubstitutionplanResponse response = SubstitutionplanFetcher.fetch(context, username, password);
                ThreadHandler.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getErrorCode() == SubstitutionplanFetcher.SubstitutionplanResponse.NO_ERROR) {
                            SharedPreferencesManager.getSharedPreferences().edit().putString(SharedPreferencesManager.SHR_SUBSITUTIONPLAN_DATA, response.getData()).commit();
                        }
                        callback.callback(response);
                    }
                }, context);
            }
        }).start();
    }


    private void saveToSharedPreferences() {
        SharedPreferencesManager.getSharedPreferences().edit()
                .putString(SharedPreferencesManager.SHR_USERNAME, username)
                .putString(SharedPreferencesManager.SHR_PASSWORD, password)
                .apply();
    }


    @AllArgsConstructor
    public static class AccountInformation {
        @Getter
        private int accountType, accountId;
        @Getter
        private String name, surname, className;
    }
}
