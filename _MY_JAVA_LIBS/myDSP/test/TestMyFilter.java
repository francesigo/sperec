package myDSP.test;

import java.util.Arrays;

import com.mathworks.matlab.types.Struct;

import myDSP.MyComplexArrayFloat;
import myDSP.MyFilter;
import myDSP.MyHilbert;
import myDSP.MyResampler;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestMyFilter {

	public static void main(String [] args) throws Exception {

		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);

		MyComplexArrayFloat hi = MyHilbert.get_old(u.samples);
		double [] abshi = MyMath.todouble(hi.getAbsFloat());
		// Resampling
		double [] x = MyResampler.exe(abshi, 1024, u.sampleRate);

		double [] b = new double[] {0.006514350434926,   0.008625611735089,   0.014537725009923,   0.023948539817104,   0.036112418048306, 
				0.049913983252129,   0.063991437800973,   0.076894537216144,   0.087257364191478,   0.093963737223264,
				0.096283773507432,   0.093963737223264,   0.087257364191478,   0.076894537216144,   0.063991437800973,
				0.049913983252129,   0.036112418048306,   0.023948539817104,   0.014537725009923,   0.008625611735089,
				0.006514350434926};
		double [] a = new double[b.length];
		Arrays.fill(a,  0.0);
		a[0] = 1.0;

		MyFilter f = new MyFilter(b, a);
		double [] zi = MyMath.uniformRandomVector(b.length-1);
		f.filter(x, zi);
		
		double [] jy = f.gety();
		double [] zf = f.getzf();
		
		MyMatlabConnection matlab = new MyMatlabConnection();
		//double [] my = matlab.eng.feval("myFilter", b, a, x);
		//double [] my = matlab.eng.feval("filter", b, a, x);
		
		Struct ma = matlab.eng.feval("myFilter", b, a, x, zi);
		
		double [] ma_y = (double [])ma.get("y");
		MyMath.compare_vectors("TestMyFilter: y: ", jy, ma_y, 1e-20);
		
		double [] ma_zf = (double [])ma.get("zf");
		MyMath.compare_vectors("TestMyFilter: zf: ", zf, ma_zf, 1e-20);

	}

}
