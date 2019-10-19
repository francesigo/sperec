package app;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import myIO.MyWavFile;
import sperec_common.AuthenticationResult;
import sperec_common.LabeledArrayList;
import sperec_common.MyAudioInfo;
import sperec_common.SPEREC;
import sperec_common.SessionsTable_Audio;
import sperec_common.StRecord;

public class ProfilingFinal {

	static public void main(String [] args) throws Exception {
		
		System.out.println("START");

		String ref_inputDir = "G:\\Il mio Drive\\FS\\_DOTTORATO\\_DATA\\toSPLIT\\Monologhi_001_150_16000Hz";
		String ref_inputFileList = ref_inputDir + File.separator + "file.lst";
		
		String test_inputDir = "G:\\Il mio Drive\\FS\\_DOTTORATO\\_DATA\\toSPLIT\\Monologhi_001_150_16000Hz";
		String test_inputFileList = test_inputDir + File.separator + "file.lst";
		
		String logFileName = "C:\\Users\\FS\\Desktop\\prove\\profiling_final.log";

		
		SessionsTable_Audio ST_ref = new SessionsTable_Audio();
		ST_ref.fromFileList(ref_inputDir, ref_inputFileList);
		ArrayList<LabeledArrayList<StRecord>> refSessionsBySpeaker = ST_ref.getArrayListOfRecordSets(); // sotto forma di array
		
		SessionsTable_Audio ST_test = new SessionsTable_Audio();
		ST_test.fromFileList(test_inputDir, test_inputFileList);
		ArrayList<LabeledArrayList<StRecord>> testSessionsBySpeaker = ST_test.getArrayListOfRecordSets(); // sotto forma di array

		
		// Open the log file
        File logFile = new File(logFileName);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.write("#Engine" + "\t" +
                                "testDur" + "\t" +
                                "exeTime" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
		
		int numRefSpeakers = refSessionsBySpeaker.size();
		int numTestSpeakers = testSessionsBySpeaker.size();

		// Scegliere il più affidabile Engine, forse lo devo ancora costruire
		SPEREC oSperec = null;
		// Indicare un nome simbolico per questo engine. Su un foglio di carte devo far corrispondere questo nome alle caratteristiche dell'engine
		String engine_str = "Engine0";
		
		double enrollSessionDurationSec = 25.0; // di solito
		double testDurationSec = 14.0; // variabile
		
		for (int r = 0; r<numRefSpeakers; r++) // For each reference speaker
		{
			LabeledArrayList<StRecord> currRefSpkSessions = refSessionsBySpeaker.get(r);
			int numSessionsOfCurrRefSpk = currRefSpkSessions.size();
			for (int sr=0; sr<numSessionsOfCurrRefSpk; sr++) // For each long session of the refrence speaker
			{
				StRecord currRefRecord = currRefSpkSessions.get(sr);
				String currRefSessionName = currRefRecord.sessionName;

				MyWavFile currRefWav = (MyWavFile)currRefRecord.data;
				MyAudioInfo currRefAudioInfo = MyAudioInfo.getWavFrom(currRefWav);
				byte []currRefBuf = currRefAudioInfo.getDataBuffer();
				int refChunkLenSamples = (int)Math.floor(enrollSessionDurationSec * currRefAudioInfo.getSampleRate());
				int refChunkLenBytes = refChunkLenSamples*currRefAudioInfo.getBytesPerSample();
				byte [] currRefChunk = new byte[refChunkLenBytes];
				for (int refOffset=0; refOffset<currRefBuf.length-refChunkLenBytes; refOffset+=refChunkLenBytes) // Do chunk the long session of the reference spekaer
				{
					// The chunk
					System.arraycopy(currRefBuf, refOffset, currRefChunk, 0, refChunkLenBytes);
					AudioFormat refAudioFormat = new AudioFormat(currRefAudioInfo.getSampleRate(), 16, 1, true, false);

					for (int t = 0; t<numTestSpeakers; t++)
					{
						
						LabeledArrayList<StRecord> currTestSpkSessions = testSessionsBySpeaker.get(t);
						int numSessionsOfCurrTestSpk = currTestSpkSessions.size();
						for (int st=0; st<numSessionsOfCurrTestSpk; st++) // For each long session of the test speaker
						{
							StRecord currTestRecord = currTestSpkSessions.get(st);
							String currTestSessionName = currTestRecord.sessionName;
							if (!currRefSessionName.equals(currTestSessionName))
							{
								MyWavFile currTestWav = (MyWavFile)currTestRecord.data;
								MyAudioInfo currTestAudioInfo = MyAudioInfo.getWavFrom(currTestWav);
								int testChunkLenSamples = (int)Math.floor(testDurationSec * currTestAudioInfo.getSampleRate());
								byte []currTestBuf = currTestAudioInfo.getDataBuffer();
								int testChunkLenBytes = testChunkLenSamples*currTestAudioInfo.getBytesPerSample();
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
							}
						}
						
						
					}
				}
			}
		}
		
		try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	
		System.out.println("END");

	}
}
