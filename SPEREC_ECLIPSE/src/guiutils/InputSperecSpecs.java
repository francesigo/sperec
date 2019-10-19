package guiutils;

import java.io.IOException;

import app.InputSpecs;
import sperec_common.SPEREC_Factory;
import sperec_common.SPEREC_Specs;


public class InputSperecSpecs extends InputSpecs<SPEREC_Specs> {
	

	public InputSperecSpecs (SPEREC_Specs initSpecs_) {
		super(initSpecs_, SPEREC_Specs.class);
		this.cfgSectionName = "SPEREC";
		this.cfgItemName = "SperecSpecs";
		this.humanReadableName = "Sperec Specs";
		
	}
	
	public InputSperecSpecs() throws IOException {
		this(null);
	}

	/**
	 * Get form the GUI
	 * @param specs_init
	 * @return
	 */
	public SPEREC_Specs getFromGUI(SPEREC_Specs specs_init) {
		
		SPEREC_Specs specs = new SPEREC_Specs();
		
		if (null==specs_init)
			specs_init = new SPEREC_Specs();
		
		SPEREC_Factory fact = new SPEREC_Factory();
		
		specs.SPEREC_Type = ComboBox.show("SPEREC Type:", fact.getTypes());
		if ( (specs.SPEREC_Type==null) || ("".equals(specs.SPEREC_Type)) )
			return null;
		
		specs.enrollSessionDurationSec = InputNumericDouble.get(specs_init.enrollSessionDurationSec, "Input the duration [s] of the enrollment sessions");
		if ((specs.enrollSessionDurationSec == null) || (specs.enrollSessionDurationSec<=0))
			return null;

		specs.UBM = InputUbmSpecs.get(specs_init.UBM);
		if (specs.UBM==null)
			return null;

		specs.TV = InputTVSpaceSpecs.get(specs_init.TV);
		if (specs.TV==null)
			return null;


		specs.LDA = InputLDASpecs.get(specs_init.LDA);
		if (specs.LDA==null)
			return null;

		specs.GPLDA = InputGPLDASpecs.get(specs_init.GPLDA);
		if (specs.GPLDA==null)
			return null;
		
			
		return specs;
	}
			
}
