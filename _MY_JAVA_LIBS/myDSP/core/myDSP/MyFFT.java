package myDSP;

import java.util.Arrays;

import be.tarsos.dsp.util.fft.FloatFFT;
import myMath.MyMath;

public class MyFFT {

	/**
	 * Compute the FFT of a real sequence
	 * @param x: the real sequence
	 * @return a ComplexArray 
	 */
	public static MyComplexArrayFloat realForwardFull (float [] x) {
		return realForwardFull(x, x.length);
	}
	public static MyComplexArrayFloat realForwardFull (double [] x) {
		return realForwardFull(MyMath.tofloat(x), x.length);
	}
	
	/**
	 * Compute the FFT of a real sequence with n bins
	 * @param xr the input sequence of reals
	 * @param n the number of bin
	 * @return the resulting complex sequence
	 */
	public static MyComplexArrayFloat realForwardFull (float [] xr, int n) {
		
		float [] x = cloneAndPadding(xr, n);
				
		//Because the result is stored in <code>buffer</code>,
	    //the size of the input array must greater or equal 2*n, with only the
	    //first n elements filled with real data.
		float [] buffer = new float[2*n];
		System.arraycopy(x, 0, buffer, 0, n);
		Arrays.fill(buffer, n, 2*n, 0.0f);
		
		FloatFFT fft = new FloatFFT(n);		

		fft.realForwardFull(buffer);

		return new MyComplexArrayFloat(buffer);
		
	}
	
	public static MyComplexArrayFloat realForwardFull (double [] xr, int n) {
		return realForwardFull(MyMath.tofloat(xr), n);
	}
	
	public static MyComplexArrayFloat realForward(float [] xr, int n) {
		
		float [] x = cloneAndPadding(xr, n);
		
		FloatFFT fft = new FloatFFT(n);
		fft.realForward(x);
		return null;
		
	}
	
	/**
	 * Compute the IFFT (Inverse Fast Fourier Transform) of a complex sequence
	 * @param x the input complex sequence
	 * @return a complex sequence as result of IFFT transform
	 */
	public static MyComplexArrayFloat complexInverse(MyComplexArrayFloat x) {
		
		MyComplexArrayFloat y = x.clone();
		int n = x.size();
		FloatFFT fft = new FloatFFT(n);	
		fft.complexInverse(y.buffer, true); // scale, matlab does
		return y;
	}
	

	/**
	 * 
	 * @param xr
	 * @param n
	 * @return
	 */
	private static float [] cloneAndPadding(float xr[], int n) {
		float [] x;
		if (n <= xr.length) {
			x = xr.clone();
		}
		else // Need an ending zero padding
		{
			x = new float [n];
			System.arraycopy(xr,  0,  x,  0,  xr.length);
			Arrays.fill(x, xr.length, n, 0.0f);
		}
		return x;
	}
	
}
