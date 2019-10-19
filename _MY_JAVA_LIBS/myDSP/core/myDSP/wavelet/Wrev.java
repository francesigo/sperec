package myDSP.wavelet;

/**
 * 
 * @author FS
 *
 *WREV Flip vector.
 *  Y = WREV(X) reverses the vector X.
 *
 *   See also FLIPLR, FLIPUD.

 *   M. Misiti, Y. Misiti, G. Oppenheim, J.M. Poggi 01-May-96.

 */
public class Wrev {

	public static double [] get(double []x) {
		double [] y = new double [x.length];
		for (int i=0; i<x.length; i++)
			y[i] = x[x.length-1-i];
		
		return y;
	}
}
