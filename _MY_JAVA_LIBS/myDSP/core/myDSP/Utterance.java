package myDSP;


public class Utterance {
	public float[] samples;
	public int sampleRate;

	public Utterance (float[] audioSamples, int sampleRate) {
		this.samples = audioSamples;
		this.sampleRate = sampleRate;
	}
	
	public double getDurationSec() {
		double dur = (double)samples.length / (double)sampleRate;
		return dur;
	}
	
	public void showInfo(String title) {
		System.out.println(title);
		System.out.println("\tNumber of samples:\t " + samples.length);
		System.out.println("\tSample rate:\t\t " + sampleRate + " [Hz]");
		System.out.println("\tSampling time:\t\t " + 1000.0/(double)sampleRate + " [ms]");
		System.out.println("\tDuration:\t\t " + getDurationSec() + " [s]");
	}
}


