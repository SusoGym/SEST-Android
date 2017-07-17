package de.konstanz.schulen.suso;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.konstanz.schulen.suso.data.DownloadStringIntentService;

public class LoginActivity extends AppCompatActivity {
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = ((EditText)findViewById(R.id.edittext_username)).getText().toString();
                password = ((EditText)findViewById(R.id.edittext_password)).getText().toString();

                PendingIntent pendingResult = createPendingResult(DownloadStringIntentService.INTENT_REQUEST_UPDATE_SUBSTPLAN, new Intent(), 0);
                Intent intent = new Intent(LoginActivity.this, DownloadStringIntentService.class);
                intent.putExtra(DownloadStringIntentService.USERNAME_EXTRA, username);
                intent.putExtra(DownloadStringIntentService.PASSWORD_EXTRA, password);
                intent.putExtra(DownloadStringIntentService.RESULT_INTENT_NAME, pendingResult);

                startService(intent);
            }
        });
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode== DownloadStringIntentService.INTENT_REQUEST_UPDATE_SUBSTPLAN){
            if(resultCode==DownloadStringIntentService.SUCCESSFUL_CODE){
                if(data.getStringExtra(DownloadStringIntentService.RESULT_EXTRA).contains("Invalid userdata!")){
                    Toast errorToast = Toast.makeText(LoginActivity.this, "Benutzername oder Passwort ungültig.", Toast.LENGTH_SHORT);
                    errorToast.show();
                }else{

                    SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
                    editor.putString(MainActivity.USERNAME_KEY, username);
                    editor.putString(MainActivity.PASSWORD_KEY, password);
                    editor.commit();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            }else if(resultCode==DownloadStringIntentService.ERROR_CODE){
                Toast errorToast = Toast.makeText(LoginActivity.this, "Fehler bei der Anmeldung. Bitte überprüfe deine Internetverbindung.", Toast.LENGTH_SHORT);
                errorToast.show();
            }
        }

    }

}
