package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JTextArea;

import org.apache.commons.math3.distribution.NormalDistribution;

import guiutils.ChooseFile;
import guiutils.SaveFile;
import myMath.MyMath;
import myMath.MyMatrix;
import sperec_common.AuthenticationResult;
import sperec_common.ConfigurationFile;
import sperec_common.FeaSpecs;
import sperec_common.LabeledArrayList;
import sperec_common.MiscUtils;
import sperec_common.AllSessionsArrays;
import sperec_common.StRecord;
import sperec_common.POPREF_MODEL;
import sperec_common.Parfor;
import sperec_common.SPEREC;
import sperec_common.SPEREC_Factory;
import sperec_common.SessionsTable_MyMatrix;
import sperec_common.ValidationResultGauss;
import sperec_common.ValidationResultOpen;
import sperec_common.LabeledArrayListOfFea;
import sperec_common.LabeledArrayListOfSessions;
import sperec_common.LabeledArrayListOfSpeakers;
import sperec_jvm.POPREF_Builder;
import sperec_jvm.SPEREC_Loader_JVM;
import myChart.MyChart;

/**
 * 
 * @author FS
 *
 */
public class SperecCrossValidator {

	/*
	 * A JtextArea where to print some messages. At the moment is not used
	 */
	JTextArea output = null;
	
	static final String newline = "\n";

	
	/**
	 * The path of the folder where to save the  of cross validation results
	 */
	String crossValOutFolder = "";
	
	/**
	 * The name of the file containing the table of the cross validation results
	 */
	String outputCrossValidationResultsFile = "";
	
	/**
	 * A cross validation session identifier. Initialized once before to start operations.
	 */
	String tempPrefix = "";
	
	/**
	 * Cross validation specs
	 */
	CrossValidationSpecs cfg = null;
	
	/**
	 * The feature specs. They will be loaded from clean features. Are required to build the poprefs
	 */
	FeaSpecs feaSpecs;
	
	/**
	 * Groups composition for cross validation
	 */
	int [][]groups = null;
	
	/**
	 * Array of engines, one engine for each group
	 */
	SPEREC sperecs [] = null;
	
	
	SessionsTable_MyMatrix STM = null;

	
	LabeledArrayListOfSpeakers<MyMatrix> [] refSpeakersChunksByGroups = null;
	LabeledArrayListOfSpeakers<MyMatrix> [] poprefSpeakersChunksByGroups = null;

	Environment env;


	/**
	 * Very basic constructor
	 * @param cfg specs for cross-validation
	 * @throws Exception 
	 */
	public SperecCrossValidator(CrossValidationSpecs cfg, Environment env) throws Exception {
		
		this.env = (env==null)? new Environment() : env;
		
		// Fit the files paths to the local env
		cfg.cleanFeaCfgFile = env.hardFindFile(cfg.cleanFeaCfgFile);
		
		// Search and store the feature configuration files
		for (int i = 0; i<cfg.feaConfigFiles.length; i++)
			cfg.feaConfigFiles[i] = env.hardFindFile(cfg.feaConfigFiles[i]);
		
		this.cfg = cfg;
	}
	
	public SperecCrossValidator() {
		
	}
	
	
	/**
	 * Misc. debug operations
	 * @throws IOException
	 */
	private static void main_debug() throws IOException {
		
		String line = "" + Double.POSITIVE_INFINITY + "\t" + 2.0 + "\t" + "Pippo.er" + "\n";
		LabeledArrayListOfSpeakers [] refSpeakersChunksByGroups = (LabeledArrayListOfSpeakers[])new LabeledArrayListOfSpeakers[15];
		refSpeakersChunksByGroups[5] = null;

		ArrayList<LabeledArrayList<StRecord>> [] noisySpeakersByGroup = (ArrayList<LabeledArrayList<StRecord>>[])new ArrayList[15];
		noisySpeakersByGroup[8]= null;
		
		MyMatrix feaOfRefSpeaker = MyMatrix.randn(800,  24,  10);
		ArrayList<MyMatrix> refTargetFeaChunks_ = feaOfRefSpeaker.splitHorizontally(300, 1);

		LabeledArrayListOfFea refTargetFeaChunks = new LabeledArrayListOfFea(refTargetFeaChunks_);
		
		String ufvFile = "E:\\_DOTTORATO\\_DATA\\toSPLIT_toVAD_toFEA\\ufv\\Monologhi_001_150_16000Hz--vad---i-0.1--of-2.0--fea-mode_M__nc_24__np_24__feadim_24\\abook-072-DOnofrio-M-16000__split_0_VAD.ufv";
		MyMatrix fea = MyMatrix.readFromUfvFile(ufvFile);
		ArrayList<MyMatrix> f = fea.splitHorizontally(700, 1);
		
		ArrayList<MyMatrix> testFea = new ArrayList<MyMatrix>( Arrays.asList(fea));
		//testFea.add(fea); // Memorizzo sotto forma di array di matrici con una sola matrice
		
		
		System.out.println("Exit");


		// For debug
		//outputRes("E:\\_DOTTORATO\\SPEREC_tmp_files\\prova1.cvr", 45.5, 12.0, "errfile2.er")
	}
	
