package de.konstanz.schulen.suso.util;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesManager
{

    public static SharedPreferences initialize(Context ctx){
        if(sharedPreferences != null)
            throw new IllegalStateException("Already Initialized");

        return sharedPreferences = ctx.getSharedPreferences("de.konstanz.schulen.suso.data", Context.MODE_PRIVATE);
    }

    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }




    public static final String SHR_USERNAME = "nov_username";
    public static final String SHR_PASSWORD = "nov_password";

    public static final String SHR_SUBSITUTIONPLAN_DATA = "subsitution_json";

}
