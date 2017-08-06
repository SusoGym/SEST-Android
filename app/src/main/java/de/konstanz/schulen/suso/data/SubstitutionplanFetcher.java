package de.konstanz.schulen.suso.data;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.credentials.Credential;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.IOUtility;
import de.konstanz.schulen.suso.util.ThreadHandler;
import lombok.Getter;
import lombok.NonNull;


public class SubstitutionplanFetcher
{

    private static final String TAG = SubstitutionplanFetcher.class.getSimpleName();

    public static void fetchAsync(Credential credential, Context ctx, Callback<SubstitutionplanResponse> callback)
    {
        fetchAsync(credential.getId(), credential.getPassword(), ctx, callback);
    }

    public static void fetchAsync(final String username, final String password, final Context ctx, final Callback<SubstitutionplanResponse> callback)
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final SubstitutionplanResponse response = fetchSync(username, password, ctx);

                ThreadHandler.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.callback(response);
                    }
                }, ctx);

            }
        }).start();
    }

    public static SubstitutionplanResponse fetchSync(Credential credential, Context ctx)
    {
        return fetchSync(credential.getId(), credential.getPassword(), ctx);
    }

    public static SubstitutionplanResponse fetchSync(@NonNull String username, @NonNull String password, Context ctx)
    {

        try{


            username = URLEncoder.encode(username, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");

            URL url = new URL(ctx.getString(R.string.base_url) + "index.php?type=vplan&user=" + username + "&pwd=" + password + "&console");

            if(BuildConfig.DEBUG_MODE) {
                Log.d(TAG, "Trying to fetch substitution data from " + url.toExternalForm());
            }

            String data = IOUtility.readFromURL(url)
                    .replace("ï»¿", "");

            Log.d(TAG, "Successfully fetched data! ");

            if(data.contains("Invalid userdata!"))
            {
                return new SubstitutionplanResponse(SubstitutionplanResponse.STATUS_INVALID_USER, data);
            } else {
                new JSONObject(data);
                return new SubstitutionplanResponse(SubstitutionplanResponse.STATUS_OK, data);
            }

        }catch(JSONException e) {
            Log.e(TAG, "Received invalid Json data from Server: " + e .getMessage());

            return new SubstitutionplanResponse(SubstitutionplanResponse.STATUS_INVALID_DATA, e.getMessage() == null ? "null" : e.getMessage());
        }catch(IOException e){

            Log.e(TAG, "I/O Error while fetching data from server: " + e .getMessage());

            return new SubstitutionplanResponse(SubstitutionplanResponse.STATUS_NETWORK_ERROR, e.getMessage() == null ? "null" : e.getMessage());
        }catch (Exception e){

            Log.e(TAG, "Error while downloading substitution data from server: " + e.getMessage());
            e.printStackTrace();

            return new SubstitutionplanResponse(SubstitutionplanResponse.STATUS_ERROR, e.getMessage() == null ? "null" : e.getMessage());
        }


    }




    @Getter
    public static class SubstitutionplanResponse
    {
        public static final int STATUS_OK            = 200;
        public static final int STATUS_INVALID_USER  = 403;
        public static final int STATUS_INVALID_DATA  = 404;
        public static final int STATUS_ERROR         = 500;
        public static final int STATUS_NETWORK_ERROR = 504;

        @NonNull
        private int statusCode;
        @NonNull
        private String payload;

        private SubstitutionplanResponse(@NonNull int statusCode, @NonNull String payload)
        {
            this.statusCode = statusCode;
            this.payload = payload;
        }

    }


}
