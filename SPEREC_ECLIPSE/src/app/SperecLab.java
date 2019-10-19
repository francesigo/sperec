package app;

import java.awt.BorderLayout;
import java.awt.Container;
//import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
//import java.awt.geom.Rectangle2D;
//import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FilenameUtils;

import app.old.NoiseValidation_OLD;
import app.old.Voice2fea_OLD;
/*import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
*/
import guiutils.ChooseFile;
import guiutils.ChooseFolder;
import guiutils.InputSperecSpecs;
import sperec_common.ConfigurationFile;
import sperec_common.EmpiricalFn;
import sperec_common.FeaSpecs;
import sperec_common.MiscUtils;
import myMath.MyMatrix;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC_Specs;
import sperec_common.SessionsTable_MyMatrix;
import sperec_common.ValidationResultGauss;
import sperec_common.ValidationResults;
import myMath.MyMatrix.VectorDim;
import sperec_jvm.POPREF_Builder;
import sperec_jvm.SPEREC_Loader_JVM;

public class SperecLab implements ActionListener, ItemListener {

	// GUI Section
	static JTextArea output;
	JScrollPane scrollPane;
	static final String newline = "\n";

	// Algorithm section	
	static SPEREC_Specs specs = null;

	private static String MAIN_OUTPUT_FOLDER_PATH = ""; //Environment.getMainOutputFolderPath();
	
	public JMenuBar createMenuBar() {
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		//JRadioButtonMenuItem rbMenuItem;
		//JCheckBoxMenuItem cbMenuItem;

		//Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu
		menu = new JMenu("New...");		//menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("New");
		menuBar.add(menu);

		menuItem = new JMenuItem("Reference Population Model...");
		menuItem.setToolTipText("Create a new background model");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Export...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		// Build the second menu
		menu = new JMenu("Cross Validation");
		menu.getAccessibleContext().setAccessibleDescription("Cross-validation related stuff");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Do Cross Validation...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Attach validation results...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Show validation results...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		/*menuItem = new JMenuItem("Noise and duration validation");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		*/
		
		
		// Build third menu
		menu = new JMenu("Audio processing");		//menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Feature extracion realted stuff");
		menuBar.add(menu);
		
		
		menuItem = new JMenuItem("Split long sessions...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		
		menuItem = new JMenuItem("Noise contamination...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Voice Activity Detection (VAD)...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Feature extraction...");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		

		return menuBar;

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		/*JMenuItem source = (JMenuItem)(e.getSource());
		String s = "Action event detected."
				+ newline
				+ "    Event source: " + source.getText()
				+ " (an instance of " + getClassName(source) + ")";
		output.append(s + newline);
		output.setCaretPosition(output.getDocument().getLength());
		*/

		try {
			
			switch (e.getActionCommand()) {
	
				case "Reference Population Model...":
					newReferencePopulationHandler();
					break;
					
				case "Export...":
					export();
					break;
		
				case "Do Cross Validation...":
					SperecCrossValidator.main(null);
					break;
					
				/*case "Noise and duration validation...":
					NoiseValidation_OLD.main1();
					break;*/
		
				case "Attach validation results...":
					attachValidationResultsHandler();
					break;
					
				case "Show validation results...":
					showValidationResultsHandler();
					break;
					
				
				// From the third menu
					
				case "Split long sessions...":
					SplitSessions.main(null);
					break;
					
				case "Noise contamination...":
					NoiseContamination.main(null);
					break;
					
				case "Voice Activity Detection (VAD)...":
					Audio2vad.main(null);
					break;
					
				
				case "Feature extraction...":
					Voice2Fea.main(null);
					break;
					
			}
		}
		catch (Exception e1) {
			output.append(e1.getMessage());
			output.setCaretPosition(output.getDocument().getLength());
			e1.printStackTrace();
		}
		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		String s = "Item event detected."
				+ newline
				+ "    Event source: " + source.getText()
				+ " (an instance of " + getClassName(source) + ")"
				+ newline
				+ "    New state: "
				+ ((e.getStateChange() == ItemEvent.SELECTED) ?
						"selected":"unselected");
		output.append(s + newline);
		output.setCaretPosition(output.getDocument().getLength());
	}

	// Returns just the class name -- no package info.
	protected String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf(".");
		return classString.substring(dotIndex+1);
	}

