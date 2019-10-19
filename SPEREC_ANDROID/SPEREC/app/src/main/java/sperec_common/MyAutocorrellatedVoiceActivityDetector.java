package sperec_common;
/*
 * (C) Copyright 2014 Amaury Crickx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * Adapted by Francesco Sigona (2016-2019)
 * 
 * Now the class (renamed MyAutocorrellatedVoiceActivityDetector) implements
 * the AudioProcessor interface defined in the TarsosDSP library
 * (https://0110.be/posts/TarsosDSP%3A_a_small_JAVA_audio_processing_library)
 * 
 * Also, the overall processing has been split into two separate stages: 
 * - getOnOff
 * - applyOnOff
 */
import java.util.Arrays;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 * A voice activity detector attempts to detect presence or abscence of voice in the signal.
 * <p>
 * The technique used here is a simple (but efficient) one based on a characteristic of (white) noise :
 * when applying autocorrelation, the mean value of the computed cofficients gets close to zero. <br/>
 * Voice activity detection has undergone quite a lot of research, best algorithms use several hints before deciding presence or
 * absence of voice.
 * </p>
 * @see <a href="http://en.wikipedia.org/wiki/White_noise">White noise</a>
 * @see <a href="http://en.wikipedia.org/wiki/Autocorrelation">Autocorrelation</a>
 * @see <a href="http://en.wikipedia.org/wiki/Voice_activity_detection">Voice activity detection</a>
 * @see <a href="http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=6403507&punumber%3D97">Unsupervised VAD article on IEEE</a>
 * @author Amaury Crickx
 */
public class MyAutocorrellatedVoiceActivityDetector implements AudioProcessor	{
    
    private static final int WINDOW_MILLIS = 1;
    private static final int FADE_MILLIS = 2;
    private static final int MIN_SILENCE_MILLIS = 4;
    private static final int MIN_VOICE_MILLIS = 200;
        
    private float threshold = 0.0001f;

    private float[] fadeInFactors;
    private float[] fadeOutFactors;
    private boolean verboseLog = true; //Sigona2017
    
    private int inSampleCounter = 0;
    private int outSampleCounter = 0;

    public int getInSampleCounter() {
    	return inSampleCounter;
    }
    public int getOutSampleCounter() {
    	return outSampleCounter;
    }
    /**
     * Returns the noise threshold used to determine if a given section is silence or not
     * @return the threshold
     */
    public float getAutocorrellationThreshold() {
        return threshold;
    }
    
    public void setVerboseLog(boolean b) {
    	verboseLog = b;
    }

    /**
     * Sets the noise threshold used to determine if a given section is silence or not
     * @param threshold the threshold
     */
    public void setAutocorrellationThreshold(float threshold) {
        this.threshold = threshold;
    }

    /**
     * Return only the result array
     * Sigona2017
     */
    public boolean [] getOnOff(float[] voiceSample, float sampleRate) {
    	int oneMilliInSamples = (int)sampleRate / 1000;

        int length = voiceSample.length;
        int minSilenceLength = MIN_SILENCE_MILLIS * oneMilliInSamples;
        int minActivityLength = getMinimumVoiceActivityLength(sampleRate);
        boolean[] result = new boolean[length];
        
        if(length < minActivityLength) {
        	Arrays.fill(result, true);
        }
        else
        {
        	// Sigona annotation: it works with windows 1-ms large
	        int windowSize = WINDOW_MILLIS * oneMilliInSamples;
	        float[] correllation = new float[windowSize];
	        float[] window = new float[windowSize];
	        
	        for(int position = 0; position + windowSize < length; position += windowSize) {
	            System.arraycopy(voiceSample, position, window, 0, windowSize);
	            float mean = bruteForceAutocorrelation(window, correllation);
	            
	            // It fills the result in the current portion with true if mean > threshold, false otherwise
	            Arrays.fill(result, position, position + windowSize, mean > threshold);
	        }
	        
	        mergeSmallSilentAreas(result, minSilenceLength);
	        
	        int silenceCounter = mergeSmallActiveAreas(result, minActivityLength);
        }
        return result;
    }
    
