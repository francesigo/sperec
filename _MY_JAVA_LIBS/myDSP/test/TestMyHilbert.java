package myDSP.test;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import com.mathworks.engine.EngineException;
import com.mathworks.matlab.types.Complex;

import myDSP.MyComplexArrayDouble;
import myDSP.MyComplexArrayFloat;
import myDSP.MyFraming;
import myDSP.MyFramingSettings;
import myDSP.MyHilbert;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestMyHilbert {


	static MyMatlabConnection matlab;
	
	public static void main(String [] args) throws Exception {
		
		String utteranceFilename = "tri-m3-n5.wav"; //"dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		//MyMath.subtractMeanSelf(u.samples);
	
		matlab = new MyMatlabConnection();
		
		test(matlab, u, 1e-20);

	}
	
	/**
	 * Double precision
	 * @param matlab
	 * @param u
	 * @param th
	 * @throws RejectedExecutionException
	 * @throws EngineException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void test(MyMatlabConnection matlab, Utterance u, double th) throws RejectedExecutionException, EngineException, InterruptedException, ExecutionException {
		
		int n = u.samples.length;
		
		MyComplexArrayDouble c = MyHilbert.get(u.samples);

		Complex [] mc = matlab.eng.feval("hilbert", u.samples, n);
			
		double mre[] = new double[n];
		double mim[] = new double[n];
		double mabs[] = new double[n];
		for (int i=0; i<n; i++) {
			mre[i] = mc[i].real;
			mim[i] = mc[i].imag;
			mabs[i] = Math.sqrt(mc[i].real * mc[i].real + mc[i].imag * mc[i].imag);
		}
		
		double [] jre = c.getReal();
		double [] jim = c.getImag();
		double [] jabs = c.getAbs();
		
		MyMath.compare_vectors("TestMyHilbert: Re[FFT] comparison", jre, mre, th);
		MyMath.compare_vectors("TestMyHilbert: Im[FFT] comparison", jim, mim, th);
		MyMath.compare_vectors("TestMyHilbert: Abs[FFT] comparison", jabs, mabs, th);
	}

	/**
	 * 
	 * @param matlab
	 * @param u
	 * @throws RejectedExecutionException
	 * @throws EngineException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void testFloat(MyMatlabConnection matlab, Utterance u, double th) throws RejectedExecutionException, EngineException, InterruptedException, ExecutionException {
		
		int n = u.samples.length;
		
		MyComplexArrayFloat c = MyHilbert.get_old(u.samples);

		Complex [] mc = matlab.eng.feval("hilbert", u.samples, n);
			
		double mre[] = new double[n];
		double mim[] = new double[n];
		double mabs[] = new double[n];
		for (int i=0; i<n; i++) {
			mre[i] = mc[i].real;
			mim[i] = mc[i].imag;
			mabs[i] = Math.sqrt(mc[i].real * mc[i].real + mc[i].imag * mc[i].imag);
		}
		
		double [] jre = MyMath.todouble(c.getRealFloat());
		double [] jim = MyMath.todouble(c.getImagFloat());
		double [] jabs = MyMath.todouble(c.getAbsFloat());
		
		MyMath.compare_vectors("TestMyHilbert: Re[FFT] comparison", jre, mre, th);
		MyMath.compare_vectors("TestMyHilbert: Im[FFT] comparison", jim, mim, th);
		MyMath.compare_vectors("TestMyHilbert: Abs[FFT] comparison", jabs, mabs, th);
	}
	
}

