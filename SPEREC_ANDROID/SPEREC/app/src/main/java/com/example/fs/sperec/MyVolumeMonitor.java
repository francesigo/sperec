package com.example.fs.sperec;

import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import sperec_common.AudioMonitor;

public class MyVolumeMonitor {

    private AudioMonitor audioMonitor;
    private ProgressBar progressBar;
    private Handler timerHandler = null;
    private Runnable timerRunnable = null;
    String TAG = "MyVolumeMonitor";

    public MyVolumeMonitor(AudioMonitor am, ProgressBar pb) {
        this.audioMonitor = am;
        this.progressBar = pb;
        timerHandler = new Handler();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                double rms = audioMonitor.getRMS();
                int volume = (int)(rms*progressBar.getMax());
                progressBar.setProgress(volume);
                Log.i(TAG, ""+volume);
                timerHandler.postDelayed(this, 100);
            }
        };

    }

    public void start() {
        timerHandler.postDelayed(timerRunnable, 0);
    }
    public void stop() {
        timerHandler.removeCallbacks(timerRunnable);
    }
}
