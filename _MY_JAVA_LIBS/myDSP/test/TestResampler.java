package myDSP.test;

import myChart.MyChart;
import myDSP.MyResampler;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestResampler {

	public static void main(String [] args) throws Exception {
		
		String utteranceFilename = "tri-m3-n5.wav"; //"dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		MyMath.subtractMeanSelf(u.samples);
		
		double [] j = MyResampler.exe(MyMath.todouble(u.samples), 1024, u.sampleRate);
		
		MyMatlabConnection matlab = new MyMatlabConnection();
		double [] m = matlab.eng.feval("resample",  MyMath.todouble(u.samples), (double)1024, (double)u.sampleRate);
		
		MyMath.compare_vectors("TestResampler: " + u.sampleRate + " --> 1024 Hz", j, m);
		
		MyChart c1 = new MyChart("TestResampler: JAVA result");
		c1.plot(j);
		MyChart c2 = new MyChart("TestResampler: MATLAB result");
		c2.plot(m);
	}
}
