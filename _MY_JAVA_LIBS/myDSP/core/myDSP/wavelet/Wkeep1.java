package myDSP.wavelet;

import myMath.MyMath;

/**
 * %WKEEP1  Keep part of a vector.
%
%   Y = WKEEP1(X,L,OPT) extracts the vector Y 
%   from the vector X. The length of Y is L.
%   If OPT = 'c' ('l' , 'r', respectively), Y is the central
%   (left, right, respectively) part of X.
%   Y = WKEEP1(X,L,FIRST) returns the vector X(FIRST:FIRST+L-1).
%
%   Y = WKEEP1(X,L) is equivalent to Y = WKEEP1(X,L,'c').

%   M. Misiti, Y. Misiti, G. Oppenheim, J.M. Poggi 07-May-2003.
 * @author FS
 *
 */
public class Wkeep1 {

	public static double [] get(double [] x, int len, int opt) throws Exception {
		
		if (len != MyMath.fix(len))
			throw new Exception("Wkeep1: invalid len");
		
		//double [] y = x.clone();
		int sx = x.length;
		
		boolean ok = (len>=0) && (len<sx);
		if (!ok)
			return x.clone(); //return y;
		
		int first = opt;
		int last = first + len -1;
		
		if ( (first != MyMath.fix(first)) || (first<1) || (last>sx))
			throw new Exception("Wkeep1: invalid arg");
		
		double [] y = new double [last-first+1];
		System.arraycopy(x,  first-1,  y,  0,  y.length);
		
		return y;
		
		
	}
}
