package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private MediaPlayer startPlayer;
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);  // your splash layout

        playStartSoundAndThenOpenMain();
    }

    private void playStartSoundAndThenOpenMain() {
        try {
            startPlayer = MediaPlayer.create(this, R.raw.app_start);

            if (startPlayer == null) {
                // If something goes wrong, just open main
                openMainScreen();
                return;
            }

            // When the sound finishes, go to MainActivity
            startPlayer.setOnCompletionListener(mp -> {
                mp.release();
                startPlayer = null;
                openMainScreen();
            });

            startPlayer.start();

        } catch (Exception e) {
            // If any error, just go to main
            openMainScreen();
        }
    }

    private void openMainScreen() {
        if (hasNavigated) return;   // avoid double navigation
        hasNavigated = true;

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (startPlayer != null) {
            if (startPlayer.isPlaying()) {
                startPlayer.stop();
            }
            startPlayer.release();
            startPlayer = null;
        }
    }
}
