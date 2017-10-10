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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.fragment.AbstractFragment;
import de.konstanz.schulen.suso.activities.fragment.BlogFragment;
import de.konstanz.schulen.suso.activities.fragment.SubstitutionplanFragment;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.DebugUtil;
import de.konstanz.schulen.suso.util.SharedPreferencesManager;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static {
        // We need to register our AbstractFragments here
        AbstractFragment.registerFragment(SubstitutionplanFragment.class, BlogFragment.class);
        // Do we have some drawer items that are not connected with an AbstractFragment? Add them here
        AbstractFragment.registerSpecialNavigationElement(R.id.nav_logout);
    }


    private static final String TAG = MainActivity.class.getSimpleName();

    private HashMap<Class<? extends AbstractFragment>, AbstractFragment> fragments = new HashMap<>();

    private SharedPreferences sharedPreferences = SharedPreferencesManager.getSharedPreferences();

    private DrawerLayout navigationDrawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private AbstractFragment currentFragment;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshContainer);
        navigationView = (NavigationView) findViewById(R.id.nav_view);


        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, navigationDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);


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

        AbstractFragment.setMasterActivity(this);

        //Add the view fragment
        setActiveFragment(SubstitutionplanFragment.class);

        navigationView.setCheckedItem(currentFragment.getNavigationId());

        disableUnknownNavItems();


    }

    @Override
    protected void onResume() {
        super.onResume();
        currentFragment.onPushToForeground();
    }

    public void disableUnknownNavItems() {
        Menu menu = navigationView.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem mI = menu.getItem(i);

            if (!AbstractFragment.isValid(mI.getItemId()))
                mI.setEnabled(false);
        }
    }


    public void setActiveFragment(Class<? extends AbstractFragment> fragment) {

        DebugUtil.infoLog(TAG, "Setting ActiveFragment to instance of " + fragment.getCanonicalName());

        AbstractFragment frgmt;
        boolean exists = false;

        if (fragments.containsKey(fragment)) {
            frgmt = fragments.get(fragment);
            exists = true;
        } else {

            try {
                frgmt = fragment.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.content_main, frgmt);

        currentFragment = frgmt;

        transaction.commit();
        fragmentManager.executePendingTransactions();

        if (!exists) {
            fragments.put(fragment, frgmt);
        }

        frgmt.onPushToForeground();

    }

    @Override
    public void onStart() {

        super.onStart();
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

        if (currentFragment.getNavigationId() == id) { // if choose current selected fragment -> reload
            currentFragment.refresh();

        } else if (clazz != null) { // selected new fragment
            setActiveFragment(clazz);
        } else if (id == R.id.nav_logout) { // selected logged out TODO: add smoother way of handling this
            AccountManager.getInstance().logout();

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

}
