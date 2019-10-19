package test;

import com.mathworks.engine.MatlabEngine;

import sperec_common.SPEREC;
import sperec_common.SPEREC_Factory;

public class MATLAB_SPEREC_Factory extends SPEREC_Factory {
	
	private final static String newline = "\n";
	
	public MATLAB_SPEREC_Factory() {
		
		// Check Matlab
		MatlabEngine eng = MATLAB.getMatlabConnection();
		MATLAB.closeSperecMatlabConnection(eng);
	}
	
	/**
	 * A String [] of all allowed SPEREC types
	 */
	protected String [] allowedSperecTypes = {"MATLAB_SPEREC_UBM_IV_GPLDA"};
	
	
	/*public MATLAB_SPEREC_Loader_JVM createLoader() {
		
		MATLAB_SPEREC_Loader_JVM loader = new MATLAB_SPEREC_Loader_JVM();
		loader.setSperecFactory(this);
		
		return loader;
	}*/
	
	
	
	/**
	 * Create an instance of the specific SPEREC of the provided type
	 * @param type
	 * @return: the SPEREC instance of the requested type, or null is the type is not allowed.
	 * @throws Exception 
	 */
	@Override
	public SPEREC createSperecEngine(String sperecType) throws Exception {
		
		
		// Then check if the test type is allowed
		sperecType = "MATLAB_" + sperecType;
		if (!isAllowed(sperecType))
		{
			String msg = "ERROR:UNSUPPORTED SPEREC TYPE: " + sperecType + newline;
			throw new Exception (msg);
		}
		
		
		SPEREC oSPEREC = null;
		
		switch (sperecType) {
		
			case "MATLAB_SPEREC_UBM_IV_GPLDA":
				oSPEREC = new MATLAB_SPEREC_UBM_IV_GPLDA();
				break;
				
			default:
				oSPEREC = null;
				break;
		}

		return oSPEREC;
	}
	
	@Override
	public String [] getTypes() {
		
		return allowedSperecTypes;
	}

}
