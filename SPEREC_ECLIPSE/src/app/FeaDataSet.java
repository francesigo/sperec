package app;

import java.io.File;
import java.io.IOException;

import sperec_common.ConfigurationFile;
import sperec_common.FeaSpecs;
import sperec_common.SessionsTable_MyMatrix;

/**
 * A SessionsTable with more properties
 * @author FS
 *
 */
public class FeaDataSet extends SessionsTable_MyMatrix {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//String feaDir = "";
	//String feaList = "";
	FeaSpecs feaSpecs = null;
			
	/**
	 * Build a FeaDaset starting from a configuration file
	 * @param cfgfilepath the path of the configuration file
	 * @return a FeaDataset instance
	 * @throws Exception 
	 */
	static public FeaDataSet fromConfigFile(String cfgfilepath) throws Exception {
		
		FeaDataSet fds = new FeaDataSet();
		
		String feaDir = new File(cfgfilepath).getParent();

		ConfigurationFile cfg = ConfigurationFile.load(cfgfilepath); // Can throw Exception
		System.out.println("Loading configuration file: "+  cfgfilepath);
		cfg.dump();
		
		// Extract the name of the file containing the list of fea files
		String feaList = cfg.getItem("FEA", "feaFileList");
		feaList = feaDir + File.separator + feaList; // The single files are supposed to be in the same folder as cfgfilepath
		
		// Actually build the FeaDataset
		fds.fromFileList(feaDir, feaList);
		//fds.feaDir = feaDir;
		//fds.feaList = feaList;
		String feaJson = cfg.getItem("FEA", "FeaSpecs");
		fds.feaSpecs = FeaSpecs.fromJsonString(feaJson, FeaSpecs.class);
		
		return fds;
	}
	
	/**
	 * For test purpose
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void main(String [] args) {
		String feaConfigFile = "F:\\GDrive_Unisalento\\FS\\_DOTTORATO\\_DATA\\toSPLIT_toNOISE_toVAD_toFEA\\Monologhi_0dB\\ufv\\Monologhi_0dB--vad---i-0.1--of-2.0--fea-mode_M__nc_42__np_42__feadim_42\\fea.cfg";
		FeaDataSet noisyFeaDataset = null;
		try
		{
			noisyFeaDataset = FeaDataSet.fromConfigFile(feaConfigFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("LOADED " + noisyFeaDataset.getNumberOfUniqueClasses() + " speakers");
	}
	
}
