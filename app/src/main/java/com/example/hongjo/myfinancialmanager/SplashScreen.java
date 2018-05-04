package com.example.hongjo.myfinancialmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

//this class is to show splash screen at the very start of this application
public class SplashScreen extends AppCompatActivity{

    //this static filed is to set the time that this splash screen is shown
    private final static long DELAY = 3000;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedStateInstance){
        super.onCreate(savedStateInstance);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable(){

            @Override
            public void run(){

                prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean login_enabled = prefs.getBoolean(LoginActivity.ENABLE_LOGIN, true);

                if(login_enabled){
                    Intent loginIntent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                }else{
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, DELAY);

    }
}