 	/**
	 * MAIN: entry point
	 * @param args
	 * @throws Exception
	 */
	public static void main(String [] args) throws Exception {
			
		if (false) //debug
		{
			main_debug();
			return;
		}
		
		// 1. Get the specs
		Environment env = new Environment();
		InputCrossValidationSpecs inputCrossValidationSpecs = new InputCrossValidationSpecs(env);
		CrossValidationSpecs cfg = inputCrossValidationSpecs.getSpecs();
		if (cfg==null)
			return;

		// 2. Do the work
		SperecCrossValidator me = new SperecCrossValidator(cfg, env);
		String inputCfgPath = inputCrossValidationSpecs.getConfigurationPath();
		String outputCfgPath = inputCfgPath; 
		me.work(outputCfgPath, true); // true = ask				
	}
	
	/**
	 * 
	 * @param cfg
	 * @throws Exception 
	 */
	public void work(String outputCfgPath, boolean ask) throws Exception {

		// 1. Check some input
		if (!check_input())
			return;

		// 2. Deal with some output inizialization
		if (!prepare_output(outputCfgPath, ask))
			return;
		
		
		// Load features of clean audio
		FeaDataSet cleanFeaDataset = FeaDataSet.fromConfigFile(cfg.cleanFeaCfgFile);
		if (null==cleanFeaDataset)
			return;
		
		// Store the feature specs
		feaSpecs = cleanFeaDataset.feaSpecs;
		
		// Qui sono già pronto per cachare le popref e pure per cachare l'enrollement
		ArrayList<LabeledArrayList<StRecord>> cleanSessionsBySpeaker = cleanFeaDataset.getArrayListOfRecordSets(); // sotto forma di array
		// Now, the cleanFeaDataSet has reference that are no more useful. cleanFeaDataset is now useless
					
		// Sia per l'enrollemnt che per la popref voglio utilizzare i dati chunkati, con durata enrollSessionDurationSec
		// Quindi prima chunko tutto, poi separo in base ai gruppi.
		int enrollSessionDurationFrames = (int)Math.round(cfg.specs.enrollSessionDurationSec/ feaSpecs.fFrameIncrementSec);
		
		STM = new SessionsTable_MyMatrix();
		LabeledArrayListOfSpeakers<MyMatrix> cleanSpeakersChunks = STM.makeChunks(cleanSessionsBySpeaker, enrollSessionDurationFrames);
		
		int numSpks = cleanSpeakersChunks.size(); // Numero di parlanti clean utilizzabili.
		if (numSpks<cfg.numberOfDevelSpks)
		{
			System.err.println("ERRORE: numero di parlanti insufficiente: " + numSpks);
			return;
		}
		int numGroups = (int)Math.ceil((double)numSpks/(double)cfg.numberOfDevelSpks); // The last group may be shorter
		groups = makeSpksGroups(numSpks, numGroups, cfg.numberOfDevelSpks);
		
		int poprefSize = numSpks - cfg.numberOfDevelSpks; // I want to keep constant the size of the popref	
		
		// Build popref, sperec anche reference speakers by group
		sperecs                      = new SPEREC [numGroups];
		refSpeakersChunksByGroups    = (LabeledArrayListOfSpeakers<MyMatrix>[])new LabeledArrayListOfSpeakers[numGroups];
		poprefSpeakersChunksByGroups = (LabeledArrayListOfSpeakers<MyMatrix>[])new LabeledArrayListOfSpeakers[numGroups];
		
		// Separate			
		for (int g=0; g<numGroups; g++)
		{			
			int [] currentGroupMembers = groups[g];
			
			// To build the popref use the enrollementSessions of the non-development speakers
			LabeledArrayListOfSpeakers<MyMatrix> popRefChunkedSessions = new LabeledArrayListOfSpeakers<MyMatrix>();
			LabeledArrayListOfSpeakers<MyMatrix> enrollmentChunkedSessions = new LabeledArrayListOfSpeakers<MyMatrix>();
			
			// Separate the speakers for reference population and those of enrollements
			selectSpeakers(cleanSpeakersChunks, currentGroupMembers, enrollmentChunkedSessions, popRefChunkedSessions);
				
			refSpeakersChunksByGroups[g] = enrollmentChunkedSessions;
			if (refSpeakersChunksByGroups[g].isEmpty()) { // It should never happen
				System.out.flush();
				System.err.println("ERROR: the group #" + g + " has no speaker useful for enrolling");
				System.err.println("Exiting.");
				return;
			}
			else
			{
				System.out.println("The group # " + g + " has " + refSpeakersChunksByGroups[g].size() + " speakers for enrolling.");
				System.out.flush();
			}
			
			// Adjust the size of the popref . If this applies, it should apply to the last group.
			while (popRefChunkedSessions.size()>poprefSize)
				popRefChunkedSessions.remove(popRefChunkedSessions.size()-1);
			
			poprefSpeakersChunksByGroups[g] = popRefChunkedSessions;
		}
		
		// Popref building
		build_popref(true); // the boolean is "use concurrency"	
								
		// Now begin the actual work.
		// Begin the main loop, that has many nested loops
		// Levels are:
		//		snr: loop through different SNR values
		//			testDur: loop through different test duration values
		// 				group : divide test speakers from reference population speakers
		// 					speakers
		//						sessions
		//							chunks
		// The most internal function is compareTwoChunks, which relies on oSperec.compareSpeakers
		for (int iSNR = 0; iSNR<cfg.SNRdB_v.length; iSNR++)
		{
			double snr = cfg.SNRdB_v[iSNR];
			String feaConfigFile = cfg.feaConfigFiles[iSNR];

			do_snr(snr, feaConfigFile);
		}

		System.out.println("DONE");
	}	


