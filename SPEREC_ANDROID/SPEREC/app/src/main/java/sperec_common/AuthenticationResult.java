package sperec_common;

public class AuthenticationResult {
	
	/**
	 * Set to true if the comparison or authentication was successful, false otherwise.
	 */
	public boolean ok;
	
	/**
	 * A custom message to the user.
	 */
	public String msg;
	
	/**
	 * The output of the scoring step.
	 */
	public double score;
	
	/**
	 * Set to the claimed identity in case of successful authentication.
	 */
	public SpeakerIdentity identity;

	public double scoreThreshold;
	public double FalseNegative_100;
	public double FalsePositive_100;
	public double EER_100;
	/**
	 * Basic constructor
	 * @param ok: a default value for the ok flag.
	 */
	
	public AuthenticationResult(boolean ok) {
		this.ok = ok;
		
		this.score = 10E-20;
		
		this.msg = "WARNING: NOT INITIALIZED";
		
		this.identity= null;
		
	}
	
	public String toString() {
		
		String newline = System.getProperty("line.separator");
		
		String s = 	"Score: " + score + newline +
					"ScoreThreshold: " + scoreThreshold + newline + 
					"False Negative (%): " + FalseNegative_100 + newline +
					"False Positive (%): " + FalsePositive_100 + newline +
					"EER (%) : " + EER_100 + newline +
					"Authenticated: " + ok + newline + 
					"Message: " + msg;
		return s;
	}
}
