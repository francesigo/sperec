package sperec_jvm;

import java.util.ArrayList;


import test.MATLAB_TVSpace;
import ex_common.MyLDA;
import sperec_common.BWStatistics;
import sperec_common.BWstatsCollector_MSR;
import sperec_common.FeaSpecs;
import sperec_common.GPLDA;
import sperec_common.GPLDA_Models;
import sperec_common.Mixture_MSR;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC_Specs;
import sperec_common.AllSessionsArrays;
import sperec_common.TVSpace;

/**
 * This class aims to create models for the reference population.
 * The input data are features of many speakers.
 * The input data are processed to compute models.
 * The models are stored in this class.
 * 
 * @author FS
 *
 */
public class POPREF_Builder {


	public Mixture_MSR ubm;
	public TVSpace tv;
	public MyLDA lda;
	public MyMatrix V_LDA;
	public BWStatistics bw;
	public MyMatrix IV;
	public MyMatrix IV_redux;
	
	public POPREF_Builder() {
		ubm = null;
		tv = null;
		lda = null;
		V_LDA = null;
		bw = null;
		IV = null;
		IV_redux = null;
	}
	
	
	/*public static POPREF_MODEL build(SPEREC_Specs specs, FeaSpecs feaSpecs) throws Exception {
		
		String feaDir = "C:\\Users\\FS\\Desktop\\tempFiles\\jfea";
		
		return build(specs, feaSpecs, feaDir);
		
	}
	*/
	
	
	/**
	 * Train UBM, Baum-Welch statistics, TVSpace and exract i-vectors.
	 * Everything goes in this
	 * @param specs
	 * @param feaSpecs
	 * @param a
	 * @param debug
	 * @throws Exception
	 */
	public void build_from_ubm_to_iv(SPEREC_Specs specs, FeaSpecs feaSpecs, AllSessionsArrays<MyMatrix> a) throws Exception {
		// Instance of the Universal Background Model: it is a Gaussian Mixture Model
		ubm = new Mixture_MSR();

		ArrayList<MyMatrix>feaSessions = a.data;
		//Short [] classes = a.classes;

		// Train the Universal Background Model
		ubm.train(feaSessions, specs.UBM.NMIX, specs.UBM.niter_ubm);

		//BWStatistics bw = new BWstatsCollector_MSR("NF").collectBWstats(ubm, feaDir, feaList, ufv, VectorDim.ROW_VECTORS);	
		bw = new BWstatsCollector_MSR("NF").collectBWstats(ubm, feaSessions, VectorDim.ROW_VECTORS); //	Baum-Welch, as row vectors

		//		TV Space training
		tv = (specs.SPEREC_Type.equals("MATLAB_SPEREC_UBM_IV_GPLDA")) ? new MATLAB_TVSpace() : new TVSpace();


		// Sigona July 2019 tv.train_tv_space(bw, ubm, specs.TV.TVDIM, specs.TV.niter_tv); //tv.train_tv_space(bw, ubm, TVDIM, niter_tv, null);
		bw.S = ubm.getSigmaDiagMatrix(VectorDim.ROW_VECTORS); //
		tv.train_tv_space(bw, specs.TV.TVDIM, specs.TV.niter_tv); //tv.train_tv_space(bw, ubm, TVDIM, niter_tv, null);

		IV = tv.extract_i_vectors_matrix(bw.N, bw.F);
		
	}
	
