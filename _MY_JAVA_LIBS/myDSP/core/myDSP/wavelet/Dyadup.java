package myDSP.wavelet;

import java.util.Arrays;

import myMath.MyMath;

/**
 * %DYADUP Dyadic upsampling.
%   DYADUP implements a simple zero-padding scheme very
%   useful in the wavelet reconstruction algorithm.
%
%   Y = DYADUP(X,EVENODD), where X is a vector, returns
%   an extended copy of vector X obtained by inserting zeros.
%   Whether the zeros are inserted as even- or odd-indexed
%   elements of Y depends on the value of positive integer
%   EVENODD:
%   If EVENODD is even, then Y(2k-1) = X(k), Y(2k) = 0.
%   If EVENODD is odd,  then Y(2k-1) = 0   , Y(2k) = X(k).
%
%   Y = DYADUP(X) is equivalent to Y = DYADUP(X,1)
%
%   Y = DYADUP(X,EVENODD,'type') or
%   Y = DYADUP(X,'type',EVENODD) where X is a matrix,
%   return extended copies of X obtained by inserting columns 
%   of zeros (or rows or both) if 'type' = 'c' (or 'r' or 'm'
%   respectively), according to the parameter EVENODD, which
%   is as above.
%
%   Y = DYADUP(X) is equivalent to
%   Y = DYADUP(X,1,'c')
%   Y = DYADUP(X,'type')  is equivalent to
%   Y = DYADUP(X,1,'type')
%   Y = DYADUP(X,EVENODD) is equivalent to
%   Y = DYADUP(X,EVENODD,'c') 
%
%            |1 2|                              |0 1 0 2 0|
%   When X = |3 4|  we obtain:  DYADUP(X,'c') = |0 3 0 4 0|
%
%                     |1 2|                      |1 0 2|
%   DYADUP(X,'r',0) = |0 0|  , DYADUP(X,'m',0) = |0 0 0|
%                     |3 4|                      |3 0 4|
%
%   See also DYADDOWN.

%   M. Misiti, Y. Misiti, G. Oppenheim, J.M. Poggi 12-Mar-96.

 * @author FS
 *
 */
public class Dyadup {

	public static double [] get(double [] x, int evenoddVal, int evenoddLen) {
		
		if ((x==null) || (x.length==0)) // Special case
			return null;
		
		int def_evenodd = 1;
		int nbInVar = 2;
		int evenLEN = 0;
		int dim=1;
		int p = evenoddVal;
		evenLEN = 1;
		int rem2 = (int) MyMath.rem(p,  2);
		
		int addLEN = (evenLEN>0) ? 0 : 2*rem2-1;
		int l = 2*x.length+addLEN;
		double [] y = new double [l];
		Arrays.fill(y,  0);
		int j=0;
		for (int i=1+rem2; i<=l; i+=2)
			y[i-1] = x[j++];
		
		return y;
	}
}
