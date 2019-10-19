package myDSP;

import myMath.MyMath;


public class MyFraming extends MyFramingSettings {
	
	MyFramingSettings inputSettings = null;
	
	public int dur_samples = -1;
	int overlap_samples = -1;
	public int frame_increment_samples = -1;
	double frame_increment_sec = -1;
	double overlap_factor = -1;
	
	float [][] frames;  // The frames
	public double [] midTimes; // The middle time of each frame;
	public double [] startTimes; // The begin time of each frame
	public double [] endTimes; // The begin time of the next frame
	
	int [] startSample;
	int [] endSample;
	
	float [] tail = null; // The remaining unframed portion of the signal
	
	/**
	 * Just the very basic initialization
	 * @param framingSettings the specifications of framing operations
	 */
	public MyFraming(MyFramingSettings framingSettings) {
		
		super(framingSettings);
		
		this.inputSettings = new MyFramingSettings(framingSettings); // Remember the input settings

	}
	
	public MyFraming() {
		// TODO Auto-generated constructor stub
	}

	public int getNumberOfFrames() {return frames.length;}
	
	public float [][] getFrames() {return frames;}
	

	/**
	 * Split u.samples into frames according the framing specs
	 * @param u the Utterance instance containing the audio samples
	 */
	public void process(Utterance u) {
				
		double dSampleRate = (double)u.sampleRate;
		
		// The number of samples in the window
		dur_samples = Math.max(1,  (int)Math.round(window_sec * dSampleRate));
		
		// The number of samples that overlap
		overlap_samples = (int)Math.round(overlap_sec * dSampleRate);
		
		// The increment as number of sample
		frame_increment_samples = dur_samples - overlap_samples;
		
		// The increment as time units (s)
		frame_increment_sec = (double)frame_increment_samples / dSampleRate;
		
		// Recompute the length of the windows in time units (s)
		window_sec = (double)dur_samples / dSampleRate;
		
		// Recompute the overlap in time units (s)
		overlap_sec = (double)overlap_samples / dSampleRate;
		
		// Recompute the time increment
		frame_increment_sec = (double)frame_increment_samples / dSampleRate;
		
		// Define the overlap factor
		overlap_factor = (double)dur_samples / (double)overlap_samples;
		
		// The number of frames
		int numFrames = 1 + (u.samples.length - dur_samples)/frame_increment_samples; //1+Math.floorDiv(u.samples.length - dur_samples, frame_increment_samples); // Math.floorDiv is not supported by API 19
		
		// Allocate rooms for each frame, start times, end times and mid times, start sample and ending sample
		frames = new float[numFrames][dur_samples];
		startTimes = new double [numFrames];
		endTimes = new double [numFrames];
		midTimes = new double [numFrames];
		startSample = new int[numFrames];
		endSample = new int[numFrames];
		
		// Actally split into overlapping frames
		int start = 0;
		double currentTime = 0;
		for (int i = 0; i<numFrames; i++)
		{
			System.arraycopy(u.samples,  start, frames[i],  0,  dur_samples);
			startSample[i] = start;
			endSample[i] = start+dur_samples-1; //Include the last sample
			
			currentTime = (double)start/dSampleRate;
			startTimes[i] = currentTime;
			endTimes[i] = currentTime + window_sec;
			midTimes[i] = currentTime + 0.5*window_sec;			
			start+=frame_increment_samples;
		}
		
		// At the end there is a tail: put it in a separate tail variable
		int tailLength = u.samples.length - (endSample[numFrames-1] +1);
		tail = new float[tailLength];
		System.arraycopy(u.samples,  endSample[numFrames-1]+1, tail,  0, tailLength);
		
	}
	
	
	/**
	 * Select a subset of frames
	 * @param keep array of boolean indexes of the frames to be selected
	 * @return a new MyFraming instance of the selected frames
	 * @throws Exception
	 */
	public MyFraming select(boolean [] keep) throws Exception {
		
		if (getNumberOfFrames() != keep.length)
			throw new Exception("Mismatch");
				
		int [] ids = MyMath.find(keep);
		
		return select(ids);
	}

	/**
	 * Select a subset of frames
	 * @param ids the integer indexes of the frames to be selected
	 * @return a new MyFraming instance of the selected frames
	 * @throws Exception
	 */
	public MyFraming select(int [] ids) throws Exception {
						
		MyFraming newFraming = new MyFraming();
		newFraming.frames = MyMath.select(this.frames, ids);  // The frames
		newFraming.midTimes = MyMath.select(this.midTimes, ids); // The middle time of each frame;
		newFraming.startTimes = MyMath.select(this.startTimes, ids); // The begin time of each frame;
		newFraming.endTimes = MyMath.select(this.endTimes, ids); // The end time of each frame;;
		newFraming.startSample = MyMath.select(this.startSample, ids); // The end time of each frame;;
		newFraming.endSample = MyMath.select(this.endSample, ids); // The end time of each frame;;
		
		return newFraming;
	}


	
	/**
	 * Show info about the framing
	 */
	public void showInfo() {
		// TODO
		
		// Show the input framing setting
		inputSettings.showInfo("Input settings:");
		
		super.showInfo("Current settings:");
		
		System.out.println("Number of frames: " + frames.length);
		showInfoFrame(0);
		showInfoFrame(1);
		showInfoFrame(2);
		showInfoFrame(frames.length-1);

	}
	
	private void showInfoFrame(int i) {
		float [] frame = frames[i];
		int startSample = this.startSample[i];
		int endSample = this.endSample[i];
		int durationSamples = frames.length;
		double startTime = this.startTimes[i];
		double endTime = this.endTimes[i];
		double midTime = this.midTimes[i];
		
		System.out.println("Frame #" + i + ":\tstart  at " + startTime + " (" + startSample + ")");
		System.out.println("\t\tend    at " + endTime + " (" + endSample + ")");
		System.out.println("\t\tcenter at " + midTime);
		System.out.println("\t\tduration: " + (endTime-startTime) + " (" + durationSamples + ")");
		
	}
}
