package sperec_common;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;

import com.google.gson.Gson;


public class EmpiricalFn {
		
	private final int [] _o_ = {0};
	
	/**
	 * Names of independent variables
	 */
	String [] xNames = null; //ArrayList<String> xVarNames;
	double [][] xValues = null; // Arrays (one item for each independent variable) of double arrays representing the values of the corresponding independent variable
	
	int nx = 0;
	int ny = 0;
	int [] xDimensions = null;
	
	/**
	 * Names of dependent variables
	 */
	String [] yNames = null; // ArrayList<String> yVarNames;
	NDimensionalArray [] yy = null;
	
	
	public double[][] geXValues() {
		return xValues;
	}
	
	/**
	 * Special case: a single value
	 * @param value
	 */
	public EmpiricalFn(ValidationResultGauss value) {
		this.nx = 1;
		this.ny = 1;
		
		//this.cla = cla;
		int arraySize = 1;
		yy = (NDimensionalArray[]) Array.newInstance(NDimensionalArray.class, arraySize);
		//this.yy[0].set(value, _o_);
	}
	
	public EmpiricalFn(String [] xNames, double [][] xValues, String [] yNames) throws Exception {
		
		if ((xNames==null) || (yNames==null) || (xNames.length!= xValues.length))
			throw new Exception("Incorrect specifications of independent variables");
		
		this.nx = xNames.length;
		this.ny = yNames.length;
		
		this.xNames = xNames.clone();
		this.xValues = xValues.clone();
		
		this.yNames = yNames.clone();
				
		int [] xDimensions = new int[this.nx];
		for (int i=0; i<nx; i++)
			xDimensions[i] = xValues[i].length;
		
		int arraySize = 1;
		
		yy = (NDimensionalArray[]) Array.newInstance(NDimensionalArray.class, arraySize);
		for (int iy=0; iy<arraySize; iy++){
			yy[iy] = new NDimensionalArray(xDimensions);
		}

	}
	

	public String [] getXNames() {
		return xNames;
	}

	public String [] getYNames() {
		return yNames;
	}
	
	public void dump(String title) {
		System.out.println(title);
		System.out.println("xNames: " + dumpStrings(xNames));
		for (int i=0; i<xNames.length; i++)
		{
			System.out.println("\t\""+xNames[i] + "\":");
			System.out.print("\t\t");
			dumpValues(xValues[i]);
		}
		
	}
	
	private void dumpValues(double [] S) {
		int n = S.length;
		System.out.print("[");
		for (int i = 0; i<n-1; i++)
			System.out.print(S[i] + ", ");
		System.out.println( S[n-1] + "];");
	}
	
	private String dumpStrings(String [] S) {
		int n = S.length;
		String R = "";
		R = R+"{";
		for (int i = 0; i<n-1; i++)
			R = R + " \"" + S[i] + "\",";
		R = R + " \"" + S[n-1] + "\"}";
		return R;
	}
	
	
	public ValidationResultGauss [] getAllYValues(String yName) throws Exception {
	
		int yInd = name2Ind(yNames, yName);
		if (yInd<0)
			throw new Exception("Incorrect specifications of dependent variable name");
		
		NDimensionalArray y = yy[yInd];
		return y.getArray();	
	}
	
	/**
	 * Constructor
	 * @param xNames
	 * @param xDimensions
	 * @param yNames
	 * @throws Exception
	 */
	/*public EmpiricalFn(String [] xNames, int [] xDimensions, String [] yNames) throws Exception {
		
		if ((xNames==null) || (yNames==null) || (xNames.length!= xDimensions.length))
			throw new Exception("Incorrect specifications of independent variables");
		
		this.nx = xNames.length;
		this.ny = yNames.length;
				
		this.xNames = xNames.clone();
		this.xValues = new double[nx][];
		
		this.yNames = yNames.clone();
		
		this.xDimensions = xDimensions.clone();
		
		this.yy = new NDimensionalArray[this.ny];
		for (int i = 0; i<this.yy.length; i++)
			this.yy[i] = new NDimensionalArray(xDimensions);
			
	}*/

	
	
	
	/*
	 * Set the values of independent variable whose name is "xName"
	 */
	/*public void setXValues(String xName, double [] values) throws Exception {
		
		int i = name2Ind(xNames, xName);

		if (values.length != xDimensions[i])
			throw new Exception("Incorrect coordinate values");
		
		xValues[i] = values.clone();
		Arrays.sort(xValues[i]);
	}*/	

	/**
	 * E.g. for TESTDUR=4seconds, and SNR=10dB, set the file of False Acceptance Rates: set("FAR_file", "somename.bin", {4, 10}) 
	 * @param yName
	 * @param yValue
	 * @param coords
	 * @throws Exception
	 */
	public void set(String yName, ValidationResultGauss yValue, double ... coords) throws Exception {
		
		int yInd = name2Ind(yNames, yName);
		if (yInd<0)
			throw new Exception("Incorrect specifications of dependent variable name");
		
		int [] xInd = coordsToInd(coords);
		
		NDimensionalArray y = yy[yInd];
		y.set(yValue, xInd);
	}
	
