package test;

import com.mathworks.engine.MatlabEngine;
import com.mathworks.matlab.types.Struct;

import Jama.Matrix;
import sperec_common.BWStatistics;
import myMath.MyMatrix;
import sperec_common.TVSpace;
import myMath.MyMatrix.VectorDim;

public class MATLAB_TVSpace extends TVSpace {
	
	private MyMatrix Tinit = null;
	
	@Override
	protected MyMatrix initialize(int numRows, int numCols) {
		
		MyMatrix T = super.initialize(numRows, numCols);
		Tinit = T.copy();
		return T;
	}
	
	public MyMatrix getTinit() { // For debug purpose
		return Tinit;
	}
	
	@Override
	protected MyMatrix my_train_tv_space_N_F_S_Tinit_SINGLE_ITER(BWStatistics bw, double [] S, int tv_dim, int nd, int fd, int nworkers, MyMatrix T, int [] idx_sv) throws Exception {
		
		MyMatrix T0 = T.copy(); // before the call of the superclass method
		
		MyMatrix Tjava = super.my_train_tv_space_N_F_S_Tinit_SINGLE_ITER(bw, S, tv_dim, nd, fd, nworkers, T, idx_sv);
		
		MyMatrix n = bw.N.copy();
		MyMatrix f = bw.F.copy();
		assert(n.getVectorDim()==VectorDim.ROW_VECTORS);  
		assert(f.getVectorDim()==VectorDim.ROW_VECTORS);

		
		compare_matlab_my_train_tv_space_N_F_S_Tinit_SINGLE_ITER(n, f, S, tv_dim, nd, fd, nworkers, T0, idx_sv, Tjava);
		
		return Tjava;
		
	}
	
