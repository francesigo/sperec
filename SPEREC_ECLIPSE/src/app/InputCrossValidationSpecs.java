package app;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import guiutils.ChooseFile;
import guiutils.InputNoiseContaminationSpecs;
import guiutils.InputSperecSpecs;
import sperec_common.ConfigurationFile;
import sperec_common.MiscUtils;
import sperec_common.SessionsTable_MyMatrix;

public class InputCrossValidationSpecs extends InputSpecs<CrossValidationSpecs>
{

	public InputCrossValidationSpecs(CrossValidationSpecs initSpecs_, Environment env_)
	{
		super(initSpecs_, CrossValidationSpecs.class, env_);
		this.cfgSectionName = "CrossValidation";
		this.cfgItemName = "CrossValidationConfig";
		this.humanReadableName = "Cross Validation";
		this.MAIN_OUTPUT_FOLDER_PATH = env.getCrossValidationDir();
	}
	public InputCrossValidationSpecs(Environment env)
	{
		this(null, env);
	}
	public InputCrossValidationSpecs(CrossValidationSpecs initSpecs_)
	{
		this(initSpecs_, null);
	}
	public InputCrossValidationSpecs()
	{
		this(null, null);
	}



	/**
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public CrossValidationSpecs getFromGUI(CrossValidationSpecs s) throws IOException {

		if (s==null)
			s = new CrossValidationSpecs();

		CrossValidationSpecs cfg = new CrossValidationSpecs(); // Not null

		JTextField snrField = new JTextField(new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(s.SNRdB_v));
		JTextField testSessionDurationSecField = new JTextField(new Gson().toJson(s.testSessionDurationSec_v));
		//JTextField enrollSessionDurationSecField = new JTextField(Double.toString(s.enrollSessionDurationSec));
		JTextField numberOfDevelSpksField = new JTextField(Integer.toString(s.numberOfDevelSpks));


		Object[] message = {
				"Ciao", null,
				"Valori SNR [dB], es. [0.0, 6.0, 15.0, 20.0]: ", snrField,
				"Durate delle sessioni di test, in sec.,  (es. [ 2.0, 4.0 ]:", testSessionDurationSecField,
				//"Durata delle sessioni di enrollent (un solo valore): ", enrollSessionDurationSecField,
				"Numero di parlanti di test in ogni gruppo:", numberOfDevelSpksField,
		};

		boolean again = true;
		String errmsg = "";

		while (again) {
			message[0] = errmsg + "Please enter the required fields, or cancel to abort";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {

				errmsg = "";

				try {
					cfg.SNRdB_v = stringToArrayOfDouble(snrField.getText());
				} catch (Exception e) {
					throw e;
				}

				try {
					cfg.testSessionDurationSec_v = stringToArrayOfDouble(testSessionDurationSecField.getText());
				} catch (Exception e) {
					throw e;
				}
				

				/*try {
					cfg.enrollSessionDurationSec = Double.parseDouble(enrollSessionDurationSecField.getText());
				} catch (Exception e) {
					throw e;
				}		
				if (cfg.enrollSessionDurationSec<=0) {
					errmsg = errmsg + "\n" + "The duration of the enrollement sessions must be positive";
				}
				*/


				try {
					cfg.numberOfDevelSpks = Integer.parseInt(numberOfDevelSpksField.getText());
				} catch (Exception e) {
					throw e;
				}
				if (cfg.numberOfDevelSpks<=0) {
					errmsg = errmsg + "\n" + "The number of develompment speakers must be positive";
				}

