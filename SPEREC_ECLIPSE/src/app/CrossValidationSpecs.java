package app;


import sperec_common.SPEREC_Specs;
import sperec_common.Specs;

/**
 * Define the cross validation configuration with some default values
 * @author FS
 *
 */
public class CrossValidationSpecs extends Specs
{
	/**
	 * The array of the values of the test duration parameter
	 */
	public double [] testSessionDurationSec_v = {2.0, 4.0, 8.0, 14.0};
	
	/**
	 * The duration (constant at the moment) of the enrollment sessions
	 */
	//public double enrollSessionDurationSec = 25.0;
	
	/**
	 * The size of each group for k-fold cross validation
	 */
	public int numberOfDevelSpks = 10;

	/**
	 * The configuration file of the features to be used for comparisons, one file for each snr
	 */
	public String[] feaConfigFiles = null;
	
	/**
	 * The file, optional, of the cached enrollment sessions to be used for background and non-target comparisons
	 */
	public String enrollmentSessionsFile = "";
	
	/**
	 * The folder containing the clean speech files, to be used for background and non-target comparisons.
	 * It is intended to be used when no cache is avalailbe
	 */
	//public String cleanSpeechDir = "";
	
	public String cleanFeaCfgFile = "";

	/**
	 * The specifications for the SPEREC engine to be cross-validated 
	 */
	public SPEREC_Specs specs = null;

	//OLD public String inputDir = "";
	//OLD public String inputNoiseFile = "";
	// OLD public AudioProcessingSpecs audioProcessingSpecs = null;
	
	/**
	 * The SNR values
	 */
	public double [] SNRdB_v = {0.0, 6.0, 15.0, 20.0};

	int [] testDurationFrames_v;
	int enrollSessionDurationFrames;
	
	static public CrossValidationSpecs fromJsonString(String json) {
		return Specs.fromJsonString(json, CrossValidationSpecs.class);
	}
	
	public void analyze(Environment env) {
		dump();
		int numberOfSNR = SNRdB_v.length;
		for (int i=0; i<numberOfSNR; i++) {
			double snr = SNRdB_v[i];
			System.out.println(" SNR ["+i+"] = "+snr);
			String feacfgfile = feaConfigFiles[i];
			String relPath = env.findRelativePath(feacfgfile); // for print purpose
			System.out.println(relPath);
			
			String fullpath = env.getRootFolder() + relPath;
			
			
			
			System.out.println("");
		}
		
	}
	
}
