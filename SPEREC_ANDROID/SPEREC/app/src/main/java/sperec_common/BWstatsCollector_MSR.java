package sperec_common;

import java.util.ArrayList;

import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;

/**
 * Collects Baum Welch statistics from multiple utterances.
 * TO DO: second order statistics support is incomplete. Do not use.
 * @author francesco.sigona
 *
 */
public class BWstatsCollector_MSR implements BWstatsCollector {
	
	// BW statistics of multiple files
	private MyMatrix N = null; // nfiles x nmix
	private MyMatrix F = null; // nfiles x ...
	private MyMatrix S = new MyMatrix(0,0);
	
	// Temporary bw statistics of a single file
	private BWStatistics singleFileBW = new BWStatistics();

	
	/**
	 * "NF" is the only allowed value
	 * TO DO: S support is incomplete
	 */
	private String mode = "";
	
	// public for debug only
	/*public MyMatrix mMU = null;
	public MyMatrix mSIGMA = null;
	public MyMatrix mW = null;
	*/
	
	// USELESS private MyMatrix mPOST = null;
	/* USELESS public MyMatrix getPost() {
		return mPOST;
	}*/
	
	// USELESS private MyMatrix mLLK = null;
	/*public MyMatrix getLLK() { USELESS
		return mLLK;
	}
	*/
	
	public BWstatsCollector_MSR(String mode) throws Exception {
				
		if (!mode.equals("NF"))
			throw new Exception ("Illegal mode: mode can be NF only");
				
		this.mode = mode;
	}

	@Override
	/**
	 * Export the statistics using the BWStatistics class
	 */
	public BWStatistics getBWStats(VectorDim required_vector_dim) throws Exception {
		
		if (required_vector_dim==VectorDim.UNSPECIFIED)
			throw new Exception("Illegal required_vector_dim parameter");
		
		BWStatistics bw = new BWStatistics();
		bw.N = this.N;
		bw.F = this.F;
		bw.S = this.S;
		
		if (required_vector_dim != this.N.getVectorDim()) {
			bw.N = bw.N.transpose();
			bw.F = bw.F.transpose();
			// Put code for S here
		}
		return bw;
	}

		

	/**
	 * Collect Baum-Welch statistics from feature matrices
	 * @param ubm the Universal Background Model
	 * @param spkSessions a list of sessions (feature matrices)
	 * @param required_vector_dim specify the matrix format of the results
	 */
	@Override
	public BWStatistics collectBWstats(MixtureModel ubm, ArrayList<MyMatrix> spkSessions, VectorDim required_vector_dim)
			throws Exception {
		
		//explode_UBM(ubm);
				
		int numFiles = spkSessions.size();
		
		double [][] dN = new double[numFiles][]; // new double[numFiles][ubm.nd];
		double [][] dF = new double[numFiles][]; //new double[numFiles][ubm.nd*ubm.fd];
		
		// For each file (i.e. utterance) compute the bw
		for (int iFile=0; iFile<numFiles; iFile++)
		{
			MyMatrix feaMatrix = spkSessions.get(iFile);
			
			// Adjust the matrix format if necessary
			if (feaMatrix.getVectorDim()==VectorDim.ROW_VECTORS)
				feaMatrix = feaMatrix.transpose();
			
			// Compute N, F, and S and store them internally into singleFileBW
			compute_bw_stats(feaMatrix, (Mixture_MSR) ubm);
			
			// Take and store them locally, file by file
			dN[iFile] = this.singleFileBW.N.transpose().getRow(0); // vector of N statistics, as row vector
			dF[iFile] = this.singleFileBW.F.transpose().getRow(0); // supervector of F statistics, as row vector	
		}
		
		// Hard-coding that statistic vectors are stored as row vectors.
		// So, the first dimension of the double array storing the statistics is the number of statistic vectors (numFiles)
		this.N = new MyMatrix(dN, VectorDim.ROW_VECTORS); // Hard-coding that statistic vectors are stored as row vectors
		this.F = new MyMatrix(dF, VectorDim.ROW_VECTORS); // Hard-coding that statistic vectors are stored as row vectors
		
		BWStatistics bwstats = this.getBWStats(required_vector_dim);
		return bwstats;
	}
	
	
	/**
	 * Compute the BW statistics for a single utterance, which is represented by a single matrix of features, given the UBM.
	 * UBM is required only to compute posteriors, and after that it is required to provide the centering matrix mMU
	 * I think that this function should be removed, because the BW computation should not know about UBM
	 * The F is centered, by subtracting the N*ubm.mu
	 * @param feaMatrix
	 * @param ubm
	 * @throws Exception 
	 */
	private void compute_bw_stats(MyMatrix feaMatrix, Mixture_MSR ubm) throws Exception {
		
		MyMatrix feaMatrix2 = feaMatrix.arrayTimes(feaMatrix); // Just to save time
		
		// Compute posterior probabilities
		MyMatrix mPOST = ubm.postprob(feaMatrix, feaMatrix2); // was this.mPOST// post = postprob(data, gmm.mu, gmm.sigma, gmm.w(:)); // post must be nmix x numFrames
		
		// The matrix for F centering
		MyMatrix mMU = ubm.getMuMatrix(VectorDim.COLUMN_VECTORS);
		
		// Actual computation
		compute_bw_stats(feaMatrix, feaMatrix2, mPOST, mMU);
	}
	
