package myDSP;

import myMath.MyMath;

public class MySigNormalizer {


	/**
	 * Process a signal that has already been framed. The normalized frames samples replace the input ones
	 * @param type the type of normalizazion required
	 * @param frames the input signal frames
	 * @param P additional parameters for the normalization
	 * @throws Exception
	 */
	public static void processSelf(String type, float[][] frames, Object P) throws Exception  {

		switch (type) {
		
		case "mean-voiced":
			
			MyVAD myVad = new MyVAD();
			MyVadSettings FS = (MyVadSettings)P;
			myVad.process(frames, FS); // Vad processing on the signal frames
			double meanVoicedEnergy = MyMath.mean(myVad.voiced_energy);
			double scalingFactor = 1.0/Math.sqrt(meanVoicedEnergy);
			// Rescale the samples of all frames
			MyMath.timesSelf(frames, scalingFactor);			
			break;
			
		default:
			throw new Exception("Unsupported normalization named " + type);
		}
	}
	
	/**
	 * Perform signal normalization of the given input Utterance, given the type of normalization, and additional parameters
	 * @param type the type of signal normalization is requested
	 * @param u the input Utterance with speech samples
	 * @param P an Object that represents additional parameters depending on the type of signal normalization required
	 * @throws Exception
	 */
	public static void processSelf(String type, Utterance u, Object P) throws Exception  {

		switch (type) {
		
		case "mean-voiced":
			
			MyVadSettings FS = (MyVadSettings)P;

			MyFraming myFraming = new MyFraming(FS);
			myFraming.process(u);
			float[][] frames = myFraming.getFrames();
			
			MyVAD myVad = new MyVAD();
			myVad.process(frames, FS);
			double meanVoicedEnergy = MyMath.mean(myVad.voiced_energy);
			double scalingFactor = 1.0/Math.sqrt(meanVoicedEnergy);
			// Rescale the samples 
			MyMath.timesSelf(u.samples, scalingFactor);			
			break;
			
		default:
			throw new Exception("Unsupported normalization named " + type);
		}
	}
}
