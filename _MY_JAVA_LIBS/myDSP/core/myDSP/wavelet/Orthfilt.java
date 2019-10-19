package myDSP.wavelet;

import myMath.MyMath;

/**
 * %ORTHFILT Orthogonal wavelet filter set.
%   [LO_D,HI_D,LO_R,HI_R] = ORTHFILT(W) computes the
%   four filters associated with the scaling filter W 
%   corresponding to a wavelet:
%   LO_D = decomposition low-pass filter
%   HI_D = decomposition high-pass filter
%   LO_R = reconstruction low-pass filter
%   HI_R = reconstruction high-pass filter.
%
%   See also BIORFILT, QMF, WFILTERS.
 * @author FS
 *
 */
public class Orthfilt {

	double [] LO_D;
	double [] HI_D;
	double [] LO_R;
	double [] HI_R;
	
	public Orthfilt(double [] W) throws Exception {
		this(W,0);
	}
	public Orthfilt(double [] W_, int P) throws Exception {
		
		// Normalize filter sum.
		double Wsum = MyMath.sum(W_);
		double [] W = new double[W_.length];
		for (int i=0; i<W.length; i++)
			W[i] = W_[i]/Wsum;
		
		// Associated filters.
		LO_R = new double[W.length];
		double sqrt2 = Math.sqrt(2);
		for (int i=0; i<W.length; i++)
			LO_R[i] = sqrt2 * W[i];
		HI_R = Qmf.getFilter(LO_R, P);
		HI_D = Wrev.get(HI_R);
		LO_D = Wrev.get(LO_R);
	}
}
