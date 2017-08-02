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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.fragment.AbstractFragment;
import de.konstanz.schulen.suso.activities.fragment.SubstitutionplanFragment;
import de.konstanz.schulen.suso.data.SubstitutionplanFetcher;
import de.konstanz.schulen.suso.substitutionplan_recyclerview.SubstitutionData;
import de.konstanz.schulen.suso.substitutionplan_recyclerview.SubstitutionDataAdapter;
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
                if(request.getStatusCode() == SubstitutionplanFetcher.SubstitutionplanResponse.STATUS_OK)
                {
                    String json = request.getPayload();
                    if(!sharedPreferences.getString(SHR_SUBSITUTIONPLAN_DATA, "").equals(json))
                    {
                        sharedPreferences.edit().putString(SHR_SUBSITUTIONPLAN_DATA, json).apply();
                        displaySubstitutionplan(json);
                    }
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.substplan_network_error), Toast.LENGTH_SHORT).show();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void displaySubstitutionplan(String json){

        LinearLayout substitutionplanContent = (LinearLayout) findViewById(R.id.content_substitutionplan);
        substitutionplanContent.removeAllViews();

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONObject coverLessons = jsonObject.getJSONObject("coverlessons");
            Iterator<String> substitutionDays = coverLessons.keys();
            //Iterate over the days
            while(substitutionDays.hasNext()){
                String dateKey = substitutionDays.next();
                JSONArray daySubstitutions = coverLessons.getJSONArray(dateKey);
                ArrayList<SubstitutionData> substitutions = new ArrayList<>();

                /*
                Get date information such as an easily readable string represantation or the day of week
                 */
                String dateString;

                DateFormat readFormat = new SimpleDateFormat("yyyyMMdd");
                DateFormat writeFormat = DateFormat.getDateInstance();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(readFormat.parse(dateKey));


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
                    dateString = dayOfWeek + ", " + writeFormat.format(calendar.getTime());

                for(int i = 0; i<daySubstitutions.length(); ++i){
                    substitutions.add(new SubstitutionData(daySubstitutions.getJSONObject(i)));
                }

                /*
                Create the UI and data interfaces necessary to represent a substitution day
                and fill them with the parsed substitutions
                 */
                SubstitutionDataAdapter adapter = new SubstitutionDataAdapter(substitutions);
                RecyclerView recyclerView = new RecyclerView(substitutionplanContent.getContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(substitutionplanContent.getContext()){
                    @Override
                    public boolean canScrollVertically(){ return false; }
                });
                recyclerView.setAdapter(adapter);

                TextView dateView = new TextView(substitutionplanContent.getContext());
                dateView.setGravity(Gravity.CENTER);
                dateView.setText(dateString);

                substitutionplanContent.addView(dateView);
                substitutionplanContent.addView(recyclerView);
            }

        } catch (JSONException e) {
            TextView infoView = new TextView(substitutionplanContent.getContext());
            infoView.setText(R.string.no_substitutions);
            infoView.setTextSize(25);
            infoView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            llp.setMargins(0, 40, 0, 0);
            infoView.setLayoutParams(llp);
            substitutionplanContent.addView(infoView);
        } catch (ParseException e) {
            Toast errorToast = Toast.makeText(MainActivity.this, getResources().getString(R.string.substplan_json_error), Toast.LENGTH_LONG);
            errorToast.show();
        }


    }
}
