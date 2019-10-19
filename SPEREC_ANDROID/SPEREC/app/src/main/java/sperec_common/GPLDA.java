package sperec_common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;
import myMath.MySingularValueDecomposition;
import myMath.MySort;
import myMath.MySort.SortDir;

public class GPLDA {
	
	public GPLDA_Models getModels() {
		GPLDA_Models g = new GPLDA_Models();
		g.nphi = nphi;
		g.Phi = Phi;
		g.Sigma = Sigma;
		g.W = W;
		g.Mcol = Mcol;
		
		//% Sigona: optimization
		g.Lambda = Lambda;
		g.Uk = Uk; //% Matrix
		g.Q_hat = Q_hat; //% Matrix
		
		return g;
	}
	
	private boolean debug = false;
	public void debug(boolean d) {debug = d;}
	public boolean debug() { return debug;}
	
	private int nphi;
	private MyMatrix Phi;
	private MyMatrix Sigma;
	private MyMatrix W;
	private double [] Mcol;
	
	//% Sigona: optimization
	private MyMatrix Lambda;
	private MyMatrix Uk; //% Matrix
	private MyMatrix Q_hat; //% Matrix
	
	private MyMatrix Uk_tr; // Uk'
	private MyMatrix Wtr;
	
	private MyMatrix Ey = null; //internal purpose
	private MyMatrix Eyy = null;
		
	/**
	 * 
	 * @param nphi
	 */
	public GPLDA(int nphi) {
		this.nphi = nphi;
		Phi = null;
		Sigma = null;
		W = null;
		Mcol = null;
		
		Lambda = null;
		Uk = null;
		Q_hat = null;
		
		Ey = null;
		Eyy = null;
	}
	
	/**
	 * 
	 * @param g
	 */
	public GPLDA(GPLDA_Models g) {
		
		Phi = g.Phi;
		assert(Phi.getVectorDim()==VectorDim.ROW_VECTORS); // Phi is ndim x nphi
		nphi = Phi.getColumnDimension();
		
		Sigma = g.Sigma;
		W = g.W;
		Wtr = W.transpose();
		
		Mcol = g.Mcol;
		
		Lambda = g.Lambda;
		Q_hat = g.Q_hat;
		Uk = g.Uk;
		Uk_tr = Uk.transpose();
		
		// Init
		Ey = null;
		Eyy = null;
	}
	
	public MyMatrix getPhi() {
		return Phi;
	}
	public MyMatrix getSigma() {
		return Sigma;
	}
	public MyMatrix getW() {
		return W;
	}
	public double [] getM() {
		return Mcol;
	}
		
