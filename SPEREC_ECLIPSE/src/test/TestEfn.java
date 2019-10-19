package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import sperec_common.EmpiricalFn;
import sperec_common.MyMatrix;
import sperec_common.ValidationResultGauss;

public class TestEfn {

	static public void main(String[] args) throws Exception {
		
		String inputFileFullPath1 = "C:\\Users\\FS\\Google Drive\\_DOTTORATO\\Sperec_tmp_files\\f_2018_04_23_22_44_16_765.efn";
		String inputFileFullPath2 = "C:\\Users\\FS\\Google Drive\\_DOTTORATO\\Sperec_tmp_files\\f_2018_05_18_13_25_25_826.efn";
		String outputFolder = "C:\\Users\\FS\\Google Drive\\_DOTTORATO\\Sperec_tmp_files";
		String outputFileName = "provaMerge.efn";

		
		EmpiricalFn efn1 = EmpiricalFn.load(inputFileFullPath1);
		efn1.dump("efn1");
		EmpiricalFn efn2 = EmpiricalFn.load(inputFileFullPath2);
		efn2.dump("efn2");
		
		// Prova ad unirle, è solo un esempio
		// Devo controllare i requisiti siano le stesse
		boolean by = Arrays.equals(efn1.getYNames(), efn2.getYNames());
		if (!by) {
			System.out.println("Fallito requisito su yNames: " + by);
			return;
		}
		boolean bxnames = Arrays.equals(efn1.getXNames(), efn2.getXNames());
		if (!by) {
			System.out.println("Fallito requisito su xNames: " + bxnames);
			return;
		}
		// Forse il seguente si può rilassare, per ora lo tolgo
		boolean bXvalues = Arrays.deepEquals(efn1.geXValues(), efn2.geXValues());
		if (!bXvalues) {
			System.out.println("Fallito requisito su xValues: " + bXvalues);
			return;
		}
		
		String [] oldYNames = efn1.getYNames();
		String [] oldXnames = efn1.getXNames();
		String newXName = "SNR";
		String [] finalXNames = prependString(newXName, oldXnames);
		
		double [][] oldXValues = efn1.geXValues();
		double [] newXValues = {6.0, 15.0}; // in numero pari alle enf caricate

		double [][] finalXValues = addXValues(oldXValues, newXValues);
		
		EmpiricalFn EFN = null;
		try {
			EFN = new EmpiricalFn( finalXNames, finalXValues, oldYNames);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		// Ora setto i valori
		//EFN.set("ValRes",  valRes, testSessionDurationSec_v[iDur]);
		//	public void set(String yName, ValidationResult yValue, double ... coords) throws Exception {
		
		ArrayList<EmpiricalFn> AEFN = new ArrayList<EmpiricalFn>();
		AEFN.add(efn1);
		AEFN.add(efn2);
		
		int nXnames = AEFN.size(); // il numero di livelli della nuova dimensione
		for (int xdim = o; xdim<nXnames; xdim++)
		{
			
		}
		EFN.set("ValRes", yValue, coords);
		EFN.saveAs(outputFolder, outputFileName);
		
		
		// Ora provo a caricarla
		String inputFileFullPath_final = outputFolder + File.separator + outputFileName;
		EmpiricalFn efn3 = EmpiricalFn.load(inputFileFullPath2);
	}
	
	
	/*
	 * 
	 */
	static private String [] prependString(String s, String [] a) {
		ArrayList<String> lista1 = new ArrayList<String>();
		lista1.add(s);
		ArrayList<String> lista2 = new ArrayList<String>( Arrays.asList( a ));
		lista1.addAll(lista2);
		return lista1.toArray(new String[0]);
		
	}
	
	
	static private double [][] addXValues(double [][] oldXValues, double [] newXValues) throws Exception {
				
		int numRighe = oldXValues.length;	

		double [][] finalXValues = new double[numRighe+1][];
		for (int i = 0; i<numRighe; i++)
			finalXValues[i] = oldXValues[i];

		finalXValues[numRighe] = newXValues;
		
		return finalXValues;
	}
}
