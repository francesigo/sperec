package myDSP.test;



import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

import com.mathworks.matlab.types.Struct;

import myDSP.MyFormants;
import myDSP.MyFraming;
import myDSP.MyFramingSettings;

public class TestFormants {

	public static void main(String [] args) throws Exception {

		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		
		MyFramingSettings FS = new MyFramingSettings();
		MyFraming myFraming = new MyFraming(FS);
		myFraming.process(u);
		
		MyFormants myFormants = new MyFormants(myFraming.getFrames(), 10);
		
		// The formants (ff are in normalized Hz, must be multiplied by the sample rate/2)
		
		// Matlab:
		Struct utterance = new Struct("SampleRate", (double)u.sampleRate, "samples", u.samples);
		MyMatlabConnection matlab = new MyMatlabConnection();
		Struct iFraming = new Struct("frame_increment_samples", (double)myFraming.frame_increment_samples, "dur_samples", (double)myFraming.dur_samples);
		
		Struct MAT = matlab.eng.feval("myFormantsTrack", utterance, iFraming, (double)10);
		double [][] mat_ff = (double[][])MAT.get("ff");
		double [][] mat_a = (double[][])MAT.get("a");
		double [][] mat_b = (double[][])MAT.get("b");
		double [] mat_n = (double [])MAT.get("n");
		
		MyMath.compare("TestFormants: aa: ", myFormants.getFormantAmplitudes(), mat_a);
		MyMath.compare("TestFormants: bb: ", myFormants.getFormantBandwidths(), mat_b);
		
		
		
		System.out.println("DONE");
	}
}
