package myVoiceBox;

import myVoiceBox.MySort.SortDir;

public class Lpcar2fm {
	
	public int nn[]; // Number of formants found, for each frame
	public double ff[][]; // Formant frequencies in normalized Hz (in increasing order), for each frame
	public double aa[][]; // Formant amplitudes
	public double bb[][]; // Formant bandwidths in normalized Hz
	
	int n;
	private double [] f;
	private double [] a;
	private double [] b;

	// Work with a single frame
	
	public Lpcar2fm(double [] ar, double t) {
		
		doSingleFrame(ar, t);
		nn = new int[] {n};
		ff = new double[][] {f.clone()};
		aa = new double[][] {a.clone()};
		bb = new double[][] {b.clone()};		
	}


	public Lpcar2fm(double [][]ar, double t) throws Exception { //n, f, a, b] = lpcar2fm(ar, t)

		int numFrames = ar.length;
		
		nn = new int[numFrames];
		ff = new double[numFrames][];
		aa = new double[numFrames][];
		bb = new double[numFrames][];
		
		
		for (int jf=0; jf<numFrames; jf++)
		{
			double [] curr_ar = ar[jf];
			doSingleFrame(curr_ar, t);
			nn[jf] = n;
			ff[jf] = f.clone();
			aa[jf] = a.clone();
			bb[jf] = b.clone();
		}
	}
	
	public Lpcar2fm() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 * @param ar
	 * @param t
	 */
	private void doSingleFrame(double [] ar, double t) {

		int p1 = ar.length;
		double [][] zz = Lpcar2zz.get(ar);
		int numberOfRoots = zz.length;
		boolean [] ig = new boolean[numberOfRoots];
		int sum_ig_2 = 0;
		for (int i =0; i<numberOfRoots; i++)
		{
			double [] root = zz[i];
			double imag = root[1];
			ig[i] = (imag<=0);
			if (imag<=0)
				sum_ig_2++;
		}
			
		int n = p1 - 1 - sum_ig_2;
		int p = p1 - 1;
		if (n<p)
		{
			boolean [] ig_sorted_= new boolean[numberOfRoots]; //p
			
			double [][] zz_sorted = new double[numberOfRoots][2];
			int ifalse = 0; //Put the logical zeros starting from the beginning
			int itrue = p-1; // Put the logical "one" starting from the end
			// Reorder zz by sorting the booleans 
			for (int i=0; i<numberOfRoots; i++) // <p
			{
				if (ig[i])
				{
					ig_sorted_[itrue] = true;
					zz_sorted[itrue] = zz[i];
					itrue--;
				}
				else
				{
					ig_sorted_[ifalse] = false;
					zz_sorted[ifalse] = zz[i];
					ifalse++;
				}
			}
			// Now keep only the first mn elements
			for (int i =0; i<n; i++)
			{
				ig[i] = ig_sorted_[i];
				zz[i] = zz_sorted[i];
			}
		}
		// WARNING: from now on, ig max allowed index is n-1 (included), the same for zz
		double [] f = new double[n];
		double [] b = new double[n];
		for (int i=0; i<n; i++)
		{
			if (ig[i])
			{
				zz[i][0] = 1;
				zz[i][1] = 0;
			}
			double re = zz[i][0];
			double im = zz[i][1];
			f[i] = Math.atan2(im, re) * 0.5 / Math.PI; // the angle *0.5/pi
			b[i] = -Math.log( Math.sqrt(re*re + im*im) ) / Math.PI;
			if (t>0)
				ig[i] |= (b[i] > t*f[i]);
			else
				ig[i] |= (b[i]+t>0);
			
			if (ig[i]) {
				f[i] = 0;
				b[i] = 0;
			}
		}
		int temps = 0;
		for (int i=0; i<n; i++)
			if (ig[i])
				temps++;
		n = n - temps; // Quindi n puo' ancora diminuire
		
		double []ig_plus_f = new double[n+temps]; // Still the old length (the old value of n)
		for (int i=0; i<ig_plus_f.length; i++)
			ig_plus_f[i] = (ig[i]) ? 1+f[i] : f[i];
		
		 int [] ix = MySort.sort_and_replace(ig_plus_f, SortDir.ASCENDING);
		 double [][] zzFinal = new double[n][2];
		 double []fFinal = new double [n];
		 double [] bFinal = new double [n];
		 for (int i=0; i<n; i++) { // take only the first n elements
			 int j = ix[i];
			 zzFinal[i][0] = zz[j][0]; // useless
			 zzFinal[i][1] = zz[j][1]; // useless
			 fFinal[i] = f[j];
			 bFinal[i] = b[j];
		 }
		 
		 double [][]y = new double[n][p1];
		 double [][]re = new double[n][p1];
		 double [][]im = new double[n][p1];
		 double [] a = new double[n];
		 for (int i = 0; i<n; i++) {
			 double re_s = 0;
			 double im_s = 0;
			 for (int j=0; j<p1; j++) {
				 double pw = -2*Math.PI*j;
				 double x = pw*fFinal[i];
				 double re_ij = Math.cos(x) * ar[j];
				 double im_ij = Math.sin(x) * ar[j];			 
				 re_s += re_ij;
				 im_s += im_ij;
			 }
			 a[i]= 1.0/Math.sqrt( re_s*re_s + im_s*im_s);
		 }
		 
		 // Output
		this.a = a;
		this.b = bFinal;
		this.f = fFinal;
		this.n = n;		
	}
	
	
	
}
