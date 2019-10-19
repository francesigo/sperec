package sperec_jvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import sperec_common.SPEREC_Loader;

public class SPEREC_Loader_JVM extends SPEREC_Loader {
	
	String baseFolder = "";
		
	
	/**
	 * No special constructor
	 */
	//public SPEREC_Loader_JVM() {}


	
	/**
	 * THIS WAS ABSTRACT IN THE UPPER CLASS
	 * Just convert a file path into the corresponding InputStream
	 * @param filleFullPath
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStream(String filleFullPath) throws Exception {
		
		if (!this.baseFolder.equals("")) {
			// Replace the old path with the loader path
			String fileName = new File(filleFullPath).getName();
			filleFullPath = this.baseFolder + File.separator + fileName;
		}
		
		File file = new File(filleFullPath);
		if (!file.exists()) {
			throw new Exception("Resource not found: " + filleFullPath);
		}
		InputStream is = new FileInputStream(file);
		
		return is;		
	}
	
	
	/**
	 * Store information required to load the SPEREC resources from JVM resources
	 * @param baseFolder: the folder where all the SPEREC resource files are stored.
	 */
	public void init(String baseFolder) {
		
		this.baseFolder = baseFolder;
	}
	
	
	/**
	 * THIS WAS ABSTRACT IN THE UPPER CLASS
	 * Just convert a file path into the corresponding InputStream
	 * Search alo in subfolders
	 * @param filleFullPath
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStreamDeep(String fullpath, String parentFolder) throws Exception {
		
		InputStream is = null;
		
		File f = new File(fullpath);
		if (f.exists()) {
			is = getInputStream(fullpath);
			return is;
		}
				
		String filename = f.getName();
		
		f = new File(parentFolder);
		String dirs[] = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		
		
		for (int i=0; i<dirs.length; i++) {
			String currentFolder = dirs[i];
			String newPath = parentFolder + File.separator + currentFolder;
			String newFullPath = newPath + File.separator + filename;
			is = getInputStreamDeep(newFullPath, newPath);
			if (null!=is)
				break;
		}
		
		return is;
	
	}

	
}