	public Container createContentPane() {
		//Create the content-pane-to-be.
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);

		//Create a scrolled text area.
		output = new JTextArea(5, 30);
		output.setEditable(false);
		scrollPane = new JScrollPane(output);

		//Add the text area to the content pane.
		contentPane.add(scrollPane, BorderLayout.CENTER);

		return contentPane;
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("SPEREC LAB"); //("SPEREC - POPREF Model Builder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		SperecLab demo = new SperecLab();
		frame.setJMenuBar(demo.createMenuBar());
		frame.setContentPane(demo.createContentPane());

		//Display the window.
		frame.setSize(450, 260);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		
		Environment env = new Environment();
		MAIN_OUTPUT_FOLDER_PATH = env.getMainOutputFolderPath();
		
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	

	
	/*private static JFreeChart createLineChart() {

        Number[][] data = new Integer[][]
            {{new Integer(-3), new Integer(-2)},
             {new Integer(-1), new Integer(1)},
             {new Integer(2), new Integer(3)}};

        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("S",
                "C", data);

        return ChartFactory.createLineChart("Line Chart", "Domain", "Range",
                dataset);

    */
	
	
	private static void showValidationResultsHandler() throws Exception {

		SperecCrossValidator o = new SperecCrossValidator(); // No params
		o.output = output;
		o.showValidationResults(output);		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private static void export() throws Exception {
		
		String cfgString = null;
		ArrayList<String> allRequiredFiles = new ArrayList<String>();
		
		
		// Select the popref configuration file
		// Select the POPREF specs
		String popRefCfgPath = ChooseFile.get(MAIN_OUTPUT_FOLDER_PATH, "Choose the file of the reference population configuration", "(*.cfg)", "cfg");
		if ( (null==popRefCfgPath) || (popRefCfgPath.equals("")))
			return;

		// Remember that the popRefCfgPath is required
		//allRequiredFiles.add(popRefCfgPath);
		
		// Load the raw configuration file
		SPEREC_Loader_JVM sperec_loader = new SPEREC_Loader_JVM();
		InputStream popRefCfgIs = sperec_loader.getInputStream(popRefCfgPath);
		ConfigurationFile popRefCfg = ConfigurationFile.load(popRefCfgIs);

		// Dump the cfg content
		cfgString = popRefCfg.toString();
		System.out.println(cfgString);
		output.append("Loaded Reference Population configuration resource: " + popRefCfgPath + newline);
		output.append(cfgString + newline);
		output.setCaretPosition(output.getDocument().getLength());
		
		// Check
		// 1. the popref file
		String dir = new File(popRefCfgPath).getParent();
		String popRefFileName = popRefCfg.getItem("SPEREC",  "popRefFileName");
		String popRefFullPath = dir + File.separator + popRefFileName;
		
		// Remember that the popRefFullPath is required
		allRequiredFiles.add(popRefFullPath);

		// Load the popref model 		
		InputStream popRefIs = sperec_loader.getInputStream(popRefFullPath); // Can throw Exception
		POPREF_MODEL pop = POPREF_MODEL.readFromInputStream(popRefIs); //readFromFile(f);
				
		
		// Ricava le sperec specs
		SPEREC_Specs s = SPEREC_Specs.fromJsonString(popRefCfg.getItem("SPEREC",  "SperecSpecs"));
		
		
		if (s.SPEREC_Type.equals("SPEREC_UBM_IV_GPLDA")) {
			boolean ok = 	(s.UBM.NMIX==pop.ubm.getNumberOfComponents()) &&
							(s.TV.TVDIM==pop.tv.T.getRowDimension()) &&
							(s.LDA.LDADIM==pop.V_LDA_redux.getRowDimension()) &&
							(s.GPLDA.NPHI==pop.gplda.getPhi().getRowDimension());
			if (!ok) {
				String msg = "ERROR: Reference Population Models mismatch" + newline;
				output.append(msg);
				output.setCaretPosition(output.getDocument().getLength());
				return;
			}
		}
		else {
			String msg = "ERROR:UNSUPPORTED SPEREC TYPE: " + s.SPEREC_Type + newline;
			output.append(msg);
			output.setCaretPosition(output.getDocument().getLength());
			return;
		}
		
		// 2. the validation results as field
		String validationResultEfn = popRefCfg.getItem("SPEREC", "validationResultEfn");
		if ( (validationResultEfn==null) || (validationResultEfn.equals(""))) {
			String msg = "ERROR: Missing validation result information" + newline;
			output.append(msg);
			output.setCaretPosition(output.getDocument().getLength());
			return;
		}
		allRequiredFiles.add(validationResultEfn);
		
		// Import that file
		ValidationResults valRes = sperec_loader.loadValidationResults(validationResultEfn);
		
		// Now specify the exact paths of files
		String parentFolder = new File(validationResultEfn).getParentFile().getAbsolutePath();
		for (int i = 0; i<valRes.size(); i++) {
			ValidationResultGauss v = valRes.get(i);
			String errfile = v.ErrorRateFile;
			String errFileFullPath = searchDeep(parentFolder+File.separator+errfile, parentFolder);
			allRequiredFiles.add(errFileFullPath);			
		}
		

		// Select output folder
		String outFolder = ChooseFolder.get(popRefCfgPath, "CREATE A NEW FOLDER AND SELECT AS OUTPUT FOLDER");
		if ( (outFolder==null) || (outFolder.equals("")) )
			return;
		
		// Export
		popRefCfg.saveAs(outFolder, "popref.cfg");
		for (int i = 0; i<allRequiredFiles.size(); i++)
		{
			String origFilePath = allRequiredFiles.get(i);
			File origFi = new File(origFilePath);
			String filename = origFi.getName();
			String finalFilename = sperec_loader.filename2AndroidResourceName(filename);
			String finalOutFilePath = outFolder + File.separator + finalFilename;
			File destFi = new File(finalOutFilePath);
			Files.copy(origFi.toPath(), destFi.toPath());
		}
		String msg = "Export sucessfully done to folder: " + outFolder + newline;
		output.append(msg);
		output.setCaretPosition(output.getDocument().getLength());
		
	}
	
	
	private static String searchDeep(String fullpath, String parentFolder) {
		
		String errFileFullPath = null;
				
		File f = new File(fullpath);
		if (f.exists()) {
			errFileFullPath = fullpath;
			return errFileFullPath;
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
			errFileFullPath = searchDeep(newFullPath, newPath);
			if (null!=errFileFullPath)
				break;
		}
		
		return errFileFullPath;
		
	}
	
	/**
	 * Attach a validation results file to a popref configuration file
	 * @throws Exception
	 */
	private static void attachValidationResultsHandler() throws Exception {
		SperecCrossValidator it = new SperecCrossValidator();
		it.output = output;
		it.attachValidationResults(output);		
	}
	
	
	/**
	 * Build a new reference population starting from cfg
	 * @param cfg
	 * @throws Exception
	 */
	private static void newReferencePopulationHandler() throws Exception {
		
		NewPopRef it = new NewPopRef();
		it.output = output;
		it.work();
	}

}

//3. the validation results' files
		/* OLD
		EmpiricalFn EFN = EmpiricalFn.fromJsonString(validationResultEfn);
		String [] yNames = EFN.getYNames();
		String baseFolder = "";
		for (int i = 0; i<yNames.length; i++) {
			String yname = yNames[i];
			ValidationResult [] valResults = EFN.getAllYValues(yname);

			for (int ii = 0; ii < valResults.length; ii++) {
				ValidationResult v = valResults[ii];
				String errFilePath = v.ErrorRateFile;
				String errFilename = new File(errFilePath).getName();

				InputStream errFileIs = null;
				try {
					errFileIs = sperec_loader.getInputStream(errFilePath);
				} catch (Exception e) {
					if (!baseFolder.equals("")) {
						errFilePath = baseFolder + File.separator + errFilename;
					}
				}
				try {
					errFileIs = sperec_loader.getInputStream(errFilePath);
				} catch (Exception e) {
					String  fileExt = FilenameUtils.getExtension(errFilePath);
					errFilePath = ChooseFile.get(dir, "File not found: " + errFilename + " Please find it.", fileExt);
					if ((errFilePath==null) || errFilePath.equals(""))
						return;
				}

				baseFolder = new File(errFilePath).getParent();
				allRequiredFiles.add(errFilePath);

			}
		}
		*/	