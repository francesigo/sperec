package myVoiceBox;

import myDSP.MyComplexArrayFloat;
import myDSP.MyFFT;
import myMath.MyMath;

/**
 * Calculate the DFT of real data
 * Data is truncated/padded to length n
 *   N even:	(N+2)/2 points are returned with
 * 			the first and last being real
 *   N odd:	(N+1)/2 points are returned with the
 * 			first being real
 * In all cases fix(1+N/2) points are returned
 * @author FS
 *
 */
public class Rfft {
	
	/**
	 * Calculate the DFT of real data
	 * @param x the input real sequence as array of double
	 * @param n number of points
	 * @return the resulting complex sequence
	 */
	static public MyComplexArrayFloat exe (double [] x, int n) {
		
		// Bad hack...
		float [] xr = MyMath.tofloat(x);
		return exe(xr, n);
		
	}
	
	/**
	 * Calculate the DFT of real data
	 * @param x the input real sequence as array of float
	 * @param n number of points
	 * @return the resulting complex sequence
	 */
	static public MyComplexArrayFloat exe (float [] x, int n) {
		
		MyComplexArrayFloat y = MyFFT.realForwardFull(x,  n);
		
		int size = 1 + (int)MyMath.fix(n/2);
		MyComplexArrayFloat res = y.select(0, size-1);
		return res;
	}
}
