package sperec_common;

/**
 * Specifications for Gaussian Probabilistic Linear Discriminant Analysis
 * @author FS
 *
 */
public class GPLDA_Specs extends Specs {
	/**
	 * dimensionality of the Eigenvoice subspace
	 */
	public int NPHI;

	/**
	 * Number of iteration for GPLDA learning
	 */
	public int niter_gplda;
}