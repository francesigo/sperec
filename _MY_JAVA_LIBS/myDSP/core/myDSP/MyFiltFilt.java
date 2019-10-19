package myDSP;

import java.util.Arrays;

import myMath.MyMath;
import myMath.MyMatrix;

/**
 * Inspired by the Matlab filtfilt function
 * @author FS
 *
 */
public class MyFiltFilt {

	int nfact;
	
	/**
	 * Initial conditions
	 */
	double [] zi;
	
	/**
	 * The filter which will be used in the current implementation
	 */
	MyFilter f;

	

	public MyFiltFilt () {
	}

	public static MyFiltFilt fromFilter(MyFilter f) throws Exception {
		double [] b_ = f.getb();
		double [] a_ = f.geta();
		return new MyFiltFilt(b_, a_);
	}
	public MyFiltFilt (double [] b_, double [] a_) throws Exception {

		// getCoeffsAndInitialConditions
		int na = a_.length;
		int nb = b_.length;
		int nfilt = Math.max(na,  nb);
		nfact = Math.max(1, 3*(nfilt-1));  //length of edge transients

		double [] b = b_;
		double [] a = a_;
		if (nb < nfilt) {
			b = new double [nfilt];
			System.arraycopy(b_, 0, b, 0, nb);
			Arrays.fill(b, nb, nfilt,  0.0);
		} else if(na<nfilt) {
			a = new double [nfilt];
			System.arraycopy(a_, 0, a, 0, na);
			Arrays.fill(a, na, nfilt,  0.0);
		}
		if (nfilt>1) {
			//rows = [1:nfilt-1, 2:nfilt-1, 1:nfilt-2];
			int [] rows = new int[3*nfilt-5];
			int i=0;
			for (int j=0; j<nfilt-1; j++)
				rows[i++] = j;
			for (int j=1; j<nfilt-1; j++)
				rows[i++] = j;
			for (int j=0; j<nfilt-2; j++)
				rows[i++] = j;

			// cols = [ones(1,nfilt-1), 2:nfilt-1, 2:nfilt-1];
			int [] cols = new int[rows.length];
			i=0;
			for (int j=0; j<nfilt-1; j++)
				cols[i++] = 0;
			for (int j=1; j<nfilt-1; j++)
				cols[i++] = j;
			for (int j=1; j<nfilt-1; j++)
				cols[i++] = j;

			// vals = [1+a(2), a(3:nfilt).', ones(1,nfilt-2), -ones(1,nfilt-2)];
			double [] vals = new double[rows.length];
			vals[0] = 1.0 + a[1];
			i=1;
			for (int j=2; j<nfilt; j++)
				vals[i++] = a[j];
			for (int j=0; j<nfilt-2; j++)
				vals[i++] = 1.0;
			for (int j=0; j<nfilt-2; j++)
				vals[i++] = -1.0;

			//rhs  = b(2:nfilt) - b(1)*a(2:nfilt);
			double [] rhs = new double[nfilt-1];
			for (int j=0; j<nfilt-1; j++)
				rhs[j] = b[j+1] - b[0]*a[j+1];

			//         zi   = sparse(rows,cols,vals) \ rhs;
			MyMatrix SP = MyMatrix.fromSparse(rows, cols, vals);
			MyMatrix ZI = SP.solve(MyMatrix.fromColumnVector(rhs));
			zi = ZI.getColumn(0);
		}
		else
		{
			zi = null;
		}
		
		this.f = new MyFilter(b, a);
	}

	/**
	 * Filter se input sequence
	 * @param x the input sequence
	 * @return the filtered sequence
	 * @throws Exception 
	 */
	public double [] filter(double [] x ) throws Exception {
		
		if (x.length <= nfact) // input data too short
			throw new Exception("FiltFilt: signal length must be at least " + nfact);

		// y = ffOneChanCat(b,a,x,zi,nfact,L);
		double [] y = ffOneChanCat(f, x, zi, nfact, 1);
		return y;
	}
	
	/**
	 * 
	 * @param f
	 * @param y: the sequence to be filtered
	 * @param zi: initial conditions
	 * @param nfact
	 * @param L
	 * @return
	 */
	private double [] ffOneChanCat(MyFilter f, double [] y, double [] zi_, int nfact, int L) {

		//% Single channel, data explicitly concatenated into one vector
		//y = [2*y(1)-y(nfact+1:-1:2); y; 2*y(end)-y(end-1:-1:end-nfact)]; %#ok<AGROW>
		int end = y.length;
		double [] y1 = new double [nfact + end + nfact];
		int i=0;
		for (int j=nfact; j>=1; j--)
			y1[i++] = 2*y[0] - y[j];
		for (int j=0; j<end; j++)
			y1[i++] = y[j];
		for (int j=end-2; j>=end-nfact-1; j--)
			y1[i++] = 2*y[end-1] - y[j];
		
		y = y1;


		//filter, reverse data, filter again, and reverse data again
		
		// y = filter(b(:,ii),a(:,ii),y,zi(:,ii)*y(1));
		f.filter(y, MyMath.times(zi, y[0]));
		y = f.gety();
		
		//y = y(end:-1:1);
		y = MyMath.flip(y);
		
		// y = filter(b(:,ii),a(:,ii),y,zi(:,ii)*y(1));
		f.filter(y, MyMath.times(zi, y[0]));
		y = f.gety();

		// retain reversed central section of y
	    //y = y(end-nfact:-1:nfact+1);
		double [] yy = new double[y.length-nfact-nfact];
		i=0;
		for (int j=y.length-nfact-1; j>=nfact; j--)
			yy[i++] = y[j];
		
		return yy;
	}
	
}