	public void init(int ndim) {
		
		if (Phi==null) // If never inizialized...
		{ 
			Sigma = MyMatrix.randn(ndim, ndim, 100.0); 
			Phi = MyMatrix.randn(ndim, nphi, 1.0);
		}
	}
	/**
	 * 
	 * @param Data_: each row of the matrix is an i-vector. So, the rowDimension is the number of the i-vectors, while the columDimension is the dimensionality of i-vectors
	 * @param spk_labs
	 * @param niter
	 * @throws Exception 
	 */
	public GPLDA_Models estimate(MyMatrix Data, Short [] spk_labs_, int niter) throws Exception {
				
		assert(Data.getVectorDim()==VectorDim.COLUMN_VECTORS);
		Short [] spk_labs = spk_labs_.clone(); // I do not want to change input data

		int ndim = Data.getRowDimension(); // [ndim, nobs] = size(data)
		int nobs = Data.getColumnDimension();
		
		if (nobs != spk_labs.length )
			throw new IllegalArgumentException("Number of data samples should match the number of labels.");

		//% [spk_labs, I] = sort(spk_labs);  % make sure the labels are sorted
		int [] sortedIndexes = MySort.sort_and_replace(spk_labs, SortDir.ASCENDING); // Now spk_labs are sorted 
		/*if (debug) {
			Struct res = MATLAB.my_sort(spk_labs);
			double normInf_I = testDebug.compare_int_double_arrays(MATLAB.toMatlabIndexesCopy(sortedIndexes), (double [])res.get("I"));
			double normInf_labs = testDebug.compare_short_arrays(spk_labs, (short [])res.get("B"));
			res = null;
		}*/
		
		MyMatrix SortedData = Data.getMatrix(0, Data.getRowDimension()-1, sortedIndexes); //data = data(:, I)
		
		//[~, ia, ic] = unique(spk_labs, 'stable');
		//spk_counts = histc(ic, 1 : numel(ia)); % # sessions per speaker
		double [] spk_count = new MyAccumulator().count_unique_stable(spk_labs);
			
		Mcol = SortedData.mean(2); // M = mean(data, 2);
		SortedData.minusEqualColumnVector(Mcol); // data = bsxfun(@minus, data, M); % centering the data
		// Ora SortedData e' anche "centered"
		
		/*if (debug) {
			double [][] res = MATLAB.length_norm(SortedData);
			MyMatrix SortedDataNormed = SortedData.normRows();
			double normInf = testDebug.compare_matrices(SortedDataNormed, res);
			res = null;
		}*/
		SortedData = SortedData.normRows(); //data = length_norm(data); % normalizing the length: data = bsxfun(@rdivide, data, sqrt(sum(data.^2)));
		
		//Ora, ogni colonna di A (i.e. ogni i-vector) ha modulo 1
		
		MyMatrix W1 = SortedData.transpose().cov().calc_white_mat(this.debug); 		//W1   = calc_white_mat(cov(data'));
		
		SortedData = W1.transpose().times(SortedData);		//data = W1' * data; % whitening the data
		SortedData.setVectorDim(VectorDim.COLUMN_VECTORS);
		
		System.out.println("\n\nRandomly initializing the PLDA hyperparameters ...\n\n");
		
		//[s1, s2] = RandStream.create('mrg32k3a', 'NumStreams', 2);
		// Sigma = 100 * randn(s1, ndim); % covariance matrix of the residual term
		//Phi = randn(s2, ndim, nphi); % factor loading matrix (Eignevoice matrix)
		init(ndim);
		//Sigma = MyMatrix.randn(ndim, ndim, 100.0); 
		//Phi = MyMatrix.randn(ndim, nphi, 1.0);
		
		Phi.minusEqualColumnVector(Phi.mean(2)); // Phi = bsxfun(@minus, Phi, mean(Phi, 2));
		
		MyMatrix W2 = Phi.transpose().times(Phi).calc_white_mat(this.debug); 		//W2   = calc_white_mat(Phi' * Phi);

		Phi = Phi.times(W2); // Phi = Phi * W2; % orthogonalize Eigenvoices (columns)
		
		System.out.println("Re-estimating the Eigenvoice subspace with " + nphi + " factors ...\n");
		
		for (int iter=0; iter<niter; iter++) {
			System.out.println("EM iter#: " + iter + " \t");
														//% expectation
			expectation_plda(SortedData, spk_count);	//[Ey, Eyy] = expectation_plda(data, Phi, Sigma, spk_counts);
			
			/*if (debug) {
				Struct res = MATLAB.expectation_plda(SortedData, Phi, Sigma, spk_count);
				double normInf_Ey = testDebug.compare_matrices(Ey, (double [][])res.get("Ey"));
				double normInf_Eyy = testDebug.compare_matrices(Eyy, (double [][])res.get("Eyy"));
				res = null;
			}*/
			
														//	% maximization
			maximization_plda(SortedData);	//throws...			//	[Phi, Sigma] = maximization_plda(data, Ey, Eyy);;
		}
		
		W = W1;
				
		// Go on with my optimization
		score_gplda_trials_STAGE_1();
		
		GPLDA_Models gplda_models = this.getModels();
		return gplda_models;
	}
	
