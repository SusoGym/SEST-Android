package de.konstanz.schulen.suso.data;


import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.URL;

public class DownloadStringIntentService extends IntentService {
    public static final String NAME = DownloadStringIntentService.class.getSimpleName();
    public static final String USERNAME_EXTRA = "username";
    public static final String PASSWORD_EXTRA = "password";
    public static final String RESULT_INTENT_NAME = "subst_update_result";
    public static final String RESULT_EXTRA = "substitution_plan";
    public static final int SUCCESSFUL_CODE = 0;
    public static final int ERROR_CODE = -1;
    public static final int INTENT_REQUEST_UPDATE_SUBSTPLAN = 1;

    public DownloadStringIntentService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PendingIntent reply = intent.getParcelableExtra(RESULT_INTENT_NAME);


        String username = intent.getStringExtra(USERNAME_EXTRA);
        String password = intent.getStringExtra(PASSWORD_EXTRA);

        String data;
        try {
            data = Utils.readFromURL(new URL("https://www.suso.schulen.konstanz.de/intern/index.php?type=vplan&user=" + username + "&pwd=" + password + "&console"))
            .replace("ï»¿", "");

        } catch (IOException e) {
            System.err.println("Error while downloading substitution plan data from server");
            try {
                reply.send(ERROR_CODE);
            } catch (PendingIntent.CanceledException e1) {}
            return;
        }

        Intent replyContent = new Intent();
        replyContent.putExtra(RESULT_EXTRA, data);
        try {
            reply.send(this, SUCCESSFUL_CODE, replyContent);
        } catch (PendingIntent.CanceledException e) {}
    }
}
