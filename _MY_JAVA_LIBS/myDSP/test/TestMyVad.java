package myDSP.test;


import com.mathworks.matlab.types.Struct;

import myDSP.MyFraming;
import myDSP.MyVAD;
import myDSP.MyVadSettings;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestMyVad {

	public static void main(String [] args) throws Exception {
		
		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		MyMath.subtractMeanSelf(u.samples);
		
		MyVadSettings FS = new MyVadSettings();

		MyMatlabConnection matlab = new MyMatlabConnection();

		do_utterance(u, FS, matlab);
		
		do_framing(u, FS, matlab);

		
		System.out.println("DONE");

	}
	
	/**
	 * 
	 * @param u
	 * @param FS
	 * @throws Exception
	 */
	private static void do_utterance(Utterance u, MyVadSettings FS, MyMatlabConnection matlab) throws Exception {
		// Do the VAD on the utterance
		MyVAD myVad = new MyVAD();
		myVad.process(u, FS);
			
		compare("TestMyVad (on utterance):", u, myVad, FS, matlab);
	}
	
	/**
	 * 
	 * @param u
	 * @param FS
	 * @throws Exception
	 */
	private static void do_framing(Utterance u, MyVadSettings FS, MyMatlabConnection matlab) throws Exception {
		// Do the VAD on the frames
		MyVAD myVad = new MyVAD();
		
		MyFraming myFraming = new MyFraming(FS);
		myFraming.process(u);
		float [][] frames = myFraming.getFrames();
		myVad.process(frames, FS);
		
		compare("TestMyVad (on frames):", u, myVad, FS, matlab);
	}
	
	/**
	 * 
	 * @param title
	 * @param u
	 * @param myVad
	 * @param FS
	 * @throws Exception
	 */
	private static void compare(String title, Utterance u, MyVAD myVad, MyVadSettings FS, MyMatlabConnection matlab) throws Exception {
		
		Struct utterance = new Struct("SampleRate", (double)u.sampleRate, "samples", u.samples);
		Struct opts = new Struct("window_sec", FS.window_sec, "overlap_sec", FS.overlap_sec, "vadPercentile", FS.vadPercentile);
		Struct RES = matlab.eng.feval("myVad", utterance, opts);
		Struct results = (Struct)RES.get("results");
		
		MyMath.compare_vectors(title + " voiced_energy: ", myVad.voiced_energy, (double [])results.get("voiced_energy"));
	
	}
	
}
