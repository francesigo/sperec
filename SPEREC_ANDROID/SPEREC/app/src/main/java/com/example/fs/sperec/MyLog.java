package com.example.fs.sperec;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MyLog {
    String TAG = null;
    Context ctx = null;
    TextView textView = null;

    public MyLog(String TAG, Context ctx) {
        this.TAG = TAG;
        this.ctx = ctx;
    }

    void setLogDestinationTextView(TextView textView) {
        this.textView = textView;
    }
    /**
     *
     * @param msg
     * @param e
     */
    void e(String msg, Exception e) {

        if (null!=e)
            e.printStackTrace();

        if (null!=msg) {
            if (null != ctx) {
                try {
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                } finally {
                }
            }
            if (null!=textView)
                textView.setText(msg);

            Log.e(TAG, msg);
        }

    }

    /**
     *
     * @param msg
     */
    void i(String msg) {

        if (null!= msg) {

            if (null != ctx)
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();

            if (null!=textView)
                textView.setText(msg);

            Log.i(TAG, msg);
        }
    }

}
