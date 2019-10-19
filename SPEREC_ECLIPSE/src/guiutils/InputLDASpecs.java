package guiutils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sperec_common.LDA_Specs;
import sperec_common.TVSpace_Specs;

public class InputLDASpecs {

	/**
	 * 
	 * @return
	 */
	public static LDA_Specs get() {
		return get(new LDA_Specs());
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static LDA_Specs get(LDA_Specs s) {

		LDA_Specs cfg = new LDA_Specs(); // Not null

		JTextField iLdaDimField = new JTextField(Integer.toString(s.LDADIM));

		Object[] message = {
				"Ciao", null,
				"Linear Discriminant Analysis dimension:", iLdaDimField,
		};

		boolean again = true;
		String errmsg = "";

		while (again) {
			message[0] = errmsg + "Please enter the required fields, or cancel to abort";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {

				errmsg = "";

				try {
					cfg.LDADIM = Integer.parseInt(iLdaDimField.getText());
				} catch (Exception e) {
				}
				if (cfg.LDADIM<=0) {
					errmsg = errmsg + "\n" + "The LDA dimension must be positive";
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
