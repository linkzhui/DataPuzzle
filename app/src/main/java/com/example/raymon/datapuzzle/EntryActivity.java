package com.example.raymon.datapuzzle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class EntryActivity extends Activity {
    private ViewPager myViewPager;
    private SlideAdapter slideAdapter;
    private TextView myButton;

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        myButton = (TextView) findViewById(R.id.id_button);

        prefs = getSharedPreferences("com.example.raymon.datapuzzle", MODE_PRIVATE);

        if (!prefs.getBoolean("firstrun", true)) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }
        else{
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit pref
            myViewPager = (ViewPager)findViewById(R.id.viewpager);
            slideAdapter = new SlideAdapter(this);
            myViewPager.setAdapter(slideAdapter);
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}