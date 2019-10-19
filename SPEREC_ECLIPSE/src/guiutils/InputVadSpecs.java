package guiutils;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import app.InputSpecs;
import sperec_common.VadSpecs;

public class InputVadSpecs extends InputSpecs<VadSpecs>{
	
	//JComboBox<Integer> iSampleRateField = null;
	
	public InputVadSpecs() throws IOException {
		super(VadSpecs.class);
		this.cfgSectionName = "VAD";
		this.cfgItemName = "VadSpecs";
		this.humanReadableName = "Voice Activity Detection (VAD)";
	}
	
	

	/**
	 * 
	 * @param s
	 * @return
	 */
	public VadSpecs getFromGUI(VadSpecs initSpecs) {
		
		if (initSpecs == null)
			initSpecs = VadSpecs.defaults();
		
		JComboBox<String> sMethodField = new JComboBox<String>();
		sMethodField.setBackground(Color.WHITE);
		sMethodField.addItem(VadSpecs.defaults().getMethod());
		
		
		/*iSampleRateField = new JComboBox<Integer>();
		iSampleRateField.setBackground(Color.WHITE);
		ActionListener a = new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				//set_fWindowSizeSecField();
			}
		};
		iSampleRateField.addActionListener( a);
	    
		set_iSampleRateField();
		
		boolean found = false;
		for (int i = 0; i<iSampleRateField.getItemCount(); i++)
			if (iSampleRateField.getItemAt(i)==initSpecs.getAudioSampleRate())
			{
				iSampleRateField.setSelectedIndex(i);
				found = true;
				break;
			}
		if (!found)
			iSampleRateField.setBackground(Color.RED);
			*/
		
		JTextField frameIncrementField = new JTextField(Double.toString(initSpecs.getFrameIncrementSec()));
		JTextField overlapFactorField = new JTextField(Double.toString(initSpecs.getOverlapFactor()));
		String errmsg = "";

		Object[] message = {
				"Ciao", null,
				"Method:", sMethodField,
				//"Audio sample rate:", iSampleRateField,
				"Frame increment [s]:", frameIncrementField,
				"Overlap Factor ( >1):", overlapFactorField
		};

		boolean again = true;
		VadSpecs s = new VadSpecs(); // temporary

		while (again) {
			message[0] = errmsg + "Please enter the required fields, or cancel to abort\n\n";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {

				/*try {
					//s.fWindowSizeSec = Float.parseFloat(fWindowSizeSecField.getText());
					s.setAudioSampleRate((int) iSampleRateField.getSelectedItem());
				} catch (Exception e) {
				}*/
				
				try {
					s.setFrameIncrementSec(Double.parseDouble(frameIncrementField.getText()));
				} catch (Exception e) {
				}
				

				try {
					s.setOverlapFactor(Double.parseDouble(overlapFactorField.getText()));
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
	 * Initialize the combo of audioSampleRate values
	 */
	/*void set_iSampleRateField () {
		int r[] = VadSpecs.getAllowedAudioSampleRate();
		for (int i=0; i<r.length; i++)
			iSampleRateField.addItem(r[i]);
	}*/
	
	
	/**
	 * For test and debug purpose
	 * @param arg
	 * @throws IOException 
	 */
	public static void main(String[] arg) throws IOException {
		
		InputVadSpecs me = new InputVadSpecs();
		VadSpecs specs = me.getSpecs();
		
		if (specs!=null)
		{
			specs.dump();
		
			// Visto che ci sono, chiedi se vuole salvare
			/*ConfigurationFile outConfigFile = new ConfigurationFile();
			outConfigFile.addSection(me.cfgSectionName);
			outConfigFile.addItem(me.cfgSectionName, me.cfgItemName, specs.toJsonString());
			
			String outPath = SaveFile.as(me.MAIN_OUTPUT_FOLDER_PATH, "Salva il file di configurazione");
			
			outConfigFile.saveAs(outPath);*/
		}
		
	}
}
