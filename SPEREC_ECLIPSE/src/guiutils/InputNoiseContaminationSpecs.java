package guiutils;

import java.io.IOException;


import app.NoiseContaminationSpecs;
import app.Environment;
import app.InputSpecs;
import sperec_common.ConfigurationFile;


public class InputNoiseContaminationSpecs extends InputSpecs<NoiseContaminationSpecs> {


	public InputNoiseContaminationSpecs(NoiseContaminationSpecs initSpecs_, Environment env) throws IOException
	{	
		super(initSpecs_, NoiseContaminationSpecs.class, env);
		this.cfgSectionName = "NoiseContamination";
		this.cfgItemName = "NoiseContaminationSpecs";
		this.humanReadableName = "Noise contamination";
	}
	
	public InputNoiseContaminationSpecs() throws IOException {
		this(null, null);
	}
	
	public InputNoiseContaminationSpecs(Environment env) throws IOException {
		this(null, env);
	}


	/**
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public NoiseContaminationSpecs getFromGUI(NoiseContaminationSpecs s) throws IOException {

		if (s==null)
			s = new NoiseContaminationSpecs();

		NoiseContaminationSpecs cfg = new NoiseContaminationSpecs(); // Not null

		Double SNRdB = InputNumericDouble.get(s.SNRdB,  "The SNR value in dB");
		
		if (SNRdB==null)
			return null;
		
		cfg.SNRdB = SNRdB;
		
		// Need to select the noise audio file
		cfg.inputNoiseFile = ChooseFile.get(s.inputNoiseFile, "Choose the input noise file at the same sample frequency", "(*.wav)", "wav");
		if (cfg.inputNoiseFile.equals(""))
			return null;

		// Need to select the folder of INPUT audio files at 16000Hz
		//cfg.inputDir = ChooseFolder.get(s.inputDir, "Select the folder of audio files at 16000 Hz");
		//if (cfg.inputDir.equals(""))
		//	return null;
		cfg.inputFileList = ChooseFile.get(s.inputFileList, "Select the input file list", "(*.lst)", "lst");
		if (cfg.inputFileList.equals(""))
			return null;



		return cfg;
	}



	/**
	 * For test and debug purpose
	 * @param arg
	 * @throws IOException 
	 */
	public static void main(String[] arg) throws IOException {

		InputNoiseContaminationSpecs me = new InputNoiseContaminationSpecs();
		NoiseContaminationSpecs cfg = me.getSpecs();

		if (cfg!=null)
		{
			cfg.dump();

			// Visto che ci sono, chiedi se vuole salvare
			ConfigurationFile outConfigFile = new ConfigurationFile();
			outConfigFile.addSection(me.cfgSectionName);
			outConfigFile.addItem(me.cfgSectionName, me.cfgItemName, cfg.toJsonString());

			String outPath = SaveFile.as(me.MAIN_OUTPUT_FOLDER_PATH, "Salva il file di configurazione");
			if (outPath!=null)
				outConfigFile.saveAs(outPath);
		}
	}
}

