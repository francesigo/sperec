package sperec_common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.gson.Gson;

import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;

public class Mixture_MSR implements MixtureModel {
		
	/**
	 * feaDim x nmix: the i-th column of the matrix mu is the mean vector of the i-th component
	 */
	MyMatrix mu;
	
	/**
	 * feaDim x nmix: the i-th column of the matrix sigma stores the diagonal values of the (diagonal) covariance matrix of the i-th component
	 */
	MyMatrix sigma;
	
	/**
	 * Weights of components: nmix x 1
	 */
	MyMatrix w;
	
	/**
	 * General mean vector of the data used during training
	 */
	private MyMatrix gm = null;
	
	/**
	 * General variances of the data used during training
	 */
	private MyMatrix gv = null;
	
	/**
	 * Number of components (number of densities)
	 */
	int numberOfComponents; // nd, aka nmix
	
	/**
	 * 
	 */
	int featureDimension; //fd
	
	//Baum-Welch statistics
	BWStatistics bwstats = null;
	
	private MyMatrix mPOST = null;
	// inutile private MyMatrix mLLK = null;
	//------------------------ CACHES VALUE USEFUL TO SAVE TIME DURING COMPUTATION (see the method "calc_optimization")
	MyMatrix one_dot_slash_sigma_tr = null;
	private MyMatrix mu_dot_slash_sigma = null;
	MyMatrix C = null;
	MyMatrix mu_dot_slash_sigma_tr = null;
	
	/**
	 * Constructor
	 * @param nmix
	 */
	public Mixture_MSR() {
				
		gm = null;
		gv = null;
		numberOfComponents = 0;
		featureDimension = 0;
		bwstats = null;
		mPOST = null;
		// Inutile mLLK = null;
		
		one_dot_slash_sigma_tr = null;
		mu_dot_slash_sigma = null;
		C = null;
		mu_dot_slash_sigma_tr = null;
	}
	
	public Mixture_MSR(double [][] mu, double [][]sigma, double[] w) {
		
		this();
		this.mu = new MyMatrix(mu, VectorDim.COLUMN_VECTORS);
		this.sigma = new MyMatrix(sigma, VectorDim.COLUMN_VECTORS);
		this.w = MyMatrix.fromColumnVector(w);
		this.numberOfComponents = this.mu.getRowDimension();
		this.featureDimension = this.mu.getColumnDimension();		
	}
	
	@Override
	public int getNumberOfComponents() {
		return numberOfComponents;
	}
	@Override
	public int getFeatureDimension() {
		return featureDimension;
	}
	@Override
	public boolean diagonal() {
		return true;
	}
	
	
	
