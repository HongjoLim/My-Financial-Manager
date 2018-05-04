package com.example.hongjo.myfinancialmanager;

import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * this class is settings preferences for the entire app for user to select app running environment
 * Author: Hongjo Lim
 * Date: April 20th, 2018
 * Time: 16:45 EST
 */

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getFragmentManager().beginTransaction().add(R.id.prefs_content, new SettingsFragment()).commit();

    }

    public static class SettingsFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.settings);
        }
    }
}
