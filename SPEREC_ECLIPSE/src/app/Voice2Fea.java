package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import app.Environment;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import guiutils.ChooseFile;
import guiutils.ChooseFolder;
import guiutils.InputFeaSpecs;
import guiutils.ProgressUI;
import sperec_common.AudioProcessingSpecs;
import sperec_common.ConfigurationFile;
import sperec_common.FeaSpecs;
import sperec_common.MiscUtils;
import myMath.MyMatrix;
import sperec_common.Parfor;
import sperec_common.SPEREC_AudioProcessor;
import sperec_common.VadSpecs;

public class Voice2Fea {

	static private class Cfg {
		private String cfgFileOfInputData = ""; // e.g. vad.cfg
		private String inputFileList = ""; // The file (e.g. .lst) with the names of the input files
		private String inputDir = ""; // The folder of he input files
		private String inputId = "";
		private String mainOutputFolderPath_ufv = "";
		private String relativeOutputSubFolderName = "";
		private String feaFileList = "";
		private FeaSpecs feaSpecs = null;
		private VadSpecs vadSpecs = null;
		private String feaName = ""; // To choose a FEA method; // Not sure if this should be included in feaSpecs

		private boolean ready = false;
	}

	//private static final String MAIN_OUTPUT_FOLDER_PATH = System.getenv("USERPROFILE") + File.separator + "Desktop" + File.separator + "SPEREC_tmp_files"; //"C:\\Users\\FS\\Desktop\\tempFiles\\outvad";
	private static String MAIN_OUTPUT_FOLDER_PATH = ""; //Environment.getMainOutputFolderPath();
	
	private static String MAIN_OUTPUT_FOLDER_PATH_UFV; // = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ufv";

	public static final String SYNOPSIS = 
			"Francesco Sigona 21 November 2017\n" +
					"Compute ....\n";


	/**
	 * By means of the SPEREC class, compute features from an audio file.
	 * VAD is supposed to have already been performed.
	 * @param inputAudioFullPath
	 * @param feaSpecs
	 * @param outputAudioFullPath
	 * @throws Exception 
	 */
	private static void voice2fea_single_file(String inputAudioFullPath, FeaSpecs feaSpecs, String sOutAudioFullPath_ufv) throws Exception {
		MyMatrix fea = null;

		AudioDispatcher mainAudioDispatcher = AudioDispatcherFactory.fromFile(new File(inputAudioFullPath), 2048, 0);

		// Here vadSpecs must be null, because the user invokes this application after the audio pre-proecessing
		// fea = new SPEREC().computeFeaFromAudio(mainAudioDispatcher, null, feaSpecs); // null= vadSpecs
		// fea.writeToUfvFile(sOutAudioFullPath_ufv);
		
		AudioProcessingSpecs audioProcessingSpecs = new AudioProcessingSpecs();
		audioProcessingSpecs.vadSpecs = null;
		audioProcessingSpecs.feaSpecs = feaSpecs;
		
		fea = SPEREC_AudioProcessor.audio2feaMatrix(mainAudioDispatcher, audioProcessingSpecs);
		
		// Save as ufv
		fea.writeToUfvFile(sOutAudioFullPath_ufv);
	}

