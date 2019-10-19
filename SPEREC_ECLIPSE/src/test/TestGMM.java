package test;

import java.io.File;
import java.util.ArrayList;

import app.Environment;
import app.FeaDataSet;
import myMath.MyMatrix;
import sperec_common.AllSessionsArrays;
import sperec_common.LabeledArrayList;
import sperec_common.Mixture_MSR;
import sperec_common.StRecord;

public class TestGMM {

	static public Mixture_MSR run(ArrayList<MyMatrix>feaSessions, int nmix, int niter) throws Exception {
				
		Mixture_MSR ubm = new Mixture_MSR();

		long tStart = System.currentTimeMillis();
		
		//int nmix = 16;
		//int niter = 10;
		
		ubm.train(feaSessions, nmix, niter);
		TEST.showElapsedTime(tStart);
		
		return ubm;
	}

	static Mixture_MSR run(String feaCfgFile, int nmix, int niter) throws Exception {
				
		//String feaCfgFile = "";  //Environment.getMainOutputFolderPath() + File.separator + f;
		System.out.println("Loading sample data from: " + feaCfgFile);
		FeaDataSet feaDataSet = FeaDataSet.fromConfigFile(feaCfgFile);
		
		System.out.println("\nNow Start computation");
		
		ArrayList<LabeledArrayList<StRecord>> q = feaDataSet.getArrayListOfRecordSets();
		AllSessionsArrays<MyMatrix> a = FeaDataSet.toSessionsArrays(q); //STM.
		ArrayList<MyMatrix>feaSessions = a.data;	
		
		return run(feaSessions, nmix, niter);

	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	/*static Mixture_MSR testUBM_MSR() throws Exception {
		
		int nmix = 4;
		int niter = 10;
		
		// Check matlab
		String feaDir = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ufv" + File.separator + "aBook_Set4--fea-_2_";
		String fileListFullPath = feaDir + File.separator + "ufvList_aBookSet4.txt";
		Mixture_MSR ubm_matlab = MATLAB.my_gmm_em_optimized_testJava(feaDir, fileListFullPath, nmix, niter, 1);
		
		Mixture_MSR ubm_java = new Mixture_MSR();
		
		ArrayList<MyMatrix> dataList = load_aBook_Set4();		
		ubm_java.train(dataList, nmix, niter);

		String outputfile = ubmOutFolder + File.separator + "ubm_msr_" + nmix + ".ubmmsr";
		ubm_java.writeToFile(new File(outputfile));
		
		compare_matrices("UBM: mu\t: ", ubm_java.getMuMatrix(VectorDim.COLUMN_VECTORS), ubm_matlab.getMuMatrix(VectorDim.COLUMN_VECTORS).getArray());
		compare_matrices("UBM: sigma\t: ", ubm_java.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS), ubm_matlab.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS).getArray());
		compare_vectors("UBM: w", ubm_java.getWMatrix(VectorDim.ROW_VECTORS).getArray()[0], ubm_matlab.getWMatrix(VectorDim.ROW_VECTORS).getArray()[0]);

		
		return ubm_java;
	}*/

	
	/*private static MyMatrix logsumexp_from_BW(MyMatrix post, int dim) {	//function y = logsumexp(x, dim) // public for debug purpose only
		MyMatrix xmax = post.max(dim); //xmax = max(x, [], dim); // xmax must be 1 x numFrames		
		MyMatrix y = xmax.plus(post.minusRowVector(xmax.getRow(0)).exp().sum(dim).log()); //y    = xmax + log(sum(exp(bsxfun(@minus, x, xmax)), dim)); // y must be 1 x numFrames

		//ind  = find(~isfinite(xmax));
				//if ~isempty(ind)
				//y(ind) = xmax(ind);
				//endfor (int i = 0; i<numFrames; i++)

		int n = y.getColumnDimension();
		for (int i=0; i<n; i++)
		{
			double yi = y.get(0, i);
			// isFinite requires Java 1.8, Android API level 24, if (!Double.isFinite(y.get(0, i)))
			if (Double.isInfinite(yi) || Double.isNaN(yi))
			{
				y.set(0, i, xmax.get(0, i));
			}
		}

		return y;
	}
	
	private static MyMatrix logsumexp_from_MSR(MyMatrix post, int dim) {	//function y = logsumexp(x, dim) // public for debug purpose only
		
		MyMatrix xmax = post.max(dim); //xmax = max(x, [], dim); // xmax must be 1 x numFrames		
		double [] xm = xmax.getRow(0);
		//OPZIONE1
		//MyMatrix y = xmax.plus(post.minusRowVector(xm).exp().sum(dim).log()); //y    = xmax + log(sum(exp(bsxfun(@minus, x, xmax)), dim)); // y must be 1 x numFrames
		//for (int i=0; i<y.getColumnDimension(); i++)
		//if (!Double.isFinite(y.get(0, i)))
		//	y.set(0,  i,  xmax.get(0,  i));
			
		
		// OPZIONE2, con dim=1, e' leggermente più veloce
		int n = post.getColumnDimension();
		int m = post.getRowDimension();
		double [][] p = post.getArray();
		double [] dy = new double[n];
		for (int c = 0; c<n; c++)
		{
			double s = 0;
			for (int r = 0; r<m; r++)
				s += Math.exp(p[r][c] - xm[c]);
			
			double v = xm[c] + Math.log(s);
			// isFinite requires Java 1.8, Android API level 24, if (!Double.isFinite(y.get(0, i)))
			//dy[c] = Double.isFinite(v) ? v: xm[c];
			dy[c] = (Double.isInfinite(v) || Double.isNaN(v)) ? xm[c] : v; //Double.isFinite(v) ? v: xm[c];
		}
		MyMatrix Z = MyMatrix.fromRowVector(dy);
		
		//TEST.compare_matrices("OPZIONE1 vs OPZIONE2", y, Z.getArray());
			
		return Z;
	}*/
	
	


	public static void main(String [] args) throws Exception {
		
		long tStart = 0;
		
		/*
		MyMatrix P = MyMatrix.randn(7000, 7000, 1.0).abs();
		tStart = System.currentTimeMillis();
		MyMatrix M_BW = logsumexp_from_BW(P, 1);
		TEST.showElapsedTime_ms(tStart);
		
		tStart = System.currentTimeMillis();
		MyMatrix M_MSR = logsumexp_from_MSR(P, 1);
		TEST.showElapsedTime_ms(tStart);
		
		TEST.compare_matrices("logsumexp_from_MSR vs logsumexp_from_BW", M_MSR, M_BW.getArray());

		*/

		
		
		Environment en = new Environment();
		String MAIN_OUTPUT_FOLDER_PATH = en.getMainOutputFolderPath();
		String ubmOutFolder = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ubm";

		String f = "toSPLIT_toVAD_toFEA" + File.separator + "ufv" + File.separator+ "Monologhi_001_150_16000Hz--vad---i-0.1--of-2.0--fea-mode_M__nc_24__np_24__feadim_24" + File.separator + "fea.cfg";
		String inputDataPath = en.getDataDir() + File.separator + f;
		Mixture_MSR ubm = TestGMM.run(inputDataPath, 8, 5);
		
		String outfile = ubmOutFolder + File.separator + "ubm_8_5.ubm";
		
		ubm.writeToFile(new File(outfile) );
		System.out.println("UBM saved to file: " + outfile);
		
	}
	
	
}
