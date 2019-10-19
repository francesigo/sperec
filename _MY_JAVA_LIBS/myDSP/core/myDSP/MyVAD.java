package myDSP;

import java.util.Arrays;

/**
 * Simple voice activity detector.
 * It computes energy of each frame and mark as "voiced" those frames having energy greater than vadPercentile percentile.
 * @author francesco.sigona
 *
 */
public class MyVAD {

	double [] energy; // [numFrames]: the energy value of each frame
	public boolean [] voiced_flags; // [numFrames]: for each frame, true if it is marked as a voiced frame, false otherwise
	public double[] voiced_energy; // [numVoiced]: energy values of each voiced frame
	public int [] voiced_indexes; // [numVoiced]: array of indexes of voiced frames
	double energyThreshold; // The energy value corresponging to vadPercentile percentile
	
	/**
	 * Compute the energy array of the utterance, then perform VAD the energy array
	 * @param u the input Utterance (it contains speech samples)
	 * @param specs Voice Activity Detection specifications
	 */
	public void process(Utterance u, MyVadSettings specs) {

		// Compute energy values
		MyEnergy NRG = new MyEnergy(u, specs);
		
		double [] energy = NRG.energy;
		
		// Decide voiced frames based on energy values
		process(energy, specs);
		
	}
	
	/**
	 * Compute the energy for each frame, then decide what frames are to be tagged as "voiced" depending on their energy values
	 * @param frames the array of frames of the signal
	 * @param specs the Voice Activity Detection specifications
	 */
	public void process(float [][] frames, MyVadSettings specs) {
		
		this.energy = MyEnergy.process(frames);
		
		process(this.energy, specs);
	}
	
	/**
	 * Decide what frames is to be tagged as "voiced" depending on its energy value.
	 * The energy values of all the frames are sorted and searchED for a percentile;
	 * the frames that have energy greater than that threshold are tagged as "voiced" frames.
	 * @param energy the array of the energy values
	 * @param specs Voice Activity Detection sepcifications
	 */
	public void process (double [] energy, MyVadSettings specs) {
		
		double[] sortedEnergy = energy.clone();
		Arrays.sort(sortedEnergy);
		int iPerc = (int)Math.round((double)sortedEnergy.length * (double)specs.vadPercentile / 100)-1;		
		energyThreshold = sortedEnergy[iPerc];
		sortedEnergy = null;

		int numFrames = energy.length;
		
		voiced_flags = new boolean[numFrames];
		
		int numVoiced = 0;
		
		for (int i = 0; i<numFrames; i++) 
		{
			boolean isVoicedFrame = energy[i]>energyThreshold;
			voiced_flags[i] = isVoicedFrame;
			if (isVoicedFrame)
				numVoiced++;			
		}
		
		voiced_energy = new double[numVoiced];
		voiced_indexes = new int[numVoiced];

		int j = 0;
		for (int i = 0; i<numFrames; i++) 
		{
			if (voiced_flags[i])
			{
				voiced_indexes[j] = i;
				voiced_energy[j] = energy[i];
				j++;
			}
		}		
	}
	
	
}
