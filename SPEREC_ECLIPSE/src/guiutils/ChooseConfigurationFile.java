package guiutils;

import java.io.IOException;

import javax.swing.JOptionPane;

import sperec_common.ConfigurationFile;

public class ChooseConfigurationFile {


	public static ConfigurationFile get(String initFolderOrFile, String opName) throws IOException {

		ConfigurationFile cfg = null;

		// Ask if the user needs a template
		int option = JOptionPane.showConfirmDialog(null, "Do you want to open a template from a past " + opName + " work?", "", JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION)
		{
			String cfgFilePath = ChooseFile.get(initFolderOrFile, "Select the template file", "(*.cfg)", "cfg");
			if ((null==cfgFilePath) || cfgFilePath.equals(""))
				return null;

			cfg = ConfigurationFile.load(cfgFilePath);
			cfg.cfgFilePath = cfgFilePath;
			System.out.println("Configuration file loaded: " + cfgFilePath);
			cfg.dump();
		}
		else
		{
			cfg = new ConfigurationFile();
		}

		return cfg;
	}


}
