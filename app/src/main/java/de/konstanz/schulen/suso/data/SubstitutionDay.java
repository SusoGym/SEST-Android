package de.konstanz.schulen.suso.data;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


class SubstitutionDay{
    //private ArrayList<SubstitutionData> substitutions = new ArrayList<>();

    SubstitutionDay(JSONArray jsonData) throws JSONException {
        for(int i = 0; i<jsonData.length(); ++i){
            //substitutions.add(new SubstitutionData(jsonData.optJSONObject(i)));
        }
    }
}
