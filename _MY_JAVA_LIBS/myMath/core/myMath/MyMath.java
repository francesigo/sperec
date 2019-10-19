package myMath;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class MyMath {

	public static boolean [] not(boolean [] x) {
		boolean [] y = new boolean [x.length];
		for (int i=0; i<y.length; i++)
			y[i] = !x[i];
		return y;
	}
	
	public static double dot(double [] x, double [] y) throws Exception {
		if (x.length!=y.length)
			throw new Exception("Length mismatch");
		
		double d = 0;
		for (int i =0; i<x.length; i++)
			d += x[i]*y[i];
		return d;
	}
	public static int nextpow2(double x) {
		int p = 0;
		int y = 1;
		while (y<x)
		{
			p++;
			y = y <<1;
		}
		return p;
	}
	public static double [] cumsum(double [] x) {
		double [] c = new double [x.length];
		c[0] = x[0];
		for (int i=1; i<x.length; i++)
			c[i] = c[i-1] + x[i];
		return c;
	}
	public static int [] cumsum(int [] x) {
		
		return cumsum(x, 0, x.length-1);
	}
	public static int [] cumsum(int [] x, int from, int to) {
		int [] c = new int [to-from+1];
		c[0] = x[from];
		for (int i=1; i<c.length; i++)
			c[i] = c[i-1] + x[from+i];
		return c;
	}
	public static boolean all(boolean [] b) {
		for (int i=0; i<b.length; i++)
			if (!b[i])
				return false;
		return true;
	}
	
	public static boolean any(boolean [] b) {
		for (int i=0; i<b.length; i++)
			if (b[i])
				return true;
		return false;
	}
	public static boolean any(double [] d) {
		for (int i=0; i<d.length; i++)
			if (d[i] != 0.0)
				return true;
		return false;
	}
	
	public static boolean [] isnan(double [] x) {
		boolean [] y = new boolean[x.length];
		for (int i=0; i<x.length; i++)
			y[i] = Double.isNaN(x[i]);
		return y;
	}
	
	/**
	 * transforms non-normally distributed data to normally distributed data.
	 * transforms the data vector DATA using a certain specified LAMBDA for the Box-Cox Transformation.
	 * The Box-Cox Transformation is the family of power transformation:
     * DATA(LAMBDA) = ((DATA^LAMBDA) - 1) / LAMBDA;     if LAMBDA ~= 0,
     *	or
 	 * DATA(LAMBDA) = log(DATA);                        if LAMBDA == 0.
	 * @param lambda
	 * @param data
	 * @return
	 */
	public static double boxcox(double lambda, double data) {
		return (lambda==0.0) ? Math.log(data) : (Math.pow(data, lambda) - 1.0)/lambda;
	}

	
	public static double [] conv2(double [] x, double [] f, String shape) throws Exception {
		
		if(!shape.equals("full"))
			throw new Exception("Wconv1: shape " + shape + " not supported");
		
		
		int m = x.length;
		int n = f.length;
		
		double [] Result = new double[m + n - 1];
		
		for (int k=1; k<=Result.length; k++) {
			int j1 = Math.max(1,  k+1-n);
			int j2 = Math.min(k,  m);
			double acc = 0.0;
			for (int j=j1; j<=j2; j++) {
				acc+= x[j-1]*f[k-j];
			}
			Result[k-1] = acc;
		}
		
	
		return Result;
	}

	/**
	 * linspace equivalent of Matlab
	 * @param d1
	 * @param d2
	 * @param n
	 * @return
	 */
	public static double [] linspace(double d1, double d2, int n) {
		int n1 = n-1;
		double d = d2-d1;
		double c = d*(n1-1);
		double [] y = new double[n];
		for (int i=0; i<n; i++)
			y[i] = d1 + i*d/n1;
		
		if (d1==d2)
			Arrays.fill(y, d1);
		else {
			y[0] = d1;
			y[n-1] = d2;
		}
		return y;
		
	}
	public static double [] linspace_old(double start, double end, int numPoints) {
		double [] x = new double[numPoints];
		int numIntervals = numPoints-1;
		double inc = (double)(end-start)/(double)numIntervals;

		x[0] = start;
		double acc = 0;
		for (int i=1; i<numPoints; i++)
		{
			acc+=inc;
			x[i] = x[0] + acc;
		}
		x[0] = start;
		x[x.length-1] = end; // This is to face precision errors
		return x;
	}
	
	/**
	 * Equivalent to Matlab's start:end
	 * @param start
	 * @param end
	 * @return
	 */
	public static int [] colon(int start, int end, int step) {
		
		if (step==0)
			return null;
		
		if (start==end)
			return new int[] {start};
		
		int n = (end - start)/step+1;
		
		//if (n*step<=0) return null;
		
		int [] x = new int[n];
		x[0] = start;
		for (int i=1; i<n; i++)
			x[i] = x[i-1]+step;
		return x;
	}
	
	public static int [] colon(int start, int end) {
		return colon(start, end, 1);
	}

	/**
	 * Count the number of zero-crossing, i.e. the number of sign-change
	 * @param x
	 * @return
	 */
	public static int zerocrossingcount(double [] x) {

		int c = 0;
		for (int i=0; i<x.length-1; i++)
			if (x[i+1]*x[i]<0)
				c++;

		return c;
	}

	/**
	 * Return a double array in reverse order.
	 * Return the result in a different array
	 * @param x
	 * @return
	 */
	public static double [] flip(double [] x) {
		int L = x.length;
		double []y = new double [L];
		for (int i=0; i<L; i++)
			y[i] = x[L-1-i];

		return y;
	}



	// ======================== OPERATIONS  ==========================
	public static int mod(int a, int m) {
		return (m==0) ? a : a - m * (int)Math.floor((double)a/(double)m);
	}
	public static double rem(int a, int b) {
		return (b==0) ? Double.NaN : a - b * (int)fix((double)a/(double)b);
	}
	public static double fix(double x) {
		if (x>=0)
			return Math.floor(x);
		else
			return Math.ceil(x);
	}


	/**
	 * Compute the sum of an array of booleans, i.e. count the "true" elements
	 * @param samples
	 * @return
	 */
	public static int sum(boolean[] samples) {
		int s=0;
		for (int i=0; i<samples.length; i++)
			if (samples[i])
				s++;
		return s;
	}
	/**
	 * Compute the sum of an array of double
	 * @param samples
	 * @return
	 * @throws Exception 
	 */
	public static double sum(double[] samples) throws Exception {
		
		if (samples==null)
			throw new Exception("llegal arguments in sum");
		
		return sum(samples, 0, samples.length-1, 1);
	}
	
	/**
	 * Sum of elements specified by indexes
	 * @param samples
	 * @param idx
	 * @return
	 * @throws Exception
	 */
	public static double sum(double[] samples, int [] idx ) throws Exception {

		double s=0;
		for (int i=0; i<idx.length; i++)
			s+=samples[idx[i]];

		return s;
	}

	public static double sum(double[] samples, int start, int end, int step) throws Exception {
		
		if ( (samples==null) || (start<0) || (end<0) || ((start!=end) && (step==0)) || ((start<end) && (step<0)) || ((start>end) && (step>0)))
			throw new Exception("llegal arguments in sum");
		
		double s=0;
		if (step>0)
			for (int i=start; i<=end; i+=step)
				s+=samples[i];
		else
			for (int i=start; i>=end; i+=step)
				s+=samples[i];
		
		return s;
	}
	/**
	 * Compute the sum of an array of double, ignoring NaN values
	 * @param samples
	 * @return
	 */
	public static double nansum(double[] samples) {
		double s=0;
		for (int i=0; i<samples.length; i++)
			if (samples[i]!= Double.NaN)
				s+=samples[i];
		return s;
	}
	/**
	 * Compute the sum of an array of float
	 * @param samples
	 * @return
	 */
	public static double sum(float[] samples) {
		float s=0;
		for (int i=0; i<samples.length; i++)
			s+=samples[i];
		return s;
	}

	/**
	 * Compute the average value of an array of double
	 * @param samples
	 * @return
	 * @throws Exception 
	 */
	public static double mean(double[] samples) throws Exception {
		return sum(samples)/samples.length;
	}
	public static double mean(double[] samples, int []idx) throws Exception {
		return sum(samples, idx)/idx.length;
	}

	/**
	 * Compute the average value of an array of float
	 * @param samples
	 * @return
	 */
	public static double mean(float [] samples) {
		return sum(samples)/samples.length;
	}

	/**
	 * Compute standard deviation, given the mean (as shortcut)
	 * @param samples
	 * @param mean
	 * @return
	 */
	public static double stdev(double [] samples, double mean) {
		int n = samples.length;
		double acc = 0.0;
		for (int i =0; i<n; i++) {
			double d = samples[i]-mean;
			acc += d*d;
		}
		acc = acc/(n-1);
		return Math.sqrt(acc);
	}



	/**
	 * Subtract the given double value from an array of float.
	 * Save the result in the original array
	 * @param samples
	 * @param d
	 */
	public static void minusSelf(float[] samples, double d) {
		for (int i=0; i<samples.length; i++)
			samples[i]-=d;
	}
	public static void minusSelf(int[] samples, int d) {
		for (int i=0; i<samples.length; i++)
			samples[i]-=d;
	}
	/**
	 * Subtract the given double value from an array of double.
	 * Save the result in the original array
	 * @param samples
	 * @param d
	 */
	public static void minusSelf(double[] samples, double d) {
		for (int i=0; i<samples.length; i++)
			samples[i]-=d;
	}
	
	public static float [] minus(float [] x, double d) {
		float [] y = x.clone();
		minusSelf(y, d);
		return y;
	}
	public static int [] minus(int [] x, int d) {
		int [] y = new int[x.length];
		for (int i=0; i<y.length; i++)
			y[i] = x[i] - d;
		return y;
	}
	public static double [] minus(double [] x, double d) {
		double [] y = x.clone();
		minusSelf(y, d);
		return y;
	}
	public static double [] minus(double [] x, double [] y) throws Exception {
		if (x.length!= y.length)
			throw new Exception("Length mismatch");
		double [] z = new double[x.length];
		for (int i=0; i<x.length; i++)
			z[i] = x[i] - y[i];
		return z;
	}
	public static double [] add(double [] x, double k) {
		double [] y = new double[x.length];
		for (int i=0; i<x.length; i++)
			y[i] = x[i]+k;
		return y;
	}
	public static double [] add(double [] x, double []y) throws Exception {
		if (x.length!=y.length)
			throw new Exception ("Length mismatch");
		
		int n = x.length;
		double [] z = new double[n];
		for (int i=0; i<n; i++)
			z[i] = x[i]+y[i];
		return z;
	}
	public static int [] add(int [] x, int k) {
		int [] y = new int[x.length];
		for (int i=0; i<x.length; i++)
			y[i] = x[i]+k;
		return y;
	}
	

	/**
	 * Compute the mean of an array of float, then subtract it from the array.
	 * Save the result in the original array
	 * @param samples
	 */
	public static void subtractMeanSelf(float[] samples) {
		double m = mean(samples);
		minusSelf(samples, m);
	}
	/**
	 * Compute the mean of an array of double, then subtract it from the array.
	 * Save the result in the original array
	 * @param samples
	 * @throws Exception 
	 */
	public static void subtractMeanSelf(double[] samples) throws Exception {
		double m = mean(samples);
		minusSelf(samples, m);
	}

	/**
	 * Multiply every elements of an array of float with the given double k.
	 * Return the results in a different array
	 * @param x
	 * @param k
	 */
	public static double [] times (double [] x, double k) {
		double [] y = new double [x.length];
		for (int i=0; i<y.length; i++)
			y[i] = x[i] * k;

		return y;
	}
	public static double [] times (int [] x, double k) {
		double [] y = new double [x.length];
		for (int i=0; i<y.length; i++)
			y[i] = (double)x[i] * k;

		return y;
	}
	
	/**
	 * Multiply array x and y element by element
	 * Return the results in a different array
	 * @param x
	 * @param y
	 * @throws Exception 
	 */
	public static double [] arrayTimes (double [] x, double [] y) throws Exception {
		
		if (y.length==1)
			return times(x, y[0]);
		
		if (x.length!= y.length)
			throw new Exception("Length mismatch");
		
		double [] z = new double [x.length];
		for (int i=0; i<x.length; i++)
			z[i] = x[i] * y[i];

		return z;
	}
	public static double [] arrayTimes (float [] x, float [] y) throws Exception {
		if (x.length!= y.length)
			throw new Exception("Length mismatch");
		
		double [] z = new double [x.length];
		for (int i=0; i<x.length; i++)
			z[i] = (double)x[i] * (double)y[i];

		return z;
	}

	/**
	 * Multiply every elements of an array of float with the given double k.
	 * Save the result in the original array.
	 * @param x
	 * @param k
	 */
	public static void timesSelf (float [] x, double k) {
		for (int i=0; i<x.length; i++)
			x[i] *=k;
	}
	public static void timesSelf (double [] x, double k) {
		for (int i=0; i<x.length; i++)
			x[i] *=k;
	}
	/**
	 * Multiply every elements of a bidimensional array of float with the given double k.
	 * Save the result in the original array.
	 * @param X
	 * @param k
	 */
	public static void timesSelf (float [][] X, double k) {
		for (int n=0; n<X.length; n++)
			timesSelf(X[n], k);
	}




	/**
	 * Compute the roots of the polynomial represented by p
	 * It relies on the estimation of eigenvalues of the companion matrix
	 * @param u is a vector containing n+1 polynomial coefficients, starting with the coefficients of x^n
	 * @return a double[][]: each row is a complex number: [0] is the real part, [1] is the imaginary part
	 */
	public static double [][] roots(double[] u) {

		int numberOfCoeff = u.length;
		int degree = numberOfCoeff-1;

		double [][] C = MyMatrix.compan(u).getArray();
		Matrix M = new Matrix(C);
		EigenvalueDecomposition E = new EigenvalueDecomposition(M);
		Matrix D = E.getD();
		double [][] dd = D.getArray();
		double [][] roots = new double [degree][2];
		int n = D.getRowDimension();
		int r = 0;
		int i = 0; // The current root
		while (r<n)
		{
			// at row r, the element dd[r][r] may be a real eigenvalue, if r is the last row, or the right element is zero (1x1block)
			// otherwise, the element dd[r][r] is the real part of a complex eignevalue, which is stored in a 2x2block...
			boolean realRoot = (r==n-1) || (dd[r][r+1]==0);

			if (realRoot)
			{
				roots[i][0] = dd[r][r];
				roots[i][1] = 0;
				i++; // advance the roots index
				r++; //advance the index of the matrix))
			}
			else
			{
				// The first complex
				roots[i][0] = dd[r][r];
				roots[i][1] = dd[r][r+1];
				i++;
				// and it conjugate
				roots[i][0] = dd[r][r];
				roots[i][1] = -dd[r][r+1];
				i++;
				r+=2;
			}
		}

		return roots;

	}

	/**
	 * Clone a bidimensional array of float
	 * @param x
	 * @return
	 */
	public static float [][] clone (float [][]x) {
		float [][] y = new float[x.length][];
		for (int r=0; r<y.length; r++)
			y[r] = x[r].clone();
		return y;
	}
	/**
	 * Clone a bidimensional array of double
	 * @param x
	 * @return
	 */
	public static double [][] clone (double [][]x) {
		double [][] y = new double[x.length][];
		for (int r=0; r<y.length; r++)
			y[r] = x[r].clone();
		return y;
	}


	// ================= MAX and MIN ===========================
	/**
	 * Compute the max value of an array of double.
	 * @param v
	 * @return a 2-elements array of double: .[0] = the max value; .[1] its index
	 */
	public static double [] maximax(double [] v) {

		double m = v[0];
		double imax = 0;
		for (int i=1; i<v.length; i++)
			if (v[i]>m)
			{
				m = v[i];
				imax = i;
			}

		double [] res = new double[2];
		res[0] = m;
		res[1] = (double)imax;
		return res;
	}
	public static int imax(double []v) {
		double [] temp = maximax(v);
		return (int)temp[1];
	}
	
	public static int [] maximax(int [] v) {

		int m = v[0];
		int imax = 0;
		for (int i=1; i<v.length; i++)
			if (v[i]>m)
			{
				m = v[i];
				imax = i;
			}

		int [] res = new int[2];
		res[0] = m;
		res[1] = imax;
		return res;
	}

	/**
	 * Return the max value of an array of double
	 * @param v
	 * @return
	 */
	public static double max(double [] v) {
		double m = v[0];
		for (int i=1; i<v.length; i++)
			m = (v[i]>m)? v[i] : m;
			return m;
	}

	/**
	 * Return the max value of an array of int
	 * @param v
	 * @return
	 */
	public static int max(int [] v) {
		int m = v[0];
		for (int i=1; i<v.length; i++)
			m = (v[i]>m)? v[i] : m;
			return m;
	}

	/**
	 * Return the min value of an array of int
	 * @param v
	 * @return
	 */
	public static double min(double [] v) {
		return min(v, 0, v.length-1);
	}
	/**
	 * Return the min value of an array of int, between the start-th and end-th samples
	 * @param v
	 * @return
	 */
	public static double min(double [] v, int start, int end) {
		double m = v[start];
		for (int i=start+1; i<=end; i++)
			m = (v[i]<m)? v[i] : m;
			return m;
	}


	public static double [] minimin(double [] v) {

		return minimin(v, 0, v.length-1);
	}
	public static double [] minimin(double [] v, int start, int end) {

		double m = v[start];
		double imin = start;
		for (int i=1; i<=end; i++)
			if (v[i]<m)
			{
				m = v[i];
				imin = i;
			}

		double [] res = new double[2];
		res[0] = m;
		res[1] = (double)imin;
		return res;
	}
	
	/**
	 * Compute minimum value and its row index for each column
	 * @param V
	 * @return a double[][] where [0] provides minimim values, and [1] their indexes
	 */
	public static double [][] minimin_1(double [][] V) {
		int m = V.length;
		int n = V[0].length;
		
		double [] mi_v = new double[n];
		double [] im_v = new double[n];
		for (int c=0; c<n; c++)
		{
			double mi = V[0][c];
			double im = 0;
			for (int r=1; r<m; r++)
				if (V[r][c] < mi) {
					mi = V[r][c];
					im = r;
				}
			
			mi_v[c] = mi;
			im_v[c] = im;
		}
		return new double [][] {mi_v, im_v};		
	}
	
	// =================== RANDOM ========================
	/**
	 * 
	 * @param p
	 * @return
	 */
	public static double [] uniformRandomVector(int p) {

		Random r = new Random();
		double [] d = new double[p];

		for (int i=0; i<p; i++)
			d[i] = r.nextDouble();

		return d;
	}





	/***************************************************
	 * WINDOWING
	 * *************************************************
	 */

	/**
	 * 
	 * @param x
	 * @return
	 */
	public static double [] applyHammingWindow(double [] x) {

		int length = x.length;
		double [] y = new double[length];

		for (int i =0; i<length; i++)
			y[i] = x[i] * ( 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (length - 1)) );

		return y;
	}
	
	/**
	 * Return a hamming window of give size
	 * @param length
	 * @return
	 */
	public static double [] hamming(int length) {
		
		double [] w = new double[length];
		
		for (int i =0; i<length; i++)
			w[i] =  0.54 - 0.46 * Math.cos(2 * Math.PI * i / (length - 1));
		
		return w;
	}
	
	/**
	 * Symmetric Hanning window construction
	 * @param n
	 * @return
	 * @throws Exception 
	 */
	public static double [] hanning(int n) throws Exception {
		
		
		double [] w = new double[n];
		if (rem(n, 2)==0)
		{
			int half = n/2;
			int j=0;
			for (int i=1; i<=half; i++) // w = .5*(1 - cos(2*pi*(1:m)'/(n+1)));
				w[j++] = 0.5 * (1.0 - Math.cos(2.0*Math.PI*i/(double)(n+1)));
			for (int i=half-1; i>=0; i--)
				w[j++] = w[i];
		}
		else
		{
			int half = (n+1)/2;
			int j=0;
			for (int i=1; i<=half; i++) // w = .5*(1 - cos(2*pi*(1:m)'/(n+1)));
				w[j++] = 0.5 * (1.0 - Math.cos(2.0*Math.PI*i/(double)(n+1)));
			for (int i=half-2; i>=0; i--)
				w[j++] = w[i];
		}
		
		return w;		
	}
	
	/**
	 * SINC(X) returns a matrix whose elements are the sinc of the elements 
%   of X, i.e.
%        y = sin(pi*x)/(pi*x)    if x ~= 0
%          = 1                   if x == 0
%   where x is an element of the input matrix and y is the resultant
%   output element.
	 * @param x
	 * @return
	 */
	public static double sinc(double x) {
		return (x==0.0) ? 1.0 : Math.sin(Math.PI*x)/(Math.PI*x);
	}
	public static double [] sinc(double []x) {
		double [] y = new double[x.length];
		for (int i=0; i<y.length; i++)
			y[i] = sinc(x[i]);
		return y;
	}

	/***************************************************
	 * UNIDIMENSIONAL ARRAYS COMPARISON
	 * *************************************************
	 */
	
	public static double compare_vectors(String msg, double [] aJava, double [] bMatlab) {
		return compare_vectors(msg, aJava, bMatlab, 1E-6);
	}
	/**
	 * 
	 * @param msg
	 * @param aJava
	 * @param bMatlab
	 * @param th
	 * @return
	 */
	public static double compare_vectors(String msg, double [] aJava, double [] bMatlab, double th) {

		if(aJava.length !=bMatlab.length)
		{
			System.out.println("Vectors have different size");
			return Double.NaN;
		}
		double [] diff    = new double[aJava.length];
		double [] diffrel = new double[aJava.length];
		double maxDiff    = 0.0;
		double maxDiffRel = 0.0;
		int idxMaxDiff    = -1;
		int idxMaxDiffRel = -1;

		for (int i=0; i<aJava.length; i++)
		{
			diff[i] = Math.abs(aJava[i] - bMatlab[i]);
			
			if (diff[i]<th)
				continue;
			
			//diffrel[i] = Math.abs(diff[i]/(aJava[i]+1E-10));
			diffrel[i] = Math.abs(diff[i]/(aJava[i]+Double.MIN_VALUE));

			if (diff[i]>maxDiff) {
				maxDiff = diff[i];
				idxMaxDiff = i;
			}
			
			if (!Double.isInfinite(diffrel[i]) && !Double.isNaN(diffrel[i])) //if (Double.isFinite(diffrel[i]))
			{
				if (diffrel[i]>maxDiffRel)
				{
					maxDiffRel = diffrel[i];
					idxMaxDiffRel = i;
				}
			}
			else
			{
				System.out.println("\tINFINITE relative difference (@ idx: " +  i + ") : " + aJava[i] + " vs " + bMatlab[i]);
			}
			
		}
		double normInf = MyMatrix.fromRowVector(diffrel).normInf();
		System.out.println(msg);
		if (idxMaxDiff>=0)
		{
			System.out.println("\tmax diff = " + maxDiff + " (@ idx: " +  idxMaxDiff + ") : " + aJava[idxMaxDiff] + " vs " + bMatlab[idxMaxDiff]);
		}
		else
			System.out.println("\tmax diff = " + maxDiff);
		
		if (idxMaxDiffRel>=0) {
			System.out.println("\tmax FINITE relative difference = max (e) = " + maxDiffRel + " (@ idx: " +  idxMaxDiffRel + ") : " + aJava[idxMaxDiffRel] + " vs " + bMatlab[idxMaxDiffRel]);
			System.out.println("\tnormInf (e) = " + normInf);
		}
		else
			System.out.println("\tmax FINITE relative difference = " + maxDiffRel);
		
		
		return normInf;

	}

	public static double compare_vectors(String msg, float [] aJava, double [] bMatlab) {
		if (aJava.length!=bMatlab.length) {
			System.out.println("Different size");
			return Double.POSITIVE_INFINITY;
		}
			
		double [] daJava = new double[aJava.length];
		for (int i=0; i<daJava.length; i++)
			daJava[i] = aJava[i];

		return compare_vectors(msg, daJava, bMatlab);
	}


	/**
	 * 
	 * @param msg
	 * @param aJava
	 * @param bMatlab
	 * @return
	 */
	public static double compare_vectors(String msg, int [] aJava, int [] bMatlab) {

		assert(aJava.length==bMatlab.length);
		int n = aJava.length;
		double [] a = new double [n];
		double [] b = new double [n];
		for (int i = 0; i<n; i++) {
			a[i] = (double)aJava[i];
			b[i] = (double)bMatlab[i];
		}
		return compare_vectors(msg, a, b);
	}


	/**
	 * Compare two unidimensional arrays. Elements can be double or integer
	 * @param aJava
	 * @param bMatlab
	 * @return
	 */
	public static double compare_vectors(String msg, int [] aJava, double [] bMatlab) {

		assert(aJava.length==bMatlab.length);
		int n = aJava.length;
		double [] a = new double [n];
		for (int i = 0; i<n; i++) {
			a[i] = (double)aJava[i];
		}
		return compare_vectors(msg, a, bMatlab);
	}

	public static double compare_vectors(String msg, short [] aJava, short [] bMatlab) {

		assert(aJava.length==bMatlab.length);
		int n = aJava.length;
		double [] a = new double [n];
		double [] b = new double [n];
		for (int i = 0; i<n; i++) {
			a[i] = (double)aJava[i];
			b[i] = (double)bMatlab[i];
		}
		return compare_vectors(msg, a, b);
	}


	/***************************************************
	 * BIIDIMENSIONAL ARRAYS COMPARISON, NOT MATRICES
	 * *************************************************
	 */

	public static void compare(String msg, double[][] A, double[][] B, int []nn) {

		// Just assume equal number of rows, and equal number of elements for each row

		int m = A.length;
		if (B.length!=m)
			throw new IllegalArgumentException("The arrays must have the same length");


		//ArrayList<MyMatrix> maximax0 = diff.maximax(0);		
		//double maxdiff = maximax0.get(0).get(0, 0);
		//double rigaOfMax = maximax0.get(1).get(0,0);
		//double colonnaOfMax = maximax0.get(2).get(0,0);

		double th=1E-7;

		//int m = diff.getRowDimension();
		//int n = diff.getColumnDimension();
		//MyMatrix diffrelM = new MyMatrix(m, n);
		double [][]diff = new double[m][];
		double [][]diffrel = new double[m][];

		double maxdiff = -1;
		double maxDiffRel = -1;
		int rigaOfMaxRel = 0;
		int colonnaOfMaxRel = 0;
		double amax = 0;
		double bmax = 0;

		for (int r=0; r<m; r++)
		{
			int n = nn[r];

			diff[r] = new double[n];
			double [] d = diff[r];

			diffrel[r] = new double[n];
			double [] dr = diffrel[r];

			double [] a = A[r];
			double [] b = B[r];

			//int n = a.length;
			//if (b.length!=n)
			//	throw new IllegalArgumentException("The arrays must have the same number of elements");
			for (int j=0; j<n; j++)
			{
				d[j] = Math.abs(a[j] - b[j]);

				if (d[j]>maxdiff)
					maxdiff = d[j];

				dr[j] = Math.abs(d[j]/a[j]);

				if (a[j]==0.0) {
					System.out.println("("+r+","+j+"): A=" + a[j]+ " B=" + b[j]+ " diff="+d[j]+" den="+a[j]+ " drel="+dr[j]);
				}
				else
				{
					if (dr[j] > maxDiffRel) {
						maxDiffRel = dr[j];
						rigaOfMaxRel = r;
						colonnaOfMaxRel = j;
						amax = a[j];
						bmax = b[j];
					}

				}
			}
		}


		//ArrayList<MyMatrix> maximax0Rel = diffrelM.maximax(0);		
		//double maxDiffRel = maximax0Rel.get(0).get(0, 0);
		//int rigaOfMaxRel = (int)maximax0Rel.get(1).get(0,0);
		//int colonnaOfMaxRel = (int)maximax0Rel.get(2).get(0,0);


		//double normInf = diffrelM.normInf();
		System.out.println(msg);
		System.out.println("\t max diff: " + maxdiff);
		System.out.println("\t max diff rel: " + maxDiffRel + " at (" +  rigaOfMaxRel + ", "+ colonnaOfMaxRel + ") ==> elements were: for A : " + amax + " ; and for B: " + bmax);
		//System.out.println("\t normInf(diffRel) = " + normInf);
		//return normInf;

	}
	
	public static void compare(String msg, double[][] A, double[][] B) {
		compare(msg, A, B, 1E-7);
	}
	public static void compare(String msg, double[][] A, double[][] B, double th) {

		// Just assume equal number of rows, and equal number of elements for each row

		int m = A.length;
		if (B.length!=m)
			throw new IllegalArgumentException("The arrays must have the same length");


		//ArrayList<MyMatrix> maximax0 = diff.maximax(0);		
		//double maxdiff = maximax0.get(0).get(0, 0);
		//double rigaOfMax = maximax0.get(1).get(0,0);
		//double colonnaOfMax = maximax0.get(2).get(0,0);

		

		//int m = diff.getRowDimension();
		//int n = diff.getColumnDimension();
		//MyMatrix diffrelM = new MyMatrix(m, n);
		double [][]diff = new double[m][];
		double [][]diffrel = new double[m][];

		double maxdiff = -1;
		double maxDiffRel = -1;
		int rigaOfMaxRel = 0;
		int colonnaOfMaxRel = 0;
		double amax = 0;
		double bmax = 0;

		for (int r=0; r<m; r++)
		{
			int n = A[r].length;

			diff[r] = new double[n];
			double [] d = diff[r];

			diffrel[r] = new double[n];
			double [] dr = diffrel[r];

			double [] a = A[r];
			double [] b = B[r];

			//int n = a.length;
			//if (b.length!=n)
			//	throw new IllegalArgumentException("The arrays must have the same number of elements");
			for (int j=0; j<n; j++)
			{
				d[j] = Math.abs(a[j] - b[j]);
				
				if (d[j]<th)
					continue;

				if (d[j]>maxdiff)
					maxdiff = d[j];

				dr[j] = Math.abs(d[j]/a[j]);

				if (a[j]==0.0) {
				//	System.out.println("("+r+","+j+"): A=" + a[j]+ " B=" + b[j]+ " diff="+d[j]+" den="+a[j]+ " drel="+dr[j]);
				}
				else
				{
					if (dr[j] > maxDiffRel) {
						maxDiffRel = dr[j];
						rigaOfMaxRel = r;
						colonnaOfMaxRel = j;
						amax = a[j];
						bmax = b[j];
					}

				}
			}
		}


		//ArrayList<MyMatrix> maximax0Rel = diffrelM.maximax(0);		
		//double maxDiffRel = maximax0Rel.get(0).get(0, 0);
		//int rigaOfMaxRel = (int)maximax0Rel.get(1).get(0,0);
		//int colonnaOfMaxRel = (int)maximax0Rel.get(2).get(0,0);


		//double normInf = diffrelM.normInf();
		System.out.println(msg);
		System.out.println("\t max diff: " + maxdiff);
		System.out.println("\t max diff rel: " + maxDiffRel + " at (" +  rigaOfMaxRel + ", "+ colonnaOfMaxRel + ") ==> elements were: for A : " + amax + " ; and for B: " + bmax);
		//System.out.println("\t normInf(diffRel) = " + normInf);
		//return normInf;

	}
	
	/**
	 * Search for x inside an array of integer
	 * @param vec
	 * @param x
	 * @return the index of x inside the array if x was found, -1 otherwise
	 */
	public static int contain(int [] vec, int x) {
		int p = -1;
		for (int i=0; i<vec.length; i++)
			if (vec[i]==x) {
				p=i;
				break;
			}
		return p;
	}
	
	public static int [] remove(int [] in, int index) throws Exception
	{
		if (index>=in.length)
			throw new Exception("index error");
		
		int [] out = new int[in.length-1];
		int i=0;
		for (int j=0; j<in.length; j++)
			if (j != index)
				out[i++] = in[j];
		return out;
	}

	/***************************************************
	 * ELEMENTS SELECTION
	 * *************************************************
	 */

	public static double [] select(double [] IN, boolean [] ids) {
		int s = sum(ids);
		double [] OUT = new double[s];
		int j=0;
		for (int i=0; i<IN.length; i++)
			if (ids[i])
				OUT[j++] = IN[i];
		return OUT;
	}
	public static boolean [] select(boolean [] IN, boolean [] ids) {
		int s = sum(ids);
		boolean [] OUT = new boolean[s];
		int j=0;
		for (int i=0; i<IN.length; i++)
			if (ids[i])
				OUT[j++] = IN[i];
		return OUT;
	}
	public static int [] select(int [] IN, boolean [] ids) {
		int s = sum(ids);
		int [] OUT = new int[s];
		int j=0;
		for (int i=0; i<IN.length; i++)
			if (ids[i])
				OUT[j++] = IN[i];
		return OUT;
	}
	public static boolean [] select(boolean [] IN, int [] ids) {

		int totkeep = ids.length; 
		boolean [] OUT = new boolean[totkeep];
		for (int j=0; j<totkeep; j++)
			OUT[j] = IN[ids[j]];
		return OUT;
	}

	public static int [] select(int [] IN, int [] ids) {

		int totkeep = ids.length; 
		int [] OUT = new int[totkeep];
		for (int j=0; j<totkeep; j++)
			OUT[j] = IN[ids[j]];
		return OUT;
	}

	public static double [] select(double [] IN, int [] ids) {
		
		int totkeep = ids.length; 
		double [] OUT = new double[totkeep];
		for (int j=0; j<totkeep; j++)
			OUT[j] = IN[ids[j]];
		return OUT;
	}
	public static float [] select(float [] IN, int [] ids) {
		
		int totkeep = ids.length; 
		float [] OUT = new float[totkeep];
		for (int j=0; j<totkeep; j++)
			OUT[j] = IN[ids[j]];
		return OUT;
	}
	static public float [][] select(float [][] faIN, int [] ids) {

		int totkeep = ids.length; 
		float [][] faOut = new float[totkeep][];
		for (int j=0; j<totkeep; j++)
			faOut[j] = faIN[ids[j]];
		return faOut;
	}
	
	static public double [] select(double [] IN, int from, int to, int step) {
		int []ids = colon(from, to, step);
		if (ids==null)
			return null;
		return select(IN, ids);
	}
	static public float [] select(float [] IN, int from, int to, int step) {
		int []ids = colon(from, to, step);
		if (ids==null)
			return null;
		return select(IN, ids);
	}
	static public double [] select(double [] IN, int from, int to) {
		int []ids = colon(from, to);
		if (ids==null)
			return null;
		return select(IN, ids);
	}
	static public float [] select(float [] IN, int from, int to) {
		int []ids = colon(from, to);
		if (ids==null)
			return null;
		return select(IN, ids);
	}
	
	// Element selection with bidimensional indexes
	static public double [][] select(double [] IN, int[][] IDS) {
		double [][] OUT = new double[IDS.length][];
		for (int i=0; i<OUT.length; i++)
			OUT[i] = select(IN, IDS[i]);
		return OUT;
	}
	
	/**
	 * 
	 * @param faIN
	 * @param keep
	 * @return
	 * @throws Exception
	 */
	static public float [][] select(float [][] faIN, boolean [] keep) throws Exception {

		if (faIN.length != keep.length)
			throw new Exception("Mismatch");

		int [] found = find(keep);

		return select(faIN, found);
	}

	/**
	 * Find the indexes of "true" elements
	 * @param x
	 * @return
	 * @throws Exception 
	 */
	static public int [] find(boolean [] x) throws Exception {

		int n = sum(x);
		
		if (n==0)
			return new int [0];
		
		return find(x, n);
	}
	/**
	 * 
	 * @param x
	 * @param from
	 * @param to
	 * @return
	 * @throws Exception
	 */
	static public int [] find(boolean [] x, int from, int to) throws Exception {

		int n = sum(x);
		
		if (n==0)
			return new int [0];
		
		return find(x, n, from, to);
	}
	/**
	 * 
	 * @param x
	 * @param n
	 * @return
	 * @throws Exception
	 */
	static public int [] find(boolean [] x, int n) throws Exception {

	  return find(x, n, 0, x.length-1);
	}
	/**
	 * 
	 * @param x
	 * @param n
	 * @param from
	 * @param to
	 * @return
	 * @throws Exception
	 */
	static public int [] find(boolean [] x, int n, int from, int to) throws Exception {

		if (n<=0)
			throw new Exception("Second argument must be a positive scalar integer");
		
		int [] found = new int[n];

		int count = 0;
		for (int i=from; (i<=to) && (count<n); i++)
			if (x[i])
				found[count++] = i;
		
		if (count<n)
		{
			int [] found2 = new int[count];
			
			if (count>0)			
				System.arraycopy(found, 0, found2, 0, count);
			
			return found2;
		}
		
		return found;
	}

	


	/***************************************************
	 * SIMPLE OUTLIERS REMOVAL
	 * *
	 * @throws Exception ************************************************
	 */
	public static boolean [] stdevThreshold(double [] x, double r) throws Exception {

		double mu = mean(x);
		double stdev = stdev(x, mu);
		double stdevTh = r*stdev;
		boolean [] keep = new boolean [x.length];

		for (int i = 0; i<x.length; i++)
			keep[i] = !(Math.abs(x[i] - mu)>stdevTh);

		return keep;
	}


	// For test and debug purpose
	public static void main(String[]args) {

		double [] u = new double [] {10, 6, 7, 8};
		System.out.print("Polynomial coefficients: [");
		for (int i =0; i<u.length-1; i++)
			System.out.print(u[i] + "; ");
		System.out.println(u[u.length-1] + " ]");


		double [][] co = MyMatrix.compan(u).getArray();
		System.out.println("\nCompanion Matrix:");
		new Matrix(co).print(5,  5);

		double [][] roots = roots(u);
		for (int i = 0; i<roots.length; i++)
			System.out.println( "(" + roots[i][0] + " ; " + roots[i][1] + ")");

		double [][] T = MyMatrix.toeplitzReal(u).getArray();
		System.out.println("Toeplitz matrix:");
		new Matrix(T).print(5,  5);

		// Test cloning bidimensional array of double
		//double [][] Tclone = T.clone(); //DOES NOT WORK!!!
		double [][] Tclone = clone(T);
		for (int r=0; r<Tclone.length; r++)
			for (int c=0; c<Tclone[r].length;c++)
				Tclone[r][c]  *=10;

		System.out.println("Cloned then changed Toeplitz matrix:");
		new Matrix(Tclone).print(5,  5);

		System.out.println("Original Toeplitz matrix:");
		new Matrix(T).print(5,  5);
	}
	
	/***************************************************
	 * FORMAT CONVERSION
	 * *************************************************
	 */
	public static double [] todouble(boolean []x) {
		double [] y = new double [x.length];
		for (int i=0; i<y.length; i++)
			y[i] = (x[i])? 1.0 : 0.0;
		return y;
	}
	public static double [][] todouble(float[][]x) {
		double [][] y = new double [x.length][];
		for (int i=0; i<y.length; i++)
			y[i] = todouble(x[i]);
		return y;
	}
	public static double [] todouble(int []x) {
		double [] y = new double [x.length];
		for (int i=0; i<y.length; i++)
			y[i] = x[i];
		return y;
	}
	public static int [] toint(double []x) {
		int [] y = new int [x.length];
		for (int i=0; i<y.length; i++)
			y[i] = (int)x[i];
		return y;
	}
	public static double [] todouble(ArrayList<Double> x) {
		double [] y = new double [x.size()];
		for (int i=0; i<y.length; i++)
			y[i] = x.get(i);
		return y;
	}
	public static double [] todouble(float [] x) {
		double [] y = new double [x.length];
		for (int i=0; i<x.length; i++)
			y[i] = x[i];
		return y;
	}
	public static float [] tofloat(double [] x) {
		float [] y = new float [x.length];
		for (int i=0; i<x.length; i++)
			y[i] = (float)x[i];
		return y;
	}
	
	// Concatenation
	public static float [] cat(float[]... f3) {
		int numArray = f3.length;
		int finalDim =0;
		for (int i=0; i<numArray; i++)
			finalDim+=f3[i].length;
		
		float [] ret = new float[finalDim];
		int j=0;
		for (int i=0; i<numArray; i++)
		{
			System.arraycopy(f3[i], 0, ret, j, f3[i].length);
			j+=f3[i].length;
		}
		return ret;		
	}
	public static double [] cat(double[]... f3) {
		int numArray = f3.length;
		int finalDim =0;
		for (int i=0; i<numArray; i++)
			if (f3[i]!=null)
				finalDim+=f3[i].length;
		
		double [] ret = new double[finalDim];
		int j=0;
		for (int i=0; i<numArray; i++)
		{
			if (f3[i] != null) {
				System.arraycopy(f3[i], 0, ret, j, f3[i].length);
				j+=f3[i].length;
			}
		}
		return ret;		
	}
	public static int [] cat(int[]... f3) {
		int numArray = f3.length;
		int finalDim =0;
		for (int i=0; i<numArray; i++)
			finalDim+=f3[i].length;
		
		int [] ret = new int[finalDim];
		int j=0;
		for (int i=0; i<numArray; i++)
		{
			System.arraycopy(f3[i], 0, ret, j, f3[i].length);
			j+=f3[i].length;
		}
		return ret;		
	}
	
	/***************************************************
	 * COMPARISON
	 * *************************************************
	 */
	
	/**
	 * Element-wise comparison of an array of double with a threshold
	 * @param x
	 * @param th
	 * @return: an array of boolean, where i-th element is equal to the boolean expression (x[i] &lt; th)
	 */
	public static boolean [] lt(double [] x, double th) {
		boolean [] b = new boolean[x.length];
		for (int i=0; i<b.length; i++)
			b[i] = x[i]<th;
		return b;
	}
	
	public static boolean [] gte(double [] x, double th) {
		boolean [] b = new boolean[x.length];
		for (int i=0; i<b.length; i++)
			b[i] = x[i]>=th;
		return b;
	}
}