	private void compare_matlab_my_train_tv_space_N_F_S_Tinit_SINGLE_ITER(MyMatrix n_, MyMatrix f_, double [] S, int tv_dim, int nd, int fd, int nworkers, MyMatrix T0, int [] idx_sv, MyMatrix T) {
		// Adjust input paramsMatlabEngine eng = null;
		int [] idx_sv_ = new int [ idx_sv.length];
		for (int i = 0; i<idx_sv_.length; i++)
			idx_sv_[i] = idx_sv [i]+1;
		// The matlab script need this format
		MyMatrix n = null;
		MyMatrix f = null;
		
		assert(f_.getVectorDim()==VectorDim.ROW_VECTORS);
		assert(n_.getVectorDim()==VectorDim.ROW_VECTORS);
		f = f_.copy();
		n = n_.copy();
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		try {
			
			double[][] matlab_T = eng.feval("my_train_tv_space_N_F_S_Tinit_SINGLE_ITER", n.getArray(), f.getArray(), S, tv_dim, nd, fd, nworkers, T0.getArray(), idx_sv_);
			//System.out.println("Comparing against matlab: expectation_tv(...) = " + testDebug.compare_matrices(RU, matlab_T) );
			System.out.println("Comparing against matlab: my_train_tv_space_N_F_S_Tinit_SINGLE_ITER(...) = " + TEST.compare_matrices(T, matlab_T) );
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MATLAB.closeSperecMatlabConnection(eng);
	}
	
	@Override
	protected MyMatrix compute_T_invS(MyMatrix T, double [] S) throws Exception {
		
		MyMatrix T_invS_java = super.compute_T_invS(T, S);
		
		compare_matlab_T_invS(T, S, T_invS_java);
		
		return T_invS_java;
	}
	
	private void compare_matlab_T_invS(MyMatrix T, double [] S, MyMatrix T_invS) { // Debug
		MatlabEngine eng = null;
		try {
			eng = MATLAB.getMatlabConnection();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assert(eng!=null);
		double[][] matlab_T = null;
		try {
			matlab_T = eng.feval("compute_T_invS", T.getArray(), S);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Comparing against matlab: compute_T_invS(T, S) = " + TEST.compare_matrices(T_invS, matlab_T) );
		MATLAB.closeSperecMatlabConnection(eng);
	}
	
	
	@Override
	protected void expectation_tv_compute_Ex_Exx(int tv_dim, int len, int nworkers, MyMatrix T_invS, Matrix N1, Matrix F1, int [] idx_sv, Matrix Ttransp) {
		
		super.expectation_tv_compute_Ex_Exx(tv_dim, len, nworkers, T_invS, N1, F1, idx_sv, Ttransp);
		
		compare_matlab_expectation_tv_compute_Ex_Exx(tv_dim, len, nworkers, T_invS, N1, F1, idx_sv, Ttransp);
	}

	private void compare_matlab_expectation_tv_compute_Ex_Exx(int tv_dim, int len, int nworkers, MyMatrix T_invS, Matrix N1, Matrix F1, int [] idx_sv, Matrix Ttransp) {
		// Adjust input paramsMatlabEngine eng = null;
		int [] idx_sv_ = new int [ idx_sv.length];
		for (int i = 0; i<idx_sv_.length; i++)
			idx_sv_[i] = idx_sv [i]+1;
		
		MatlabEngine eng = MATLAB.getMatlabConnection();

		try {
			
			Struct res_matlab = eng.feval("expectation_tv_compute_Ex_Exx_testJava", tv_dim, len, nworkers, T_invS.getArray(), N1.getArray(), F1.getArray(), idx_sv_, Ttransp.getArray());
			double [][] Ex_matlab = (double [][])res_matlab.get("Ex");
			double [][][] Exx_matlab = (double[][][])res_matlab.get("Exx");
			TEST.compare_matrices("\tEx: ", Ex, Ex_matlab);
			for (int i=0; i<Exx.size(); i++)
			{
				MyMatrix Exx_i = Exx.get(i);
				if (Exx_i==null) {
					System.out.println("compare_matlab_expectation_tv_compute_Ex_Exx: skipping index " + i);
				}
				else
				{
					int m = Exx_i.getRowDimension();
					int n = Exx_i.getColumnDimension();
					double [][] xx_matlab = new double [m][n];
					for (int r =0; r<m;  r++)
						for (int c=0;c<n; c++)
							xx_matlab[r][c] = Exx_matlab[r][c][i];

					//TEST.compare_matrices("\tExx ["+ i + "] : ", Exx_i, xx_matlab);
				}

			}
					
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MATLAB.closeSperecMatlabConnection(eng);
	}
	
	
	
	@Override
	protected void expectation_tv(MyMatrix T, BWStatistics bw, double S[], int tv_dim, int nd, int fd, int nworkers, int [] idx_sv) throws Exception {
		
		super.expectation_tv(T, bw, S, tv_dim, nd, fd, nworkers, idx_sv);
		
		MyMatrix n = bw.N.copy();
		MyMatrix f = bw.F.copy();
		assert(n.getVectorDim()==VectorDim.ROW_VECTORS);  
		assert(f.getVectorDim()==VectorDim.ROW_VECTORS); 
		
		compare_matlab_expectation_tv(T, n, f, S, tv_dim, nd, fd, nworkers, idx_sv);
	}
	
	/**
	 * 
	 * @param T
	 * @param n
	 * @param f
	 * @param S
	 * @param tv_dim
	 * @param nd
	 * @param fd
	 * @param nworkers
	 * @param idx_sv
	 */
	private void compare_matlab_expectation_tv(MyMatrix T, MyMatrix n_, MyMatrix f_, double [] S, int tv_dim, int nd, int fd, int nworkers, int [] idx_sv) {
		// Adjust indexing for matlab
		int [] idx_sv_ = new int [ idx_sv.length];
		for (int i = 0; i<idx_sv_.length; i++)
			idx_sv_[i] = idx_sv [i]+1;
		// The matlab script need this format
		MyMatrix n = null;
		MyMatrix f = null;
		
		assert(f_.getVectorDim()==VectorDim.ROW_VECTORS);
		assert(n_.getVectorDim()==VectorDim.ROW_VECTORS);
		f = f_.copy();
		n = n_.copy();
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		try {
			Struct res_Matlab = eng.feval("my_expectation_tv", T.getArray(), n.getArray(), f.getArray(), S, tv_dim, nd, fd, nworkers, idx_sv_);
			double [][] RU_Matlab = (double[][]) res_Matlab.get("RU");
			Object LU_Matlab = res_Matlab.get("LU");
			System.out.println("Comparing against matlab: expectation_tv(...) = " + TEST.compare_matrices(RU, RU_Matlab) );
			//System.out.println("Comparing against matlab: expectation_tv(...) = " + testDebug.compare_matrices(LU.get(1), matlab_T) );
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MATLAB.closeSperecMatlabConnection(eng);
	}
	
	
	/****************************************************************************
	 * I-VECTOR EXTRACION METHODS (overridden)
	 ****************************************************************************/
	
	@Override
	protected MyMatrix extract_single_ivector(MyMatrix T_invS, MyMatrix n_file, Matrix f_file, MyMatrix Ttransp, int [] idx_sv_) {
		
		MyMatrix res_Java = super.extract_single_ivector(T_invS, n_file, f_file, Ttransp, idx_sv_);
		
		/*	L = I +  bsxfun(@times, T_invS, N(idx_sv)') * T';
		B = T_invS * F;
		x = pinv(L) * B; % Can crash due to Inf and/or Nan inside L
	   */
		int [] idx_sv = new int [idx_sv_.length];
		for (int i=0; i<idx_sv.length; i++)
			idx_sv[i] = idx_sv_[i] + 1; // due to the different indexing between Java and Matlab
		
		double [] res_Matlab = MATLAB.extract_single_ivector_testJava(n_file, f_file, Ttransp, T_invS, idx_sv);
		
		if (res_Java!=null)
		{
			//TEST.compare_vectors("extract_single_ivector: \t", res_Java.transpose().getArray()[0], res_Matlab);
		}
		
		return res_Java;
		
	}

}
