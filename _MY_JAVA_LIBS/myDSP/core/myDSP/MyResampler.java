package myDSP;

import myMath.MyInterp1;
import myMath.MyMath;

public class MyResampler {

	static public float [] exe(float [] data, int p, int q) {
		//return uniformResample(data, p, q);
		double [] dSamples = MyMath.todouble(data);
		double [] dRes = execute_old(dSamples, p, q);
		return MyMath.tofloat(dRes);
	}
	
	static public double [] exe(double [] data, int p, int q) {
		//return uniformResample(data, p, q);
		return execute_old(data, p, q);
	}

	static public double [] uniformResample(double [] data, int p, int q) {
		
		// Here we assume that P and Q have already be reduced as if [p,q]=rat(p/q)
		if ((p==1) && (q==1))
			return data;
			
		int bta = 5;
		int N = 10;
		return uniformResample(data, p, q, N, bta);
		
	}
	// prova
	static public double [] uniformResample(double [] data, int p, int q, int N, int bta) {
		// Here we assume that P and Q have already be reduced as if [p,q]=rat(p/q)
		if ((p==1) && (q==1))
			return data;
		
		int pqmax = Math.max(p,  q);
		
		// Design filter
		double fc;
		int L;
		if (N>0) {
			fc = 1.0 / 2.0 / (double)pqmax;
			L = 2*N*pqmax +1;
			
			// h = firls( L-1, [0 2*fc 2*fc 1], [1 1 0 0]).*kaiser(L,bta)' ;
			//TODO TO BE CONTINUED
		}
		return null;
		
	}
	
	//resamples data such that the data is interpolated by a factor P and then decimated by a factor Q. resample(z,1,Q) results in decimation by a factor Q.
	static public double [] execute_old(double [] data, int P, int Q) {
		
		int iN1 = data.length;
		double dt1 = 1.0/(double)iN1;
		
		double f = (double)P / (double)Q; // if f>1 ==> upsample; if f<1 ==> downsample
		double dN2 = (double)iN1 * f;
		double dt2 = 1.0/dN2;
		
		int iN2 = (int)Math.ceil(dN2);
		double []sourceX = new double [iN1];
		for (int i=0; i<iN1; i++)
			sourceX[i] = i*dt1;
		
		double [] estimateX = new double[iN2];
		for (int i=0; i<iN2; i++)
			estimateX[i] = i*dt2;
		
		estimateX[estimateX.length-1] = sourceX[sourceX.length-1]; // To avoid precision errors
		
		
		double[] y = MyInterp1.interp1(sourceX, data, estimateX);
		return y;
		
	}
}
