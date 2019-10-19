package app;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import guiutils.ChooseConfigurationFile;
import guiutils.ChooseFile;
import guiutils.InputSperecSpecs;
import guiutils.SaveFile;
import myMath.MyMatrix;
import sperec_common.ConfigurationFile;
import sperec_common.FeaSpecs;
import sperec_common.LabeledArrayList;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC_Specs;
import sperec_common.SessionsTable_MyMatrix;
import sperec_common.Specs;
import sperec_common.LabeledArrayListOfSpeakers;
import sperec_common.AllSessionsArrays;
import sperec_common.StRecord;
import sperec_common.VadSpecs;
import sperec_jvm.POPREF_Builder;

public class NewPopRef {

	/*
	 * A JtextArea where to print some messages. At the moment is not used
	 */
	JTextArea output = null;
	
	/*
	 * The base path where to put files. At the moment is empty
	 */
	String MAIN_OUTPUT_FOLDER_PATH = ""; //Environment.getMainOutputFolderPath();

	
	static final String newline = "\n";

	/*
	 * GUI object to input the sperec specifications
	 */
	InputSperecSpecs inputSperecSpecs = null;
	
	/*
	 * The current sperec specifications
	 */
	SPEREC_Specs specs = null;
	
	/*
	 * The current configuration file object
	 */
	ConfigurationFile cfg = null;
	
	/*
	 * The current path of the configuration file for the features
	 */
	String feaCfgFilePath = "";

	Container createContentPane() {
		//Create the content-pane-to-be.
		JPanel contentPane = new JPanel(new BorderLayout());
		JScrollPane scrollPane;

		contentPane.setOpaque(true);

		//Create a scrolled text area.
		output = new JTextArea(15, 100);
		output.setEditable(false);
		scrollPane = new JScrollPane(output);

		//Add the text area to the content pane.
		contentPane.add(scrollPane, BorderLayout.CENTER);

		return contentPane;
	}
	
	private void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("NEW REFERENCE POPULATION MODELS"); //("SPEREC - POPREF Model Builder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		//SperecLab demo = new SperecLab();
		//frame.setJMenuBar(demo.createMenuBar());
		frame.setContentPane(createContentPane());