	/**
	 * 
	 * @param yName
	 * @param coords
	 * @return
	 * @throws Exception
	 */
	public ValidationResultGauss get(String yName, double ... coords) throws Exception {

		int [] xInd = coordsToInd(coords);
		int yInd = name2Ind(yNames, yName);
		return yy[yInd].get(xInd);
	}


	public AuthenticationResult compute(double score, double ... coords) throws Exception {

		AuthenticationResult res = null;

		// Missing the yNname by design, then check
		if (yNames.length>1)
			throw new Exception("Missing the y name");

		String yname = yNames[0];

		if (coords.length>1)
			throw new Exception("Only single coordinate supported at the moment");

		// Continue with the single coordinate
		double c = coords[0];
		double [] xvalues = xValues[0];
		int numc = xvalues.length;
		if (c <= xvalues[0]) {
			ValidationResultGauss vr = get(yname, xvalues[0]);
			res = vr.compute(score);
		} else if (c >= xvalues[numc-1]) {
			ValidationResultGauss vr = get(yname, xvalues[numc-1]);
			res = vr.compute(score);
		} else {
			int i2 = 0;
			for (i2 =0; i2<numc; i2++) {
				if (c <= xvalues[i2])
					break;
			}
			int i1 = i2-1;
			double c1 = xvalues[i1];
			double c2 = xvalues[i2];

			ValidationResultGauss vr1 = get(yname, c1);
			ValidationResultGauss vr2 = get(yname, c2);

			AuthenticationResult res1 = vr1.compute(score);
			AuthenticationResult res2 = vr2.compute(score);

			double w1 = (c2-c)/(c2-c1);
			double w2 = (c-c1)/(c2-c1);

			res.score = score;
			res.scoreThreshold = res1.scoreThreshold * w1 + res2.scoreThreshold * w2;
			res.EER_100 = Math.min(res1.EER_100 * w1 + res1.EER_100 * w2, 100.0);
			res.FalseNegative_100 = Math.min(res1.FalseNegative_100 * w1 + res2.FalseNegative_100 * w2, 100.0);
			res.FalsePositive_100 = Math.min(res1.FalsePositive_100 * w1 + res2.FalsePositive_100 * w2, 100.0);
		}


		return res;
	}
	
	/**
	 * Special case
	 * @return
	 * @throws Exception 
	 */
	public Object get() throws Exception {
		if ((nx !=1 ) || (ny!=1))
			throw new Exception("Incorrect specifications of independent variables");
		
		return yy[0].get(_o_);
	}



	
	public String toJsonString() {
		Gson gson = new Gson();
		String json = gson.toJson(this, EmpiricalFn.class);
		return json;
	}
	
	
	static public EmpiricalFn fromJsonString(String json) {
		Gson gson = new Gson();
		EmpiricalFn v = (EmpiricalFn) gson.fromJson(json, EmpiricalFn.class);
		return v;
	}
	
	public static EmpiricalFn load(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return EmpiricalFn.load(br);
	}
	public static EmpiricalFn load(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		return EmpiricalFn.load(br);
	}
	public static EmpiricalFn load(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		return EmpiricalFn.load(br);
	}
	private static EmpiricalFn load(BufferedReader br) throws IOException {
		ConfigurationFile C = new ConfigurationFile();
		String line = br.readLine();
		br.close();
		return fromJsonString(line);
	}
	
	/**
	 * Save as json text file, in the File f
	 * @param f
	 * @throws IOException
	 */
	public void saveAs(File f) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(f));

		String json = toJsonString();
		writer.write(json+"\n");
		writer.flush();
		writer.close();
	}
	
	/**
	 * 
	 * @param destFolder
	 * @param filename
	 * @throws IOException
	 */
	public void saveAs(String destFolder, String filename) throws IOException {
		 String fpath = destFolder + File.separator + filename;
		 
		 File f = new File(fpath);
		 saveAs(f);
	}
	
	
	// ------------------ PRIVATE
	
	private int [] coordsToInd(double [] coords) throws Exception {

		if ((coords==null) || (coords.length != nx))
			throw new Exception("Incorrect coordinate values");

		int [] xInd = new int [nx];
		
		for (int ic=0; ic<nx; ic++) {
			
			double xVal[] = xValues[ic]; // The allowed values for that coordinate
			double c = coords[ic]; // The coordinate value to search for
			
			int ind = xVal.length-1;
			while ( (ind>=0) && (xVal[ind] != c))
				ind--;
			
			if (ind<0)
				throw new Exception("Incorrect value (" + c + ") of coordinate " + xNames[ic]);
			
			xInd[ic] = ind;
		}

		return xInd;
	}
	
	/**
	 * Map xName to the index whitin xNames
	 * @param xName
	 * @return
	 */
	private int name2Ind(String [] names, String name) {
		
		for (int i=0; i<names.length; i++)
			if (names[i].equals(name))
				return i;
		
		return -1;
	}
}
