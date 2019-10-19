package myDSP.test;

import com.mathworks.matlab.types.Complex;

import myDSP.MyComplexArrayFloat;
import myMath.MyMath;

public class TestMyComplexArray {
	
	public static Complex [] toComplexMatlab (MyComplexArrayFloat X) {
		Complex [] c = new Complex[X.size()];
		for (int i=0; i<X.size(); i++)
			c[i] = new Complex(X.getRealFloat(i), X.getImagFloat(i));
		return c;
	}
	public static void compareToComplexMatlab(MyComplexArrayFloat fft, Complex [] c, String prefix) {
		float []jre = fft.getRealFloat();
		float []jim = fft.getImagFloat();

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

		MyMath.compare_vectors(prefix + " Re[FFT] comparison", jre, mre);
		MyMath.compare_vectors(prefix + " Im[FFT] comparison", jim, mim);
	}

}