	/**
	 * Compute BW stats without knowing where posteriors came from
	 * The resulting F is uncentered
	 * @param feaMatrix the matrix of the features for the current utterance
	 * @param mPOST posterior probabilities. Should be nmix x numFrames
	 * @param mMU centering matrix for F centering
	 * @throws Exception 
	 */
	private void compute_bw_stats(MyMatrix feaMatrix, MyMatrix feaMatrix2, MyMatrix mPOST) throws Exception {

		int feaDim = feaMatrix.getRowDimension();
		int nmix   = mPOST.getRowDimension();
		MyMatrix mMU_zeros = new MyMatrix(feaDim, nmix, 0.0, VectorDim.COLUMN_VECTORS);
		
		compute_bw_stats(feaMatrix, feaMatrix2, mPOST, mMU_zeros);	 
	}
	
	/**
	 * Compute BW statistics without knowing what has computed the posteriors neighter the mMU.
	 * Results are N, F and S, and are stored internally
	 * @param feaMatrix the matrix of the features for the current utterance
	 * @param mPOST posterior probabilities: nmix x numfiles
	 * @param mMU centering matrix for F centering
	 * @throws Exception 
	 */
	private void compute_bw_stats(MyMatrix feaMatrix, MyMatrix feaMatrix2, MyMatrix mPOST, MyMatrix mMU) throws Exception {
		
		if (mMU.getVectorDim()!= VectorDim.COLUMN_VECTORS)
			throw new Exception("Illegal mMU format");
		
		// In matlab:
		// ubm.mu = feadim x nmix
		// ubm.sigma = feadim x nmix
		// ubm.w = 1 x nd
		
		// ndim is feadim
		// [ndim, nmix] = size(ubm.mu);
		// m = reshape(ubm.mu, ndim * nmix, 1); // reshape procede lungo la prima colonna, poi lungo la 2a etc.., per colonne
		
		// 1. Calcola N ed F
		//expectation(feaMatrix, feaMatrix.arrayTimes(feaMatrix), ubm); //[N, F] = expectation(data, ubm); // data = feadim x numframes; N must be nmix x 1; F must be feaDim x nmix
		MyMatrix postTr = mPOST.transpose(); // Compute now, because they will be used twice. Anyway, that's memory demanding
		
		// Compute zero-th order Baum Welch statistics, aka probabilistic count
		// It is a scalar for each component, therefore the result is a (column) vector of nmix elements
		singleFileBW.N = mPOST.sum(2); // N = sum(post, 2); N must be nmix x 1;  ATTENZIONE: NEL FILE compute_bw_stats 
		
		// Compute the first order Baum Welch statistics
		// It is a feaDim-long vector for each component,
		// therefore the following gives a matrix where the g-th column if the F statistic (column vector) of the g-th component
		singleFileBW.F = feaMatrix.times(postTr); // F = data * post'; F must be feaDim x nmix
		
		// Compute the second order Baum Welch statistics if required
		// It is a square, symmetric matrix of size feaDim x feaDim, FOR EACH COMPONENT
		// So, the full result is a collection of nmix matrice
		// Just doing feaMatrix2.times(postTr) gives a matrix whose columns are the diagonal of such nmix matrices
		// that means that one is not interested in off-diagonal elements, as in the UBM training
		if (mode.equals("NFS"))
			singleFileBW.S = feaMatrix2.times(postTr); // It is a feaDim x nMix matrix : its columns are the elements of diagonal matrices //S = (data .* data) * post';
				
		// 2. Ora centra le F (se mMU è diversa da 0) e restituisce la F come supervettore (feaDim*nmix x 1)
		// idx_sv = reshape(repmat(1 : nmix, ndim, 1), ndim * nmix, 1);
		// F = reshape(F, ndim * nmix, 1);
		// F = F - N(idx_sv) .* m; % centered first order stats
		int feaDim = mMU.getRowDimension();
		int nmix   = mMU.getColumnDimension();
		int i=0;
		double [] f = new double [feaDim*nmix];
		for (int imix=0; imix<nmix; imix++)
		{
			double    N_imix   = singleFileBW.N.get(imix, 0); // The N statistic of the imix component. It is a scalar
			double [] F_imix   = singleFileBW.F.getColumn(imix); // The F statistic of the imix component: it is a feaDim-sized vector
			double [] mMU_imix = mMU.getColumn(imix); // Centering vector: cab be the mu of the imix component
			
			for (int ifea=0; ifea<feaDim; ifea++)
				f[i++] = F_imix[ifea] -  N_imix * mMU_imix[ifea]; //f[ifea++] = singleFileBW.F.get(ifea, imix) -  N_imix * mMU.get(ifea, imix);
		}

		singleFileBW.F = MyMatrix.fromColumnVector(f);
		 
	}
	
	
	
	
	
