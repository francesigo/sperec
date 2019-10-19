package com.example.fs.sperec;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import sperec_common.AudioMonitor;
import sperec_common.AuthenticationResult;
import sperec_common.IEnrolledSpeakersDatabase;
import sperec_common.SPEREC;
import sperec_common.SpeakerIdentity;
import sperec_common.SpeakerModel;

public class TestActivity extends AppCompatActivity {
    SPEREC oSperec = null;
    SperecAndroidWrapper SAW = null;
    //Context ctx = null; USELESS
    SpeakerModel referenceSpeakerModel = null;
    SpeakerIdentity referenceSpeakerIdentity = null;
    AudioDispatcher mainAudioDispatcher = null;
    AudioMonitor AM = null; // For the volume monitoring

    // GUI
    Button btnStartRec = null;
    Button btnStopRec = null;
    TextView txt_timer = null;
    Spinner uiUserList = null;
    MyTimer myTimer = null;
    MyVolumeMonitor myVolumeMonitor = null;
    ProgressBar volumeBar = null;
    MyLog MLog = null;
    TextView logBar = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Get the configuration from the calling Activity
        SAW = EventBus.getDefault().removeStickyEvent(SperecAndroidWrapper.class);

        // Configure here
        oSperec      = SAW.oSperec;
        // ctx          = SAW.mainActivity.getApplicationContext();
        final IEnrolledSpeakersDatabase users = oSperec.getEnrolledSpeakersDatabase();

        // Set the logbar
        logBar = findViewById(R.id.statusBar);
        logBar.setText("");

        MLog = new MyLog("SPEREC_AUTHENTICATION ACTIVITY", null);
        MLog.setLogDestinationTextView(logBar);


        // Reset the volume bar
        volumeBar = findViewById(R.id.progressBar);
        volumeBar.setProgress(0);

        //////////////////// GUI USER LIST
        uiUserList = findViewById(R.id.uiUserList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users.getSpeakerIdentitiesStrings());
        uiUserList.setAdapter(adapter);
        uiUserList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected username
                String userName = (String) parent.getItemAtPosition(position);
                Log.v("item", userName);
                // Load speaker model from file
                referenceSpeakerIdentity = users.getSpeakerIdentityByUserName(userName);
                referenceSpeakerModel = users.getSpeaker(referenceSpeakerIdentity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        /////////////// BUTTONS
        btnStartRec = (Button) findViewById(R.id.btnStartRec);
        btnStartRec.setEnabled(true);
        btnStopRec = (Button) findViewById(R.id.btnStopRec);
        btnStopRec.setEnabled(false);

        btnStartRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_rec(referenceSpeakerIdentity);
            }
        });

        btnStopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop_rec();
            }
        });

        txt_timer=(TextView) findViewById(R.id.txt_timer);
        myTimer = new MyTimer(txt_timer);
    }

    /**
     * START THE THREAD TO MAKE AUDIO RECORDING AND MODEL COMPUTING
     */
    void start_rec(final SpeakerIdentity claimedId) {

        // Adjust UI
        btnStartRec.setEnabled(false);
        btnStopRec.setEnabled(true);

        // Start the timer
        myTimer.startTimer();

        AM = new AudioMonitor();

        // The thread
        Thread recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int sampleRate = 11025;
                mainAudioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, 2048, 0);

                // Attach an audio monitor
                mainAudioDispatcher.addAudioProcessor(AM);

                try {
                    final AuthenticationResult res = oSperec.authenticateFromSpeech(mainAudioDispatcher, claimedId);

                    // Show the results in the TestResult Activity
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Intent i = new Intent(TestActivity.this , TestResultActivity.class);
                            EventBus.getDefault().postSticky(res);
                            startActivity(i);
                        }
                    });
                } catch (Exception e) {
                    MLog.e("INTERNAL ERROR: Can not authenticate the speaker", e);
                }
            }
        }, "Authentication Recording Thread");
        recordingThread.start();

        myVolumeMonitor = new MyVolumeMonitor(AM, volumeBar);
        myVolumeMonitor.start();
    }


    /**
     *
     */
    void stop_rec() {
        // Adjust UI
        btnStopRec.setEnabled(false);
        btnStartRec.setEnabled(true);

        // Stop the timer
        myTimer.stopTimer();

        // Stop the audio dispatcher
        if (null!=mainAudioDispatcher) {
            mainAudioDispatcher.stop();
            MLog.i("STOPPED");
        }

        myVolumeMonitor.stop();
    }

}
