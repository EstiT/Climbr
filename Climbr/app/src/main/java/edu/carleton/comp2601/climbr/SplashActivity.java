package edu.carleton.comp2601.climbr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = (ImageView)findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);

        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {

                    sleep(2000);  //Delay of 10 seconds
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    Intent i = new Intent(SplashActivity.this,
                            LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
}
