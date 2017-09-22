package de.konstanz.schulen.suso.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.crashlytics.android.answers.LoginEvent;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.DebugUtil;
import de.konstanz.schulen.suso.util.FabricHandler;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SAVE = 1;
    private static final int RC_READ = 3;
    private static GoogleApiClient mCredentialsApiClient;
    private boolean mIsResolving = false;

    public static GoogleApiClient getCredentialsApiClient() {
        return mCredentialsApiClient;
    }

    private void checkLogin(final String username, final String pwd, final boolean smartLock) {

        AccountManager.getInstance().setPassword(pwd).setUsername(username);

        AccountManager.getInstance().loadFromOnline(this, new Callback<String>() {
            @Override
            public void callback(String response) {

                boolean success = false;

                switch (response)
                {
                    case "OK":
                        saveCredentialToSmartLock(username, pwd);
                        AccountManager.getInstance().saveToSharedPreferences();
                        success = true;

                        FirebaseHandler.getInstance().registerToken(); // send our new account to database to link with this device
                        break;
                    case "INVALID_DATA":
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_invalid_data), Toast.LENGTH_SHORT).show();
                        break;
                    case "INVALID_USER":
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_network_error), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                        break;
                }


                FabricHandler.logLoginEvent(new LoginEvent().putMethod(smartLock ? "smartLock" : "manual").putSuccess(success));

                if (!success) {
                    requestCredentials();
                } else {
                    startMain();
                }


            }
        }, true);

    }

    private void startMain() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(this);
        ImageView img = (ImageView) findViewById(R.id.login_img);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCredentials();
            }
        });

        mCredentialsApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .addConnectionCallbacks(this)
                .build();


    }

    @Override
    public void onClick(View view) {
        String username = ((EditText) findViewById(R.id.edittext_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.edittext_password)).getText().toString();

        if (BuildConfig.DEBUG_MODE) {

            if (username.equals("") && password.equals("")) {
                username = password = "Oberstufe";
            }

            DebugUtil.infoLog("LoginActivity", "Login try with: ['" + username + "', '" + password + "']");
        }

        if (username.equals("") || password.equals("")) {

            Toast.makeText(view.getContext(), "Password or Username is empty", Toast.LENGTH_LONG).show();
            return;
        }

        checkLogin(username, password, false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        DebugUtil.infoLog(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        Auth.CredentialsApi.disableAutoSignIn(mCredentialsApiClient);

        if (!mIsResolving) {
            requestCredentials();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended: " + i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        smartLockOnActivity(requestCode, resultCode, data);

    }

    private void smartLockOnActivity(int requestCode, int resultCode, Intent data) {
        DebugUtil.infoLog(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        switch (requestCode) {
            case RC_READ:
                if (resultCode == RESULT_OK) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    processRetrievedCredential(credential);
                } else {
                    Log.e(TAG, "Credential Read: NOT OK");
                }

                mIsResolving = false;
                break;
            case RC_SAVE:
                if (resultCode == RESULT_OK) {
                    DebugUtil.infoLog(TAG, "Credential Save: OK");
                } else {
                    Log.e(TAG, "Credential Save: NOT OK");
                }

                mIsResolving = false;
                break;
        }
    }

    private void saveCredentialToSmartLock(String user, String pwd) {
        DebugUtil.infoLog(TAG, "Saving Credential:" + user + ":" + anonymizePassword(pwd));
        final Credential credential = new Credential.Builder(user)
                .setPassword(pwd)
                .build();


        // NOTE: this method unconditionally saves the Credential built, even if all the fields
        // are blank or it is invalid in some other way.  In a real application you should contact
        // your app's back end and determine that the credential is valid before saving it to the
        // Credentials backend.

        Auth.CredentialsApi.save(mCredentialsApiClient, credential).setResultCallback(
                new ResolvingResultCallbacks<Status>(this, RC_SAVE) {
                    @Override
                    public void onSuccess(@NonNull Status status) {
                        DebugUtil.infoLog(TAG, "Credential saved");
                    }

                    @Override
                    public void onUnresolvableFailure(@NonNull Status status) {
                        DebugUtil.infoLog(TAG, "Save Failed:" + status);
                    }
                });
    }

    /**
     * Request Credentials from the Credentials API.
     */
    private void requestCredentials() {
        // Request all of the user's saved SHR_USERNAME/SHR_PASSWORD credentials.  We are not using
        // setAccountTypes so we will not load any credentials from other Identity Providers.
        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();


        Auth.CredentialsApi.request(mCredentialsApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(@NonNull CredentialRequestResult credentialRequestResult) {
                        Status status = credentialRequestResult.getStatus();
                        if (status.isSuccess()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // sign-in enabled.
                            processRetrievedCredential(credentialRequestResult.getCredential());
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one
                            resolveResult(status, RC_READ);
                        } else {
                            Log.w(TAG, "Unexpected status code: " + status.getStatusCode());
                        }
                    }
                });
    }

    /**
     * Attempt to resolve a non-successful Status from an asynchronous request.
     *
     * @param status      the Status to resolve.
     * @param requestCode the request code to use when starting an Activity for result,
     *                    this will be passed back to onActivityResult.
     */
    private void resolveResult(Status status, int requestCode) {
        // We don't want to fire multiple resolutions at once since that can result
        // in stacked dialogs after rotation or another similar event.
        if (mIsResolving) {
            Log.w(TAG, "resolveResult: already resolving.");
            return;
        }

        DebugUtil.infoLog(TAG, "Resolving: " + status);
        if (status.hasResolution()) {
            DebugUtil.infoLog(TAG, "STATUS: RESOLVING");
            try {
                status.startResolutionForResult(LoginActivity.this, requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "STATUS: Failed to send resolution.", e);
            }
        } else {
            Log.e(TAG, "STATUS: FAIL");
        }
    }

    /**
     * Process a Credential object retrieved from a successful request.
     *
     * @param credential the Credential to process.
     */
    private void processRetrievedCredential(Credential credential) {
        DebugUtil.infoLog(TAG, "Credential Retrieved: " + credential.getId() + ":" +
                anonymizePassword(credential.getPassword()));

        checkLogin(credential.getId(), credential.getPassword(), true);
    }

    /**
     * Make a SHR_PASSWORD into asterisks of the right length, for logging.
     **/
    private String anonymizePassword(String password) {
        if (password == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }

}
