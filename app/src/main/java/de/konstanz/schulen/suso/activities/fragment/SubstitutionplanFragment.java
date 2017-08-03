package de.konstanz.schulen.suso.activities.fragment;


import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.MainActivity;

public class SubstitutionplanFragment extends AbstractFragment
{

    public SubstitutionplanFragment()
    {
        super(R.layout.fragment_substitutionplan, R.id.nav_substitutionplan, R.string.nav_substitutionplan);
    }

    @Override
    public void refresh()
    {
        if(!(getActivity() instanceof MainActivity))
            return;

        MainActivity a = (MainActivity)getActivity();

        a.updateSubstitutionplan();


    }
}
