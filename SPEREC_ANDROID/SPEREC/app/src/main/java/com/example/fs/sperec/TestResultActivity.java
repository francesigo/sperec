package com.example.fs.sperec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import sperec_common.AuthenticationResult;

public class TestResultActivity extends AppCompatActivity {

    TextView txtScore = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        AuthenticationResult res = (AuthenticationResult) EventBus.getDefault().removeStickyEvent(AuthenticationResult.class);
        double dScore = res.score;

        String displayText = res.msg + " - Score = " + res.score;

        txtScore=(TextView) findViewById(R.id.txtScore);
        txtScore.setText(displayText);

    }
}
