package de.konstanz.schulen.suso.data;





import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class SubstitutionplanData {
    private ArrayList<SubstitutionDay> substitutionDays = new ArrayList<>();


    SubstitutionplanData(String username, String password){
        String data;
        JSONObject jsonData;
        try {
            data = Utils.readFromURL(new URL("https://www.suso.schulen.konstanz.de/intern/index.php?type=vplan&user=" + username + "&pwd=" + password + "&console"));
            jsonData = new JSONObject(data);



            JSONObject coverLessons = jsonData.getJSONObject("coverlessons");
            Iterator<String> keys = coverLessons.keys();
            //Iterate over all days in the JSON file
            String currentKey;
            while(keys.hasNext()){
                currentKey = keys.next();
               substitutionDays.add(new SubstitutionDay(coverLessons.getJSONArray(currentKey)));


            }

        }catch (IOException e) {
            System.err.println("Error while retrieving substitution data from server:\n" + e.getMessage());
            return;
        } catch (JSONException e) {
            System.err.println("Error while reading substitution data:\n" + e.getMessage());
            return;
        }


    }






}
