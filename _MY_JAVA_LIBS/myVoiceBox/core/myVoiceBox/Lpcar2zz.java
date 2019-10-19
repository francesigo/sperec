package myVoiceBox;

import myMath.MyMath;

/**
 * Convert ar filter to z-plane poles
 * @author FS
 *
 */
public class Lpcar2zz {


	/**
	 * Convert ar filter to z-plane poles
	 * @param ar coefficients
	 * @return roots; i-th root is [i][0] = real part; [i][1] = imaginary part
	 */
	public static double [][] get(double [] ar) {
		
		double [][] zz = MyMath.roots(ar);
		return zz;
	}
	
	/**
	 * Convert ar filter to z-plane poles, frame by frame
	 * @param ar ar coefficients, frame by frame
	 * @return double [][][], where [k][i][0] is the real part of the i-th root of the k-frame and [k][i][1] is the imaginary part of the i-th root of the k-frame
	 */
	public static double [][][] get(double [][] ar) {
		
		int numFrames = ar.length;
		
		double [][][] zzz = new double[numFrames][][];
		
		for (int jf=0; jf<numFrames; jf++)
			zzz[jf] = MyMath.roots(ar[jf]);
		
		return zzz;
	}

}