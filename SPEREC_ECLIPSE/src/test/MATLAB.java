package test;


import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.matlab.types.Struct;

import Jama.Matrix;
import sperec_common.BWStatistics;
import sperec_common.FeaSpecs;
import sperec_common.GPLDA_Models;
import sperec_common.MixtureModel;
import sperec_common.Mixture_MSR;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import sperec_common.SPEREC_Specs;
import sperec_common.TVSpace_Models;

public class MATLAB {
	
	private static MatlabEngine eng = null;
	
	public static MatlabEngine getMatlabConnection() {
		
		if (eng==null)
			eng = newSperecMatlabConnection();
		
		return eng;
	}
	

	/**
	 * PINV (PSEUDO IVERSE)
	 * @param M
	 * @return
	 */
	public static double [][] pinv(MyMatrix M) {
		
		double [][] res = null;
		MatlabEngine eng = getMatlabConnection();
		try {
			res = (double [][])eng.feval("pinv",  M.getArray());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		closeSperecMatlabConnection(eng);
		return res;
	}

	/**
	 * 
	 * @param SortedData
	 * @param Phi
	 * @param Sigma
	 * @param spk_count
	 * @return
	 */
	public static Struct expectation_plda(MyMatrix SortedData, MyMatrix Phi, MyMatrix Sigma, double [] spk_count) {
		
		Struct res = null;
		MatlabEngine eng = getMatlabConnection();
		try {
			res = eng.feval("my_expectation_plda",  SortedData.getArray(), Phi.getArray(), Sigma.getArray(), spk_count);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		closeSperecMatlabConnection(eng);
		return res;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static double [][] length_norm(MyMatrix data) {
		
		MatlabEngine eng = getMatlabConnection();
		double [][] res = null;
		try {
			res = (double [][])eng.feval("length_norm",  data.getArray());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		closeSperecMatlabConnection(eng);
		return res;
	}

	/**
	 * [B, I] = sort(A)
	 * @param A
	 * @return
	 */
	public static Struct my_sort(short [] A) {
		
		MatlabEngine eng = getMatlabConnection();

		Struct res = null;
		try {
			res = eng.feval("my_sort",  A);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		closeSperecMatlabConnection(eng);
		return res;
	}
	//[spk_labs, I] = sort(spk_labs)

	/**
	 * On Matlab you need: matlab.engine.shareEngine
	 * @return
	 */
	private static MatlabEngine newSperecMatlabConnection() {
		String [] engines;
		MatlabEngine eng = null; 
		System.out.println("Searching for Matlab shared engines...");
		try {			
			engines = MatlabEngine.findMatlab();
			try {
				eng = MatlabEngine.connectMatlab(engines[0]);
			} catch (EngineException e) {
				System.out.println("EngineException!! " + e.getMessage());
			} catch (InterruptedException e) {
				System.out.println("InterruptedException!!");
			} catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("ArrayIndexOutOfBoundsException!!");
			}

		} catch (EngineException e) {
			e.printStackTrace();
		}
		if (eng != null) {
			System.out.println("Matlab shared engine found.");
		}
		assert(eng!=null);
		return eng;
	}

	/**
	 * 
	 * @param eng
	 */
	public static void closeSperecMatlabConnection(MatlabEngine eng) {
		/*try {
			eng.close();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	/**
	 * 
	 * @param in
	 * @return
	 */
	public static int [] toMatlabIndexesCopy(int [] in) {
		
		int [] out = new int [in.length];
		
		for (int i=0; i<in.length; i++)
			out[i] = in[i]+1;
		
		return out;
	}


	/**
	 * 
	 * @param Mcol
	 * @param Wtr
	 * @param Uk_tr
	 * @param Q_hat
	 * @param Lambda
	 * @param model_iv
	 * @param test_iv
	 * @return
	 */
	public static double score_gplda_trials_STAGE_2_testJava(double [] Mcol, MyMatrix Wtr, MyMatrix Uk_tr, MyMatrix Q_hat, MyMatrix Lambda, MyMatrix model_iv, MyMatrix test_iv) {
		
		double res = 0;;

		MatlabEngine eng = getMatlabConnection();

		try {
			res = eng.feval("score_gplda_trials_STAGE_2_testJava",  Mcol, Wtr.getArray(), Uk_tr.getArray(), Q_hat.getArray(), Lambda.getArray(), model_iv.getArray(), test_iv.getArray());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		closeSperecMatlabConnection(eng);
		return res;
	}

	/**
	 * Convert our ubm structure (i.e. the Mixture content) in a format that can be passed to my Matlab code (MSR based)
	 * @param ubm
	 * @return
	 */
	static public Struct UBM_to_GMM_MSR_MATLAB(MixtureModel ubm) {
		
		double [][] mu = ubm.getMuMatrix(VectorDim.COLUMN_VECTORS).getArray();
		double [][] sigma = ubm.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS).getArray();
		double [] w = ubm.getWMatrix(VectorDim.ROW_VECTORS).getArray()[0];

		Struct res = new Struct("mu", mu, "sigma", sigma, "w", w);		

		return res;
	}
	
	/**
	 * 
	 * @param post
	 * @param dim
	 * @return
	 */
	public static MyMatrix logsumexp_testJava(MyMatrix post, int dim) {
				
		MyMatrix M = null;
		MatlabEngine eng = getMatlabConnection();
		
		double [] res = null;
		try {
			res = (double [])eng.feval("logsumexp_testJava", post.getArray(), dim);
			M = MyMatrix.fromRowVector(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeSperecMatlabConnection(eng);
		
		return M;

	}
	

	/**
	 * 
	 * @param data
	 * @param mu
	 * @param sigma
	 * @param w
	 * @return
	 */
	public static MyMatrix lgmmprob_testJava(MyMatrix data, MyMatrix mu, MyMatrix sigma, MyMatrix w) {
		
		MyMatrix logprob = null;
		
		MatlabEngine eng = getMatlabConnection();

		double [][] res = null;
		try {
			res = (double [][])eng.feval("lgmmprob_testJava", data.getArray(), mu.getArray(), sigma.getArray(), w.getArray());
			logprob = new MyMatrix(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeSperecMatlabConnection(eng);

		return logprob;
	}
	
	/**
	 * 
	 * @param spkSessions
	 * @param ubm
	 * @return
	 */
	public static BWStatistics compute_bw_stats_testJava(ArrayList<MyMatrix> spkSessions, MixtureModel ubm) {

		BWStatistics bw = new BWStatistics();
		
		Struct res = null;
		Struct matlabUBM = MATLAB.UBM_to_GMM_MSR_MATLAB(ubm);

		int numSessions = spkSessions.size();

		MatlabEngine eng = getMatlabConnection();

		double [][] N = new double [numSessions][];
		double [][] F = new double [numSessions][];
		for (int s = 0; s<numSessions; s++)
		{
			MyMatrix feaMatrix = spkSessions.get(s);
			double [][] fea = feaMatrix.transpose().getArray(); // PFor MSR code, the fea must be [feaDim x numframes]
			try {
				res = eng.feval("compute_bw_stats_testJava",  fea, matlabUBM);
				F[s] = (double []) res.get("F");
				N[s] = (double []) res.get("N");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		bw.F = new MyMatrix(F, VectorDim.ROW_VECTORS);
		bw.N = new MyMatrix(N, VectorDim.ROW_VECTORS);

		closeSperecMatlabConnection(eng);
		
		return bw;

	}


	/**
	 * 
	 * @param N
	 * @param F
	 * @param Ttransp
	 * @param T_invS
	 * @param idx_sv
	 * @return
	 */
	public static double [] extract_single_ivector_testJava (MyMatrix N, Matrix F, MyMatrix Ttransp, MyMatrix T_invS, int [] idx_sv) {

		double[] v = null;

		MatlabEngine eng = MATLAB.getMatlabConnection();
		try {
			v = eng.feval("extract_single_ivector_testJava", N.getArray(), F.getArray(), Ttransp.getArray(), T_invS.getArray(), idx_sv);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		MATLAB.closeSperecMatlabConnection(eng);

		return v;
	}

	
	public static Mixture_MSR my_gmm_em_optimized_testJava(String feaDir, String dataList, int nmix, int niter, int ds_factor) {
		
		Mixture_MSR ubm = null;
		MatlabEngine eng = MATLAB.getMatlabConnection();
		Struct obj = null;

		try {
			int nworkers = 2;
			obj = eng.feval("my_gmm_em_optimized_testJava", feaDir, dataList, nmix, niter, ds_factor, nworkers);
			ubm = to_Mixture_MSR(obj);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return ubm;
	}
	
	private static Mixture_MSR to_Mixture_MSR(Object obj) {
		
		Struct s = (Struct)obj;
		double [][] mu = (double[][])s.get("mu");
		double [][] sigma = (double[][])s.get("sigma");
		double [] w = (double[])s.get("w");
		Mixture_MSR ubm = new Mixture_MSR(mu, sigma, w);
		
		return ubm;
	}
	
	public static double [][] calc_white_mat(MyMatrix M) {
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		double [][] W = null;
		
		try {
			W = (double[][])eng.feval("my_calc_white_mat", M.getArray());//, true);
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MATLAB.closeSperecMatlabConnection(eng);
		
		return W;
	}
	
	public static Struct svd(MyMatrix M) { //, boolean debug) {
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		//double [][] W = null;
		Struct MATLAB_svd = null;
		
		try {
			MATLAB_svd = eng.feval("my_svd_testJava", M.getArray()); //, debug);
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MATLAB.closeSperecMatlabConnection(eng);
		
		return MATLAB_svd;
	}

	static GPLDA_Models my_gplda_em(MyMatrix Data, Short [] spk_labs, int nphi, int niter, MyMatrix Sigma_init, MyMatrix Phi_init) {

		boolean debug = true;

		GPLDA_Models g = null;
	
		MatlabEngine eng = MATLAB.getMatlabConnection();
		//% V = lda(dev_ivs, spk_labs_for_plda);
		try {
			Struct matlab_plda = eng.feval("my_gplda_em_testJava", Data.getArray(), spk_labs, nphi, niter, Sigma_init.getArray() , Phi_init.getArray(), debug);
			g = to_GPLDA_Models(matlab_plda);
			
		} catch (RejectedExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MATLAB.closeSperecMatlabConnection(eng);
		return g;
	}
	
	
	static MATLAB_POPREF_MODEL popref_testjava(SPEREC_Specs sperecSpecs, FeaSpecs feaSpecs, String feaDir, String feaFileList, int nsess, MyMatrix tInit, boolean debug) throws Exception, Exception, Exception, Exception {
		
		MATLAB_POPREF_MODEL pop_matlab = new MATLAB_POPREF_MODEL();
		

		MatlabEngine eng = MATLAB.getMatlabConnection();
		Struct res = null;

		// popref_testjava(nmix, niter_ubm, tvdim, niter_tv, lda_dim, spk_labs_for_plda, nphi, niter_plda, feadir, dataList)
		res = eng.feval("popref_testjava", sperecSpecs.UBM.NMIX, sperecSpecs.UBM.niter_ubm, 
				sperecSpecs.TV.TVDIM ,sperecSpecs.TV.niter_tv,
				sperecSpecs.LDA.LDADIM, nsess,
				sperecSpecs.GPLDA.NPHI, sperecSpecs.GPLDA.niter_gplda,
				tInit.getArray(),
				debug, 
				feaDir, feaFileList);

		Object obj = null;

		pop_matlab.ubm = to_Mixture_MSR(res.get("ubm"));

		obj = res.get("TVSpace_Models");
		pop_matlab.tv = to_TVSpace_Models(obj);

		pop_matlab.V_LDA_redux = new MyMatrix((double [][])res.get("V_LDA_redux"));

		obj = res.get("plda");
		pop_matlab.gplda_models = to_GPLDA_Models(obj);
			
		pop_matlab.N = new MyMatrix((double [][])res.get("N"));
		pop_matlab.F = new MyMatrix((double [][])res.get("F"));
		
		pop_matlab.dev_ivs = new MyMatrix((double[][])res.get("dev_ivs"));
		pop_matlab.dev_ivs_redux = new MyMatrix((double[][])res.get("dev_ivs_redux"));
		
		return pop_matlab;
	}
	
	private static TVSpace_Models to_TVSpace_Models(Object obj) {
		
		Struct s = (Struct)obj;
		double [][] T = (double[][])s.get("T");
		double [][] T_invS = (double[][])s.get("T_invS");
		double [] idx_sv_d = (double[])s.get("idx_sv");
		int [] idx_sv = new int[idx_sv_d.length];
		for (int i =0; i<idx_sv_d.length; i++)
			idx_sv[i] = (int)idx_sv_d[i];
		
		TVSpace_Models tv = new TVSpace_Models();
		tv.T = new MyMatrix(T);
		tv.T_invS = new MyMatrix(T_invS);
		tv.idx_sv = idx_sv;
		
		return tv;
	}
	
	private static GPLDA_Models to_GPLDA_Models(Object obj) {
		
		Struct matlab_plda = (Struct)obj;
		
		double [][] matlab_phi = (double [][])matlab_plda.get("Phi");
		double [][] matlab_sigma = (double [][])matlab_plda.get("Sigma");
		double [] matlab_M = (double [])matlab_plda.get("M");
		double [][] matlab_W = (double [][])matlab_plda.get("W");

		double [][] matlab_Lambda = (double [][])matlab_plda.get("Lambda");
		double [][] matlab_Uk = (double [][])matlab_plda.get("Uk");
		double [][] matlab_Q_hat = (double [][])matlab_plda.get("Q_hat");

		GPLDA_Models g = new GPLDA_Models();
		g.Phi = new MyMatrix(matlab_phi);
		g.Sigma = new MyMatrix(matlab_sigma);
		g.Mcol = matlab_M;
		g.W = new MyMatrix(matlab_W);
		g.Lambda = new MyMatrix(matlab_Lambda);
		g.Uk = new MyMatrix(matlab_Uk);
		g.Q_hat = new MyMatrix(matlab_Q_hat);
		
		return g;
	}
	
	public static MyMatrix testTV_testJava(MixtureModel ubm, BWStatistics bw, MyMatrix Tinit_for_debug, int niter, int nworkers) throws Exception, EngineException, InterruptedException, ExecutionException {
		
		MatlabEngine eng = MATLAB.getMatlabConnection();
		
		//S = reshape(ubm.sigma, ndim * nmix, 1);
		int fd = ubm.getFeatureDimension();
		int nd = ubm.getNumberOfComponents();
		double [] S = ubm.getSigmaDiagMatrix(VectorDim.COLUMN_VECTORS).reshape(fd*nd, 1).transpose().getArray()[0];
		
		int tv_dim = Tinit_for_debug.getRowDimension();
		
		double[][] matlab_T = eng.feval("my_train_tv_space_N_F_S_Tinit_testJava", 
				bw.N.getArray(), bw.F.getArray(), S, tv_dim, nd, fd, niter, nworkers, Tinit_for_debug.getArray());
		
		MATLAB.closeSperecMatlabConnection(eng);
		
		MyMatrix T = new MyMatrix(matlab_T);
		return T;
	}
	
	public static MyMatrix my_lda_testJava(MyMatrix M, Short [] classes) {
		
		MyMatrix V = null;
		MatlabEngine eng = MATLAB.getMatlabConnection();
		try {
			double[][] dV = eng.feval("my_lda_testJava", M.getArray(), classes); //, debug);
			V = new MyMatrix(dV);
			
		} catch (RejectedExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MATLAB.closeSperecMatlabConnection(eng);	
		
		return V;
	}
}
