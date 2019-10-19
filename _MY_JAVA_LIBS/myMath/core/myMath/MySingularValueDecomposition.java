package myMath;

import Jama.SingularValueDecomposition;

public class MySingularValueDecomposition extends SingularValueDecomposition {

	private boolean debug = false;
	
	//double [] svd_D_debug = null;
	//MyMatrix svd_V_debug = null;
	//MyMatrix svd_U_debug = null;
	MyMatrix myV = null;
	MyMatrix myU = null;
	double [] myD = null;
	
	//public void debug(boolean d) {debug = d;}
	//public boolean debug() { return debug;}
	
	
	public MySingularValueDecomposition(MyMatrix M) { //, boolean debug) {
		super(M);
		//this.debug = debug;
		
		//if (debug) {
			adjust_for_debug(M);
		//}	
	}
	
	/**
	 * 
	 */
	public double [] getSingularValues() { //OVERRIDE
		/*if (debug)
			return svd_D_debug;
		else
			return super.getSingularValues();
			*/
		return myD;
	}
	
	/**
	 * 
	 */
	public MyMatrix getV() { // OVERRIDE
		/*
		if (debug)
			return svd_V_debug;
	
		else
			return new MyMatrix(super.getV());
			*/
		return myV;
	}
	
	public MyMatrix getU() { //OVERRIDE
		/*
		if (debug)
			return svd_U_debug;
		
		else
			return new MyMatrix(super.getU());
			*/
		return myU;
	}
	
	public MyMatrix getS() { //OVERRIDE
		return new MyMatrix(super.getS());
	}
	
	/*private void adjust_for_debug() {
		// Adjust V
		svd_D_debug = super.getSingularValues().clone();
		MyMatrix svd_V_unsorted = new MyMatrix(super.getV());
		// Sort the values and the vectors
		//svd_V_debug = MyEigenvalueDecomposition.sortEig(svd_V_unsorted.copy(), svd_D_debug); //eigenvectors as column vectors. They and evals have been sorted
		svd_V_debug = svd_V_unsorted.sortColumnsAndKeys(svd_D_debug, SortDir.ASCENDING); // ASCENDING?
		// normrows
		svd_V_debug = svd_V_debug.normRows();
		// change signs
		svd_V_debug = svd_V_debug.arrayRightDivide(svd_V_debug.getMatrix(0, 0, 0, svd_V_debug.getColumnDimension()-1).sign());
		
		// Adjust U
		svd_D_debug = super.getSingularValues().clone();
		MyMatrix svd_U_unsorted = new MyMatrix(super.getU());
		//svd_U_debug = MyEigenvalueDecomposition.sortEig(svd_U_unsorted.copy(), svd_D_debug);
		svd_U_debug = svd_U_unsorted.sortColumnsAndKeys(svd_D_debug, SortDir.ASCENDING);
		svd_U_debug = svd_U_debug.normRows();
		svd_U_debug = svd_U_debug.arrayRightDivide(svd_U_debug.getMatrix(0, 0, 0, svd_U_debug.getColumnDimension()-1).sign());
	}*/
	
	private void adjust_for_debug(MyMatrix A) {
		
		myD = super.getSingularValues().clone();
		MyMatrix superV = new MyMatrix(super.getV());
		MyMatrix segni = superV.getMatrix(0, 0, 0, superV.getColumnDimension()-1).sign();
				// change signs
		//svd_V_debug = svd_V_debug.arrayRightDivide(svd_V_debug.getMatrix(0, 0, 0, svd_V_debug.getColumnDimension()-1).sign());
		myV = superV.arrayRightDivide(segni);
		MyMatrix superU = new MyMatrix(super.getU());
		myU = superU.arrayRightDivide(segni);
		
		// Check
		MyMatrix dRel = A.minus(myU.times(getS()).times(myV.transpose())).arrayRightDivide(A); //    ((X-U2*D*V2')./X))
		double max = dRel.max(1).max(2).get(0, 0);
		System.out.println("JAVA SVD: check: " + max) ;
	}
}
