package myDSP.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import com.mathworks.engine.EngineException;
import com.mathworks.matlab.types.Complex;


import myDSP.MyComplexArrayDouble;
import myDSP.MyFFTDouble;
import myDSP.MyFraming;
import myDSP.MyFramingSettings;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestFFTDouble {

	public static void main(String [] args) throws Exception {
		
		// The file name to load
		String utteranceFilename = "tri-m3-n5.wav"; //"dis-f1-b1.wav";

		// Load the audio and build an Utterance instance
		Utterance u = SampleFileLoader.load(utteranceFilename);

		// Framing of the signal, the convert in double []
		MyFramingSettings frameSettings = new MyFramingSettings();
		MyFraming myFraming = new MyFraming(frameSettings);
		myFraming.process(u);
		float[][] frames_float = myFraming.getFrames();
		double [][] frames = MyMath.todouble(frames_float);
		
		
		// Just take the first frame
		double[] buffer = frames[0].clone();
		
		int n = buffer.length;
		
		// Prima il matlab
		MyMatlabConnection matlab = new MyMatlabConnection();
		Complex [] mc = matlab.eng.feval("fft", buffer, n);
		//testForwardTransform(buffer, mc);
		
		buffer = frames[0].clone();
		MyComplexArrayDouble jfft = testRealForwardFull(buffer, mc, 1e-20);
		
		
		// Test the inverse.
		// JAVA
		MyComplexArrayDouble ifft = MyFFTDouble.complexInverse(jfft);

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
			double [] ifft_j = ifft.getReal();
			MyMath.compare_vectors("testInverse: ", ifft_j, ifft_m, 1e-20);
		}
	
	}
	
	/**
	 * (BETTER)
	 * @param buffer_
	 * @param c
	 */
	private static MyComplexArrayDouble testRealForwardFull(double [] buffer_, Complex [] c, double th) {
		
		MyComplexArrayDouble fft = MyFFTDouble.realForwardFull(buffer_);
		
		TestMyComplexArrayDouble.compareToComplexMatlab(fft, c, th, "testRealForwardFull:");
		
		
		return fft;
	}
	
	/**
	 * Given a double[] array and the number of bin, compute the FFT both with my Java code and Matlab code and compare the results
	 * @param buffer_
	 * @param n
	 * @param matlab
	 * @return
	 * @throws RejectedExecutionException
	 * @throws EngineException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static MyComplexArrayDouble testRealForwardFull(double [] buffer_, int n, MyMatlabConnection matlab, double th) throws RejectedExecutionException, EngineException, InterruptedException, ExecutionException {
		
		Complex [] mc = matlab.eng.feval("fft", buffer_, n);
		
		MyComplexArrayDouble fft = MyFFTDouble.realForwardFull(buffer_, n);
		
		TestMyComplexArrayDouble.compareToComplexMatlab(fft, mc, th, "testRealForwardFull:");
		
		
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
