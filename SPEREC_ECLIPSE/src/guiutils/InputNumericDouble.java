package guiutils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class InputNumericDouble {

	public static Double get(double v, String title) {
		Double res = null;
		JTextField jField = new JTextField(Double.toString(v));

		Object[] message = {
				"Ciao", null,
				title, jField,
		};

		boolean again = true;
		String errmsg = "";

		while (again) {
			message[0] = errmsg + "Please enter the required fields, or cancel to abort";
			int option = JOptionPane.showConfirmDialog(null, message, "Please enter the required fields, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				errmsg = "";
				try {
					res = Double.parseDouble(jField.getText());
					again = false;
				} catch (Exception e) {
				}

			} else {
				System.out.println("Operation aborted");
				res = null; // Discard all
				again = false;
			}
		}

		return res;

	}
}
