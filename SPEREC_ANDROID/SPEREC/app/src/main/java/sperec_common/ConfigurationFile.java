package sperec_common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationFile {
	static final String SECTION_TAG = "#SECTION:";
	static final int SECTION_TAG_LENGHT = SECTION_TAG.length();
	public String cfgFilePath = ""; // at run time

	
	private Map<String, HashMap<String, String>> sections = null;

	public ConfigurationFile() {
		sections = new HashMap<String, HashMap<String, String>>();
	}
	
	public void addSection(String sectionName) {
		if (!sections.containsKey(sectionName))
			sections.put(sectionName, new HashMap<String, String>());
	}
	public void removeSection(String sectionName) {
		if (sections.containsKey(sectionName))
			sections.remove(sectionName);
	}
	
	public void addItem(String sectionName, String itemName, String itemValue) {
		addSection(sectionName); // if required
		sections.get(sectionName).put(itemName, itemValue);		
	}
	
	public void addOrReplaceItem(String sectionName, String itemName, String itemValue) {
		addSection(sectionName); // if required
		if (sections.get(sectionName).containsKey(itemName))
			removeItem(sectionName, itemName);
		sections.get(sectionName).put(itemName, itemValue);		
	}
	
	public void removeItem(String sectionName, String itemName) {
		if (sections.containsKey(sectionName))
			sections.get(sectionName).remove(itemName);
	}
	
	public boolean hasSection(String sectionName) {
		return (null!=sections.get(sectionName));
	}
	
	public String getItem(String sectionName, String itemName) {
		String res = null;
		if (null!=sections)
		{
			HashMap<String, String> section = sections.get(sectionName);
		
			if (null!= section)
			{
				res = section.get(itemName);
			}
		}
		return res;
	}
	
	
	public boolean contains(ConfigurationFile y) {
		
		for(Map.Entry<String, HashMap<String, String>> sectionObject : y.sections.entrySet()) // Loop through all the sections of y
		{
			String sectionName = sectionObject.getKey();
			
			if (!this.hasSection(sectionName))
				return false;
			
			HashMap<String, String> sectionContent = sectionObject.getValue();
			for(Map.Entry<String, String> item : sectionContent.entrySet())  {
				String item_name = item.getKey();
				String item_value = item.getValue();

				String this_item_value = this.getItem(sectionName, item_name);
				
				if ( (null==this_item_value) || !this_item_value.equals(item_value))
					return false;
			}
		}
		return true;	
	}
	
	public boolean equals(ConfigurationFile y) {
		boolean this_contains_y = this.contains(y);
		boolean y_contains_this = y.contains(this);
		
		return (this_contains_y && y_contains_this);
	}
	
	public String toString() {
		final String newLine = "\n";
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, HashMap<String, String>> sectionObject : sections.entrySet()) {
			String sectionName = sectionObject.getKey();
			sb.append("#SECTION:\t"); sb.append(sectionName); sb.append(newLine);
			HashMap<String, String> sectionContent = sectionObject.getValue();
			for(Map.Entry<String, String> item : sectionContent.entrySet())  {
				String item_name = item.getKey();
				String item_value = item.getValue();
				sb.append(item_name); sb.append(":\t"); sb.append(item_value); sb.append(newLine);
			}
			//System.out.println("");
		}
		
		return sb.toString();
		
	}
	public void dump() throws IOException {
		/*
		for(Map.Entry<String, HashMap<String, String>> sectionObject : sections.entrySet()) {
			String sectionName = sectionObject.getKey();
			System.out.println("#SECTION:\t" + sectionName);
			HashMap<String, String> sectionContent = sectionObject.getValue();
			for(Map.Entry<String, String> item : sectionContent.entrySet())  {
				String item_name = item.getKey();
				String item_value = item.getValue();
				System.out.println(item_name + ":\t" + item_value);
			}
			System.out.println("");
		}*/
		System.out.println(toString()); // TODO : to try
	}
	
	public void saveAs(File f) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(f));

		for(Map.Entry<String, HashMap<String, String>> sectionObject : sections.entrySet()) {
			String sectionName = sectionObject.getKey();
			writer.write("#SECTION:\t" + sectionName + "\n");
			HashMap<String, String> sectionContent = sectionObject.getValue();
			for(Map.Entry<String, String> item : sectionContent.entrySet())  {
				String item_name = item.getKey();
				String item_value = item.getValue();
				writer.write(item_name + ":\t" + item_value + "\n");
			}
			writer.write("\n");
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * 
	 * @param destFolder
	 * @param filename
	 * @throws IOException
	 */
	public String saveAs(String destFolder, String filename) throws IOException {
		 String fpath = destFolder + File.separator + filename;
		 saveAs(fpath);
		 return fpath;
	}
	
	public void saveAs(String fullpath) throws IOException {
		 File f = new File(fullpath);
		 saveAs(f);
	}
	
	public static ConfigurationFile load(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return ConfigurationFile.load(br);
	}
	public static ConfigurationFile load(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		return ConfigurationFile.load(br);
	}
	public static ConfigurationFile load(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		return ConfigurationFile.load(br);
	}
	private static ConfigurationFile load(BufferedReader br) throws IOException {
		ConfigurationFile C = new ConfigurationFile();
		

		String lastSeenSectionName = "";
	    while (br.ready()) { //while ((line != null) && !line.equals("")) {
	    	String line = br.readLine();
	    	if ((line.length()>=SECTION_TAG_LENGHT) && (line.substring(0, SECTION_TAG_LENGHT).equals(SECTION_TAG))) {
	    		// Found a section
	    		lastSeenSectionName = line.substring(SECTION_TAG_LENGHT, line.length()).trim();
	    		C.addSection(lastSeenSectionName);
	    	} else {
	    		String[] parts = line.split(":\t");
	    		if (parts.length==2) {
	    			String itemName = parts[0].trim();
	    			String itemValue = parts[1].trim();
	    			C.addItem(lastSeenSectionName, itemName, itemValue);
	    		}
	    	}
	        
	    	//line = br.readLine();
	    }
		 
		return C;
	}
	
	//------------------------------------ TEST SAVE
	public static void testSave(String destFolder, String filename) throws IOException {
		ConfigurationFile C = new ConfigurationFile();
		C.addSection("PRIMA");
		C.addItem("PRIMA",  "entry1.1", "value1.1");
		C.addItem("PRIMA",  "entry1.2", "value1.2");
		C.addSection("SECONDA");
		C.addItem("SECONDA",  "entry2.1", "value2.1");
		C.addItem("SECONDA",  "entry2.2", "value2.2");

		C.saveAs(destFolder, filename);
	}
}
