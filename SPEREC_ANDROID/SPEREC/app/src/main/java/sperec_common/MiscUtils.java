package sperec_common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

//import org.apache.commons.io.FilenameUtils;


public class MiscUtils {

	/**
	 * 
	 * @return a unique filepath+filename in the system temporary folder, or "" if it fails
	 */
	public static String getTemporaryFileFullPath() {

		String tmpDir = System.getProperty("java.io.tmpdir");
		return MiscUtils.getTemporaryFileFullPath(tmpDir);
	}

	/**
	 * 
	 * @return a unique filepath+filename in the tmpDir folder, or "" if it fails
	 */
	public static String getTemporaryFileFullPath(String tmpDir) {

		return MiscUtils.getTemporaryFileName(tmpDir);
	}

	/**
	 * 
	 * @param tmpDir
	 * @return a unique filename (just the name, no path) in the tmpDir folder, or "" if it fails
	 */
	public static String getTemporaryFileName(String tmpDir) {
		File f = null;
		int maxCount = 10;
		String temporaryOutputFile = "";

		while (maxCount-- >= 0) {
			temporaryOutputFile = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new java.util.Date()); //Use _ for Android...
			temporaryOutputFile = "f_" + temporaryOutputFile; //Andoird resource file names need to start with a letter
			f = new File(tmpDir + File.separator + temporaryOutputFile);
			if (!f.exists()) {
				maxCount = -1; //Exit loop
			} else {
				try {
					Thread.sleep((long)(Math.random() * 1000)); // Sleep a random amount of ms
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (f.exists())
			return "";
		else
			return temporaryOutputFile;

	}

	/**
	 * Collect names of files with specific extension in an ArrayList<String> and (optionally) write to a File
	 * @param feaDir
	 * @param feaList
	 * @param fileExt: file etension with initial "."
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> dumpFileList(String feaDir, String feaList, String fileExt) throws IOException {

		ArrayList<String> arr = new ArrayList<String>();

		//if (fileExt.startsWith(".")) { // Remove the initial point because of FilenameUtils.getExtension
		//	fileExt = fileExt.substring(1);
		//}

		BufferedWriter writer = ((feaList==null) || feaList.isEmpty()) ? null: new BufferedWriter(new FileWriter(new File(feaList)));

		File[] directoryListing = new File(feaDir).listFiles();
		String newline = System.getProperty("line.separator");
		if (directoryListing != null)
		{
			for (File child : directoryListing)
			{
				String fname = child.getName();
				int p = fname.lastIndexOf(".");
				String ext = fname.substring(p);
				if (ext.equals(fileExt))
				{
					arr.add(child.getName());

					if (null!=writer)
						writer.write(child.getName()+newline);
				}
			}
		}

		if (null!=writer)
			writer.close();

		return arr;
	}

	/**
	 * Write the strings in the input array as lines in the provided text file
	 * @param filepath
	 * @param arr
	 * @throws IOException
	 */
	public static void writeTextLinesToFile(String filepath, ArrayList<String> arr) throws IOException {

		File ffile = new File(filepath);
		BufferedWriter w = new BufferedWriter(new FileWriter(ffile));
		String newline = System.getProperty("line.separator");

		for (int i=0; i<arr.size(); i++)
			w.write(arr.get(i) + newline);

		w.flush();
		w.close();
	}

	/**
	 * Fill an ArrayList of Strings read from a text file
	 * @param filepath: the path of the text file
	 * @return: the ArrayList of lines
	 * @throws IOException
	 */
	public static ArrayList<String> loadTextLines(String filepath) throws IOException {

		ArrayList<String> res = new ArrayList<String>();

		File logFile = new File(filepath);
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		String line;
		while ((line = br.readLine()) != null) {
			res.add(line);
		}
		br.close();
		return res;

	}

	/**
	 * Create a new list based on the provided one, without the elements indicated in spks.
	 * @param a: the original list of Strings
	 * @param ind: the indexes of elements to remove
	 * @return: the new "filtered" list.
	 */
	public static ArrayList<String> filterArrayListOfStrings (ArrayList<String> a, int [] ind) {

		@SuppressWarnings("unchecked")
		ArrayList<String> res = (ArrayList<String>)a.clone();

		String mark = MiscUtils.getTemporaryFileName(null); // was mark = ""

		// Mark the elements to remove without messing the indexes sequence.
		for (int is =0; is<ind.length; is++)
			res.set(ind[is], mark); // This will not affect the original array, I checked.

		// Actually remove the previously marked elements
		int s = 0;
		while(s<res.size())
		{
			if (res.get(s).equals(mark))
				res.remove(s);
			else
				s++;
		}
		return res;
	}


		
	/*
	 * 
	 */
	public static double [] imax (double [] v) {
		
		int imax = 0;
		double max = -1e16;
		for (int i=0;i<v.length; i++)
		{
			if (v[i]>max) {
				max = v[i];
				imax = i;
			}
		}
		double [] res = new double[2];
		res[0] = max; res[1] = (double)imax;
		return res;
	}


	public static Short [] simulate_classes(int nobs, int nsess) {
		
		Short [] classes = new Short[nobs];
		//int b= 3 ; ///=nobs/20+1;
		int ii = 0;
		short cl = 0;
		while (ii<nobs) {
			int n = Math.min(nobs-ii, nsess);
			for (int i=ii; i<ii+n; i++)
				classes[i] = cl;
			ii+=n;
			cl++;
		}
		
		/*
		 // QUi assumo che le classi (i) siano tutte diverse, anche se non ha molto senso
		for (short i=0; i<nobs; i++)
			classes[i] = i;
		*/
		
		return classes;
	}
	
	/**
	 * 
	 * @param tStart
	 */
	public static String showElapsedTime(long tStart) {
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = Math.round(tDelta / 1000.0);
		String msg = "Elapsed time: " + elapsedSeconds + " [s]";
		System.out.println(msg );
		return msg;
	}
	

	/**
	 * 
	 * @param dirPath
	 * @param dotExt
	 * @return
	 */
	public static File [] searchForFilesByDotExtension(String dirPath, final String dotExt) { // e.g. ".wav"
		System.out.print("Searching for *" + dotExt + " into " + dirPath + " ....");
		File f = new File(dirPath);
		if (!f.exists()) {
			System.err.println("\nERROR: folder dirPath does not exist");
			return null;
		}
		
		File [] directoryListing = new File(dirPath).listFiles(new FilenameFilter () {
			public boolean accept(File f, String name) {
				//boolean notisdir = !f.isDirectory();
				boolean endswith = name.toLowerCase().endsWith(dotExt);
				//System.out.println(name);
				return endswith; //return (notisdir && endswith); //(!f.isDirectory() && name.toLowerCase().endsWith(dotExt));
			}
		});
		if (directoryListing.length==1)
			System.out.println(" found " + directoryListing.length + " file.");
		else
			System.out.println(" found " + directoryListing.length + " files.");
		return directoryListing;
	}
	
	/**
	 * 
	 * @param inputDirOrFileList
	 * @param classNames
	 * @return
	 * @throws IOException
	 */
	public static String [] getListOfAudioInputFiles(String inputDirOrFileList, ArrayList<String> classNames) throws IOException {
		
		String [] inputArray = null;
		
		if (new File(inputDirOrFileList).isDirectory())
		{
			File [] inputFileArray = MiscUtils.searchForFilesByDotExtension(inputDirOrFileList, ".wav");
			inputArray = new String[inputFileArray.length];
			for (int i=0; i<inputArray.length; i++)
				inputArray[i] = inputFileArray[i].getAbsolutePath();	
			
		}
		else
		{
			//String inputDir = FilenameUtils.getFullPath(inputDirOrFileList);
			String inputDir = new File(inputDirOrFileList).getParent();
			
			// Read the input list
			ArrayList<String> inputFileNames = new ArrayList<String>();
			SessionsTable_MyMatrix.readSessionsListFromFile(inputDirOrFileList, inputFileNames, classNames);
			
			inputArray = new String[inputFileNames.size()];
			for (int i=0; i<inputArray.length; i++)
			{
				// Build the full path of the audio input file
				String inputAudioFullPath = inputDir + File.separator + inputFileNames.get(i);
				inputArray[i] = inputAudioFullPath;
			}
		}
		return inputArray;		
	}
	
	/**
	 * Convert, if necessary, a string into a valid file (or folder) name
	 * @param s
	 * @return
	 */
	public static String fixStringForFileName(String s) {
		return s.equals("")?  "" : s.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
	}
	
	/**
	 * For test and debug
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String []args) throws IOException {
		
		String inputFileList = "http:\\127.0.0.1\\_DOTTORATO\\_DATA\\audioDataOrig\\Monologhi_001_150\\Monologhi_001_150_16000Hz\\file.lst";
		String thisClass = new Object() { }.getClass().getEnclosingClass().getSimpleName();
		System.out.println(thisClass + " : Reading: " + inputFileList);
		ArrayList<String> classNames = new  ArrayList<String>();
		String [] fList = getListOfAudioInputFiles(inputFileList, classNames);
		System.out.println(fList.length + " items read");
		System.out.println("Done");
	}
}
