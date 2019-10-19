package sperec_common;

import java.io.IOException;
import java.util.ArrayList;


import myMath.MyMatrix;

import myMath.MyMatrix.VectorDim;

public class SessionsTable_MyMatrix extends SessionsTable_v2<MyMatrix> {
	
	
	public SessionsTable_MyMatrix() {
		super(MyMatrix.class);
		// TODO Auto-generated constructor stub
	}

	
	String inputDir = "";
	String inputFileList = "";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7065852547315992406L;
	
	/**
	 * Convert a long session (in StRecord format) into a set of chunks, in LabeledArrayListOfFea format
	 * @param session
	 * @param chunkSize
	 * @return
	 * @throws Exception
	 */
	//public LabeledArrayList<MyMatrix> chunkSession(StRecord<MyMatrix> session, int chunkSize) throws Exception {
	@Override
	public LabeledArrayList<MyMatrix> chunkSession(StRecord session, int chunkSize, int chunkMode) throws Exception {
	
		// Here I cast
		MyMatrix bigFeaMatrix = (MyMatrix) session.data;
		
		if (bigFeaMatrix.getVectorDim()==VectorDim.COLUMN_VECTORS)
			throw new Exception("ERROR: unexpected matrix shape");
		
		//0 = do not return blocks with less rows than rowDim;
	    //1 = always return blocks with less rows than rowDim;
		ArrayList<MyMatrix> shortMatrices = bigFeaMatrix.splitHorizontally(chunkSize, chunkMode); // was 0 //1
		if (shortMatrices.isEmpty())
		{
			//System.out.println("WARNING");
		}
		
		LabeledArrayList<MyMatrix> chunkedSession = new LabeledArrayList<MyMatrix>();
		chunkedSession.addAll(shortMatrices);
		chunkedSession.label = session.sessionName; // Anche se non e' rilevante
		
		return chunkedSession;
	}

	
	/**
	 * For debug and test purpose
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String thisClass = new Object() { }.getClass().getEnclosingClass().getSimpleName();
		
		// File list address provided
		String inputFileList = "http:\\127.0.0.1\\_DOTTORATO\\_DATA\\audioDataOrig\\Monologhi_001_150\\Monologhi_001_150_16000Hz\\file.lst";
		if (MyURL.isURL(inputFileList))
			inputFileList = MyURL.fixURL(inputFileList);
		
		// Read the list od sessions from file
		System.out.println(thisClass + " : Reading: " + inputFileList);
		ArrayList<String>fileNames = new ArrayList<String>();
		ArrayList<String>classNames = new ArrayList<String>();
		readSessionsListFromFile(inputFileList, fileNames, classNames);
		System.out.println(fileNames.size() + " items read");
		System.out.println("Done");
	}

	
	// --- BEGIN TRASH
	
	/**
	 * A way to keep info about a session
	 * @author FS
	 *
	 */
	/*public class StRecord {
		
		public String className; // Il nome dello speaker, non e' essenziale, ma puo' servire per debug
		public String sessionName = ""; //il nome del file della sessione, se serve
		public MyMatrix data = null;
		
		
		public StRecord(MyMatrix M, String sessionName) {
			this.data = M;
			this.sessionName = sessionName;
		}
		
		public StRecord(MyMatrix M) {
			this(M, "");
		}
		public StRecord() {
			this(null, "");
		}
		
	}*/
	

	// Used to get a view of the current content, as arrays
	/*public class AllSessionsArrays {
		//public ArrayList<String> classNames = new ArrayList<String>(); useless
		public ArrayList<String> sessionNames = new ArrayList<String>();
		public ArrayList<MyMatrix> data = new ArrayList<MyMatrix>();
		public Short [] classes = null;
	}*/
	
	
	/**
	 * The core of the table: is a HashMap where the key are class names (String)
	 */
	//HashMap<String, LabeledArrayList<StRecord>> tableString = new HashMap<String, LabeledArrayList<StRecord>>();
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	/*public LabeledArrayList<StRecord> get(String key) {
		return tableString.get(key);
	}*/
	
	/*public void remove(String key) {
		tableString.remove(key);
	}*/
	
	/**
	 * get and remove
	 * @param key
	 * @return
	 */
	/*public LabeledArrayList<StRecord> cut(String key) {
		LabeledArrayList<StRecord> x = get(key);
		remove(key);
		return x;
	}*/
	
	
	/**
	 * 
	 * @return
	 */
	/*public int getNumberOfUniqueClasses() {
		return tableString.size();
	}*/
	
	/**
	 * Clear the hashmap
	 */
	/*public void clear() {
		tableString.clear();
	}*/
	
	/**
	 * Return the content in order to use indexes. After that, the SessionsTable should not changed any more
	 * @return
	 */
	/*public ArrayList<LabeledArrayList<StRecord>> getArrayListOfRecordSets() {
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
	*/
	
	/**
	 * Useful for POPREF building, using dest.data as List of MyMatrix and dest.classes (to be converted into [])
	 * Also, it is useful to manage indexing OF SESSIONS
	 * @param in
	 * @return
	 */
	/*static public AllSessionsArrays toSessionsArrays(ArrayList<LabeledArrayList<StRecord>> in) {
		
		AllSessionsArrays dest = new SessionsTable_MyMatrix().new AllSessionsArrays();
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
	}*/
	
