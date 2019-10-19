package myIO.gui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ChooseFile {

	public static String get(String init, String title, String fileFilter) {
		
		String dir = "";
		String filePath = "";
		
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);

		if ( (init!=null) && (!init.equals("")) )
		{
			if (Files.exists(Paths.get(init))) {
				File f = new File(init);
				if (f.isDirectory()){
					dir = init;
					fc.setCurrentDirectory(f);
				}
				else
				{
					dir = f.getPath();
					fc.setCurrentDirectory(new File(dir));
					fc.setSelectedFile(new File(init));
				}
			}
			else
			{
				if (new File(init).getParentFile()!=null)
					init = new File(init).getParentFile().toString();
			}
			
		}
		else
		{
			//fc.setCurrentDirectory(new File(new Environment().getDataDir()));
		}
		
		if ( (fileFilter!=null) && !fileFilter.equals("") )
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter(fileFilter, fileFilter); // e.g. mp3
			fc.setFileFilter(filter);
		}
		
		int returnVal = fc.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return "";

		File feaCfgFile = fc.getSelectedFile();
		filePath = feaCfgFile.getPath();
		
		return filePath;
	}
	
	
	/**
	 * For test and debug purpose
	 * @param args
	 */
	public static void main(String [] args) {
		String f = ChooseFile.get("", "Choose ta wav file", "wav");
		System.out.println(f);
		
	}
	
	
}
