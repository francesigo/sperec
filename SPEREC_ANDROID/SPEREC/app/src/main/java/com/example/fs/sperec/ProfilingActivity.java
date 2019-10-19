package com.example.fs.sperec;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import sperec_common.SPEREC;
import sperec_common.VadSpecs;

public class ProfilingActivity extends AppCompatActivity {

    SPEREC oSperec = null;
    SperecAndroidWrapper SAW = null;


    Button btnProfilingStart = null;
    TextView logBar = null;
    Spinner dropdown = null;

    //double probingDuration = -1;
    double [] probingDuration_v = null;

    MyLog MLog = null;

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    updateProgress(msg.arg1, (String)msg.obj);
                default:
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiling);

        // Get the configuration from the caller Activity
        SAW = (SperecAndroidWrapper) EventBus.getDefault().removeStickyEvent(SperecAndroidWrapper.class);

        // Configure here
        oSperec = SAW.oSperec;


        setGUI();
    }

    void setGUI() {

        // Set the logbar
        logBar = findViewById(R.id.statusBar);
        logBar.setText("");

        // Set my logger
        MLog = new MyLog("SPEREC_PROFILING", null);
        MLog.setLogDestinationTextView(logBar);

        // Set the buttons and the actions
        setProfilingStartButton();

        // The dropdown
        Spinner dropdown = findViewById(R.id.probingSpinner);
        //create a list of items for the spinner.
        final double[] testDur_v = new double[]{2.0, 4.0, 8.0, 14.0};
        String [] items = new String[testDur_v.length+1];
        for (int i=0; i<testDur_v.length; i++)
            items[i] = ""+testDur_v[i];
        items[testDur_v.length] = "All";
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String selectedItem = (String)parent.getItemAtPosition(position);
                if (selectedItem.equals("All"))
                    probingDuration_v = testDur_v;
                else
                    probingDuration_v = new double[] {Double.parseDouble(selectedItem)};

                //probingDuration = (Double)parent.getItemAtPosition(position);
                String msg = "Selected duration: " + selectedItem + " [s]";
                logBar.setText(msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });





        // Welcome message
        MLog.i("WELCOME TO PROFILING");
    }

    void setProfilingStartButton() {
        btnProfilingStart = (Button) findViewById(R.id.buttonStart);


        btnProfilingStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        startProfiling();
                    }
                });
            }
        });
    }


    void startProfiling() {
        Profiling prof = null;

        //prof = initVadProfiling();
        prof = initMainProfiling();

        prof.ctx = this.getApplicationContext();
        prof.handler = handler;
        prof.runThread();
    }


    Profiling initMainProfiling() {
        MLog.i("Profiling FINAL STARTED...");
        ProfilingFinal prof = new ProfilingFinal();

        prof.oSperec = oSperec;
        // Indicare un nome simbolico per questo engine. Su un foglio di carte devo far corrispondere questo nome alle caratteristiche dell'engine
        prof.engine_str = "Engine0";

        prof.inputDirOrFileList = "";
        prof.logFileName = "/mnt/sdcard/final_profiling.log";

        prof.ref_inputFileList = "/mnt/sdcard/_DATA/toSPLIT/Monologhi_001_150_16000Hz/file.lst";
        prof.test_inputFileList = "/mnt/sdcard/_DATA/toSPLIT/Monologhi_001_150_16000Hz/file.lst";
        prof.enrollSessionDurationSec = 25.0; // di solito
        prof.testDurationSec_v = probingDuration_v; // variabile

        // Run limits
        prof.MAX_NUM_SPKS = 5;
        prof.MAX_NUM_SESSIONS = 1;

        return prof;
    }

    Profiling initVadProfiling() {
        MLog.i("ProfilingVAD STARTED...");
        ProfilingVAD prof = new ProfilingVAD();
        VadSpecs vadSpecs = new VadSpecs();
        vadSpecs.setMethod("MyAutocorrellatedVoiceActivityDetector");
        vadSpecs.setFrameIncrementSec(0.1);
        vadSpecs.setOverlapFactor(2.0);
        prof.vadSpecs = vadSpecs;
        //prof.inputDirOrFileList = "http://192.168.56.1/_DOTTORATO/_DATA/audioDataOrig/Monologhi_001_150/Monologhi_001_150_16000Hz/file.lst";
        prof.inputDirOrFileList = "/mnt/sdcard/_DATA/audioDataOrig/Monologhi_001_150/Monologhi_001_150_16000Hz";

        prof.logFileName = "/mnt/sdcard/vad_profiling.log";

        return prof;
    }



    void updateProgress(final int i, final String msg) {
        ProfilingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (i>=0) {
                    if (msg==null)
                        MLog.i("" + i + "%");
                    else
                        MLog.i(msg + " : " + i + "%");
                }
                else
                {
                    if (msg==null)
                        MLog.i("Profiling END");
                    else
                        MLog.i(msg);
                }

            }
        });

    }
}
