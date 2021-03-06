package com.hashbang.coffeenerd;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.hashbang.coffeepro.R;

public class MainActivity extends SherlockFragmentActivity
{

    // ListView click listener in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id)
        {
            selectItem(position);
        }
    }

    // Declare Variables
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    MenuListAdapter mMenuAdapter;
    String[] title;
    String[] subtitle;
    String themeVal = "dark";
    boolean isPortrait = true;

    int[] icon;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String[] mCoffeeTypes;

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
        
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        isPortrait = sharedPref.getBoolean(PreferencesFragment.PERSPECTIVE_TAG, true);
        if(isPortrait)
        {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else
        {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        
        themeVal = sharedPref.getString(PreferencesFragment.THEME_TAG, "dark");
        // Get the view from drawer_main.xml
        if("light".equalsIgnoreCase(themeVal))
        {
        	setTheme(R.style.LightTheme);
        }
        else
        {
        	setTheme(R.style.DarkTheme);
        }
        setContentView(R.layout.main_activity);

        // Get the Title
        mTitle = mDrawerTitle = getTitle();

        mCoffeeTypes = getResources().getStringArray(R.array.coffee_options);
        // Generate title

        // Generate icon
        icon = new int[]
        { R.drawable.french, R.drawable.chemex, R.drawable.siphon,
                R.drawable.aero,R.drawable.hario, R.drawable.drip, R.drawable.custom_timer,R.drawable.ic_sysbar, R.drawable.references, R.drawable.ic_menu_info_details };

        // Locate DrawerLayout in drawer_main.xml
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Locate ListView in drawer_main.xml
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
                if("light".equalsIgnoreCase(themeVal))
        {
        	mDrawerList.setBackgroundColor(Color.GRAY);
        }

        // Set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        // Pass string arrays to MenuListAdapter
        mMenuAdapter = new MenuListAdapter(MainActivity.this, mCoffeeTypes,
                icon);

        // Set the MenuListAdapter to the ListView
        mDrawerList.setAdapter(mMenuAdapter);

        // Capture listview menu item click
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close)
        {

            @Override
            public void onDrawerClosed(View view)
            {
                // TODO Auto-generated method stub
                getActionBar().setTitle(mTitle);
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                // TODO Auto-generated method stub
                // Set the title on the action when drawer open
                getSupportActionBar().setTitle(mDrawerTitle);
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if(savedInstanceState == null)
        {
            selectItem(9); //About Fragment
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if(item.getItemId() == android.R.id.home)
        {

            if(mDrawerLayout.isDrawerOpen(mDrawerList))
            {
                mDrawerLayout.closeDrawer(mDrawerList);
            }
            else
            {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private void selectItem(int position)
    {

        mDrawerList.setItemChecked(position, true);
        FragmentManager fragmentManager = getFragmentManager();
        setTitle(mCoffeeTypes[position]);
        if(position < 6)
        {
        	CoffeeFragment cf = CoffeeFragment.newInstance(position);
            if("light".equalsIgnoreCase(themeVal))
            {
            	cf.setThemeColour(Color.BLACK);
            }
            else
            {
            	cf.setThemeColour(Color.WHITE);
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, cf)
                    .commit();
        }
        else if(position == 6)
        {
        	CustomTimerFragment ctf = CustomTimerFragment.newInstance();
        	if("light".equalsIgnoreCase(themeVal))
            {
        		ctf.setThemeColour(Color.BLACK);
            }
            else
            {
            	ctf.setThemeColour(Color.WHITE);
            }
        	 fragmentManager.beginTransaction().replace(R.id.content_frame, ctf)
             .commit();
        }
        else if(position == 7) //Preferences Fragment
        {
        	PreferencesFragment pf = PreferencesFragment.newInstance();
        	 fragmentManager.beginTransaction().replace(R.id.content_frame, pf)
             .commit();
        }
        else if(position == 8) //References Fragment
        {
        	ReferencesFragment rf = ReferencesFragment.newInstance();
        	 fragmentManager.beginTransaction().replace(R.id.content_frame, rf)
             .commit();
        }
        else //Show About Fragment
        {
           AboutFragment af = AboutFragment.newInstance();
           fragmentManager.beginTransaction().replace(R.id.content_frame, af)
           .commit();
        }
        mDrawerLayout.closeDrawer(mDrawerList);
        mDrawerList.setItemChecked(position, true);
        // Close drawer
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
}