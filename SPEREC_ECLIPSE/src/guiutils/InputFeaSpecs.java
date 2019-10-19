package guiutils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import app.InputSpecs;
import sperec_common.FeaSpecs;

/**
 * @author FS
 *
 */
public class InputFeaSpecs extends InputSpecs<FeaSpecs> {
	
	JComboBox<Float> fWindowSizeSecField = null;
	JComboBox<Integer> iSampleRateField = null;
	
	
	public InputFeaSpecs() throws IOException {
		this(null);
	}
	public InputFeaSpecs(FeaSpecs initSpecs) throws IOException {
		super(initSpecs, FeaSpecs.class);
		this.cfgSectionName = "FEA";
		this.cfgItemName = "FeaSpecs";
		this.humanReadableName = "Feature Extraction";
	}
	
	/**
	 * 
	 */
	public FeaSpecs getFromGUI(FeaSpecs fea) {
		
		if (fea==null)
			fea = FeaSpecs.defaults();
		
		JComboBox<String> sMethodField = new JComboBox<String>();
		sMethodField.setBackground(Color.WHITE);
		sMethodField.addItem(FeaSpecs.defaults().method);
		
		fWindowSizeSecField = new JComboBox<Float>();
		fWindowSizeSecField.setBackground(Color.WHITE);
		
		iSampleRateField = new JComboBox<Integer>();
		iSampleRateField.setBackground(Color.WHITE);
		ActionListener a = new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				set_fWindowSizeSecField();
			}
		};		    
		    
		iSampleRateField.addActionListener( a);
		    
		set_iSampleRateField();



		boolean found = false;
		for (int i = 0; i<iSampleRateField.getItemCount(); i++)
			if (iSampleRateField.getItemAt(i)==fea.iAudiosampleRate)
			{
				iSampleRateField.setSelectedIndex(i);
				found = true;
				break;
			}
		if (!found)
			iSampleRateField.setBackground(Color.RED);
		
		
		found = false;
		for (int i = 0; i<fWindowSizeSecField.getItemCount(); i++)
			if (fWindowSizeSecField.getItemAt(i)==fea.fWindowSizeSec)
			{
				fWindowSizeSecField.setSelectedIndex(i);
				found = true;
				break;
			}
		if (!found)
			fWindowSizeSecField.setBackground(Color.RED);
		
		
		//set_fWindowSizeSecField();
			
		JTextField dFrameIncrementSamplesToWindowSizeSamplesRatioField = new JTextField(Double.toString(fea.dFrameIncrementSamplesToWindowSizeSamplesRatio));
		JTextField iAmountOfCepstrumCoefField = new JTextField(Integer.toString(fea.iAmountOfCepstrumCoef));
		JTextField iAmountOfMelFiltersField = new JTextField(Integer.toString(fea.iAmountOfMelFilters));
		JTextField fLowerFilterFreqField = new JTextField(Double.toString(fea.fLowerFilterFreq));
		JTextField fUpperFilterFreqField = new JTextField(Double.toString(fea.fUpperFilterFreq));
		
		Object[] message = {
				"Ciao", null,
				"Method:", sMethodField,
				"Audio sample rate:", iSampleRateField,
				"Window size [s]:", fWindowSizeSecField,
				"Frame increment to windows size ratio ( <1):", dFrameIncrementSamplesToWindowSizeSamplesRatioField,
				"Amount fo cepstral coefficients: ", iAmountOfCepstrumCoefField,
				"Amount of Mel filters: ", iAmountOfMelFiltersField, 
				"Filter's lower frequency [Hz]: ", fLowerFilterFreqField, 
				"Filter's upper frequency [Hz]: ", fUpperFilterFreqField
		};

		boolean again = true;
		String errmsg = "";
		FeaSpecs s = new FeaSpecs(); // temporary
		
		while (again)
		{
			message[0] = errmsg + "Please enter the required fields, or cancel to abort\n\n";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {

				errmsg = "";
				
				try {
					//s.fWindowSizeSec = Float.parseFloat(fWindowSizeSecField.getText());
					s.iAudiosampleRate = (int) iSampleRateField.getSelectedItem();
				} catch (Exception e) {
				}
				
				try {
					//s.fWindowSizeSec = Float.parseFloat(fWindowSizeSecField.getText());
					s.method = (String) sMethodField.getSelectedItem();
				} catch (Exception e) {
				}
				
				
				s.fWindowSizeSec = -100000;
				try {
					//s.fWindowSizeSec = Float.parseFloat(fWindowSizeSecField.getText());
					s.fWindowSizeSec = (float) fWindowSizeSecField.getSelectedItem();
				}
				finally{};
				//catch (Exception e) {}
				
				s.dFrameIncrementSamplesToWindowSizeSamplesRatio = -1000000;
				try {
					s.dFrameIncrementSamplesToWindowSizeSamplesRatio = Float.parseFloat(dFrameIncrementSamplesToWindowSizeSamplesRatioField.getText());
				} catch (Exception e) {
				}
				

				s.iAmountOfCepstrumCoef = -1;
				try {
					s.iAmountOfCepstrumCoef = Integer.parseInt(iAmountOfCepstrumCoefField.getText());					
				} catch (Exception e) {
				}
				
				
				s.iAmountOfMelFilters = -1;
				try {
					s.iAmountOfMelFilters = Integer.parseInt(iAmountOfMelFiltersField.getText());					
				} catch (Exception e) {
				}
				
				
				s.fLowerFilterFreq = -1;
				try {
					s.fLowerFilterFreq = Float.parseFloat(fLowerFilterFreqField.getText());
				} catch (Exception e) {
				}
				
				
				s.fUpperFilterFreq = -1;
				try {
					s.fUpperFilterFreq = Float.parseFloat(fUpperFilterFreqField.getText());
				} catch (Exception e) {
				}
				
				errmsg = s.check();
							
				
				if (!errmsg.isEmpty()) {
					errmsg = "ERROR: " + errmsg + "\n";
					System.out.println(errmsg);
				} else
					again = false;

			} else {
				System.out.println("Operation aborted");
				s = null;
				again = false;
			}
		}

		return s;
		
	}
	
	
	/**
	 * Shortcut to get the selected value of sample rate
	 * @return
	 */
	private int getSelectedSampleRate() {
		return (Integer)iSampleRateField.getSelectedItem();	
	}
	
	/**
	 * Set the allowed values, depending on the selected sample rate value
	 */
	void set_fWindowSizeSecField() {
		// Get the selected sampleRate
		int iSampleRate = getSelectedSampleRate();
		float dSamplePeriod = 1.0f/(float)iSampleRate;
		final int [] samples = FeaSpecs.getAllowedWindowSizeSamples();
		fWindowSizeSecField.removeAllItems();
		for (int i = 0; i<samples.length; i++)
			fWindowSizeSecField.addItem( (float)samples[i]* dSamplePeriod  );	
	}
	
	/**
	 * Initialize the combo of audioSampleRate values
	 */
	void set_iSampleRateField () {
		int r[] = FeaSpecs.getAllowedAudioSampleRate();
		for (int i=0; i<r.length; i++)
			iSampleRateField.addItem(r[i]);
	}
	
	/**
	 * For test and debug purpose
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		FeaSpecs initSpecs = FeaSpecs.defaults();
		InputFeaSpecs me = new InputFeaSpecs(initSpecs);
		FeaSpecs specsFinal = me.getSpecs();
		if (specsFinal != null)
			specsFinal.dump();
	}
}
