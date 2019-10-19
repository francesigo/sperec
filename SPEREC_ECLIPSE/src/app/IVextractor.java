package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.swing.JTextArea;

import guiutils.ChooseFile;
import guiutils.SaveFile;
import myIO.IOClass;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import sperec_common.AllSessionsArrays;
import sperec_common.BWStatistics;
import sperec_common.BWstatsCollector_MSR;
import sperec_common.ConfigurationFile;
import sperec_common.FeaSpecs;
import sperec_common.LabeledArrayList;
import sperec_common.LabeledArrayListOfSpeakers;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC;
import sperec_common.SPEREC_Factory;
import sperec_common.SPEREC_Specs;
import sperec_common.SessionsTable_MyMatrix;
import sperec_common.StRecord;
import sperec_common.TVSpace;
import sperec_common.VadSpecs;
import sperec_jvm.SPEREC_Loader_JVM;

public class IVextractor {

	static final String newline = "\n";


	/*
	 * A JtextArea where to print some messages. At the moment is not used
	 */
	static JTextArea output = null;

	SPEREC oSperec = null;
	SPEREC_Specs specs = null;
	FeaSpecs feaSpecs = null;
	POPREF_MODEL pop = null;

	AllSessionsArrays<MyMatrix> allsessions = null; // The required data


	public static void main(String [] argv) throws Exception {


		IVextractor me = new IVextractor();

		// Load the data
		me.load_data();
		if (me.allsessions==null)
			return;

		// Load the popref
		me.load_popref();
		if (me.oSperec==null)
			return;

		//POPREF_Builder pb = new POPREF_Builder();
		//pb.build_from_ubm_to_iv(me.specs, me.feaSpecs, me.allsessions);
		Short[] classes = me.allsessions.classes;
		
		// i-vectors extraction
		// a. Baum-Welch
		BWStatistics bw = new BWstatsCollector_MSR("NF").collectBWstats(me.pop.ubm, me.allsessions.data, VectorDim.ROW_VECTORS);
		// b. i-vectors
		TVSpace tvSpace = new TVSpace();
		tvSpace.setModels(me.pop.tv);
		MyMatrix IV = tvSpace.extract_i_vectors_matrix(bw.N, bw.F); // Can throw an Exception
		
		// Saving
		String outfile_iv = SaveFile.as("", "Save i-vectors matrix as:", "ufv");
		IV.writeToUfvFile(outfile_iv);
		
		String outfile_class = SaveFile.as(outfile_iv, "Save class file as:", "label");
		File file = new File(outfile_class);
		OutputStream os = (file == null ? System.out : new FileOutputStream(file));
		IOClass.writeShortArray(os, classes, ByteOrder.LITTLE_ENDIAN);

		outputToUser("Successfully saved " + IV.getColumnDimension() + " i-vectors and class labels");
	}

	/**
	 * Load the required data
	 * @return
	 * @throws Exception
	 */
	void load_data() throws Exception {

		String feaCfgFilePath = "";

		// 2. Get the feature specifications
		feaCfgFilePath = ChooseFile.get(feaCfgFilePath, "Select the Feature Configuration File", "(.cfg)", "cfg");
		if ( (feaCfgFilePath==null)  || (feaCfgFilePath.equals("")))
			return;
		
		feaSpecs = getFeaSpecs(feaCfgFilePath);

		// Get the feature dataset
		FeaDataSet cleanFeaDataset = FeaDataSet.fromConfigFile(feaCfgFilePath);

		// Get as array list of recordsets
		ArrayList<LabeledArrayList<StRecord>> sessions = cleanFeaDataset.getArrayListOfRecordSets(); // sotto forma di array
		// Now make the chunks for enrollment

		//int enrollSessionDurationFrames = (int)Math.round(specs.enrollSessionDurationSec/ feaSpecs.fFrameIncrementSec);

		SessionsTable_MyMatrix STM = new SessionsTable_MyMatrix();
		int chunkMode = 1;
		LabeledArrayListOfSpeakers<MyMatrix> speakersChunks = STM.makeChunks(sessions, Integer.MAX_VALUE, chunkMode);

		outputToUser("FOUND " + speakersChunks.size() + " speaker available for reference population computation");
		AllSessionsArrays<MyMatrix> a = STM.toSessionsArrays(speakersChunks);

		this.allsessions = a;
	}
	/**
	 * 
	 * @throws Exception
	 */
	void load_popref() throws Exception {

		String MAIN_OUTPUT_FOLDER_PATH = "";
		String cfgString;
		final String newline = "\n";


		// Select the POPREF
		String popRefCfgPath = ChooseFile.get(MAIN_OUTPUT_FOLDER_PATH, "Choose the file of the reference population configuration", "(*.cfg)", "cfg");

		// Load the POPREF usign the loader
		SPEREC_Loader_JVM loader = new SPEREC_Loader_JVM();
		loader.init(new File(popRefCfgPath).getParent()); // useless for load_pop
		SPEREC_Factory factory = new SPEREC_Factory();
		loader.setSperecFactory(factory);
		pop = loader.load_pop(popRefCfgPath);
		oSperec = loader.getSperec();
		specs = loader.getSperecSpecs();

	}

	/**
	 * Get the feature specification
	 * @param feaCfgFilePath
	 * @return
	 * @throws IOException
	 */
	static FeaSpecs getFeaSpecs(String feaCfgFilePath) throws IOException {
		//feaDir = new File(feaCfgFilePath).getParent();

		// Import the configuration of the new selected fea and set the datasource name
		ConfigurationFile feaCfg = ConfigurationFile.load(feaCfgFilePath);
		//String vadJson = feaCfg.getItem("VAD", "VadSpecs"); UNUSED
		//VadSpecs vadSpecs = VadSpecs.fromJsonString(vadJson, VadSpecs.class);
		String feaJson = feaCfg.getItem("FEA", "FeaSpecs");
		FeaSpecs feaSpecs = FeaSpecs.fromJsonString(feaJson, FeaSpecs.class);
		//dataSource = feaSpecs.dataSource;
		//String feaFileList = feaCfg.getItem("FEA", "feaFileList"); // Relative to the directory where is feaCfgFilePath
		//feaFileList = feaDir + File.separator + feaFileList;

		if (feaSpecs!=null)
		{
			//cfg.addItem("FEA", "FeaSpecs", feaJson);
			outputToUser(newline + "Feature specs: " + newline + feaSpecs.toPrettyJsonString() + newline);

			String vadJson = feaCfg.getItem("VAD",  "VadSpecs");
			VadSpecs vadSpecs = VadSpecs.fromJsonString(vadJson); 
			//cfg.addItem("VAD", "VadSpecs", vadJson);
			if (vadSpecs!= null)
				outputToUser(newline + "Voice Activity Detection specs: " + newline + vadSpecs.toPrettyJsonString() + newline);
		}
		return feaSpecs;
	}

	/**
	 * Display a message for the user
	 * @param msg
	 */
	static void outputToUser(String msg) {
		if (output!=null)
		{
			output.append(msg);
			output.setCaretPosition(output.getDocument().getLength());
		}
		System.out.println(msg);
	}

}
