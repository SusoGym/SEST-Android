package de.konstanz.schulen.suso.data;


import org.json.JSONException;
import org.json.JSONObject;

class SubstitutionData{
    private String subject;
    private String teacher;
    private String subTeacher;
    private String subSubject;
    private String subRoom;
    private String classes;
    private String comment;
    private String hour;


    SubstitutionData(JSONObject jsonData) throws JSONException {
        subject = jsonData.getString("subject");
        teacher = jsonData.getString("teacher");
        subTeacher = jsonData.getString("subTeacher");
        subSubject = jsonData.getString("subSubject");
        subRoom = jsonData.getString("subRoom");
        classes = jsonData.getString("classes");
        comment = jsonData.getString("comment");
        hour = jsonData.getString("hour");
    }
}
