package sperec_common;

public class FeaSpecs extends Specs{
	
	public String method = ""; // An id of the algorithm used to build the features
	public String dataSource = ""; // An id of the data set (audio) used as input to compute the spec. No filepath, just an id.
	
	public int iAudiosampleRate;
	public float fWindowSizeSec;
	public int iWindowSizeSamples;
	
    public int iFrameIncrementSamples;
    public float dFrameIncrementSamplesToWindowSizeSamplesRatio;
    public float fFrameIncrementSec;
    
    public int iAmountOfCepstrumCoef;
    public int iAmountOfMelFilters;
    public float fLowerFilterFreq;
    public float fUpperFilterFreq;

    
    public FeaSpecs() {
    }   
   
    /**
     * For test and debug purpose only
     * @return
     */
    static public FeaSpecs defaults() {
    	
    	FeaSpecs feaSpecs = new FeaSpecs();
		
		// Defaults values, other than in declaration
    	feaSpecs.iAudiosampleRate = 11025;
		feaSpecs.fWindowSizeSec = (float) (256.0 / (float)feaSpecs.iAudiosampleRate);
		feaSpecs.fFrameIncrementSec = feaSpecs.fWindowSizeSec/2;
		feaSpecs.dFrameIncrementSamplesToWindowSizeSamplesRatio = 0.5f;
		feaSpecs.iAmountOfCepstrumCoef = 40;
		feaSpecs.iAmountOfMelFilters = 50;
		feaSpecs.fLowerFilterFreq = 300;
		feaSpecs.fUpperFilterFreq = 3000;
		feaSpecs.method = "MFCCtarsos";
		return feaSpecs;
    }
    
    public int getFeatureDimension() {
    	return iAmountOfCepstrumCoef; // TO BE CHANGED WHEN WE HAVE MANY TYPES OF FEATURES
    }
    
    public String check() {
    	
    	String errmsg = "";
    	
		if (fWindowSizeSec<=0) {
			errmsg = errmsg + "\n" + "The window size must be a nonnegative value. (" + fWindowSizeSec + ")";
		}

		
		if (dFrameIncrementSamplesToWindowSizeSamplesRatio>=1) {
			errmsg = errmsg + "\n" + "The overlap factor must be less than 1.0";
		}

		if (iAmountOfCepstrumCoef<=0) {
			errmsg = errmsg + "\n" + "The amount of cepstral coefficients must be a positive integer number";
		}
		
		if (iAmountOfMelFilters<=0) {
			errmsg = errmsg + "\n" + "The amount of mel filters must be a positive integer number";
		}
		
		if (fLowerFilterFreq<0) {
			errmsg = errmsg + "\n" + "The lower frequency of the filter must be a non negative integer number";
		}
		
		if (fUpperFilterFreq<=0) {
			errmsg = errmsg + "\n" + "The upper frequency of the filter must be a non negative integer number";
		}
    	
    	return errmsg;
    }
    
    
    public static int [] getAllowedAudioSampleRate() {
    	int [] r = {48000, 44100, 22050, 16000, 11025, 8000};
    	return r;
    }
    public static int [] getAllowedWindowSizeSamples() {
    	int [] r = {2048, 1024, 512, 256, 128, 64, 32, 16};
    	return r;
    }
    /**
     * 
     * @param json
     * @return
     */
    static public FeaSpecs fromJsonString(String json) {
		
		return Specs.fromJsonString(json, FeaSpecs.class);
	}
    
}
