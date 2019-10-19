package myIO;



import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Environment {
	
	private static final String [] rootCandidates = new String [] {
			System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FS" + File.separator + "Lavoro" + File.separator + "SPHERA" + File.separator + "_code_client_server" + File.separator + "SerServerWithMatlab",
	};
	//"E:" + File.separator + "Google Drive" + File.separator + "_DOTTORATO",

	
	private String rootFolder = "";
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public Environment() {
		getRootFolder();
		String machineName = getComputerName();
		System.out.println("LOCAL MACHINE: " + machineName);
		System.out.println("USER HOME: " + System.getProperty("user.home"));
		System.out.println("NOW IS: " + now());
		System.out.println("LOCAL ROOT FOLDER: " + getRootFolder());
	}
	
	public String now() {
		Date date = new Date();
        String s = sdf.format(date);
        return s;
	}
	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	public String getComputerName()
	{
	    Map<String, String> env = System.getenv();
	    if (env.containsKey("COMPUTERNAME"))
	        return env.get("COMPUTERNAME");
	    else if (env.containsKey("HOSTNAME"))
	        return env.get("HOSTNAME");
	    else
	    	try {
	    		InetAddress myHost = InetAddress.getLocalHost();
	    		return myHost.getHostName();
	    	}
	    	catch (Exception e) {
	        	return "Unknown Computer";
	    	}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getRootFolder() {
		
		if (rootFolder.equals(""))
		{
			for (int i = 0; i<rootCandidates.length; i++)
			{
				String c = rootCandidates[i]; //System.out.println(c);
				File f = new File(c); 
				if (f.isDirectory() && f.exists())
				{
					rootFolder = c;
					break;
				}
			}			
		}
		return rootFolder;		
	}
	
	
	public void setRootFolder(String filepath) {
		String fileRoot = findRoot(filepath);
		if (!fileRoot.equals(""))
				rootFolder = fileRoot;		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getSampleDataDir() {
		String ret = "";
		String root = getRootFolder();
		if (!root.equals("")) {
			ret = root + File.separator + "sampleData";
		}
		return ret;		
	}
			


	public String hardFindFile(String filepath) throws Exception {
		String s = findFile(filepath);
		if (s.equals(""))
			throw new Exception ("FILE NOT FOUND : " + filepath);
		
		return s;
	}
	/**
	 * Dato il percorso di un file (p.e. su una share montata su un altro PC) lo traduce in un percorso corrente.
	 * La parte iniziale del percorso in input viene confrontata cin ciascuno dei candidati a "root".
	 * Il candidato che viene trovato, viene sostituito nel percorso in input con la root corrente.
	 * @param filepath
	 * @return
	 */
	public String findFile(String filepath_) {
		
		String filepath = filepath_.replace("/", File.separator).replace("\\", File.separator);
		
		String relativePath = findRelativePath(filepath);
		if (relativePath.equals(""))
			return "";
		else
			return getRootFolder() + relativePath;
	}
	
	/**
	 * Dato il percorso di un file (p.e. su una share montata su un altro PC) lo traduce in un percorso corrente.
	 * La parte iniziale del percorso in input viene confrontata cin ciascuno dei candidati a "root".
	 * @param filepath
	 * @return
	 */
	public String findRelativePath(String filepath) {
		
		// Find the root of this file
		String fileRoot = findRoot(filepath);
		
		if (fileRoot.equals(""))
			return "";
		
		String relativePath = filepath.substring(fileRoot.length(), filepath.length());
		
		return relativePath;	
	}
	
	/**
	 * 
	 * @param filepath
	 * @return
	 */
	public String findRoot(String filepath) {
		
		for (int i = 0; i<rootCandidates.length; i++)
		{
			String candidateRoot = rootCandidates[i];
			if (filepath.length()>candidateRoot.length())
			{
				String s1 = filepath.substring(0, candidateRoot.length());
				//System.out.println(candidateRoot + "-----------" + s1 );
				if ((filepath.length()>candidateRoot.length()) && (s1.equals(candidateRoot)))
					return candidateRoot;
			}
		}
		return "";
	}
	
	/**
	 * For debug and test purpose
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filePath = "dis-f1-b1.wav";
		
		Environment me = new Environment();
		System.out.println("ROOT = " + me.getRootFolder());
		System.out.println("FILE REQUEST: "+ filePath);
		String file2 = me.findFile(filePath);
		System.out.println("FOUND: " + file2);
	}
}
