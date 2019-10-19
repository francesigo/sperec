package myDSP;

import java.util.Arrays;

import myMath.MyMath;

public class MyFFTDouble {

	/**
	 * Compute the FFT of a real sequence
	 * @param x: the real sequence
	 * @return a ComplexArray 
	 */
	public static MyComplexArrayDouble realForwardFull (double [] x) {
		return realForwardFull(x, x.length);
	}
	
	/**
	 * Compute the FFT of a real sequence with n bins
	 * @param xr the input sequence of reals
	 * @param n the number of bin
	 * @return the resulting complex sequence
	 */
	public static MyComplexArrayDouble realForwardFull (double [] xr, int n) {
		
		double [] x = cloneAndPadding(xr, n);
				
		//Because the result is stored in <code>buffer</code>,
	    //the size of the input array must greater or equal 2*n, with only the
	    //first n elements filled with real data.
		double [] buffer = new double[2*n];
		System.arraycopy(x, 0, buffer, 0, n);
		Arrays.fill(buffer, n, 2*n, 0.0f);
		
		DoubleFFT fft = new DoubleFFT(n);		

		fft.realForwardFull(buffer);

		return new MyComplexArrayDouble(buffer);
		
	}
	

	
	public static MyComplexArrayDouble realForward(double [] xr, int n) {
		
		double [] x = cloneAndPadding(xr, n);
		
		DoubleFFT fft = new DoubleFFT(n);
		fft.realForward(x);
		return null;
		
	}
	
	/**
	 * Compute the IFFT (Inverse Fast Fourier Transform) of a complex sequence
	 * @param x the input complex sequence
	 * @return a complex sequence as result of IFFT transform
	 */
	public static MyComplexArrayDouble complexInverse(MyComplexArrayDouble x) {
		
		MyComplexArrayDouble y = x.clone();
		int n = x.size();
		DoubleFFT fft = new DoubleFFT(n);	
		fft.complexInverse(y.buffer, true); // scale, matlab does
		return y;
	}
	

	/**
	 * 
	 * @param xr
	 * @param n
	 * @return
	 */
	private static double [] cloneAndPadding(double xr[], int n) {
		double [] x;
		if (n <= xr.length) {
			x = xr.clone();
		}
		else // Need an ending zero padding
		{
			x = new double [n];
			System.arraycopy(xr,  0,  x,  0,  xr.length);
			Arrays.fill(x, xr.length, n, 0.0f);
		}
		return x;
	}
	
}
