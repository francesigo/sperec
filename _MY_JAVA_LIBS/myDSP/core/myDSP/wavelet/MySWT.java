package myDSP.wavelet;

public class MySWT {

	public double [][] SWA;
	public double [][] SWD;
	
	public MySWT() {
		SWA = null;
		SWD = null;
	}
	
	public void execute(double [] x, int n, String wname) throws Exception {
		
		//swt(y, levels, 'sym8')
		int s = x.length;
		double pow = Math.pow(2, n);
		
		if ( s % pow > 0)
			throw new Exception (this.getClass().getName() + ": invalid input length");
		
		if (!(wname.equals("sym8")))
			throw new Exception (this.getClass().getName() + ": " + wname + " wavelet not yet supported");
		
		int wtype = 1; // orth. wavelet
		
		if ((wtype!=1) && (wtype!=2))
			throw new Exception(this.getClass().getName() + ": wavelet type must be orthogonal or biorthogonal");
		
		double [][] ff = Wfilters.getFilters(wname, "d");
		double [] lo = ff[0];
		double [] hi = ff[1];
		
		// Compute stationary wavelet coefficients.
		int evenoddVal = 0;
		int evenLEN    = 1;
		
		double [][] swd = new double[n][];
		double [][] swa = new double[n][];
		
		for (int k=0; k<n; k++)
		{
			 // Extension.
			 int   lf = lo.length;
			 double [] xe  = Wextend.get("1D", "per", x, lf/2);
			 
			 // Decomposition
			 swd[k] = Wkeep1.get(Wconv1.get(xe,hi), s, lf+1);
			 swa[k] = Wkeep1.get(Wconv1.get(xe,lo), s, lf+1);
			 
			 // upsample filters.
			 lo = Dyadup.get(lo, evenoddVal, evenLEN);
			 hi = Dyadup.get(hi, evenoddVal, evenLEN);
			 
			 // New value of x
			 x = swa[k];
		}
		
		SWA = swa;
		SWD = swd;
	}
}
