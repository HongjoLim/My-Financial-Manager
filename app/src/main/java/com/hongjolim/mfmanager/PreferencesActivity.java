package com.hongjolim.mfmanager;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Name: HONGJO Lim
 * Date: Apr 20th, 2018
 * Purpose: This class is to set preferences for the entire app for user to customize the running environments
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
