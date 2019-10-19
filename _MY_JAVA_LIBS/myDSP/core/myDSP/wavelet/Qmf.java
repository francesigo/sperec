package myDSP.wavelet;

import myMath.MyMath;

/**
 * QMF    Quadrature mirror filter.
 * @author FS
 *
 *   Y = QMF(X,P) changes the signs of the even index entries
 *   of the reversed vector filter coefficients X if P is even.
 *   If P is odd the same holds for odd index entries.
 *
 *  Y = QMF(X) is equivalent to Y = QMF(X,0).

 *  M. Misiti, Y. Misiti, G. Oppenheim, J.M. Poggi 12-Mar-96.
 */

public class Qmf {

	public static double [] getFilter(double [] x) throws Exception {
		return getFilter(x, 0);
	}
	
	public static double [] getFilter(double [] x, int p) throws Exception {

		//Check arguments.
		if ((p!=MyMath.fix(p)) || (p<0))
			throw new Exception("Qmf: Invalid argument p");

		//Compute quadrature mirror filter.
		int L = x.length;
		double [] y = new double[L];
		for (int i=0; i<L; i++)
			y[i] = x[L-1-i];
		int first = 2- (int)MyMath.rem(p, 2) -1;
		for (int i = first; i<L; i+=2)
			y[i] = -y[i];

		return y;		
	}
}