    /**
     * 
     * @param voiceSample
     * @param sampleRate
     * @param result
     * @param offsetIndex
     * @param numSamples
     * @return
     */
    public float[] applyOnOff(float [] voiceSample, float sampleRate, boolean [] result, int offsetIndex, int numSamples) {
    	
    	// Get the silenceCounter
    	int silenceCounter = 0;
    	for (int i=0; i<numSamples; i++)
    		if (!result[offsetIndex+i])
    			silenceCounter++;
    	
    	// Go on
    	float[] shortenedVoiceSample = new float[numSamples - silenceCounter];
    	if (silenceCounter > 0)
    	{
            int oneMilliInSamples = (int)sampleRate / 1000;
            int fadeLength = FADE_MILLIS * oneMilliInSamples;
            initFadeFactors(fadeLength);
            
            int copyCounter = 0;
            for (int i = 0; i < numSamples; i++) {
                if (result[offsetIndex+i]) {
                    // detect lenght of active frame
                    int startIndex = i;
                    int counter = 0;
                    
                    while (i < numSamples && result[offsetIndex+ i++])
                        counter++;
                    
                    int endIndex = startIndex + counter;

                    applyFadeInFadeOut(voiceSample, fadeLength, startIndex+offsetIndex, endIndex+offsetIndex);
                    System.arraycopy(voiceSample, startIndex+offsetIndex, shortenedVoiceSample, copyCounter, counter);
                    copyCounter += counter;
                }
            }
            
        } else {
            System.arraycopy(voiceSample, 0, shortenedVoiceSample, 0, shortenedVoiceSample.length);
        }
        return shortenedVoiceSample;
    }
    
    public float[] applyOnOff(float [] voiceSample, float sampleRate, boolean [] result, int numSamples) {
    	
    	return applyOnOff(voiceSample, sampleRate, result, 0, numSamples);
    }
    
    // End Sigona2017
    
    /**
     * Removes silence out of the given voice sample
     * @param voiceSample the voice sample
     * @param sampleRate the sample rate
     * @return a new voice sample with silence removed
     */
        
    public float[] removeSilence(float[] voiceSample, float sampleRate) {
        int oneMilliInSamples = (int)sampleRate / 1000;

        int length = voiceSample.length;
        int minSilenceLength = MIN_SILENCE_MILLIS * oneMilliInSamples;
        int minActivityLength = getMinimumVoiceActivityLength(sampleRate);
        boolean[] result = new boolean[length];
        
        if(length < minActivityLength) {
            return voiceSample;
        }

        int windowSize = WINDOW_MILLIS * oneMilliInSamples;
        float[] correllation = new float[windowSize];
        float[] window = new float[windowSize];
        
        
        for(int position = 0; position + windowSize < length; position += windowSize) {
            System.arraycopy(voiceSample, position, window, 0, windowSize);
            float mean = bruteForceAutocorrelation(window, correllation);
            Arrays.fill(result, position, position + windowSize, mean > threshold);
        }
        

        mergeSmallSilentAreas(result, minSilenceLength);
        
        int silenceCounter = mergeSmallActiveAreas(result, minActivityLength);

//        System.out.println((int)((float)silenceCounter / result.length * 100.0d) + "% removed");
   
        if (silenceCounter > 0) {
            
            int fadeLength = FADE_MILLIS * oneMilliInSamples;
            initFadeFactors(fadeLength);
            float[] shortenedVoiceSample = new float[voiceSample.length - silenceCounter];
            int copyCounter = 0;
            for (int i = 0; i < result.length; i++) {
                if (result[i]) {
                    // detect lenght of active frame
                    int startIndex = i;
                    int counter = 0;
                    while (i < result.length && result[i++]) {
                        counter++;
                    }
                    int endIndex = startIndex + counter;

                    applyFadeInFadeOut(voiceSample, fadeLength, startIndex, endIndex);
                    System.arraycopy(voiceSample, startIndex, shortenedVoiceSample, copyCounter, counter);
                    copyCounter += counter;
                }
            }
            return shortenedVoiceSample;
            
        } else {
            return voiceSample;
        }
    }

    /**
     * Gets the minimum voice activity length that will be considered by the remove silence method
     * @param sampleRate the sample rate
     * @return the length
     */
    public int getMinimumVoiceActivityLength(float sampleRate) {
        return MIN_VOICE_MILLIS * (int) sampleRate / 1000;
    }