	//public POPREF_MODEL build(SPEREC_Specs specs, FeaSpecs feaSpecs, SessionsTable sessionsTable, boolean debug) throws Exception {
	/**
	 * Build the model of the reference population
	 * @param specs specifications for the models
	 * @param feaSpecs specifications for the features
	 * @param a the feature values, organized in sessions
	 * @param debug 
	 * @return
	 * @throws Exception
	 */
	public POPREF_MODEL build(SPEREC_Specs specs, FeaSpecs feaSpecs, AllSessionsArrays<MyMatrix> a, boolean debug) throws Exception {
		
		/*
		// Instance of the Universal Background Model: it is a Gaussian Mixture Model
		ubm = new Mixture_MSR();

		ArrayList<MyMatrix>feaSessions = a.data;
		Short [] classes = a.classes;
		
		// Train the Universal Background Model
		ubm.train(feaSessions, specs.UBM.NMIX, specs.UBM.niter_ubm);

		//BWStatistics bw = new BWstatsCollector_MSR("NF").collectBWstats(ubm, feaDir, feaList, ufv, VectorDim.ROW_VECTORS);	
		bw = new BWstatsCollector_MSR("NF").collectBWstats(ubm, feaSessions, VectorDim.ROW_VECTORS); //	Baum-Welch, as row vectors

		//		TV Space training
		tv = (specs.SPEREC_Type.equals("MATLAB_SPEREC_UBM_IV_GPLDA")) ? new MATLAB_TVSpace() : new TVSpace();

		
		// Sigona July 2019 tv.train_tv_space(bw, ubm, specs.TV.TVDIM, specs.TV.niter_tv); //tv.train_tv_space(bw, ubm, TVDIM, niter_tv, null);
		bw.S = ubm.getSigmaDiagMatrix(VectorDim.ROW_VECTORS); //
		tv.train_tv_space(bw, specs.TV.TVDIM, specs.TV.niter_tv); //tv.train_tv_space(bw, ubm, TVDIM, niter_tv, null);
		
		IV = tv.extract_i_vectors_matrix(bw.N, bw.F);
		*/
		
		Short [] classes = a.classes;

		build_from_ubm_to_iv(specs, feaSpecs, a);

		// ---------------------------------- LDA start		
		int nobs = IV.getColumnDimension();
		int fd = IV.getRowDimension(); // [fd x nObs]

		// Centering the data //data = bsxfun(@minus, data, mean(data, 2)); % centering the data
		MyMatrix IV_mean_centered = IV.minusColumnVector(IV.mean(2));

		lda = new MyLDA(fd);
		//lda.debug(debug);
		
		//short [] classes = MiscUtils.simulate_classes(nobs, nsess);
		
		lda.estimate(classes, IV_mean_centered);
		V_LDA = new MyMatrix(lda.getProjection());
		// ---------------------------------- LDA end

		// ------------------------------------------------- i-vector LDA redux start

		//dev_ivs = V(:, 1 : lda_dim)' * dev_ivs;
		
		int actual_lda_dim = 0;
		if ( V_LDA.getColumnDimension() < specs.LDA.LDADIM) 
		{
			System.out.println("WARNING LDA DIM > V_LDA dimension " + V_LDA.getColumnDimension());
			actual_lda_dim = V_LDA.getColumnDimension();
		}
		else
			actual_lda_dim = specs.LDA.LDADIM;
		
		MyMatrix V_LDA_redux = V_LDA.getMatrix(0, V_LDA.getRowDimension()-1, 0, actual_lda_dim-1);

		// According to the MSR code, these IV vectors must not mean-centered
		IV_redux = V_LDA_redux.transpose().times(IV);
		IV_redux.setVectorDim(VectorDim.COLUMN_VECTORS);


		// ------------------------------------------------- i-vector LDA redux end

		// --------------------------------- GPLDA Start
		int actual_nphi = 0;
		if (actual_lda_dim < specs.GPLDA.NPHI)
		{
			System.out.println("WARNING: NPHI > LDA_DIM" + actual_lda_dim);
			actual_nphi = actual_lda_dim;
		}
		else
			actual_nphi = specs.GPLDA.NPHI;
		
		GPLDA_Models gplda_models = new GPLDA(actual_nphi).estimate(IV_redux, classes, specs.GPLDA.niter_gplda);
		// --------------------------------- GPLDA end

		POPREF_MODEL POP = new POPREF_MODEL();
		POP.gplda_models = gplda_models;
		POP.tv = tv.getModels();
		POP.ubm = ubm;
		POP.V_LDA_redux = V_LDA_redux;
		POP.gplda = new GPLDA(gplda_models); // Re-create GPLDA on the computed gplda models

		return POP;

	}
	
	
	/**
	 * 
	 * Create models for a reference population.
	 * @param specs: the specification for the reference population models
	 * @param feaSpecs: the specifications of the features.
	 * @param feaDir: the folder where the features are stored
	 * @param feaList: a file with the list of the features file to be used. If empty, a new list will be created with all the feature files present in the feaDir folder.
	 * @param nsess:
	 * @param debug
	 * @return
	 * @throws Exception
	 */
	/*private POPREF_MODEL build(SPEREC_Specs specs, FeaSpecs feaSpecs, String feaDir, String feaList, int nsess, boolean debug) throws Exception {
		

		// If no feaList file was provided, then take all the feature files
		if ((null==feaList) || feaList.isEmpty())
		{
			feaList = feaDir + "\\" + "ufvList.txt";
			MiscUtils.dumpFileList(feaDir, feaList, "ufv"); // Dump the fea files list
		}
				
		/*
		// Other Initializer Input
		String ubm_outfolder = System.getProperty("java.io.tmpdir"); //"C:\\Users\\FS\\Desktop\\tempFiles\\ubm";
		
		// ----------------------------------  Initializer Start
		String UBM_Initializer_Strategy = specs.UBM.UBM_Initializer_Strategy;
		int ufv = feaSpecs.getFeatureDimension(); //iAmountOfCepstrumCoef;
		String outFileGmmName = "init" + "--s-" + UBM_Initializer_Strategy + "--NMIX-" + Integer.toString(NMIX) + ".ubm";
    	String initial_GMM_File = ubm_outfolder + File.separator + outFileGmmName;
		{
	    	String [] args = {"--dir", feaDir, "--list", feaList, "--ufv", Integer.toString(ufv), "--gmm", initial_GMM_File, "-n", Integer.toString(NMIX), "-s", UBM_Initializer_Strategy };
			Initializer.main(args);
		}
		// ---------------------------------- Initializer End
		
		// ---------------------------------- GaussEM start
		String final_GMM_File = ubm_outfolder + "\\ubm--s-uniform--NMIX-"+NMIX+".ubm";
		{
			String update = "wmv";
			String [] args = {"-i", initial_GMM_File, "-o", final_GMM_File, "-n", Integer.toString(niter_ubm), "-l", feaList, "-d", feaDir, "--update", update};
			GaussEM.main(args);
		}
		// Read the output
		ubm = MixtureModel.readFromFile(new File(final_GMM_File));
		assert(ubm.diagonal());
		// ---------------------------------- GaussEM end
		
		
		
		ArrayList<MyMatrix> dataList = MiscUtils.loadFeatures(feaDir, feaList, 1, VectorDim.COLUMN_VECTORS);
		
		POPREF_MODEL POP = build(specs, feaSpecs, dataList, nsess, debug);
		
		return POP;
				
	}*/
	
}
