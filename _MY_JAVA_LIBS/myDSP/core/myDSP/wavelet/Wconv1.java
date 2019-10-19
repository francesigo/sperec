package myDSP.wavelet;

import myMath.MyMath;

/**
 * %WCONV1 1-D Convolution.
%   Y = WCONV1(X,F) performs the 1-D convolution of the 
%   vectors X and F.
%   Y = WCONV1(...,SHAPE) returns a subsection of the
%   convolution with size specified by SHAPE (See CONV2).

%   M. Misiti, Y. Misiti, G. Oppenheim, J.M. Poggi 06-May-2003.

 * @author FS
 *
 */
public class Wconv1 {

	public static double [] get(double [] x, double [] f) throws Exception {
		return get(x, f, "full");
		
	}
	public static double [] get(double [] x, double [] f, String shape) throws Exception {
		
		return MyMath.conv2(x,  f,  shape);
	}
	
	/**
	 * For test and debug
	 * @param arg
	 * @throws Exception
	 */
	static public void main (String arg[]) throws Exception {
		double [] x = new double [] {10, 20, 30, 40, 50};
		double [] f = new double [] {-1, -2, -3};
		double [] res = Wconv1.get(x, f);
	}
}
