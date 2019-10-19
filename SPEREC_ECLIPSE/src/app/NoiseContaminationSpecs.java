package app;

import sperec_common.Specs;

/**
 * Define the cross validation configuration with some default values
 * @author FS
 *
 */
public class NoiseContaminationSpecs extends Specs
{
	public double SNRdB;

	//public String inputDir = "";
	public String inputFileList;

	public String inputNoiseFile = "";

	
	static public NoiseContaminationSpecs fromJsonString(String json) {
		return Specs.fromJsonString(json, NoiseContaminationSpecs.class);
	}
	
}

