package myDSP.test;

import com.mathworks.matlab.types.Complex;

import myDSP.MyComplexArrayDouble;
import myMath.MyMath;

public class TestMyComplexArrayDouble {
	
	public static Complex [] toComplexMatlab (MyComplexArrayDouble X) {
		Complex [] c = new Complex[X.size()];
		for (int i=0; i<X.size(); i++)
			c[i] = new Complex(X.getReal(i), X.getImag(i));
		return c;
	}
	public static void compareToComplexMatlab(MyComplexArrayDouble fft, Complex [] c, double th, String prefix) {
		double []jre = fft.getReal();
		double []jim = fft.getImag();

		if (jre.length != c.length)
		{
			System.out.println(prefix + " the arrays have different size");
			return;
		}
		
		int n=jre.length;
		
		double mre[] = new double[n];
		double mim[] = new double[n];
		for (int i=0; i<n; i++) {
			mre[i] = c[i].real;
			mim[i] = c[i].imag;
		}

		MyMath.compare_vectors(prefix + " Re[FFT] comparison", jre, mre, th);
		MyMath.compare_vectors(prefix + " Im[FFT] comparison", jim, mim, th);
	}

}
