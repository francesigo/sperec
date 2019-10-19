package guiutils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sperec_common.TVSpace_Specs;

public class InputTVSpaceSpecs {

	public static TVSpace_Specs get() {
		return get(new TVSpace_Specs());
	}
	
	public static TVSpace_Specs get(TVSpace_Specs s) {
		
		TVSpace_Specs cfg = new TVSpace_Specs(); // Not null

		JTextField iNiterField = new JTextField(Integer.toString(s.niter_tv));
		JTextField iNmixField = new JTextField(Integer.toString(s.TVDIM));
				
		Object[] message = {
				"Ciao", null,
				"Total Variability Space dimension:", iNmixField,
				"Number of iterations:", iNiterField
		};

		boolean again = true;
		String errmsg = "";

		while (again) {
			message[0] = errmsg + "Please enter the required fields, or cancel to abort";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {

				errmsg = "";
				
				try {
					cfg.TVDIM = Integer.parseInt(iNmixField.getText());
				} catch (Exception e) {
				}
				if (cfg.TVDIM<=0) {
					errmsg = errmsg + "\n" + "The Total Variability Space dimension must be positive";
				}

				try {
					cfg.niter_tv = Integer.parseInt(iNiterField.getText());
				} catch (Exception e) {
				}
				if (cfg.niter_tv<=0) {
					errmsg = errmsg + "\n" + "The number of iterations must be positive";
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


		return cfg;
	}
		
}
