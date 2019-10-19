package guiutils;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ComboBox extends JFrame {

	static public String  show(String titolo, String [] argv) { // TO DO: introduce a preselected item
		
		String res = null;
		JComboBox  jc = new JComboBox (argv);


		Object[] message = {
				titolo, jc, //"SPEREC Type:", jc,
		};

		//int option = JOptionPane.showConfirmDialog(null, message, "Please select the SPEREC Type, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
		int option = JOptionPane.showConfirmDialog(null, message, "Please select an option, or cancel to abort\n ", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			res = argv[jc.getSelectedIndex()];
		}
		else {
			res = null;

		}
		return res;
	}

}
