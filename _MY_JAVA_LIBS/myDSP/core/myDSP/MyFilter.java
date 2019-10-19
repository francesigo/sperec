package myDSP;

import java.math.BigDecimal;
import java.util.Arrays;

import myMath.MyMath;

/**
 * Implements Matlab filter. Inspired to: https://github.com/airloaf/Matlab-Filter-Function-In-Java
 * Originally, it used BigDecimal. I want to replace BigDecimal with Double or (better) deouble. 
 * @author FS
 *
 */
public class MyFilter {
	
	private int PRECISION = 10;
	private double [] a;
	private double [] b;
	private double [] zf; // the final conditions of the filter delays
	private double [] y; // the filtered signal
	
	public double [] geta() {return a;}
	public double [] getb() {return b;}
	public double [] gety() {return y;};
	public double [] getzf() {return zf;}
	
	public MyFilter clone() {
		MyFilter f = new MyFilter();
		f.a = a.clone();
		f.b = b.clone();
		return f;
	}
	
	public MyFilter() {
	}
	
	/**
	 * Filter constructor
	 * @param b coefficients of denominator
	 * @param a coefficients of numerator
	 * @throws Exception 
	 */
	public MyFilter(double [] b, double [] a) throws Exception {
		
		// Don't want to modify the original coefficients
		this.a = a.clone();
		this.b = b.clone();
		
		//Divide b and a by first coefficient of a
		divideEach(this.b, a[0]);
		divideEach(this.a, a[0]);
	}
	
	/**
	 * Shortcut to get the filtered signal in one instruction similar to Matlab implementation
	 * @param b denominator of the filter
	 * @param a numerator of the filter
	 * @param x signal to be filtered
	 * @return the filtered signal
	 * @throws Exception 
	 */
	static public double [] filter(double [] b, double []a, double [] x) throws Exception {
		MyFilter me = new MyFilter(b, a);
		me.filter(x);
		return me.gety();
	}
	static public double [] filter(double [] b, double a, double [] x) throws Exception {
		double [] aa = new double[b.length];
		Arrays.fill(aa, 0.0);
		aa[0] = a;
		MyFilter me = new MyFilter(b, aa);
		me.filter(x);
		return me.gety();
	}
	static public double [] filter(double [] b, double a, float [] x) throws Exception {
		double [] da = MyMath.todouble(x);
		return filter(b, a, da);
	}
	
	
	/**
	 * Filter the sequence X without initial conditions, i.e. zi[i] = 0.0.
	 * Filtered sequence is stored inside the current instance 
	 * @param X the input sequence to be filtered
	 */
	public void filter(double[] X) {
		
		int n = a.length; // not b.length
		
		//Filter delay filled with zeros
		double[] z = new double[n-1];
		Arrays.fill(z,  0.0);
		
		filter_hlp(X, z);
	}
	
	/**
	 * Filter the sequence X using initial conditions z. Do not change zs
	 * @param X the input sequence to be filtered
	 * @param z_ the input initial conditions
	 */
	public void filter(double[] X, double [] z_) {

		double [] z = z_.clone();

		filter_hlp(X, z);
	}
	
	/**
	 * Filter the signal. Put the filtered signal in this.y. Changes z and put it in this.zf
	 * @param X the input sequence
	 * @param z the initial conditions for the filter
	 */
	public void filter_hlp(double [] X, double [] z) {
		
		int n = a.length; // not b.length

		//Filter delay filled with zeros
		if (z.length <n)
		{
			double [] zz = new double[n];
			System.arraycopy(z, 0, zz, 0, z.length);
			Arrays.fill(zz, z.length, n, 0.0);
			z = zz;
		}

		//The filtered signal filled with zeros
		double[] Y = new double[X.length];
		Arrays.fill(Y, 0.0);
		
		for(int m = 0; m < Y.length; m++)
		{
			//Calculates the filtered value using
			Y[m] = b[0] * X[m] + z[0];

			for(int i= 1; i < n; i++) //Previous filter delays recalculated by
				z[i-1] = b[i] * X[m] + z[i] - a[i] * Y[m];
		}

		//Trims the last element off of filter delay
		double[] zC = z.clone();
		z = new double[zC.length-1];
		for(int i = 0; i < z.length; i++)
			z[i] = zC[i];

		//The filtered signal
		this.zf = z; // the final conditions
		this.y = Y;
	}
	
	//Divides
	private void divideEach(double[] array, double divisor) {
		for(int i = 0; i < array.length; i++)
			array[i] = array[i] / divisor;
	}

		
	
	// -------------------------- BIGDECIMAL
	
	public BigDecimal[] filter(BigDecimal[] b, BigDecimal[] a, BigDecimal[] X){

		//Checks if these conditions are met otherwise it
		//will return the original input x
		if(a[0] != BigDecimal.ZERO && (a.length >= b.length)){

			int n = b.length;

			//Filter delay filled with zeros
			BigDecimal[] z = new BigDecimal[n];
			fillZeros(z);

			//The filtered signal filled with zeros
			BigDecimal[] Y = new BigDecimal[X.length];
			fillZeros(Y);

			//Divide b and a by first coefficient of a
			divideEach(b, a[0]);
			divideEach(a, a[0]);

			for(int m = 0; m < Y.length; m++){

				//Calculates the filtered value using
				//Y[m] = b[0] * X[m] + z[0]
				Y[m] = b[0].multiply(X[m]).add(z[0]).setScale(PRECISION, BigDecimal.ROUND_HALF_UP);

				for(int i= 1; i < n; i++){

					//Previous filter delays recalculated by
					//z[i-1] = b[i] * X[m] + z[i] - a[i] * Y[m]
					z[i-1] = b[i].multiply(X[m]).add(z[i]).subtract(a[i].multiply(Y[m])).setScale(PRECISION, BigDecimal.ROUND_HALF_UP);

				}

			}

			//Trims the last element off of filter delay
			BigDecimal[] zC = z.clone();
			z = new BigDecimal[zC.length-1];
			for(int i = 0; i < z.length; i++)
				z[i] = zC[i];

			//The filtered signal
			return Y;
		}

		//Returns original signal when conditions not met
		return X;
	}

	//Divides a BigDecimal array by a big decimal
	private void divideEach(BigDecimal[] array, BigDecimal divisor){
		for(int i = 0; i < array.length; i++){
			array[i] = array[i].divide(divisor).setScale(PRECISION, BigDecimal.ROUND_HALF_UP);
		}
	}

	//Fills a big decimal array with zeros
	private void fillZeros(BigDecimal[] array){
		for(int i = 0; i < array.length; i++){
			array[i] = BigDecimal.ZERO;
		}
	}


}
