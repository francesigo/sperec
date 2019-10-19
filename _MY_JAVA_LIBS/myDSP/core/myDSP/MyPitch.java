package myDSP;

import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import myMath.MyMath;
import myVoiceBox.Fxrapt;

public class MyPitch {

	// ------------------------- Input
	MyPitchSettings FS;
	
	
	
	// ------------------------- Output
	/**
	 * The values of pitch frame by frame
	 */
	public double[] pitchValues;
	
	/**
	 * Probability values associated to the picth values, frame by frame
	 */
	public double[] pitchProbabilities;
	
	public boolean [] isPitched;
	
	/**
	 * Time sampling of the pitch
	 */
	public double [] mid_time;
	
	
	public MyPitch(MyPitchSettings FS) {
		this.FS = FS;
	}
	
	
	/**
	 * Compute the pitch values of the signal.
	 * The signal can be provided as float [] of samples, or as arrays of frames
	 * If frames ares provided, the output should be "aligned" to them
	 * @param samples the long speech sequence
	 * @param sampleRate the sample rate in sample per second
	 * @param frames the framed signal as bidimensional array of float
	 * @throws Exception
	 */
	public void exe(float [] samples, float sampleRate, float [][]frames) throws Exception {
		
	
		switch (FS.algorithmName) {
		
		case "myVbox.Fxrapt":
		
			double dSampleRate = (double)sampleRate;
			Fxrapt me = new Fxrapt(samples, dSampleRate, "u");
			pitchValues = me.fx;
			pitchProbabilities = null;
			isPitched = MyMath.not(MyMath.isnan(pitchValues));
			mid_time = new double[me.tt.length];
			for (int i=0; i<mid_time.length; i++)
				mid_time[i] = 0.5*(double)(me.tt[i][1]+ me.tt[i][0])/dSampleRate;
			
			break;
			
		case "YIN":

			int frameSize = frames[0].length;
			int numFrames = frames.length;
			
			pitchValues = new double[numFrames];
			pitchProbabilities = new double[numFrames];
			isPitched = new boolean[numFrames];
			
			PitchDetector PD = PitchEstimationAlgorithm.YIN.getDetector(sampleRate, frameSize); // Ad esempio
			
			for (int i = 0; i<numFrames; i++ )
			{
				float [] frame = frames[i];
				PitchDetectionResult PDR = PD.getPitch(frame);
				pitchValues[i] = PDR.getPitch();
				pitchProbabilities[i] = PDR.getProbability();
				isPitched[i] = PDR.isPitched();
			}
			break;
			
		default:
			throw new Exception("UNRECOGNIZED ALGORITHM: " + FS.algorithmName);
		}
		
	}
	
	/**
	 * Keep only selected samples and discard other.
	 * @param keepIds array of integer indexes of samples that must be kept:
	 */
	public void selectSelf(int [] keepIds) {
		
		this.pitchValues = MyMath.select(pitchValues, keepIds);
		
		if (this.pitchProbabilities!=null)
			this.pitchProbabilities = MyMath.select(pitchProbabilities, keepIds);
		
		this.isPitched = MyMath.select(isPitched, keepIds);
		
		if (this.mid_time!=null)
			this.mid_time = MyMath.select(this.mid_time, keepIds);
	}
	
	/**
	 * Keep only selected samples and discard other.
	 * @param keepIds array of boolean indexes of samples that must be kept: true=keep, false=discard
	 */
	public void selectSelf(boolean [] keepIds) {
		this.pitchValues = MyMath.select(pitchValues, keepIds);
		
		if (this.pitchProbabilities!=null)
			this.pitchProbabilities = MyMath.select(pitchProbabilities, keepIds);
		
		this.isPitched = MyMath.select(isPitched, keepIds);
		
		if (this.mid_time!=null)
			this.mid_time = MyMath.select(this.mid_time, keepIds);
	}
}
