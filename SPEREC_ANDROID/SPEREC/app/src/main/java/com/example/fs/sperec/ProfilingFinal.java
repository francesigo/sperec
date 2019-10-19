package com.example.fs.sperec;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import myIO.MyWavFile;
import sperec_common.AuthenticationResult;
import sperec_common.LabeledArrayList;
import sperec_common.MiscUtils;
import sperec_common.MyAudioInfo;
import sperec_common.SPEREC;
import sperec_common.SessionsTable_Audio;
import sperec_common.SpeakerModel;
import sperec_common.StRecord;



import tryTarsos.MyAudioDispatcherFactory;



public class ProfilingFinal extends Profiling {

    SPEREC oSperec = null;
    String engine_str = "";

    String ref_inputFileList = "";
    String test_inputFileList ="";
    double enrollSessionDurationSec = -1.0;
    double[] testDurationSec_v = null; //-1.0;
    String logFileName = "";

    // Limits to avoid too long run
    int MAX_NUM_SPKS = Integer.MAX_VALUE;
    int MAX_NUM_SESSIONS = Integer.MAX_VALUE;

    public void do_work() throws Exception {
        System.out.println("START");
        long tStart = System.currentTimeMillis();
        // Open the log file
        File logFile = new File(logFileName);
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(logFile));
        writer.write("#Engine" + "\t" +
                "testDur" + "\t" +
                "exeTime" + "\n");
        for (int i=0; i<testDurationSec_v.length; i++)
        {
            do_testdur(testDurationSec_v[i], writer);
        }
        writer.close();
        String msg = MiscUtils.showElapsedTime(tStart);
        updateProgress(-1, msg);
        System.out.println("END");
    }

    public void do_testdur(double testDurationSec, BufferedWriter writer) throws Exception {

        SessionsTable_Audio ST_ref = new SessionsTable_Audio(String.class);
        ST_ref.fromFileList(ref_inputFileList);
        ArrayList<LabeledArrayList<StRecord>> refSessionsBySpeaker = ST_ref.getArrayListOfRecordSets(); // sotto forma di array

        SessionsTable_Audio ST_test = new SessionsTable_Audio(String.class);
        ST_test.fromFileList(test_inputFileList);
        ArrayList<LabeledArrayList<StRecord>> testSessionsBySpeaker = ST_test.getArrayListOfRecordSets(); // sotto forma di array

        // Shuffle. Assumes that the speakers are the same and in the same order
        int [] shuffledSpeakerIndexes = shuffle(refSessionsBySpeaker.size());
        int numRefSpeakers =  Math.min(refSessionsBySpeaker.size(), MAX_NUM_SPKS);
        int numTestSpeakers = Math.min(testSessionsBySpeaker.size(), MAX_NUM_SPKS);

        for (int ir = 0; ir<numRefSpeakers; ir++) // For each reference speaker
        {
            updateProgress((int)((double)ir/numRefSpeakers*100.0), "testDur = " + testDurationSec);

            int r = shuffledSpeakerIndexes[ir];
            LabeledArrayList<StRecord> currRefSpkSessions = refSessionsBySpeaker.get(r);

            //free memory for the next ref speaker
            refSessionsBySpeaker.set(r, null);

            // Shuffle the sessions
            int [] shuffledRefSessionIndexes = shuffle(currRefSpkSessions.size());
            // Limit the number of sessions
            int numSessionsOfCurrRefSpk = Math.min(currRefSpkSessions.size(), MAX_NUM_SESSIONS);

            for (int isr = 0; isr<numSessionsOfCurrRefSpk; isr++) // For each long session of the reference speaker
            {
                // Take the random session
                int sr = shuffledRefSessionIndexes[isr];
                StRecord currRefRecord = currRefSpkSessions.get(sr);

                String currRefSessionName = currRefRecord.sessionName;

                MyAudioInfo currRefAudioInfo = getAudioInfo(currRefRecord);
                byte [] currRefBuf = currRefAudioInfo.getDataBuffer();

                int refChunkLenSamples = (int)Math.floor(enrollSessionDurationSec * currRefAudioInfo.getSampleRate());
                int refChunkLenBytes = refChunkLenSamples*currRefAudioInfo.getBytesPerSample();
                for (int refOffset=0; refOffset<currRefBuf.length-refChunkLenBytes; refOffset+=refChunkLenBytes) // Do chunk the long session of the reference spekaer
                {
                    // Compute the refernce speaker model. In actual application it may also be pre-computed or fetched from the database of enrolled spekaers
                    ByteArrayInputStream BAIS_ref = new ByteArrayInputStream(currRefBuf, refOffset, refChunkLenBytes);
                    AudioDispatcher audioDispatcherReference = MyAudioDispatcherFactory.fromAndroidInputStream(BAIS_ref, currRefAudioInfo.getSampleRate(),2048, 0);
                    SpeakerModel refSpkModel = oSperec.computeSpeakerModelFromSpeech(audioDispatcherReference);

                    for (int it = 0; it<numTestSpeakers; it++)
                    {
                        int t = shuffledSpeakerIndexes[it];
                        LabeledArrayList<StRecord> currTestSpkSessions = testSessionsBySpeaker.get(t);

                        // Shuffle the sessions
                        int [] shuffledTestSessionIndexes = shuffle(currTestSpkSessions.size());
                        // Limit the sessions
                        int numSessionsOfCurrTestSpk = Math.min(currTestSpkSessions.size(), MAX_NUM_SESSIONS);

                        //for (int st=0; st<numSessionsOfCurrTestSpk; st++) // For each long session of the test speaker
                        int ist = 0;
                        int stCount = 0;
                        // ist must be able to scan the whole dataset, while the counter cannot exceed the limit
                        while ((stCount<numSessionsOfCurrTestSpk) && (ist<currTestSpkSessions.size()))
                        {
                            int st = shuffledTestSessionIndexes[ist++]; //Always increment ist
                            StRecord currTestRecord = currTestSpkSessions.get(st);

                            String currTestSessionName = currTestRecord.sessionName;
                            if (!currRefSessionName.equals(currTestSessionName))
                            {
                                stCount++; // The session can be done, so increment the counter
                                MyAudioInfo currTestAudioInfo = getAudioInfo(currTestRecord);
                                byte [] currTestBuf = currTestAudioInfo.getDataBuffer();

                                int testChunkLenSamples = (int)Math.floor(testDurationSec * currTestAudioInfo.getSampleRate());
                                int testChunkLenBytes = testChunkLenSamples*currTestAudioInfo.getBytesPerSample();

                                for (int testOffset=0; testOffset<currTestBuf.length-testChunkLenBytes; testOffset+=testChunkLenBytes) // Do chunk the long session of the test spekaer
                                {
                                    double runExeTime_sec = helper_comp(currTestAudioInfo, testChunkLenBytes, testOffset,
                                                                        refSpkModel); //currRefAudioInfo, refChunkLenBytes, refOffset);

                                    if (runExeTime_sec>0)
                                        writer.write(engine_str + "\t" +
                                                    testDurationSec + "\t" +
                                                    runExeTime_sec + "\n");
                                }
                            }
                        }
                    }
                }
                int a=2;

            }
        }
    }


    MyAudioInfo getAudioInfo(StRecord stRecord) throws Exception {
        String inputFileAudio = (String)stRecord.data;
        MyWavFile currWav = MyWavFile.openWavFile(new File(inputFileAudio));
        //myIO.core.MyWavFile currRefWav = (myIO.core.MyWavFile)currRefRecord.data;
        MyAudioInfo aInfo = MyAudioInfo.getWavFrom(currWav);
        return aInfo;
    }

    /*

     */
    double helper_comp(MyAudioInfo currTestAudioInfo, int testChunkLenBytes, int testOffset,
                       SpeakerModel refSpkModel) //MyAudioInfo currRefAudioInfo, int refChunkLenBytes, int refOffset)
            throws Exception {

        byte [] currTestBuf = currTestAudioInfo.getDataBuffer();
        //byte []currRefBuf = currRefAudioInfo.getDataBuffer();

        ByteArrayInputStream BAIS_test = new ByteArrayInputStream(currTestBuf, testOffset, testChunkLenBytes);
        AudioDispatcher audioDispatcherTest = MyAudioDispatcherFactory.fromAndroidInputStream(BAIS_test, currTestAudioInfo.getSampleRate(),2048, 0);

        //ByteArrayInputStream BAIS_ref = new ByteArrayInputStream(currRefBuf, refOffset, refChunkLenBytes);
        //AudioDispatcher audioDispatcherReference = MyAudioDispatcherFactory.fromAndroidInputStream(BAIS_ref, currRefAudioInfo.getSampleRate(),2048, 0);

        long tStart = System.currentTimeMillis();

        SpeakerModel testSpkModel = oSperec.computeSpeakerModelFromSpeech(audioDispatcherTest);
        if (testSpkModel==null) {
            return -1.0;
        }
        AuthenticationResult aur = oSperec.compareModels(refSpkModel, testSpkModel);
        //AuthenticationResult aur = oSperec.compareSpeakers(audioDispatcherReference, audioDispatcherTest);
        long tEnd = System.currentTimeMillis();
        long runExeTime_ms = tEnd-tStart;
        double runExeTime_sec = (double)runExeTime_ms  / 1000.0;

        return runExeTime_sec;
    }

    /*void helper_jvm() {
        byte [] currTestChunk = new byte[testChunkLenBytes];

        for (int testOffset=0; testOffset<currTestBuf.length-testChunkLenBytes; testOffset+=testChunkLenBytes) // Do chunk the long session of the test spekaer
        {
            // The chunk
            System.arraycopy(currRefBuf, refOffset, currTestChunk, 0, refChunkLenBytes);
            AudioFormat testAudioFormat = new AudioFormat(currTestAudioInfo.getSampleRate(), 16, 1, true, false);

            // The comparison
            // Each time redo the refAudioDispatcher
            AudioDispatcher audioDispatcherReference = AudioDispatcherFactory.fromByteArray(currRefChunk, refAudioFormat, 2048, 0);
            AudioDispatcher audioDispatcherTest = AudioDispatcherFactory.fromByteArray(currTestChunk, testAudioFormat, 2048, 0);

            long tStart = System.currentTimeMillis();
            AuthenticationResult aur = oSperec.compareSpeakers(audioDispatcherReference, audioDispatcherTest);
            long tEnd = System.currentTimeMillis();
            long runExeTime_ms = tEnd-tStart;
            double runExeTime_sec = (double)runExeTime_ms  / 1000.0;

            try {
                writer.write(engine_str + "\t" +
                        testDurationSec + "\t" +
                        runExeTime_sec + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }*/

}