				if (!errmsg.isEmpty()) {
					errmsg = "ERROR: " + errmsg + "\n";
					System.out.println(errmsg);
				} else
					again = false;

			} else {
				System.out.println("Operation aborted");
				cfg = null; // Discard all
				again = false;
			}
		}

		if (cfg==null)
			return null;




		// Need to select the folder of INPUT files FOR EACH SNR
		String [] feaConfigFiles = new String[cfg.SNRdB_v.length];
		String feaConfigFile = env.getDataDir();
		for (int i =0; i<feaConfigFiles.length; i++)
		{
			double snr = cfg.SNRdB_v[i];
			
			again = true;
			while(again)
			{
				again = false;
				feaConfigFile = ChooseFile.get(feaConfigFile, "Select the configuration file of Features for SNR = " + snr + " dB, or cancel to abort", "(.cfg)", "cfg");
				if (feaConfigFile.equals(""))
					return null;

				if (Math.abs(snr) != Double.POSITIVE_INFINITY)
				{
					// Check
					Double snr2 = getSnrFromConfigFiles(feaConfigFile);
					if ((snr2!=null) && (snr2.doubleValue()!=snr)) {
						System.err.println("ERROR: SNR MISMATCH");
						again = true;
					}
				}
				// Check the file list
				ConfigurationFile tempCfgFile = ConfigurationFile.load(feaConfigFile);
				String feaDir = new File(feaConfigFile).getParent();
				String tmpInputFileList = tempCfgFile.getItem("FEA", "feaFileList");
				String feaList = feaDir + File.separator + tmpInputFileList;
				File f = new File(feaList);
				if (!f.canRead()) {
					System.err.println("ERROR: Can not read the file " + feaList);
					again = true;
				} else 	{
					// Check each file
					again = !SessionsTable_MyMatrix.fileListIsOk(feaDir, feaList);
					if (again)
						System.err.println("ERROR: Can not read one or more file in the list at " + feaList);

				}

			}


			feaConfigFiles[i] = feaConfigFile;
		}
		cfg.feaConfigFiles = feaConfigFiles;



		// Optionally, select the file of cached enrollment sessions for non-target comparison
		//cfg.enrollmentSessionsFile = ChooseFile.get(s.enrollmentSessionsFile, "(OPTIONAL) Select the file of the enrollment sessions for background and non-target comparisons", "cache");

		// If no cache have been selected, ask for the clean files
		if (cfg.enrollmentSessionsFile.equals("")) {

			/*cfg.cleanSpeechDir = ChooseFolder.get(s.cleanSpeechDir, "Select the folder with the clean original speech files, for background and non-target comparison");
			if (cfg.cleanSpeechDir.equals(""))
				return null;
			 */
			cfg.cleanFeaCfgFile = ChooseFile.get(cfg.cleanFeaCfgFile, "Select the file list of clean Features (e.g. fea.cfg) or Cancel to abort", "(*.cfg)", "cfg");
			if (cfg.cleanFeaCfgFile.equals(""))
				return null;
		}

		// Server specs
		InputSperecSpecs inputSperecSpecs = new InputSperecSpecs(s.specs);
		cfg.specs = inputSperecSpecs.getSpecs();
		if (cfg.specs == null)
			return null;

		return cfg;
	}



	/**
	 * 
	 * @param cfgFileWithNoiseSpecs
	 * @return
	 * @throws IOException
	 */
	public Double getSnrFromConfigFiles(String cfgFileWithNoiseSpecs) throws IOException {
		Double snr = null;

		// Now try to get the snr
		// Look inside the fea cfg
		String tmpInputFileList;
		String dirPath;
		ConfigurationFile tempCfgFile;
		File [] tmpFiles;
		File tmpF;
		NoiseContaminationSpecs noiseSpecs = new InputNoiseContaminationSpecs(env).getFromFile(cfgFileWithNoiseSpecs, false);
		if (noiseSpecs==null) {
			tempCfgFile = ConfigurationFile.load(cfgFileWithNoiseSpecs);
			String tmpInputFileList1 = tempCfgFile.getItem("FEA", "inputFileList");
			tmpInputFileList = env.findFile(tmpInputFileList1);
			dirPath = new File(tmpInputFileList).getParent();
			if (new File(dirPath).exists()) {
				tmpFiles = MiscUtils.searchForFilesByDotExtension(dirPath, ".cfg");
				if (tmpFiles!=null) {
					tmpF = tmpFiles[0]; // Prendo solo il primo
					cfgFileWithNoiseSpecs = tmpF.getAbsolutePath();
					noiseSpecs = new InputNoiseContaminationSpecs(env).getFromFile(cfgFileWithNoiseSpecs, false);
					if (noiseSpecs==null) {
						// Assume che sia un VAD
						tempCfgFile = ConfigurationFile.load(cfgFileWithNoiseSpecs);
						tmpInputFileList1 = tempCfgFile.getItem("VAD", "inputFileList");
						tmpInputFileList = env.findFile(tmpInputFileList1);
						dirPath = new File(tmpInputFileList).getParent();
						tmpFiles = MiscUtils.searchForFilesByDotExtension(dirPath, ".cfg");
						if (tmpFiles!=null) {
							tmpF = tmpFiles[0]; // Prendo solo il primo
							String cfgFileWithNoiseSpecs1 = tmpF.getAbsolutePath();
							cfgFileWithNoiseSpecs = env.findFile(cfgFileWithNoiseSpecs1);
							noiseSpecs = new InputNoiseContaminationSpecs(env).getFromFile(cfgFileWithNoiseSpecs, false);
							// Mi fermo qui
						}
					}
				}
			}
		}

		if (noiseSpecs!=null)
			snr = noiseSpecs.SNRdB;
		
		return snr;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	static double [] stringToArrayOfDouble(String line_) {
		
		String line = line_.trim().toLowerCase();
		
		double inf1 = Double.POSITIVE_INFINITY;
		String inf_str = new Double(inf1).toString();
		
		//double inf2 = Double.longBitsToDouble(0x7ff0000000000000L);
		if (line.contains(inf_str.toLowerCase()))
			line = line.replace(inf_str.toLowerCase(), inf_str);
		else if (line.contains("inf"))
			line = line.replace("inf", inf_str);
			
		String s = line.trim();

		if (s.charAt(0) != '[')
			s = "[" + s;

		if (s.charAt(s.length()-1) != ']')
			s = s + "]";

		return new Gson().fromJson(s, double[].class);

	}
	
	
	void analyze(String inCfgFile) throws IOException {
		
		String cfgFilePath = env.findFile(inCfgFile);
		CrossValidationSpecs specs = getFromFile(cfgFilePath, false);
		System.out.println("Configuration file loaded: " + inCfgFile + " was (" + cfgFilePath +")");
		specs.analyze(env);
	}
		
	/**
	 * For test and debug purpose
	 * @param arg
	 * @throws IOException 
	 */
	public static void main(String[] arg) throws IOException {

		InputCrossValidationSpecs me = new InputCrossValidationSpecs();
		me.setAsk(true);
		CrossValidationSpecs specs = me.getSpecs();

		if (specs!=null)
		{
			specs.analyze(me.env);//cfg.dump();
			
			
			// Visto che ci sono, chiedi se vuole salvare
			/*ConfigurationFile outConfigFile = new ConfigurationFile();
			outConfigFile.addSection("CrossValidation");
			outConfigFile.addItem("CrossValidation", "CrossValidationConfig", cfg.toJsonString());

			String outPath = SaveFile.as(me.MAIN_OUTPUT_FOLDER_PATH, "Salva il file di configurazione");
			if (outPath!=null)
				outConfigFile.saveAs(outPath);
				*/
		}
	}

}
