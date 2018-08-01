package com.hongjolim.mfmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 *  Name: HONGJO LIM
 *  Date: Apr 6 2018
 *  Purpose: This class is to show splash screen when the app is started
 * */
public class SplashScreen extends AppCompatActivity{

    //static field for setting the delay
    private final static long DELAY = 3000;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedStateInstance){
        super.onCreate(savedStateInstance);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable(){

            @Override
            public void run(){

                //get the data from shared preference whether the user enabled the login function or not
                prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                boolean login_enabled = prefs.getBoolean(LoginActivity.ENABLE_LOGIN, true);

                //if the user has enabled the login function, call 'LoginActivity'
                if(login_enabled){
                    Intent loginIntent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                //if the login function is off, call 'MainActivity'
                }else{
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, DELAY);

    }
}