		//Display the window.
		frame.setSize(450, 260);
		frame.setVisible(true);
	}
	
	/**
	 * Stand-alone main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		
		NewPopRef me = new NewPopRef();
		me.createAndShowGUI();
		try {
			me.work();
		}
		catch (Exception e)
		{
			String msg = "ERROR" + newline + e.getMessage();
			me.outputToUser(newline + msg + newline);
			throw e;
		}
	}
	
	/**
	 * Get the sperc specs. The user can provide also a cross validation specs.
	 * @throws IOException
	 */
	public void getSperecSpecs() throws IOException {
		
		inputSperecSpecs = new InputSperecSpecs();
		cfg = ChooseConfigurationFile.get("", inputSperecSpecs.humanReadableName);
		if  ((cfg==null) || (cfg.cfgFilePath.equals("")) )
		{
			specs = inputSperecSpecs.getFromGUI(null);
			if (specs==null)
				return;
		}
		else
		{
			//cfg = inputSperecSpecs.getConfigurationFile();
			if (cfg.hasSection(inputSperecSpecs.cfgSectionName))
			{
				specs = Specs.fromJsonString(cfg.getItem(inputSperecSpecs.cfgSectionName, inputSperecSpecs.cfgItemName), SPEREC_Specs.class);
				inputSperecSpecs = new InputSperecSpecs(specs);
				feaCfgFilePath = cfg.getItem("FEA", "feaCfgFile");
			}
			else
			{
				InputCrossValidationSpecs inputCrossValidationSpecs = new InputCrossValidationSpecs();
				if (cfg.hasSection(inputCrossValidationSpecs.cfgSectionName)) 
				{
					CrossValidationSpecs cvSpecs = Specs.fromJsonString(cfg.getItem(inputCrossValidationSpecs.cfgSectionName, inputCrossValidationSpecs.cfgItemName), CrossValidationSpecs.class);
					inputSperecSpecs = new InputSperecSpecs(cvSpecs.specs);
					feaCfgFilePath = cvSpecs.cleanFeaCfgFile;
				}
			}
		}
		specs = inputSperecSpecs.getSpecs();

		// For the user
		if (specs!=null)
			outputToUser("Sperec specs:" + newline + specs.toPrettyJsonString() + newline);
	}
	
	/**
	 * Get the feature specification
	 * @param feaCfgFilePath
	 * @return
	 * @throws IOException
	 */
	FeaSpecs getFeaSpecs(String feaCfgFilePath) throws IOException {
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
			cfg.addItem("FEA", "FeaSpecs", feaJson);
			outputToUser(newline + "Feature specs: " + newline + feaSpecs.toPrettyJsonString() + newline);

			String vadJson = feaCfg.getItem("VAD",  "VadSpecs");
			VadSpecs vadSpecs = VadSpecs.fromJsonString(vadJson); 
			cfg.addItem("VAD", "VadSpecs", vadJson);
			outputToUser(newline + "Voice Activity Detection specs: " + newline + vadSpecs.toPrettyJsonString() + newline);
	    }
		return feaSpecs;
	}
	
	/**
	 * Save the current configuration
	 * @param popRefOutFolder
	 * @param popRefOutFileName
	 * @param baseOutName
	 * @param feaSpecs
	 * @throws IOException
	 */
	String saveCfg(String popRefOutFolder, String popRefOutFileName, String baseOutName, FeaSpecs feaSpecs) throws IOException {
		ConfigurationFile newCfg = new ConfigurationFile();

		newCfg.addSection(inputSperecSpecs.cfgSectionName);
		newCfg.addItem(inputSperecSpecs.cfgSectionName, inputSperecSpecs.cfgItemName, specs.toJsonString());
		newCfg.addItem(inputSperecSpecs.cfgSectionName,  "popRefFileName", popRefOutFileName);
		newCfg.addItem(inputSperecSpecs.cfgSectionName,  "dataSource", feaSpecs.dataSource);

		newCfg.addSection("FEA");
		newCfg.addItem("FEA", "FeaSpecs", cfg.getItem("FEA",  "FeaSpecs"));
		newCfg.addItem("FEA", "feaCfgFile", feaCfgFilePath);

		newCfg.addSection("VAD");
		newCfg.addItem("VAD", "VadSpecs", cfg.getItem("VAD", "VadSpecs"));

		// Save the reference population configuration file
		return newCfg.saveAs(popRefOutFolder, baseOutName + ".cfg");

	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void work() throws Exception {

		String popRefOutFolder = MAIN_OUTPUT_FOLDER_PATH; // Default, to be changed at run-time
		String popRefOutFileName = "";
		
		// 1. Get the specs specifications and other settings
		getSperecSpecs();
		if (specs==null)
			return;
				
		// 2. Get the feature specifications
		feaCfgFilePath = ChooseFile.get(feaCfgFilePath, "Select the Feature Configuration File", "(.cfg)", "cfg");
		if ( (feaCfgFilePath==null)  || (feaCfgFilePath.equals("")))
			return;
		FeaSpecs feaSpecs = getFeaSpecs(feaCfgFilePath);

		// ------------------------------ Output
		// 3. Set output locations	
		String popRefFullPath = SaveFile.as(cfg.cfgFilePath, "Save reference popoulation as", "pop");
		if ((popRefFullPath==null) || popRefFullPath.equals(""))
			return;
		File tempF = new File(popRefFullPath);
		popRefOutFolder = tempF.getParent().toString();
		popRefOutFileName = tempF.getName();
		String baseOutName = popRefOutFileName.substring(0, popRefOutFileName.lastIndexOf("."));

		// 4. Save the current configuration
		String newCfgFullPath = saveCfg(popRefOutFolder, popRefOutFileName, baseOutName, feaSpecs);

	
		// 5. ---------------------------------------- Do the job

		// Get the feature dataset
		FeaDataSet cleanFeaDataset = FeaDataSet.fromConfigFile(feaCfgFilePath);
		
		// Get as array list of recordsets
		ArrayList<LabeledArrayList<StRecord>> enrollmentSessions = cleanFeaDataset.getArrayListOfRecordSets(); // sotto forma di array
		// Now make the chunks for enrollment
				
		int enrollSessionDurationFrames = (int)Math.round(specs.enrollSessionDurationSec/ feaSpecs.fFrameIncrementSec);

		SessionsTable_MyMatrix STM = new SessionsTable_MyMatrix();
		LabeledArrayListOfSpeakers<MyMatrix> speakersChunks = STM.makeChunks(enrollmentSessions, enrollSessionDurationFrames);
		
		outputToUser("FOUND " + speakersChunks.size() + " speaker available for reference population computation");
		AllSessionsArrays<MyMatrix> a = STM.toSessionsArrays(speakersChunks);

		POPREF_MODEL pop = new POPREF_Builder().build(specs, feaSpecs, a, false); // Can throw Exception
		
		// Save
		pop.writeToFile(new File(popRefFullPath));

		// To user
		outputToUser(newline + "Output file of engine models: " + popRefFullPath + newline + newline + "DONE" + newline);
		outputToUser(newline + "Output file of configuration: " + newCfgFullPath + newline + newline + "DONE" + newline);


	}
	
	/**
	 * Display a message for the user
	 * @param msg
	 */
	void outputToUser(String msg) {
		if (output!=null)
		{
			output.append(msg);
			output.setCaretPosition(output.getDocument().getLength());
		}
		System.out.println(msg);
	}
	

}