    /**
     * Applies a linear fade in / out to the given portion of audio (removes unwanted cracks)
     * @param voiceSample the voice sample
     * @param fadeLength the fade length
     * @param startIndex fade in start point
     * @param endIndex fade out end point
     */
    private void applyFadeInFadeOut(float[] voiceSample, int fadeLength, int startIndex, int endIndex) {
        int fadeOutStart = endIndex -  fadeLength;
        for(int j = 0; j < fadeLength; j++) {
            voiceSample[startIndex + j] *= fadeInFactors[j];
            voiceSample[fadeOutStart + j] *= fadeOutFactors[j];
        }
    }

    /**
     * Merges small active areas
     * @param result the voice activity result
     * @param minActivityLength the minimum length to apply
     * @return a count of silent elements
     */
    private int mergeSmallActiveAreas(boolean[] result, int minActivityLength) {
        boolean active;
        int increment = 0;
        int silenceCounter = 0;
        for(int i = 0; i < result.length; i += increment) {
            active = result[i];
            increment = 1;
            while((i + increment < result.length) && result[i + increment] == active) {
                increment++;
            }
            if(active && increment < minActivityLength) {
                // convert short activity to opposite
                Arrays.fill(result, i, i + increment, !active);
                silenceCounter += increment;
            } 
            if(!active) {
                silenceCounter += increment;
            }
        }
        return silenceCounter;
    }

    /**
     * Merges small silent areas
     * @param result the voice activity result
     * @param minSilenceLength the minimum silence length to apply
     */
    private void mergeSmallSilentAreas(boolean[] result, int minSilenceLength) {
        boolean active;
        int increment = 0;
        for(int i = 0; i < result.length; i += increment) {
            active = result[i];
            increment = 1;
            while((i + increment < result.length) && result[i + increment] == active) {
                increment++;
            }
            if(!active && increment < minSilenceLength) {
                // convert short silence to opposite
                Arrays.fill(result, i, i + increment, !active);
            } 
        }
    }

    /**
     * Initialize the fade in/ fade out factors properties
     * @param fadeLength
     */
    private void initFadeFactors(int fadeLength) {
        fadeInFactors = new float[fadeLength];
        fadeOutFactors = new float[fadeLength];
        for(int i = 0; i < fadeLength; i ++) {
            fadeInFactors[i] = (1.0f / fadeLength) * i;
        }
        for(int i = 0; i < fadeLength; i ++) {
            fadeOutFactors[i] = 1.0f - fadeInFactors[i];
        }
    }

    /**
     * Applies autocorrelation in O2 operations. Keep arrays very short !
     * Sigona annotation: it does not change voiceSample values, so in the calling function is not strictly required to make a copy
     * @param voiceSample the voice sample buffer
     * @param correllation the correlation buffer
     * @return the mean correlation value
     */
    private float bruteForceAutocorrelation(float[] voiceSample, float[] correllation) {
        Arrays.fill(correllation, 0);
        int n = voiceSample.length;
        
        // Sigona: small change: include the mean correlation computation.
        // In the original code was in a separate loop
        float mean = 0.0f;
        for (int j = 0; j < n; j++)
        {
            for (int i = 0; i < n; i++)
                correllation[j] += voiceSample[i] * voiceSample[(n + i - j) % n];
            
            mean += correllation[j];
        }
           
        /*float mean = 0.0f;
        for(int i = 0; i < voiceSample.length; i++)
            mean += correllation[i];*/
        
        return mean / correllation.length;        
    }

	@Override
	public boolean process(AudioEvent audioEvent) {
		if (verboseLog) {
			System.out.println("VAD: processing at timestamp = " + audioEvent.getTimeStamp());
		}
		float [] faWindowBuffer = audioEvent.getFloatBuffer();
		float fSampleRate = audioEvent.getSampleRate();
		boolean [] onOff = getOnOff(faWindowBuffer, fSampleRate);
		
		//int iFrameIncrementSamples=faWindowBuffer.length - audioEvent.getOverlap();
		// Take voice inside the first iFrameIncrementSamples samples
		int startIndex = audioEvent.getOverlap();
		int numSamples = faWindowBuffer.length-startIndex;
		float [] faVoiceSamples = applyOnOff(faWindowBuffer, fSampleRate, onOff, startIndex, numSamples);
		audioEvent.setFloatBuffer(faVoiceSamples);
		
		// Update my counters
		inSampleCounter += numSamples;
		outSampleCounter += faVoiceSamples.length;
		
		return true;
	}

	@Override
	public void processingFinished() {
		// TODO Auto-generated method stub
		
	}
}
