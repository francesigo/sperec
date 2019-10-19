package test;

import java.io.File;
import java.util.ArrayList;

import app.Environment;
import app.FeaDataSet;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import sperec_common.AllSessionsArrays;
import sperec_common.BWStatistics;
import sperec_common.BWstatsCollector_MSR;
import sperec_common.LabeledArrayList;
import sperec_common.Mixture_MSR;
import sperec_common.StRecord;

public class TestBW {


	
	
	public static void main(String [] args) throws Exception {
		
		Environment en = new Environment();
		String f = "toSPLIT_toVAD_toFEA" + File.separator + "ufv" + File.separator+ "Monologhi_001_150_16000Hz--vad---i-0.1--of-2.0--fea-mode_M__nc_24__np_24__feadim_24" + File.separator + "fea.cfg";
		String inputDataPath = en.getDataDir() + File.separator + f;
		
		ArrayList<MyMatrix> feaSessions = loadFea(inputDataPath); 
		
		// Prima carico l'ubm
		String MAIN_OUTPUT_FOLDER_PATH = en.getMainOutputFolderPath();
		String ubmOutFolder = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ubm";
		String ubmfile = ubmOutFolder + File.separator + "ubm_8_5.ubm";
		Mixture_MSR ubm = Mixture_MSR.readFromFile(new File(ubmfile));
		
		// Poi uso l'UBM per ricavare le BW sul training set
		long tStart = System.currentTimeMillis();
		System.out.println("\nNow Start BW computation");
		BWstatsCollector_MSR BWcollector = new BWstatsCollector_MSR("NF");
		
		BWStatistics bw = BWcollector.collectBWstats(ubm, feaSessions, VectorDim.ROW_VECTORS); //	Baum-Welch, as row vectors
		TEST.showElapsedTime(tStart);
		
	}
	
	public static ArrayList<MyMatrix> loadFea(String feaCfgFile) throws Exception {
		//String feaCfgFile = "";  //Environment.getMainOutputFolderPath() + File.separator + f;
		System.out.println("Loading sample data from: " + feaCfgFile);
		long tStart = System.currentTimeMillis();
		FeaDataSet feaDataSet = FeaDataSet.fromConfigFile(feaCfgFile);

		TEST.showElapsedTime(tStart);
		System.out.println("\nNow Start computation");
		tStart = System.currentTimeMillis();

		// Qui raccolgo le sessions senza distinguere tra i parlanti (per l'UBM)
		ArrayList<LabeledArrayList<StRecord>> q = feaDataSet.getArrayListOfRecordSets();
		AllSessionsArrays<MyMatrix> a = FeaDataSet.toSessionsArrays(q); //STM.
		ArrayList<MyMatrix>feaSessions = a.data;
		
		return feaSessions;
		
	}
}
