package myDSP.test;

import com.mathworks.matlab.types.Struct;

import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMath.MyMatrix;
import myMatlabConnection.MyMatlabConnection;
import myDSP.MyFraming;
import myDSP.MyFramingSettings;

public class TestFraming {

	
	public static void main(String args[]) throws Exception {
		
		String utteranceFilename = "dis-f1-b1.wav";
		
		Utterance u = SampleFileLoader.load(utteranceFilename);
		
		MyFramingSettings frameSettings = new MyFramingSettings();
		
		MyFraming myFraming = new MyFraming(frameSettings);
		myFraming.process(u);
		
		myFraming.showInfo("Framing summary: ");
		
		// Test with matlab
		MyMatlabConnection matlab = new MyMatlabConnection();
		Struct margs = new Struct("s", u.samples, "t", new double [] {myFraming.frame_increment_samples, myFraming.dur_samples, 0});
		Struct mres_lpcauto_framing = matlab.call("lpcauto_framing", margs);
		
		compare_lpcauto_framing("TestFraming (lpcauto_framing): ", myFraming, mres_lpcauto_framing);
		
		Struct um = new Struct("SampleRate", (double)u.sampleRate, "samples", u.samples);
		Struct fs = new Struct("window_sec", frameSettings.window_sec, "overlap_sec", frameSettings.overlap_sec);
		Struct mres_myframing = matlab.eng.feval("myFraming", um, fs);
		
		compare_myframing("TestFraming (myFraming): ", myFraming, mres_myframing);

		
		System.out.println("DONE");
		
	}
	
	/**
	 * 
	 * @param title
	 * @param myFraming
	 * @param mres
	 */
	public static void compare_lpcauto_framing(String title, MyFraming myFraming, Struct mres) {
		
		double [][] m_frames = (double[][]) mres.get("frames");
		
		MyMatrix J = MyMatrix.fromFloatArray(myFraming.getFrames());
		J.compare(title + "frames: ", m_frames);	
	}
	
	/**
	 * 
	 * @param title
	 * @param myFraming
	 * @param mres
	 */
	public static void compare_myframing(String title, MyFraming myFraming, Struct mres) {
		
		double [][] m_frames = (double[][]) mres.get("frames");
		
		MyMatrix J = MyMatrix.fromFloatArray(myFraming.getFrames());
		J.compare(title + "frames: ", m_frames);
		
		Struct oFraming = (Struct)mres.get("oFraming");
		
		MyMath.compare_vectors(title + "midTimes: ", myFraming.midTimes,  (double [])oFraming.get("mid_time"));	
	}
}
