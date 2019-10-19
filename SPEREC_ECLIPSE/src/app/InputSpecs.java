package app;

import java.io.IOException;

import javax.swing.JOptionPane;

import guiutils.ChooseConfigurationFile;
import sperec_common.ConfigurationFile;
import sperec_common.Specs;

/**
 * Generic class to input parameters using Graphical User Interface (GUI)
 * @author FS
 *
 * @param <T>
 */
public abstract class InputSpecs<T extends Specs> {
	
	protected String MAIN_OUTPUT_FOLDER_PATH; // = Environment.getMainOutputFolderPath();

	private final Class<T> claT;
	private T initSpecs = null;
	private T finalSpecs = null;
	//private String cfgPath = null;
	private ConfigurationFile cfg = null;
	protected String cfgSectionName = "";
	protected String cfgItemName = "";
	protected String humanReadableName = "";
	protected Environment env = null;
	
	/**
	 * If true, ask the user. Use getter and setter
	 */
	private boolean ask = true;
	public boolean	isAsk() { return ask; }
	public void 	setAsk(boolean ask) { this.ask = ask; }
	
	
	public InputSpecs(T initSpecs_, Class<T> cla, Environment env)
	{
		this.initSpecs = initSpecs_;
		this.claT = cla;
		this.env = (env ==null) ? new Environment(): env;
		this.MAIN_OUTPUT_FOLDER_PATH = this.env.getMainOutputFolderPath();
	}
	
	/**
	 * 
	 * @param initSpecs_
	 * @param cla
	 * @throws IOException
	 */
	public InputSpecs(T initSpecs_, Class<T> cla)
	{
		this(initSpecs_, cla, null);
	}
	
	public InputSpecs(Class<T> cla) throws IOException {
		this(null, cla);
	}
	
	
	/**
	 * Actually use the class, by getting the specs
	 * @return
	 * @throws IOException
	 */
	public T getSpecs() throws IOException {
		//return finalSpecs;
		if (this.initSpecs==null) // Ask for a configuration file
			this.initSpecs = getFromFile();
		
		finalSpecs = this.initSpecs; // By default
		if (finalSpecs!= null)
			finalSpecs.dump();
		
		if (isAsk() || (finalSpecs== null))
		{
			// Get specs from GUI
			int option = JOptionPane.showConfirmDialog(null, "Do you want to review the configuration?", "", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION)
			{
				finalSpecs = getFromGUI(this.initSpecs);
			}	
			
		}

		return finalSpecs;
	}
	
	public ConfigurationFile getConfigurationFile() {
		return cfg;
	}
	public String getConfigurationPath() {
		return cfg.cfgFilePath;
	}
	
	/**
	 * Specific
	 * @param t
	 * @return
	 * @throws IOException
	 */
	public abstract T getFromGUI(T t) throws IOException;
	
	
	/**
	 * Generic
	 * @param cla
	 * @return
	 * @throws IOException
	 */
	public T getFromFile() throws IOException{
		return getFromFile(MAIN_OUTPUT_FOLDER_PATH, true);
	}
	
	public T getFromFile(boolean ask) throws IOException {
		return getFromFile(MAIN_OUTPUT_FOLDER_PATH, ask);
	}
	public T getFromFile(String filepath) throws IOException {
		return getFromFile(filepath, true);
	}
	
	/**
	 * 
	 * @param filepath
	 * @param ask
	 * @return
	 * @throws IOException
	 */
	public T getFromFile(String filepath, boolean ask) throws IOException {
				
		return (ask) ? getFromFile_Ask(filepath) : getFromFile_NoAsk(filepath);
	}
	
	/**
	 * Get from file, ask the user
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	private T getFromFile_Ask(String filepath) throws IOException {
		
		T specs = null;
		
		cfg = ChooseConfigurationFile.get(filepath, this.humanReadableName);

		if ((cfg != null) && (cfg.cfgFilePath!=""))
		{
			if (!cfg.hasSection(this.cfgSectionName))
			{
				System.err.println("ERROR: Selected configuration file has no " + this.cfgSectionName + " section. Exiting");
			}
			else
			{
				specs = Specs.fromJsonString(cfg.getItem(this.cfgSectionName, this.cfgItemName), claT);
				// Assuming that the file was correct, set the root dir accordingly into the enviroment
				env.setRootFolder(cfg.cfgFilePath);
			}
		}
		
		return specs;
	}

	/**
	 * Get from file, do not ask the user
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	private T getFromFile_NoAsk(String filepath ) throws IOException {
				
		cfg = ConfigurationFile.load(filepath);
		
		if ( (cfg != null) && (cfg.hasSection(this.cfgSectionName)) )
			return Specs.fromJsonString(cfg.getItem(this.cfgSectionName, this.cfgItemName), claT);
		
		return null;
	}
	
}
