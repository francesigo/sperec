package com.example.fs.sperec;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import tryTarsos.MyAudioDispatcherFactory;
//import com.example.fs.sperec.specs.SPEREC_Specs;
//import com.example.fs.sperec.specs.fea.MyFeatureSpecs;
//import com.example.fs.sperec.specs.fea.MyTarsosMFCC_Specs;

import java.io.File;
import java.io.IOException;


import be.tarsos.dsp.AudioDispatcher;


public class SPEREC_OLD {


    //public MyAudioInfo aInfoPostVad;
    private String TAG = "SPEREC_OLD"; // For LOG
    private Context ctx = null; // Android app context


    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub

        //demo();
    }

    public SPEREC_OLD(Context ctx) {
        this.ctx = ctx;
    }


    //----------------------------------------------------------------------------------------------
    /*private SPEREC_Specs demoDemoSperecSpecs() {

        SPEREC_Specs ss = new SPEREC_Specs();

        // USE MFCC TARSOS AS FEATURES
        ss.myFeaSpecs = new MyTarsosMFCC_Specs();

        return ss;
    }
*/
    //----------------------------------------------------------------------------------------------
    /*
    public void demo() {


        //SPEREC_Specs sperecSpecs = demoDemoSperecSpecs();

        // AudioDispatcher configuration
        String resName = "a20170622_140431";
        int audioBufferSizeForDispatcher = 2048; // Number of audio samples //TO DO A BETTER VALUE
        //int bufferOverlapForDispatcher = 0;
        int bufferOverlapForDispatcher = 1024;

        // VAD configuration
        final String VAD_TYPE = "VoiceActivityDetector";

        // Misc
        String errMsg = null;
        boolean err = false;


        // BEGIN .................................................................................
        Toast.makeText(ctx, "DEMO START", Toast.LENGTH_SHORT).show();


        // AudioDispatcher .......................................................................
        MyAudioDispatcherFactory myADF = new MyAudioDispatcherFactory();
        final AudioDispatcher dispatcher = myADF.fromAndroidResourceName(ctx, resName, audioBufferSizeForDispatcher, bufferOverlapForDispatcher);
        if (null==dispatcher) {
            err = true;
            errMsg = "Can not dispatch audio resource " + resName;
        }

        // VAD processor
        if (!err) {
            MyVAD_TarsosAudioProcessor vadProcessor = new MyVAD_TarsosAudioProcessor(VAD_TYPE, dispatcher.getFormat());
            dispatcher.addAudioProcessor(vadProcessor);
        }

        // Feature extraction
        //if (!err) {
          //  MyFeature_TarsosAudioProcessor feaTarsosProcessor = new MyFeature_TarsosAudioProcessor(sperecSpecs.myFeaSpecs);
            //dispatcher.addAudioProcessor(feaTarsosProcessor);//            float [] fea = myMFCC.process(aInfoPostVad);
        //}

        // RUN....................................................................................
        if (!err) {

            // Before to run, choose if debug or not
            myADF.setDebug(); // (uncomment to debug)

            // RUN
            dispatcher.run();
        }

        if (err) {
            Log.e(TAG, errMsg);
        }

    }
    */

    //**********************************


    //----------------------------------------------------------------------------------------------
    /*public void demo_old() {

        boolean err = false;
        String errMsg = null;

        MyAudioInfo aInfo = null;
        MyAudioInfo aInfoPostVad = null;

        // VAD
        final String VAD_TYPE = "VoiceActivityDetector";
        String resName = "a20170622_140431"; //"ciao";

        //Features
        final String FEA_TYPE = "MFCC_Tarsos";


        // DEBUG variables
        final boolean debug_VAD = false;
        final String VAD_OUT_FOLDER = VAD_TYPE;
        final String VAD_OUT_WAV_FILE = "VoiceActivityDetector_out.wav";


        // BEGIN .................................................................................
        Toast.makeText(ctx, "DEMO START", Toast.LENGTH_SHORT).show();

        // Find the resource
        int resId = ctx.getResources().getIdentifier(resName, "raw", ctx.getPackageName());

        if (resId<=0) {
            err = true;
            errMsg = "Can not find the resource " + resName;
        }

        // Audio decoding .........................................................................
        if (!err) {
            aInfo = MyAudioDecoder.decode(ctx, resId);

            if (null == aInfo) {
                err = true;
                errMsg = "Can not decode audio resource " + resName;
            } else aInfo.display(); // DEBUG
        }

        // Voice activity detection ................................................................
        // INPUT: aInfo
        if (!err) {
            MyVAD myVad = new MyVAD(VAD_TYPE);
            aInfoPostVad = aInfo.copy(); // Make a copy

            aInfoPostVad.data_b = myVad.process(aInfo); // put the vadded data into data_b
        }
        if (!err && debug_VAD) { // DEBUG VAD
            // Save a wav file to check the result
            String outFileFullPath = getFullPath(VAD_OUT_FOLDER, VAD_OUT_WAV_FILE);
            saveAudioSamplesAsWaveFile(aInfoPostVad, outFileFullPath);
            Log.i(TAG, "WAV: " + outFileFullPath);
            Toast.makeText(ctx, outFileFullPath, Toast.LENGTH_SHORT).show();
            new PostAudioFinalChunkTask().execute(aInfoPostVad);
        }

        // Feature extraction ......................................................................
        // INPUT: aInfoPostVad, (optional: myVad)
        if (!err) {
            MyFeatureProducer myMFCC = new MyFeatureProducer(FEA_TYPE);
            float [] fea = myMFCC.process(aInfoPostVad);
        }


            // END
        if (err) {
            Toast.makeText(ctx, errMsg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ctx, "DEMO END", Toast.LENGTH_SHORT).show();
        }
    }
*/

    // =================================FOR DEBUG PURPOSES ONLY ==================================





    //----------------------------------------------------------------------------------------------
    private String getFullPath(String myFolder, String myFile) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, myFolder);
        boolean mkOk = true;
        if (!file.exists()) {
            mkOk = file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + myFile);
    }
}
