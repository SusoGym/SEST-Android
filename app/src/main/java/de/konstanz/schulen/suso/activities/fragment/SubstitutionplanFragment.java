package de.konstanz.schulen.suso.activities.fragment;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import de.konstanz.schulen.suso.data.fetch.DownloadManager;
import de.konstanz.schulen.suso.data.fetch.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.DebugUtil;
import de.konstanz.schulen.suso.util.FabricHandler;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;
import lombok.Getter;

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

        DebugUtil.infoLog(TAG, "Pushing to foreground");
        showSubstitutionplan();
    }

    @Override
    public void refresh() {
        updateSubstitutionplan();
    }






    /**
     * Converts the date format of the json retrieved from the server to an easily readable string
     * @param date
     * @return
     * @throws ParseException
     */
    private String getDate(String date) throws ParseException {

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







    /**
     * Displays and updates the substitutionplan
     */
    private void showSubstitutionplan() {

        String savedSubstitutionplanData = SharedPreferencesManager.getSharedPreferences().getString(SHR_SUBSITUTIONPLAN_DATA, null);
        if (savedSubstitutionplanData != null) {
            DebugUtil.infoLog(TAG, savedSubstitutionplanData);
            displaySubstitutionplan(savedSubstitutionplanData);
        }
        DebugUtil.infoLog(TAG, "Updating substitution plan");
        updateSubstitutionplan();
    }

    /**
     * Downloads the substitution plan from the server and displays it if it differs from the locally saved one
     */
    public void updateSubstitutionplan() {

        DownloadManager.getInstance().updateSubstitutionplanData(getActivity(), new Callback<SubstitutionplanFetcher.SubstitutionplanResponse>() {
            @Override
            public void callback(SubstitutionplanFetcher.SubstitutionplanResponse request) {
                boolean success = true;
                if (request.getErrorCode() == SubstitutionplanFetcher.SubstitutionplanResponse.NO_ERROR) {
                    String json = request.getData();
                    if (!SharedPreferencesManager.getSharedPreferences().getString(SHR_SUBSITUTIONPLAN_DATA, "").equals(json)) {
                        //SharedPreferencesManager.getSharedPreferences().edit().putString(SHR_SUBSITUTIONPLAN_DATA, json).commit();
                        displaySubstitutionplan(json);
                    }

                }else{
                    DebugUtil.errorLog(TAG, "Error while trying to reload subsitutions: " + request.getErrorCode() + "/" + request.getData());
                    success = false;
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.substplan_network_error), Toast.LENGTH_SHORT).show();
                }

                FabricHandler.logCustomEvent(new CustomEvent("Reloaded Substitutionplan").putCustomAttribute("success", success + ""));

                swipeContainer.setRefreshing(false);
            }
        });
    }

    /**
     * Displays the substitution plan without updating anything
     * @param json The substitution plan as a json string, may contain no substitution
     */
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


    /**
     * Displays the substitution plan when there is at least one substitution
     * @param coverLessons The substitution plan, must contain at least one substitution
     */
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


    /**
     * Displays the substitution plan when there is no substitution
     */
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






    private static class SubstitutionData {
        @Getter
        private String subject, teacher, subTeacher, subSubject, subRoom, classes, comment, hour;
        @Getter
        private boolean highlightSubject, highlightTeacher;


        public SubstitutionData(JSONObject jsonData) throws JSONException {
            subject = jsonData.getString("subject").trim();
            teacher = jsonData.getString("teacher").trim();
            subTeacher = jsonData.getString("subteacher").trim();
            subSubject = jsonData.getString("subsubject").trim();
            subRoom = jsonData.getString("subroom").trim();
            classes = jsonData.getString("classes").trim();
            comment = jsonData.getString("comment").trim();
            hour = jsonData.getString("hour").trim();




            /*
        Display no text instead of 3 bars for a clear field
         */
            if (subTeacher.equals("---")) subTeacher = "";
            if (subSubject.equals("---")) subSubject = "";
            if (subRoom.equals("---")) subRoom = "";



        /*
        If subject or teacher stay the same, show now crossed-out text, else add a space between
        the crossed-out and the new text
         */
            if (subSubject.equalsIgnoreCase(subject)) subject = "";
            else highlightSubject = true;
            //else subject = subject + ' ';
            if (subTeacher.equalsIgnoreCase(teacher)) teacher = "";
            else highlightTeacher = true;
            //else teacher = teacher + ' ';



        }



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







    private static class SubstitutionDataViewHolder extends RecyclerView.ViewHolder {
        private TextView hourView;
        private TextView teacherView;
        private TextView subjectView;
        private TextView roomView;
        private TextView commentView;

        //private LinearLayout roomLayout;
        //private LinearLayout commentLayout;
        private TableLayout table;


        private SubstitutionDataViewHolder(View itemView) {
            super(itemView);

            hourView = (TextView) itemView.findViewById(R.id.substitution_card_hour);
            teacherView = (TextView) itemView.findViewById(R.id.substitution_card_teacher);
            subjectView = (TextView) itemView.findViewById(R.id.substitution_card_subject);
            roomView = (TextView) itemView.findViewById(R.id.substitution_card_room);
            commentView = (TextView) itemView.findViewById(R.id.substitution_card_comment);

            //roomLayout = (LinearLayout) itemView.findViewById(R.id.substitution_layout_room);
            //commentLayout = (LinearLayout) itemView.findViewById(R.id.substitution_layout_comment);
            table = (TableLayout) itemView.findViewById(R.id.substitution_card_table);


        }

        private void initialize(SubstitutionData data) {
            hourView.setText(data.getHour());

            Spannable teacherViewSpannable = null;

            if(displayTeacherName(data)){
                if(!data.getTeacher().isEmpty()){
                    teacherViewSpannable = new SpannableString(data.getTeacher() + ' ' + data.getSubTeacher());
                    teacherViewSpannable.setSpan(new StrikethroughSpan(), 0, data.getTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //teacherView.setText(data.getTeacher() + ' ' + data.getSubTeacher(), TextView.BufferType.SPANNABLE);
                    //Spannable spannable = (Spannable) teacherView.getText();
                    //spannable.setSpan(new StrikethroughSpan(), 0, data.getTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }else{
                    teacherViewSpannable = new SpannableString(data.getSubTeacher());
                    //teacherView.setText(data.getSubTeacher());
                }
            }else if(!data.subTeacher.isEmpty()){
                teacherViewSpannable = new SpannableString(data.getSubTeacher());
                //teacherView.setText(data.subTeacher);
            }
            else table.removeView(table.findViewById(R.id.substitution_card_teacher_row));

            if(teacherViewSpannable!=null) {
                if (data.isHighlightTeacher()) {
                    if (data.getTeacher().isEmpty())
                        teacherViewSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, data.getSubTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    else
                        teacherViewSpannable.setSpan(new StyleSpan(Typeface.BOLD), data.getTeacher().length() + 1, data.getTeacher().length() + 1 + data.getSubTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //Spannable spannable = (Spannable) teacherView.getText();
                    //if(data.getTeacher().isEmpty()) spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, data.getSubTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    //else spannable.setSpan(new StyleSpan(Typeface.BOLD), data.getTeacher().length()+1, data.getTeacher().length()+1 + data.getSubTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                teacherView.setText(teacherViewSpannable);
            }



            Spannable subjectSpannable;
            if(!data.getSubject().isEmpty()){
                subjectSpannable = new SpannableString(data.getSubject() + ' ' + data.getSubSubject());
                subjectSpannable.setSpan(new StrikethroughSpan(), 0, data.getSubject().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //subjectView.setText(data.getSubject() + ' ' + data.getSubSubject(), TextView.BufferType.SPANNABLE);
                //Spannable spannable = (Spannable) subjectView.getText();
                //spannable.setSpan(new StrikethroughSpan(), 0, data.getSubject().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if(data.isHighlightSubject()){
                    subjectSpannable.setSpan(new StyleSpan(Typeface.BOLD), data.getSubject().length()+1, data.getSubject().length()+1+data.getSubSubject().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            else{
                subjectSpannable = new SpannableString(data.getSubSubject());
                //subjectView.setText(data.getSubSubject());
                if(data.isHighlightSubject()){
                    subjectSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, data.getSubSubject().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            subjectView.setText(subjectSpannable);






            if(data.getSubRoom()!="") roomView.setText(data.getSubRoom());
            else table.removeView(table.findViewById(R.id.substitution_card_room_row));

            commentView.setText(data.getComment());




            /*hourView.setText(data.getHour());
            //teacherView.setText(data.getTeacher() + " " + data.getSubTeacher(), TextView.BufferType.SPANNABLE);
            subjectView.setText(data.getSubject() + data.getSubSubject(), TextView.BufferType.SPANNABLE);
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
                if(displayTeacherName(data)) {
                    teacherView.setText(data.getTeacher() + data.getSubTeacher(), TextView.BufferType.SPANNABLE);
                    Spannable span = (Spannable) teacherView.getText();
                    span.setSpan(new StrikethroughSpan(), 0, data.getTeacher().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }else{
                    teacherView.setText(data.getSubTeacher(), TextView.BufferType.SPANNABLE);
                }
            }

            if (!data.getSubTeacher().isEmpty()) {
                Spannable span = (Spannable) subjectView.getText();
                span.setSpan(new StrikethroughSpan(), 0, data.getSubject().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }*/
        }

        private boolean displayTeacherName(SubstitutionData data){
            if(data.getClasses().equalsIgnoreCase("11") || data.getClasses().equalsIgnoreCase("12")
                    || data.getSubject().equalsIgnoreCase("ev") || data.getSubject().equalsIgnoreCase("rk")
                    || data.getSubject().equalsIgnoreCase("sp") || data.getSubject().equalsIgnoreCase("nwt")
                    || data.getSubject().equalsIgnoreCase("f") || data.getSubject().equalsIgnoreCase("or"))
                return true;
            return false;
        }
    }

}
