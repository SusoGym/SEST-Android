package de.konstanz.schulen.suso;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import de.konstanz.schulen.suso.data.DownloadStringIntentService;
import de.konstanz.schulen.suso.substitutionplan_recyclerview.SubstitutionData;
import de.konstanz.schulen.suso.substitutionplan_recyclerview.SubstitutionDataAdapter;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BlogFragment.OnFragmentInteractionListener, SubstitutionplanFragment.OnFragmentInteractionListener{

    public static final int INTENT_REQUEST_UPDATE_SUBSTPLAN = 1;
    public static final String SUBSTITUTION_PLAN_DATA_KEY = "substitution_json";
    public static final String USERNAME_KEY = "nov_username";
    public static final String PASSWORD_KEY = "nov_password";

    //TODO TEST
    private String username = "EbertDan";
    private String password = "qatze2";

    DrawerLayout navigationDrawer;
    FragmentManager fragmentManager;
    Fragment currentFragment;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navigationDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        //Add the view fragment
        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        currentFragment = new SubstitutionplanFragment();
        transaction.add(R.id.content_main, currentFragment);
        transaction.commit();



        Button toolbarRefresh = (Button) findViewById(R.id.toolbar_refresh);
        toolbarRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentFragment instanceof SubstitutionplanFragment){
                    updateSubstitutionplan(username, password);
                }
            }
        });



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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id==R.id.nav_vertretungsplan){
            if(!(currentFragment instanceof SubstitutionplanFragment)){
                setViewFragment(new SubstitutionplanFragment());
                showSubstitutionplan();
            }
        }else if(id==R.id.nav_blog){
            if(!(currentFragment instanceof BlogFragment)){
                setViewFragment(new BlogFragment());
            }
        }


        navigationDrawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setViewFragment(Fragment fragment){
        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        currentFragment = fragment;
        transaction.replace(R.id.content_main, currentFragment);
        //TODO Optionally add to the back stack to make menu navigation reversible
        transaction.commit();
        fragmentManager.executePendingTransactions();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    private void showSubstitutionplan(){
        String savedSubstitutionplanData = sharedPreferences.getString(SUBSTITUTION_PLAN_DATA_KEY, "");
        if (!savedSubstitutionplanData.equals("")) {
            displaySubstitutionplan(savedSubstitutionplanData);
        }
        //TODO Add login UI
        updateSubstitutionplan(username, password);
    }

    private void updateSubstitutionplan(String username, String password){
        PendingIntent pendingResult = createPendingResult(INTENT_REQUEST_UPDATE_SUBSTPLAN, new Intent(), 0);
        Intent intent = new Intent(getApplicationContext(), DownloadStringIntentService.class);
        intent.putExtra(DownloadStringIntentService.USERNAME_EXTRA, username);
        intent.putExtra(DownloadStringIntentService.PASSWORD_EXTRA, password);
        intent.putExtra(DownloadStringIntentService.RESULT_INTENT_NAME, pendingResult);

        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode== INTENT_REQUEST_UPDATE_SUBSTPLAN){
            if(resultCode==DownloadStringIntentService.SUCCESSFUL_CODE){
                //Executed after the substitution plan has successfully been downloaded
                String json = data.getStringExtra(DownloadStringIntentService.RESULT_EXTRA);
                String savedJson = sharedPreferences.getString(SUBSTITUTION_PLAN_DATA_KEY, "");
                //Only do anything if the old and updated data doesn't match
                if(!json.equals(savedJson)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SUBSTITUTION_PLAN_DATA_KEY, json);
                    editor.commit();
                    displaySubstitutionplan(json);

                }
            }else if(resultCode==DownloadStringIntentService.ERROR_CODE){
                Toast errorToast = Toast.makeText(MainActivity.this, "Unable to download latest data. Please check your network connection.", Toast.LENGTH_SHORT);
                errorToast.show();
            }
        }

    }


    private void displaySubstitutionplan(String json){
        LinearLayout substitutionplanContent = (LinearLayout) findViewById(R.id.content_substitutionplan);
        substitutionplanContent.removeAllViews();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject coverLessons = jsonObject.getJSONObject("coverlessons");
            //TODO The order of the days might not be correct
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
                {
                    DateFormat readFormat = new SimpleDateFormat("yyyyMMdd");
                    DateFormat writeFormat = new SimpleDateFormat("dd.MM.yyyy");
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
                }
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
            e.printStackTrace();
            Toast errorToast = Toast.makeText(MainActivity.this, "Error at reading subsitutionplan (JSON). Data may not be displayed correctly", Toast.LENGTH_LONG);
            errorToast.show();
        } catch (ParseException e) {
            Toast errorToast = Toast.makeText(MainActivity.this, "Error at reading subsitutionplan dates (JSON). Data may not be displayed correctly", Toast.LENGTH_LONG);
            errorToast.show();
        }

    }
}
