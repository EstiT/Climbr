package edu.carleton.comp2601.climbr;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //display logo animation on splash page
        VideoView logo = (VideoView)findViewById(R.id.logo);
        String path= "android.resource://edu.carleton.comp2601.climbr/"+R.raw.anim_slow;
        logo.setVideoPath(path);

        logo.start();

        logo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                Thread welcomeThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            //bring the user into the app
                            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                };
                welcomeThread.start();
            }
        });

    }
}