	/**
	 * Prepare the file to store the results of the cross validation
	 * @param cfgSavedFullPath_
	 * @param ask
	 * @return
	 * @throws IOException
	 */
	private boolean prepare_output(String cfgSavedFullPath_, boolean ask) throws IOException {
				
		// Deal with the configuration
		String cfgSavedFullPath = ask ? saveCfg(cfgSavedFullPath_) : cfgSavedFullPath_;
			
		if (cfgSavedFullPath.equals(""))
			return false;

		crossValOutFolder = new File(cfgSavedFullPath).getParent();
		tempPrefix = MiscUtils.getTemporaryFileName(crossValOutFolder);

		outputCrossValidationResultsFile = cfgSavedFullPath.endsWith(".cfg") ? cfgSavedFullPath.substring(0, cfgSavedFullPath.length()-4) :  cfgSavedFullPath;
		outputCrossValidationResultsFile = outputCrossValidationResultsFile + ".cvr";
		
		if (ask)
			outputCrossValidationResultsFile = SaveFile.as(outputCrossValidationResultsFile, "INDICA IL NOME DEL FILE DI OUTPUT DEI RISULTATI O ANNULLA PER ABBANDONARE" , "cvr"); // cvr=cross-validation resulst
		
		if (outputCrossValidationResultsFile.equals(""))
			return false;

		// Check if it is possible to write results, by initializing
		initOutputTable(outputCrossValidationResultsFile);
		
		return true;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	boolean check_input() throws IOException {
		
		boolean ok = true;
		
		System.out.flush();

		// Check clean fea
		if (!new File(cfg.cleanFeaCfgFile).exists())
		{
			System.err.println("ERROR: the file of clean features does not exist: " + cfg.cleanFeaCfgFile);
			ok = false;
		}
			
		
		// Check noisy fea
		int numberOfSnrValues = cfg.SNRdB_v.length;
		
		for (int iSNR = 0; iSNR<numberOfSnrValues; iSNR++)
		{
			double snr = cfg.SNRdB_v[iSNR];
			if (Math.abs(snr) != Double.POSITIVE_INFINITY)
			{
				String feaConfigFile = cfg.feaConfigFiles[iSNR];

				Double snrInConfigFile = new InputCrossValidationSpecs(env).getSnrFromConfigFiles(feaConfigFile);
				if (snrInConfigFile==null) {
					System.err.println("CANNOT VERIFY SNR VALUE: " + snr);
					ok = false;
				} else if ((snrInConfigFile.doubleValue()!=snr)) {
					System.err.println("ERROR: SNR MISMATCH: nominal = " + snr + ", whiel detected in fea config file is " + snrInConfigFile);
					ok = false;
				}
			}
		}
		
		return ok;
	}
	
	
	/**
	 * 
	 * @param use_concurrency
	 */
	private void build_popref(boolean use_concurrency) {
		
		if (use_concurrency)
			build_popref_concurrency();
			
		else // single thread
			build_popref_current_thread();
	}
	
	/**
	 * Create poprefs and sperec instance for all the groups, using the current thread
	 */
	private void build_popref_current_thread() {
		
		int numGroups = groups.length;
		for (int g=0; g<numGroups; g++)
			hlp_pop(g);
	}
	/**
	 * Create poprefs and sperec instance for all the groups, using concurrency
	 */
	private void build_popref_concurrency() {
		
		int numGroups = groups.length;
		new Parfor() {
			public void iter(int thread_idx, int g) {
				hlp_pop(g);
			}
		}.execute(0, numGroups);
	}
	
	/**
	 * Internal helper for pop ref creation and sperec setting
	 * @param g
	 */
	private void hlp_pop(int g) {
		
		LabeledArrayListOfSpeakers<MyMatrix> speakersChunks = poprefSpeakersChunksByGroups[g];
		AllSessionsArrays<MyMatrix> a = STM.toSessionsArrays(speakersChunks);

		try
		{
			POPREF_MODEL pop = new POPREF_Builder().build(cfg.specs, feaSpecs, a, false); // Can throw Exception
		
			// Need to select a SPEREC Engine
			SPEREC_Factory fact = new SPEREC_Factory();
			SPEREC oSperec = fact.createSperecEngine(cfg.specs.SPEREC_Type);
			oSperec.setPopRef(pop);
			sperecs[g] = oSperec;
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	
	
	/**
	 * 
	 * @param snr
	 * @param feaConfigFile
	 * @throws Exception 
	 */
	private void do_snr(double snr, String feaConfigFile_) throws Exception {
		
		// Import the features of noisy speech
		String feaConfigFile = env.findFile(feaConfigFile_);
		FeaDataSet noisyFeaDataset = FeaDataSet.fromConfigFile(feaConfigFile);
		if (noisyFeaDataset==null)
			return;
		
		double noisyFeaIncrement = noisyFeaDataset.feaSpecs.fFrameIncrementSec;
		
		// Assing noisy speakers to groups
		// Per ogni gruppo dei clean, individuo per nome i corrispondenti paraltni noisy e faccio un gruppo.
		// Alla fine, resteranno quei parlanti che, in versione clean, erano troppo corti.
		// Questi parlanti li aggiungo una volta sola nell'ultimo gruppo.
		// Perciò nell'ultimo gruppo avrò più confronti non-target del solito, ma non importa
		ArrayList<LabeledArrayList<StRecord>> [] noisySpeakersByGroup = assignSpeakersToGroups(noisyFeaDataset);	
		//ArrayList<LabeledArrayList<StRecord>> [] noisySpeakersByGroup = assignSpeakersToGroups(allNoisySpeakers);
		// Now FeaDataset has become useless
		
		// Loop 
		int nDur = cfg.testSessionDurationSec_v.length;
		int numGroups = groups.length;
		
		//new Parfor() { public void iter(int thread_idx, int iDur)
		int thread_idx = 0; for (int iDur = 0; iDur<nDur; iDur++)
		{
			double testDurationSec = cfg.testSessionDurationSec_v[iDur];
			
			// Per convertire secondi in numero di frames, divido i secondi per feaDataSet.feaSpecs.fFrameIncrementSec
			int testDurationFrames = (int)Math.round(testDurationSec / noisyFeaIncrement);
			double actualDurationSec = (double)testDurationFrames * noisyFeaIncrement; // for debug
			System.out.println(	"testDurationSec = " + testDurationSec + 
								" ==> testDurationFrames = " + testDurationFrames + 
								" ==> actualDurationSec = " + actualDurationSec); 
			
			ArrayList<Boolean> target = new ArrayList<Boolean>(); // Collect isTarget information for the current combination of NOISE,DUR
			ArrayList<Double> scores = new ArrayList<Double>(); // Collect scores for the current combination of NOISE,DUR
			
			// Now we have established the two parameters: noise and dur.
			// Now we can go on with the numGroups-fold crossvalidation,
		
			boolean use_concurrency = true;
			//boolean use_concurrency = false;
			
			// ----------------------------------  Loop through the groups: BEGIN
			if (use_concurrency)
			{
				System.out.println("USING THREAD CONCURRENCY");
				new Parfor() { public void iter(int thread_idx, int g)
				{		
					do_group(g, noisySpeakersByGroup, testDurationFrames, target, scores, thread_idx);
				}}.execute(0, numGroups);
			}
			else
			{
				System.out.println("USING SINGLE THREAD");
				for (int g=0; g<numGroups; g++)		
					do_group(g, noisySpeakersByGroup, testDurationFrames, target, scores, 0);
			}
			// ----------------------------------  Loop through the groups: END
			
							
			// Alla fine, quando sono finalmente ritornato nel thread principale, unisco tutti gli array scores e target
			if (scores.size()>0)
				output(snr, testDurationSec, target, scores);
			else
				System.err.println("WARNING: no results");

		//}}.execute(0, nDur);
		}
	
	}
	
	/**
	 * 
	 * @param g
	 * @param noisySpeakersByGroup
	 * @param testDurationFrames
	 * @param target
	 * @param scores
	 * @param thread_idx
	 */
	private void do_group(int g, ArrayList<LabeledArrayList<StRecord>> [] noisySpeakersByGroup, int testDurationFrames, ArrayList<Boolean> target, ArrayList<Double> scores, int thread_idx) {
		
		int numGroups = groups.length;

		System.out.println("Thread " + thread_idx + " Processing group " + g + " out of " + numGroups);
		SPEREC oSperec = sperecs[g];
		
		// Creo qui gli array di scores e target, che quindi saranno proprietà esclusiva del thread corrente
		ArrayList<Boolean> target_g = new ArrayList<Boolean>(); // Collect isTarget information for the current combination of NOISE,DUR, for the curretn group g
		ArrayList<Double> scores_g = new ArrayList<Double>(); // Collect scores for the current combination of NOISE,DUR, for the curretn group g
		
		ArrayList<LabeledArrayList<StRecord>> currentNoisySpeakersSessions = noisySpeakersByGroup[g];

		SessionsTable_MyMatrix STM = new SessionsTable_MyMatrix();
		try
		{
			// Chunko ora
			LabeledArrayListOfSpeakers<MyMatrix> currentNoisySpeakersChunkedSessions = STM.makeChunks(currentNoisySpeakersSessions, testDurationFrames);

			LabeledArrayListOfSpeakers<MyMatrix> currentReferenceSpeakersChunkedSessions = refSpeakersChunksByGroups[g];

			compareArrayListOfChunkedSpeakers(currentNoisySpeakersChunkedSessions, currentReferenceSpeakersChunkedSessions, oSperec, target_g, scores_g);

			// merge with the main collection
			synchronized(this) {
				target.addAll(target_g);
				scores.addAll(scores_g);
			}
		} catch (Exception e) {
			e.printStackTrace();	
		}
	}
	
	/**
	 * Assing noisy speakers to groups
	 * @param noisyFeaDataset
	 * @return
	 */
	/*private ArrayList<LabeledArrayList<StRecord>> [] assignSpeakersToGroups(ArrayList<LabeledArrayList<StRecord>> allNoisySpeakers) {
	
		// Initialize the output
		ArrayList<LabeledArrayList<StRecord>> [] noisySpeakersByGroup = (ArrayList<LabeledArrayList<StRecord>>[])new ArrayList[numGroups];
			
		for (int g=0; g<numGroups; g++)
		{
			int [] selectedIndexes = groups[g];
			ArrayList<LabeledArrayList<StRecord>> selected = new ArrayList<LabeledArrayList<StRecord>>();

			for (int i = 0; i<selectedIndexes.length; i++)
				selected.add(allNoisySpeakers.get(selectedIndexes[i]));
			
			noisySpeakersByGroup[g] = selected;
		}
		
		return noisySpeakersByGroup;
	}*/
	
	/**
	 * 
	 * @param noisyFeaDataset
	 * @return
	 */
	private ArrayList<LabeledArrayList<StRecord>> [] assignSpeakersToGroups(FeaDataSet noisyFeaDataset) {
		int numGroups = groups.length;
		ArrayList<LabeledArrayList<StRecord>> [] noisySpeakersByGroup = (ArrayList<LabeledArrayList<StRecord>>[])new ArrayList[numGroups];
		for (int g = 0; g<numGroups; g++)
		{
			// Indexes of group g. I need the size only
			int [] group = groups[g];

			ArrayList<LabeledArrayList<StRecord>> lista = new ArrayList<LabeledArrayList<StRecord>>(); 

			// Clean speakers (chunked sessions) of group g
			LabeledArrayListOfSpeakers<MyMatrix> enrollmentChunkedSessions = refSpeakersChunksByGroups[g];
			for (int s = 0; s<group.length; s++)
			{
				LabeledArrayListOfSessions cleanChunkedSession = enrollmentChunkedSessions.get(s);
				String spkName = cleanChunkedSession.label;
				LabeledArrayList<StRecord> noisySpk = noisyFeaDataset.cut(spkName); // At the end I must recover the remaining ones
				lista.add(noisySpk);
			}
			noisySpeakersByGroup[g] = lista;
		}
		// At the end, must be left those speakers too short for enrollment purpose, but in noisy version.
		// I am going to attach at the end of the last group.
		ArrayList<LabeledArrayList<StRecord>> remainingNoisySpeakers = noisyFeaDataset.getArrayListOfRecordSets();
		int destGroup = numGroups-1;
		noisySpeakersByGroup[destGroup].addAll(remainingNoisySpeakers);
		return noisySpeakersByGroup;
	}
	
	
	/**
	 * 
	 * @param snr
	 * @param testDur
	 * @param target
	 * @param scores
	 * @throws Exception
	 */
	void output(double snr, double testDur, ArrayList<Boolean> target, ArrayList<Double> scores) throws Exception
	{
		// Compose the filename
		String baseFileName = tempPrefix + "__snr_" + snr + "__testdur_" + testDur;
		
		// Fix for Android
		baseFileName = MiscUtils.fixStringForFileName(baseFileName);
		
		// Add the file extension
		String errFileName = baseFileName + ".er";
		
		// Build the fullpath
		String fullPathErrFile = crossValOutFolder + File.separator + errFileName;
    
		// Build the object to save
		ValidationResultGauss valRes = new ValidationResultGauss(scores, target);
		
		// Save the object
		valRes.writeErrorRatesToMatrixFile(fullPathErrFile);
		valRes.reduceMemory(); // After having saved the Error Rates curves, this will delete some memory
    
		// Write the output table
		outputResToTable(outputCrossValidationResultsFile, snr, testDur, errFileName);
		
	}
	
	
	/**
	 * 
	 * @param outputfile
	 * @throws IOException
	 */
	private void initOutputTable(String outputfile) throws IOException {
		
		String line = "#CREATED ON: " + env.now() + "\t ON MACHINE: " + env.getComputerName() + "\n"
				+ "#SNR\ttstDur\terrors_file\n";
    	
		Path pa = Paths.get(outputfile);
		if (!Files.exists(pa))
	    	Files.write(pa, line.getBytes(), StandardOpenOption.CREATE_NEW);
		else
		    Files.write(pa, line.getBytes(), StandardOpenOption.APPEND);
	}
	/**
	 * 
	 * @param snr
	 * @param testDurSec
	 * @param errFile
	 * @throws IOException
	 */
	private void outputResToTable(String outputfile, double snr, double testDurSec, String errFile) throws IOException {
		
		Path pa = Paths.get(outputfile);
	    if (!Files.exists(pa))
	    	initOutputTable(outputfile);

	    String line = "" + snr + "\t" + testDurSec + "\t" + errFile + "\n";		    	
	    Files.write(Paths.get(outputfile), line.getBytes(), StandardOpenOption.APPEND);
	}
	
	
	
	/**
	 * 
	 * @param testSpeakerSessions
	 * @param referenceSpeakersSessions
	 * @param noisyFeaDataset
	 * @param oSperec
	 * @param target
	 * @param scores
	 * @throws Exception
	 */
	private void compareArrayListOfChunkedSpeakers(	LabeledArrayListOfSpeakers<MyMatrix> testSpeakerChunkedSessions,
													LabeledArrayListOfSpeakers<MyMatrix> referenceSpeakersChunkedSessions,
													SPEREC oSperec,
													ArrayList<Boolean> target, ArrayList<Double> scores) throws Exception {
		
		int numTestSpeakers = testSpeakerChunkedSessions.size();
		int numRefSpeakers = referenceSpeakersChunkedSessions.size();
		
		for (int iTestSpeaker=0; iTestSpeaker<numTestSpeakers; iTestSpeaker++)
		{
			LabeledArrayListOfSessions<MyMatrix> sessionsOfCurrentTestSpeaker = testSpeakerChunkedSessions.get(iTestSpeaker);
			for (int iRefSpeaker=0; iRefSpeaker<numRefSpeakers; iRefSpeaker++)
			{			
				LabeledArrayListOfSessions<MyMatrix> sessionsOfCurrentRefSpeaker = referenceSpeakersChunkedSessions.get(iRefSpeaker);
								
				compareTwoSpeakers(sessionsOfCurrentTestSpeaker, sessionsOfCurrentRefSpeaker, oSperec, target, scores);
			}
		}
		
	}
	
	
		
	/**
	 * 
	 * @param testSessions
	 * @param refSessions
	 * @param noisyFeaDataset
	 * @param oSperec
	 * @param isTarget
	 * @param target
	 * @param scores
	 * @throws Exception
	 */
	private void compareTwoSpeakers(LabeledArrayListOfSessions<MyMatrix> testSessions,
									LabeledArrayListOfSessions<MyMatrix> refSpeaker,
									SPEREC oSperec,
									ArrayList<Boolean> target,
									ArrayList<Double> scores) throws Exception {
		
		
		String testSpkName = testSessions.label;
		String refSpkName = refSpeaker.label;
		boolean isTarget = testSpkName.equals(refSpkName);
		
		int numTestSessions = testSessions.size();
		int numRefSessions = refSpeaker.size();
				
		for (int iTestSession=0; iTestSession<numTestSessions; iTestSession++)
		{
			//StRecord currentTestSession = testSessions.get(iTestSession);
			
			//  																			Spezzetta la sessione in base alla durata
			LabeledArrayList<MyMatrix> testChunkedSession = testSessions.get(iTestSession);; //SessionsTable.chunkSession(currentTestSession, testDurationFrames);
			
			for (int iRefSession = 0; iRefSession<numRefSessions; iRefSession++)
			{
				LabeledArrayList<MyMatrix> refChunkedSession = refSpeaker.get(iRefSession);
				String refChunkLabel = refChunkedSession.label;

				// Posso confrontare soltanto se i nomi delle sessioni sono diversi
				if (! (isTarget && testChunkedSession.label.equals(refChunkLabel)) )
				{
					compareTwoChunkedSessions(oSperec, testChunkedSession, refChunkedSession, isTarget, target, scores);
				}
			}
		}
	}
	
	
	
	/**
	 * Compare each chunk of a test session, with each chunk of a reference session
	 * @param testSession
	 * @param refSession
	 * @param noisyFeaDataset
	 * @param oSperec
	 * @param isTarget
	 * @param target
	 * @param scores
	 * @throws Exception
	 */
	public void compareTwoChunkedSessions(	SPEREC oSperec,
											LabeledArrayList<MyMatrix> testFeaChunks,
											LabeledArrayList<MyMatrix> refTargetFeaChunks,
											boolean isTarget,
											ArrayList<Boolean> target, ArrayList<Double> scores) throws Exception {
		
		
		int numberOfTestSessions = testFeaChunks.size();
		
		// Iterate over testFeaChunks
		for (int testSe = 0; testSe<numberOfTestSessions; testSe++)
		{
			// Take a chunk from the matrix of the features of the test speaker
			MyMatrix testFeatures = testFeaChunks.get(testSe);
			ArrayList<MyMatrix> testFea = new ArrayList<MyMatrix>( Arrays.asList(testFeatures)); // Memorizzo sotto forma di array di matrici con una sola matrice

			// Ora ciclo sui chunk della sessione reference
			for (int iRefSe = 0; iRefSe < refTargetFeaChunks.size(); iRefSe++ ) // Se la sessione era vuota (non soddisfaceva la lughezza minima, allora .size=0 e il loop non viene eseguito
			{ 
				MyMatrix refChunkMatrix = refTargetFeaChunks.get(iRefSe);
				ArrayList<MyMatrix> refFea = new ArrayList<MyMatrix>( Arrays.asList(refChunkMatrix)); // Memorizzo sotto forma di array di matrici con una sola matrice
				
				// Do the comparison
				compareTwoChunks(oSperec, refFea, testFea, isTarget, target, scores);

			} // End of reference speakers loop
		} // End of loop of test sessions
		
	}
		
	/**
	 * The core comparison
	 * @param oSperec
	 * @param refFea
	 * @param testFea
	 * @param isTarget
	 * @param target
	 * @param scores
	 * @return
	 */
	private AuthenticationResult compareTwoChunks(	SPEREC oSperec, ArrayList<MyMatrix> refFea,
							ArrayList<MyMatrix> testFea,
							boolean isTarget,
							ArrayList<Boolean> target, ArrayList<Double> scores) {
		
		AuthenticationResult ares = null;
		
		try {
			// Call the SPEREC API
			ares = oSperec.compareSpeakers(refFea, testFea);
			
			// It is very important that target and score get updated at the same time
			// This is why I propagated isTarget until here.
			target.add(isTarget);
			scores.add(ares.score);

		} catch (Exception e) {
			// Go on. I don'care about. Simply, the two chunks were not comparable.
			//e.printStackTrace();
			//return; //return false;
		}
		
		return ares; //useless
	}
	
	
	
	/**
	 * 
	 * @param inChunkedSessions
	 * @param selectedIndexes
	 * @param selected
	 * @param remaining
	 */
	private static void selectSpeakers(	LabeledArrayListOfSpeakers inChunkedSessions, int [] selectedIndexes, 
										LabeledArrayListOfSpeakers selected, LabeledArrayListOfSpeakers remaining)
	{
		int numSpeakers = inChunkedSessions.size();
		
		// Select 
		/*if (selected!=null)
			for (int i = 0; i<selectedIndexes.length; i++)
			 // Potrebbe accadere che un indice faccia ora riferimento ad un parlante che era troppo corto quindi senza chunk
				if (selectedIndexes[i]<inChunkedSessions.size())
					selected.add(inChunkedSessions.get(selectedIndexes[i]));*/
		
		// Select
		if (selected!=null)
			for (int i = 0; i<selectedIndexes.length; i++)
				selected.add(inChunkedSessions.get(selectedIndexes[i]));
							
		// The remaining ones
		if (remaining!=null) // If s is not inside selectedIndexes, the take it
			for (int s=0; s<numSpeakers; s++)
				if (!contains(selectedIndexes, s))
					remaining.add(inChunkedSessions.get(s));

	}
	
	/**
	 * 
	 * @param vector
	 * @param x
	 * @return
	 */
	private static boolean contains(int [] vector, int x) {
			
		for (int j=0; j<vector.length; j++)
			if (x==vector[j])
				return true;
		
		return false;
	}
	
	
	/**
	 * 
	 * @param totElems
	 * @param numGroups
	 * @param numElems
	 * @return
	 */
	/*
	private static int [][] makeSpksGroups(int totElems, int numGroups, int numElems) {

		int [] indici = new int[totElems];
		for (int i = 0; i<totElems; i++)
			indici[i] = i;
		shuffleArray(indici);

		int [][] groups = new int[numGroups][numElems];

		int i = 0;
		for (int g=0; g<numGroups; g++)
			for (int s=0; s<numElems; s++)
				groups[g][s] = indici[i++];

		return groups;
	}*/
	
	
	private static int [][] makeSpksGroups(int totElems, int numGroups, int maxSize) {
		int [] indici = new int[totElems];
		for (int i = 0; i<totElems; i++)
			indici[i] = i;
		shuffleArray(indici);
		int i = 0;
		int [][] groups = new int[numGroups][];
		for (int g=0; g<numGroups; g++)
		{
			int numRemainingElements = totElems-i;
			int size = numRemainingElements>maxSize ? maxSize : numRemainingElements;
			int [] thisGroup = new int [size];
			for (int s=0; s<size; s++)
				thisGroup[s] = indici[i++];
			
			groups[g] = thisGroup;
		}
		return groups;
		
	}


	/**
	 * Implementing Fisher–Yates shuffle
	 * @param ar input/output array to be shuffled
	 */
	private static void shuffleArray(int[] ar)
	{
		// If running on Java 6 or older, use `new Random()` on RHS here
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--)
		{
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	
	/**
	 * Save the configuration
	 * @return
	 * @throws IOException
	 */
	private String saveCfg(String initPath) throws IOException {
		
		String cfgSavedFullPath = "";
		
		if (cfg!=null)
		{
			InputCrossValidationSpecs inputCrossValidationSpecs = new InputCrossValidationSpecs(env);
			ConfigurationFile outConfigFile = new ConfigurationFile();
			outConfigFile.addSection(inputCrossValidationSpecs.cfgSectionName);
			outConfigFile.addItem(inputCrossValidationSpecs.cfgSectionName, inputCrossValidationSpecs.cfgItemName, cfg.toJsonString());
	
			cfgSavedFullPath = SaveFile.as(initPath, "Salva il file di configurazione", "cfg");
			if (!cfgSavedFullPath.equals(""))			
				outConfigFile.saveAs(cfgSavedFullPath); // Actually save the configuration
		}
		
		return cfgSavedFullPath;
	}
	
	/**
	 * Attach a validation results file to a popref configuration file
	 * @throws Exception
	 */
	public void attachValidationResults(JTextArea output) throws Exception {
			
		String MAIN_OUTPUT_FOLDER_PATH = "";
		String cfgString;
		final String newline = "\n";

		
		// ------------------------------------------ STEP 1
		// Select the validation result file
		String valresFilePath = ChooseFile.get(MAIN_OUTPUT_FOLDER_PATH, "Choose the file with validation results", "(*.val; *.cvr)", "val", "cvr");
		if ( (null==valresFilePath) || (valresFilePath.equals("")))
			return;
		
		// Import that file
		//SPEREC_Loader_JVM sperec_loader = new SPEREC_Loader_JVM();
		//ValidationResults valRes = sperec_loader.loadValidationResults(valresFilePath); // In realta' non serve

		output.append("Loaded Cross validation result file: " + valresFilePath + newline);
		InputStream is = new SPEREC_Loader_JVM().getInputStream(valresFilePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for(String line; (line = br.readLine()) != null; )
			outputToUser(line + newline);
		br.close();

		
		// ------------------------------------------ STEP 2

		// Select the POPREF specs
		String popRefCfgPath = ChooseFile.get(MAIN_OUTPUT_FOLDER_PATH, "Choose the file of the reference population configuration", "(*.cfg)", "cfg");
		if ( (null==popRefCfgPath) || (popRefCfgPath.equals("")))
			return;
		
		ConfigurationFile popRefCfg = ConfigurationFile.load(popRefCfgPath);

		// Dump the cfg content
		cfgString = popRefCfg.toString();
		System.out.println(cfgString);
		output.append("Loaded Reference Population configuration file: " + popRefCfgPath + newline);
		output.append(cfgString + newline);
		output.setCaretPosition(output.getDocument().getLength());
		
		//String efnContent = valResCfg.getItem(sectionName, itemName)
		
		// ------------------------------------------ STEP 3
		// Merge
		//popRefCfg.addItem("SPEREC", "validationResultEfn", EFN.toJsonString());
		popRefCfg.addItem("SPEREC", "validationResultEfn", valresFilePath); // Ottobre 2018
		
		popRefCfg.dump();	
		
		// ------------------------------------------ STEP 4
		// Save
		popRefCfg.saveAs(new File(popRefCfgPath));
		
		output.append("Configuration file updated: " + popRefCfgPath + newline);
		output.setCaretPosition(output.getDocument().getLength());
		
	}
	
	/**
	 * 
	 * @param output
	 * @throws Exception
	 */
	public void showValidationResults(JTextArea output) throws Exception {
		
		outputToUser("showValidationResults: start" + newline);
		String inputFile = ChooseFile.get("", "Select a cross validation result file" , "Cross validation results (*.val;*.cvr; *.er)", "val", "cvr", "er");
		if (inputFile!=null)
			outputToUser("Selected input file: " + inputFile);
		if ((inputFile!=null) && !inputFile.equals(""))
		{
			String ext = inputFile.substring(inputFile.lastIndexOf("."), inputFile.length());
			if (ext.equals(".er"))
				showEr(inputFile);
		}
		
		outputToUser(newline + "showValidationResults: end" + newline);
	}
	
	void showEr(String errFilePath) throws Exception {
		ValidationResultOpen v = new ValidationResultOpen();
		v.loadErrorRatesFromMatrixFile(errFilePath);
		v.ErrorRateFile = errFilePath;
		double [] scores = v.getScores();
		outputToUser(newline + "scores samples: " + scores.length);
		
		MyChart CH1 = new MyChart("FNR: ");
		//CH1.plot(scores, v.getFNR());
		
		
		//MyChart CH2 = new MyChart("FPR: ");
		//CH2.plot(scores, v.getFPR());
		
		double [] scoreFN = MyMath.select(scores, v.getLables());
		double FNavg = MyMath.mean(scoreFN);
		double FNstd = MyMath.stdev(scoreFN,  FNavg);
		int FNn = scoreFN.length;
		double [] FNrateGauss = new double [FNn];
		NormalDistribution FNGauss = new NormalDistribution(FNavg, FNstd);
		for (int i=0; i<FNn; i++)
			FNrateGauss[i] = FNGauss.cumulativeProbability(scoreFN[i]);
		
		MyChart CH1gauss = new MyChart("FNR (Gauss)");
		CH1gauss.plot(scoreFN, FNrateGauss);
		
		//double [] scoreTN = MyMath.select(scores,  MyMath.not(v.getLables()));
		
		
	}
	
	void outputToUser(String msg) {
		if (output!=null)
		{
			output.append(msg);
			output.setCaretPosition(output.getDocument().getLength());
		}
		System.out.println(msg);
	}
}
