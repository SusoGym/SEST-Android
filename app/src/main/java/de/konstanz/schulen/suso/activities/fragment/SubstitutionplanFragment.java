package de.konstanz.schulen.suso.activities.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.data.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.FabricHandler;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;

import static de.konstanz.schulen.suso.util.SharedPreferencesManager.SHR_SUBSITUTIONPLAN_DATA;

public class SubstitutionplanFragment extends AbstractFragment {

    private static final String TAG = SubstitutionplanFragment.class.getSimpleName();
    private SwipeRefreshLayout swipeContainer;

    public SubstitutionplanFragment() {
        super(R.layout.substitutionplan_fragment, R.id.nav_substitutionplan, R.string.nav_substitutionplan);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        swipeContainer = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshContainer);
    }

    @Override
    public void onPushToForeground() {
        showSubstitutionplan();
    }

    @Override
    public void refresh() {
        updateSubstitutionplan();
    }

    public void displaySubsitutionplan(JSONObject coverLessons) {

        LinearLayout substitutionplanContent = (LinearLayout) getActivity().findViewById(R.id.substitutionplan_content);

        if (substitutionplanContent == null) {
            Log.i(TAG, "Error while trying to display Subsitutionplan: Layout is null; This is fine tho");
            return;
        }

        substitutionplanContent.removeAllViews();

        try {

            Iterator<String> substitutionDays = coverLessons.keys();
            //Iterate over the days

            while (substitutionDays.hasNext()) {

                String dateKey = substitutionDays.next();

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView dateView = new TextView(substitutionplanContent.getContext());
                dateView.setGravity(Gravity.CENTER);
                dateView.setText(getDate(dateKey));
                dateView.setTextSize(27f);
                lp.setMargins(0, 70, 0, 30);
                lp.gravity = Gravity.CENTER;
                dateView.setLayoutParams(lp);

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

        LinearLayout substitutionplanContent = (LinearLayout) getActivity().findViewById(R.id.substitutionplan_content);

        substitutionplanContent.removeAllViews();

        TextView infoView = new TextView(substitutionplanContent.getContext());
        infoView.setText(R.string.no_substitutions);
        infoView.setTextSize(25);
        infoView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.setMargins(0, 40, 0, 0);
        infoView.setLayoutParams(llp);
        substitutionplanContent.addView(infoView);


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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.substitutionplan_cardview, parent, false);
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
        private TextView hourView;
        private TextView teacherView;
        private TextView subjectView;
        private TextView roomView;
        private TextView commentView;

        private LinearLayout roomLayout;
        private LinearLayout commentLayout;

        private SubstitutionDataViewHolder(View itemView) {
            super(itemView);

            hourView = (TextView) itemView.findViewById(R.id.substitution_card_hour);
            teacherView = (TextView) itemView.findViewById(R.id.substitution_card_teacher);
            subjectView = (TextView) itemView.findViewById(R.id.substitution_card_subject);
            roomView = (TextView) itemView.findViewById(R.id.substitution_card_room);
            commentView = (TextView) itemView.findViewById(R.id.substitution_card_comment);

            roomLayout = (LinearLayout) itemView.findViewById(R.id.substitution_layout_room);
            commentLayout = (LinearLayout) itemView.findViewById(R.id.substitution_layout_comment);
        }

        private void initialize(SubstitutionData data) {


            hourView.setText(data.getHour());
            teacherView.setText(data.getTeacher() + " " + data.getSubTeacher(), TextView.BufferType.SPANNABLE);
            subjectView.setText(data.getSubject() + " " + data.getSubSubject(), TextView.BufferType.SPANNABLE);
            if (data.getSubRoom().isEmpty()) {
                roomLayout.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, R.id.substitution_layout_subject);
                commentLayout.setLayoutParams(params);
            } else {
                roomView.setText(data.getSubRoom());
            }
            commentView.setText(data.getComment());

            if (!data.getSubTeacher().isEmpty()) {
                Spannable span = (Spannable) teacherView.getText();
                span.setSpan(new StrikethroughSpan(), 0, data.getTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (!data.getSubTeacher().isEmpty()) {
                Spannable span = (Spannable) subjectView.getText();
                span.setSpan(new StrikethroughSpan(), 0, data.getSubject().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }


    private void showSubstitutionplan() {

        String savedSubstitutionplanData = SharedPreferencesManager.getSharedPreferences().getString(SHR_SUBSITUTIONPLAN_DATA, null);
        if (savedSubstitutionplanData != null) {
            displaySubstitutionplan(savedSubstitutionplanData);
        }

        updateSubstitutionplan();
    }

    public void updateSubstitutionplan() {
        SubstitutionplanFetcher.fetchAsync(AccountManager.getInstance().getUsername(), AccountManager.getInstance().getPassword(), getActivity(), new Callback<SubstitutionplanFetcher.SubstitutionplanResponse>() {
            @Override
            public void callback(SubstitutionplanFetcher.SubstitutionplanResponse request) {
                boolean success = true;
                if (request.getStatusCode() == SubstitutionplanFetcher.SubstitutionplanResponse.STATUS_OK) {
                    String json = request.getPayload();
                    if (!SharedPreferencesManager.getSharedPreferences().getString(SHR_SUBSITUTIONPLAN_DATA, "").equals(json)) {
                        SharedPreferencesManager.getSharedPreferences().edit().putString(SHR_SUBSITUTIONPLAN_DATA, json).apply();
                        displaySubstitutionplan(json);
                    }
                } else {
                    Log.e(TAG, "Error while trying to reload subsitutions: " + request.getStatusCode() + "/" + request.getPayload());
                    success = false;
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.substplan_network_error), Toast.LENGTH_SHORT).show();
                }

                FabricHandler.logCustomEvent(new CustomEvent("Reloaded Substitutionplan").putCustomAttribute("success", success + ""));

                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void displaySubstitutionplan(String json) {

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONObject coverLessons = jsonObject.getJSONObject("coverlessons");
            displaySubsitutionplan(coverLessons);

        } catch (JSONException e) {
            Log.e(TAG, "Error while trying to display substitutions: " + e.getMessage());
            displayNoSubstitution();

            if (!e.getMessage().contains("Value [] at coverlessons")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.substplan_json_error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.substplan_no_subst), Toast.LENGTH_LONG).show();
            }

        }


    }

}
