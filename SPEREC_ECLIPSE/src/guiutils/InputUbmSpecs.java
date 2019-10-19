package guiutils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sperec_common.UBM_Specs;

public class InputUbmSpecs {

	public static UBM_Specs get() {
		return get(new UBM_Specs());
	}
	
	public static UBM_Specs get(UBM_Specs s) {
		
		UBM_Specs cfg = new UBM_Specs(); // Not null

		JTextField iNiterField = new JTextField(Integer.toString(s.niter_ubm));
		JTextField iNmixField = new JTextField(Integer.toString(s.NMIX));
				
		Object[] message = {
				"Ciao", null,
				"Number of MIxture Components:", iNmixField,
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
					cfg.NMIX = Integer.parseInt(iNmixField.getText());
				} catch (Exception e) {
				}
				if ((cfg.NMIX & (cfg.NMIX - 1))!=0) { // (n & (n - 1)) == 0 means that n is a power of 2
					errmsg = errmsg + "\n" + "The number of componens must be a power of 2";
				}

				try {
					cfg.niter_ubm = Integer.parseInt(iNiterField.getText());
				} catch (Exception e) {
				}
				if (cfg.niter_ubm<=0) {
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
