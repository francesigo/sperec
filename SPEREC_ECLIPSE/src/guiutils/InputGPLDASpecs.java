package guiutils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sperec_common.GPLDA_Specs;

public class InputGPLDASpecs {
	
	/**
	 * 
	 * @return
	 */
	public static GPLDA_Specs get() {
		return get(new GPLDA_Specs());
	}
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static GPLDA_Specs get(GPLDA_Specs s) {

		GPLDA_Specs cfg = new GPLDA_Specs(); // Not null

		JTextField iNiterField = new JTextField(Integer.toString(s.niter_gplda));
		JTextField iNphiField = new JTextField(Integer.toString(s.NPHI));

		Object[] message = {
				"Ciao", null,
				"Eigenvoice (PHI) dimension:", iNphiField,
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
					cfg.NPHI = Integer.parseInt(iNphiField.getText());
				} catch (Exception e) {
				}
				if (cfg.NPHI<=0) {
					errmsg = errmsg + "\n" + "The Eigenvoice (PHI) dimension must be positive";
				}

				try {
					cfg.niter_gplda = Integer.parseInt(iNiterField.getText());
				} catch (Exception e) {
				}
				if (cfg.niter_gplda<=0) {
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
