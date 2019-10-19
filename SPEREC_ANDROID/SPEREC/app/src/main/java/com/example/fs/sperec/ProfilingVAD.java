package com.example.fs.sperec;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import sperec_common.MiscUtils;
import sperec_common.MyAudioInfo;
import sperec_common.SPEREC_AudioProcessor;
import sperec_common.VadSpecs;
import sperec_common.tryTarsos_common.WriterFloatArrayCopyMemory;
import tryTarsos.MyAudioDispatcherFactory;

public class ProfilingVAD extends Profiling {

    VadSpecs vadSpecs = null;

    public void do_work() throws Exception {

        // Get the list of input files and speaker names
        ArrayList<String> inputClassNames = new ArrayList<String>();
        String[] inputFileArray = new String[0];
        try {
            inputFileArray = MiscUtils.getListOfAudioInputFiles(inputDirOrFileList, inputClassNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double [] testDurArray = new double[] {2.0, 4.0, 8.0, 14.0};

        long tStart = System.currentTimeMillis();

        // Open the log file
        File logFile = new File(logFileName);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.write("#Filename" + "\t" +
                                "chunk" + "\t" +
                                "vadType" + "\t" +
                                "testDur" + "\t" +
                                "samplesIn" + "\t" +
                                "samplesOut" + "\t" +
                                "exeTime" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < inputFileArray.length; i++)
        {
            updateProgress((int)((double)i/inputFileArray.length*100.0));

            String inputAudioFullPath = inputFileArray[i];
            String filename = new File(inputAudioFullPath).getName();
            Log.i("MY PROFILING", filename);
            MyAudioInfo aInfo = MyAudioInfo.getFrom(inputAudioFullPath);

            byte [] buf = aInfo.getDataBuffer();
            int bufLenBytes = buf.length;

            // Try different testDur
            for (int j=0; j<testDurArray.length; j++)
            {

                double currentDur = testDurArray[j];
                int k=0;
                int chunkLenSamples = (int)Math.floor(currentDur * aInfo.getSampleRate());
                int chunkLenBytes = chunkLenSamples*aInfo.getBytesPerSample();
                int offset = 0;
                int chunk = 0;
                while(offset+chunkLenBytes<=bufLenBytes)
                {
                    ByteArrayInputStream BAIS = new ByteArrayInputStream(buf, offset, chunkLenBytes);
                    AudioDispatcher mainAudioDispatcher = MyAudioDispatcherFactory.fromAndroidInputStream(BAIS, aInfo.getSampleRate(),2048, 0);
                    SPEREC_AudioProcessor oSAP = new SPEREC_AudioProcessor(mainAudioDispatcher);
                    oSAP.setProfilingState(true);
                    oSAP.setVad(vadSpecs);
                    oSAP.setFea(null);
                    WriterFloatArrayCopyMemory wp = new WriterFloatArrayCopyMemory();
                    oSAP.setOutput(wp); // Not interested in collecting output

                    try {
                        oSAP.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    double exTime = oSAP.getLastRunExeTime_sec();

                    try {
                        writer.write(filename + "\t" +
                                        chunk + "\t" +
                                        vadSpecs.getMethod() + "\t" +
                                        currentDur + "\t" +
                                        oSAP.voiceDetector.getInSampleCounter() + "\t" +
                                        oSAP.voiceDetector.getOutSampleCounter() + "\t" +
                                        exTime + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    chunk++;

                    oSAP.close();
                    // Next chunk
                    offset+=chunkLenBytes;
                }
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MiscUtils.showElapsedTime(tStart);

        updateProgress(-1);
    }



}
