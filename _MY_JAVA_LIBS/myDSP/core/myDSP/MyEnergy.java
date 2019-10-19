package myDSP;

import myMath.MyMath;

public class MyEnergy {

	public double [] energy;
	public MyFraming oFraming;
	
	/**
	 * Compute power in the time domain for each frame in X
	 * @param X: bidimensional array of float samples (e.g. audio frames)
	 * @return the array of energy values
	 */
	public static double [] process(float [][] X) {
		
		int numFrames = X.length;

		double [] energy = new double[numFrames];

		// For each frame, subtract its mean, then square its samples. Then take the mean
		for (int i=0; i<numFrames; i++)
		{
			float [] y = MyMath.minus(X[i], MyMath.mean(X[i]));
			for (int j=0; j<y.length; j++)
				y[j] = y[j]*y[j];
			
			energy[i] = MyMath.mean(y);
		}
		
		return energy;
	}
	
	/**
	 * Divide the signal into frames, then compute the energy for each frame 
	 * @param u the Utterance containing 
	 * @param frameSettings information about how to split the signal into frames
	 */
	public MyEnergy(Utterance u, MyFramingSettings frameSettings) {
		
		oFraming = new MyFraming(frameSettings);
		oFraming.process(u);
		
		float [][] frames = oFraming.getFrames();
		energy = process(frames);
	}
}
