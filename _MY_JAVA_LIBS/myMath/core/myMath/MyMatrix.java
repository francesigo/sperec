package myMath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Jama.Matrix;
import myIO.IOClass;
import myMath.MySort.SortDir;
import myMath.MyMath;


/**
 * MyMatrix aims to represent a collection of equal-sized vectors, such as "observed" feature vectors, i-vectors, etc.
 * Moreover, this class aims to extend the basic capabilities of JAMA Matrix class
 * @author FS
 *
 */
public class MyMatrix extends Matrix  { //implements Serializable { // 30 ottobre 2018: ho aggiunto Serializable))
	//implements IUReader
	/**
	 * This play a key role in telling if feature vectors, i-vectors, etc. are row vectors or column vectors in this matrix.
	 * 0 = UNSPECIFIED (default)
	 * 1 = ROW: the vectors are stored as row vectors. Therefore, the number of columns of the matrix is the vectors' dimensionality, while the number of rows is the number of available
	 * 2 = COLUMN: the vectors are stored as column. Therefore, the number of rows is the vectors' dimensionality, while the number of columns is the number of available vectors
	 */
	
	public enum VectorDim { UNSPECIFIED, ROW_VECTORS, COLUMN_VECTORS}
	
	private VectorDim vector_dim = VectorDim.UNSPECIFIED; //int vector_dim = 0; // The user application is in charge to specify this field, when instantiating this class
	
	public void setVectorDim(VectorDim vdim) {
		vector_dim = vdim;
	}
	
