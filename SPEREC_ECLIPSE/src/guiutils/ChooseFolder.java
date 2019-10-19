package guiutils;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;

import app.Environment;


public class ChooseFolder {
			
	
	/**
	 * Use a GUI to ask for the folder containing the files to process
	 * @param init
	 * @param init
	 * @param title
	 * @return
	 */
	public static String get(String init, String title) {
		String selDir = null;
		JFileChooser  chooser = new JFileChooser();

		if ( (init==null) || (init=="") || !Files.exists(Paths.get(init)))
			init = new Environment().getDataDir();

		chooser.setCurrentDirectory(new java.io.File(init));
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			selDir = chooser.getSelectedFile().getPath();
			System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
			System.out.println("getSelectedFile() : " +  chooser.getSelectedFile());
			return selDir;
		}
		else {
			System.out.println("No Selection ");
			return "";
		}
	}
	
	

}
