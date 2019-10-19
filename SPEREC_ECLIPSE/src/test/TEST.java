package test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.matlab.types.Struct;

import Jama.Matrix;
import app.Environment;
import app.FeaDataSet;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;
import sperec_common.de.fau.cs.jstk.io.ChunkedDataSet;
import sperec_common.de.fau.cs.jstk.io.FrameInputStream;
import sperec_common.de.fau.cs.jstk.io.FrameOutputStream;
//import de.fau.cs.jstk.app.GaussEM;
//import de.fau.cs.jstk.app.Initializer;
//import de.fau.cs.jstk.io.ChunkedDataSet;
//import de.fau.cs.jstk.io.FrameInputStream;
//import de.fau.cs.jstk.io.FrameOutputStream;
import sperec_common.de.fau.cs.jstk.io.IOUtil;
import sperec_common.de.fau.cs.jstk.stat.Mixture;
import ex_common.MyLDA;
import guiutils.ChooseFile;
import guiutils.ComboBox;
import myIO.IOClass;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import myMath.MySort;
import sperec_common.AllSessionsArrays;
import sperec_common.AudioProcessingSpecs;
import sperec_common.AuthenticationResult;
import sperec_common.BWStatistics;
import sperec_common.BWstatsCollector;
import sperec_common.BWstatsCollector_MSR;
import sperec_common.ConfigurationFile;
import sperec_common.EmpiricalFn;
import sperec_common.FeaSpecs;
import sperec_common.GPLDA;
import sperec_common.GPLDA_Models;
import sperec_common.LabeledArrayList;
//import sperec_common.IOClass;
import sperec_common.MiscUtils;
import sperec_common.MixtureModel;
import sperec_common.Mixture_MSR;
import sperec_common.MyAccumulator;
import sperec_common.MyAutocorrellatedVoiceActivityDetector;
import sperec_common.MyEigenvalueDecomposition;
import sperec_common.MyRingBuffer;
import sperec_common.NDimensionalArray;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC;
import sperec_common.SPEREC_AudioProcessor;
import sperec_common.SPEREC_Loader;
import sperec_common.SPEREC_Specs;
import sperec_common.SPEREC_UBM_IV_GPLDA;
import sperec_common.SessionsTable_MyMatrix;
import sperec_common.SpeakerModel;
import sperec_common.StRecord;
import sperec_common.TVSpace;
import sperec_common.TVSpace_Models;
import sperec_common.VadSpecs;
import sperec_common.ValidationResultGauss;
import sperec_common.tryTarsos_common.MyMFCCfromTarsos;
import sperec_common.tryTarsos_common.WriterToRingBuffer;
import sperec_jvm.POPREF_Builder;
import sperec_jvm.SPEREC_Loader_JVM;
import tryTarsos.MyAudioDispatcherFactory;

public class TEST {
	
	private static final String MAIN_OUTPUT_FOLDER_PATH = new Environment().getMainOutputFolderPath();

	static String tempFolder = MAIN_OUTPUT_FOLDER_PATH; //"C:/Users/FS/Desktop/tempFiles";
	
	static String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
	static String sAudioFilename = "AUDIO-01_cut-11025.wav";
	static String sOutAudioFolder = "C:/Users/FS/Desktop/tempFiles";
	static String sAudioFullPath = sAudioFolder + "/" + sAudioFilename;
	
	static String popRefOutFolder = "C:\\Users\\FS\\Dropbox\\FS\\_DOTTORATO\\SANNIO\\_code\\SPEREC_ANDROID\\SPEREC\\app\\src\\main\\res\\raw";
	static String popRefOutFileName = "prova.popref";
	static String poprefFile = popRefOutFolder + File.separator + popRefOutFileName;
	
	static String sAudioFileEnroll = "C:\\Users\\FS\\Dropbox\\FS\\_DOTTORATO\\SANNIO\\_code\\SPEREC_ANDROID\\SPEREC\\app\\src\\main\\res\\raw\\abook_002_enroll.wav";
	static String sAudioFileTest = "C:\\Users\\FS\\Dropbox\\FS\\_DOTTORATO\\SANNIO\\_code\\SPEREC_ANDROID\\SPEREC\\app\\src\\main\\res\\raw\\abook_002_test.wav";
	
	static String ubmOutFolder = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ubm";
	
	
	static public void showElapsedTime(long tStart) {
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = Math.round(tDelta / 1000.0);
		System.out.println("Elapsed seconds: " + elapsedSeconds );
	}
	
	static public void showElapsedTime_ms(long tStart) {
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		System.out.println("Elapsed ms: " + tDelta );
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		long tStart;

		System.out.println("main: START");
		
		// Ottobre2018
		// Profiling UBM training
		//Mixture_MSR ubm = new Mixture_MSR();
		//String f = "enrollFea.cache";
		//String inputDataPath = Environment.getMainOutputFolderPath() + File.separator + f;
					
		TestGMM.main(null);
		
		
    	//MyEnframe.test_enframe_1();
    	//MyEnframe.test_enframe_2();
    	//MyEnframe.test_enframe_3();
    	//MyEnframe.test_enframe_4();
    	
    	//MyVad.test_vad_1();
		//MyVad.test_vad_2();
		//MyVad.test_vad_3();
		//MyVad.test_vad_4();
		
		//MyAudioInputStreamFactory.testFromFile();
		//MyEnframeAudioTest.test1();
    	
    	//testTarsosJVM_0();
    	//testTarsosJVM_1();
    	//testTarsosJVM_VAD();
    	
    	//MyRingBuffer.test1();
    	//testProducerConsumerThread();
    	//testProConVAD();
    	//testProConVADMFCC();
    	//testSperecAudioProcessorWithVAD();
		
		//test_JSTK_Initializer();
		//test_JSTK_GaussEM();
		
		
		
		// testBWstatsCollector();
		
		
	
		
		// testIvectorExtraction();
		
		//testEig(100);
		
		//testLDA(2);
		// testLDA(false);
		// testIvectorReduxLdadim();
		// testCov();
		// test_MyAccumulator_count_unique_stable();
		// test_MySort_sort_and_replace();
		// test_svd(300);
		//test_calc_white_mat();
		//testGPLDA();
		//testPopref();
		
		//testSperecComputeSpeakerModelFromRawAudioFile();
		
		//testBw();
		// testSperecScoring();
		
    	//ConfigurationFile.testSave("C:\\Users\\FS\\Desktop\\tempFiles", "prova.cfg");
		//ConfigurationFile C = ConfigurationFile.load("C:\\Users\\FS\\Desktop\\tempFiles\\prova.cfg");
		
		
		// Test Vad specs I/O
		//VadSpecs v = VadSpecs.defaults();
		//v.saveToFile(tempFolder + File.separator + "prova.vad.json");
			
		//VadSpecs v = Specs.loadFromFile(tempFolder + File.separator + "prova.vad.json", VadSpecs.class);
		

		//UBM_Specs qq = InputUbmSpecs.get(new UBM_Specs());
		
		//test_IOUtil_readDouble();
		//test_IOClass_readDouble();
		//System.out.println(System.getenv("USERPROFILE"));
		
		// Prova
		//ArrayList<Boolean> target = new ArrayList<Boolean>();
		//target.add(true);
		//target.add(false);
		//Boolean [] target_v = new Boolean[target.size()];
		//target_v = target.toArray(target_v);
			
		
		// Test NDimensionalArray
		/*NDimensionalArray NDA = new NDimensionalArray(2, 3, 4);
		NDA.set("Ciao", 1, 1, 1);
		NDA.set("QQQ", 1, 2, 2);
		System.out.println(NDA.get(1, 1, 1));
		System.out.println(NDA.get(1, 2, 2));
		
		String json = new Gson().toJson(NDA);
		System.out.println(json);
		
		Gson gson = new Gson();
		NDimensionalArray NDA2 = (NDimensionalArray) gson.fromJson(json, NDimensionalArray.class);
		String json2 = new Gson().toJson(NDA2);
		System.out.println(json2);*/
		
		// Test EmpiricalFn
		/*
		String [] xNames = {"TESTDUR", "SNR"};
		double [] testDur = {2, 4, 8, 14};
		double [] SNR = {0, 10, 20};
		double [][] xValues = {testDur, SNR};
		//int [] dim = {testDur.length, SNR.length};
		String [] yNames = {"ErrorCDFile"};
		
		EmpiricalFn EFN = new EmpiricalFn( xNames, xValues, yNames);
		EFN.set("ErrorCDFile", "ciao.bin", 2.0, 10.0);
		System.out.println(EFN.get("ErrorCDFile", 2.0, 10.0));
		EFN.set("ErrorCDFile", "ciao_4_0.bin", 4.0, 0.0);
		System.out.println(EFN.get("ErrorCDFile", 4.0, 0.0));
		String json = EFN.toJsonString();
		System.out.println(json);
		EmpiricalFn EFN2 = EmpiricalFn.fromJsonString(json);
		System.out.println(EFN2.toJsonString());
		System.out.println(EFN2.get("ErrorCDFile", 4.0, 0.0));
		*/
		
		/*double x = Math.ulp(1.0);
		System.out.println(x);
		
		String [] s = {"AAA", "BBB"};
		ComboBox.show("Scegli", s);
		*/
		
		/*
		ArrayList<Double> scores = new ArrayList<Double>();
		ArrayList<Boolean> target = new ArrayList<Boolean>();
		scores.add(5.0);scores.add(5.5);scores.add(6.0);scores.add(7.0);
		target.add(false);target.add(false);target.add(true);target.add(true);
		ValidationResult valRes = new ValidationResult(scores, target);
		
		NDimensionalArray nV = new NDimensionalArray(1, 2);
		nV.set(valRes,  0,0);
		
		System.out.println(nV.get(0,0).toString());
		
		
		String [] xNames = {"x1"};
		double [] testSessionDurationSec_v = {8.0};
		double [][] xValues = {testSessionDurationSec_v};
		String [] yNames = {"Ciao"};
		
		EmpiricalFn EFN = new EmpiricalFn(xNames, xValues, yNames);
		
		EFN.set("Ciao",  valRes, 8.0);
		
		ValidationResult valRes2 = EFN.get("Ciao",  8.0);
		System.out.println(valRes.toString());
		valRes.reduceMemory();
		String json = EFN.toJsonString();
		System.out.println(json);
		
		
		EmpiricalFn EFN2 = EmpiricalFn.fromJsonString(json);
		
		EmpiricalFn EFN3 = EmpiricalFn.load("C:\\Users\\FS\\Google Drive\\_DOTTORATO\\Sperec_tmp_files\\2018.03.29.18.15.29.574.efn");
				
		*/
		
			// Test my preferred sorting method
		//testSortSelf();
		
			// Test the SPEREC Loader
		//{SPEREC oSPEREC = testSperecLoader("C:\\Users\\FS\\Google Drive\\_DOTTORATO\\Sperec_tmp_files\\popref\\prova2", "popref.cfg");}		
		
			// Test Arraylist<String> cloning
		//testArrayListOfString_clone();


		//testMatlab();
		// ------------------------------ MAIN TEST - START 
		// testUBM_MSR(); // CHECKED: OK
		// testBWstatsCollector_MSR();
		// testTVSpaceNew();
		// testIvectorExtraction();
		// testLDA();
		// testIvectorReduxLdadim();
		// testGPLDA();
		//TestPopRef2.main();

		// ------------------------------MAIN TEST - END

		System.out.println("\nDONE\n");

	}
	
