package de.konstanz.schulen.suso.substitutionplan_recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.konstanz.schulen.suso.MainActivity;
import de.konstanz.schulen.suso.R;


/**
 * Created by Jasper on 6/1/2017.
 */

public class SubstitutionDataAdapter extends RecyclerView.Adapter{
    private static final String HOUR = "Stunde: ";
    private static final String TEACHER = "Lehrer: ";
    private static final String SUBJECT = "Fach: ";
    private static final String ROOM = "Raum: ";
    private static final String COMMENT = "Kommentar: ";

    private List<SubstitutionData> substitutions;


    public SubstitutionDataAdapter(List<SubstitutionData> substitutions){
        this.substitutions = substitutions;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_substitutionplan, parent, false);
        SubstitutionDataViewHolder viewHolder = new SubstitutionDataViewHolder(view);
        return viewHolder;
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
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }




    public static class SubstitutionDataViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView textView;

        public SubstitutionDataViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.substitution_cardview);
            textView = (TextView) itemView.findViewById(R.id.substitution_textview);

        }

        void initialize(SubstitutionData data){
            //TODO Create textview content
            textView.setText(HOUR + data.getHour() + "\n" +
                            TEACHER + data.getTeacher() + data.getSubTeacher() + '\n' +
                            SUBJECT + data.getSubject() + data.getSubSubject() + '\n' +
                            (data.getSubRoom().isEmpty() ? "" : ROOM + data.getSubRoom() + '\n') +
                            COMMENT + data.getComment(), TextView.BufferType.SPANNABLE);

            Spannable spannable = (Spannable) textView.getText();

            int teacherIndex = 17 + data.getHour().length();
            int teacherEnd = teacherIndex + data.getTeacher().length();

            int subjectIndex = teacherEnd + data.getSubTeacher().length() + 7;
            int subjectEnd = subjectIndex + data.getSubject().length();
            if(!data.getTeacher().isEmpty()) spannable.setSpan(new StrikethroughSpan(), teacherIndex, teacherEnd-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(!data.getSubject().isEmpty()) spannable.setSpan(new StrikethroughSpan(), subjectIndex, subjectEnd-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
