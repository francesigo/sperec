package myDSP.test;

import com.mathworks.matlab.types.Struct;

import myDSP.MyEnergy;
import myDSP.MyFramingSettings;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestMyEnergy {

	public static void main(String [] args) throws Exception {
		
		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		MyMath.subtractMeanSelf(u.samples);
		
		MyFramingSettings frameSettings = new MyFramingSettings();

		MyEnergy myNRG = new MyEnergy(u, frameSettings);
		
		MyMatlabConnection matlab = new MyMatlabConnection();
		Struct um = new Struct("SampleRate", (double)u.sampleRate, "samples", u.samples);
		Struct fs = new Struct("window_sec", frameSettings.window_sec, "overlap_sec", frameSettings.overlap_sec);
		Struct RES = matlab.eng.feval("myEnergy", um, fs);
		double [] mnrg = (double[]) RES.get("energy");
		
		MyMath.compare_vectors("TestMyEnergy: ", myNRG.energy, mnrg);
		
		
		
	}
}
