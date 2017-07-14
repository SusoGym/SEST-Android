package de.konstanz.schulen.suso.substitutionplan_recyclerview;


import org.json.JSONException;
import org.json.JSONObject;

public class SubstitutionData{
    private String subject;
    private String teacher;
    private String subTeacher;
    private String subSubject;
    private String subRoom;
    private String classes;
    private String comment;
    private String hour;


    public SubstitutionData(JSONObject jsonData) throws JSONException {
        subject = jsonData.getString("subject") + ' ';
        teacher = jsonData.getString("teacher") + ' ';
        subTeacher = jsonData.getString("subteacher");
        subSubject = jsonData.getString("subsubject");
        subRoom = jsonData.getString("subroom");
        classes = jsonData.getString("classes");
        comment = jsonData.getString("comment");
        hour = jsonData.getString("hour");

        if(subSubject.equalsIgnoreCase(subject)) subject = "";
        if(subTeacher.equalsIgnoreCase(teacher)) teacher = "";

        if(subTeacher.equals("---")) subTeacher = "";
        if(subSubject.equals("---")) subSubject = "";
        if(subRoom.equals("---")) subRoom = "";

    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getSubTeacher() {
        return subTeacher;
    }

    public String getSubSubject() {
        return subSubject;
    }

    public String getSubRoom() {
        return subRoom;
    }

    public String getClasses() {
        return classes;
    }

    public String getComment() {
        return comment;
    }

    public String getHour() {
        return hour;
    }


}
