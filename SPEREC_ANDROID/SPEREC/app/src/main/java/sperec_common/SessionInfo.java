package sperec_common;

/**
 * This class stores any useful information about the user session (i.e. features sessions (how many, their durations), SNR.
 * A SessionInfo instance should be created as the user tries to authenticate/identify, and progressively built during the next processing steps.
 * @author FS
 *
 */
public class SessionInfo {

	// TO DO: Raw Audio Info
	
	
	// In effetti stanno gia' in SPEREC  .... AudioProcessingSpecs audioProcessingSpecs;
	
	/**
	 * For reference speaker models, more than one session could be used, so feaDuration must be an array.
	 * For test speaker model, usually one single session is used.
	 */
	double feaDuration[];
	
}
