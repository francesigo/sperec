package test;

import java.io.File;
import java.io.IOException;

import app.Fant;
import sperec_common.MyWavFile;
import sperec_common.WavFileException;

public class TestFant {
	
	static long sampleRate=-1;
	static int iValidBits;

	public static void main(String[] args) throws Exception {
		
		String testSpeechFilePath = "G:\\Il mio Drive\\FS\\_DOTTORATO\\_DATA\\audioDataOrig\\Monologhi_001_150\\Monologhi_001_150_16000Hz\\abook-001-Farn-E-16000.wav";
		String noiseFilePath = "G:\\Il mio Drive\\FS\\_DOTTORATO\\_DATA\\noise\\mercato_3_30_16KHz.wav";
		String outpuFilePath = "C:\\Users\\FS\\Desktop\\SPEREC_tmp_files\\testNoisy.wav";
			
		
		// Leggi lo speech e il noise
		float[] noise = leggi(noiseFilePath);
		float[] speech = leggi(testSpeechFilePath);
		
	
		// Le opzioni rilevanti sono: -u -m snr_8khz -d -s 6 -l -20
		
		// Ciò che a me interessa cambiare di vlta in volta, oltre ai segnali di speech
		// sono: -s (il SNR), E BASTA
		String [] a = buildArgs(6.0);
		
		
		float [] noisy = Fant.filter_add_noise(speech, noise, a); // noisy speech is returned in "speech"
		
		MyWavFile oWriteWavFile = MyWavFile.newWavFile(new File(outpuFilePath), 1, noisy.length, iValidBits, sampleRate);
		oWriteWavFile.writeFrames(noisy);
		oWriteWavFile.close();
		System.out.println("Fatto: ascolta il file: " + outpuFilePath);

	}
	
	static String [] buildArgs(double snr) {
		String[] a = {"-u", "-m", "snr_8khz", "-d", "-s", Double.toString(snr), "-l", "-20.0"};
		return a;
	}
	
	/**
	 * 
	 * @param filepath
	 * @return
	 * @throws Exception 
	 */
	static float[] leggi(String filepath) throws Exception {
		float[] speech = null;
		MyWavFile oWavfile = MyWavFile.openWavFile(new File(filepath));
		int iNumSamples  = (int)oWavfile.getNumFrames();
		long curr_sampleRate = oWavfile.getSampleRate();
		iValidBits = oWavfile.getValidBits();
		if ( (sampleRate <0) || (sampleRate==curr_sampleRate)) {
			sampleRate = curr_sampleRate;
			speech = new float[iNumSamples];
			oWavfile.readFrames(speech, iNumSamples);
			oWavfile.close();
		}
		else
		{
			throw new Exception("Sample rate non coincidenti");
		}
		oWavfile.close();
		return speech;
		
	}
}
