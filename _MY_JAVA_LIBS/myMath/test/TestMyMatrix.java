package myMath.test;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import com.mathworks.engine.EngineException;
import com.mathworks.matlab.types.Struct;

import myMath.MyMath;
import myMath.MyMatrix;
import myMatlabConnection.MyMatlabConnection;
import myMath.MySingularValueDecomposition;
import myMath.MySort;

public class TestMyMatrix {

	MyMatlabConnection matlab = null;
	
	static final String sep ="-------------------------------------------------\n";


	public TestMyMatrix() throws Exception {
		matlab = new MyMatlabConnection();
	}


	private void sortrows() {
		MyMatrix A = new MyMatrix(new double [][] { {10, 2, 8}, {10, 1, 7}, {4, 1, 1}, {2, 1, 1}, {10, 2, 7}});
		MyMatrix SOL = new MyMatrix(new double [][] { {2, 1, 1}, {4, 1, 1}, {10, 1, 7}, {10, 2, 7}, {10, 2, 8}});
		MyMatrix RES = A.sortrows(MySort.SortDir.ASCENDING);
		//System.out.println(sep);
		//RES.print(0,  0);
		SOL.compare(sep+"TestMyMatrix: sortrows(ASCENDING)", RES);
	}
	/**
	 * 
	 * @throws Exception
	 */
	public void compan() throws Exception {


		double [] d = MyMath.uniformRandomVector(10);

		double [][] m_co = matlab.eng.feval("compan",  d);

		MyMatrix JCO = MyMatrix.compan(d);

		JCO.compare(sep+"TestMyMatrix: compan()", m_co);
	}


	/**
	 * 
	 * @param dim
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws EngineException 
	 * @throws RejectedExecutionException 
	 */
	void test_svd(int dim) throws RejectedExecutionException, EngineException, InterruptedException, ExecutionException {
		
		MyMatrix Sb = MyMatrix.randn(dim, dim, 1.0);
		Sb = Sb.times(Sb.transpose());
		
		System.out.println(sep+"TestMyMatrix: TESTING SINGULAR VALUES DECOMPOSITION (SVD)");
		test_svd("Sb", Sb); //, false);
	}
	
	/**
	 * 
	 * @param id
	 * @param Sb
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws EngineException 
	 * @throws RejectedExecutionException 
	 */
	private MySingularValueDecomposition test_svd(String id, MyMatrix Sb) throws RejectedExecutionException, EngineException, InterruptedException, ExecutionException { //, boolean debug) {
		
		String testName = "my_svd(X)" + id;
		MySingularValueDecomposition JAVA_svd = Sb.svd(); //(debug);
		Struct MATLAB_svd = matlab.eng.feval("my_svd_testJava", Sb.getArray());

		MyMatrix JAVA_SVD_V = JAVA_svd.getV();
		MyMatrix MATLAB_SVD_V = new MyMatrix((double [][])MATLAB_svd.get("V"));
		JAVA_SVD_V.compare(testName + " V : \t", MATLAB_SVD_V);

		double [] JAVA_SVD_D = JAVA_svd.getSingularValues();
		double [] MATLAB_SVD_D = new MyMatrix((double [][])MATLAB_svd.get("D")).getDiag();
		MyMath.compare_vectors(testName + " D : \t", JAVA_SVD_D, MATLAB_SVD_D);
		
		int dim = JAVA_SVD_V.getRowDimension();
		if (dim<5)
		{
			System.out.print("JAVA   D: ");
			for (int i=0; i<dim-1; i++) System.out.print(" " + JAVA_SVD_D[i] + ", ");
			System.out.println(" " + JAVA_SVD_D[dim-1] );
			
			System.out.print("MATLAB D: ");
			for (int i=0; i<dim-1; i++) System.out.print(" " + MATLAB_SVD_D[i] + ", ");
			System.out.println(" " + MATLAB_SVD_D[dim-1] );
		}

		MyMatrix JAVA_SVD_U = JAVA_svd.getU();
		MyMatrix MATLAB_SVD_U = new MyMatrix((double [][])MATLAB_svd.get("U"));
		JAVA_SVD_U.compare(testName + " U : \t", MATLAB_SVD_U);
		
		if (dim<5)
		{
			System.out.println("JAVA V:");
			JAVA_SVD_V.print(10,  10);
			System.out.println("\nMATLAB V:");
			MATLAB_SVD_V.print(10, 10);
		}
		
		return JAVA_svd;
	}
	

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception {

		TestMyMatrix me = new TestMyMatrix();

		me.compan();
		
		me.test_svd(10);
		
		me.sortrows();

		System.out.println("\n"+sep+"DONE");
	}
}