	/**
	 * main
	 * @param args
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, InterruptedException {

		Cfg cfg = new Cfg();
		
		// Confirm or choose the output path
		Environment env = new Environment();
		MAIN_OUTPUT_FOLDER_PATH = env.getDataDir();
		MAIN_OUTPUT_FOLDER_PATH = ChooseFolder.get(MAIN_OUTPUT_FOLDER_PATH, "Please choose the path for output");
		if (MAIN_OUTPUT_FOLDER_PATH=="")
			return;		

		// Ask for FEA configuration
		InputFeaSpecs inputFeaSpecs = new InputFeaSpecs(cfg.feaSpecs);
		cfg.feaSpecs = inputFeaSpecs.getSpecs();
		if (null==cfg.feaSpecs)
			return;
				

		// Get the file of the configuration of the input data (e.g. vad.cfg)
		ConfigurationFile cfgFile = inputFeaSpecs.getConfigurationFile();
		if (cfgFile!=null)
			cfg.cfgFileOfInputData = cfgFile.getItem("FEA", "cfgFileOfInputData");
		
		// Suggest the same or a new file of the configuration of the input data (e.g. vad.cfg)
		cfg.cfgFileOfInputData = ChooseFile.get(cfg.cfgFileOfInputData, "CHOOSE A CONFIGURATION FILE OF INPUT DATA OR CANCEL TO SKIP", "cfg");
		
		// If the user has selected a file, then get whatever is required
		String vadFilesPath = "";
		if ((cfg.cfgFileOfInputData != null) && (new File(cfg.cfgFileOfInputData)).exists()) {
	
			ConfigurationFile cfgVad = ConfigurationFile.load(cfg.cfgFileOfInputData); // vadCfgFilePath
			String vadJson = cfgVad.getItem("VAD", "VadSpecs");
			cfg.vadSpecs = VadSpecs.fromJsonString(vadJson);
			
			vadFilesPath = new File(cfg.cfgFileOfInputData).getParent();
			
			//cfg.inputDir = cfgVad.getItem("VAD", "input_folder"); useless			
		}
		
		// Suggest the same or a new file of the list of input files
		cfg.inputFileList = ChooseFile.get(vadFilesPath, "CHOOSE AN INPUT FILE LIST OR CANCEL TO SKIP", "(*.lst)", "lst");
		if (cfg.inputFileList.equals(""))
		{
			// Only when no input file list has been selected, ask for an input directory
			cfg.inputDir = ChooseFolder.get(cfg.inputDir, "Selet the folder of audio files");
			if (cfg.inputDir.equals(""))
				return;
		}
		
		
		// Ask for an ID for INPUT data
		if (cfg.inputFileList.equals(""))
			cfg.inputId = new File(cfg.inputDir).getName();
		else
			cfg.inputId  = new File(new File(cfg.inputFileList).getParent()).getName();

		cfg.inputId = getNameFromUser("Enter an ID for the selected INPUT data, or cancel to abort", cfg.inputId);
		if (cfg.inputId.equals(""))
			return;
		
			
						
		MAIN_OUTPUT_FOLDER_PATH_UFV = MAIN_OUTPUT_FOLDER_PATH + File.separator + "ufv";
		
		cfg.mainOutputFolderPath_ufv = MAIN_OUTPUT_FOLDER_PATH_UFV;

		//parseCommandLine(args, cfg);
			
		
		checkRelativeOutputSubFolderName(cfg);
		
		if (cfg.ready) 
			showSummary(cfg);
		
		if (cfg.ready) {
			
			String outd = cfg.mainOutputFolderPath_ufv + File.separator + cfg.relativeOutputSubFolderName;
			if (! new File(outd).mkdirs()) {
				System.err.println("ERROR: CAN NOT CREATE THE FOLLOWING PATH: " + 	outd);
				return;
			}
			
			// ufv format
			String ufvOutDir = cfg.mainOutputFolderPath_ufv + File.separator + cfg.relativeOutputSubFolderName;
			new File(ufvOutDir).mkdirs();
			
			// Update the feaSpecs
			cfg.feaSpecs.dataSource = cfg.inputId;
			
			cfg.feaFileList = "file.lst"; // Output file list
			
			if (cfg.inputFileList.equals(""))
			{
				// Do the job in the inputDir
				voice2fea_directory(cfg.inputDir, cfg.feaSpecs, outd, ufvOutDir, cfg.feaFileList); // da considerarsi obsoleto
			}
			else
			{
				voice2fea_file_list(cfg.inputFileList, cfg.feaSpecs, outd, ufvOutDir, cfg.feaFileList);
			}
						
			
			saveInfoFile(cfg, ufvOutDir);
			
			System.out.println("ufvOutDir : " + ufvOutDir);
		}
	}

	/**
	 * 
	 * @param args
	 * @param cfg
	 */
	private static void parseCommandLine(String[] args, Cfg cfg) {
		
		if (null!=args) {
			// Parse the command line
			for (int i=0; i < args.length; i++) {
	
				if (args[i].equals("--idir"))
					cfg.inputDir = args[++i];
	
				if (args[i].equals("--odir"))
					cfg.relativeOutputSubFolderName = args[++i];
	
				if (args[i].equals("--fea-nc"))
					cfg.feaSpecs.iAmountOfCepstrumCoef = Integer.parseInt(args[++i]);
	
				if (args[i].equals("--fea-nf"))
					cfg.feaSpecs.iAmountOfMelFilters = Integer.parseInt(args[++i]);
	
				if (args[i].equals("--fea-lowf"))
					cfg.feaSpecs.fLowerFilterFreq = Float.parseFloat(args[++i]);
	
				if (args[i].equals("--fea-upf"))
					cfg.feaSpecs.fUpperFilterFreq = Float.parseFloat(args[++i]);
	
			}
		}
	}
	