	/**
	 * The true constructor: the UBM model is trained
	 * @param dataList
	 * @param niter
	 * @throws Exception
	 */
	public void train(ArrayList<MyMatrix> dataList, int nmix, int niter) throws Exception {

		// Ottobre 2018 ArrayList<MyMatrix> dataList2 = new ArrayList<MyMatrix>();
		// Ho riscontrato che, con i prevad, la memoria puo' non bastare (outOfMemory), quindi rinuncio al precalcolo
		// che in effetti non era necessario
		
		// Column vectors are preferred. So be sure that all the feature matrices are in the desider format
		asColumnVectorsMatrix(dataList);
		
		// Since alla the matrices are now in columnVector format, the following holds:
		featureDimension = dataList.get(0).getRowDimension();
			
		System.out.println("Mixture_MSR.train: Initializing the GMM hyperparameters ...");
		
		// Compute and store the global mean (gm) and global variance (gv)
		comp_gm_gv(dataList);
		
		// Initizialize mean and variance as gm and gv
		gmm_init(gm, gv);
		
		// Initialize temporary Baum-Welch statistics
		BWStatistics tempbw = new BWStatistics();
		
		// Generic matrix of feature vectors
		MyMatrix feaMatrix = null; 	
		
		// The number of available utterances (each one being a feature matrix)
		int nfiles = dataList.size();
		
		int mix = 1;
		// inutile int ds_factor = 1; // Constant
		
		// Loop while the current number of components is not greater than the desired one
		while (mix<=nmix)
		{
			this.numberOfComponents = mix; // Sigona July 2019: update the number of components
			System.out.println("Mixture_MSR.train: Re-estimating the GMM hyperparameters for " + mix + " components ...");
			for (int iter = 1; iter<=niter; iter++)
			{
				System.out.println("Mixture_MSR.train: Em iter #:" + iter); //+ " - gmm = " + this.toString());

				// inutile double L = 0;
				// inutile int nframes = 0;
				
				for (int ix = 0; ix<nfiles; ix++)
				{
					//Ottobre2018 expectation(dataList.get(ix), dataList2.get(ix), tempbw); // [n, f, s, l]  = ... The output here are the global mLLK and tempbw content
					feaMatrix = dataList.get(ix);
					MyMatrix feaMatrix2 = feaMatrix.arrayTimes(feaMatrix);
					expectation(feaMatrix, feaMatrix2, tempbw); // [n, f, s, l]  = ... The output here are the global mLLK and tempbw content
					if (ix==0)
					{
						bwstats = tempbw.copy();
						// Inutile L = mLLK.sum(2).get(0, 0);
					}
					else
					{
						bwstats.plusEquals(tempbw); // Accumula le bwstats
						// Inutile L += mLLK.sum(2).get(0, 0);
					}
					//N = N + n; F = F + f; S = S + s; L = L + sum(l);
					//nframes += mLLK.getRowDimension(); // Contollare, ma inutile
				}
				//System.out.println("Before maximization: " + this.toString());
			    maximization(bwstats.N, bwstats.F, bwstats.S);
			    
			    //System.out.println("Mixture_MSR.train: Em iter #:" + iter + " (END)  - gmm = " + this.toString());
			}
			
			if (mix<nmix) // if ( mix < nmix ), gmm = gmm_mixup(gmm); end
				gmm_mixup();
				
			// The next iteration will train 2 * times the current number of components:
			// 1 --> 2 --> 4 --> 8 --> 16 etc.
			mix = mix * 2;
		}
		
		//numberOfComponents = nmix; // Sigona July 2019: no more required
	}
	
	/**
	 * Computes the global mean and variance of data
	 * @param data
	 */
	private void comp_gm_gv(ArrayList<MyMatrix> data) { // function [gm, gv] = comp_gm_gv(data)

		// The number of feature matrices
		int nfiles = data.size();
		
		// Sum the number of columns of all the matrices
		int nframes = 0;

		// 1. Compute gm (global mean)
		// Initialize as a single column vector [0; 0; 0; .. 0], fd x 1
		MyMatrix gm = new MyMatrix(featureDimension, 1, 0.0, VectorDim.COLUMN_VECTORS); //gm.setVectorDim(VectorDim.COLUMN_VECTORS);

		for (int i = 0; i<nfiles; i++)
		{
			MyMatrix feaMatrix = data.get(i);
			nframes += feaMatrix.getColumnDimension();
			gm.plusEquals(feaMatrix.sum(2)); // Accumulate the sum of the elements in the horizontal directions
		}
		// The divide by the number of columns
		gm = gm.times(1.0/(double)nframes);

		// 2. Compute gv (global variance)
		MyMatrix gv = new MyMatrix(featureDimension, 1, 0.0, VectorDim.COLUMN_VECTORS); // gv.setVectorDim(VectorDim.COLUMN_VECTORS);
		for (int i = 0; i<nfiles; i++)
		{	// For each matrix, subtract the global mean
			MyMatrix feaMatrix = data.get(i);
			MyMatrix t = feaMatrix.minusColumnVector(gm);
			gv.plusEquals(t.arrayTimes(t).sum(2)); // and accumulate the sum of squares
		}
		// The divide by N-1 (estimate of the Variance)
		gv = gv.times(1.0/(double)(nframes-1));

		/*
			nframes = cellfun(@(x) size(x, 2), data, 'UniformOutput', false);
			nframes = sum(cell2mat(nframes));
			gm = cellfun(@(x) sum(x, 2), data, 'UniformOutput', false);
			gm = sum(cell2mat(gm'), 2)/nframes;
			gv = cellfun(@(x) sum(bsxfun(@minus, x, gm).^2, 2), data, 'UniformOutput', false);
			gv = sum(cell2mat(gv'), 2)/( nframes - 1 );
		 */

		// Set global mean and global variances
		this.gm = gm;
		this.gv = gv;
	}
	
