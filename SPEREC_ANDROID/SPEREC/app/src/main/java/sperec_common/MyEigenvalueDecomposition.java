package sperec_common;

import java.util.Iterator;
import java.util.LinkedList;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import myMath.MyMatrix.VectorDim;
import myMath.MySort.SortDir;
import myMath.MyMatrix;
import sperec_common.de.fau.cs.jstk.util.Pair;

public class MyEigenvalueDecomposition extends EigenvalueDecomposition { // For debug purpose
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private boolean debug = false;
	
	// To be returned when the debug flag is enabled
	//private MyMatrix V_debug = null; 
	//private MyMatrix D_debug = null;
	
	// To be returned when the debug flag is disabled
	private MyMatrix MyV = null;
	private MyMatrix MyD = null;
	
	
	//public void debug(boolean d) {debug = d;}
	//public boolean debug() { return debug;}

	public MyEigenvalueDecomposition(Matrix M) { //, boolean debug
		
		// Call the JAMA method
		super(M);
		
		// Then, by default, I want that eigenvalues are sorted in ascending order, as Matlab does.
		// So, sort eigenvectors and eigenvalues
		Matrix Dunsorted = super.getD().copy();
		double [] evals = new MyMatrix(Dunsorted).getDiag(); // Eigenvalues
		MyMatrix Vunsorted = new MyMatrix(super.getV());
		
		MyV = Vunsorted.sortColumnsAndKeys(evals, SortDir.ASCENDING);
		// and rebuild D
		int m = Dunsorted.getRowDimension();
		int n = Dunsorted.getColumnDimension();
		MyD = new MyMatrix(m, n);
		
		int min = Math.min(m,  n);
		for (int i=0; i<min; i++)
		{
			MyD.set(i,  i,  evals[i]);
		}
		
		//this.debug = debug;
		
		//if (debug) {
			adjust_for_debug();
		//}
	}
	
	/**
	 * 
	 */
	
	public MyMatrix getD() { //OVERRIDE
		return MyD;
		/*if (debug)
			return MyD;
		else
			return MyD; //new MyMatrix(super.getD());
			*/
	}
	
	
	/**
	 * 
	 */
	public MyMatrix getV() { // OVERRIDE
		
		/*if (debug)
			return V_debug;
	
		else*/
			return MyV; //snew MyMatrix(super.getV());
	}
	
	/**
	 * 
	 */
	private void adjust_for_debug() {
		
		// D and V have already been sorted
		/*
		Matrix Dunsorted = super.getD().copy();
		double [] evals = new MyMatrix(Dunsorted).getDiag(); // Eigenvalues
		Matrix Vunsorted = super.getV().copy();
		
		// 4) Rebuild D
		D_debug = new MyMatrix(Dunsorted);
		int m = Dunsorted.getRowDimension();
		int n = Dunsorted.getColumnDimension();
		int min = Math.min(m,  n);
		for (int i=0; i<min; i++) {
			D_debug.set(i,  i,  evals[i]);
		}
		
		// 1) Sort eigenvectors and eigenvalues
		V_debug = MyEigenvalueDecomposition.sortEig(Vunsorted, evals); //eigenvectors as column vectors. They and evals have been sorted
		*/
		
		// 2) then normrows
		MyV = MyV.normRows();
		
		// 3) Adjust signs
		MyV = MyV.arrayRightDivide(MyV.getMatrix(0, 0, 0, MyV.getColumnDimension()-1).sign());
		
		
	}
	
	/**
	 * Sort the eigenvalues evals in ascending order, and sort the eigenvectors accordingly
	 * @param V0: the matrix of eigenvectors (as column vectors) related to evals
	 * @param evals: unsorted then sorted eigenvalues
	 * @return: sorted eigenvalues in eval, sorted eigenvectors in the return Matrix
	 */
	//WRONG
	/*
	public static MyMatrix sortEig(Matrix V0, double [] evals) {
		// V0 has eigenvectors as column vectors
		int numv = V0.getColumnDimension();
		//System.out.println("V0:"); V0.print(5,5);
		// save the eigenvectors (use transposed for java convenience)
		double [][] vhelp = V0.transpose().getArray(); // Gli autovettori ora sono sulle righe
		
		LinkedList<Pair<double [], Double>> sortedEV = MySort.sortRowsAscending(vhelp, evals); //MySort.sortRowsDescending(vhelp, evals);
		double [][] proj = new double [numv][]; //autovettori sulle righe // projection matrix [rows][columns] 
		Iterator<Pair<double [], Double>> it = sortedEV.iterator();
		for (int i = 0; i < numv; ++i) {
			Pair<double [], Double> p = it.next();
			proj[i] = p.a;
			evals[i] = p.b;
		}
		MyMatrix V = new MyMatrix(proj).transpose(); // Eigenvectors as column vectors
		V.setVectorDim(VectorDim.COLUMN_VECTORS);
		// System.out.println("V:"); V.print(5,5);
		return V;
	}
*/
	

}
