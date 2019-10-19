package test;

import java.io.File;
import java.util.ArrayList;

import app.Environment;
import guiutils.ChooseFile;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import sperec_common.BWStatistics;
import sperec_common.ConfigurationFile;
import sperec_common.FeaSpecs;
import sperec_common.GPLDA_Models;
import sperec_common.MiscUtils;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC_Specs;
import sperec_common.SessionsTable_MyMatrix;
import sperec_jvm.POPREF_Builder;

public class TestPopRef2 {
	/**
	 * The main function to call is: POPREF_MODEL POPREF_Builder.build(SPEREC_Specs specs, FeaSpecs feaSpecs, String feaDir, String feaList) throws Exception {
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
				
		String MAIN_OUTPUT_FOLDER_PATH = Environment.getMainOutputFolderPath();
		String cfgFilePath = MAIN_OUTPUT_FOLDER_PATH + File.separator + "popref" + File.separator + "prova.cfg";
		
		// Load configuration
		ConfigurationFile cfg = ConfigurationFile.load(cfgFilePath);
		
		// Get the specs from the configuration
		SPEREC_Specs sperecSpecs = SPEREC_Specs.fromJsonString(cfg.getItem("SPEREC", "SperecSpecs"));
		String feaCfgFilePath = cfg.getItem("FEA", "feaCfgFile");
		
		// Import the configuration of the new selected fea and set the datasource name
		if (!new File(feaCfgFilePath).exists())
			feaCfgFilePath = ChooseFile.get(MAIN_OUTPUT_FOLDER_PATH, "Select the Feature Configuration File: " + new File(feaCfgFilePath).getName(), "(*.cfg)", "cfg");
		
		String feaDir = new File(feaCfgFilePath).getParent();
		ConfigurationFile feaCfg = ConfigurationFile.load(feaCfgFilePath);
		// String vadJson = feaCfg.getItem("VAD", "VadSpecs"); USELESS HERE
		//VadSpecs vadSpecs = VadSpecs.fromJsonString(vadJson, VadSpecs.class);
		String feaJson = feaCfg.getItem("FEA", "FeaSpecs");
		FeaSpecs feaSpecs = FeaSpecs.fromJsonString(feaJson, FeaSpecs.class);
		
		String feaFileList = feaCfg.getItem("FEA", "feaFileList"); // Relative to the directory where is feaCfgFilePath
		feaFileList = feaDir + File.separator + feaFileList;
		
		if (sperecSpecs.SPEREC_Type.equals("SPEREC_UBM_IV_GPLDA"))
			sperecSpecs.SPEREC_Type = "MATLAB_SPEREC_UBM_IV_GPLDA";
		
		ArrayList<MyMatrix> dataList = SessionsTable_MyMatrix.loadFeatures(feaDir, feaFileList, 1, VectorDim.COLUMN_VECTORS);
		
		int nsess = 3; // Number of sessions for each speaker
		
		boolean debug = true;
		
		POPREF_Builder popBuilder = new POPREF_Builder();
		
		double enrollSessionDurationSec = 25;
		int enrollSessionDurationFrames = (int)Math.round(enrollSessionDurationSec/ feaSpecs.fFrameIncrementSec);
		SessionsTable_MyMatrix feaSessionsTable = SessionsTable_MyMatrix.splitSpeakerLongSessionsByDuration(dataList, enrollSessionDurationFrames);

		POPREF_MODEL POP_Java = popBuilder.build(sperecSpecs, feaSpecs, feaSessionsTable, debug);
				
		MATLAB_TVSpace tvSpace = (MATLAB_TVSpace)popBuilder.tv;
		MyMatrix tInit = tvSpace.getTinit();
		
		MATLAB_POPREF_MODEL POP_Matlab = MATLAB.popref_testjava(sperecSpecs, feaSpecs, feaDir, feaFileList, nsess, tInit, debug);
		
		TEST.compare_matrices("UBM.MU: ", POP_Java.ubm.getMuMatrix(VectorDim.COLUMN_VECTORS), POP_Matlab.ubm.getMuMatrix(VectorDim.COLUMN_VECTORS));
		TEST.compare_matrices("UBM.SIGMA: ", POP_Java.ubm.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS), POP_Matlab.ubm.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS));
		TEST.compare_matrices("UBM.w: ", POP_Java.ubm.getWMatrix(VectorDim.COLUMN_VECTORS), POP_Matlab.ubm.getWMatrix(VectorDim.COLUMN_VECTORS));
		
		BWStatistics bw = popBuilder.bw;
		TEST.compare_matrices("BW.N: ", bw.N, POP_Matlab.N);
		TEST.compare_matrices("BW.F: ", bw.F, POP_Matlab.F);
		
		TEST.compare_matrices("T: ", POP_Java.tv.T, POP_Matlab.tv.T);
		TEST.compare_matrices("TinvS: ", POP_Java.tv.T_invS, POP_Matlab.tv.T_invS);
		
		
		//MyMatrix matlab_T = MATLAB.testTV_testJava(POP_Java.ubm, bw, tInit, sperecSpecs.TV.niter_tv, 2);
		//TEST.compare_matrices("T SECONDA PROVA: ", POP_Java.tv.T, matlab_T);

		
		TEST.compare_vectors("idx_sv", POP_Java.tv.idx_sv, POP_Matlab.tv.idx_sv);
		
		TEST.compare_matrices("dev_ivs: \t", popBuilder.IV, POP_Matlab.dev_ivs);
		
		TEST.compare_matrices("V_LDA_redux:\t", POP_Java.V_LDA_redux, POP_Matlab.V_LDA_redux);
		
		TEST.compare_matrices("IV_redux:\t", popBuilder.IV_redux, POP_Matlab.dev_ivs_redux);
		
		GPLDA_Models g = POP_Java.gplda_models;
		GPLDA_Models gMatlab = POP_Matlab.gplda_models;
		TEST.compare_matrices("Comparing against matlab: Phi:\t\t", g.Phi, gMatlab.Phi);
		TEST.compare_matrices("Comparing against matlab: Sigma:\t\t", g.Sigma, gMatlab.Sigma);
		TEST.compare_vectors("Comparing against matlab: M:\t\t", g.Mcol,gMatlab.Mcol);
		TEST.compare_matrices("Comparing against matlab: W:\t\t", g.W, gMatlab.W);

		TEST.compare_vectors("Comparing against matlab: Lambda:\t", g.Lambda.getDiag(), gMatlab.Lambda.getDiag());
		TEST.compare_matrices("Comparing against matlab: Uk:\t\t", g.Uk, gMatlab.Uk);
		TEST.compare_matrices("Comparing against matlab: Q_hat:\t", g.Q_hat, gMatlab.Q_hat);

	}

}