	/**
	 * initialize the GMM hyperparameters (Mu, Sigma, and W)
	 * @param glob_mu
	 * @param glob_sigma
	 */
	void gmm_init(MyMatrix glob_mu, MyMatrix glob_sigma) {
		mu    = glob_mu.copy();
		sigma = glob_sigma.copy();
		int currentNumberOfComponents = mu.getRowDimension();
		w     = new MyMatrix(currentNumberOfComponents, 1, 1.0);
		w.setVectorDim(VectorDim.COLUMN_VECTORS);
		
		calc_optimization();
	}
	
	/**
	 * Compute the sufficient statistics
	 * @param feaMatrix
	 * @param feaMatrix2 = feaMatrix.arrayTimes(feaMatrix)
	 * @param ubm
	 * @param N
	 * @param F
	 */
	private void expectation (MyMatrix feaMatrix, MyMatrix feaMatrix2, BWStatistics bwout) {

		// Compute mPOST and mLLK
		mPOST = postprob(feaMatrix, feaMatrix2); // post = postprob(data, gmm.mu, gmm.sigma, gmm.w(:)); // post must be nmix x numFrames
		MyMatrix postTr = mPOST.transpose();

		bwout.N = mPOST.sum(2).transpose(); // N = sum(post, 2); N must be nmix x 1;  ATTENZIONE: NEL FILE compute_bw_stats
		
		bwout.F = feaMatrix.times(postTr); // F = data * post'; F must be feaDim x nmix
		bwout.F.setVectorDim(VectorDim.COLUMN_VECTORS);
		
		bwout.S = feaMatrix2.times(postTr);//S = (data .* data) * post';
		bwout.S.setVectorDim(VectorDim.COLUMN_VECTORS);
	
	}
	
	/**
	 * Compute the posterior probability of mixtures for each frame.
	 * It relies on lgmmprob and logsumexp methods
	 * This output also this.mPOST (while this.mLLK is useless)
	 * @param feaMatrix feaDim x numFrames
	 * @param feaMatrix2 is feaMatrix.arrayTimes(feaMatrix)
	 */
	public MyMatrix postprob(MyMatrix feaMatrix, MyMatrix feaMatrix2) {
		
		MyMatrix mPOST = lgmmprob(feaMatrix, feaMatrix2);// post = lgmmprob(data, mu, sigma, w); // post must be nmix x numFrames
		MyMatrix mLLK = logsumexp(mPOST, 1); // llk  = logsumexp(post, 1); // llk must be 1 x numFrames
		
		// OPZIONE1
		double [] mLLKv = mLLK.getRow(0);
		//MyMatrix mPOST__1 = mPOST.minusRowVector(mLLKv).exp();// post = exp(bsxfun(@minus, post, llk)); // post must be nmix x numFrames
		
		// OPZIONE2
		int m = mPOST.getRowDimension();
		int n = mPOST.getColumnDimension();
		double [][] x = mPOST.getArray();
		double [][] y = mPOST.getArray(); // new double[m][n];
		for (int r=0; r<m; r++) {
			double [] rigax = x[r];
			double [] rigay = y[r];

			for (int c=0; c<n; c++)
				rigay [c] = Math.exp(rigax[c] - mLLKv[c]);
		}
		
		//TEST.compare_matrices("OPZIONE1 vs OPZIONE2", mPOST__1, y);
		
		//this.mPOST = mPOST;
		//this.mPOST = mPOST__1;
		// inutile this.mLLK = mLLK;
		
		// Instead of this.mPOST = mPOST;, return mPost
		return mPOST; 
	}
	
