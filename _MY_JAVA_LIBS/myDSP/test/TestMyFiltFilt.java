package myDSP.test;

import java.util.Arrays;

import myDSP.MyComplexArrayDouble;
import myDSP.MyComplexArrayFloat;
import myDSP.MyFiltFilt;
import myDSP.MyFilter;
import myDSP.MyHilbert;
import myDSP.MyResampler;
import myDSP.MySigNormalizer;
import myDSP.MyVadSettings;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestMyFiltFilt {

	public static void main (String [] args) throws Exception {
		
		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		MyMath.subtractMeanSelf(u.samples);
		
		// Energy normalization
		MyVadSettings FS = new MyVadSettings();
		MySigNormalizer.processSelf("mean-voiced", u, FS);	

		// Compute the hilbert transform
		MyComplexArrayDouble hi = MyHilbert.get(u.samples);
		// Get the magnitude
		double [] abshi = hi.getAbs();
		// Resampling
		double [] x = MyResampler.exe(abshi, 1024, u.sampleRate);

		double [] b = new double[] {
				0.006514350434926, 0.008625611735089, 0.014537725009923, 0.023948539817104, 0.036112418048306, 
				0.049913983252129, 0.063991437800973, 0.076894537216144, 0.087257364191478, 0.093963737223264,
				0.096283773507432, 0.093963737223264, 0.087257364191478, 0.076894537216144, 0.063991437800973,
				0.049913983252129, 0.036112418048306, 0.023948539817104, 0.014537725009923, 0.008625611735089,
				0.006514350434926};
		double [] a = new double[b.length];
		Arrays.fill(a, 0.0);
		a[0] = 1.0;

		MyFilter f = new MyFilter(b, a);

		MyFiltFilt FF = MyFiltFilt.fromFilter(f);
		
		double [] jy = FF.filter(x);
		
		MyMatlabConnection matlab = new MyMatlabConnection();
		double [] my = matlab.eng.feval("filtfilt", b, a, x);
		
		MyMath.compare_vectors("TestFiltFilt: ", jy, my);
		
		
	}

}
