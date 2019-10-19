package com.example.fs.sperec;

import android.os.Handler;
import android.widget.TextView;

/**
 * Created by FS on 26/12/2017.
 */

public class MyTimer {

    private TextView txt_timer = null;
    private long startTime = 0;
    private Handler timerHandler = null;
    private Runnable timerRunnable = null;

    public MyTimer(final TextView txt_timer) {
        timerHandler = new Handler();
        this.txt_timer = txt_timer;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millisecondi = System.currentTimeMillis() - startTime;
                int secondi = (int) (millisecondi / 1000);
                int minuti = secondi / 60;
                secondi = secondi % 60;
                txt_timer.setText(String.format("%d:%02d", minuti, secondi));
                timerHandler.postDelayed(this, 500);
            }
        };
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }
    public void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
}