	/**
	 * 
	 * @param cfg
	 * @param outdir
	 * @throws IOException
	 */
	private static void saveInfoFile(Cfg cfg, String outdir) throws IOException {
		ConfigurationFile C = new ConfigurationFile();
		C.addSection("FEA");
		C.addItem("FEA", "cfgFileOfInputData", cfg.cfgFileOfInputData);
		C.addItem("FEA", "inputFileList", cfg.inputFileList);
		C.addItem("FEA", "input_folder", cfg.inputDir);
		C.addItem("FEA", "input_id", cfg.inputId);
		C.addItem("FEA",  "feaFileList", new File(cfg.feaFileList).getName()); //cfg.fileList
		C.addItem("FEA", "FeaSpecs", cfg.feaSpecs.toJsonString());
		if (null!=cfg.vadSpecs)
		{
			C.addSection("VAD");
			C.addItem("VAD", "VadSpecs", cfg.vadSpecs.toJsonString());
		}		
		C.saveAs(outdir, "fea.cfg");
	}
	
	/**
	 * 
	 * @param cfg
	 */
	private static void checkRelativeOutputSubFolderName(Cfg cfg) {

		String s = cfg.relativeOutputSubFolderName;

		if (s.equals("")) {
			String s0 = cfg.inputId + "--fea-" + cfg.feaName; //+ "--i-" + cfg.vadSpecs.getFrameIncrementSec() + "--of-" + cfg.vadSpecs.getOverlapFactor();
			s = MiscUtils.fixStringForFileName(s0);
		}

		if (s.equals(""))
			return;


		// propose to the user
		s = getNameFromUser("Enter a name for the ouput folder, or cancel to abort:", s); // Need to handle this or abort
		Path outFullpath = Paths.get(cfg.mainOutputFolderPath_ufv + File.separator + s);

		if (Files.exists(outFullpath)) {
			String sNew = s;
			Path pathNew = outFullpath;
			while( !sNew.equals("") && Files.exists(pathNew)) {
				int c = 0;
				pathNew = outFullpath;
				while(Files.exists(pathNew)) { // Compute a new path
					sNew = s + "_" + (++c) + "_";
					pathNew = Paths.get(cfg.mainOutputFolderPath_ufv + File.separator + sNew);
				}
				// propose to the user
				sNew = getNameFromUser("Enter a name for the ouput folder, or cancel to abort:", sNew); // Need to handle this or abort
				pathNew = Paths.get(cfg.mainOutputFolderPath_ufv + File.separator + sNew);
			}
			if (sNew.equals(""))
				return;
			s = sNew;
		}
		 
		
		cfg.relativeOutputSubFolderName = s;
		// Need to set cfg.ready
		cfg.ready = true;
	}
	

	
	/**
	 * Get a string from the standard in
	 * @param s0
	 * @return
	 */
	private static String getNameFromUser(String msg, String s0) {

		String s1 = JOptionPane.showInputDialog(msg, s0);

		if (null==s1)
			return "";

		String s2 = MiscUtils.fixStringForFileName(s1);
		return s2;
	}
	

	
	/**
	 * Show a summary to the user before to run
	 * feaSpecs.fWindowSizeSec = 0.03f;
		feaSpecs.dFrameIncrementSamplesToWindowSizeSamplesRatio = 0.5f;
		feaSpecs.iAmountOfCepstrumCoef = 40;
		feaSpecs.iAmountOfMelFilters = 50;
		feaSpecs.fLowerFilterFreq = 300;
		feaSpecs.fUpperFilterFreq = 3000;
	 * @param cfg
	 */
	private static void showSummary(Cfg cfg) {
		Object[] message = {
				"Input folder: " + cfg.inputDir, null,
				"Input ID:" + cfg.inputId, null,
				"Output subfolder: " + cfg.relativeOutputSubFolderName, null,
				"Windows size: " + Double.toString(cfg.feaSpecs.fWindowSizeSec), null,
				"Frame increment to windows size ratio: " + Double.toString(cfg.feaSpecs.dFrameIncrementSamplesToWindowSizeSamplesRatio), null,
				"Amount of cepstral coefficients; " + Integer.toString(cfg.feaSpecs.iAmountOfCepstrumCoef), null,
				"Amount of mel filters: " + Integer.toString(cfg.feaSpecs.iAmountOfMelFilters), null,
				"Filter's lower frequency: " + Double.toString(cfg.feaSpecs.fLowerFilterFreq), null,
				"Filer's upper frequency: " + Double.toString(cfg.feaSpecs.fUpperFilterFreq), null
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Accept to run, or cancel operation", JOptionPane.OK_CANCEL_OPTION);
		cfg.ready = (option == JOptionPane.OK_OPTION);	
	}
	
		
	/**
	 * Compute features for each audio file in the specified input directory.
	 * @param inputDir	the input directory
	 * @param feaSpecs	the parameters that specify how to compute the features
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 */
	private static void voice2fea_directory(String inputDir, FeaSpecs feaSpecs, String outputDir, String ufvOutDir, String outFileNameFileList) throws UnsupportedAudioFileException, IOException, InterruptedException {

		// For each "wav" file in the "dir" folder...

		// Open the file of the output list
		String tableFilePath = outputDir + File.separator + outFileNameFileList; //+"file.lst";
		BufferedWriter writer = new BufferedWriter(new FileWriter(tableFilePath));
		writer.write("#Filename" + "\t" + "Speaker" + "\n");
				
				
		File [] directoryListing = MiscUtils.searchForFilesByDotExtension(inputDir, ".wav");


		if (directoryListing==null)
			return;

		int numFiles = directoryListing.length;

		ArrayList<String> outFileNames = new ArrayList<String>();

		// Create progress bar and task box
		ProgressUI progressUI = new ProgressUI("FEA", numFiles);

		long tStart = System.currentTimeMillis();

		//for (File child : directoryListing) {
		new Parfor() { // int _num_threads = 1;
			public void iter(int thread_idx, int i) {
				
				File child = directoryListing[i];
	
				String sOutAudioFilename = hlp(feaSpecs, child.getAbsolutePath(), child.getName(), ufvOutDir, thread_idx);
				
				try {
					writer.write(sOutAudioFilename + "\t" + sOutAudioFilename + "\n"); //spkId = sOutAudioFilename
				}
				catch (Exception e)
				{};

				outFileNames.add(sOutAudioFilename);

				progressUI.increaseProgress(i, sOutAudioFilename + "\n");// Update GUI

			}
		}.execute(0, numFiles);
		
		writer.close();

		
		// Check for errors
		for (int i=0; i<outFileNames.size(); i++)
			if (!(new File(outputDir + File.separator + outFileNames.get(i)).exists()))
				System.err.println("ERROR: file " + outFileNames.get(i) + " has not been created");

		MiscUtils.showElapsedTime(tStart);

		progressUI.close();

	}
	
	
	
	/**
	 * 
	 * @param feaSpecs
	 * @param inputAudioFullPath
	 * @param baseName
	 * @param outputDir
	 * @param thread_idx
	 * @return
	 */
	private static String hlp(FeaSpecs feaSpecs, String inputAudioFullPath, String baseName, String outputDir, int thread_idx) {
		String sOutFilename = baseName + ".ufv";
		String sOutFullPath = outputDir + File.separator + sOutFilename;
		String runMsg = "Thread " + thread_idx + " - Processing file: " + sOutFullPath;
		System.out.println(runMsg);

		try {
			// Better to delete old file
			Files.deleteIfExists(Paths.get(sOutFullPath));	

			// Do the job
			voice2fea_single_file(inputAudioFullPath, feaSpecs, sOutFullPath); // ... do the job
		}
		catch (Exception e) {
			System.out.println("Thread " + thread_idx + " : " + e.getMessage());
		}

		return sOutFilename;
	}
	
	
	
	/**
	 * 
	 * @param fileListFullPath
	 * @param feaSpecs
	 * @param outputDir
	 * @param ufvOutDir
	 * @throws IOException
	 */
	private static void voice2fea_file_list(String fileListFullPath, FeaSpecs feaSpecs, String outputDir, String ufvOutDir, String outFileNameFileList) throws IOException {
		String inputDir = FilenameUtils.getFullPath(fileListFullPath);

		// Open the file of the input list
		BufferedReader br = new BufferedReader(new FileReader(fileListFullPath));

		// Open the file of the output list
		String tableFilePath = outputDir + File.separator + outFileNameFileList; //+"file.lst";
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

				String sOutAudioFilename = hlp(feaSpecs, inputAudioFullPath, baseFilename, outputDir, thread_idx);
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

	
}

