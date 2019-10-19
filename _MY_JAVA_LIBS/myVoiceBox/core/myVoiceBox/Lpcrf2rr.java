package myVoiceBox;

import java.util.Arrays;

import myMath.MyMath;

/**
 * Convert reflection coefficients to autocorrelation coefficients
 *
 * @author FS
 *
 */
public class Lpcrf2rr {

	/**
	 * Output n+1 autocorrelation coefficients for each frame
	 */
	double [][] rr = null;
	
	/**
	 * Output n+1 AR filter coefficients for each frame
	 */
	double [][] ar = null;
	
	/**
	 * Convert reflection coefficients to autocorrelation coefficients.
	 * Works on matrices, frame by frame.
	 * @param rf reflection coefficients, n+1, frame by frame.
	 * @throws Exception
	 */
	public Lpcrf2rr(double [][] rf) throws Exception {
		
		int nf = rf.length;
		rr = new double[nf][];
		ar = new double[nf][];
				
		for (int i=0; i<nf; i++)
		{
			double [][] rrar = exe_helper(rf[i]);
			rr[i] = rrar[0];
			ar[i] = rrar[1];
		}
	}
	
	/**
	 * Convert reflection coefficients to autocorrelation coefficients
	 * Here a single frame is provided
	 * @param rf reflection coefficients, n+1
	 * @throws Exception
	 */
	public Lpcrf2rr(double [] rf) throws Exception {
		
		int nf = 1;
		rr = new double[nf][];
		ar = new double[nf][];
				
		double [][] rrar = exe_helper(rf);
		rr[0] = rrar[0];
		ar[0] = rrar[1];
		
	}
	
	
	
	// Helper functions
		
	/**
	 * Do the job on a single frame of rf vectors
	 * Return both rr and ar in a double[][], where rr = .[0], and ar = -.[1]
	 * @param rf
	 * @return
	 * @throws Exception
	 */
	private static double [][] exe_helper (double [] rf) throws Exception {
	
		int p1 = rf.length; // [nf,p1]=size(rf);
		int p0 = p1-1;
		double [] rr =  null;
		double [] ar = null;
		
		if (p0!=0)
		{
			double [] a = new double [] {rf[1]}; //a = rf(:,2); // array eith a single elemtne, at the moment

			// rr=[ones(nf,1) -a zeros(nf,p0-1)];
			rr = new double[p0+1];
			Arrays.fill(rr,  0.0);
			rr[0] = 1;
			rr[1] = -a[0];

			// e = (a.^2-1);
			double e = a[0]*a[0]-1;

			for (int n = 2; n<=p0; n++) // for n = 2:p0
			{
				double k = rf[n]; // k=rf(:,n+1);  // It s a column vector

				// rr(:,n+1) =k.*e - sum(rr(:,n:-1:2).*a,2);
				rr[n] = k*e - MyMath.sum(MyMath.arrayTimes(MyMath.select(rr, n-1, 1, -1), a));

				// a = [a+k(:,ones(1,n-1)).*a(:,n-1:-1:1) k];
				double [] aa = new double[a.length +1];
				for (int i=0; i<a.length; i++)
					aa[i] = a[i] + k*a[n-2-i];
				aa[aa.length-1] = k;
				a = aa;

				// e = e.*(1-k.^2);
				e *= (1-k*k);
			}
			  
			// ar = [ones(nf,1) a];
			ar = new double [1+a.length];
			ar[0] = 1;
			System.arraycopy(a, 0, ar, 1, a.length);
			
			double r0 = 1.0/MyMath.sum(MyMath.arrayTimes(rr,  ar)); // r0=sum(rr.*ar,2).^(-1);
			
			MyMath.timesSelf(rr, r0); // rr=rr.*r0(:,ones(1,p1));
			
			// if nargin>1 && ~isempty(p)
			//      if p<p0
			//         rr(:,p+2:p1)=[];
			//      else
			//
			//         rr=[rr zeros(nf,p-p0)];
			//         af=-ar(:,p1:-1:2);
			//         for i=p0+1:p
			//            rr(:,i+1)=sum(af.*rr(:,i-p0+1:i),2);
			//         end
			//      end
			//   end
		}
		else
		{
			rr = new double [] {1.0}; // rr=ones(nf,1);
			ar = new double [] {1.0}; // ar=rr;
		}

		return new double [][] {rr, ar};
	}
}
