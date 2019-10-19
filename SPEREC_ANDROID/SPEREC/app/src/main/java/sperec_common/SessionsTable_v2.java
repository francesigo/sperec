package sperec_common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;




/**
 * I'm trying to create a generic SessionTable
 * @author FS
 *
 */
public abstract class SessionsTable_v2<T> implements Serializable {
	
	
	String inputDir = "";
	String inputFileList = "";
	final Class<T> typeParameterClass;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7065852547315992406L;
	
	public SessionsTable_v2 (Class<T> typeParameterClass) {
		this.typeParameterClass = typeParameterClass;
	}

	

	/*// Used to get a view of the current content, as arrays
	public class AllSessionsArrays<T> {
		//public ArrayList<String> classNames = new ArrayList<String>(); useless
		public ArrayList<String> sessionNames = new ArrayList<String>();
		public ArrayList<T> data = new ArrayList<T>();
		public Short [] classes = null;
	}*/
	
	
	/**
	 * The core of the table: is a HashMap where the key are class names (String)
	 */
	HashMap<String, LabeledArrayList<StRecord>> tableString = new HashMap<String, LabeledArrayList<StRecord>>();
	
	/**
	 * Get the LabeledArrayList of StRecords, which corresponds to key
	 * @param key
	 * @return
	 */
	public LabeledArrayList<StRecord> get(String key) {
		return tableString.get(key);
	}
	
	/**
	 * Remove the LabeledArrayList of StRecords which corresponds to key
	 * @param key
	 */
	public void remove(String key) {
		tableString.remove(key);
	}
	
