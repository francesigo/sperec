package myMath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import myDSP.MyComplexArrayFloat;

/**
 * Sparse matrix of complex numbers
 * @author FS
 *
 */
public class MySparseFloat {

	private int m; // Number of rows
	
	public int getNumRows() {return m;}
	
	private int n; // number of columns
	public int getNumCols() {return n;}
	
	private int rows[]; // Row indexes of elements
	private int cols[]; // Column indexes of elements
	MyComplexArrayFloat vals; // The elements

	private int p; // Number of elements

	/**
	 * Prepare room for a sparse matrix with size m x n and p "explicit" elements
	 * @param m
	 * @param n
	 * @param p
	 */
	public MySparseFloat(int m, int n, int p) {
		this.m = m;
		this.n= n;
		this.p = p;
		rows = new int[p];
		cols = new int[p];
		vals = new MyComplexArrayFloat(p);
	}

	/**
	 * 
	 * @param index
	 * @param ro
	 * @param co
	 * @param re
	 * @param im
	 */
	public void set(int index, int ro, int co, float re, float im) {
		rows[index] = ro;
		cols[index] = co;
		vals.set(index, re, im);
	}
	
	/**
	 * Transpose the matrix
	 */
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
	 * Multiply the current sparse float complex array by a float complex array
	 * @param x the float complex array to be multiplied by the current sparse float complex array
	 * @return the resulting float complex array
	 * @throws Exception 
	 */
	public MyComplexArrayFloat timesComplex(MyComplexArrayFloat x) throws Exception {

		if (x.size() != n)
			throw new Exception("timesComplex: length mismatch");

		MyComplexArrayFloat y = new MyComplexArrayFloat(m);
		y.fill(0.0f);

		for (int i=0; i<m; i++ ) // Current row of the sparse ==> current index in the resulting array
		{
			float acc_re = 0.0f;
			float acc_im = 0.0f;
			for (int ip=0; ip<p; ip++) // For each element of the sparse
			{
				if (rows[ip]==i) // if the row index is equal to the current row...
				{
					int k = cols[ip];
					double re = vals.getRealFloat(ip)*x.getRealFloat(k) - vals.getImagFloat(ip)*x.getImagFloat(k);
					double im = vals.getImagFloat(ip)*x.getRealFloat(k) + vals.getRealFloat(ip)*x.getImagFloat(k);

					acc_re += re;
					acc_im += im;
				}
			}
			y.set(i, acc_re, acc_im);

		}
		return y;		
	}
	
	/**
	 * Debug function
	 * @param B
	 */
	public void compareStrict(MySparseFloat B) {
		MyMath.compare_vectors("Sparse Matrix comparison: rows: ", this.rows, B.rows);
		MyMath.compare_vectors("Sparse Matrix comparison: cols: ", this.cols, B.cols);
		MyMath.compare_vectors("Sparse Matrix comparison: reals: ", MyMath.todouble(this.vals.getRealFloat()),  MyMath.todouble(B.vals.getRealFloat()));
		MyMath.compare_vectors("Sparse Matrix comparison: imag: ",  MyMath.todouble(this.vals.getImagFloat()),  MyMath.todouble(B.vals.getImagFloat()));
	}

	//====================================== I/O ===================================
	public static MySparseFloat fromFile(String filepath) throws Exception {
		
		if (filepath.endsWith(".txt"))
				return fromFileTxt(filepath);
		//else if (filepath.endsWith(".dat"))
		//	return fromFileDat(filepath);
		else
			throw new Exception("Unsupported file type");
	}
	
	
	/**
	 * 
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public static MySparseFloat fromFileTxt(String filepath) throws IOException {

		File file = new File(filepath);
		// Assuming a txt file

		MySparseFloat M = null;

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

				M = new MySparseFloat(m, n, p);
			}
			else
			{
				int ro = (int)Float.parseFloat(parts[0]);
				int co = (int)Float.parseFloat(parts[1]);
				float re = Float.parseFloat(parts[2]);
				float im = Float.parseFloat(parts[3]);
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
