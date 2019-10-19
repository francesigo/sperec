package app;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.google.gson.Gson;

import guiutils.ChooseFile;
import guiutils.ChooseFolder;
import guiutils.InputSperecSpecs;
/*
public class prova extends InputSpecs<CrossValidationSpecs> {

	public prova() throws IOException {
		super(CrossValidationSpecs.class);
		this.cfgSectionName = "SEZIONE PROVA";
		this.cfgItemName = "ITEM PROVA";
		this.humanReadableName = " --- PROVA --- ";
		// TODO Auto-generated constructor stub
	}


	
	@Override
	public CrossValidationSpecs getFromGUI(CrossValidationSpecs s) throws IOException {

		
		if (s==null)
			s = new CrossValidationSpecs();


		CrossValidationSpecs cfg = new CrossValidationSpecs(); // Not null

		JTextField testSessionDurationSecField = new JTextField(new Gson().toJson(s.testSessionDurationSec_v));
		JTextField enrollSessionDurationSecField = new JTextField(Double.toString(s.enrollSessionDurationSec));
		JTextField numberOfDevelSpksField = new JTextField(Integer.toString(s.numberOfDevelSpks));


		Object[] message = {
				"Ciao", null,
				"Durate delle sessioni di test, in sec.,  (es. [ 2.0, 4.0 ]:", testSessionDurationSecField,
				"Durata delle sessioni di enrollent (un solo valore): ", enrollSessionDurationSecField,
				"Numero di parlanti di test in ogni gruppo:", numberOfDevelSpksField,
		};

		boolean again = true;
		String errmsg = "";

		while (again) {
			message[0] = errmsg + "Please enter the required fields, or cancel to abort";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {

				errmsg = "";

				try {
					cfg.testSessionDurationSec_v = stringToArrayOfDouble(testSessionDurationSecField.getText());
				} catch (Exception e) {
					throw e;
				}
				//if (cfg.TVDIM<=0) {
					//errmsg = errmsg + "\n" + "The Total Variability Space dimension must be positive";
				//}

				try {
					cfg.enrollSessionDurationSec = Double.parseDouble(enrollSessionDurationSecField.getText());
				} catch (Exception e) {
					throw e;
				}
				if (cfg.enrollSessionDurationSec<=0) {
					errmsg = errmsg + "\n" + "The duration of the enrollement sessions must be positive";
				}

				
				try {
					cfg.numberOfDevelSpks = Integer.parseInt(numberOfDevelSpksField.getText());
				} catch (Exception e) {
					throw e;
				}
				if (cfg.numberOfDevelSpks<=0) {
					errmsg = errmsg + "\n" + "The number of develompment speakers must be positive";
				}

				if (!errmsg.isEmpty()) {
					errmsg = "ERROR: " + errmsg + "\n";
					System.out.println(errmsg);
				} else
					again = false;

			} else {
				System.out.println("Operation aborted");
				cfg = null; // Discard all
				again = false;
			}
		}

		if (cfg==null)
			return null;

		// Server specs
		InputSperecSpecs inputSperecSpecs = new InputSperecSpecs(s.specs);
		cfg.specs = inputSperecSpecs.getSpecs();
		if (cfg.specs == null)
			return null;


		// Need to select the folder of INPUT files
		cfg.feaConfigFile = ChooseFile.get(s.feaConfigFile, "Select the feature configuration file", "cfg");
		if (cfg.feaConfigFile.equals(""))
			return null;


		// Optionally, select the file of cached enrollment sessions for non-target comparison
		cfg.enrollmentSessionsFile = ChooseFile.get(s.enrollmentSessionsFile, "(OPTIONAL) Select the file of the enrollment sessions for background and non-target comparisons", "cache");
		
		// If no cache have been selected, ask for the clean files
		if (cfg.enrollmentSessionsFile.equals("")) {
			
			cfg.cleanSpeechDir = ChooseFolder.get(s.cleanSpeechDir, "Select the folder with the clean original speech files, for background and non-target comparison");
			if (cfg.cleanSpeechDir.equals(""))
				return null;
		}
		
		
		return cfg;
	}*/
	
	/*
	static double [] stringToArrayOfDouble(String line) {
		String s = line.trim();

		if (s.charAt(0) != '[')
			s = "[" + s;

		if (s.charAt(s.length()-1) != ']')
			s = s + "]";

		return new Gson().fromJson(s, double[].class);

	}
	
	public static void main(String[] args) throws IOException {
		
		prova p = new prova();
		p.getClass();
	}




}
*/