	static void testEig(int dim) throws RejectedExecutionException, EngineException, InterruptedException, ExecutionException {
		
		String testName = "my_eig(Sb, Sw) ";
		MyMatrix Sb = MyMatrix.randn(dim, dim, 1.0);
		MyMatrix Sw = MyMatrix.randn(dim, dim, 1.0);
		Sb = Sb.times(Sb.transpose());
		Sw = Sw.times(Sw.transpose());
		Matrix Swi = Sw.inverse();
		
		MyEigenvalueDecomposition JAVA_eig = new MyEigenvalueDecomposition(Swi.times(Sb)); //, debug);
		MatlabEngine eng = MATLAB.getMatlabConnection();
		Struct MATLAB_eig = eng.feval("my_eig_testJava", Sb.getArray(), Sw.getArray()); //, debug);
		
		// Compare eigenvalues
		double [] JAVA_evals = JAVA_eig.getD().getDiag(); // Eigenvalues
		double [] MATLAB_evals = new MyMatrix((double [][])MATLAB_eig.get("D")).getDiag();
		compare_vectors(testName + ": D : \t", JAVA_evals, MATLAB_evals);
		
		if (dim <= 5)
		{
			System.out.print("JAVA   D: ");
			for (int i=0; i<JAVA_evals.length; i++)
				System.out.print("\t" + JAVA_evals[i]);
			System.out.println("");
			
			System.out.print("MATLAB D: ");
			for (int i=0; i<MATLAB_evals.length; i++)
				System.out.print("\t" + MATLAB_evals[i]);
			System.out.println("");
			
		}
					
		// Compare eigenvectors
		MyMatrix JAVA_V = JAVA_eig.getV();
		double [][] MATLAB_V = (double [][])MATLAB_eig.get("V");
		
		System.out.println(testName + " JAVA_V:");
		JAVA_V.print(10, 10);
		System.out.println(testName + " MATLAB_V:");
		new MyMatrix(MATLAB_V).print(10, 10);
		
		compare_matrices(testName + " V:\t", JAVA_V, MATLAB_V);		
	}
	