	public VectorDim getVectorDim() {
		return vector_dim;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	// ------------------------------- CONSTRUCTORS ETC.
	/**
	 * 
	 * @param dT
	 */
	public MyMatrix(double[][] dT) {
		this(dT, VectorDim.UNSPECIFIED); // vector_dim = 0 (unspecified)
	}
		
	/**
	 * 
	 * @param dT
	 */
	public MyMatrix(double[][] dT, VectorDim vector_dim) {
		super(dT);
		this.vector_dim = vector_dim;
	}
	
	/**
	 * 
	 * @param M
	 */
	public MyMatrix(Matrix M, VectorDim vector_dim) {
		super(M.getArray());
		this.vector_dim = vector_dim;
	}
	
	public MyMatrix(Matrix M) {
		super(M.getArray());
	}
	
	
	/**
	 * 
	 * @param m
	 * @param n
	 * @param s
	 */
	public MyMatrix(int m, int n, double s) {
		super(m, n, s);
	}
	
	public MyMatrix(int m, int n, double s, VectorDim vector_dim) {
		this(m, n, s);
		this.setVectorDim(vector_dim);
	}
	
	public MyMatrix(int m, int n) {
		super(m, n);
	}

	public static MyMatrix fromArrayListOfArrayListOfDouble(ArrayList<ArrayList<Double>> X) {
		double [][] y = new double[X.size()][];
		for (int i=0; i<y.length; i++)
		{
			ArrayList<Double> Xi = X.get(i);
			y[i] = new double[Xi.size()];
			double [] t = y[i];
			for (int j=0; j<t.length; j++)
				t[j] = Xi.get(j);
		}
		return new MyMatrix(y);
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public static MyMatrix fromRowVector(double [] v) {
		double [][] dA = new double[1][];
		dA[0] = v;
		return new MyMatrix(dA, VectorDim.ROW_VECTORS); //1 = the vector in the matrix is a row vector
	}
	
	/**
	 * 
	 * @param al
	 * @param vectorDim
	 * @return
	 */
	public static MyMatrix fromArrayListOfFloatArray(ArrayList<float[]> al, VectorDim vectorDim) {
		
		int n = al.size();
		
		if (n==0) {
			return null;
		}
		
		int m = al.get(0).length; // feaSpecs.iAmountOfCepstrumCoef;
		double [][] dA = new double[n][m];
		
		for (int i=0; i<n; i++)
		{	
			float [] samples = al.get(i);
			
			if (m!=samples.length)
				throw new IllegalArgumentException("MyMatrix.fromArrayList: row vectors dimensions  must agree.");
			
			for (int j=0; j<m; j++)
				dA[i][j] = (double)samples[j];
		}
		return new MyMatrix(dA, vectorDim);
	}
	
	/**
	 * 
	 * @param al
	 * @param vectorDim
	 * @return
	 */
	public static MyMatrix fromArrayListOfDoubleArray(ArrayList<double[]> al, VectorDim vectorDim) {
		int n = al.size();
		if (n==0) {
			return null;
		}
		int m = al.get(0).length; // feaSpecs.iAmountOfCepstrumCoef;
		double [][] dA = new double[n][m];
		
		for (int i=0; i<n; i++) {
			
			double [] samples = al.get(i);
			if (m!=samples.length) {
				throw new IllegalArgumentException("MyMatrix.fromArrayList: row vectors dimensions  must agree.");
			}
			
			for (int j=0; j<m; j++)
				dA[i][j] = samples[j];
		}
		return new MyMatrix(dA, vectorDim);
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public static MyMatrix fromColumnVector(double [] v) {
		
		double [][] dA = new double[v.length][1];
		for (int r=0; r<v.length; r++)
			dA[r][0] = v[r];
		
		return new MyMatrix(dA, VectorDim.COLUMN_VECTORS); //1 = the vector in the matrix is a column vector
	}

	

	/**
	 * Create a matrix given the values their coordinates
	 * @param rows
	 * @param cols
	 * @param vals
	 * @return
	 */
	public static MyMatrix fromSparse(int [] rows, int [] cols, double [] vals) {
		
		if (!(rows.length == cols.length) && (rows.length == vals.length)) 
			throw new IllegalArgumentException("fromSparse: length mismatch.");
		
		int numRows = MyMath.max(rows)+1;
		int numCols = MyMath.max(cols)+1;
		double [][] a = new double[numRows][numCols];
		for (int i=0; i<vals.length; i++)
			a[rows[i]][cols[i]] = vals[i];
		
		return new MyMatrix(a);
		
	}
	/**
	 * 
	 * @return
	 */
	private VectorDim toggle_vector_dim() {
		VectorDim new_vector_dim = (vector_dim==VectorDim.UNSPECIFIED) ? VectorDim.UNSPECIFIED : ( (vector_dim==VectorDim.ROW_VECTORS)? VectorDim.COLUMN_VECTORS: VectorDim.ROW_VECTORS);
		return new_vector_dim;
	}
	
	/**
	 * 
	 */
	public MyMatrix transpose() {
		VectorDim new_vector_dim = toggle_vector_dim();
		return new MyMatrix(super.transpose(), new_vector_dim);
	}
	
	/**
	 * /**
	 * Wrap the super.solve method and set vector_dim.
	 * The super.solve method is based on LUDecomposition (when the Matrix is square) and QRDecomposition for rectangluar matrix.
	 * The method LUDecomposition.solve can throw IllegalArgumentException and RuntimeException. RuntimeException is thrown when the LUDecomposition is singular.
	 * Here, this is outlined in the method declaration.
	 * @param B
	 * @return
	 * @throws Exception
	 */
	public MyMatrix solve(MyMatrix B) throws Exception {
		
		VectorDim new_vector_dim = VectorDim.COLUMN_VECTORS; // A.solve(B) implies is used to solve AX=B, i.e. Ax=b, so x is supposed to be a column vector, and X a matrix of column vectors
		Matrix m = null;
		try {
			m = super.solve(B);
		}
		catch (Exception e) {
			System.out.println("MyMatrix.solve: Excepiton: " + e.getMessage());
			throw e;
		}
		
		return new MyMatrix(m, new_vector_dim); 
	}
	
	/**
	 * 
	 */
	public MyMatrix times(double s) {
		return new MyMatrix(super.times(s), this.vector_dim);
	}
	
	/** Linear algebraic matrix multiplication, A * B
	   @param B    another matrix
	   @return     Matrix product, A * B
	   @exception  IllegalArgumentException Matrix inner dimensions must agree.
	   */
	public MyMatrix times(Matrix B) {
		VectorDim new_vector_dim = VectorDim.UNSPECIFIED; // Actually, I don not know. Place a breakpoint if not sure
		
		// OPZIONE1
		//MyMatrix M = null;
		//try {
		//	 M = new MyMatrix(super.times(B), new_vector_dim);
		//}
		//catch (Exception e) {
		//	throw e;
		//}
		//return M;
		
		// OPZIONE 2
		return new MyMatrix(super.times(B), new_vector_dim);	
	}

	public MyMatrix copy() {
		return new MyMatrix(super.copy(), this.vector_dim);
	}
	
	/** Element-by-element multiplication, C = A.*B
	   @param B    another matrix
	   @return     A.*B
	   */
	public MyMatrix arrayTimes(Matrix B) {
		return new MyMatrix(super.arrayTimes(B), this.vector_dim);
	}
	
	public MyMatrix getMatrix(int [] arg0, int arg1, int arg2) {
		return new MyMatrix(super.getMatrix(arg0,  arg1, arg2), this.vector_dim);
	}
	
	public MyMatrix getMatrix(int arg0, int arg1, int arg2, int arg3) {
		return new MyMatrix(super.getMatrix(arg0,  arg1, arg2, arg3), this.vector_dim);
	}
	
	public MyMatrix getMatrix(int arg0, int arg1, int [] arg2) {
		return new MyMatrix(super.getMatrix(arg0,  arg1, arg2), this.vector_dim);
	}
	
	
	/**
	 * Linear indexing
	 * @param idx
	 * @return
	 */
	public double get(int idx) {
		int m = this.getRowDimension();
		int c = idx / m; //Math.floorDiv(idx, m); Math.floorDiv is not supportd by API 19
		int r = idx - c*m;
		return get(r, c);
	}
	
	public MyMatrix plus(Matrix B) {
		return new MyMatrix(super.plus(B), this.vector_dim);
	}
	public MyMatrix minus(Matrix B) {
		return new MyMatrix(super.minus(B), this.vector_dim);
	}
	public MyMatrix minus(double k) {
		MyMatrix K = new MyMatrix(this.getRowDimension(), this.getColumnDimension(), k);
		return minus(K);
	}
	public MyMatrix inverse() {
		VectorDim new_vector_dim = toggle_vector_dim();
		return new MyMatrix(super.inverse(), new_vector_dim);
	}
	public static MyMatrix identity(int a, int b) {
		return new MyMatrix(Matrix.identity(a,b), VectorDim.UNSPECIFIED);
	}
	public void setMatrix(int arg0, int arg1, int arg2, int arg3, MyMatrix M) {
		super.setMatrix(arg0, arg1, arg2, arg3, M);
	}
	
	
	public double numel() {
		return this.getRowDimension()*this.getColumnDimension();
	}

	/**
	 * 
	 * @param m: the new row dimension
	 * @param n: the new column dimension
	 * @return
	 */
	public MyMatrix reshape(int m, int n) {
		
		if (this.numel() != m*n)
			throw new IllegalArgumentException("reshape: Matrix dimensions must agree.");
		
		int im = 0;
		int in = 0;
		double [][] A = this.getArray();
		double [][] B = new double[m][n];
		
		for (int c=0; c<this.getColumnDimension(); c++) {
			for (int r=0; r<this.getRowDimension(); r++) {
				B[im][in] = A[r][c];
				im++;
				if (im>=m) {
					im=0;
					in++;
				}
			}
		}
		return new MyMatrix(B, VectorDim.UNSPECIFIED);
	}
	
	/**
	 *
	 * @param fromRow
	 * @param toRow
	 * @param fromCol
	 * @param toCol
	 * @param dim: dim: 1 (along rows), 2 (along columns)
	 * @return
	 */
	//public double [] sum(int fromRow, int toRow, int fromCol, int toCol, int dim) {
	public MyMatrix sum(int fromRow, int toRow, int fromCol, int toCol, int dim) {
		MyMatrix M = null;
		double [][] A = this.getArray();
		int numCol = toCol - fromCol +1;
		int numRows = toRow - fromRow + 1;
		double [] sum = null;
		if (dim==1) {
			sum = new double[numCol];
			Arrays.fill(sum, 0);
			for (int c=fromCol; c<=toCol; c++)
				for (int r=fromRow; r<=toRow; r++)
					sum[c-fromCol]+=A[r][c];
			
			M = MyMatrix.fromRowVector(sum);
					
		} else {
			sum = new double[numRows];
			Arrays.fill(sum, 0);
			for (int r=fromRow; r<=toRow; r++)
				for (int c=fromCol; c<=toCol; c++)
					sum[r-fromRow]+=A[r][c];
			
			M = MyMatrix.fromColumnVector(sum);
		}
		return M; //sum;
	}
	
	
	/**
	 * 
	 * @param dim: 1 (along rows), 2 (along columns)
	 * @return
	 */
	//public double [] sum(int dim) {
	public MyMatrix sum(int dim) {
		return sum(0, this.getRowDimension()-1, 0, this.getColumnDimension()-1, dim);
	}
	
	
	
	/**
	 * 
	 * @param fromRow
	 * @param toRow
	 * @param fromCol
	 * @param toCol
	 * @param dim
	 * @return
	 */
	public double [] mean(int fromRow, int toRow, int fromCol, int toCol, int dim) {
		
		MyMatrix S = sum(fromRow, toRow, fromCol, toCol, dim); // double [] s =
		
		double s[] = (dim==1) ? S.getRow(0) : S.getColumn(0);
		
		int den = (dim==1) ? (toRow - fromRow + 1) : (toCol - fromCol + 1);
		
		for (int i=0; i<s.length; i++)
			s[i]/=den;
		
		return s;
	}
	
	/**
	 * 
	 * @param dim
	 * @return
	 */
	public double [] mean(int dim) {
		
		MyMatrix S = sum(dim); // double [] s =
		double s[] = (dim==1) ? S.getRow(0) : S.getColumn(0);
		int den = (dim==1) ? (this.getRowDimension()) : (this.getColumnDimension());
		for (int i=0; i<s.length; i++)
			s[i]/=den;
		return s;
	}
	
	
	/**
	 * 
	 * @param dim
	 * @return
	 */
	public MyMatrix meanAsMatrix(int dim) {
		
		double [] m = mean(dim);
		
		MyMatrix M = (dim==2) ? MyMatrix.fromColumnVector(m) : MyMatrix.fromRowVector(m);
		return M;
	}
	
	/**
	 * Compute the standard deviation, given the mean
	 * @param mean
	 * @return
	 * @throws Exception 
	 */
	public MyMatrix stdevAsMatrix(MyMatrix mean) throws Exception {
		if ((mean.getRowDimension()>1) && (mean.getColumnDimension()>1))
			throw new Exception("stdevAsMatrix: illegal format for mean matrix");
		
		MyMatrix res = null;
		if (mean.getRowDimension()==1)
		{
			// The mean matrix is a row vector, this means that we want to compute the standard deviation on dimension 1
			res = this.minusRowVector(mean.getRow(0));
			res.arrayTimesEquals(res);
			res = res.sum(1);
			int N = this.getRowDimension();
			res.timesEquals( 1.0/(double)(N-1) );
		}
		else {
			// The mean matrix is a column vector, this means that we want to compute the standard deviation on dimension 2
			res = this.minusColumnVector(mean.getColumn(0));
			res.arrayTimesEquals(res);
			res = res.sum(2);
			int N = this.getColumnDimension();
			res.timesEquals( 1.0/(double)(N-1) );
		}
			
		return res;
	}
	
	
	/**
	 * Select a single row (no data copy)
	 * @param r
	 * @return a pointer to the row
	 */
	public double [] getRow(int r) {
		double [][] A = this.getArray();
		return A[r];
	}
	/**
	 * Select multiple rows (no data copy) and build a Mymatrix instance
	 * Can be used to rearrange the order of the rows
	 * @param r
	 * @return
	 */
	public MyMatrix getRows(int r[]) {
		int numRows = r.length;
		double [][] b = new double[numRows][];
		double [][] a = this.getArray();
		for (int i=0; i<r.length; i++)
			b[i] = a[r[i]];
		MyMatrix B = new MyMatrix(b, this.getVectorDim());
		return B;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public double [] getColumn(int c) {
		double [] col = new double[this.getRowDimension()];
		double [][] A = this.getArray();
		for (int i=0; i<col.length; i++)
			col[i] = A[i][c];
		return col;
	}
	
	/**
	 * 
	 * @return
	 */
	public double [] getDiag() {
		int p = Math.min(this.getRowDimension(),  this.getColumnDimension());
		double [] d = new double[p];
		for (int i=0; i<p; i++)
			d[i]=this.get(i,  i);
		return d;
	}
	
	/**
	 * Remove rows from the current matrix, creating a new matrix.
	 * @param start: the first row to be removed
	 * @param end: the last row to be removed
	 * @return
	 */
	public MyMatrix removeRows(int start, int end) {
		int numberOfRowsToRemove = end-start+1;
		int newNumberOfRows = this.getRowDimension() - numberOfRowsToRemove;
		int numberOfColumns = this.getColumnDimension();
		double A[][] = this.getArray();
		double B[][] = new double[newNumberOfRows][numberOfColumns];
		int r = 0;
		for (int i=0; i<this.getRowDimension(); i++)
		{
			if ((i<start) || (i>end))
			{
				for (int c = 0; c<numberOfColumns; c++)
				{
					B[r][c] = A[i][c];
				}
				r++;
			}
		}
		
		return new MyMatrix(B, this.vector_dim);
	}
	public MyMatrix removeRows(boolean [] idx) {
		int numberOfRowsToRemove = MyMath.sum(idx);
		int newNumberOfRows = this.getRowDimension() - numberOfRowsToRemove;
		
		if (newNumberOfRows==0)
			return null; // Empty matrix
		
		int numberOfColumns = this.getColumnDimension();
		double A[][] = this.getArray();
		double B[][] = new double[newNumberOfRows][numberOfColumns];
		int r = 0;
		for (int i=0; i<this.getRowDimension(); i++)
		{
			if (!idx[i])
			{
				for (int c = 0; c<numberOfColumns; c++)
				{
					B[r][c] = A[i][c];
				}
				r++;
			}
		}

		return new MyMatrix(B, this.vector_dim);
	}
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public MyMatrix removeColumns(int start, int end) {
		int numberOfColumnsToRemove = end-start+1;
		int newNumberOfColumns = this.getColumnDimension() - numberOfColumnsToRemove;
		int numberOfRows = this.getRowDimension();
		double A[][] = this.getArray();
		double B[][] = new double[numberOfRows][newNumberOfColumns];
		int c = 0;
		for (int i=0; i<this.getColumnDimension(); i++)
		{
			if ((i<start) || (i>end))
			{
				for (int r = 0; r<numberOfRows; r++)
				{
					B[r][c] = A[r][i];
				}
				c++;
			}
		}
		
		return new MyMatrix(B, this.vector_dim);
	}
		

	/**
	 * Split the matrix into blocks, according to block size, i.e. rowDim x colDim. Blocks at the right and bottom borders are handled according to "mode"
	 * @param rowDim
	 * @param colDim
	 * @param mode: 0 = do not return blocks smaller than rowDim x colDim;
	 * 				1 = always return blocks smaller than rowDim x colDim;
	 * @return
	 */
	public ArrayList<ArrayList<MyMatrix>> splitBlockSize(int rowDim, int colDim, int mode) {
		
		ArrayList<ArrayList<MyMatrix>> res = new ArrayList<ArrayList<MyMatrix>>();
		
		int r = this.getRowDimension();
		int c = this.getColumnDimension();

		int rstart = 0;
		int cstart = 0;
		
		int rStep;
		int cStep;
		
		MyMatrix block = null;
			
		int remainingRows = r;
		int remainingCols = c;
		
		
		int minRowDim = (mode==0) ? rowDim : 1; // Blocks cannot have row dimension less than minRowDim
		int minColDim = (mode==0) ? colDim : 1; // Blocks cannot have column dimension less than minColDim
		
		while (remainingRows>=minRowDim)
		{
			
			rStep = remainingRows>=rowDim ? rowDim : remainingRows;
					
			cstart = 0;
			remainingCols = c;
	
			if (remainingCols>=minColDim) // Add a new "row"
			{ 
				ArrayList<MyMatrix> blockRow = new ArrayList<MyMatrix>();
						
				while (remainingCols>=minColDim)
				{
					cStep = (remainingCols>=colDim) ? colDim : remainingCols;
		
					block = this.getMatrix(rstart,  rstart+rStep-1,  cstart,  cstart+cStep-1);
					block.setVectorDim(this.vector_dim);
					blockRow.add(block);
					cstart += cStep;
					remainingCols-= cStep;
				}
				res.add(blockRow);
				
				rstart+=rStep;
				remainingRows-=rStep;
			}
		}
				
		return res;
		
	}
	
	/**
	 * Split the matrix into rectangular "bands" of rowDim rows Blocks at the bottom borders are handled according to "mode"
	 * @param rowDim
	 * @param mode: 0 = do not return blocks with less rows than rowDim;
	 * 				1 = always return blocks with less rows than rowDim;
	 * @return
	 */
	public ArrayList<MyMatrix> splitHorizontally(int rowDim, int mode) {
		
		ArrayList<MyMatrix> res = new ArrayList<MyMatrix>();
		
		int c = this.getColumnDimension();
		
		ArrayList<ArrayList<MyMatrix>> blocks = splitBlockSize(rowDim, c, mode);
		
		for (int i=0; i<blocks.size(); i++)
			res.add(blocks.get(i).get(0));
			
		return res;	
		
	}
	
	/**
	 * 
	 * @param colDim
	 * @param mode
	 * @return
	 */
	public ArrayList<MyMatrix> splitVertically(int colDim, int mode) {
		
		ArrayList<MyMatrix> res = new ArrayList<MyMatrix>();
		
		int r = this.getRowDimension();
		
		ArrayList<ArrayList<MyMatrix>> blocks = splitBlockSize(r, colDim, mode);
		
		for (int i=0; i<blocks.size(); i++)
			res.add(blocks.get(i).get(0));
			
		return res;	
		
	}
	

	/**
	 * 
	 * @return
	 */
	public MyMatrix arraySqrt() {
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] dX = new double[m][n];
		double [][] dA = this.getArray();
		for (int r=0; r<m; r++)
			for (int c=0; c<n; c++)
				dX[r][c] = Math.sqrt(dA[r][c]);
		return new MyMatrix(dX, this.getVectorDim());
	}
	
	/**
	 * 
	 * @param B
	 * @return
	 */
	public MyMatrix arrayRightDivide(MyMatrix B) { //override
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		int mb = B.getRowDimension();
		int nb = B.getColumnDimension();
		
		if ((m==mb) && (n==nb)) {
			return new MyMatrix(super.arrayRightDivide(B), this.getVectorDim());
			
		} else if ((mb==1) && (nb==1)) { // Just a scalar
			return new MyMatrix(super.times(1/B.get(0, 0)), this.getVectorDim());
			
		} else if ((mb==1) && (n==nb)) { // B is a row vector
			double [][] dX = new double[m][n];
			double [][] dA = this.getArray();
			double [][] dB = B.getArray();
			for (int r=0; r<m; r++)
				for (int c=0; c<n; c++)
					dX[r][c] = dA[r][c] / dB[0][c];
			return new MyMatrix(dX, this.getVectorDim());
			
		} else if ((mb==m) && (nb==1)) { // B is a column vector
			double [][] dX = new double[m][n];
			double [][] dA = this.getArray();
			double [][] dB = B.getArray();
			for (int r=0; r<m; r++)
				for (int c=0; c<n; c++)
					dX[r][c] = dA[r][r] / dB[r][0];
			return new MyMatrix(dX, this.getVectorDim());
			
		} else { 
			throw new IllegalArgumentException("rdivide: Matrix dimensions must agree.");
		}
	}
	
	/**
	 * For each column, compute the Euclidean norm (sqrt of sum of squares) and divide the column elements.
	 */
	public MyMatrix normRows () {
		MyMatrix A2 = this.arrayTimes(this); // Ogni elemento e' stato elevato al quadrato
		MyMatrix sumsq = A2.sum(1); // MyMatrix.fromRowVector(A2.sum(1));
		MyMatrix sqrt = sumsq.arraySqrt();
		
		return this.arrayRightDivide(sqrt);
	}
	
	
	/**
	 * For each columns, compute the Euclidean norm (sqrt of sum of squares) and divide the column elements.
	 */
	public void normCols() {
		int nc = this.getColumnDimension();
		int nr = this.getRowDimension();
		double [][]A = this.getArray();
		for (int c=0; c<nc; c++) {
			double tmps = 0;
			for (int r=0; r<nr; r++)
				tmps+=A[r][c]*A[r][c];
			tmps = Math.sqrt(tmps);
			for (int r=0; r<nr; r++)
				A[r][c] /= tmps;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public MyMatrix cov() {
		int N = this.getRowDimension();
		int nc = this.getColumnDimension();
		double [][] A = this.getArray();
		
		double [] mA = this.mean(1);
		
		double [][] d = new double[nc][nc];
		
		for (int i=0; i<nc; i++) {
			for (int j=i; j<nc; j++) {
				d[i][j] = 0;
				for (int r=0; r<N; r++)
					d[i][j] += (A[r][i]-mA[i]) * (A[r][j]-mA[j]);
				
				d[i][j] /= (N-1);
				d[j][i] = d[i][j];
			}
		}
		return new MyMatrix(d, VectorDim.UNSPECIFIED);		
	}
	
	/**
	 * Compute singular value decomposition of the current MyMatrix instance
	 * It is a wrapper for the call to the static MySingularValueDecomposition
	 */
	public MySingularValueDecomposition svd() { // (boolean debug) {
		return new MySingularValueDecomposition(this); //, debug);
	}
	
	/**
	 * Calculates the whitening transformation for cov matrix X.
	 * "This" is supposed to be a covariance matrix
	 * @return
	 */
	public MyMatrix calc_white_mat(boolean debug) {
		
		if (debug)
			System.out.println("calc_white_mat: rank of the input matrix: " + this.rank());
		
		//[~, D, V] = svd(X);
		MySingularValueDecomposition svd = this.svd(); //(debug);
		//W = V * diag(sparse(1./( sqrt(diag(D)) + 1e-10 )));
		Matrix V = svd.getV();
		double [] S = svd.getSingularValues(); //S is the same as diag(D)
		int nr = V.getRowDimension();
		int nc = V.getColumnDimension(); // mi aspetto nr==nc
		double [][] dV = V.getArray();
		double [][] dW = new double[nr][nc];
		for (int r=0; r<nr; r++)
			for (int c=0; c<nc; c++)
				dW[r][c] = dV[r][c] / ( Math.sqrt(S[c]) + 1e-10);
		
		return new MyMatrix(dW, VectorDim.UNSPECIFIED);
	}
	
	// Gaussian random numbers
	public static MyMatrix randn(Random r, int numRows, int numCols, double scalingFactor) {
		double [][] d = new double[numRows][numCols];
		for (int i=0; i<numRows; i++)
			for (int j=0; j<numCols; j++)
				d[i][j] = scalingFactor * r.nextGaussian();
		return new MyMatrix(d, VectorDim.UNSPECIFIED);
	}
	
	public static MyMatrix randn(int numRows, int numCols, double scalingFactor) {
		return randn(new Random(), numRows, numCols, scalingFactor);
	}
	
	/**
	 * 
	 * @param v: an array of double that represents a row vector, to be subtracted from each row of this matrix
	 */
	public void minusEqualRowVector(double []v) {
		if (v.length != this.getColumnDimension())
			throw new IllegalArgumentException("The length of the vectror should match the column dimension of this matrix.");
		
		double A[][] = this.getArray();
		for (int r = 0; r<this.getRowDimension(); r++)
			for (int c = 0; c<this.getColumnDimension(); c++)
				A[r][c] -= v[c]; // Da ogni elemento c-esimo della riga r-esima, sottraggo l'elemento c-esimo di mea
	}
	
	/**
	 * 
	 * @param v: an array of double that represents a column vector, to be subtracted from each column of this matrix
	 */
	public void minusEqualColumnVector(double [] v) {
		if (v.length != this.getRowDimension())
			throw new IllegalArgumentException("The length of the vectror should match the row dimension of this matrix.");
		
		double A[][] = this.getArray();
		for (int c = 0; c<this.getColumnDimension(); c++)
			for (int r = 0; r<this.getRowDimension(); r++)
				A[r][c] -= v[r]; // Da ogni elemento c-esimo della riga r-esima, sottraggo l'elemento c-esimo di mea
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public MyMatrix minusColumnVector(double [] v) {
		if (v.length != this.getRowDimension())
			throw new IllegalArgumentException("The length of the vector should match the row dimension of this matrix.");
		
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		
		double B [][] = new double [m][n];
		double A [][] = this.getArray();
		for (int c = 0; c<n; c++)
			for (int r = 0; r<m; r++)
				B[r][c] = A[r][c] - v[r]; // Da ogni elemento c-esimo della riga r-esima, sottraggo l'elemento c-esimo di mea
		
		return new MyMatrix(B, this.getVectorDim());
	}
	
	/**
	 * 
	 * @param C
	 * @return
	 */
	public MyMatrix minusColumnVector(MyMatrix C) {
		
		if (C.getRowDimension() != this.getRowDimension())
			throw new IllegalArgumentException("The length of the vector should match the row dimension of this matrix.");
		
		if (C.getColumnDimension() !=1)
			throw new IllegalArgumentException("Not a column vector");
		
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		
		double B [][] = new double [m][n];
		double A [][] = this.getArray();
		for (int c = 0; c<n; c++)
			for (int r = 0; r<m; r++)
				B[r][c] = A[r][c] - C.get(r,  0); // Da ogni elemento c-esimo della riga r-esima, sottraggo l'elemento c-esimo di mea
		
		return new MyMatrix(B, this.getVectorDim());
	}
	
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public MyMatrix minusRowVector(double [] v) {
		
		if (v.length != this.getColumnDimension())
			throw new IllegalArgumentException("The length of the vector should match the column dimension of this matrix.");

		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		
		double B [][] = new double [m][n];
		double A [][] = this.getArray();
		
		for (int r = 0; r<m; r++)
			for (int c=0; c<n; c++)
				B[r][c] = A[r][c] - v[c];
		
		return new MyMatrix(B, this.getVectorDim());

	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public MyMatrix plusColumnVector(double [] v) {
		if (v.length != this.getRowDimension())
			throw new IllegalArgumentException("The length of the vectror should match the row dimension of this matrix.");
		
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		
		double B [][] = new double [m][n];
		double A [][] = this.getArray();
		for (int c = 0; c<n; c++)
			for (int r = 0; r<m; r++)
				B[r][c] = A[r][c] + v[r]; // Da ogni elemento c-esimo della riga r-esima, sottraggo l'elemento c-esimo di mea
		
		return new MyMatrix(B, this.getVectorDim());
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public MyMatrix plusRowVector(double [] v) {
		if (v.length != this.getRowDimension())
			throw new IllegalArgumentException("The length of the vector should match the row dimension of this matrix.");
		
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		
		double B [][] = new double [m][n];
		double A [][] = this.getArray();
		for (int r = 0; r<m; r++)
			for (int c = 0; c<n; c++)
				B[r][c] = A[r][c] + v[c]; // Da ogni elemento c-esimo della riga r-esima, sottraggo l'elemento c-esimo di mea
		
		return new MyMatrix(B, this.getVectorDim());
	}
	
	/**
	 * 
	 * @return
	 */
	public MyMatrix sign() {
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] dX = new double[m][n];
		for (int r=0; r<m; r++)
			for (int c=0; c<n; c++)
				dX[r][c] = Math.signum(this.get(r,  c));
		return new MyMatrix(dX, this.getVectorDim());
	}
	
	/**
	 * Compute logarithms of each element of the current matrix
	 * @return a new Matrix with the results
	 */
	public MyMatrix log() {
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] dX = new double[m][n];
		for (int r=0; r<m; r++)
			for (int c=0; c<n; c++)
				dX[r][c] = Math.log(this.get(r,  c));
		return new MyMatrix(dX, this.getVectorDim());
	}
	
	/**
	 * Just add the scalar x to each element
	 * @param x the scalar to be added to the matrix
	 * @return
	 */
	public MyMatrix plus(double x) {
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] dX = new double[m][n];
		for (int r=0; r<m; r++)
			for (int c=0; c<n; c++)
				dX[r][c] = this.get(r,  c) + x;
		return new MyMatrix(dX, this.getVectorDim());
	}
	
	/** Compute the maximum along the specified matrix dimension
	 * @param dim
	 * @return
	 */
	public MyMatrix max(int dim) {
		
		MyMatrix M = null;
		int r = this.getRowDimension();
		int c = this.getColumnDimension();
		if (dim==1)
		{
			double [] x = new double [c];
			for (int i = 0; i<c; i++)
				x [i] = MyMath.max(this.getColumn(i));
				
			M = MyMatrix.fromRowVector(x);
			
		} else
		{
			// return  a column vector
			double [] x = new double [r];
			for (int i = 0; i<r; i++)
				x[i] = MyMath.max(this.getRow(i));
			
			M = MyMatrix.fromColumnVector(x);
		}
		return M;
	}
	
	
	
	/**
	 * For each column otf the matrix, computes the maximum value and its row index, the store the computed values into two separate single-row matrices.
	 * The first Matrix is the row whose i-th element (0<=i) is the maximum value of the i-th column
	 * The second matrix is the row whose i-th element (0<=i) is the row index of the maximu value of the i-th column
	 * The two matrices are returned into an ArrayList<Matrix>, in the same order explained above: get(0) provides the max values, get(1) provides the indexes 
	 * @return
	 */
	private ArrayList<MyMatrix> maximax_1() {
		
		MyMatrix Max = null;
		MyMatrix Imax = null;
		
		int r = this.getRowDimension();
		int c = this.getColumnDimension();
		
		Max = new MyMatrix(1, c);
		Imax = new MyMatrix(1, c);
		
		for (int i = 0; i<c; i++)
		{
			double [] x = MyMath.maximax(this.getColumn(i));
			Max.set(0,  i,  x[0]);
			Imax.set(0,  i,  x[1]);
		}
		
		ArrayList<MyMatrix> res = new ArrayList<MyMatrix>();
		res.add(Max);
		res.add(Imax);
		return res;
		
	}
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<MyMatrix> maximax_2() {
		
		MyMatrix Max = null;
		MyMatrix Imax = null;
		
		int r = this.getRowDimension();
		int c = this.getColumnDimension();
		
		Max = new MyMatrix(r, 1);
		Imax = new MyMatrix(r, 1);
		for (int i = 0; i<r; i++)
		{
			double [] x = MyMath.maximax(this.getRow(i));
			Max.set(i,  0,  x[0]);
			Imax.set(i,  0,  x[1]);
		}
		
		ArrayList<MyMatrix> res = new ArrayList<MyMatrix>();
		res.add(Max);
		res.add(Imax);
		return res;
	}
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<MyMatrix> maximax_12() {
		
		ArrayList<MyMatrix> maximax1 = this.maximax(1);
		MyMatrix diff1max = maximax1.get(0); // row Matrix.MyMatrix Each c-th element is the maximum along the c-th column was found
		MyMatrix imax1 = maximax1.get(1); // row matrix. Each c-th element is the row position where the maximum along the c-th column was found
		
		ArrayList<MyMatrix> maximax2 = diff1max.maximax(2); 
		MyMatrix maxdiffMatrix = maximax2.get(0); // here is a single-value matrix. The maximum value of "this" matrix
		MyMatrix colonnaOfMax_Matrix = maximax2.get(1); // here is a single-value matrix. The column index of the maximum value
				
		double maxdiff = maxdiffMatrix.get(0, 0); // The maximum values
		double colonnaOfMax = colonnaOfMax_Matrix.get(0,0);
		MyMatrix rigaOfMax_Matrix = imax1.getMatrix(0, 0, (int)colonnaOfMax, (int)colonnaOfMax);
		
		ArrayList<MyMatrix> res = new ArrayList<MyMatrix>();
		res.add(maxdiffMatrix);
		res.add(rigaOfMax_Matrix);
		res.add(colonnaOfMax_Matrix);
		
		return res;
	}
	
	/**
	 * 
	 * @param dim
	 * @return
	 */
	public ArrayList<MyMatrix> maximax(int dim) {

		if (dim==1)
			return maximax_1();
		
		if (dim==2)
			return maximax_2();

		return maximax_12();
	}

	

	/**
	 * Compute exp of each element
	 * @return
	 */
	public MyMatrix exp() {
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] dY = new double[m][n];
		double [][] me = this.getArray();
		
		// OPZIONE1
		//for (int r=0; r<m; r++)
		//	for (int c=0; c<n; c++)
		//		dY[r][c] = Math.exp(me[r][c]); //dY[r][c] = Math.exp(this.get(r,  c));
		
		//OPZIONE2: evita reiterate doppie indicizzazioni
		for (int r=0; r<m; r++) {
			double [] miaRiga = me[r];
			double [] yriga = dY[r];
			for (int c =0; c<n; c++)
				yriga[c] = Math.exp(miaRiga[c]);
		}
		return new MyMatrix(dY, this.getVectorDim());
		
	}
	

	/**
	 * Compute absolute value of each element
	 * @return
	 */
	public MyMatrix abs() {
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] dY = new double[m][n];
		double [][] me = this.getArray();
		//OPZIONE 1
		//for (int r=0; r<m; r++)
		//	for (int c=0; c<n; c++)
		//		dY[r][c] = Math.abs(this.get(r,  c));
		
		// OPZIONE2
		for (int r=0; r<m; r++) {
			double [] miaRiga = me[r];
			double [] yriga = dY[r];
			for (int c =0; c<n; c++)
				yriga[c] = Math.abs(miaRiga[c]);
		}
			
		return new MyMatrix(dY, this.getVectorDim());
	}
	
	/**
	 * Matrix concatenation
	 * @param dim
	 * @return
	 * @throws Exception 
	 */
public MyMatrix cat(MyMatrix B, int dim) throws Exception {
		
		if ((dim!=1) && (dim!=2))
			throw new Exception("Illegal dim value");
		
		int mA = this.getRowDimension();
		int nA = this.getColumnDimension();
		int mB = B.getRowDimension();
		int nB = B.getColumnDimension();
		double [][] x = null;
		double [][] a = this.getArray();
		double [][] b = B.getArray();
		if (dim==1)
		{
			if (nA != nB)
				throw new Exception("Matrix dimension mismatch");

			int n = nA;
			x = new double[mA+mB][n];
			
			for (int r=0; r<mA; r++)
				System.arraycopy(a[r], 0, x[r], 0, n);
			for (int r=0; r<mB; r++)
				System.arraycopy(b[r], 0, x[r+mA], 0, n);
		}
		else
		{
			if (mA != mB)
				throw new Exception("Matrix dimension mismatch");

			int m = mA;
			x = new double[m][nA+nB];
			for (int r=0; r<m; r++) {
				System.arraycopy(a[r], 0, x[r], 0, nA);
				System.arraycopy(b[r], 0, x[r], nA, nB);
			}
		}

		MyMatrix X = new MyMatrix(x, this.getVectorDim());
		return X;

}
		
	/*
	 * BUGGED
	public MyMatrix cat(MyMatrix B, int dim) throws Exception {
		
		if ((dim!=1) && (dim!=2))
			throw new Exception("Illegal dim value");
		
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		double [][] x = null;
		double [][] a = this.getArray();
		double [][] b = B.getArray();
		if (dim==1)
		{
			if (n != B.getColumnDimension())
				throw new Exception("Matrix dimension mismatch");

			x = new double[2*m][n];
			
			for (int r=0; r<m; r++) {
				for (int c=0; c<n; c++) {
					x[r][c]   = a[r][c];
					x[r+m][c] = b[r][c];
				}
			}
		}
		else
		{
			if (m != B.getRowDimension())
				throw new Exception("Matrix dimension mismatch");
			
			x = new double[m][2*n];
			for (int r=0; r<m; r++) {
				for (int c=0; c<n; c++) {
					x[r][c]   = a[r][c];
					x[r][c+n] = b[r][c];
				}
			}
		}
		
	
		MyMatrix X = new MyMatrix(x, this.getVectorDim());
		return X;
			
	}
	*/
	
	/**
	 * 
	 * @return
	 */
	public float [][] toFloatArray() {
		
		int m = this.getRowDimension();
		int n = this.getColumnDimension();
		float [][] fa = new float[m][n];
		double [][] da = this.getArray();
		
		for(int r=0; r<m; r++)
			for (int c=0; c<n; c++)
				fa[r][c] = (float)da[r][c];
		
		return fa;
	}
	
	/**
	 * Create a new MyMatrix instance from a bidimensional array of float
	 * @param ff the bidimensional array of float
	 * @return the new MyMatrix instance, filled with input data
	 */
	public static MyMatrix fromFloatArray(float [][]ff) {
		
		// Check the size of each row
		int n = ff[0].length;
		int m = ff.length;
		for (int i = 1; i<m; i++)
			if (ff[i].length!=n)
				throw new IllegalArgumentException("MyMatrix.fromFloatArray: row vectors dimensions must agree.");				
		
		double [][] da = new double [m][n];
		for(int r=0; r<m; r++)
			for (int c=0; c<n; c++)
				da[r][c] = ff[r][c];
		
		return new MyMatrix(da);
	}
	
	/**
	 * Sort rows of a matrix in ASCENDING order
	 * Inspired to Matlab's sortrows
	 * @return
	 */
	public MyMatrix sortrows() {
		return sortrows(SortDir.ASCENDING);
	}
	/**
	 * Sort rows of a matrix according to the provided direction
	 * Inspired to Matlab's sortrows
	 * @param direction the sort direction
	 * @return
	 */
	public MyMatrix sortrows(SortDir direction) {
		int n = this.getColumnDimension();
		int [] col = MyMath.colon(0, n-1); // i.e. 0:(n-1)
		int [] I = sortBackToFront(col, direction);
		MyMatrix B = this.getRows(I);
		return B;
		// in a single row: return this.getRows(sortBackToFront(MyMath.colon(0, this.getColumnDimension()-1, direction));
	}
	private int [] sortBackToFront(int [] col, SortDir direction) {

		int n = col.length;
		int m = this.getRowDimension();

		int [] I = MyMath.colon(0, m-1);

		for (int k=n-1; k>=0; k--)
		{
			int ck = col[k];
			double [] column = this.getColumn(ck);
			double [] keys = MyMath.select(column, I);
			int [] ind = MySort.sort_and_replace(keys, direction);  // keys are usless now
			I = MyMath.select(I, ind);			
		}
		return I;	
	}

	
	
	
	/**
	 * 
	 * @param keys
	 * @param direction
	 * @return
	 */
	public MyMatrix sortColumnsAndKeys(double [] keys, SortDir direction) {
		
		int [] indici_sorted = MySort.sort_and_replace(keys, direction);
		MyMatrix sortedMatrix = this.getMatrix(0, this.getRowDimension()-1, indici_sorted);
		return sortedMatrix;
	}
	
	/**
	 * 
	 * @param keys
	 * @param direction
	 * @return
	 */
	public MyMatrix sortColumnsAndKeys(Short [] keys, SortDir direction) {
		
		int [] indici_sorted = MySort.sort_and_replace(keys, direction);
		MyMatrix sortedMatrix = this.getMatrix(0, this.getRowDimension()-1, indici_sorted);
		return sortedMatrix;
	}
	

	/**********************************************************************
	 * 						MATRIX COMPARISON
	 * ********************************************************************
	 */
	/**
	 * 
	 * @param B
	 * @return
	 */
	public double compare(double[][] B) {
		
		MyMatrix diff = this.minus(new Matrix(B));
		
		double th=1E-8;
		
		int m = diff.getRowDimension();
		int n = diff.getColumnDimension();
		MyMatrix diffrelM = new MyMatrix(m, n);
		double [][]diffrel = diffrelM.getArray();
		
		for (int r=0; r<m; r++)
			for (int c = 0; c<n; c++)
				diffrel[r][c] = (diff.get(r, c) <= th)? 0: diff.get(r, c)/this.get(r,  c);
					
		double normInf = diffrelM.normInf();
		return normInf;		
	}
	
	/**
	 * 
	 * @param msg
	 * @param Mmatlab
	 * @return
	 */
	public double compare(String msg, double[][] Mmatlab) {
		
		MyMatrix diff = this.minus(new Matrix(Mmatlab)).abs();
		
		ArrayList<MyMatrix> maximax0 = diff.maximax(0);		
		double maxdiff = maximax0.get(0).get(0, 0);
		double rigaOfMax = maximax0.get(1).get(0,0);
		double colonnaOfMax = maximax0.get(2).get(0,0);
		
		double th=1E-8;
		
		int m = diff.getRowDimension();
		int n = diff.getColumnDimension();
		MyMatrix diffrelM = new MyMatrix(m, n);
		double [][]diffrel = diffrelM.getArray();
		
		for (int r=0; r<m; r++)
			for (int c = 0; c<n; c++)
			{
				double den = Mmatlab[r][c];
				double di = diff.get(r, c);
				double drel = Math.abs(di/den);
				//if (den==0.0) {
				//	System.out.println("("+r+","+c+"): Mjava=" + this.get(r,  c)+ " Mmatlab=" + Mmatlab[r][c]+ " diff="+di+" den="+den+ " drel="+drel);
				//}
				//System.out.println("("+r+","+c+"): Mjava=" + Mjava.get(r,  c)+ " Mmatlab=" + Mmatlab[r][c]+ "di="+di+" den="+den+ " drel="+drel);
				
				//System.out.println("("+r+","+c+"): den="+den+ " drel="+drel);
				diffrel[r][c] = drel;
				//diffrel[r][c] = (diff.get(r, c) <= th)? 0: diff.get(r, c)/A.get(r,  c);
			}
					
		ArrayList<MyMatrix> maximax0Rel = diffrelM.maximax(0);		
		double maxDiffRel = maximax0Rel.get(0).get(0, 0);
		int rigaOfMaxRel = (int)maximax0Rel.get(1).get(0,0);
		int colonnaOfMaxRel = (int)maximax0Rel.get(2).get(0,0);
		
		double normInf = diffrelM.normInf();
		System.out.println(msg);
		System.out.println("\t max diff: " + maxdiff);
		if (maxDiffRel==0)
			System.out.println("\t max diff rel: " + maxDiffRel);
		else
			System.out.println("\t max diff rel: " + maxDiffRel + " at (" +  rigaOfMaxRel + ", "+ colonnaOfMaxRel + ") ==> elements were: " + this.get(rigaOfMaxRel, colonnaOfMaxRel) + " ; and : " + Mmatlab[rigaOfMaxRel][colonnaOfMaxRel]);
		System.out.println("\t normInf(diffRel) = " + normInf);
		return normInf;
	}
	
	/**
	 * Compare the current MyMatrix with the provided MyMatrix argument
	 * @param msg a message to be displayed
	 * @param Mmatlab the MyMatrix to be compared with the current one
	 * @return
	 */
	public double compare(String msg, MyMatrix Mmatlab) {
		return compare(msg, Mmatlab.getArray());
	}
	
	/**********************************************************************
	 * 						 SPECIAL MATRIX
	 * ********************************************************************
	 */
	
	/**
	 * 
	 * @param x
	 * @return
	 */
	public static MyMatrix toeplitzReal(double[] x, int dim) {

		double [][] T = new double [dim][dim];
		
		for (int r=0; r<dim; r++)
			for (int k = 0; k<dim; k++)
				T[r][k] = x[Math.abs(k-r)];

		MyMatrix M = new MyMatrix(T);
		
		return M;
	}
	
	public static MyMatrix toeplitzReal(double[] x) {
		return toeplitzReal(x, x.length);
	}
	
	/**
	 * Compute the companion matrix of the monic polynomial u(0)*x^n + u(1)*x^(n-1) + ... + u(n)
	 * 
	 * @param u is the array of the coefficients starting from n degree
	 * @return
	 */
	public static MyMatrix compan(double[] u) {
		
		int numberOfCoeff = u.length;
		
		int dim = numberOfCoeff-1;
		double [][] C = new double[dim][dim];
		
		double uMaxDeg = u[0];
		for (int j=0; j<dim; j++) // The first row
			C[0][j] = -u[j+1]/uMaxDeg;
			
		for (int i = 1; i<dim; i++)
			for (int j = 0; j<dim; j++)
				C[i][j] = (j==(i-1)) ? 1 : 0;
		
		MyMatrix M = new MyMatrix(C);
		
		return M;

	}
	
	
	
	/**********************************************************************
	 * 						MATRIX INPUT / OUTPUT
	 * ********************************************************************
	 */
	
	/**
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void write(OutputStream os) throws IOException {
		IOClass.writeInt(os, this.getRowDimension(), ByteOrder.LITTLE_ENDIAN);
		IOClass.writeInt(os, this.getColumnDimension(), ByteOrder.LITTLE_ENDIAN);
		
		// For I/O only
		int vector_dim_code = this.vector_dim==VectorDim.UNSPECIFIED ? 0 :
							this.vector_dim==VectorDim.ROW_VECTORS ? 1: 2;
								
		IOClass.writeInt(os, vector_dim_code, ByteOrder.LITTLE_ENDIAN);
		double [][] A = this.getArray();
		for (int r=0; r<this.getRowDimension(); r++)
			IOClass.writeDoubleArray(os, A[r], ByteOrder.LITTLE_ENDIAN);
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
	 * @param filePath
	 * @throws IOException
	 */
	public void writeToFile(String filePath) throws IOException  {
		
		int i = filePath.lastIndexOf('.');
		if (i > 0) {
		    String extension = filePath.substring(i+1);
		    if (extension.equals("ufv"))
		    {
		    	writeToUfvFile(filePath);
		    	return;
		    }
		}
		
		writeToFile(new File(filePath));
	}
	
	/**
	 * Required format for GMM estimation
	 * @param filePath
	 * @throws IOException
	 */
	public void writeToUfvFile(String filePath) throws IOException  {
		File file = new File(filePath);
		OutputStream os = file == null ? System.out : new FileOutputStream(file);
		
		// Need a frame for each row, so vectors must be row vectors
		MyMatrix M = (this.vector_dim==VectorDim.COLUMN_VECTORS) ? this.transpose() : this;
			
		// Write the dimension of each row vector of features
		IOClass.writeInt(os, this.getColumnDimension(), ByteOrder.LITTLE_ENDIAN);
		
		double [][] A = M.getArray();
		for (int r=0; r<M.getRowDimension(); r++)
			IOClass.writeDoubleArray(os, A[r], ByteOrder.LITTLE_ENDIAN);
		
		os.flush();
		os.close();
		
	}
	
	
	/*@Override
	public Object uReadFromFile(String filePath) throws IOException, ClassNotFoundException {
		return MyMatrix.readFromFile(filePath);
	}*/
	
	/**
	 * My read function for UFV file format
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public static MyMatrix readFromUfvFile(String filepath) throws IOException {
		MyMatrix m = null;
		File file = new File(filepath);
		InputStream is = (file==null) ? System.in : new FileInputStream(file);
		
		// In the UFV format the first item to read is the dimensionality of vectors/frames/features
		int fd = IOClass.readInt(is, ByteOrder.LITTLE_ENDIAN);
		
		// Then, we try to read an integer number of fd-sized vectors of double
		ArrayList<double[]> arr = new ArrayList<double[]>();
		boolean eof = false;
		while (!eof)
		{
			double [] buf = new double[fd]; // A new double[] is necessary each time
			
			int read = IOClass.readDoubleArray(is, buf, ByteOrder.LITTLE_ENDIAN);
			
			eof = (read<0);
			
			if (!eof)
			{
				if (read<buf.length) // If the vector can not be fully read, something is wrong in the file
					throw new IOException("Could not read from file " + filepath);
				
				arr.add(buf);
			}
		}
		is.close();
		m = MyMatrix.fromArrayListOfDoubleArray(arr, VectorDim.ROW_VECTORS);
		return m;
		
	}
	

	/**
	 * 
	 * @param filePath
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static MyMatrix readFromFile(String filePath) throws ClassNotFoundException, IOException {
		
		// Check extension
		int i = filePath.lastIndexOf('.');
		if (i > 0) {
		    String extension = filePath.substring(i+1);
		    if (extension.equals("ufv"))
		    {
		    	return readFromUfvFile(filePath);
		    }
		}
		return readFromFile(new File(filePath));
	}
	
	
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static MyMatrix readFromFile(File file) throws IOException, ClassNotFoundException {
			
		InputStream is = null;
		if (file == null)
			is = System.in;
		else
			is = new FileInputStream(file);
			
		MyMatrix M = MyMatrix.readFromInputStream(is);
			
		is.close();
		return M;
	}
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static MyMatrix readFromInputStream(InputStream is) throws IOException {

		int m          = IOClass.readInt(is, ByteOrder.LITTLE_ENDIAN);
		int n          = IOClass.readInt(is, ByteOrder.LITTLE_ENDIAN);
		
		int vector_dim_code = IOClass.readInt(is, ByteOrder.LITTLE_ENDIAN);
		VectorDim vector_dim = vector_dim_code == 0 ? VectorDim.UNSPECIFIED :
								vector_dim_code == 1 ? VectorDim.ROW_VECTORS : VectorDim.COLUMN_VECTORS;
				
		double [][] A = new double[m][n];
		for (int r = 0; r<m; r++)
		{
			/*if (!IOClass.readDouble(is, A[r], ByteOrder.LITTLE_ENDIAN))
				throw new IOException("Could not read the matrix");*/
			if (IOClass.readDoubleArray(is, A[r], ByteOrder.LITTLE_ENDIAN)< A[r].length)
				throw new IOException("Could not read the matrix");
		}
		MyMatrix M = new MyMatrix(A, vector_dim);
		return M;
	}

}
