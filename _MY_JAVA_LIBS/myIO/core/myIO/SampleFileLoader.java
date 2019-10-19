package myIO;

import java.io.File;
import java.io.IOException;

import myDSP.Utterance;

public class SampleFileLoader {

	public static Utterance load(String utteranceFilename) throws IOException, Exception {
		Environment en = new myIO.Environment();

		String filePath = en.getSampleDataDir() + File.separator + utteranceFilename;
		return loadFullPath(filePath);
		
	}
	
	
	public static Utterance loadFullPath(String fullpath) throws IOException, Exception {
		
		// Load the file
		System.out.println("Loading file: " + fullpath + " ...");
		MyWavFile wav = MyWavFile.openWavFile(new File(fullpath));
		wav.readAllFloat();
		wav.close();
		
		float [] audioSamples = wav.getFloatSamples();
		int sampleRate = (int)wav.getSampleRate();
		
		Utterance u = new Utterance(audioSamples, sampleRate);
		System.out.println("File " + fullpath + " loaded.");
		u.showInfo("");
		return u;
	}
}
