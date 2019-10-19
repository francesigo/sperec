package myDSP;

/**
 * %DOWNSAMPLE Downsample input signal.
%   DOWNSAMPLE(X,N) downsamples input signal X by keeping every
%   N-th sample starting with the first. If X is a matrix, the
%   downsampling is done along the columns of X.
 * @author FS
 *
 */
public class MyDownSample {

	public static double [] exe(double [] x, int q) {
		int L = (int)Math.ceil(x.length/q);
		double [] y = new double [L];
		int i=0;
		for (int j=0; j<L; j++)
		{
			y[j] = x[i];
			i+=q;
		}
		return y;		
	}
}