	/**
	 * These ops were located in a scoring function, but actually they do not depend on the data to be scored.
	 * So I have extracted and put them in this method, to use later in a cross-validation experiment.
	 */
	private void score_gplda_trials_STAGE_1() {
		
		int nphi = Phi.getRowDimension();	//VERY STRANGE, I THINK MSR MADE A MISTAKE ==>		// nphi = size(Phi, 1);
		
		MyMatrix Sigma_ac = Phi.times(Phi.transpose()); 			// Sigma_ac  = Phi * Phi';
		MyMatrix Sigma_tot = Sigma_ac.plus(Sigma); 					// Sigma_tot = Sigma_ac + Sigma;

		MyMatrix Sigma_tot_i = Sigma_tot.inverse(); 				// Sigma_tot_i = pinv(Sigma_tot);
		//Sigma_i = pinv(Sigma_tot-Sigma_ac*Sigma_tot_i*Sigma_ac);
		MyMatrix Sigma_i = Sigma_tot.minus(Sigma_ac.times(Sigma_tot_i).times(Sigma_ac)).inverse();
		
		MyMatrix Q = Sigma_tot_i.minus(Sigma_i); 					// Q = Sigma_tot_i-Sigma_i;
		MyMatrix P = Sigma_tot_i.times(Sigma_ac).times(Sigma_i);	// P = (Sigma_tot_i * Sigma_ac) * Sigma_i;

		// [U, S] = svd(P);
		MySingularValueDecomposition svd = P.svd();//false);
		MyMatrix U = svd.getU();
		MyMatrix S = svd.getS();
		
		// S = diag(S); Lambda = diag(S(1 : nphi));
		Lambda = S.getMatrix(0,  nphi-1,  0, nphi-1);									
		
		Uk = U.getMatrix(0,  U.getRowDimension()-1,  0, nphi-1); // Uk     = U(:, 1 : nphi);
		Uk_tr = Uk.transpose();
		
		Q_hat = Uk_tr.times(Q).times(Uk);			// Q_hat  = Uk' * Q * Uk;
	}
	
	
	/**
	 * % computes the posterior mean and covariance of the factors
	 * @param data
	 * @param spk_counts
	 * @throws Exception 
	 */
	private void expectation_plda(MyMatrix data, double [] spk_counts) throws Exception {
		// Data_ has column vectors, as the MSR matlab code.
		// The same convention holds for the other matrices here.
	
		// nphi     = size(Phi, 2);
		int nsamples = data.getColumnDimension(); // nsamples = size(data, 2);
		double nspks = spk_counts.length; // nspks    = size(spk_counts, 1);

		Ey  = new MyMatrix(nphi, nsamples, 0); 		//Ey  = zeros(nphi, nsamples);
		Eyy = new MyMatrix(nphi, nphi, 0); 			//Eyy = zeros(nphi);
		
		ArrayList<Double> uniqFreqs = new MyAccumulator().get_unique_arraylist(spk_counts); 		//uniqFreqs  	  = unique(spk_counts); // 'sorted' is deafult

		int nuniq = uniqFreqs.size(); 		// nuniq 		  = size(uniqFreqs, 1);

		ArrayList<MyMatrix> invTerms = new ArrayList<MyMatrix>(nuniq); // invTerms      = cell(nuniq, 1);
		for (int i=0; i<nuniq; i++) 		// invTerms(:)   = {zeros(nphi)};			
			invTerms.add(null); // no need to fill with zeros
		
		// Devo fare: PhiT_invS_Phi = ( Phi'/Sigma ) * Phi;
		// Considero Phi'/Sigma che equivale a (Sigma'\Phi'')' i.e. (Sigma'\Phi)'
		// Qui P\Q equivale a P.solve(Q)
		// quindi si ottienere (Sigma'.solve(Phi))'
		// ed infine:
		// Sigma.transpose().solve(Phi).tranpose();
		
				
	    // QUindi: PhiT_invS_Phi = ( Phi'/Sigma ) * Phi
		// significa
		MyMatrix PhiT_invS_Phi = null;
		try {
			PhiT_invS_Phi = Sigma.transpose().solve(Phi).transpose().times(Phi);
		} catch (Exception e) {
			System.out.println("GPLDA.expectation_plda: Exception: " + e.getMessage());
			throw e;
		}
		
		MyMatrix I = MyMatrix.identity(nphi,  nphi); //I = eye(nphi)
		
		MyMatrix Cyy = null;
		MyMatrix nPhiT_invS_Phi = null;
		for (int ix = 0; ix<nuniq; ix++) { 							//for ix = 1 : nuniq
			nPhiT_invS_Phi = PhiT_invS_Phi.times(uniqFreqs.get(ix)); 	//	nPhiT_invS_Phi = uniqFreqs(ix) * PhiT_invS_Phi;
			Cyy = I.plus(nPhiT_invS_Phi).inverse(); 				//	Cyy =  pinv(I + nPhiT_invS_Phi);
			/*if (debug) {
				double [][] res = MATLAB.pinv(I.plus(nPhiT_invS_Phi));
				double normInf = testDebug.compare_matrices(Cyy, res);
				res = null;
			}*/
			invTerms.set(ix, Cyy);    								//	invTerms{ix} = Cyy;
		}
		//end
		try {
			data = Sigma.solve(data);									//data = Sigma\data;
		} catch (Exception e) {
			System.out.println("GPLDA.expectation_plda: Exception: " + e.getMessage());
			throw e;
		}
		int cnt = 1; //	cnt  = 1;
		double nsessions;
		MyMatrix Data = null;
		MyMatrix Ey_spk = null;
		MyMatrix PhiT_invS_y = null;
		MyMatrix Eyy_spk = null;
		MyMatrix Phi_transp = Phi.transpose();
		for (int spk=0; spk<nspks; spk++) { 							//for spk = 1 : nspks
		    nsessions = spk_counts[spk];								//	nsessions = spk_counts(spk);
		    // Speaker indices
		    int [] idx = new int[ (int) spk_counts[spk] ];				//	idx = cnt : ( cnt - 1 ) + spk_counts(spk);
		    for (int ii=0; ii< spk_counts[spk]; ii++)
		    	idx[ii] = cnt + ii-1; // The -1 is because idx will be used as index, and I need to start from zero when in matlab starts from 1
		    cnt += spk_counts[spk];										// cnt  = cnt + spk_counts(spk);
		    Data = data.getMatrix(0, data.getRowDimension()-1, idx); 		//   Data = data(:, idx);
		    PhiT_invS_y = Phi_transp.times(Data).sum(2); //PhiT_invS_y = MyMatrix.fromColumnVector(Phi_transp.times(Data).sum(2)); //	 PhiT_invS_y = sum(Phi' * Data, 2);
		    Cyy = invTerms.get(uniqFreqs.indexOf(nsessions));			//	 Cyy = invTerms{ uniqFreqs == nsessions };
		    Ey_spk = Cyy.times(PhiT_invS_y); 	// e' un vettore colonna	//   Ey_spk  = Cyy * PhiT_invS_y;
		    Eyy_spk = Cyy.plus(Ey_spk.times(Ey_spk.transpose())); 		//	Eyy_spk = Cyy + Ey_spk * Ey_spk';
		    Eyy.plusEquals(Eyy_spk.times(nsessions));					//     Eyy     = Eyy + nsessions * Eyy_spk;
		    
		    for (int c=0; c<idx.length; c++)							//     Ey(:, idx) = repmat(Ey_spk, 1, nsessions);
		    	Ey.setMatrix(0, Ey.getRowDimension()-1, idx[c], idx[c], Ey_spk);
		}

	}
	
