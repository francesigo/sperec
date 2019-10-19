package myMath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import myDSP.MyComplexArrayDouble;
import myDSP.MyComplexArrayFloat;
import myIO.IOClass;

/**
 * Sparse matrix of complex numbers
 * @author FS
 *
 */
public class MySparseDouble {

	private int m; // Number of rows
	
	public int getNumRows() {return m;}
	
	private int n; // number of columns
	public int getNumCols() {return n;}
	
	private int rows[]; // Row indexes of elements
	private int cols[]; // Column indexes of elements
	MyComplexArrayDouble vals; // The elements

	private int p; // Number of elements

	public MySparseDouble(int m, int n, int p) {
		this.m = m;
		this.n= n;
		this.p = p;
		rows = new int[p];
		cols = new int[p];
		vals = new MyComplexArrayDouble(p);
	}
	
	/**
	 * TODO input check
	 * @param numRows
	 * @param numCols
	 * @param numElems
	 * @param rows2
	 * @param cols2
	 * @param re
	 * @param im
	 */
	public MySparseDouble(int numRows, int numCols, int numElems, int[] rows2, int[] cols2, double[] re, double[] im) {
		this.m = numRows;
		this.n = numCols;
		this.p = numElems;
		this.rows = rows2;
		this.cols = cols2;
		this.vals = new MyComplexArrayDouble(re, im);
	}



	

	public void set(int index, int ro, int co, double re, double im) {
		rows[index] = ro;
		cols[index] = co;
		vals.set(index, re, im);
	}
	public void transposeSelf() {
		
		// Swap indexes
		int [] temp = rows;
		rows = cols;
		cols = temp;
		
		// Swap size
		int temp2 = m;
		m = n;
		n = temp2;
	}


	/**
	 * Sparse complex matrix times complex array v
	 * @param x
	 * @return
	 * @throws Exception
	 */
	public MyComplexArrayDouble timesComplex(MyComplexArrayDouble x) throws Exception {

		if (x.size() != n)
			throw new Exception("timesComplex: length mismatch");

		MyComplexArrayDouble y = new MyComplexArrayDouble(m);
		y.fill(0.0);

		for (int i=0; i<m; i++ ) // Current row of the sparse ==> current index in the resulting array
		{
			double acc_re = 0.0;
			double acc_im = 0.0;
			for (int ip=0; ip<p; ip++) // For each element of the sparse
			{
				if (rows[ip]==i) // if the row index is equal to the current row...
				{
					int k = cols[ip];
					double re = vals.getReal(ip)*x.getReal(k) - vals.getImag(ip)*x.getImag(k);
					double im = vals.getImag(ip)*x.getReal(k) + vals.getReal(ip)*x.getImag(k);

					acc_re += re;
					acc_im += im;
				}
			}
			y.set(i, acc_re, acc_im);

		}
		return y;		
	}

	//====================================== I/O ===================================
	public static MySparseDouble fromFile(String filepath, boolean fixIndexing) throws Exception {
		
		if (filepath.endsWith(".txt"))
				return fromFileTxt(filepath, fixIndexing);
		else if (filepath.endsWith(".dat"))
			return fromFileDat(filepath, fixIndexing);
		else
			throw new Exception("Unsupported file type");
	}
	
	/**
	 * Debug function
	 * @param B
	 */
	public void compareStrict(MySparseDouble B, double th) {
		MyMath.compare_vectors("Sparse Matrix comparison: rows: ", this.rows, B.rows);
		MyMath.compare_vectors("Sparse Matrix comparison: cols: ", this.cols, B.cols);
		MyMath.compare_vectors("Sparse Matrix comparison: reals: ", this.vals.getReal(),  B.vals.getReal(), th);
		MyMath.compare_vectors("Sparse Matrix comparison: imag: ",  this.vals.getImag(),  B.vals.getImag(), th);
	}
	
	/**
	 * 
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public static MySparseDouble fromFileDat(String filepath, boolean fixIndexing) throws IOException {
	
		File file = new File(filepath);
		FileInputStream in = new FileInputStream(file);
		
		int numRows = IOClass.readInt(in, ByteOrder.nativeOrder());
		int numCols = IOClass.readInt(in, ByteOrder.nativeOrder());
		int numElems = IOClass.readInt(in, ByteOrder.nativeOrder());
		
		MySparseDouble M = new MySparseDouble(numRows, numCols, numElems);
		for (int i=0; i<numElems; i++)
		{
			int ro = IOClass.readInt(in, ByteOrder.nativeOrder());
			if (fixIndexing)
				ro--;
			int co = IOClass.readInt(in, ByteOrder.nativeOrder());
			if (fixIndexing)
				co--;
			double re = IOClass.readDouble(in,  ByteOrder.nativeOrder());
			double im = IOClass.readDouble(in,  ByteOrder.nativeOrder());
			M.set(i, ro, co, re, im);
		}
		
		in.close();
		return M;
	}
	
	public static MySparseDouble fromFileTxt(String filepath, boolean fixIndexing) throws IOException {

		File file = new File(filepath);
		// Assuming a txt file

		MySparseDouble M = null;

		int i =-1;
		int p = 0;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null)
		{
			// process the line.
			String[] parts = line.split(",");
			if (i==-1)
			{// First line
				int m = Integer.parseInt(parts[0]);
				int n = Integer.parseInt(parts[1]);
				p = Integer.parseInt(parts[2]);

				M = new MySparseDouble(m, n, p);
			}
			else
			{
				int ro = (int)Double.parseDouble(parts[0]);
				if (fixIndexing)
					ro--;
				int co = (int)Double.parseDouble(parts[1]);
				if (fixIndexing)
					co--;
				double re = Double.parseDouble(parts[2]);
				double im = Double.parseDouble(parts[3]);
				M.set(i, ro, co, re, im);
			}
			i++;
			if (i==p)
				break; //Exit anyway
		}

		br.close();
		return M;
	}
}
