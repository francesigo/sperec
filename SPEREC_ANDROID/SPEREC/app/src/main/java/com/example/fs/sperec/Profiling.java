package com.example.fs.sperec;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Profiling implements Runnable {
    Context ctx = null;
    String logFileName = "";
    Handler handler = null;
    String inputDirOrFileList = "";

    public Profiling() {
    }

    public void run() {
        try {
            do_work();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            updateProgress(-1, e.getMessage());
        }
    }

    abstract void do_work() throws Exception ;

    void updateProgress(int i) {
        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.arg1 = i;
        handler.sendMessage(msg);
    }
    void updateProgress(int i, String ms) {
        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.arg1 = i;
        msg.obj = ms;
        handler.sendMessage(msg);
    }

    void runThread() {
        Thread th = new Thread(this);
        th.start();
    }



    /**
     * Create a sequence from 0 to n-1
     * @param n
     * @return
     */
    static int [] sequence(int n) {
        int [] indexes = new int[n];
        for (int i=0; i<n; i++)
            indexes[i] = i;
        return indexes;
    }
    /**
     * Shuffle a sequence from 0 to n-1
     * @param n
     * @return
     */
    static int [] shuffle(int n) {
        int [] indexes = sequence(n);
        shuffleArray(indexes);
        return indexes;
    }
    private static void shuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random(); //ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