	/// ------------------- OLD STUFF ----------------------------------------------------------------------------
	
	/**
	 * compute the posterior probability of mixtures for each frame
	 * @param feaMatrix: feaDim x numFrames
	 * @param feaMatrix2 = feaMatrix.arrayTimes(feaMatrix)
	 * @param ubm: with: 	 mu: feaDim x nmix , sigma: feaDim x nmix , w: nmix x 1
	 * @param post: OUTPUT
	 * @param llk: OUTPUT
	 */
	/*private void postprob(MyMatrix feaMatrix, MyMatrix feaMatrix2, Mixture_MSR ubm) {
		
		//MyMatrix mPOST = lgmmprob(feaMatrix, feaMatrix2, ubm);// post = lgmmprob(data, mu, sigma, w); // post must be nmix x numFrames
		MyMatrix mPOST = ubm.lgmmprob(feaMatrix, feaMatrix2);// post = lgmmprob(data, mu, sigma, w); // post must be nmix x numFrames
		MyMatrix mLLK = Mixture_MSR.logsumexp(mPOST, 1); // llk  = logsumexp(post, 1); // llk must be 1 x numFrames
		mPOST = mPOST.minusRowVector(mLLK.getRow(0)).exp();// post = exp(bsxfun(@minus, post, llk)); // post must be nmix x numFrames

		this.mPOST = mPOST;
		// USELESS this.mLLK = mLLK;
	}*/

	/**
	 * 
	 * @param feaMatrix feaDim x numFrames
	 * @param feaMatrix2 feaMatrix.arrayTimes(feaMatrix)
	 * @param ubm
	 * @return
	 */
	/*private MyMatrix lgmmprob(MyMatrix feaMatrix, MyMatrix feaMatrix2, Mixture_MSR ubm) { // public for debug purpose
		
		MyMatrix logprob = null;
		int feaDim = feaMatrix.getRowDimension();	//ndim = size(data, 1);
		//C = sum(mu.*mu./sigma) + sum(log(sigma)); C must be 1 x nmix
	
		//D = (1./sigma)' * (data .* data) - 2 * (mu./sigma)' * data  + ndim * log(2 * pi); // D must be nmix x numFrames
		MyMatrix D1 = ubm.one_dot_slash_sigma_tr.times(feaMatrix2);
		MyMatrix D2 = ubm.mu_dot_slash_sigma_tr.times(feaMatrix).times(-2.0);
		MyMatrix D = D1.plus(D2).plus((double)feaDim*Math.log(2*Math.PI));
		
		//logprob = -0.5 * (bsxfun(@plus, C',  D)); // logprob must be nmix x numFrames
		//logprob = bsxfun(@plus, logprob, log(w)); // logprob must be nmix x numFrames
		int nmix = ubm.getNumberOfComponents();
		int numFrames = feaMatrix.getColumnDimension();
		double [][] xx = new double[nmix][numFrames];
		for (int ir = 0; ir<nmix; ir++)
			for (int ic=0; ic<numFrames; ic++)
				xx[ir][ic] = -0.5*(ubm.C.get(0,  ir) + D.get(ir,  ic)) + Math.log(ubm.w.get(ir,  0)); // C.get(0, ir) = Ctr.get(ir, 0)
		logprob = new MyMatrix(xx);		
			
		return logprob;
	}*/
	
