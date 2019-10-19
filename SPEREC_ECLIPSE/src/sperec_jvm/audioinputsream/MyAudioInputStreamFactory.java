package sperec_jvm.audioinputsream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.UnsupportedAudioFileException;

public class MyAudioInputStreamFactory {
	
	/*
	 * ...From file...
	 */
	public static IMyAudioInputStream fromFile(File audioFile) throws UnsupportedAudioFileException, IOException {
		final IMyAudioInputStream stream = new MyAudioInputStreamFromFile(audioFile);
		return stream;

	}

	//----------------- TEST FUNCTIONS
	public static void testFromFile() throws UnsupportedAudioFileException, IOException {
		
		String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
    	String sAudioFilename = "AUDIO-01_cut-11025.wav";
    	String sOutAudioFolder = "C:/Users/FS/Desktop";
    	String sOutAudioFilename = sAudioFilename + "_testFromFile.wav";
    	
    	String sAudioFileFullPath = sAudioFolder + "/" + sAudioFilename;
		final IMyAudioInputStream stream = new MyAudioInputStreamFromFile(sAudioFileFullPath);
		byte [] baBuffer = new byte [1024];
		
		String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
		
		MyAudioSpecs myAudioSpecs = stream.getAudioSpecs();
		MyWavWriter w = new MyWavWriter(myAudioSpecs, sOutAudioFullPath);
		
		while (stream.read(baBuffer, 0, baBuffer.length)>0) {
			w.write(baBuffer);
		}
		w.close();
	}
}
