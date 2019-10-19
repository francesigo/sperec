package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;
import guiutils.ChooseFile;
import guiutils.ChooseFolder;
import guiutils.InputVadSpecs;
import guiutils.ProgressUI;
import sperec_common.ConfigurationFile;
import sperec_common.MiscUtils;
import sperec_common.MyURL;
import sperec_common.Parfor;
import sperec_common.SPEREC_AudioProcessor;
import sperec_common.VadSpecs;

/**
 * Perform Voice Activity Detection in each file within the given folder.
 * @author FS
 *
 */
public class Audio2vad {

	static private class Cfg {
		private String inputFileList = "";
		private String inputDir = "";
		private String inputId = "";
		private String mainOutputFolderPath = "";
		private String relativeOutputSubFolderName = "";
		private VadSpecs vadSpecs = null;
		private String vadName = ""; // To choose a VAD method; // Not sure if this should be included in vadSpecs

		private boolean ready = false;
	}

	public static final String SYNOPSIS = 
			"Francesco Sigona 21 November 2017\n" +
					"Compute ....\n";

	private static String MAIN_OUTPUT_FOLDER_PATH = "";


	private Cfg cfg = null;
	private String outputDir = ""; // Output directory
	private boolean use_concurrency = false;
	private String [] inputArray = null;
	private ArrayList<String>inputClassNames = null;
	private ArrayList<String> outFileNames = null;
	private BufferedWriter writer = null;
	private ProgressUI progressUI = null;

