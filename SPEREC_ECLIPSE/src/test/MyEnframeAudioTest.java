package test;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import sperec_jvm.MyEnframeAudio;
import sperec_jvm.audioinputsream.IMyAudioInputStream;
import sperec_jvm.audioinputsream.MyAudioInputStreamFromFile;
import sperec_jvm.audioinputsream.MyAudioSpecs;
import sperec_jvm.audioinputsream.MyWavWriter;

public class MyEnframeAudioTest {
	
	/*
	 * Read overlapped frames from file
	 */
	public static void test1() throws UnsupportedAudioFileException, IOException {
		String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
    	String sAudioFilename = "AUDIO-01_cut-11025.wav";
    	String sOutAudioFolder = "C:/Users/FS/Desktop";
    	String sOutAudioFilename = sAudioFilename + "_test1.wav";
    			    	    	
    	//Specs
    	double dReqFrameIncrementSec = 0.010; // The same of vadsohn...
    	double dReqOverlapFactor     = 2.0;
    	String sAudioFileFullPath = sAudioFolder + "/" + sAudioFilename;
    	//File f = new File(sAudioFileFullPath);
    	IMyAudioInputStream ais = new MyAudioInputStreamFromFile(sAudioFileFullPath);
    	
    	MyEnframeAudio m = new MyEnframeAudio(ais, dReqFrameIncrementSec, dReqOverlapFactor);
    	
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
    	MyWavWriter w = new MyWavWriter(ais.getAudioSpecs(), sOutAudioFullPath);
    	
    	int bytesRead = 0;
    	while (   (bytesRead = m.readNextBlock()) >0) {
    		System.out.println(bytesRead);
    		w.write(m.getByteBuffer());
    	}
    	w.close();
    	
	}

}