	/**
	 * Actual input parameters are:
	 * - the i-vectors coming from LDA reduction, stored in the IV_lda_File file
	 * - spk_labs: labels to separate i-vectors of different speakers
	 * - nphi: dimensionalty of eigenvoice space
	 * - niter: number of iterations
	 * 
	 * Actual output are:
	 * - Phi: the matrix describing the eigenvoice space
	 * - Sigma
	 * - W
	 * - M
	 * @throws Exception 
	 */
	public static void testGPLDA() throws Exception {
		
		boolean debug = true;
		
		String IV_lda_File =  ubmOutFolder + File.separator + "ubm_msr_" + 4 + "ivs_redux.ivs";
		
		MyMatrix Data = MyMatrix.readFromFile(new File(IV_lda_File));
		if (Data.getVectorDim() != VectorDim.COLUMN_VECTORS)
			throw new Exception("Illegal vector_dim value");

		int nobs = Data.getColumnDimension();

		int nsess = 3;
		
		Short [] classes = MiscUtils.simulate_classes(nobs, nsess);
	
		int nphi = 20;
		int niter = 1;
			
		// Do the job
		MATLAB_GPLDA gplda = new MATLAB_GPLDA(nphi);
		gplda.debug(debug);
		
		// For debug, not required otherwise
		int initDim = Data.getRowDimension();
		gplda.init(initDim);
		MyMatrix Phi_init = gplda.getPhi().copy();
		MyMatrix Sigma_init = gplda.getSigma().copy();
		
		gplda.estimate(Data, classes, niter);
		
		// Now I need to compare with Matlab: plda = my_gplda_em(data, spk_labs, nphi, niter, Sigma, Phi)
		//------------------------------------------------------
		compare_matlab_gplda(Data, classes, nphi, niter, Sigma_init, Phi_init, gplda);
		//------------------------------------------------------
	}
	
	
	/**
	 * 
	 * @param Data
	 * @param spk_labs
	 * @param nphi
	 * @param niter
	 * @param Sigma_init
	 * @param Phi_init
	 * @param gplda
	 */
	static void compare_matlab_gplda(MyMatrix Data, Short [] spk_labs, int nphi, int niter, MyMatrix Sigma_init, MyMatrix Phi_init, MATLAB_GPLDA gplda) {
				
		GPLDA_Models gMatlab = MATLAB.my_gplda_em(Data, spk_labs, nphi, niter, Sigma_init, Phi_init);
		GPLDA_Models g = gplda.getModels();
			
		compare_matrices("Comparing against matlab: Phi:\t\t", g.Phi, gMatlab.Phi);
		compare_matrices("Comparing against matlab: Sigma:\t\t", g.Sigma, gMatlab.Sigma);
		compare_vectors("Comparing against matlab: M:\t\t", g.Mcol,gMatlab.Mcol);
		compare_matrices("Comparing against matlab: W:\t\t", g.W, gMatlab.W);

		compare_vectors("Comparing against matlab: Lambda:\t", g.Lambda.getDiag(), gMatlab.Lambda.getDiag());
		compare_matrices("Comparing against matlab: Uk:\t\t", g.Uk, gMatlab.Uk);
		compare_matrices("Comparing against matlab: Q_hat:\t", g.Q_hat, gMatlab.Q_hat);
	}

	
	/**
	 * 
	 * @throws Exception 
	 */
	public static void testIvectorReduxLdadim() throws Exception {
		
		String VFile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + "lda.V";
		String IVFile =  ubmOutFolder + File.separator + "ubm_msr_" + 4 + "ivs.ivs";
		String IV_lda_File =  ubmOutFolder + File.separator + "ubm_msr_" + 4 + "ivs_redux.ivs";
		
		// Read the i-vectors as they exited from the TVSpace
		MyMatrix IV = MyMatrix.readFromFile(new File(IVFile));
		if (IV.getVectorDim() != VectorDim.COLUMN_VECTORS)
			throw new Exception("Illegal vector_dim value");
		
		// Get the LDA Matrix as it exited from training
		MyMatrix V_LDA = MyMatrix.readFromFile(new File(VFile));
		if (V_LDA.getVectorDim() != VectorDim.COLUMN_VECTORS)
			throw new Exception("Illegal vector_dim value");
		
		// The  column dimension of the V_LDA matrix provides an upper limit for lda_dim. It is the number of classes (during training)-1
		int ldadim_max = V_LDA.getColumnDimension();
		
		int ldadim = 25;
		
		
		//dev_ivs = V(:, 1 : lda_dim)' * dev_ivs;
		MyMatrix V_LDA_redux = V_LDA.getMatrix(0, V_LDA.getRowDimension()-1, 0, ldadim-1);
		
		MyMatrix IV_redux_Java = V_LDA_redux.transpose().times(IV);
		IV_redux_Java.setVectorDim(VectorDim.COLUMN_VECTORS);
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		try {
			double[][] IV_redux_Matlab = eng.feval("lda_redux_testJava", V_LDA.getArray(), ldadim, IV.getArray());
			
			compare_matrices("Comparing against matlab: lda reduction: normInf(relative err) = ", IV_redux_Java, IV_redux_Matlab);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		MATLAB.closeSperecMatlabConnection(eng);
		
		IV_redux_Java.writeToFile(IV_lda_File);
	}
	
	/**
	 * 
	 * @throws Exception 
	 */
	public static void testLDA(int nsess) throws Exception {
		
		String IVFile =  ubmOutFolder + File.separator + "ubm_msr_" + 4 + "ivs.ivs";
		String VFile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + "lda.V";
		
		MyMatrix IV = MyMatrix.readFromFile(new File(IVFile));
		
		if (IV.getVectorDim() != VectorDim.COLUMN_VECTORS)
			throw new Exception("Illegal vector_dim value");
		
		int nobs = IV.getColumnDimension();
		int fd = IV.getRowDimension(); // [nObs x fd]
		
		//int nsess = 3;
		
		Short [] classes = MiscUtils.simulate_classes(nobs, nsess);

		MyLDA lda = new MyLDA(fd);
		//lda.debug(debug);
		
		MyMatrix IV0 = IV.copy();

		//  dal MSR
		
		// Centering the data //data = bsxfun(@minus, data, mean(data, 2)); % centering the data
		IV.minusEqualColumnVector(IV.mean(2));
		
		lda.estimate(classes, IV);
		MyMatrix V = lda.getProjection();
		
		//EigenvalueDecomposition eig = new EigenvalueDecomposition(Swi.times(Sb));
		
		/* Questo dovrebbe funzionare, ma solo se ho meno classi del numero di osservazioni //
		LDA lda = new LDA(fd);
		for (int i=0; i<nobs; i++)
			lda.accumulate(classes[i], IV.getRow(i));
		lda.estimate(null);
		double [][] dV = lda.getProjection();
		*/
		V.writeToFile(VFile);
		
		MyMatrix V_matlab = MATLAB.my_lda_testJava(IV0, classes);
		
		compare_matrices("Comparing against matlab: lda: ", V, V_matlab);
		
	}


	/**
	 * 
	 * @throws Exception 
	 */
	public static void testIvectorExtraction() throws Exception {

		String bwfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".bwmsr";
		String ubmfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".ubmmsr";
		String TVfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".tvs";
		String IVFile =  ubmOutFolder + File.separator + "ubm_msr_" + 4 + "ivs.ivs";
		
		BWStatistics bw = BWStatistics.readFromFile(bwfile);

		Mixture_MSR ubm = Mixture_MSR.readFromFile(new File(ubmfile));
		assert(ubm.diagonal());
		
		TVSpace_Models tvmodels = TVSpace_Models.readFromFile(new File(TVfile));
			
		TVSpace tv = new MATLAB_TVSpace();
		tv.setModels(tvmodels);
		
		if (bw.N.getVectorDim()!= VectorDim.ROW_VECTORS)
			bw.N = bw.N.transpose();
		
		if (bw.F.getVectorDim()!= VectorDim.ROW_VECTORS)
			bw.F = bw.F.transpose();
		
		MyMatrix X = tv.extract_i_vectors_matrix(bw.N, bw.F);
		X.writeToFile(IVFile);
		
		//------------------------------------------------------
		MyMatrix T_invS = tvmodels.T_invS;
		int [] idx_sv = tvmodels.idx_sv;
		MyMatrix Ttransp = tvmodels.T.transpose();
		compare_matlab_my_extract_ivector_helper(bw.N, bw.F, Ttransp, T_invS, idx_sv, X);
		//------------------------------------------------------
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public static void testTVSpaceNew() throws Exception {
		
		String bwfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".bwmsr";
		String ubmfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".ubmmsr";
		String TVfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".tvs";
		
		MyMatrix T = null;
		
		MixtureModel ubm = Mixture_MSR.readFromFile(new File(ubmfile)); //Mixture ubm = Mixture.readFromFile(new File(ubmfile));
		assert(ubm.diagonal());
		
		BWStatistics bw =  BWStatistics.readFromFile(bwfile);
		
		int tv_dim = 75; //200;
		int niter = 10;
		
		//int nworkers = 1;
		int nworkers = Runtime.getRuntime().availableProcessors(); // Not used at the moment
			
		MATLAB_TVSpace tv = new MATLAB_TVSpace();
		tv.train_tv_space(bw, ubm, tv_dim, niter);
		MyMatrix Tinit_for_debug = tv.getTinit();
		
		TVSpace_Models tvmodels = tv.getModels();
		T = tvmodels.T; //T = tv.getT();
		
		if (T.getVectorDim() == VectorDim.UNSPECIFIED)
			throw new Exception("Illegal vector_dim value");
		
		// Debug
		
		MyMatrix matlab_T = MATLAB.testTV_testJava(ubm, bw, Tinit_for_debug, niter, nworkers);

		compare_matrices(" T:\t", T, matlab_T);
		
		tvmodels.writeToFile(new File(TVfile));
	}
	
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	/*static private ArrayList<MyMatrix> load_aBook_Set4() throws Exception {
		
		String feaDir = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ufv" + File.separator + "aBook_Set4--fea-_2_";
		String fileListFullPath = feaDir + File.separator + "ufvList_aBookSet4.txt";
		int ufv = 1;
		VectorDim required_vector_dim = VectorDim.ROW_VECTORS;
		//ArrayList<MyMatrix> dataList = MiscUtils.loadFeatures(feaDir, fileListFullPath, ufv, required_vector_dim);
		ArrayList<MyMatrix> dataList = SessionsTable_MyMatrix.loadFeatures(feaDir, fileListFullPath, ufv, required_vector_dim);
		
		return dataList;
	}*/
	
		
	/**
	 * This function, if executed with the debugger, is able to show that 
	 * the readDouble (double []) function of de.fau.cs.jstk.io.IOUtil is not able to detect if the underlying InputStream,
	 * such as a FileInputStream, has less data left than the required. In this case, no Exception is thrown and false is returned.
	 * I think that it's a dangerous behaviour because it hides underlying inconsistencies in I/O operations.
	 * So, since 7 March 2018, I code my own IOUtil, i.e. IOClass.
	 */
	static void test_IOUtil_readDouble() {
		String tempFileName = MiscUtils.getTemporaryFileFullPath();
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(tempFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double [] bufW = new double[320];
		try {
			IOUtil.writeDouble(os, bufW, ByteOrder.LITTLE_ENDIAN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now try to read more bytes
		InputStream is = null;
		try {
			is = new FileInputStream(new File(tempFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double [] bufR = new double [bufW.length + 640];
		
		try {
			boolean eof = !IOUtil.readDouble(is, bufR, ByteOrder.LITTLE_ENDIAN);
			System.out.println(eof);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	static void test_IOClass_readDouble() {
		String tempFileName = MiscUtils.getTemporaryFileFullPath();
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(tempFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double [] bufW = new double[320];
		try {
			IOClass.writeDoubleArray(os, bufW, ByteOrder.LITTLE_ENDIAN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now try to read more bytes
		InputStream is = null;
		try {
			is = new FileInputStream(new File(tempFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double [] bufR = new double [bufW.length + 640];
		
		try {
			int read = IOClass.readDoubleArray(is, bufR, ByteOrder.LITTLE_ENDIAN);
			System.out.println(read + " read out of " + bufR.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	static void testSperecScoring() throws Exception {
		
		System.out.println("testSperecScoring: start");
		
		SPEREC oSperec = testSperecLoader(MAIN_OUTPUT_FOLDER_PATH + File.separator + "popref" + File.separator + "prova3", "prova.cfg");
				
		AudioDispatcher adRef  = AudioDispatcherFactory.fromFile(new File(sAudioFileEnroll), 2048, 0);
		AudioDispatcher adTest = AudioDispatcherFactory.fromFile(new File(sAudioFileTest), 2048, 0);		

		AuthenticationResult aRes = oSperec.compareSpeakers(adRef, adTest);
		System.out.println(aRes.toString());
		
		System.out.println("testSperecScoring: end");
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	/*
	static void testBw() throws Exception {
		
		System.out.println("testBw: start");
		
		MATLAB_SPEREC_UBM_IV_GPLDA oSperec = (MATLAB_SPEREC_UBM_IV_GPLDA) testSperecLoader(MAIN_OUTPUT_FOLDER_PATH + File.separator + "popref" + File.separator + "prova3", "prova.cfg");
		AudioDispatcher ad  = AudioDispatcherFactory.fromFile(new File(sAudioFileEnroll), 2048, 0);

		SPEREC_AudioProcessor oSAP = new SPEREC_AudioProcessor(ad);
		AudioProcessingSpecs sp = oSperec.getAudioProcessingSpecs();
		oSAP.setVad(sp.vadSpecs);
		oSAP.setFea(sp.feaSpecs);
		oSAP.run(); // It blocks until job finished
		
		// Get the features as a Matrix
		MyMatrix feaMatrix = oSAP.outputAsMatrix();
		
		POPREF_MODEL pop = oSperec.getPopRefModel();
		
		BWstatsCollector_MSR BW = new BWstatsCollector_MSR("NF");
		ArrayList<MyMatrix> spkSessions = new ArrayList<MyMatrix>();
		spkSessions.add(feaMatrix);
		BWStatistics bwStats = BW.collectBWstats(pop.ubm, spkSessions, VectorDim.ROW_VECTORS);
		MixtureModel ubm = pop.ubm;
		
		MyMatrix mu = ubm.getMuMatrix(VectorDim.COLUMN_VECTORS);
		MyMatrix sigma = ubm.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS);
		MyMatrix w = ubm.getWMatrix(VectorDim.COLUMN_VECTORS);
		MyMatrix logprob_Java = BW.lgmmprob(feaMatrix.transpose(), mu, sigma, w);
		MyMatrix logprob_Matlab = MATLAB.lgmmprob_testJava(feaMatrix.transpose(), mu, sigma, w);
		
		compare_matrices("\t lgmmprob output: ", logprob_Java, logprob_Matlab.getArray());
		
		
		MyMatrix logse_Java = BW.logsumexp(logprob_Java, 1);
		MyMatrix logse_Matlab = MATLAB.logsumexp_testJava(logprob_Java, 1);
		
		compare_matrices("\t logsumexp output: ", logse_Java, logse_Matlab.getArray());
		
		System.out.println("testBw: end");

	}
	*/
	
	/**
	 * 
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws InterruptedException
	 */
	/*
	public static void testSperecComputeSpeakerModelFromRawAudioFile() throws IOException, UnsupportedAudioFileException, InterruptedException {

		SPEREC oSperec = new SPEREC_UBM_IV_GPLDA();
		
		oSperec.setPopRef(POPREF_MODEL.readFromFile(new File(poprefFile)));
		
		AudioDispatcher mainAudioDispatcher = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), 2048, 0);
		VadSpecs vadSpecs = VadSpecs.defaults();
		FeaSpecs feaSpecs = FeaSpecs.defaults();
		MyMatrix spkIV = oSperec.computeSpeakerModelFromRawAudio(mainAudioDispatcher, vadSpecs, feaSpecs);
	}
	*/	
	/**
	 * 
	 * @throws Exception
	 */
	public static void testMatlab() throws Exception {
		
		boolean debug = true;
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		
		double normInf;
		MyMatrix R, A, B, C;
		String testName;
		
		testName = "Inverse of a square matrix";
		R = MyMatrix.randn(500, 500, 1.0);	
		normInf = compare_matrices(R.inverse(), eng.feval("inv", R.getArray()));
		System.out.println(testName + ": \t" + normInf);
		
		testName = "Pesudoinverse";
		R = MyMatrix.randn(500, 400, 1.0);	
		normInf = compare_matrices(R.inverse(), eng.feval("pinv", R.getArray()));
		System.out.println(testName + ": \t" + normInf);
		
		testName = "Left divide";
// 		RU = B \ A;
		A = MyMatrix.randn(200, 200, 1.0);
		B = MyMatrix.randn(200, 50, 1.0);
		C = B.solve(A);
		normInf = compare_matrices(C, eng.feval("mldivide", B.getArray(), A.getArray())); // mldivide(A, B) è lo stesso che A\B
		System.out.println(testName + ": \t" + normInf);
		
		// ---------------------------------    Test EigenValue decomposition ------------------------
		testEig(100);

		
		// ---------------------------- Test Singular Values Decomposition ----------------------------
		//test_svd(100);
		
		
		
		test_calc_white_mat();
		
		
		
		MATLAB.closeSperecMatlabConnection(eng);

	}
	
	static MyMatrix test_calc_white_mat() {
		
		int dim =100;
		MyMatrix Sb = MyMatrix.randn(dim, dim, 1.0);
		return test_calc_white_mat("\nTESTING calc_white_mat", Sb);
	}
	
		
	static MyMatrix test_calc_white_mat(String msg, MyMatrix Sb) {
		
		boolean debug = true;
		String testName = "calc_white_mat";
		if (null==msg)
			msg = "TESTING calc_white_mat";
		
		System.out.println(msg + ";  dim = " + Sb.getRowDimension());
		MyMatrix JAVA_WM = Sb.calc_white_mat(debug);
		
		double [][] MATLAB_WM = MATLAB.calc_white_mat(Sb);
	
		compare_matrices(msg + ": \t", JAVA_WM, MATLAB_WM);
		
		return JAVA_WM;

	}
	/**
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	public static double compare_matrices(MyMatrix A, double[][] B) {
		MyMatrix diff = A.minus(new Matrix(B));
		
		
		double th=1E-8;
		
		int m = diff.getRowDimension();
		int n = diff.getColumnDimension();
		MyMatrix diffrelM = new MyMatrix(m, n);
		double [][]diffrel = diffrelM.getArray();
		
		
		for (int r=0; r<m; r++)
			for (int c = 0; c<n; c++)
				diffrel[r][c] = (diff.get(r, c) <= th)? 0: diff.get(r, c)/A.get(r,  c);
					
		double normInf = diffrelM.normInf();
		return normInf;		
	}
	
	public static double compare_matrices(String msg, MyMatrix Mjava, MyMatrix Mmatlab) {
		return compare_matrices(msg, Mjava, Mmatlab.getArray());
	}
	/**
	 * 
	 * @param msg
	 * @param A
	 * @param B
	 * @return
	 */
	public static double compare_matrices(String msg, MyMatrix Mjava, double[][] Mmatlab) {
		
		MyMatrix diff = Mjava.minus(new Matrix(Mmatlab)).abs();
		
		ArrayList<MyMatrix> maximax0 = diff.maximax(0);		
		double maxdiff = maximax0.get(0).get(0, 0);
		double rigaOfMax = maximax0.get(1).get(0,0);
		double colonnaOfMax = maximax0.get(2).get(0,0);
		
		double th=1E-8;
		
		int m = diff.getRowDimension();
		int n = diff.getColumnDimension();
		MyMatrix diffrelM = new MyMatrix(m, n);
		double [][]diffrel = diffrelM.getArray();
		
		for (int r=0; r<m; r++)
			for (int c = 0; c<n; c++)
			{
				double den = Mmatlab[r][c];
				double di = diff.get(r, c);
				double drel = Math.abs(di/den);
				if (den==0.0) {
					System.out.println("("+r+","+c+"): Mjava=" + Mjava.get(r,  c)+ " Mmatlab=" + Mmatlab[r][c]+ " diff="+di+" den="+den+ " drel="+drel);
				}
				//System.out.println("("+r+","+c+"): Mjava=" + Mjava.get(r,  c)+ " Mmatlab=" + Mmatlab[r][c]+ "di="+di+" den="+den+ " drel="+drel);
				
				//System.out.println("("+r+","+c+"): den="+den+ " drel="+drel);
				diffrel[r][c] = drel;
				//diffrel[r][c] = (diff.get(r, c) <= th)? 0: diff.get(r, c)/A.get(r,  c);
			}
					
		ArrayList<MyMatrix> maximax0Rel = diffrelM.maximax(0);		
		double maxDiffRel = maximax0Rel.get(0).get(0, 0);
		int rigaOfMaxRel = (int)maximax0Rel.get(1).get(0,0);
		int colonnaOfMaxRel = (int)maximax0Rel.get(2).get(0,0);
		
		
		double normInf = diffrelM.normInf();
		System.out.println(msg);
		System.out.println("\t max diff: " + maxdiff);
		System.out.println("\t max diff rel: " + maxDiffRel + " at (" +  rigaOfMaxRel + ", "+ colonnaOfMaxRel + ") ==> elements were: for Java : " + Mjava.get(rigaOfMaxRel, colonnaOfMaxRel) + " ; and for Matlab: " + Mmatlab[rigaOfMaxRel][colonnaOfMaxRel]);
		System.out.println("\t normInf(diffRel) = " + normInf);
		return normInf;

	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public static double [] imax (double [] v) {
		int imax = 0;
		double max = -1e16;
		for (int i=0;i<v.length; i++) {
			if (v[i]>max) {
				max = v[i];
				imax = i;
			}
		}
		double [] res = new double[2];
		res[0] = max; res[1] = (double)imax;
		return res;
	}
	
	/***************************************************
	 * UNIDIMENSIONAL ARRAYS COMPARISON
	 * *************************************************
	 */
	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double compare_vectors(String msg, double [] aJava, double [] bMatlab) {
		
		assert(aJava.length==bMatlab.length);
		double [] diff = new double[aJava.length];
		double [] diffrel = new double[aJava.length];
		double maxDiff = -Double.MAX_VALUE;
		double maxDiffRel = -Double.MAX_VALUE;
		
		for (int i=0; i<aJava.length; i++) {
			diff[i] = Math.abs(aJava[i] - bMatlab[i]);
			diffrel[i] = Math.abs(diff[i]/(aJava[i]+1E-10));
			
			maxDiff = diff[i]>maxDiff ? diff[i] : maxDiff;
			maxDiffRel = diffrel[i]>maxDiffRel ? diffrel[i] : maxDiffRel;
		}
		double normInf = MyMatrix.fromRowVector(diffrel).normInf();
		System.out.println(msg + " max diff = " + maxDiff + " ; max relative difference = max (e) = " + maxDiffRel + " ; normInf (e) = " + normInf);
		return normInf;

	}
	
	/**
	 * 
	 * @param a
	 * @param b
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
	
	
	
	
	
	
	
		/**
	 * 
	 * @throws Exception
	 */
	/*
	public static void testTVSpace() throws Exception {
		String bwfile = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea\\bwstats.jbw";
		String ubmfile = "C:\\Users\\FS\\Desktop\\tempFiles\\ubm\\ubm--s-uniform--NMIX-4.ubm";
		String TVfile = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea\\TVSpace.tvs";
		
		MyMatrix T = null;
		
		MixtureModel ubm = Mixture_MSR.readFromFile(new File(ubmfile)); //Mixture ubm = Mixture.readFromFile(new File(ubmfile));
		assert(ubm.diagonal());
		
		BWStatistics bw =  BWStatistics.readFromFile(bwfile);
		
		int tv_dim = 100; //200;
		int niter = 4;
		
		//int nworkers = 1;
		int nworkers = Runtime.getRuntime().availableProcessors(); // Not used at the moment
			
		//MyMatrix Tinit_for_debug = MyMatrix.randn(tv_dim, ubm.fd*ubm.nd, 1.0);
		//Tinit_for_debug.setVectorDim(VectorDim.COLUMN_VECTORS);
		// T = my_train_tv_space_N_F_UBM_Tinit(N, F, tv_dim, niter, nworkers, Tinit)
		MATLAB_TVSpace tv = new MATLAB_TVSpace();
		//tv.train_tv_space(bw, ubm, tv_dim, niter, nworkers, Tinit_for_debug); //Tinit_for_debug);
		tv.train_tv_space(bw, ubm, tv_dim, niter); //Tinit_for_debug);
		MyMatrix Tinit_for_debug = tv.getTinit();
		
		TVSpace_Models tvmodels = tv.getModels();
		T = tvmodels.T; //T = tv.getT();
		
		if (T.getVectorDim() == VectorDim.UNSPECIFIED)
			throw new Exception("Illegal vector_dim value");
		
		tvmodels.writeToFile(new File(TVfile));
		
		// Debug
		MatlabEngine eng = MATLAB.getMatlabConnection();
		//S = reshape(ubm.sigma, ndim * nmix, 1);
		double [] S = new double[ubm.fd*ubm.nd];
		for (int i=0; i<ubm.nd; i++) 
			System.arraycopy(ubm.components[i].cov, 0, S, i*ubm.fd, ubm.fd);
		double[][] matlab_T = eng.feval("my_train_tv_space_N_F_S_Tinit", bw.N.getArray(), bw.F.getArray(), S, tv_dim, ubm.nd, ubm.fd, niter, nworkers, Tinit_for_debug.getArray());
		MATLAB.closeSperecMatlabConnection(eng);
		
		System.out.println("Comparing against matlab: normInf(relative err) = " + compare_matrices(T, matlab_T) );
	}
	*/
	
	
	
	private static void compare_matlab_my_extract_ivector_helper(MyMatrix N, MyMatrix F, MyMatrix Ttransp, MyMatrix T_invS, int [] idx_sv, MyMatrix X) {
		// Adjust input params
		int [] idx_sv_ = new int [ idx_sv.length];
			for (int i = 0; i<idx_sv_.length; i++)
				idx_sv_[i] = idx_sv [i]+1;
				
		MatlabEngine eng = null;
		try {
			eng = MATLAB.getMatlabConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[][] matlab_T = null;
		try {
			matlab_T = eng.feval("my_extract_ivector_matrix_testJava", N.getArray(), F.getArray(), Ttransp.getArray(), T_invS.getArray(), idx_sv_);
		} catch (RejectedExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		MATLAB.closeSperecMatlabConnection(eng);
		TEST.compare_matrices("Comparing against matlab: my_extract_ivector_helper(...) = ", X, matlab_T);
		//------------------------------------------------------
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	/*public static void testPopref() throws Exception {
		
		String outFileFullPath = popRefOutFolder + File.separator + popRefOutFileName;

		// MAIN INPUT:
		SPEREC_Specs specs = new SPEREC_Specs();
		
		specs.UBM.NMIX = 16;
		specs.UBM.niter_ubm = 2; 
		specs.UBM.UBM_Initializer_Strategy = "uniform";
		
		specs.TV.TVDIM = 50;
		specs.TV.niter_tv = 4;
		
		specs.LDA.LDADIM = 50;
		
		specs.GPLDA.NPHI = 50;
		specs.GPLDA.niter_gplda = 4;
		
		String feaDir = "C:\\Users\\FS\\Desktop\\tempFiles\\ufv\\Monologhi_001_150_11025Hz--vad---i-0.015--of-2.0--fea-_1_";
		
		ConfigurationFile cfg = ConfigurationFile.load(feaDir + File.separator + "fea.cfg");
		String feaJson = cfg.getItem("FEA", "FeaSpecs");
		FeaSpecs feaSpecs = VadSpecs.fromJsonString(feaJson, FeaSpecs.class);
		
		int nsess = 3;
		
		//POPREF_MODEL POP = new POPREF_Builder().build(specs, feaSpecs, feaDir, "", nsess, true);
		
		double enrollSessionDurationSec = 25; // TODO
		String feaFileList = cfg.getItem("FEA", "feaFileList"); // Relative to the directory where is feaCfgFilePath
		feaFileList = feaDir + File.separator + feaFileList;
		
		ArrayList<MyMatrix> dataList = SessionsTable_MyMatrix.loadFeatures(feaDir, feaFileList, 1, VectorDim.COLUMN_VECTORS);
		int enrollSessionDurationFrames = (int)Math.round(enrollSessionDurationSec/ feaSpecs.fFrameIncrementSec);
		SessionsTable_MyMatrix feaSessionsTable = SessionsTable_MyMatrix.splitSpeakerLongSessionsByDuration(dataList, enrollSessionDurationFrames);
		
		POPREF_MODEL POP = new POPREF_Builder().build(specs, feaSpecs, feaSessionsTable, true);
		POP.writeToFile(new File(outFileFullPath));
	}
	*/
	
	
	
	
	
	
	/*private static void testBWstatsCollector_MSR () throws Exception {
		
		String ubmfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".ubmmsr";
		Mixture_MSR ubm = Mixture_MSR.readFromFile(new File(ubmfile));
		
		ArrayList<MyMatrix> dataList = load_aBook_Set4();

		BWStatistics BWstats_java = new BWstatsCollector_MSR("NF").collectBWstats(ubm, dataList, VectorDim.COLUMN_VECTORS); // I want statistics are column vector

		BWStatistics BWstats_matlab = MATLAB.compute_bw_stats_testJava(dataList, ubm);
		
		compare_matrices( " N: \t", BWstats_java.N, BWstats_matlab.N.transpose().getArray());
		compare_matrices( " F: \t", BWstats_java.F, BWstats_matlab.F.transpose().getArray());
		
		String bwfile = ubmOutFolder + File.separator + "ubm_msr_" + 4 + ".bwmsr";
		BWstats_java.writeToFile(bwfile);

	}
	*/
	
	/**
	 * 
	 * @throws Exception 
	 */
	/*public static void testBWstatsCollector() throws Exception {
		String fileList = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea\\jfealist.txt";
		String fileFolder = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea";
		Mixture ubm = Mixture.readFromFile(new File("C:\\Users\\FS\\Desktop\\tempFiles\\ubm\\ubm--s-uniform--NMIX-4.ubm"));
		String outFile = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea\\bwstats.jbw";
		
		BWStatistics bwstats = new BWstatsCollector().collectBWstats(ubm, fileFolder, fileList, 0, VectorDim.COLUMN_VECTORS); // I want statistics are column vector

		bwstats.writeToFile(outFile);
		
		//Test the I/O
		BWStatistics bwstats2 = BWStatistics.readFromFile(outFile);
		if (!bwstats.equals(bwstats2)) {
			double a = testDebug.compare_matrices(bwstats.N, bwstats2.N.getArray());
			double b = testDebug.compare_matrices(bwstats.F, bwstats2.F.getArray());
			assert(b==0);
			assert(a==0);
			double c=0;
		}
		//assert(bwstats.equals(bwstats2));
	}*/
	
	
			
	
	/**
	 * 
	 * @param inDir
	 * @param fileListFullPath
	 * @return
	 * @throws IOException
	 */
	public static LinkedList<LinkedList<double []>> collectSamplesBySpeakersFromFileList(String inDir, String fileListFullPath) throws IOException {
		LinkedList<LinkedList<double[]>> observationsBySpeaker = new LinkedList<LinkedList<double []>>();
		ChunkedDataSet set = new ChunkedDataSet(new File(fileListFullPath), inDir, 0); // 0==ufv
		ChunkedDataSet.Chunk chunk;
		while ((chunk = set.nextChunk()) != null) {
			List<double []> obs = new LinkedList<double []>();
			FrameInputStream r = chunk.getFrameReader();
			double [] buf = new double [r.getFrameSize()];
			while (r.read(buf))
				obs.add(buf.clone());
			observationsBySpeaker.add((LinkedList<double[]>) obs);
		}
		return observationsBySpeaker;
	}
	
	public static List<double []> collectSamplesFromFileList(String inDir, String fileListFullPath) throws IOException {
		List<double []> obs = new LinkedList<double []>();
		ChunkedDataSet set = new ChunkedDataSet(new File(fileListFullPath), inDir, 0); // o==ufv
		ChunkedDataSet.Chunk chunk;
		while ((chunk = set.nextChunk()) != null) {
			FrameInputStream r = chunk.getFrameReader();
			double [] buf = new double [r.getFrameSize()];
			while (r.read(buf))
				obs.add(buf.clone());
		}
		return obs;
	}
	
	/**
	 * Apply Expectation maximization to the initial Gaussian Mixture
	 * 
	 * Actual input are:
	 * - init: the file where the initial GMM was saved.
	 * - niter: the number of iterations
	 * - the fea data, which are stored in the files listed into fealist, in the folder 
	 * - update: the update strategy
	 * 
	 * Actual output is the Mixture that will be saved in the output file
	 * 
	 * @throws Exception 
	 * @throws IOException 
	 * 
	 */
	/*private static void test_JSTK_GaussEM() throws IOException, Exception {
		
		String init = "C:\\Users\\FS\\Desktop\\tempFiles\\ubm\\init--s-uniform--NMIX-4.ubm";
		String outfile = "C:\\Users\\FS\\Desktop\\tempFiles\\ubm\\ubm--s-uniform--NMIX-4.ubm";
		int niter = 2;
		String fealist = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea\\jfealist.txt";
		String feadir = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea";
		String update = "wmv";
		
		// Do the job
		String [] args = {"-i", init, "-o", outfile, "-n", Integer.toString(niter), "-l", fealist, "-d", feadir, "--update", update};
		GaussEM.main(args);
		
		// Read the output
		Mixture ubm = Mixture.readFromFile(new File(outfile));
	}*/
	
	/**
	 * Actual input parameters are:
	 * - feaDir: the directory where the feature files have been stored
	 * - the fea data that have been stored in the files listed into the jfeaList, in the folder feadDir
	 * - NMIX: the number of Gaussian densities composing the Mixture
	 * - strategy: the way the initial Mixture has to be initialized
	 * - outFileGmm: the file where the computed Mixture will be saved
	 * 
	 * Actual output is a Gaussian Mixture, which will be saved into a file
	 * 
	 * @throws Exception
	 */
	/*static void test_JSTK_Initializer() throws Exception {
		
		// Input
		String outfolder = "C:\\Users\\FS\\Desktop\\tempFiles\\ubm";
		String strategy = "uniform";
		String feaDir = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea";
		String feaList = feaDir + "\\" + "jfeaList.txt";
		int NMIX = 4; //"4";
		
		// Dump to a file the list of files with ".jfea" extension, contained in the folder feaDir.
		// because the fealist was not available.
		MiscUtils.dumpFileList(feaDir, null, feaList);
	    
	    String outFileGmmName = "init" + "--s-" + strategy + "--NMIX-" + Integer.toString(NMIX) + ".ubm";
	    
	    String outFileGmm = outfolder + "\\" + outFileGmmName;
	    
		String [] args = {"--dir", feaDir, "--list", feaList, "--gmm", outFileGmm, "-n", Integer.toString(NMIX), "-s", strategy };
		Initializer.main(args);
		
		// Read the output
		Mixture initial = Mixture.readFromFile(new File(outFileGmm));
	}*/
	
	/**
	 * 
	 * @param feaDir
	 * @param feaList
	 * @throws IOException
	 */
	/*public static void dumpFeaList(String feaDir, String feaList) throws IOException {
		
		File logFile = new File(feaList);
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
		File[] directoryListing = new File(feaDir).listFiles();
		String newline = System.getProperty("line.separator");
		if (directoryListing != null)
			for (File child : directoryListing)
				if (FilenameUtils.getExtension(child.getName()).equals("jfea"))
					writer.write(child.getName()+newline);
	    writer.close();
	}
	*/
	
	public static void testCov() {
		double [][] A = {{1, 3, 8, 10, 50}, {-1, -4, 10, 10, 30}, { -2, 1, 10, 15, -1}};
		MyMatrix C = new MyMatrix(A).cov();
	}
	
	public static void test_MyAccumulator_count_unique_stable() {
		double [] spk_count = new MyAccumulator().test_count_unique_stable();
	}
	
	public static void test_MySort_sort_and_replace() {
		MySort.test_sort_and_replace();
	}
	
	/**
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * 
	 */
	private static void testTarsosJVM_VAD () throws UnsupportedAudioFileException, IOException {
		
		// The only processor here (other than the writer)
    	MyAutocorrellatedVoiceActivityDetector voiceDetector = new MyAutocorrellatedVoiceActivityDetector(); // Using class AutocorrellatedVoiceActivityDetector

    	// Create the dispatcher
        AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), 2048, 1024); // are temporary values
        
    	// Configure the audioDispatcher
        double dSampleRate = audioDispatcher.getFormat().getFrameRate();
		double dReqFrameIncrementSec = 0.500; //
    	double dReqWindowSizeSec = 0.500 + voiceDetector.getMinimumVoiceActivityLength(1000f)/1000f;
    	double dOverlapSec = dReqWindowSizeSec - dReqFrameIncrementSec;
        int bufferOverlapForDispatcher = (int)Math.round(dOverlapSec*dSampleRate);
        int audioBufferSizeForDispatcher = (int)Math.round(dReqWindowSizeSec*dSampleRate);
        audioDispatcher.setStepSizeAndOverlap(audioBufferSizeForDispatcher, bufferOverlapForDispatcher);

        // Connect the processor
    	audioDispatcher.addAudioProcessor(voiceDetector);
    	
    	// Configure the wav writer
        String sOutAudioFilename = sAudioFilename + "_" + "testTarsosJVM_2" + ".wav";
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        WriterProcessor w = new WriterProcessor(audioDispatcher.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
        
        // Connect to the dispatcher
        audioDispatcher.addAudioProcessor(w);
        
        // Run the dispatcher in this thread
        audioDispatcher.run();
        
        // Run the dispatcher in another Thread
        //new Thread(audioDispatcher).start();
        
        // Create the 2nd dispatcher
        /*AudioDispatcher audioDispatcher2 = AudioDispatcherFactory.fromPipe(sOutAudioFullPath, (int) dSampleRate, audioBufferSizeForDispatcher, bufferOverlapForDispatcher);
        audioDispatcher2.run();*/        
	}
	
	/**
	 * Read the audiofile in a single operation, then write as another file using the TarsosDSP WriterProcessor.
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * 
	 */
	private static void testTarsosJVM_0 () throws UnsupportedAudioFileException, IOException {
		AudioDispatcher oAD;
		final AudioInputStream stream;
		WriterProcessor w;
		int dataSize;
		int audioBufferSizeForDispatcher;
		int bufferOverlapForDispatcher;
		String sOutAudioFilename;
		String sOutAudioFullPath;
		
		stream = AudioSystem.getAudioInputStream(new File(sAudioFullPath));
		dataSize = (int) stream.getFrameLength();
		audioBufferSizeForDispatcher = dataSize; // Number of audio samples 
        bufferOverlapForDispatcher = 0; //10240;
        oAD = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), audioBufferSizeForDispatcher, bufferOverlapForDispatcher);
        
        sOutAudioFilename = sAudioFilename + "_" + "testTarsosJVM_0" + ".wav";
    	sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        w = new WriterProcessor(oAD.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
        oAD.addAudioProcessor(w);
        
        oAD.run();
	}
	
	/**
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * 
	 */
	private static void testTarsosJVM_1 () throws UnsupportedAudioFileException, IOException {
		
		int audioBufferSizeForDispatcher = 20480; // Number of audio samples
        int bufferOverlapForDispatcher = 0; //10240;
        AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), audioBufferSizeForDispatcher, bufferOverlapForDispatcher);
        audioDispatcher.setZeroPadLastBuffer(false);
        
        String sOutAudioFilename = sAudioFilename + "_" + "testTarsosJVM_1" + ".wav";
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        WriterProcessor w = new WriterProcessor(audioDispatcher.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
        audioDispatcher.addAudioProcessor(w);
        
        audioDispatcher.run();
	}
	
	/**
	 * In a first thread, and AudioDispatcher read an audioFile and write audio chunks in a ringBuffer.
	 * A second thread read chunks from the same ringbuffer then write it as a secodn wav file.
	 * The first thread also write chunks to a wav file, in order to let the user make a comparison.
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private static void testProducerConsumerThread() throws UnsupportedAudioFileException, IOException, InterruptedException {
		System.out.println("testProducerConsumerThread: START");
		
		String sOutAudioFilename;
		String sOutAudioFullPath;
		
		MyRingBuffer ringBuffer = new MyRingBuffer(10000);

		// Create the dispatcher
        AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), 2048, 0); // are temporary values
        audioDispatcher.setZeroPadLastBuffer(false);
        
        sOutAudioFilename = sAudioFilename + "_" + "testTarsosJVM_NORING" + ".wav";
    	sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        WriterProcessor w0 = new WriterProcessor(audioDispatcher.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
        audioDispatcher.addAudioProcessor(w0);
        
        // Create the writer to the ringBuffer
        WriterToRingBuffer w1 = new WriterToRingBuffer(ringBuffer);
        // Add it to the 1st chain
        audioDispatcher.addAudioProcessor(w1);
        
        AudioDispatcher a2 = MyAudioDispatcherFactory.fromRingBuffer(ringBuffer, audioDispatcher.getFormat(), 1000, 0);
        a2.setZeroPadLastBuffer(false);
        
        sOutAudioFilename = sAudioFilename + "_" + "testTarsosJVM_RingBuffer" + ".wav";
    	sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        WriterProcessor w = new WriterProcessor(a2.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
        a2.addAudioProcessor(w);
              
        new Thread(audioDispatcher).start(); //Producer
        new Thread(a2).start(); //Consumer
        
        Thread.sleep(5000);
		System.out.println("testProducerConsumerThread: END (main thread)");
	}
	
	/**
	 * In a first thread, and AudioDispatcher read and audioFile and write audio chunks in a ringBuffer.
	 * A second thread read chunks from the same ringbuffer then write it as a second wav file.
	 * The first thread also write chunks to a wav file, in order to let the user make a comparison.
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private static void testProConVAD() throws UnsupportedAudioFileException, IOException, InterruptedException {
		System.out.println("testProConVAD: START");
		
		String sOutAudioFilename;
		String sOutAudioFullPath;
			
		//Specifications for the first chain
		double dReqFrameIncrementSec_x_VAD = 0.010; // 10 ms
		double dReqOverlapFactor_x_VAD = 2;
		double dReqWindowSizeSec_x_VAD = dReqOverlapFactor_x_VAD * dReqFrameIncrementSec_x_VAD; // 20 ms
		
		// ----------------------- 1. Create a first chain
		// a) Create AudioDispatcher and VAD
        AudioDispatcher oAD_1 = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), 2048, 0); // are temporary values
        double dSampleRate    = oAD_1.getFormat().getFrameRate(); // Store the sampleRate

     	MyAutocorrellatedVoiceActivityDetector voiceDetector = new MyAutocorrellatedVoiceActivityDetector(); // Using class AutocorrellatedVoiceActivityDetector
     	     
     	// Create the RingBuffer
     	MyRingBuffer ringBuffer = new MyRingBuffer();
     		
     	// Create the writer to the ringBuffer
        WriterToRingBuffer oWriterToRingBuffer = new WriterToRingBuffer(ringBuffer);
        
        // Connect the VAD
    	oAD_1.addAudioProcessor(voiceDetector);
    	// Connect the WriterToRingBuffer
        oAD_1.addAudioProcessor(oWriterToRingBuffer);
        
           
        //----- The second chain
        // Specs for the second chain 
        //     n   length of frame in samples [default power of 2 < (0.03*fs)]
        //     inc frame increment [default n/2]
        double dReqWindowSizeSec_x_MFCC      = 0.03; // 30 ms
        double dReqWindowSizeSamples_x_MFCC  = dReqWindowSizeSec_x_MFCC*dSampleRate;
        int    iReqWindowSizeSamples_x_MFCC  = (int)Math.pow(2, Math.floor(Math.log(dReqWindowSizeSamples_x_MFCC)/Math.log(2)));
        int    iReFrameIncrement_X_MFCC      = iReqWindowSizeSamples_x_MFCC/2;
        double dActualReqWindowSizeSec_x_MFCC = (double)iReqWindowSizeSamples_x_MFCC/dSampleRate; //unused
       
        // Create the 2nd AudioDispatcher that reads from the ringbuffer
        int bufferOverlapForDispatcher_2 = iReqWindowSizeSamples_x_MFCC - iReFrameIncrement_X_MFCC;
        AudioDispatcher oAD_2 = MyAudioDispatcherFactory.fromRingBuffer(ringBuffer, oAD_1.getFormat(), 1000, 0); // Temporary values
        oAD_2.setStepSizeAndOverlap(iReqWindowSizeSamples_x_MFCC, bufferOverlapForDispatcher_2);
        oAD_2.setZeroPadLastBuffer(false);

        // Create the WriteProcessor (in a later example, it will be replaced by the MFCC producer
        sOutAudioFilename = sAudioFilename + "_" + "testProConVAD" + ".wav";
    	sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        WriterProcessor w = new WriterProcessor(oAD_2.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
              
        // Write to file
        oAD_2.addAudioProcessor(w);
        
        
        // Before to start, configure the blocks
        // The first dispatcher
        int minNumSamples_x_VAD = voiceDetector.getMinimumVoiceActivityLength((float)dSampleRate);
     	double minWindowSizeSec_x_VAD = minNumSamples_x_VAD/dSampleRate;
     	dReqWindowSizeSec_x_VAD = Math.max(dReqWindowSizeSec_x_VAD, minWindowSizeSec_x_VAD);      	// Recompute the Window duration
        int audioBufferSizeForDispatcher_1 = (int)Math.ceil(dReqWindowSizeSec_x_VAD*dSampleRate);
     	double dOverlapSec = dReqWindowSizeSec_x_VAD - dReqFrameIncrementSec_x_VAD;
     	int bufferOverlapForDispatcher_1 = (int)Math.round(dOverlapSec*dSampleRate);
        oAD_1.setStepSizeAndOverlap(audioBufferSizeForDispatcher_1, bufferOverlapForDispatcher_1);
        oAD_1.setZeroPadLastBuffer(false);         
        
        // Configure the ringBufferSize
        ringBuffer.setCapacity(10*(int)Math.max(audioBufferSizeForDispatcher_1, iReqWindowSizeSamples_x_MFCC));
        
        new Thread(oAD_1).start(); //Producer
        new Thread(oAD_2).start(); //Consumer
        
        Thread.sleep(5000);
		System.out.println("testProConVAD: END (main thread)");
	}
	
	/**
	 * In a first thread, and AudioDispatcher read and audioFile and write audio chunks in a ringBuffer.
	 * A second thread read chunks from the same ringbuffer then compute MFCC , then write it as a file.
	 * The first thread also write chunks to a wav file, in order to let the user make a comparison.
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	/*private static void testProConVADMFCC() throws UnsupportedAudioFileException, IOException, InterruptedException {
		System.out.println("testProConVADMFCC: START");
		
		String sOutAudioFilename;
		String sOutAudioFullPath;
			
		//Specifications for the first chain
		double dReqFrameIncrementSec_x_VAD = 0.010; // 10 ms
		double dReqOverlapFactor_x_VAD = 2;
		double dReqWindowSizeSec_x_VAD = dReqOverlapFactor_x_VAD * dReqFrameIncrementSec_x_VAD; // 20 ms
		
		// ----------------------- 1. Create a first chain
		// a) Create AudioDispatcher and VAD
        AudioDispatcher oAD_1 = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), 2048, 0); // are temporary values
        double dSampleRate    = oAD_1.getFormat().getFrameRate(); // Store the sampleRate

     	MyAutocorrellatedVoiceActivityDetector voiceDetector = new MyAutocorrellatedVoiceActivityDetector(); // Using class AutocorrellatedVoiceActivityDetector
     	voiceDetector.setVerboseLog(false);
     	
     	// Create the RingBuffer
     	MyRingBuffer ringBuffer = new MyRingBuffer();
     		
     	// Create the writer to the ringBuffer
        WriterToRingBuffer oWriterToRingBuffer = new WriterToRingBuffer(ringBuffer);
        
        // Connect the VAD
    	oAD_1.addAudioProcessor(voiceDetector);
    	// Connect the WriterToRingBuffer
        oAD_1.addAudioProcessor(oWriterToRingBuffer);
        
           
        //----- The second chain
        // Specs for the second chain 
        //     n   length of frame in samples [default power of 2 < (0.03*fs)]
        //     inc frame increment [default n/2]
        double dReqWindowSizeSec_x_MFCC      = 0.03; // 30 ms
        double dReqWindowSizeSamples_x_MFCC  = dReqWindowSizeSec_x_MFCC*dSampleRate;
        int    iReqWindowSizeSamples_x_MFCC  = (int)Math.pow(2, Math.floor(Math.log(dReqWindowSizeSamples_x_MFCC)/Math.log(2)));
        int    iReFrameIncrement_X_MFCC      = iReqWindowSizeSamples_x_MFCC/2;
        double dActualReqWindowSizeSec_x_MFCC = (double)iReqWindowSizeSamples_x_MFCC/dSampleRate; //unused
       
        // Create the 2nd AudioDispatcher that reads from the ringbuffer
        int bufferOverlapForDispatcher_2 = iReqWindowSizeSamples_x_MFCC - iReFrameIncrement_X_MFCC;
        AudioDispatcher oAD_2 = MyAudioDispatcherFactory.fromRingBuffer(ringBuffer, oAD_1.getFormat(), 1000, 0); // Temporary values
        oAD_2.setStepSizeAndOverlap(iReqWindowSizeSamples_x_MFCC, bufferOverlapForDispatcher_2);
        oAD_2.setZeroPadLastBuffer(true); // Qui serve true perchè MFCC si aspetta quel numero di campioni e non di meno

        // MFCC specs
        int amountOfCepstrumCoef = 40;
        int amountOfMelFilters = 50;
        float lowerFilterFreq = 300;
        float upperFilterFreq = 3000;
        final MyMFCCfromTarsos oMyMFCCfromTarsos = new MyMFCCfromTarsos(
        		iReqWindowSizeSamples_x_MFCC, (float)dSampleRate, 
        		amountOfCepstrumCoef, amountOfMelFilters, 
        		lowerFilterFreq, upperFilterFreq);
        oAD_2.addAudioProcessor(oMyMFCCfromTarsos);
        
        
        // Create the WriteProcessor (in a later example, it will be replaced by the MFCC producer
        //sOutAudioFilename = sAudioFilename + "_" + "testProConVAD" + ".wav";
    	//sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        //WriterProcessor w = new WriterProcessor(oAD_2.getFormat(), new RandomAccessFile(sOutAudioFullPath, "rw"));
              
        // Write to file
        //oAD_2.addAudioProcessor(w);
        
        
        
        // Before to start, configure the blocks
        // The first dispatcher
        int minNumSamples_x_VAD = voiceDetector.getMinimumVoiceActivityLength((float)dSampleRate);
     	double minWindowSizeSec_x_VAD = minNumSamples_x_VAD/dSampleRate;
     	dReqWindowSizeSec_x_VAD = Math.max(dReqWindowSizeSec_x_VAD, minWindowSizeSec_x_VAD);      	// Recompute the Window duration
        int audioBufferSizeForDispatcher_1 = (int)Math.ceil(dReqWindowSizeSec_x_VAD*dSampleRate);
     	double dOverlapSec = dReqWindowSizeSec_x_VAD - dReqFrameIncrementSec_x_VAD;
     	int bufferOverlapForDispatcher_1 = (int)Math.round(dOverlapSec*dSampleRate);
        oAD_1.setStepSizeAndOverlap(audioBufferSizeForDispatcher_1, bufferOverlapForDispatcher_1);
        oAD_1.setZeroPadLastBuffer(false);         
        
        // Configure the ringBufferSize
        ringBuffer.setCapacity(10*(int)Math.max(audioBufferSizeForDispatcher_1, iReqWindowSizeSamples_x_MFCC));
        
        new Thread(oAD_1).start(); //Producer
        new Thread(oAD_2).start(); //Consumer
        
        if (!oMyMFCCfromTarsos.hasFinished()) {
        	System.out.println("testProConVADMFCC: WAITING FOR MFCC");
        	oMyMFCCfromTarsos.waitUntilFinished();
        	System.out.println("testProConVADMFCC: MFCC READY");
        }
        
        // Save as JSTK's FrameOutputStream
        sOutAudioFilename = sAudioFilename + "_" + "testProConVAD" + ".jfea";
    	sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
        FrameOutputStream oFOS = new FrameOutputStream(amountOfCepstrumCoef, new File(sOutAudioFullPath), true);
        for (int i=0; i<oMyMFCCfromTarsos.alMfcc.size(); i++) {
        	oFOS.write(oMyMFCCfromTarsos.alMfcc.get(i));
        }
        oFOS.close();
        
        
        Thread.sleep(5000);
		System.out.println("testProConVADMFCC: END (main thread)");
	}
	*/
	/**
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * @throws InterruptedException 
	 * 
	 */
	/*private static void testSperecAudioProcessorWithVAD() throws UnsupportedAudioFileException, IOException, InterruptedException {

		SPEREC_AudioProcessor oSAP = null;
		double dSampleRate = 0;
		VadSpecs vadSpecs = null;
		FeaSpecs feaSpecs = null;
		
		System.out.println("testSperecAudioProcessor: START");
		
		// 1) Set the audio source, then get the sample rate
		AudioDispatcher mainAudioDispatcher = AudioDispatcherFactory.fromFile(new File(sAudioFullPath), 2048, 0);
		dSampleRate = mainAudioDispatcher.getFormat().getSampleRate();
		oSAP = new SPEREC_AudioProcessor(mainAudioDispatcher); //new SPEREC_AudioProcessor(sAudioFullPath);
		//dSampleRate = oSAP.getSampleRate();
		
		// 2) VAD Specs
		vadSpecs = new VadSpecs();
		vadSpecs.setFrameIncrementSec(0.010);
		vadSpecs.setOverlapFactor(2.0);
		
		// 3) Fea Specs (requires the sample rate) , MFCC
        feaSpecs = FeaSpecs.defaults();
               
        String sOutAudioFilename = sAudioFilename + "_" + "testSperecAudioProcessor" + ".jfea";
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
		
        oSAP.setVad(vadSpecs);
        oSAP.setFea(feaSpecs);
		oSAP.run();
		
		oSAP.outputAsFile(sOutAudioFullPath);
		
		System.out.println("testSperecAudioProcessor: END (main thread)");
	}
	*/
	
	/**
	 * Just check if ArrayList<String>.clone is safe...
	 */
	static void testArrayListOfString_clone() {
		
		ArrayList<String> a1 = new ArrayList<String>();
		a1.add("Primo"); a1.add("Secondo");
		System.out.println("Orig: " + a1.toString());
		@SuppressWarnings("unchecked")
		ArrayList<String> a2 = (ArrayList<String>) a1.clone();
		System.out.println("Cloned: " + a2.toString());
		a2.set(0,  "Terzo");
		System.out.println("Modified cloned: " + a2.toString());
		System.out.println("Back to orginal: " + a1.toString());
	}
	
	/**
	 * Instantiate a SPEREC Engine from filesystem
	 * @return
	 * @throws Exception 
	 */
	static SPEREC testSperecLoader(String initFolder, String initFile) throws Exception {
		
		System.out.println("testSperecLoader: start");
		
		MATLAB_SPEREC_Factory fact = new MATLAB_SPEREC_Factory();
		
		// Ottobre 2018
		//MATLAB_SPEREC_Loader_JVM loader = fact.createLoader();
		SPEREC_Loader_JVM loader = new SPEREC_Loader_JVM();
		loader.setSperecFactory(fact);
				
		loader.init(initFolder);
		SPEREC oSPEREC = loader.load(initFile);
		System.out.println("testSperecLoader: end");
		return oSPEREC;
	}
	
	/**
	 * 
	 */
	static void testSortSelf() {
		System.out.println("testSortSelf: start");

		ArrayList<Boolean> alob = new ArrayList<Boolean>();
		alob.add(true);
		alob.add(false);
		alob.add(false);
		ArrayList<Double> alod = new ArrayList<Double>();
		alod.add(10.0);
		alod.add(2.5);
		alod.add(-4.0);
		System.out.println("Before sorting: \n\t" + alod.toString() + "\n\t" + alob.toString());
		MySort.sortself(alob, alod, MySort.SortDir.ASCENDING);
		System.out.println("After ASCENDING sorting: \n\t" + alod.toString() + "\n\t" + alob.toString());
		MySort.sortself(alob, alod, MySort.SortDir.DESCENDING);
		System.out.println("After DESCENDING sorting: \n\t" + alod.toString() + "\n\t" + alob.toString());
		
		System.out.println("testSortSelf: start");
	}
}