	/**
	 * 
	 * @param args
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, InterruptedException {
		
		// Default config (for local purpose)
		Cfg cfg = new Cfg();

		// Get the path of the output folder, also asking the user
		Environment env = new Environment();
		MAIN_OUTPUT_FOLDER_PATH = env.getDataDir();	
		// Confirm or choose the output path
		MAIN_OUTPUT_FOLDER_PATH = ChooseFolder.get(MAIN_OUTPUT_FOLDER_PATH, "Please choose the path for output (e.g.: ..../outVad)");
		if (MAIN_OUTPUT_FOLDER_PATH=="")
			return;

		// Choose the input VAD specs, user interaction
		InputVadSpecs inputVadSpecs = new InputVadSpecs();
		cfg.vadSpecs = inputVadSpecs.getSpecs();
		ConfigurationFile cfgFile = inputVadSpecs.getConfigurationFile();
		if (cfgFile != null)
			cfg.inputDir = cfgFile.getItem("VAD", "input_folder");

		// The very main folder where to place every VAD experiment
		cfg.mainOutputFolderPath = MAIN_OUTPUT_FOLDER_PATH;

		// Safety check of input parameters from the command line
		// UNUSED parseCommandLine(args, cfg);


		// If not already specified into the cfg, ask the user for input file list or directory (GUI)
		cfg.inputFileList = ChooseFile.get(cfg.inputDir, "CHOOSE AN INPUT FILE LIST OR CANCEL TO SKIP", "(.lst)", "lst");
		if (cfg.inputFileList.equals(""))
		{
			// Ask for an input directory
			cfg.inputDir = ChooseFolder.get(cfg.inputDir, "Select the folder of audio files or cancel to abort");
			if (cfg.inputDir.equals(""))
				return;
		}


		// Suggest and ask the user for an ID for INPUT data
		if (cfg.inputFileList.equals(""))
			cfg.inputId = new File(cfg.inputDir).getName();
		else
			cfg.inputId  = new File(new File(cfg.inputFileList).getParent()).getName();
		cfg.inputId = getNameFromUser("Enter an ID for the selected INPUT data, or cancel to abort", cfg.inputId);
		if (cfg.inputId.equals(""))
			return;


		// Check the name of the subfolder where to place the output audio files
		checkRelativeOutputSubFolderName(cfg);

		if (cfg.ready) 
			showSummary(cfg);

		if (!cfg.ready)
			return;


		// The final fullpath for output audio
		String outd = cfg.mainOutputFolderPath + File.separator + cfg.relativeOutputSubFolderName;

		if (! new File(outd).mkdirs()) {
			System.err.println("ERROR: CAN NOT CREATE THE FOLLOWING PATH: " + 	outd);
			return;
		}

		// Actual processing
		Audio2vad me = new Audio2vad();
		me.cfg = cfg;
		me.outputDir = outd;
		
		if (cfg.inputFileList.equals(""))
		{
			me.audio2wav_multipleFiles(cfg.inputDir);
		}
		else
		{
			me.audio2wav_multipleFiles(cfg.inputFileList);
		}

		// After actual processing, finalize the work: save an info file in the same output folder
		cfg.vadSpecs.dataSource = cfg.inputId;
		saveInfoFile(cfg, outd);

		// End message for the user
		System.out.println("OUTPUT DIRECTORY: " + outd);


	}


	/**
	 * Process the single file
	 * @param inputAudioFullPath the (string) full path of the input audio file
	 * @param vadSpecs the VAD specification
	 * @param outputAudioFullPath the (string) full path of the file where to store the output
	 * @throws Exception
	 */
	private static void audio2wav_singlefile(String inputAudioFullPath, VadSpecs vadSpecs, String outputAudioFullPath) throws Exception {

		// 1. Set the audio source
		AudioDispatcher mainAudioDispatcher = null;
		if (MyURL.isURL(inputAudioFullPath))
		{
			URL u = new URL(inputAudioFullPath);
			mainAudioDispatcher = AudioDispatcherFactory.fromURL(u, 2048, 0);
		}
		else
			mainAudioDispatcher = AudioDispatcherFactory.fromFile(new File(inputAudioFullPath), 2048, 0);
		
		SPEREC_AudioProcessor oSAP = new SPEREC_AudioProcessor(mainAudioDispatcher);
		oSAP.setVad(vadSpecs);
		oSAP.setFea(null);
		
		// Set the output destination of the processing
		WriterProcessor outpuWP = new WriterProcessor(mainAudioDispatcher.getFormat(), new RandomAccessFile(outputAudioFullPath, "rw")); // WAS outFileFullPath
		oSAP.setOutput(outpuWP);
		
		// 2. Run
		oSAP.run();

		// 3. Flush results to file
		// No more useful oSAP.outputAsFile(outputAudioFullPath);
	}


	
	/**
	 * 
	 * @param inputFileFullPaths
	 * @param vadSpecs
	 * @param outputDir
	 * @throws IOException 
	 */
	//private void audio2wav_multipleFiles(String inputDirOrFileList, VadSpecs vadSpecs, String outputDir) throws IOException {
	private void audio2wav_multipleFiles(String inputDirOrFileList) throws IOException {
		
		// Get the list of input files and speaker names
		inputClassNames = new ArrayList<String>();
		inputArray = MiscUtils.getListOfAudioInputFiles(inputDirOrFileList, inputClassNames);
		
		int numFiles = inputArray.length;

		outFileNames = new ArrayList<String>();

		// Create progress bar and task box
		progressUI = new ProgressUI("VAD", numFiles);

		long tStart = System.currentTimeMillis();

		// Open the file of the output list
		String tableFilePath = outputDir + File.separator + "file.lst";
		writer = new BufferedWriter(new FileWriter(tableFilePath));
		writer.write("#Filename" + "\t" + "Speaker" + "\n");
		
		if (use_concurrency)
			helper_loop_concurrency();
			
		else // single thread
			helper_loop_current_thread();
		
		writer.close();

		// Check for errors
		for (int i=0; i<outFileNames.size(); i++)
			if (!(new File(outputDir + File.separator + outFileNames.get(i)).exists()))
				System.err.println("ERROR: file " + outFileNames.get(i) + " has not been created");


		MiscUtils.showElapsedTime(tStart);
		progressUI.close();
		
	}
	
	
	/**
	 * Iteration in the multiple file VAD operations
	 * @param thread_idx
	 * @param i
	 */
	private void helper_iter(int thread_idx, int i) {
		
		String fileFullpath = inputArray[i];
		String filename = new File(fileFullpath).getName();
		
		// 1. Complete the file name and extension
		String sOutAudioFilename = filename + "_VAD.wav";

		// 2. Complete the file path
		String sOutAudioFullPath = outputDir + File.separator + sOutAudioFilename;

		// A message for the user
		String runMsg = "Thread " + thread_idx + " - Processing file: " + sOutAudioFullPath;
		System.out.println(runMsg);

		try {
			// 3. Better to delete old file
			Files.deleteIfExists(Paths.get(sOutAudioFullPath));	

			// 4. Do the job
			audio2wav_singlefile(fileFullpath, cfg.vadSpecs, sOutAudioFullPath);
		}
		catch (Exception e) {
			System.out.println("Thread " + thread_idx + " : " + e.getMessage());
		}

		outFileNames.add(sOutAudioFilename);
		
		// Add a new line to the output file list
		String spkId = ((inputClassNames!=null) && (inputClassNames.size()>0)) ? inputClassNames.get(i) : filename;
		try {
			writer.write(sOutAudioFilename + "\t" + spkId + "\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		progressUI.increaseProgress(i, sOutAudioFilename + "\n");
	}
	
	
	private void helper_loop_concurrency() {

		int numFiles = inputArray.length;
		new Parfor() { // int _num_threads = 1;
			public void iter(int thread_idx, int i)
			{
				helper_iter(thread_idx, i);
			} 
		}.execute(0, numFiles);
	}
	
	/**
	 * Create poprefs and sperec instance for all the groups, using the current thread
	 */
	private void helper_loop_current_thread() {
		
		int numFiles = inputArray.length;
		for (int i=0; i<numFiles; i++)
			helper_iter(0, i);
	}
	


	/**
	 * Save the configuration used for these VADded audio files
	 * @param cfg
	 * @param outdir
	 * @throws IOException
	 */
	private static void saveInfoFile(Cfg cfg, String outdir) throws IOException {
		ConfigurationFile C = new ConfigurationFile();
		C.addSection("VAD");
		C.addItem("VAD", "input_folder", cfg.inputDir);
		C.addItem("VAD",  "inputFileList", cfg.inputFileList);
		C.addItem("VAD", "input_id", cfg.inputId);
		C.addItem("VAD", "VadSpecs", cfg.vadSpecs.toJsonString());
		C.saveAs(outdir, "vad.cfg");
	}

	/**
	 * Check the subfolder where the VAD result should be placed
	 * @param relativeOutputSubFolderName
	 * @return
	 */
	private static void checkRelativeOutputSubFolderName(Cfg cfg) {

		String s = cfg.relativeOutputSubFolderName;

		if (s.equals("")) {
			String s0 = cfg.inputId + "--vad-" + cfg.vadName + "--i-" + cfg.vadSpecs.getFrameIncrementSec() + "--of-" + cfg.vadSpecs.getOverlapFactor();
			s = MiscUtils.fixStringForFileName(s0);
		}

		if (s.equals(""))
			return;

		Path outFullpath = Paths.get(cfg.mainOutputFolderPath + File.separator + s);

		if (Files.exists(outFullpath)) {
			String sNew = s;
			Path pathNew = outFullpath;
			while( !sNew.equals("") && Files.exists(pathNew)) {
				int c = 0;
				pathNew = outFullpath;
				while(Files.exists(pathNew)) { // Compute a new path
					sNew = s + "_" + (++c) + "_";
					pathNew = Paths.get(cfg.mainOutputFolderPath + File.separator + sNew);		
				}
				// propose to the user
				sNew = getNameFromUser("Enter a name for the ouput folder, or cancel to abort:", sNew); // Need to handle this or abort
				pathNew = Paths.get(cfg.mainOutputFolderPath + File.separator + sNew);
			}
			if (sNew.equals(""))
				return;
			s = sNew;
		}

		cfg.relativeOutputSubFolderName = s;
		// Need to set cfg.ready
		cfg.ready = true;
	}





	//---------------------- GUI section ---------------------------------

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
	 * @param cfg
	 */
	private static void showSummary(Cfg cfg) {
		Object[] message = {
				"Input folder: " + cfg.inputDir, null,
				"Input ID:" + cfg.inputId, null,
				"Output subfolder: " + cfg.relativeOutputSubFolderName, null,
				"Frame increament: " + Double.toString(cfg.vadSpecs.getFrameIncrementSec()), null,
				"Overlap factor: " + Double.toString(cfg.vadSpecs.getOverlapFactor()), null
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Accept to run, or cancel operation", JOptionPane.OK_CANCEL_OPTION);
		cfg.ready = (option == JOptionPane.OK_OPTION);	
	}

	

	//---------------------- UNUSED stuff ---------------------------------



	/**
	 * Parse arguments from commnad line. UNUSED
	 * @param args
	 * @param cfg
	 */
	/*private static void parseCommandLine(String[] args, Cfg cfg) {

		if (null!=args) {
			for (int i=0; i < args.length; i++)
			{
				if (args[i].equals("--idir"))
					cfg.inputDir = args[++i]; // Override the input dir

				if (args[i].equals("--vad-i"))
					cfg.vadSpecs.setFrameIncrementSec(Float.parseFloat(args[++i]));

				if (args[i].equals("--vad-of"))
					cfg.vadSpecs.setOverlapFactor(Float.parseFloat(args[++i]));

				if (args[i].equals("--vad-name"))
					cfg.vadSpecs.setMethod(args[++i]); // Set the vad algorithm

				if (args[i].equals("--odir"))
					cfg.relativeOutputSubFolderName = args[++i]; // Override the relativeOutputSubFolderName
			}
		}
	}
	 */
	
	/**
	 * Process all the files listed in the provided file list
	 * @param fileListFullPath
	 * @param vadSpecs
	 * @param outputDir
	 * @throws IOException
	 */
	/*private static void audio2wav_file_list_OLD(String fileListFullPath, VadSpecs vadSpecs, String outputDir) throws IOException {

		// The input folder will be the one where fileListFullPath is stored
		String inputDir = FilenameUtils.getFullPath(fileListFullPath);

		// Open the file of the input list
		BufferedReader br = new BufferedReader(new FileReader(fileListFullPath));

		// Open the file of the output list
		String tableFilePath = outputDir + File.separator + "file.lst";
		BufferedWriter writer = new BufferedWriter(new FileWriter(tableFilePath));
		writer.write("#Filename" + "\t" + "Speaker" + "\n");

		int thread_idx = 0;

		ArrayList<String> outFileNames = new ArrayList<String>();


		// Record the start time of the whole loop.
		// But at the moment thin include also the processing of the input file list
		// and the creation of output lists
		long tStart = System.currentTimeMillis();

		while (br.ready())
		{
			String line = br.readLine().trim();
			if (line.charAt(0) != '#')
			{
				String[] parts = line.split("\t");
				String filename = parts[0];
				String spkId = parts[1];

				// Build the full path of the audio input file
				String inputAudioFullPath = inputDir + File.separator + filename;

				// Extract just the filename
				String	baseFilename = FilenameUtils.getBaseName(filename);

				// Call a helper to get the job done
				String sOutAudioFilename = hlp(vadSpecs, inputAudioFullPath, baseFilename, outputDir, thread_idx);

				// Add a new line to the output file list
				writer.write(sOutAudioFilename + "\t" + spkId + "\n");

				// Add a new entry to the list of output file names
				outFileNames.add(sOutAudioFilename);

			}
		}
		br.close();
		writer.close();

		// Check for errors
		for (int i=0; i<outFileNames.size(); i++)
			if (!(new File(outputDir + File.separator + outFileNames.get(i)).exists()))
				System.err.println("ERROR: file " + outFileNames.get(i) + " has not been created");

		// Show the elapsed time fo the whole loop
		MiscUtils.showElapsedTime(tStart);
	}*/
	
	/**
	 * 
	 * @param inputDir
	 * @param vadSpecs
	 * @param outputDir
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	/*private static void audio2wav_folder_OLD(String inputDir, VadSpecs vadSpecs, String outputDir) throws UnsupportedAudioFileException, IOException, InterruptedException {

		// dir()
		File [] directoryListing = MiscUtils.searchForFilesByDotExtension(inputDir, ".wav");

		if (directoryListing==null)
			return;

		int numFiles = directoryListing.length;

		ArrayList<String> outFileNames = new ArrayList<String>();

		// Create progress bar and task box
		ProgressUI progressUI = new ProgressUI("VAD", numFiles);

		long tStart = System.currentTimeMillis();

		new Parfor() { // int _num_threads = 1;
			public void iter(int thread_idx, int i) {
				File child = directoryListing[i];

				String sOutAudioFilename = hlp(vadSpecs, child.getAbsolutePath(), child.getName(), outputDir, thread_idx);
				outFileNames.add(sOutAudioFilename);

				progressUI.increaseProgress(i, sOutAudioFilename + "\n");

			} 
		}.execute(0, numFiles);

		// Check for errors
		for (int i=0; i<outFileNames.size(); i++)
			if (!(new File(outputDir + File.separator + outFileNames.get(i)).exists()))
				System.err.println("ERROR: file " + outFileNames.get(i) + " has not been created");


		MiscUtils.showElapsedTime(tStart);
		progressUI.close();
	}
	 */
	
	/**
	 * Build the final output filename and do the job. It relies on audio2wav_singlefile
	 * @param vadSpecs the VAD specification
	 * @param inputAudioFullPath
	 * @param baseName a prefix for the output file name; the final file name will be like <baseName>_VAD.wav
	 * @param outputDir
	 * @param thread_idx
	 * @return the output file name
	 */
	/*private static String hlp(VadSpecs vadSpecs, String inputAudioFullPath, String baseName, String outputDir, int thread_idx) {

		// 1. Complete the file name and extension
		String sOutAudioFilename = baseName + "_VAD.wav";

		// 2. Complete the file path
		String sOutAudioFullPath = outputDir + File.separator + sOutAudioFilename;

		// A message for the user
		String runMsg = "Thread " + thread_idx + " - Processing file: " + sOutAudioFullPath;
		System.out.println(runMsg);

		try {
			// 3. Better to delete old file
			Files.deleteIfExists(Paths.get(sOutAudioFullPath));	

			// 4. Do the job
			audio2wav_singlefile(inputAudioFullPath, vadSpecs, sOutAudioFullPath);
		}
		catch (Exception e) {
			System.out.println("Thread " + thread_idx + " : " + e.getMessage());
		}

		return sOutAudioFilename;
	}*/
}

