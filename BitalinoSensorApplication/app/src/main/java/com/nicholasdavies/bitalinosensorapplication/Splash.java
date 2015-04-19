package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;

/**
 * Splash Screen
 *
 * @author Nick Davies
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** Displays an image then sleeps for a few seconds*/
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                } finally {
                    Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
                    startActivity(openStartingPoint);
                }

            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
