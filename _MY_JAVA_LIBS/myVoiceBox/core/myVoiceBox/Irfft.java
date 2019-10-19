package myVoiceBox;

import myDSP.MyComplexArrayFloat;
import myDSP.MyFFT;
import myMath.MyMath;

/**
 * Inverse fft of a conjugate symmetric spectrum X=(Y,N,D)
 * @author FS

%
% Inputs:  Y(M)   The first half of a complex spectrum
%          N      The number of output points to generate (default: 2M-2)
%          D      The dimension along which to perorm the transform
%                 (default: first non-singleton dimension of Y)
%
% Outputs: X(N)   Real inverse dft of Y
%
% This routine calculates the inverse DFT of a conjugate-symmetric to give a real-valued
% output of dimension N. Only the first half of the spectrum need be supplied: if N is even,
% this includes the Nyquist term and is of dimension M=N/2 + 1 whereas if N is odd then there is
% no Nyquist term and the input is of dimension M=(N+1)/2.
% Note that the default value of N is always even so that N must be given explicitly
% if it is odd.
 *
 */
public class Irfft {

	public static float [] exe(MyComplexArrayFloat y) throws Exception {
		
		if (y.size()==1)
			return y.clone().getRealFloat();
		
		int m = y.size();
		int k = 1;
		
		MyComplexArrayFloat v = y; // Name change
		
		int n = 2*m-2;
		
		float [] x = null;
		if (MyMath.rem(n, 2)!=0) // odd output length
			x = MyFFT.complexInverse(v.cat(v.select(m-1, 1, -1).conj())).getRealFloat(); //x=real(ifft([v;conj(v(m:-1:2,:))],[],1));    % do it the long way
		else // % even output length
		{
			v.set(m-1, v.getRealFloat(m-1), 0.0f);//v(m,:)=real(v(m,:));	% force nyquist element real
			
			// t=-0.5i* exp((2i*pi/n)*(0:m-1)).';
			// Is isqual to  0.5*sin(2*pi/n * (0:m-1))  + i * -0.5*cos(2*pi/n * (0:m-1))
			float [] _re = new float[m];
			float [] _im = new float[m];
			for (int i=0; i<m; i++)
			{
				_re[i] = (float)(0.5*Math.sin(2.0*Math.PI/n*i));
				_im[i] = (float)(-0.5*Math.cos(2.0*Math.PI/n*i));
			}
			MyComplexArrayFloat t = new MyComplexArrayFloat(_re, _im); 
			
			// z=(t(:,w)+0.5).*(conj(flipud(v))-v)+v;
			MyComplexArrayFloat z = t.addSelf(0.5f, 0).arrayTimes(v.flip().conjSelf().minusSelf(v)).addSelf(v);
			z = z.select(0, m-2); //z(m,:)=[];
			MyComplexArrayFloat zz = MyFFT.complexInverse(z);
			
			// x=zeros(n,k);
	        //x(1:2:n,:)=real(zz);
	        //x(2:2:n,:)=imag(zz);
			x = zz.getBuffer(); // the buffer is arranged like that by design
		}
		return x;
	}
}
