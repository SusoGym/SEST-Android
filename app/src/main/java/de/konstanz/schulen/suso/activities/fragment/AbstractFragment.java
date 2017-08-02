package de.konstanz.schulen.suso.activities.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public abstract class AbstractFragment extends Fragment {

    // SparseArray = Hashmap<Int, Object>
    private static SparseArray<Class<? extends AbstractFragment>> navbarFragment = new SparseArray<>();
    private static ArrayList<Integer> specialIds = new ArrayList<>();

    private static Activity masterActivity;
    private static final String TAG = AbstractFragment.class.getSimpleName();

    public static void registerFragment(Class<? extends AbstractFragment>... classes)
    {

        for (Class<? extends AbstractFragment> clazz : classes)
        {
            try{
                AbstractFragment fragment = clazz.newInstance();

                navbarFragment.put(fragment.getNavigationId(), clazz);
            }catch (Exception e)
            {
                Log.e(TAG, "Error while trying to register AbstractFragment " + clazz.getCanonicalName() + ": " + e.getMessage());
            }
        }

    }

    public static void registerSpecialNavigationElement(int... id)
    {
        for (int i: id ) {
            specialIds.add(i);
        }
    }

    public static boolean isValid(int id)
    {
        return getByNavbarItem(id) != null || specialIds.contains(id);
    }

    public static Class<? extends AbstractFragment> getByNavbarItem(int id) {
        return navbarFragment.get(id);
    }

    public static void setMasterActivity(Activity activity) {
        masterActivity = activity;
    }

    private int layoutId;
    private String title;
    private int navigationId;

    public AbstractFragment(int layout, int navigationId, String title) {
        this.layoutId = layout;
        this.title = title;
        this.navigationId = navigationId;
    }

    public AbstractFragment(int layout, int navigationId, int title) {
        this(layout, navigationId, null);
        if (masterActivity != null) {
            this.title = masterActivity.getString(title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layoutId for this fragment
        return inflater.inflate(layoutId, container, false);
    }

    public int getNavigationId() {
        return navigationId;
    }

    public String getTitle() {
        return title;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public abstract void refresh();

}
