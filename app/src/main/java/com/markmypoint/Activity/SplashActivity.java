package com.markmypoint.Activity;

import android.content.Intent;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;


import com.markmypoint.R;
import com.markmypoint.Utils.TypeWriter;

public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 5000;

    private TypeWriter mAppText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAppText=(TypeWriter) findViewById(R.id.app_text);





        //TypeWriter

        //Add a character every 150ms
        mAppText.setCharacterDelay(150);
        mAppText.animateText("Mark My Points");

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                // load the animation

                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
