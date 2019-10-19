package sperec_common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import myMath.MyMatrix;

/**
 * A class with Input/Output methods that integrates the different components of the model
 * @author FS
 *
 */
public class POPREF_MODEL {
	
	public MixtureModel ubm = null;
	public TVSpace_Models tv = null;
	public MyMatrix V_LDA_redux = null;
	public GPLDA_Models gplda_models = null;
	public GPLDA gplda = null;
	
	
	
	
	/***************************************************************************
	 * I/O
	 * ************************************************************************/
	
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static POPREF_MODEL readFromInputStream(InputStream is) throws IOException {
		POPREF_MODEL pm = new POPREF_MODEL();
		pm.ubm = Mixture_MSR.readFromInputStream(is);
		pm.tv = TVSpace_Models.readFromInputStream(is);
		pm.V_LDA_redux = MyMatrix.readFromInputStream(is);
		pm.gplda = GPLDA.readFromInputStream(is);
		return pm;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static POPREF_MODEL readFromFile(File file) throws IOException {
		InputStream is = null;
		if (file == null)
			is = System.in;
		else
			is = new FileInputStream(file);
				
		POPREF_MODEL pm = POPREF_MODEL.readFromInputStream(is);
				
		is.close();
		return pm;
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		OutputStream os = (file == null ? System.out : new FileOutputStream(file));
		write(os);
		os.flush();
		os.close();
	}

	/**
	 *
	 * @param os
	 * @throws IOException
	 */
	private void write(OutputStream os) throws IOException {
		ubm.writeToOutputStream(os);
		tv.writeToOutputStream(os);
		V_LDA_redux.write(os);
		gplda_models.writeToOutputStream(os);
	}
}
