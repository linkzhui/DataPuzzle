package com.example.raymon.datapuzzle;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;

public class UserModeActivity extends FragmentActivity implements ActionBar.TabListener{


    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_mode);

        //create the adapter that will return a feature mode for user's choice
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        //Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

    }


    // When the given tab is selected, switch to the corresponding page in the ViewPager.
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }


    /**
     * AppSectionsPagerAdapter that returns a fragment corresponding to the user's choice
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                default:
                    // return individual mode to the user
                    return new IndividualModeFragment();

                case 1:
                    // return cooperate mode to the user
                    // The other sections of the app are dummy placeholders.
                    return new CooperateModeFragment();
            }
        }

        //number of mode is 2
        @Override
        public int getCount() {
            return 2;
        }


        //There will be two mode, they are individual mode and cooperate mode respectively
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position)
            {
                default:
                    return "INDIVIDUAL";
                case 1:
                    return  "COOPERATE";
            }
        }
    }

    //create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting, menu);
        return true;
    }

    //response to the menu item select
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

}


