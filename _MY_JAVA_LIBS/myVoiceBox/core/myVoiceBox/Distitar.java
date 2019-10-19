package myVoiceBox;

import myMath.MyMath;
import myMath.MyMatrix;

/*
 * calculates the Itakura distance between AR coefficients D=(AR1,AR2,MODE)
 
%
% Inputs: AR1,AR2     AR coefficient sets to be compared. Each row contains a set of coefficients.
%                     AR1 and AR2 must have the same number of columns.
%
%         MODE        Character string selecting the following options:
%                         'x'  Calculate the full distance matrix from every row of AR1 to every row of AR2
%                         'd'  Calculate only the distance between corresponding rows of AR1 and AR2
%                              The default is 'd' if AR1 and AR2 have the same number of rows otherwise 'x'.
%                          'e'  Calculates exp(d) instead of d (quicker because no log is necessary)
%           
% Output: D           If MODE='d' then D is a column vector with the same number of rows as the shorter of AR1 and AR2.
%                     If MODE='x' then D is a matrix with the same number of rows as AR1 and the same number of columns as AR2'.
%
% If ave() denotes the average over +ve and -ve frequency, the Itakura spectral distance is 
%
%                               log(ave(pf1/pf2)) - ave(log(pf1/pf2))
%
% The Itakura distance is gain-independent, i.e. distitpf(f*pf1,g*pf2) is independent of f and g.
%
% The Itakura distance may be expressed as log(ar2*toeplitz(lpcar2rr(ar1))*ar2') where the ar1 and ar2 polynomials
% have first been normalised by dividing through by their 0'th order coefficients.

% Since the power spectrum is the fourier transform of the autocorrelation, we can calculate
% the average value of p1/p2 by taking the 0'th order term of the convolution of the autocorrelation
% functions associated with p1 and 1/p2. Since 1/p2 corresponds to an FIR filter, this convolution is
% a finite sum even though the autocorrelation function of p1 is infinite in extent.
% The average value of log(pf1) is equal to log(ar1(1)^-2) where ar1(1) is the 0'th order AR coefficient.

% The Itakura distance can also be calculated directly from the power spectra; providing np is large
% enough, the values of d0 and d1 in the following will be very similar:
%
%         np=255; d0=distitar(ar1,ar2); d1=distitpf(lpcar2pf(ar1,np),lpcar2pf(ar2,np))
%

% Ref: A.H.Gray Jr and J.D.Markel, "Distance measures for speech processing", IEEE ASSP-24(5): 380-391, Oct 1976
%      L. Rabiner abd B-H Juang, "Fundamentals of Speech Recognition", Section 4.5, Prentice-Hall 1993, ISBN 0-13-015157-2
%      F. Itakura, "Minimum prediction residual principle applied to speech recognition", IEEE ASSP-23: 62-72, 1975
*/
public class Distitar {

	
	static public double [][] exe(double [][] ar1, double [][] ar2, String mode) throws Exception {
		
		if (!mode.equals("e"))
			throw new Exception("Distitar: unsupported mode: " + mode);
		
		double [][] dd = null;
		
		int p1 = ar1[0].length;
		int nf1 = ar1.length; // [nf1,p1]=size(ar1);
		int nf2 = ar2.length; // nf2=size(ar2,1)
		
		double [][] m2 = Lpcar2ra.exe(ar2); //m2=lpcar2ra(ar2);
		//m2(:,1)=0.5*m2(:,1);
		for (int i=0; i<m2.length; i++)
			m2[i][0] *= 0.5;

		// if nargin<3 | isempty(mode) mode='0'; end
		if (mode.contains("d") || (!mode.equals("x") && nf1==nf2) )// if any(mode=='d') | (mode~='x' & nf1==nf2)
		{
		   int nx = Math.min(nf1, nf2); //nx=min(nf1,nf2);
		  // d=2*sum(lpcar2rr(ar1(1:nx,:)).*m2(1:nx,:),2).*((ar1(1:nx,1)./ar2(1:nx,1)).^2);
		   double [] d = new double[nx];
		   for (int i=0; i<nx; i++)
		   {
			   double [] _da = Lpcar2rr.exe(ar1[i]);
			   double [] _db = MyMath.arrayTimes(_da, m2[i]);
			   double _d0 = 2.0 * MyMath.sum(_db);
			   double _d1 = Math.pow(ar1[i][0]/ar2[i][0], 2);
			   double _d = _d0*_d1;
			   d[i] = _d;
			   //d[i] = 2.0 * MyMath.sum(MyMath.arrayTimes(Lpcar2rr.exe(ar1[i]), m2[i])) * Math.pow(ar1[i][0]/ar2[i][0], 2);
		   }
		   
		   dd = new double[][] {d};
		}
		else
		{
			// d=2*lpcar2rr(ar1)*m2'.*((ar1(:,1)*ar2(:,1)'.^(-1)).^2);
			MyMatrix RR = new MyMatrix(Lpcar2rr.exe(ar1));
			MyMatrix M2 = new MyMatrix(m2);
			
			MyMatrix D = RR.times(M2.transpose());
			MyMatrix AR1 = new MyMatrix(ar1);
			
			double [][] _t1 = new double[nf1][nf1];
			for (int i=0; i<nf1; i++)
				for (int j=0; j<nf1; j++)
					_t1[i][j] = 2.0 * 1.0/Math.pow(ar1[i][0] * ar2[i][0], 2);
			MyMatrix _T1 = new MyMatrix(_t1);
			
			dd = D.arrayTimes(_T1).getArray();
		}
		
		return dd;
	}
}