	/**
	 * compute log(sum(exp(x),dim)) while avoiding numerical underflow
	 * @param post: nmix x numFrames
	 * @param dim
	 * @return
	 */
	/*
	private MyMatrix logsumexp(MyMatrix post, int dim) {	//function y = logsumexp(x, dim) // public for debug purpose only
		
		MyMatrix xmax = post.max(dim); //xmax = max(x, [], dim); // xmax must be 1 x numFrames		
		MyMatrix y = xmax.plus(post.minusRowVector(xmax.getRow(0)).exp().sum(dim).log()); //y    = xmax + log(sum(exp(bsxfun(@minus, x, xmax)), dim)); // y must be 1 x numFrames

		//ind  = find(~isfinite(xmax));
				//if ~isempty(ind)
				//y(ind) = xmax(ind);
				//endfor (int i = 0; i<numFrames; i++)

		int n = y.getColumnDimension();
		for (int i=0; i<n; i++)
		{
			double yi = y.get(0, i);
			// isFinite requires Java 1.8, Android API level 24, if (!Double.isFinite(y.get(0, i)))
			if (Double.isInfinite(yi) || Double.isNaN(yi))
			{
				y.set(0, i, xmax.get(0, i));
			}
		}

		return y;
	}*/
	
	// Format conversion
	/*public void explode_UBM (MixtureModel ubm) {
		int nd = ubm.getNumberOfComponents();
		int fd = ubm.getFeatureDimension();

		assert(ubm.diagonal());
		double [][] mu = new double [fd][nd]; // This is the format for MSR code
		double [][] sigma = new double [fd][nd]; // Diagonal matrices, stored this way
		double [] w = new double[nd];
		
		double sumOfWeights = 0.0;
		for (int iComp=0; iComp<nd; iComp++)
		{
			Density den = ubm.components[iComp];
			w[iComp] = den.apr;
			sumOfWeights += w[iComp];
			for (int iFea = 0; iFea<fd; iFea++)
			{
				mu[iFea][iComp] = den.mue[iFea];
				sigma[iFea][iComp] = den.cov[iFea];
			}
		}

		//System.out.println(sumOfWeights);
		
		mMU = new MyMatrix(mu, VectorDim.COLUMN_VECTORS);
		mSIGMA = new MyMatrix(sigma, VectorDim.COLUMN_VECTORS);
		mW = MyMatrix.fromColumnVector(w);
		

	}*/
	
