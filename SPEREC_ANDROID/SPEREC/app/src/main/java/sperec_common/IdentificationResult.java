package sperec_common;

public class IdentificationResult {

	/**
	 * Set true if the the identification was successful
	 */
	public boolean ok;
	
	/**
	 * A custom message to the user
	 */
	public String msg;
	
	/**
	 * Set only for a successful (identification)
	 */
	public SpeakerIdentity userid;
	
	
	
	/**
	 * Basic constructor
	 */
	public IdentificationResult() {
		this.ok = false;
		
		this.msg = "WARNING: NOT INITIALIZED";
		
		this.userid = null;
	}
	
}
