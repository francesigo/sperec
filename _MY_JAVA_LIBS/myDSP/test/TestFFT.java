package myDSP.test;

import com.mathworks.matlab.types.Complex;


import myDSP.MyComplexArrayFloat;
import myDSP.MyFFT;
import myDSP.MyFraming;
import myDSP.MyFramingSettings;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestFFT {

	public static void main(String [] args) throws Exception {
		
		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);

		MyFramingSettings frameSettings = new MyFramingSettings();

		MyFraming myFraming = new MyFraming(frameSettings);
		myFraming.process(u);
		
		float[][] frames = myFraming.getFrames();
		
		float[] buffer = frames[0].clone();
		
		int n = buffer.length;
		
		// Prima il matlab
		MyMatlabConnection matlab = new MyMatlabConnection();
		Complex [] mc = matlab.eng.feval("fft", buffer, n);
		//testForwardTransform(buffer, mc);
		
		buffer = frames[0].clone();
		MyComplexArrayFloat jfft = testRealForwardFull(buffer, mc);
		
		
		// Test the inverse.
		// JAVA
		MyComplexArrayFloat ifft = MyFFT.complexInverse(jfft);

		// MATLAB: I cannot pass Complex[] to matlab and use ifft.I needed a trick
		//Complex [] ifft_m = matlab.eng.feval("ifft ", c);
		double mre[] = new double[n];
		double mim[] = new double[n];
		for (int i=0; i<n; i++) {
			mre[i] = mc[i].real;
			mim[i] = mc[i].imag;
		}
		try {
			Complex [] ifft_m = matlab.eng.feval("myifft", mre, mim);
		}
		catch (Exception e){
			double [] ifft_m = matlab.eng.feval("myifft", mre, mim);
			float [] ifft_j = ifft.getRealFloat();
			MyMath.compare_vectors("testInverse: ", ifft_j, ifft_m);
		}
	
	}
	
	/**
	 * (BETTER)
	 * @param buffer_
	 * @param c
	 */
	private static MyComplexArrayFloat testRealForwardFull(float [] buffer_, Complex [] c) {
		
		MyComplexArrayFloat fft = MyFFT.realForwardFull(buffer_);
		
		TestMyComplexArray.compareToComplexMatlab(fft, c, "testRealForwardFull:");
		
		
		return fft;
	}
	
	
	
	
	/**
	 * 
	 * @param buffer
	 * @param c
	 */
	/*private static void testForwardTransform(float [] buffer, Complex [] c) {

		int n = buffer.length;
		
		FFT fft = new FFT(n);

		fft.forwardTransform(buffer); // Modifica il contenuto di buffer

		int n2 = n/2;
		double jre[] = new double[n2];
		double jim[] = new double[n2];
		for (int i=0; i<n2; i++) {
			jre[i] = (double)buffer[2*i];
			jim[i] = (double)buffer[2*i+1];
		}
		double mre[] = new double[n2];
		double mim[] = new double[n2];
		for (int i=0; i<n2; i++) {
			mre[i] = c[i].real;
			mim[i] = c[i].imag;
		}

		MyMath.compare_vectors("testForwardTransform: Re[FFT] comparison", jre, mre);
		MyMath.compare_vectors("testForwardTransform: Im[FFT] comparison", jim, mim);
	}*/
	

	
	
}