	/**
	 * Compute the log probability of observations given the GMM.
	 * Does not change anything inside ubm.
	 * @param feaMatrix feaDim x numFrames
	 * @param feaMatrix2 is feaMatrix.arrayTimes(feaMatrix)
	 * @return
	 */
	public MyMatrix lgmmprob(MyMatrix feaMatrix, MyMatrix feaMatrix2) { // public for debug purpose
		
		int feaDim = feaMatrix.getRowDimension();	//ndim = size(data, 1);
		//MyMatrix C = mu.arrayTimes(mu).arrayRightDivide(sigma).sum(1).plus( sigma.log().sum(1) );  //C = sum(mu.*mu./sigma) + sum(log(sigma)); C must be 1 x nmix
		//int r = sigma.getRowDimension();
		//int c = sigma.getColumnDimension();
		
		//D = (1./sigma)' * (data .* data) - 2 * (mu./sigma)' * data  + ndim * log(2 * pi); // D must be nmix x numFrames
		MyMatrix D1 = this.one_dot_slash_sigma_tr.times(feaMatrix2);
		MyMatrix D2 = this.mu_dot_slash_sigma_tr.times(feaMatrix).times(-2.0);
		MyMatrix D = D1.plus(D2).plus((double)feaDim*Math.log(2*Math.PI));
		
		//logprob = -0.5 * (bsxfun(@plus, C',  D)); // logprob must be nmix x numFrames
		//logprob = bsxfun(@plus, logprob, log(w)); // logprob must be nmix x numFrames
		int nmix = this.getNumberOfComponents(); //mu.getColumnDimension();
		int numFrames = feaMatrix.getColumnDimension();
		double [][] xx = new double[nmix][numFrames];
		for (int ir = 0; ir<nmix; ir++)
		{
			for (int ic=0; ic<numFrames; ic++)
			{
				try {
					xx[ir][ic] = -0.5*(this.C.get(0,  ir) + D.get(ir,  ic)) + Math.log(this.w.get(ir,  0)); // C.get(0, ir) = Ctr.get(ir, 0)
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		MyMatrix logprob = new MyMatrix(xx);		
		
		return logprob;
	}
	
	/**
	 * compute log(sum(exp(x),dim)) while avoiding numerical underflow
	 * @param post: nmix x numFrames
	 * @param dim
	 * @return
	 */
	public static MyMatrix logsumexp(MyMatrix post, int dim) {	//function y = logsumexp(x, dim) // public for debug purpose only
		
		MyMatrix xmax = post.max(dim); //xmax = max(x, [], dim); // xmax must be 1 x numFrames		
		double [] xm = xmax.getRow(0);
		//OPZIONE1
		/*MyMatrix y = xmax.plus(post.minusRowVector(xm).exp().sum(dim).log()); //y    = xmax + log(sum(exp(bsxfun(@minus, x, xmax)), dim)); // y must be 1 x numFrames
		for (int i=0; i<y.getColumnDimension(); i++)
		if (!Double.isFinite(y.get(0, i)))
			y.set(0,  i,  xmax.get(0,  i));
			*/
		
		// OPZIONE2, con dim=1. e' leggermente più veloce rispetto alla "notazione compatta"
		int n = post.getColumnDimension();
		int m = post.getRowDimension();
		double [][] p = post.getArray();
		double [] dy = new double[n];
		for (int c = 0; c<n; c++)
		{
			double s = 0;
			for (int r = 0; r<m; r++)
				s += Math.exp(p[r][c] - xm[c]);
			
			double v = xm[c] + Math.log(s);
			dy[c] = Double.isFinite(v) ? v: xm[c];
		}
		MyMatrix Z = MyMatrix.fromRowVector(dy);
		
		//TEST.compare_matrices("OPZIONE1 vs OPZIONE2", y, Z.getArray());
			
		return Z;
	}
	
	
	/**
	 * ML re-estimation of GMM hyperparameters which are updated from accumulators
	 * @param N: [1 x nmix]
	 * @param F: [feaDim x nmix]
	 */
	private void maximization(MyMatrix N, MyMatrix F, MyMatrix S) {
		
		MyMatrix sumN = N.sum(2);
		assert((sumN.getRowDimension()==1) && (sumN.getColumnDimension()==1));
		
		MyMatrix w = N.times(1.0/sumN.get(0, 0)); // w  = N / sum(N); // Here w is a row vectros
		// mu = bsxfun(@rdivide, F, N);
		double [][] dmu = mu.getArray();
		int feaDim = mu.getRowDimension();
		int nmix = mu.getColumnDimension();
		for (int r=0; r<feaDim; r++)
			for (int c=0; c<nmix; c++)
				dmu[r][c] = F.get(r, c)/N.get(0, c);
		
		// sigma = bsxfun(@rdivide, S, N) - (mu .* mu);
		double [][] ds = sigma.getArray();
		for (int r=0; r<feaDim; r++)
			for (int c=0; c<nmix; c++)
				ds[r][c] = S.get(r, c)/N.get(0, c);		
		sigma.minusEquals(mu.arrayTimes(mu));
		
		sigma = apply_var_floors(w, sigma, 0.1); // sigma = apply_var_floors(w, sigma, 0.1);
		
		this.w = w.transpose(); // Because I need a column vector 
		calc_optimization();
				
	}
	
	/**
	 * 
	 * @param w
	 * @param sigma: feaDim x nmix
	 * @param floor_const
	 * @return
	 */
	private MyMatrix apply_var_floors(MyMatrix w, MyMatrix sigma, double floor_const ) {
		
		MyMatrix vFloor = sigma.times(w.transpose()).times(floor_const); // feaDim x 1
		int m = sigma.getRowDimension();
		int n = sigma.getColumnDimension();
		for (int r = 0; r<m; r++)
		{
			double v = vFloor.get(r,  0);
		
			for (int c=0; c<n; c++)
				if (v > sigma.get(r,  c))
						sigma.set(r,  c,  v);
		}
		return sigma;
	}
	
	
			

	/**
	 * perform a binary split of the GMM hyperparameters
	 * @throws Exception 
	 */
	void gmm_mixup() throws Exception {
		
		/*
		 * mu = gmm.mu; sigma = gmm.sigma; w = gmm.w;
			[ndim, nmix] = size(sigma);
			[sig_max, arg_max] = max(sigma);
			eps = sparse(0 * mu);
			eps(sub2ind([ndim, nmix], arg_max, 1 : nmix)) = sqrt(sig_max);
			% only perturb means associated with the max std along each dim 
			mu = [mu - eps, mu + eps];
			% mu = [mu - 0.2 * eps, mu + 0.2 * eps]; % HTK style
		 */
		//int ndim = sigma.getRowDimension();
		int nmix = sigma.getColumnDimension();
		ArrayList<MyMatrix> MI = sigma.maximax(1);
		double [] dmax = MI.get(0).getArray()[0];
		double [] imax = MI.get(1).getArray()[0];
		MyMatrix newMu = mu.cat(mu, 2);			
		for (int c = 0; c<nmix; c++)
		{
			int ind = (int)imax[c];
			double eps = Math.sqrt(dmax[c]);
			//System.out.println("eps = " + eps);
			newMu.set(ind, c,       newMu.get(ind,  c)-eps);
			newMu.set(ind, c+nmix,  newMu.get(ind,  c+nmix)+eps);			
		}
		
		MyMatrix newSigma = sigma.cat(sigma,  2); // sigma = [sigma, sigma];
		MyMatrix neww = w.cat(w,  1).times(0.5); // w = [w ; w]*0.5; % Sigona w = [w, w] * 0.5;
		
		this.mu = newMu;
		this.sigma = newSigma;
		this.w = neww;
		
		calc_optimization();

	}

	

	/**
	 * 
	 */
	private void calc_optimization() {
		
		assert ((sigma.getVectorDim()==VectorDim.COLUMN_VECTORS) && (mu.getVectorDim()==VectorDim.COLUMN_VECTORS));
		
		this.one_dot_slash_sigma_tr = new MyMatrix(sigma.getRowDimension(), sigma.getColumnDimension(), 1.0).arrayRightDivide(sigma).transpose(); // (1./gmm.sigma)';
		this.mu_dot_slash_sigma = mu.arrayRightDivide(sigma); //	gmm.mu_dot_slash_sigma = gmm.mu ./ gmm.sigma;
		this.C = mu.arrayTimes(this.mu_dot_slash_sigma).sum(1).plus(sigma.log().sum(1)); //	gmm.C = sum(gmm.mu .* gmm.mu_dot_slash_sigma) + sum(log(gmm.sigma)); % Sigona ottimizzazione
		this.mu_dot_slash_sigma_tr = this.mu_dot_slash_sigma.transpose(); //gmm.mu_dot_slash_sigma_tr = gmm.mu_dot_slash_sigma';
	}
	
	@Override
	public MyMatrix getMuMatrix(VectorDim required_vector_dim) {
		if (mu.getVectorDim()!=required_vector_dim)
			return mu.transpose();
		else
			return mu;
	}

	@Override
	public MyMatrix getSigmaDiagMatrix(VectorDim required_vector_dim) {
		if (sigma.getVectorDim()!=required_vector_dim)
			return sigma.transpose();
		else
			return sigma;
	}

	@Override
	public MyMatrix getWMatrix(VectorDim required_vector_dim) {
		if (w.getVectorDim()!=required_vector_dim)
			return w.transpose();
		else
			return w;
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		OutputStream os = (file == null ? System.out : new FileOutputStream(file));
		writeToOutputStream(os);
		os.flush();
		os.close();
	}
	
	/**
	 * 
	 * @param os
	 * @throws IOException
	 */
	@Override
	public void writeToOutputStream(OutputStream os) throws IOException {
		mu.write(os);
		sigma.write(os);
		w.write(os);
	}
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	static public Mixture_MSR readFromInputStream(InputStream is) throws IOException {
		
		Mixture_MSR ubm = new Mixture_MSR();
		ubm.mu = MyMatrix.readFromInputStream(is);
		ubm.sigma = MyMatrix.readFromInputStream(is);
		ubm.w = MyMatrix.readFromInputStream(is);
		
		ubm.numberOfComponents = ubm.mu.getVectorDim()==VectorDim.COLUMN_VECTORS? ubm.mu.getColumnDimension() : ubm.mu.getRowDimension();
		ubm.featureDimension = ubm.mu.getVectorDim()==VectorDim.COLUMN_VECTORS? ubm.mu.getRowDimension() : ubm.mu.getColumnDimension();
		
		ubm.calc_optimization();
		
		return ubm;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	static public Mixture_MSR readFromFile(File file) throws IOException {
		
		InputStream is = (file==null) ? System.in : new FileInputStream(file);
		Mixture_MSR ubm = readFromInputStream(is);
		is.close();
		
		return ubm;
	}
	
	/**
	 * 
	 */
	public String toString() {
		return new Gson().toJson(sigma);
	}
	
	/**
	 * Put the matrices in the input list in columnVectors format, by transposing if necessary
	 * @param dataList the input ArrayList of MyMatrix
	 */
	private void asColumnVectorsMatrix(ArrayList<MyMatrix> dataList) {
		// The number of matrices in the list
		int nfiles = dataList.size();
		MyMatrix feaMatrix = null; // Generic matrix of feature vectors
		for (int i =0; i<nfiles; i++)
		{
			feaMatrix = dataList.get(i);
			if (feaMatrix.getVectorDim()==VectorDim.ROW_VECTORS)
			{	
				//System.out.print("Transposing data matrix # " + i + " ( < " + nfiles + ")\n");
				feaMatrix = feaMatrix.transpose();
				dataList.set(i, feaMatrix);
			}	
			// Ottobre 2018 dataList2.add(feaMatrix.arrayTimes(feaMatrix)); // to reuse
		}
	}
}
