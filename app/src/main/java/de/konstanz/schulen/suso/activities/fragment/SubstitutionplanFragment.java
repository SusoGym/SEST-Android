package de.konstanz.schulen.suso.activities.fragment;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.MainActivity;

public class SubstitutionplanFragment extends AbstractFragment {

    public SubstitutionplanFragment() {
        super(R.layout.fragment_substitutionplan, R.id.nav_substitutionplan, R.string.nav_substitutionplan);
    }

    @Override
    public void refresh() {
        if (!(getActivity() instanceof MainActivity))
            return;

        MainActivity a = (MainActivity) getActivity();

        a.updateSubstitutionplan();
    }

    public void displaySubsitutionplan(JSONObject coverLessons) {

        LinearLayout substitutionplanContent = (LinearLayout) getActivity().findViewById(R.id.content_substitutionplan);
        substitutionplanContent.removeAllViews();

        try {

            Iterator<String> substitutionDays = coverLessons.keys();
            //Iterate over the days

            while (substitutionDays.hasNext()) {

                String dateKey = substitutionDays.next();

                TextView dateView = new TextView(substitutionplanContent.getContext());
                dateView.setGravity(Gravity.CENTER);
                dateView.setText(getDate(dateKey));

                JSONArray daySubstitutions = coverLessons.getJSONArray(dateKey);
                ArrayList<SubstitutionData> substitutions = new ArrayList<>();


                for (int i = 0; i < daySubstitutions.length(); ++i) {
                    substitutions.add(new SubstitutionplanFragment.SubstitutionData(daySubstitutions.getJSONObject(i)));
                }

                /*
                Create the UI and data interfaces necessary to represent a substitution day
                and fill them with the parsed substitutions
                 */
                SubstitutionDataAdapter adapter = new SubstitutionDataAdapter(substitutions);

                RecyclerView recyclerView = new RecyclerView(substitutionplanContent.getContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(substitutionplanContent.getContext()) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                });
                recyclerView.setAdapter(adapter);

                substitutionplanContent.addView(dateView);
                substitutionplanContent.addView(recyclerView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void displayNoSubstitution() {

        LinearLayout substitutionplanContent = (LinearLayout) getActivity().findViewById(R.id.content_substitutionplan);

        TextView infoView = new TextView(substitutionplanContent.getContext());
        infoView.setText(R.string.no_substitutions);
        infoView.setTextSize(25);
        infoView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.setMargins(0, 40, 0, 0);
        infoView.setLayoutParams(llp);
        substitutionplanContent.addView(infoView);
        Toast.makeText(getActivity(), getResources().getString(R.string.substplan_json_error), Toast.LENGTH_LONG).show();

    }

    private String getDate(String date) throws ParseException {

                /*
                Get date information such as an easily readable string represantation or the day of week
                 */

        DateFormat readFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat writeFormat = DateFormat.getDateInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(readFormat.parse(date));


        String dayOfWeek = "";
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                dayOfWeek = getResources().getString(R.string.day_monday);
                break;
            case Calendar.TUESDAY:
                dayOfWeek = getResources().getString(R.string.day_tuesday);
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = getResources().getString(R.string.day_wednesday);
                break;
            case Calendar.THURSDAY:
                dayOfWeek = getResources().getString(R.string.day_thursday);
                break;
            case Calendar.FRIDAY:
                dayOfWeek = getResources().getString(R.string.day_friday);
                break;
            case Calendar.SATURDAY:
                dayOfWeek = getResources().getString(R.string.day_saturday);
                break;
            case Calendar.SUNDAY:
                dayOfWeek = getResources().getString(R.string.day_sunday);
                break;
        }
        return dayOfWeek + ", " + writeFormat.format(calendar.getTime());
    }

    private static class SubstitutionDataAdapter extends RecyclerView.Adapter {
        private List<SubstitutionData> substitutions;


        private SubstitutionDataAdapter(List<SubstitutionData> substitutions) {
            this.substitutions = substitutions;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_substitutionplan, parent, false);
            return new SubstitutionDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((SubstitutionDataViewHolder) holder).initialize(substitutions.get(position));
        }

        @Override
        public int getItemCount() {
            return substitutions.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }

    private static class SubstitutionData {
        private String subject;
        private String teacher;
        private String subTeacher;
        private String subSubject;
        private String subRoom;
        private String classes;
        private String comment;
        private String hour;


        public SubstitutionData(JSONObject jsonData) throws JSONException {
            subject = jsonData.getString("subject");
            teacher = jsonData.getString("teacher");
            subTeacher = jsonData.getString("subteacher");
            subSubject = jsonData.getString("subsubject");
            subRoom = jsonData.getString("subroom");
            classes = jsonData.getString("classes");
            comment = jsonData.getString("comment");
            hour = jsonData.getString("hour");

        /*
        If subject or teacher stay the same, show now crossed-out text, else add a space between
        the crossed-out and the new text
         */
            if (subSubject.equalsIgnoreCase(subject)) subject = "";
            else subject = subject + ' ';
            if (subTeacher.equalsIgnoreCase(teacher)) teacher = "";
            else teacher = teacher + ' ';

        /*
        Display no text instead of 3 bars for a clear field
         */
            if (subTeacher.equals("---")) subTeacher = "";
            if (subSubject.equals("---")) subSubject = "";
            if (subRoom.equals("---")) subRoom = "";

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

    private static class SubstitutionDataViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        private Context ctx;

        private SubstitutionDataViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.substitution_textview);
            ctx = itemView.getContext();
        }

        void initialize(SubstitutionData data) {

            String hour = ctx.getString(R.string.substcard_hour) + " " + data.getHour() + "\n";
            String teacher = ctx.getString(R.string.substcard_teacher) + " " + data.getTeacher() + data.getSubTeacher() + '\n';
            String subject = ctx.getString(R.string.substcard_subject) + " " +data.getSubject() + data.getSubSubject() + '\n';
            String room = (data.getSubRoom().isEmpty() ? "" : ctx.getString(R.string.substcard_room) + " " + data.getSubRoom() + '\n');
            String comment = ctx.getString(R.string.substcard_comment) + " " + data.getComment();

            textView.setText( hour + teacher + subject + room + comment, TextView.BufferType.SPANNABLE);

            Spannable spannable = (Spannable) textView.getText();

            int teacherIndex = hour.length() + ctx.getString(R.string.substcard_teacher).length() + 1;
            int teacherEnd = teacherIndex + data.getTeacher().length();

            int subjectIndex = hour.length() + teacher.length() + ctx.getString(R.string.substcard_subject).length() + 1;
            int subjectEnd = subjectIndex + data.getSubject().length();

            if (!data.getTeacher().isEmpty())
                spannable.setSpan(new StrikethroughSpan(), teacherIndex, teacherEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!data.getSubject().isEmpty())
                spannable.setSpan(new StrikethroughSpan(), subjectIndex, subjectEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }


}
