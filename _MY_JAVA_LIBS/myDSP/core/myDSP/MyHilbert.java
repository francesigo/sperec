package myDSP;

import java.util.Arrays;

import myMath.MyMath;

/**
 * Hilbert transform of a real sequence
 * @author FS
 *
 */
public class MyHilbert {
		
	static public MyComplexArrayDouble get(float [] xr) {
		return get(MyMath.todouble(xr), xr.length);	
	}
	
	
	static public MyComplexArrayDouble get(double [] xr, int n) {
		
		MyComplexArrayDouble c = MyFFTDouble.realForwardFull(xr, n);

		double[] h = new double[n];
		if ( (double)n == 2.0*MyMath.fix((double)n/2.0)) {
			h[0] = 1;
			Arrays.fill(h, 1, n/2, 2.0f);
			h[n/2] = 1;	
			Arrays.fill(h, n/2+1, n, 0.0f);
		}
		else
		{
			h[0] = 1;
			Arrays.fill(h, 1, (n+1)/2, 2.0f);
			Arrays.fill(h, (n+1)/2, n, 0.0f);
		}
		
		// Multiply the fft with the real sequence h. The result is stored into the myFFT buffer
		c.timesRealSequenceSelf(h);
		
		// ifft. The result is stored into the myFFT buffer
		c = MyFFTDouble.complexInverse(c);
		
		return c;
	}
	

	//// OLD, with float
	static public MyComplexArrayFloat get_old(float [] xr) {
		return get_old(xr, xr.length);	
	}
	
	static public MyComplexArrayFloat get_old(float [] xr, int n) {
		
		MyComplexArrayFloat c = MyFFT.realForwardFull(xr, n);

		float[] h = new float[n];
		if ( (double)n == 2.0*MyMath.fix((double)n/2.0)) {
			h[0] = 1;
			Arrays.fill(h, 1, n/2, 2.0f);
			h[n/2] = 1;	
			Arrays.fill(h, n/2+1, n, 0.0f);
		}
		else
		{
			h[0] = 1;
			Arrays.fill(h, 1, (n+1)/2, 2.0f);
			Arrays.fill(h, (n+1)/2, n, 0.0f);
		}
		
		// Multiply the fft with the real sequence h. The result is stored into the myFFT buffer
		c.timesRealSequenceSelf(h);
		
		// ifft. The result is stored into the myFFT buffer
		c = MyFFT.complexInverse(c);
		
		return c;
	}

}
