package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import guiutils.ChooseFolder;
import guiutils.InputNoiseContaminationSpecs;
import guiutils.SaveFile;
import myIO.MyWavFile;
import sperec_common.ConfigurationFile;
import sperec_common.MiscUtils;


/**
 * Interactive program to contaminate with noise a set of audio files.
 * It is based on my porting of FANT (class Fant)
 * @author FS
 *
 */
public class NoiseContamination {

	//private static final String MAIN_OUTPUT_FOLDER_PATH = Environment.getMainOutputFolderPath();

	
	/**
	 * MAIN ENTRY POINT: Start Noise Contamination using config file
	 * Get the noise specs, save to file, get the output dir, then start the job
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// 1. Get the noise contamination specs
		InputNoiseContaminationSpecs iSpecs = new InputNoiseContaminationSpecs();
		NoiseContaminationSpecs cfg = iSpecs.getSpecs();
		if (cfg==null)
			return;
		
		// 2. Since we are done with the configuration, save now the specs
		ConfigurationFile outConfigFile = new ConfigurationFile();
		outConfigFile.addSection(iSpecs.cfgSectionName);
		outConfigFile.addItem(iSpecs.cfgSectionName, iSpecs.cfgItemName, cfg.toJsonString());
		String cfgSavedFullPath = iSpecs.getConfigurationPath();
		cfgSavedFullPath = SaveFile.as(cfgSavedFullPath, "Salva il file di configurazione");
		if (cfgSavedFullPath.equals(""))
			return;
		outConfigFile.saveAs(cfgSavedFullPath); // Actually save the configurations
		
		// 3. Select the output folder for the noisy audio
		Environment env = new Environment();
		String outputDir = env.getMainOutputFolderPath();
		if (outConfigFile != null)
			outputDir = outConfigFile.cfgFilePath;
		outputDir = ChooseFolder.get(outputDir, "CREATE A NEW FOLDER AND SELECT AS OUTPUT FOLDER");
		if ( (outputDir==null) || (outputDir.equals("")) )
			return;
		
		// 4. Do the job
		main2(cfg, outputDir);
	}
	
	
	/**
	 * Do noise contamination given the NoiseContaminationSpecs and the output path
	 * @param cfg the NoiseContaminationSpecs
	 * @param outputDir the path (string) of the output folder where to place the noisy audio files
	 * @throws Exception
	 */
	static private void main2(NoiseContaminationSpecs cfg, String outputDir) throws Exception {
				
		// 1. Carica il file del rumore
		MyWavFile noise = MyWavFile.openWavFile(new File(cfg.inputNoiseFile));
		noise.readAllFloat();	
		noise.close();
		float [] noiseSamples = noise.getFloatSamples();
		double sampleRate = noise.getSampleRate();
		
		// 2. Get the configuration parameters for FANT, use the provided SNR [dB]
		String[] noiseArgs = getNoiseArgsForFant(cfg.SNRdB);
		
		// 3.------------------------------------------------------------
		// Ora per ogni file esegue la contaminazione
		String inputDir = FilenameUtils.getFullPath(cfg.inputFileList);

		// Open the file of the input list
		BufferedReader br = new BufferedReader(new FileReader(cfg.inputFileList));

		// Open the file of the output list
		String tableFilePath = outputDir + File.separator + "file.lst";
		BufferedWriter writer = new BufferedWriter(new FileWriter(tableFilePath));
		writer.write("#Filename" + "\t" + "Speaker" + "\n");

		int thread_idx = 0;

		ArrayList<String> outFileNames = new ArrayList<String>();


		long tStart = System.currentTimeMillis();

		while (br.ready())
		{
			String line = br.readLine().trim();
			if (line.charAt(0) != '#')
			{
				String[] parts = line.split("\t");
				String filename = parts[0];
				String spkId = parts[1];

				// Carico il file, faccio il lavoro
				String inputAudioFullPath = inputDir + File.separator + filename;
				String	baseFilename = FilenameUtils.getBaseName(filename);

				String sOutAudioFilename = hlp(noiseSamples, noiseArgs, sampleRate, inputAudioFullPath, filename, outputDir);
				
				writer.write(sOutAudioFilename + "\t" + spkId + "\n");

				outFileNames.add(sOutAudioFilename);

			}
		}
		br.close();
		writer.close();

		// Check for errors
		for (int i=0; i<outFileNames.size(); i++)
			if (!(new File(outputDir + File.separator + outFileNames.get(i)).exists()))
				System.err.println("ERROR: file " + outFileNames.get(i) + " has not been created");

		MiscUtils.showElapsedTime(tStart);

	}
	
	/**
	 * Do the job on the single file
	 * @param noiseSamples
	 * @param noiseArgs
	 * @param sampleRate
	 * @param inputAudioFullPath
	 * @param filename
	 * @param outputDir
	 * @return
	 * @throws Exception
	 */
	private static String hlp(float [] noiseSamples, String [] noiseArgs, double sampleRate, String inputAudioFullPath, String filename, String outputDir) throws Exception {
		
		// 1. Apre il file audio da contaminare
		MyWavFile oWavfile = MyWavFile.openWavFile(new File(inputAudioFullPath));
		
		// 2. Controlla che abbia lo stesso sample rate
		if (sampleRate != oWavfile.getSampleRate())
			throw new Exception("ERROR: Audio must have the same sempe rate of the noise");
		
		// 3. Carica in memoria l'intero contenuto e chiude il file
		oWavfile.readAllFloat();	
		oWavfile.close();
				
		// 4. CONTAMINA, lavora con float (single precision)
		float [] noisySpeechSamples = Fant.filter_add_noise(oWavfile.getFloatSamples(), noiseSamples, noiseArgs);
		
		// 5. Salva l'audio noisy
	    MyWavFile.saveMonoToFile(outputDir + File.separator + filename, noisySpeechSamples, (long)sampleRate, oWavfile.getValidBits());
		
		return filename;
	}
	
	
	
	/**
	 * Get the args for FANT, given the SNR value. SOme parameters are hard coded
	 * @param snr
	 * @return
	 */
	private static String [] getNoiseArgsForFant(double snr) {
		return new String[] {"-u", "-m", "snr_8khz", "-d", "-s", Double.toString(snr), "-l", "-20.0"};
	}
	
	
}