	/**
	 * Useful for POPREF building, using dest.data as List of MyMatrix and dest.classes (to be converted into [])
	 * @param speakersChunks
	 * @return
	 */
	/*static public AllSessionsArrays toSessionsArrays(LabeledArrayListOfSpeakers speakersChunks) {
	
		AllSessionsArrays dest = new SessionsTable_MyMatrix().new AllSessionsArrays();
		ArrayList<Short> classes = new ArrayList<Short>();
		
		int numClasses = speakersChunks.size();
		
		for (short c=0; c<numClasses; c++)
		{
			LabeledArrayListOfSessions sessionsOfCurrentRefSpeaker = speakersChunks.get(c);
			int numSessions = sessionsOfCurrentRefSpeaker.size();
			for (int se = 0; se<numSessions; se++)
			{
				LabeledArrayListOfFea chunkedSession = sessionsOfCurrentRefSpeaker.get(se);
				String sessionName = chunkedSession.label;
				int numChunksOfSession = chunkedSession.size();
				for (int ch=0; ch<numChunksOfSession; ch++)
				{
					MyMatrix M = chunkedSession.get(ch);
					
					dest.sessionNames.add(sessionName);
					dest.data.add(M);
					classes.add(c);
				}
			}
			
		}
		
		// Convert to array
		dest.classes = classes.toArray(new Short[classes.size()]);
		
		return dest;
	}*/
	
	/**
	 * 
	 * @param className
	 * @return
	 */
	/*private LabeledArrayList<StRecord> newClass(String className) {
		
		LabeledArrayList<StRecord> recordSet = new LabeledArrayList<StRecord>(); //speakerRecordSet.add(R);
		recordSet.label = className; // Very important
		
		tableString.put(className, recordSet);
		
		return recordSet;
	}*/
	
	/**
	 * Add a record using the speaker name as key
	 * @param R
	 * @param className
	 * @return
	 */
	/*private void addRecordToSpeaker(StRecord R, String className) {
		
		// Search the class name
		LabeledArrayList<StRecord> speakerRecordSet = tableString.get(className);
		
		if (speakerRecordSet ==null) // If the name was not found...
			speakerRecordSet = newClass(className);
		
		// The speaker is already in the memory
		speakerRecordSet.add(R);
	}*/

	/**
	 * Crea una SessionsTable partendo dal file contenente la lista di sessioni;
	 * @param inputDir
	 * @param inputFileList
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	/*public void fromFileList(String inputDir, String inputFileList) throws IOException, ClassNotFoundException {
		
		SessionsTable_MyMatrix ST = this;
		ArrayList<String>fileNames = new ArrayList<String>();
		ArrayList<String>classNames = new ArrayList<String>();
		
		readSessionsListFromFile(inputFileList, fileNames, classNames);
		
		int numItems = fileNames.size();
				
		for (int i=0; i<numItems; i++)
		{
			String filename = fileNames.get(i);
			String filePath = inputDir + File.separator + filename;
			System.out.println("Loading file: " + filePath); //filename
			MyMatrix M = MyMatrix.readFromFile(filePath);
			
			StRecord R = ST.new StRecord(M, filename);
			String className = classNames.get(i);
			R.className = className;
			//R.sessionName = filename;
			//R.data = M;
			
			ST.addRecordToSpeaker(R, className);
					
		}
		this.inputDir = inputDir;
		this.inputFileList = inputFileList;
	}*/
	
	
	/**
	 * 
	 * @param inputDir
	 * @param inputFileList
	 * @return
	 * @throws IOException
	 */
	/*public static boolean fileListIsOk(String inputDir, String inputFileList) throws IOException {
		
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
		
	}*/

	/*public static void readSessionsListFromBufferedReader(BufferedReader br, ArrayList<String>fileNames, ArrayList<String>classNames) throws java.io.IOException {
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
	}*/


	/**
	 * 
	 * @param inputFileList
	 * @param fileNames
	 * @param classNames
	 * @throws IOException
	 */
	/*public static void readSessionsListFromFile(String inputFileList, ArrayList<String>fileNames, ArrayList<String>classNames) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(inputFileList));
		readSessionsListFromBufferedReader(br, fileNames, classNames);
	}*/


	/**
	 *
	 * @param sessions
	 * @param chunkSize
	 * @return
	 * @throws Exception
	 */
	/*private static LabeledArrayListOfSessions chunkSingleClass(LabeledArrayList<StRecord> sessions, int chunkSize) throws Exception {

		int numSessions = sessions.size();
		String className = sessions.label;
		
		// Initialize the output
		LabeledArrayListOfSessions segmentedSessions = new LabeledArrayListOfSessions(className);

		// Loop through all sessions
		for (int iSession = 0; iSession<numSessions; iSession++)
		{
			StRecord session = sessions.get(iSession);
			
			// Split the matrix and put in a list: we get the session segmented into chunks
			LabeledArrayListOfFea chunks = chunkSession(session, chunkSize);
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
	}*/
	
	/**
	 * Segment (chunk) the sessions of every speaker. It is also a check on the duration of sessions
	 * @param speakersSessions
	 * @param chunkSize
	 * @return
	 * @throws Exception
	 */
	/*public static LabeledArrayListOfSpeakers makeChunks(ArrayList<LabeledArrayList<StRecord>>speakersSessions, int chunkSize) throws Exception {
	
		// Initialize the output
		LabeledArrayListOfSpeakers<T> speakersChunks = new LabeledArrayListOfSpeakers<T>(); // No need of a label
		
		int actualNumberOfSpeakers = speakersSessions.size();
		for (int iSpeaker = 0; iSpeaker<actualNumberOfSpeakers; iSpeaker++)
		{
			LabeledArrayList<StRecord> sessionsOfCurrentSpk = speakersSessions.get(iSpeaker);
			String speakerName = sessionsOfCurrentSpk.label;

			LabeledArrayListOfSessions segmentedSessionsOfCurrentSpk = chunkSingleClass(sessionsOfCurrentSpk, chunkSize);		
			
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
	}*/
	

}
