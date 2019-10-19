package sperec_common;

public class AudioProcessingSpecs extends Specs{

	// Per ora le metto qui:
	public VadSpecs vadSpecs = null;
	public FeaSpecs feaSpecs = null;
	
	public AudioProcessingSpecs() {
		
	}
	/**
	 * Crea delle audioProcessingSpecs con solo VadSpecs
	 * @param vadSpecs_
	 */
	public AudioProcessingSpecs(VadSpecs vadSpecs_) {
		this.vadSpecs = vadSpecs_;
	}
	
	/**
	 * Crea delle audioProcessingSpecs con solo FeaSpecs
	 * @param feaSpecs_
	 */
	public AudioProcessingSpecs(FeaSpecs feaSpecs_) {
		this.feaSpecs = feaSpecs_;
	}
}


