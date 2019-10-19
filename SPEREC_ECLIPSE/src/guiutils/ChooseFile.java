package guiutils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import app.Environment;
import sperec_common.MyURL;

public class ChooseFile {

	public static String get(String init, String title, String fileFilterDesc, String ...fileFilter) {
		
		String dir = "";
		String filePath = "";
		
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);

		if ( (init!=null) && (!init.equals("") && Files.exists(Paths.get(init))) )
		{
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
			fc.setCurrentDirectory(new File(new Environment().getDataDir()));
		}
		
		//if ( (fileFilter!=null) && !fileFilter.equals(""))
		if ( (fileFilter!=null) && (fileFilter.length>0))
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter(fileFilterDesc, fileFilter); // e.g. mp3
			fc.setFileFilter(filter);
		}
		
		int returnVal = fc.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return "";

		File feaCfgFile = fc.getSelectedFile();
		filePath = feaCfgFile.getPath();
		
		// Experimental: try to return special address (http)
		String currentDir = fc.getCurrentDirectory().toString();
		String boxContent = filePath.substring(currentDir.length()+1);
		
		if (MyURL.isURL(boxContent))
			return MyURL.fixURL(boxContent);
		
		return filePath;
	}
	
	
	/**
	 * For test and debug purpose
	 * @param args
	 */
	public static void main(String [] args) {
		String f = ChooseFile.get("", "Choose ta wav file", "(*.wav)", "wav");
		System.out.println(f);
		
	}
	
	
}