	/**
	 * Compute the BW statistics for a single utterance, which is represented by a single matrix of features.
	 * The F is centered, by subtracting the N*ubm.mu
	 * @param feaMatrix
	 * @param ubm
	 */
	/*private void compute_bw_stats_OLD(MyMatrix feaMatrix, Mixture_MSR ubm) {
		// In matlab:
		// ubm.mu = feadim x nmix
		// ubm.sigma = feadim x nmix
		// ubm.w = 1 x nd
		
		// ndim is feadim
		// [ndim, nmix] = size(ubm.mu);
		// m = reshape(ubm.mu, ndim * nmix, 1); // reshape procede lungo la prima colonna, poi lungo la 2a etc.., per colonne
		// idx_sv = reshape(repmat(1 : nmix, ndim, 1), ndim * nmix, 1);
		
		// 1. Calcola N ed F
		expectation(feaMatrix, feaMatrix.arrayTimes(feaMatrix), ubm); //[N, F] = expectation(data, ubm); // data = feadim x numframes; N must be nmix x 1; F must be feaDim x nmix
				
		// 2. Ora centra le F
		// F = reshape(F, ndim * nmix, 1);
		// F = F - N(idx_sv) .* m; % centered first order stats
		int feaDim   = ubm.getFeatureDimension(); //mMU.getRowDimension();
		int nmix     = ubm.getNumberOfComponents(); //mMU.getColumnDimension();
		MyMatrix mMU = ubm.getMuMatrix(VectorDim.COLUMN_VECTORS);
		
		int p = feaDim*nmix;
		double [] f = new double [p];
		for (int imix=0; imix<nmix; imix++)
		{
			double N_of_current_comp = this.N.get(imix, 0);
			
			for (int ifea=0; ifea<feaDim; ifea++)
				f[ifea++] = this.singleFileBW.F.get(ifea, imix) -  N_of_current_comp * mMU.get(ifea, imix);
		}

		this.singleFileBW.F = MyMatrix.fromColumnVector(f);
		 
	}*/
	/**
	 * compute the sufficient statistics
	 * @param feaMatrix
	 * @param feaMatrix2 = feaMatrix.arrayTimes(feaMatrix)
	 * @param ubm
	 * @param N
	 * @param F
	 */
	/*void expectation (MyMatrix feaMatrix, MyMatrix feaMatrix2, Mixture_MSR ubm) {

		MyMatrix mPOST = ubm.postprob(feaMatrix, feaMatrix2); // was this.mPOST// post = postprob(data, gmm.mu, gmm.sigma, gmm.w(:)); // post must be nmix x numFrames
		MyMatrix postTr = mPOST.transpose();

		singleFileBW.N = mPOST.sum(2); // N = sum(post, 2); N must be nmix x 1;  ATTENZIONE: NEL FILE compute_bw_stats 
		singleFileBW.F = feaMatrix.times(postTr); // F = data * post'; F must be feaDim x nmix
		
		if (mode.equals("NFS"))
			singleFileBW.S = feaMatrix2.times(postTr); //S = (data .* data) * post';
	
	}*/
	
	/**
	 * Collect Baum-Welch statistics from feature files listed in a file list.
	 * @param ubm
	 * @param inDir
	 * @param fileListFullPath
	 * @throws Exception 
	 */
	/*@Override
	public BWStatistics collectBWstats(	MixtureModel ubm, 
										String inDir,
										String fileListFullPath,
										int ufv,
										VectorDim required_vector_dim) throws Exception {
		
		//explode_UBM(ubm);
		//int feaDim = ubm.getFeatureDimension(); //mMU.getRowDimension();
		//int nmix = ubm.getNumberOfComponents(); //mMU.getColumnDimension();
		
		ArrayList<String> filenames = MiscUtils.loadTextLines(fileListFullPath);
		int numFiles = filenames.size();
		
		double [][] dN = new double[numFiles][]; // new double[numFiles][ubm.nd];
		double [][] dF = new double[numFiles][]; //new double[numFiles][ubm.nd*ubm.fd];
		// Put code for S here
		
		for (int iFile=0; iFile<numFiles; iFile++)
		{
			String fileFullPath = inDir + File.separator + filenames.get(iFile);
			MyMatrix feaMatrix = (ufv==0) ? MyMatrix.readFromFile(fileFullPath) :
											MyMatrix.readFromUfvFile(fileFullPath);
			
			if (feaMatrix.getVectorDim()==VectorDim.ROW_VECTORS)
				feaMatrix = feaMatrix.transpose();
			
			// Compute N, F, and S and store them into singleFileBW
			compute_bw_stats(feaMatrix, (Mixture_MSR) ubm);
			
			// Take and store them locally, file by file
			dN[iFile] = this.singleFileBW.N.transpose().getRow(0);
			dF[iFile] = this.singleFileBW.F.transpose().getRow(0);			
		}
		
		// Hard-coding that statistic vectors are stored as row vectors.
		// So, the first dimension of the double array storing the statistics is the number of statistic vectors (numFiles)
		this.N = new MyMatrix(dN, VectorDim.ROW_VECTORS); // Hard-coding that statistic vectors are stored as row vectors
		this.F = new MyMatrix(dF, VectorDim.ROW_VECTORS); // Hard-coding that statistic vectors are stored as row vectors
		
		// Export the final BW statistics 
		BWStatistics bwstats = this.getBWStats(required_vector_dim);
		return bwstats;
	}
	*/

}
