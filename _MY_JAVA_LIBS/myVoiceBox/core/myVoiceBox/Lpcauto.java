package myVoiceBox;

import java.util.Arrays;

import myMath.MyMath;
import myMath.MyMatrix;

/**
 * Performs autocorrelation LPC analysis
 * @author FS
 *
 */
public class Lpcauto {
	
	/**
	 * The AR coefficient with ar[i][0] = 1, for each frame i-th
	 */
	double [][] ar;
	
	/**
	 * The energy of the residuals
	 */
	double e[];
	
	//public double [][] rrDebug;
	
	/**
	 * Getter for ar
	 * @return the ar coefficients
	 */
	public double [][] getAR() {return ar;}
	
	/**
	 * Getter for the energy residuals
	 * @return the energy residuals
	 */
	public double [] getE() {return e;}
	
	/**
	 * Do the job for each frame, where a frame is an array of float
	 * @param faFrames arrays of frames
	 * @param p order
	 * @return the Lpcauto instance witjh results
	 * @throws Exception
	 */
	public static Lpcauto fromFloatFramed(float[][] faFrames, int p) throws Exception {
		
		// Convert to double
		double [][] daFrames = new double[faFrames.length][];
		for (int i=0; i<daFrames.length; i++)
		{
			float [] f = faFrames [i];
			int n = f.length;
			double [] d = new double[n];
			daFrames[i] = d;
			for (int j=0; j<n; j++)
				d[j] = f[j];
		}
		
		return fromDoubleFramed(daFrames, p);
	}	
	
	/**
	 * Do the job for each frame, where a frame is an array of double
	 * @param daFrames: the framed (not windowed) audio signal
	 * @param p: the order
	 * @return the Lpcauto instance with results
	 * @throws Exception 
	 */
	public static Lpcauto fromDoubleFramed(double[][] daFrames, int p) throws Exception {
		
		Lpcauto me = new Lpcauto();
		
		int numFrames = daFrames.length;
		
		// Preallocate room for residual energy values...
		me.e = new double [numFrames];
		
		// Initialize a matrix with numFrames rows and p+1 columns
		me.ar = new double[numFrames][p+1];

		int nc = daFrames[0].length; // // Number of samples in the current frame Dovrebbe essere sempre uguale
		int pp = Math.min(p, nc);
		
		//rrDebug = new double[numFrames][];

		// For each frame....
		for (int jf=0; jf<numFrames; jf++)			
			me.e[jf] = lpcauto(daFrames[jf], p, pp, me.ar[jf]);
		
		return me;

	}
	
	/**
	 * Compute the a.r. given the frame, return the energy of the residual
	 * @param dd: the samples forming the frame
	 * @param p: the order
	 * @param ar: the buffer where to put the a.r.
	 * @return the energy of the residual
	 * @throws Exception 
	 */
	private static double lpcauto(double [] dd, int p, int pp, double [] ar) throws Exception {
		
		int nc = dd.length;
		
		Arrays.fill(ar,  0.0);
		ar[0] = 1;
		
		// Apply a Hamming Window
		double [] wd = MyMath.applyHammingWindow(dd);

		double[] rr = new double[pp+1];
		for (int j=0; j<pp+1; j++)
		{
			double s = 0.0;
			for (int i=0; i+j<nc; i++)
				s += wd[i]*wd[i+j];

			rr[j] = s;
		}
		//rrDebug[jf] = rr;

		MyMatrix RM = MyMatrix.toeplitzReal(rr, pp); // Just pp elements, not rr.length
		int rk = RM.rank();
		if (rk>0)
		{
			if (rk<pp)
				RM = RM.getMatrix(0, rk-1, 0, rk-1);

			double [][] temp = new double[1][rk];				
			for (int i = 0; i<rk; i++)
				temp[0][i] = -rr[i+1];

			// I think that this may have some round off errors with respect to matlab implementation
			double [][] xt = RM.transpose().solve(new MyMatrix(temp).transpose()).getArray();

			for (int i=0; i<rk; i++)
				ar[i+1] = xt[i][0];
			
		}

		// The residual energy
		double en = 0;
		for (int i=0; i<pp+1; i++)
			en += rr[i]*ar[i];

		return en;		
	}

	/**
	 * Print information about the first three frames
	 * @param title the title to be printed before any other info
	 */
	public void showInfo(String title) {

		System.out.println(title);
		System.out.println("  Number of frames: " + ar.length);
		showInfoFrame(0);
		showInfoFrame(1);
		showInfoFrame(2);
		showInfoFrame(ar.length-1);
	}
	
	/**
	 * Print information about a frame
	 * @param i the index of the frame to be displayed
	 */
	public void showInfoFrame(int i) {
		
		double [] iar = this.ar[i];
		System.out.print("    frame # " + i + "\t[" + iar.length + "]: {");
		
		for (int j=0; j<iar.length-1; j++)
			System.out.print(" " + iar[j] + ",");
		
		System.out.println(" " + iar[iar.length-1] + "}");
		
	}
}
