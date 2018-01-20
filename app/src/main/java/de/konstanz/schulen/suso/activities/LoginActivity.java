package de.konstanz.schulen.suso.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.data.fetch.DownloadManager;
import de.konstanz.schulen.suso.data.fetch.SmartLockHandler;
import de.konstanz.schulen.suso.data.fetch.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.firebase.FirebaseHandler;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.DebugUtil;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener{

    private static final String TAG = LoginActivity.class.getSimpleName();
    private SmartLockHandler smartLockHandler;


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
        /*ImageView img = (ImageView) findViewById(R.id.login_img);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCredentials();
            }
        });*/

        smartLockHandler = new SmartLockHandler(this);
        smartLockHandler.tryLogin(new Callback<Boolean>() {
            @Override
            public void callback(Boolean successful) {
                if(successful) startMain();
            }
        });
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

        //checkLogin(username, password, false);
        final String finalUsername = username;
        final String finalPassword = password;

        DownloadManager.getInstance().login(this, username, password, new Callback<Integer>() {
            @Override
            public void callback(Integer errorCode) {
                switch (errorCode)
                {
                    case SubstitutionplanFetcher.SubstitutionplanResponse.NO_ERROR:
                        smartLockHandler.saveCredential(finalUsername, finalPassword);
                        FirebaseHandler.getInstance().registerToken(); // send our new account to database to link with this device
                        startMain();
                        return;
                    case SubstitutionplanFetcher.SubstitutionplanResponse.INVALID_USERDATA:
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_invalid_data), Toast.LENGTH_SHORT).show();
                        break;
                    case SubstitutionplanFetcher.SubstitutionplanResponse.NETWORK_ERROR:
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_network_error), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                        break;
                }

                //Not sure why we would be collecting this information
                //FabricHandler.logLoginEvent(new LoginEvent().putMethod(smartLock ? "smartLock" : "manual").putSuccess(success));
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        smartLockHandler.handleActivityResult(requestCode, resultCode, data);
    }


}