	/**
	 * get and remove
	 * @param key
	 * @return
	 */
	public LabeledArrayList<StRecord> cut(String key) {
		LabeledArrayList<StRecord> x = get(key);
		remove(key);
		return x;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getNumberOfUniqueClasses() {
		return tableString.size();
	}
	
	/**
	 * Clear the hashmap
	 */
	public void clear() {
		tableString.clear();
	}
	
	/**
	 * Return the content in order to use indexes. After that, the SessionsTable should not changed any more
	 * @return
	 */
	public ArrayList<LabeledArrayList<StRecord>> getArrayListOfRecordSets() {
		// Questa soluzione funziona ma non mi consente di recuperare le chiavi
		//ArrayList<LabeledArrayList<StRecord>> a = new ArrayList<LabeledArrayList<StRecord>>(this.tableString.values());
		
		ArrayList<LabeledArrayList<StRecord>> a = new ArrayList<LabeledArrayList<StRecord>>();
		Iterator<Entry<String, LabeledArrayList<StRecord>>> it = tableString.entrySet().iterator();
	    while (it.hasNext())
	    {
	        Map.Entry<String, LabeledArrayList<StRecord>> pair = (Map.Entry<String, LabeledArrayList<StRecord>>)it.next();
	        //System.out.println(pair.getKey() + " = " + pair.getValue());
	        LabeledArrayList<StRecord> elem = new LabeledArrayList<StRecord>();
	        LabeledArrayList<StRecord> v = pair.getValue();
	        elem.label = pair.getKey();
	        elem.addAll(v);
		     
	        a.add(elem);
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		return a;
	}
	
	/**
	 * Useful for POPREF building, using dest.data as List of MyMatrix and dest.classes (to be converted into [])
	 * Also, it is useful to manage indexing OF SESSIONS
	 * @param in
	 * @return
	 */
	static public <T> AllSessionsArrays<T> toSessionsArrays(ArrayList<LabeledArrayList<StRecord>> in) {
		
		AllSessionsArrays dest = new AllSessionsArrays();
		ArrayList<Short> classes = new ArrayList<Short>();
		StRecord currentSession = null;
		for (short c = 0; c<in.size(); c++)
		{
			LabeledArrayList<StRecord> sessionsOfTheClass = in.get(c); // The sessions of the class c
			for (int i=0; i<sessionsOfTheClass.size(); i++)
			{
	        	currentSession = sessionsOfTheClass.get(i); // The i-th session of the c-th class
	        	// USELESS dest.classNames.add(currentSession.className);
	        	dest.sessionNames.add(currentSession.sessionName);
	        	dest.data.add(currentSession.data);
	        	classes.add(c);
	        }
		}
		
		// Convert to array
		dest.classes = classes.toArray(new Short[classes.size()]);
		
		return dest;
		
	}
	
	/**
	 * Useful for POPREF building, using dest.data as List of MyMatrix and dest.classes (to be converted into [])
	 * @param speakersChunks
	 * @return
	 */
	static public <T> AllSessionsArrays<T> toSessionsArrays(LabeledArrayListOfSpeakers<T> speakersChunks) {
	
		AllSessionsArrays<T> dest = new AllSessionsArrays<T>();
		ArrayList<Short> classes = new ArrayList<Short>();
		
		int numClasses = speakersChunks.size();
		
		for (short c=0; c<numClasses; c++)
		{
			LabeledArrayListOfSessions<T> sessionsOfCurrentRefSpeaker = speakersChunks.get(c);
			int numSessions = sessionsOfCurrentRefSpeaker.size();
			for (int se = 0; se<numSessions; se++)
			{
				LabeledArrayList<T> chunkedSession = (LabeledArrayList<T>) sessionsOfCurrentRefSpeaker.get(se);
				String sessionName = chunkedSession.label;
				int numChunksOfSession = chunkedSession.size();
				for (int ch=0; ch<numChunksOfSession; ch++)
				{
					T M = chunkedSession.get(ch);
					
					dest.sessionNames.add(sessionName);
					dest.data.add(M);
					classes.add(c);
				}
			}
			
		}
		
		// Convert to array
		dest.classes = classes.toArray(new Short[classes.size()]);
		
		return dest;
	}
	
	/**
	 * 
	 * @param className
	 * @return
	 */
	private LabeledArrayList<StRecord> newClass(String className) {
		
		LabeledArrayList<StRecord> recordSet = new LabeledArrayList<StRecord>(); //speakerRecordSet.add(R);
		recordSet.label = className; // Very important
		
		tableString.put(className, recordSet);
		
		return recordSet;
	}
	
	/**
	 * Add a record using the speaker name as key
	 * @param R
	 * @param className
	 * @return
	 */
	private void addRecordToSpeaker(StRecord R, String className) {
		
		// Search the class name
		LabeledArrayList<StRecord> speakerRecordSet = tableString.get(className);
		
		if (speakerRecordSet ==null) // If the name was not found...
			speakerRecordSet = newClass(className);
		
		// The speaker is already in the memory
		speakerRecordSet.add(R);
	}
	
	
	public void fromFileList(String inputFileListFullPath) throws Exception {
		File f = new File(inputFileListFullPath);
		String inputDir = f.getParent().toString();
		fromFileList(inputDir, inputFileListFullPath);
	}

	/**
	 * Crea una SessionsTable partendo dal file contenente la lista di sessioni;
	 * @param inputDir
	 * @param inputFileList
	 * @throws Exception 
	 */
	public void fromFileList(String inputDir, String inputFileList) throws Exception {
		
		SessionsTable_v2<T> ST = this;
		ArrayList<String>fileNames = new ArrayList<String>();
		ArrayList<String>classNames = new ArrayList<String>();
		
		readSessionsListFromFile(inputFileList, fileNames, classNames);
		
		int numItems = fileNames.size();
				
		for (int i=0; i<numItems; i++)
		{
			String filename = fileNames.get(i);
			String filePath = inputDir + File.separator + filename;
			System.out.println("Loading file: " + filePath); //filename
			Object M = IUReader.uReadFromFile(filePath, this.typeParameterClass);
			
			StRecord R = new StRecord(M, filename);
			String className = classNames.get(i);
			R.className = className;
			//R.sessionName = filename;
			//R.data = M;
			
			ST.addRecordToSpeaker(R, className);
					
		}
		this.inputDir = inputDir;
		this.inputFileList = inputFileList;
	}
	
	
	/**
	 * 
	 * @param inputDir
	 * @param inputFileList
	 * @return
	 * @throws IOException
	 */
	public static boolean fileListIsOk(String inputDir, String inputFileList) throws IOException {
		
		boolean ret = true;
		
		ArrayList<String>fileNames = new ArrayList<String>();
		ArrayList<String>classNames = new ArrayList<String>();
		
		readSessionsListFromFile(inputFileList, fileNames, classNames);
		
		int numItems = fileNames.size();

		for (int i=0; i<numItems; i++)
		{
			String filename = fileNames.get(i);
			String filePath = inputDir + File.separator + filename;
			if (!new File(filePath).canRead())
			{
				System.err.println("Can not read the file: " + filePath);
				ret = false;
			}				
		}
		return ret;
		
	}

	public static void readSessionsListFromBufferedReader(BufferedReader br, ArrayList<String>fileNames, ArrayList<String>classNames) throws java.io.IOException {
		while (br.ready())
		{
			String line = br.readLine().trim();
			if (line.charAt(0) != '#')
			{
				String[] parts = line.split("\t");
				String filename = parts[0];
				String spkId = parts[1];
				fileNames.add(filename);
				classNames.add(spkId);
			}
		}
		br.close();
	}


	/**
	 * 
	 * @param inputFileList
	 * @param fileNames
	 * @param classNames
	 * @throws IOException
	 */
	public static void readSessionsListFromFile(String inputFileList, ArrayList<String>fileNames, ArrayList<String>classNames) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(inputFileList));
		readSessionsListFromBufferedReader(br, fileNames, classNames);
	}


