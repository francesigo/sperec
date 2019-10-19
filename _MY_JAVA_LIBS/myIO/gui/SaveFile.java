package myIO.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SaveFile {

	/**
	 * 
	 * @param init
	 * @param title
	 * @return: the full path of the saved file, or ""
	 */
	public static String as(String init, String title, String fileFilter) {
		
		String res = "";

		JFileChooser c = new JFileChooser();
		File f = new java.io.File(init);
		c.setCurrentDirectory(f);
		c.setSelectedFile(f);
		c.setDialogTitle(title);
		
		if ( (fileFilter!=null) && !fileFilter.equals("") )
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter(fileFilter, fileFilter); // e.g. mp3
			c.setFileFilter(filter);
		}
		
		
	    // Demonstrate "Save" dialog:
	    int rVal = c.showSaveDialog(null);
	    if (rVal == JFileChooser.APPROVE_OPTION)
	    {
	      String filename = c.getSelectedFile().getName();
	      
	      if (!filename.endsWith("." + fileFilter))
	    	  filename = filename + "." + fileFilter;
	      
	      String folder = c.getCurrentDirectory().toString();
	      res = folder + File.separator + filename;
	    }
	    if (rVal == JFileChooser.CANCEL_OPTION) {
	    }
	    return res;
	}
	
	public static String as(String init, String title) {
		return as(init, title, "");
	}
	
	/**
	 * For test purpose
	 * @param args
	 */
	public static void main(String [] args) {
		String pippo = SaveFile.as("F:\\GDrive_Unisalento\\FS\\_DOTTORATO\\pluto.cfg", "Scegli un file", "cfg");
		
		System.out.println(pippo);
				
	}
}
