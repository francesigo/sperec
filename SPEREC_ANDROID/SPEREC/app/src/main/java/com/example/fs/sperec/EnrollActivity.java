package com.example.fs.sperec;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import sperec_common.AudioMonitor;
import sperec_common.SPEREC;
import sperec_common.SimpleSpeakerIdentity;
import sperec_common.SpeakerModel;

public class EnrollActivity extends AppCompatActivity {
    SPEREC oSperec = null;
    AudioDispatcher mainAudioDispatcher = null;

    MyVolumeMonitor myVolumeMonitor = null;
    ProgressBar volumeBar = null;

    SperecAndroidWrapper SAW = null;
    MyTimer myTimer = null;
    SimpleSpeakerIdentity currentIdentity = null;

    // GUI
    Button btnStartRec = null;
    Button btnStopRec = null;
    TextView txt_timer = null;
    TextView txtUserName = null;
    AudioMonitor AM = null; // For the volume monitoring
    TextView logBar = null;


    MyLog MLog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        // Get the configuration from the caller Activity
        SAW = (SperecAndroidWrapper) EventBus.getDefault().removeStickyEvent(SperecAndroidWrapper.class);

        // Configure here
        oSperec = SAW.oSperec;

        // Set the logbar
        logBar = findViewById(R.id.statusBar);
        logBar.setText("");

        // Create my logger
        MLog = new MyLog("SPEREC_ENROLL ACTIVITY", null);
        MLog.setLogDestinationTextView(logBar);

        MLog.i("READY TO ENROLL");


        // Reset the volume bar
        volumeBar = findViewById(R.id.progressBar);
        volumeBar.setProgress(0);

        /////////////// BUTTONS
        btnStartRec = (Button) findViewById(R.id.btnStartRec);
        btnStartRec.setEnabled(true);
        btnStopRec = (Button) findViewById(R.id.btnStopRec);
        btnStopRec.setEnabled(false);

        // BUTTON START (Enroll)
        btnStartRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the username from the GUI
                String userName = txtUserName.getText().toString();

                // Build a simple identity card with that username
                try {
                    currentIdentity = new SimpleSpeakerIdentity(userName);
                } catch (Exception e) {
                    MLog.e("INTERNAL ERROR: Can not instantiate SimpleSpeakerIdentity. " + e.getMessage(), e);
                    currentIdentity = null;
                    return;
                }

                // Check if the that identity was enrolled before
                boolean found = oSperec.getEnrolledSpeakersDatabase().hasIdentity(currentIdentity);

                if (found) {
                    MLog.i("User " + userName + " was already enrolled. Please choose a different username");
                } else {
                    // Ready: update the GUI: i.e. turn the start rec off, turn the stop rec on
                    btnStartRec.setEnabled(false);
                    btnStopRec.setEnabled(true);

                    // Start the time
                    myTimer.startTimer();

                    AM = new AudioMonitor();

                    // Define the recording thread
                    Thread recordingThread = defineRecordingThread(currentIdentity);

                    // Start the recording thread
                    recordingThread.start();

                    myVolumeMonitor = new MyVolumeMonitor(AM, volumeBar);
                    myVolumeMonitor.start();
                }
            }
        });

        // BUTTON STOP
        btnStopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MLog.i("TRYING TO STOP RECORDING...");

                // Stop the audio dispatcher
                if (null != mainAudioDispatcher) {
                    mainAudioDispatcher.stop();
                    MLog.i("STOPPED");
                }

                // Stop the timer
                myTimer.stopTimer();

                myVolumeMonitor.stop();

                // Update the GUI: enable the start rec button, disable the stop rec button
                btnStopRec.setEnabled(false);
                btnStartRec.setEnabled(true);
            }
        });

        // Initialize the timer
        txt_timer = (TextView) findViewById(R.id.txt_timer);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        myTimer = new MyTimer(txt_timer);
    }

    /**
     *
     * @return
     */
    Thread defineRecordingThread(final SimpleSpeakerIdentity speakerIdentity) {

        Thread recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                mainAudioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(11025, 2048, 0);

                // Attach an audio monitor
                mainAudioDispatcher.addAudioProcessor(AM);

                try {
                    // Build the identity inside the thread
                    SpeakerModel spkModel = oSperec.enrollFromSpeech(mainAudioDispatcher, speakerIdentity);
                    if (null == spkModel)
                        MLog.e("Could not enroll. Try again.", null);

                } catch (Exception e) {
                    MLog.e("INTERNAL ERROR: Can not enroll the speaker. " + e.getMessage(), e);
                }
            }
        }, "Enrollment Recording Thread");

        return recordingThread;
    }
}