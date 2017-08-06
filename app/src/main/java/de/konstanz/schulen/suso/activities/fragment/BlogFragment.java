package de.konstanz.schulen.suso.activities.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.SusoApplication;


public class BlogFragment extends AbstractFragment {

    public BlogFragment() {
        super(R.layout.fragment_blog, R.id.nav_blog, R.string.nav_blog);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false);
    }


    @Override
    public void refresh()
    {

        boolean success = true;

        if(SusoApplication.USE_FABRIC)
        {
            Answers.getInstance().logCustom(new CustomEvent("Reloaded Blog").putCustomAttribute("success", success + ""));
        }

    }
}
