package de.konstanz.schulen.suso.data.fetch;


import android.content.Context;
import android.os.Debug;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.util.DebugUtil;
import de.konstanz.schulen.suso.util.IOUtility;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class SubstitutionplanFetcher {
    private static final String TAG = SubstitutionplanFetcher.class.getClass().getSimpleName();




    public static SubstitutionplanResponse fetch(Context context, @NonNull String username, @NonNull String password){
        try {
            DebugUtil.errorLog(TAG, "USERNAME: " + (username==null));
            DebugUtil.errorLog(TAG, "PASSWORD: " + (password==null));
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for(StackTraceElement  s : st){
                DebugUtil.errorLog(TAG, s.getClassName() + " " + s.getLineNumber());
            }
            username = URLEncoder.encode(username, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");

            URL url = new URL(context.getString(R.string.base_url) + "index.php?type=vplan&user=" + username + "&pwd=" + password + "&console");

            String data = IOUtility.readFromURL(url).replace("ï»¿", "");






            if (data.contains("Invalid userdata!"))
                return new SubstitutionplanResponse(SubstitutionplanResponse.INVALID_USERDATA, data);

            //Check if the received data is in a JSON format
            new JSONObject(data);
            DebugUtil.infoLog(TAG, "Successfully fetched substitution data from " + url.toExternalForm());
            return new SubstitutionplanResponse(SubstitutionplanResponse.NO_ERROR, data);


        }catch (IOException e){
            DebugUtil.errorLog(TAG, "I/O Error while fetching data from server: " + e .getMessage());
            e.printStackTrace();
            return new SubstitutionplanResponse(SubstitutionplanResponse.NETWORK_ERROR, null);
        }catch (JSONException e) {
            DebugUtil.errorLog(TAG, "Received invalid Json data from Server: " + e .getMessage());
            return new SubstitutionplanResponse(SubstitutionplanResponse.INVALID_DATA, null);
        }catch(Exception e){
            DebugUtil.errorLog(TAG, "Unknown error while downloading substitution data from server: " + e.getMessage());
            return new SubstitutionplanResponse(SubstitutionplanResponse.UNKNOWN_ERROR, null);
        }

    }







    @AllArgsConstructor
    public static class SubstitutionplanResponse{
        public static final int NO_ERROR = 0, INVALID_USERDATA = 1, NETWORK_ERROR = 2,
                INVALID_DATA = 3, UNKNOWN_ERROR = 4;



        @Getter
        private int errorCode;
        @Getter
        private String data;
    }
}