	/**
	 * % ML re-estimation of the Eignevoice subspace and the covariance of the
	 * % residual noise (full).
	 * @param data
	 * @throws Exception 
	 */
	private void maximization_plda(MyMatrix data) throws Exception {
		
		if (data.getVectorDim()!=VectorDim.COLUMN_VECTORS)
			throw new Exception("maximization_plda: invalid data format");
				
		int nsamples = data.getColumnDimension(); //nsamples = size(data, 2);
		MyMatrix dataTransp = data.transpose();
		
		MyMatrix Data_sqr = data.times(dataTransp); 	// Data_sqr = data * data';

		// Phi      = data * Ey' / (Eyy)
		MyMatrix tmp = data.times(Ey.transpose());
		// turns into : tmp / Eyy, which corresponds to (Eyy'\tmp')'
		Phi = Eyy.transpose().solve(tmp.transpose()).transpose();
		
		Sigma = Data_sqr.minus(Phi.times(Ey).times(dataTransp)).times(1.0/nsamples); //Sigma = 1/nsamples * (Data_sqr - (Phi * Ey) * data');
	}
	
	
	/**
	 * % computes the verification scores as the log-likelihood ratio of the same 
% versus different speaker models hypotheses.
%
% Inputs:
%   plda            : structure containing PLDA hyperparameters
%   model_iv        : data matrix for enrollment i-vectors (column observations)
%   test_iv         : data matrix for test i-vectors (one observation per column)
%
% Outputs:
%    scores         : output verification scores matrix (all model-test combinations)
%
% References:
%   [1] D. Garcia-Romero and C.Y. Espy-Wilson, "Analysis of i-vector length 
%       normalization in speaker recognition systems," in Proc. INTERSPEECH,
%       Florence, Italy, Aug. 2011, pp. 249-252.
%
% Omid Sadjadi <s.omid.sadjadi@gmail.com>
% Microsoft Research, Conversational Systems Research Center

	 * @param model_iv
	 * @param test_iv
	 */
	public MyMatrix score_gplda_trials_STAGE_2(MyMatrix model_iv, MyMatrix test_iv) {
		// Requires:
		// 	this.Mcol
		//	this.Wtr
		//	this.Uk_tr
		//	this.Q_hat
		//	this.Lambda
		//
		// Anziche W serve W' e anziche Uk serve Uk. OTTIMIZZARE! TODO
		// Pero' vedi che dopo ci soni altri trasposti, quindi...
		//
		// Stage 1 of scoring already performed
				
		//W       = plda.W;
		//M       = plda.M;
		//Lambda = plda.Lambda;
		//Uk     = plda.Uk;
		//Q_hat  = plda.Q_hat;

		//%%%%% post-processing the model i-vectors %%%%%
		model_iv = model_iv.minusColumnVector(Mcol);	// model_iv = bsxfun(@minus, model_iv, M); % centering the data
		model_iv = model_iv.normRows(); 				// model_iv = length_norm(model_iv); % normalizing the length
		model_iv = Wtr.times(model_iv);					// model_iv = W' * model_iv; % whitening data

		//%%%%% post-processing the test i-vectors %%%%%
		test_iv = test_iv.minusColumnVector(Mcol);		// test_iv = bsxfun(@minus, test_iv, M); % centering the data
		test_iv = test_iv.normRows();					// test_iv = length_norm(test_iv); % normalizing the length
		test_iv = Wtr.times(test_iv);					// test_iv  = W' * test_iv; % whitening data

		model_iv = Uk_tr.times(model_iv);	// model_iv = Uk' * model_iv;
		test_iv  = Uk_tr.times(test_iv);	// test_iv  = Uk' * test_iv;

		// score_h1 = diag(model_iv' * Q_hat * model_iv);
		double [] score_h1 = model_iv.transpose().times(Q_hat).times(model_iv).getDiag(); // column vector
		double [] score_h2 = null;
		try {
			score_h2 = test_iv.transpose().times(Q_hat).times(test_iv).getDiag(); // column vector // score_h2 = diag(test_iv' * Q_hat * test_iv);
		}
		catch (Exception e) {
			throw e;
		}
		MyMatrix score_h1h2 = model_iv.transpose().times(Lambda).times(test_iv).times(2.0); //score_h1h2 = 2 * model_iv' * Lambda * test_iv;

		MyMatrix scores = score_h1h2.plusColumnVector(score_h1); // scores = bsxfun(@plus, score_h1h2, score_h1);
		scores = scores.plusRowVector(score_h2);  // scores = bsxfun(@plus, scores, score_h2');
		
		return scores;
		
	}
	
	
	/***************************************************************************
	 * I/O
	 * ************************************************************************/
	
	public static GPLDA readFromInputStream(InputStream is) throws IOException {
		
		GPLDA_Models g = GPLDA_Models.readFromInputStream(is);
		GPLDA gplda = new GPLDA(g);
		
		return gplda;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static GPLDA readFromFile(File file) throws IOException, ClassNotFoundException {
			
		InputStream is = null;
		if (file == null)
			is = System.in;
		else
			is = new FileInputStream(file);
				
		GPLDA gplda = GPLDA.readFromInputStream(is);
				
		is.close();
		return gplda;
	}
}
