package guiutils;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class InputNumericInteger {

	public static Integer get(int v, String title) {
		Integer res = null;
		JTextField jField = new JTextField(Integer.toString(v));

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
					res = Integer.parseInt(jField.getText());
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