	private LabeledArrayListOfSessions<T> chunkSingleClass(LabeledArrayList<StRecord> sessions, int chunkSize) throws Exception {
		return chunkSingleClass(sessions, chunkSize, 0);
	}
	/**
	 *
	 * @param sessions
	 * @param chunkSize
	 * @return
	 * @throws Exception
	 */
	private LabeledArrayListOfSessions<T> chunkSingleClass(LabeledArrayList<StRecord> sessions, int chunkSize, int chunkMode) throws Exception {

		int numSessions = sessions.size();
		String className = sessions.label;
		
		// Initialize the output
		LabeledArrayListOfSessions<T> segmentedSessions = new LabeledArrayListOfSessions<T>(className);

		// Loop through all sessions
		for (int iSession = 0; iSession<numSessions; iSession++)
		{
			StRecord session = sessions.get(iSession);
			
			// Split the matrix and put in a list: we get the session segmented into chunks
			LabeledArrayList<T> chunks = chunkSession(session, chunkSize, chunkMode);
			if ((chunks==null) || chunks.isEmpty())
			{
				// Do not add the chunk to the chunk list
				System.out.println("WARNING: can not chunk the session " + session.sessionName + " of speaker " + className + " becuase it is shorter than " + chunkSize + " frames. Skipping it.");
			}
			else
			{
				// Add the chunks to the list. Here the label is the session Name
				segmentedSessions.add(chunks);
			}

		}
		return segmentedSessions;
	}
	
	public LabeledArrayListOfSpeakers<T> makeChunks(ArrayList<LabeledArrayList<StRecord>>speakersSessions, int chunkSize) throws Exception {
		return makeChunks(speakersSessions, chunkSize, 0);
	}
	/**
	 * Segment (chunk) the sessions of every speaker. It is also a check on the duration of sessions
	 * @param speakersSessions
	 * @param chunkSize
	 * @return
	 * @throws Exception
	 */
	public LabeledArrayListOfSpeakers<T> makeChunks(ArrayList<LabeledArrayList<StRecord>>speakersSessions, int chunkSize, int chunkMode) throws Exception {
	
		// Initialize the output
		LabeledArrayListOfSpeakers speakersChunks = new LabeledArrayListOfSpeakers(); // No need of a label
		
		int actualNumberOfSpeakers = speakersSessions.size();
		for (int iSpeaker = 0; iSpeaker<actualNumberOfSpeakers; iSpeaker++)
		{
			LabeledArrayList<StRecord> sessionsOfCurrentSpk = speakersSessions.get(iSpeaker);
			String speakerName = sessionsOfCurrentSpk.label;

			LabeledArrayListOfSessions<T> segmentedSessionsOfCurrentSpk = chunkSingleClass(sessionsOfCurrentSpk, chunkSize, chunkMode);		
			
			// Add the list and place the same label of the speaker
			if (segmentedSessionsOfCurrentSpk.isEmpty()) {
				// Do not add
				System.out.println("WARNING: can not chunk the speaker " + speakerName + " because all sessions are shorter than " + chunkSize + " frames. Skipping it");
			}
			else
			{	// Add
				speakersChunks.add(segmentedSessionsOfCurrentSpk);
				speakersChunks.label = speakerName;
			}
						
			// CAN'T DO THAT!!!Free some reference
			//speakersSessions.set(iSpeaker, null);
		}
		
		return speakersChunks;
	}
	
	public LabeledArrayList<T> chunkSession(StRecord session, int chunkSize) throws Exception {
		return chunkSession(session, chunkSize, 0);
	}
	/**
	 * Convert a long session (in StRecord format) into a set of chunks, in LabeledArrayListOfFea format
	 * @param session
	 * @param chunkSize
	 * @return
	 * @throws Exception
	 */
	public abstract LabeledArrayList<T> chunkSession(StRecord session, int chunkSize, int chunkMode) throws Exception;
	/*public static LabeledArrayListOfFea chunkSession(StRecord session, int chunkSize) throws Exception {
		
		MyMatrix bigFeaMatrix = session.data;
		
		if (bigFeaMatrix.getVectorDim()==VectorDim.COLUMN_VECTORS)
			throw new Exception("ERROR: unexpected matrix shape");
		
		//0 = do not return blocks with less rows than rowDim;
	    //1 = always return blocks with less rows than rowDim;
		ArrayList<MyMatrix> shortMatrices = bigFeaMatrix.splitHorizontally(chunkSize, 0); //1
		if (shortMatrices.isEmpty())
		{
			//System.out.println("WARNING");
		}
		
		LabeledArrayListOfFea chunkedSession = new  LabeledArrayListOfFea(shortMatrices);
		chunkedSession.label = session.sessionName; // Anche se non e' rilevante
		
		return chunkedSession;
	}*/
	
	
	/**
	 * For debug and test purpose
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String thisClass = new Object() { }.getClass().getEnclosingClass().getSimpleName();
		String inputFileList = "http:\\127.0.0.1\\_DOTTORATO\\_DATA\\audioDataOrig\\Monologhi_001_150\\Monologhi_001_150_16000Hz\\file.lst";
		if (MyURL.isURL(inputFileList))
			inputFileList = MyURL.fixURL(inputFileList);
		System.out.println(thisClass + " : Reading: " + inputFileList);
		ArrayList<String>fileNames = new ArrayList<String>();
		ArrayList<String>classNames = new ArrayList<String>();
		readSessionsListFromFile(inputFileList, fileNames, classNames);
		System.out.println(fileNames.size() + " items read");
		System.out.println("Done");
	}
	

}
