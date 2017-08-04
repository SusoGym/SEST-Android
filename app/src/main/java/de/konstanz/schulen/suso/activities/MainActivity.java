package de.konstanz.schulen.suso.activities;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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

import de.konstanz.schulen.suso.BuildConfig;
import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.fragment.AbstractFragment;
import de.konstanz.schulen.suso.activities.fragment.SubstitutionplanFragment;
import de.konstanz.schulen.suso.data.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;

import static de.konstanz.schulen.suso.util.SharedPreferencesManager.SHR_PASSWORD;
import static de.konstanz.schulen.suso.util.SharedPreferencesManager.SHR_SUBSITUTIONPLAN_DATA;
import static de.konstanz.schulen.suso.util.SharedPreferencesManager.SHR_USERNAME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static {
        // We need to register our AbstractFragments here
        AbstractFragment.registerFragment(SubstitutionplanFragment.class);
        // Do we have some drawer items that are not connected with an AbstractFragment? Add them here
        AbstractFragment.registerSpecialNavigationElement(R.id.nav_logout);
    }


    private static final String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences = SharedPreferencesManager.getSharedPreferences();

    private DrawerLayout navigationDrawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private AbstractFragment currentFragment;
    private SwipeRefreshLayout swipeContainer;

    private @NonNull String username = sharedPreferences.getString(SHR_USERNAME, null);
    private @NonNull String password = sharedPreferences.getString(SHR_PASSWORD, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainerSubstitutionplan);
        navigationView = (NavigationView) findViewById(R.id.nav_view);


        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, navigationDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        AbstractFragment.setMasterActivity(this);

        //Add the view fragment
        setActiveFragment(SubstitutionplanFragment.class);

        navigationView.setCheckedItem(currentFragment.getNavigationId());


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentFragment.refresh();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        disableUnknownNavItems();

    }

    public void disableUnknownNavItems()
    {
        Menu menu = navigationView.getMenu();

        for(int i = 0; i < menu.size(); i++)
        {
            MenuItem mI = menu.getItem(i);

            if(!AbstractFragment.isValid(mI.getItemId()))
                mI.setEnabled(false);
        }
    }


    public void setActiveFragment(Class<? extends AbstractFragment> fragment)
    {

        Log.d(TAG, "Setting ActiveFragment to instance of " + fragment.getCanonicalName());

        AbstractFragment frgmt;
        try{
            frgmt = fragment.newInstance();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if(currentFragment == null)
        {
            transaction.replace(R.id.content_main, frgmt);
        } else {
            transaction.add(R.id.content_main, frgmt);
        }

        currentFragment = frgmt;

        transaction.commit();
        fragmentManager.executePendingTransactions();

    }

    @Override
    public void onStart(){

        super.onStart();
        showSubstitutionplan();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Class<? extends AbstractFragment> clazz = AbstractFragment.getByNavbarItem(id);

        if(currentFragment.getNavigationId() == id)
        { // if choose current selected fragment -> reload
            currentFragment.refresh();

        } else if(clazz != null)
        { // selected new fragment
            setActiveFragment(clazz);
        } else if(id==R.id.nav_logout)
        { // selected logged out TODO: add smoother way of handling this
            sharedPreferences.edit().remove(SHR_PASSWORD).remove(SHR_USERNAME).apply();

            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            startActivity(intent);
            finish();
            return false;
        } else { // unknown menu item
            Toast.makeText(this, "Unhandled drawer item!", Toast.LENGTH_LONG).show();
            return false;
        }


        navigationDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSubstitutionplan(){

        String savedSubstitutionplanData = sharedPreferences.getString(SHR_SUBSITUTIONPLAN_DATA, null);
        if (savedSubstitutionplanData != null) {
            displaySubstitutionplan(savedSubstitutionplanData);
        }

        updateSubstitutionplan();
    }

    public void updateSubstitutionplan(){
        SubstitutionplanFetcher.fetchAsync(this.username, this.password, this, new Callback<SubstitutionplanFetcher.SubstitutionplanResponse>() {
            @Override
            public void callback(SubstitutionplanFetcher.SubstitutionplanResponse request) {
                boolean success = true;
                if(request.getStatusCode() == SubstitutionplanFetcher.SubstitutionplanResponse.STATUS_OK)
                {
                    String json = request.getPayload();
                    if(!sharedPreferences.getString(SHR_SUBSITUTIONPLAN_DATA, "").equals(json))
                    {
                        sharedPreferences.edit().putString(SHR_SUBSITUTIONPLAN_DATA, json).apply();
                        displaySubstitutionplan(json);
                    }
                } else {
                    success = false;
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.substplan_network_error), Toast.LENGTH_SHORT).show();
                }
                if(!BuildConfig.DEBUG_MODE)
                {
                    Answers.getInstance().logCustom(new CustomEvent("Reloaded Substitutionplan").putCustomAttribute("success", success + ""));
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void displaySubstitutionplan(String json){

        if(!(currentFragment instanceof SubstitutionplanFragment))
            return;

        SubstitutionplanFragment f = (SubstitutionplanFragment)currentFragment;

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONObject coverLessons = jsonObject.getJSONObject("coverlessons");
            f.displaySubsitutionplan(coverLessons);

        } catch (JSONException e) {
            f.displayNoSubstitution();
        }


    }
}
