package myVoiceBox;

import myMath.MyMath;

/**
 * Convert autoregressive coefficients to autocorrelation coefficients.
 * The routine calculated the autocorrelation coefficients of the signal
 * that results from feeding unit-variance, zero-mean noise into the all-pole filter
 *
 * @author FS
 *
 */
public class Lpcar2rr {

	
	/**
	 * Convert autoregressive coefficients to autocorrelation coefficients (frame by frame)
	 * The computation relies on lpcar2rf and lpcrf2rr.
	 * lpcar2rf works frame by frame
	 * lpcrf2rr works frames by frame
	 * So even the current function can work frame by frame
	 * @param ar autoregressive coefficients frame by frame: ar[0] are coefficients of the first frame, etc.
	 * @return autocorrelation coefficients, frame by frame
	 * @throws Exception 
	 */
	static public double [][] exe(double [][]ar) throws Exception {
		
		int nf = ar.length;
		double [][] rr = new double[nf][];
		for (int i=0; i<nf; i++)
			rr[i] = exe(ar[i]);
		return rr;
	}
	
	/**
	 * Convert autoregressive coefficients to autocorrelation coefficients (single frame)
	 * @param ar autoregressive coefficients
	 * @return the autocorrelation coefficients
	 * @throws Exception
	 */
	static public double [] exe(double [] ar) throws Exception {
		double k = 1.0/(ar[0]*ar[0]); // k=ar(:,1).^(-2);
		double [] rr;
		if( ar.length==1 )
			rr = new double [] {k};
		else
		{
			// rr=lpcrf2rr(lpcar2rf(ar)).*k(:,ones(1,size(ar,2)));
			double [] A = Lpcar2rf.exe(ar);
			Lpcrf2rr B = new Lpcrf2rr(A);
			double[] _B = B.rr[0];
			rr = MyMath.times(_B, k);
		}
		
		return rr;			
	}
	
	
}
