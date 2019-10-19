package myDSP;

import java.util.Arrays;

/**
%UPSAMPLE Upsample input signal.
%   UPSAMPLE(X,N) upsamples input signal X by inserting
%   N-1 zeros between input samples.  X may be a vector
%   or a signal matrix (one signal per column).
*/
public class MyUpsample {

	public static double [] exe(double [] x, int p) {
		double [] y = new double[p*x.length];
		Arrays.fill(y,  0.0);
		int i=0;
		for (int j=0; i<y.length; j+=p)
			y[j] = x[i++];
		return y;
	}
}
