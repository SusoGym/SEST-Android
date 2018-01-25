package de.konstanz.schulen.suso.data.fetch;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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

import de.konstanz.schulen.suso.SusoApplication;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.DebugUtil;


public class SmartLockHandler implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String TAG = SmartLockHandler.class.getSimpleName();
    private static final int RC_SAVE = 1;
    private static final int RC_READ = 3;

    private FragmentActivity context;
    private GoogleApiClient mCredentialsApiClient;
    private boolean mIsResolving = false;
    private Callback<Boolean> callback = null;


    public SmartLockHandler(@NonNull FragmentActivity context){
        this.context = context;
        mCredentialsApiClient = new GoogleApiClient.Builder(context).enableAutoManage(context, this)
                .addApi(Auth.CREDENTIALS_API)
                .addConnectionCallbacks(this)
                .build();
    }




    public void tryLogin(final Callback<Boolean> callback){
        this.callback = callback;
        (new Thread(){
            @Override
            public void run(){
                tryLoginSync();
            }
        }).start();
    }

    private void tryLoginSync(){
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




    public void handleActivityResult(int requestCode, int resultCode, Intent data){
        DebugUtil.infoLog(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        switch (requestCode) {
            case RC_READ:
                if (resultCode == Activity.RESULT_OK) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    processRetrievedCredential(credential);
                } else {
                    Log.e(TAG, "Credential Read: NOT OK");
                }

                mIsResolving = false;
                break;
            case RC_SAVE:
                if (resultCode == Activity.RESULT_OK) {
                    DebugUtil.infoLog(TAG, "Credential Save: OK");
                } else {
                    Log.e(TAG, "Credential Save: NOT OK");
                }
                mIsResolving = false;
                break;
        }
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
                status.startResolutionForResult(context, requestCode);
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
        DebugUtil.infoLog(TAG, "Credential Retrieved: " + credential.getId());

        DownloadManager.getInstance().login(context, credential.getId(), credential.getPassword(), new Callback<Integer>() {
            @Override
            public void callback(Integer errorCode) {
                callback.callback(errorCode== SubstitutionplanFetcher.SubstitutionplanResponse.NO_ERROR);
                callback = null;
            }
        });
    }







    public void saveCredential(String user, String pwd) {

        if(SusoApplication.TESTING)
        {
            return; // usually gets stuck here...
        }
        DebugUtil.infoLog(TAG, "Saving Credential:" + user);
        final Credential credential = new Credential.Builder(user)
                .setPassword(pwd)
                .build();


        if(!mCredentialsApiClient.isConnected())
        {
            DebugUtil.errorLog(TAG, "Did not save login to SmartLock, as google api was not connected yet!");
            return;
        }

        // NOTE: this method unconditionally saves the Credential built, even if all the fields
        // are blank or it is invalid in some other way.  In a real application you should contact
        // your app's back end and determine that the credential is valid before saving it to the
        // Credentials backend.

        Auth.CredentialsApi.save(mCredentialsApiClient, credential).setResultCallback(
                new ResolvingResultCallbacks<Status>(context, RC_SAVE) {
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
}
