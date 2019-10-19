package myVoiceBox;

import java.util.Arrays;

import myMath.MyMath;

/**
 * Finds peaks with quadratic interpolation [K,V]=(Y,M,W,X)
 * 
 * @author FS
 *
 */
public class V_findpeaks {
	
	/**
	 * The positions the peaks in Y (fractional)
	 */
	public double [] kk;
	
	/**
	 * the peak amplitudes: the amplitudes will be interpolated
	 */
	public double [] v;

	/**
	 * Finds peaks with quadratic interpolation
	 * If there is a plateau rather than a sharp peak, the routine will place the
	 * peak in the centre of the plateau. 
	 * @param y is the input signal
	 * @param mode must be set to "q" performs quadratic interpolation
	 * @throws Exception
	 */
	public V_findpeaks(double [] y, String mode) throws Exception {
		
		int nargin = 2;
		
		// model MUST be equal to "q"
		if (!mode.equals("q"))
			throw new Exception ("Unsupported mode : " + mode);
		
		int ny = y.length; // NUmber of samples
		
		// From now, I will used matlab indexing, starting from 1.
		// Java array will be longer (1 additional element) and the first element will be unused
		// dx=y(2:end)-y(1:end-1);
		double [] dx = new double [ny-1+1];
		for (int i=1; i<dx.length; i++)
			dx[i] = y[i-1+1] - y[i-1];
		
		//r=find(dx>0);
		//f=find(dx<0);
		boolean [] rpos = new boolean[dx.length];
		boolean [] rneg = new boolean[dx.length];
		for (int i=1; i<dx.length; i++) {
			rpos[i] = dx[i]>0;
			rneg[i] = dx[i]<0;			
		}
		int [] r = MyMath.find(rpos, 1, rpos.length-1); // therefore, r are indexes, >=0
		int [] f = MyMath.find(rneg, 1, rneg.length-1); // therefore, f are indexes, >=0
		// Gli indici per r ed f partono da 0
		
		int [] k = null;
		if (r.length>0 && f.length>0) // we must have at least one rise and one fall
		{
			//dr=r;
		    //dr(2:end)=r(2:end)-r(1:end-1);
			int [] dr = new int [r.length+1]; // Gli indici per dr partiranno da 1
			dr[1] = r[0];
			for (int i=2; i<dr.length; i++)
				dr[i] = r[i-1] - r[i-2];
			// Quindi, dr[1 rappresenta l'indice del campione da cui inizia il primo "rise"
			// metre gli altri dr sono le distanze tra un rise ed il precedente
			
			//rc=ones(ny,1);
			int [] rc = new int [ny+1]; // Indici da 1
			Arrays.fill(rc,  1);
			//rc(r+1)=1-dr;
			for (int i=0; i<r.length; i++) // i indicizza r. Devo sommare 1 per indicizzare dr
				rc[r[i]+1] = 1-dr[i+1];
			rc[1] = 0; //rc(1)=0;
			
			int [] rs = MyMath.cumsum(rc, 1, rc.length-1); // rs=cumsum(rc); % = time since the last rise
			// Gli indici per rs partono da zero
			
			// df=f;
		    // df(2:end)=f(2:end)-f(1:end-1);
			int [] df = new int[f.length+1]; // indici da 1
			df[1] = f[0]; // Gli indici di f partivano da zero
			for (int i=2; i<df.length; i++)
				df[i] = f[i-1] - f[i-2];
			
			// fc=ones(ny,1);
			int [] fc = new int[ny+1]; // indici da 1
			Arrays.fill(fc,  1);
			
			// fc(f+1)=1-df;
			for (int i=0; i<f.length; i++) //indici di f da zero, di df da 1
				fc[f[i]+1] = 1-df[i+1];
			
			fc[1] = 0; // fc(1)=0;
			int [] fs = MyMath.cumsum(fc, 1, fc.length-1); // fs=cumsum(fc); % = time since the last fall
			// Indici di fs sono da 0
			
			// rp=repmat(-1,ny,1);
			int [] rp = new int [ny+1]; // Indici da 1
			Arrays.fill(rp, -1);
			
			// rp([1; r+1])=[dr-1; ny-r(end)-1];
			rp[1] = dr[1]-1;
			for (int i=2; i<dr.length; i++) { // i indicizza dr...
				int i2 = i-2;
				int r12 = r[i-2];
				int rpi = r12+1;
				rp[r[i-2]+1] = dr[i]-1;
			}
			rp[r[r.length-1]+1] = ny-r[r.length-1]-1;
			
			int [] rq = MyMath.cumsum(rp, 1, rp.length-1); // indici da 0// rq=cumsum(rp);  % = time to the next rise
			
			// fp=repmat(-1,ny,1);
			int [] fp = new int[ny+1]; // indici da 1
			Arrays.fill(fp, -1);
				
		    // fp([1; f+1])=[df-1; ny-f(end)-1];
			fp[1] = df[1]-1;
			for (int i=2; i<df.length; i++) // i indicizza df, ma gli indici di f partono da 0
				fp[f[i-2]+1] = df[i]-1;
			fp[f[f.length-1]+1] = ny-f[f.length-1]-1;
			
			int [] fq = MyMath.cumsum(fp, 1, fp.length-1); // indici da 0// = time to the next fall
			
			// k=find((rs<fs) & (fq<rq) & (floor((fq-rs)/2)==0));   % the final term centres peaks within a plateau
			boolean [] _b1 = new boolean[rs.length+1]; // indici da 1 
			for (int i=0; i<rs.length; i++) // qui tutti gli indici partono da zero, tranne b1
				_b1[i+1] = (rs[i]<fs[i]) && (fq[i]<rq[i]) && (Math.floor((double)(fq[i]-rs[i])/2)==0);
			
			k = MyMath.find(_b1, 1, _b1.length-1); //indici da zero (ma contiene indici da 1)
			
			v = MyMath.select(y, MyMath.minus(k,1)); // v=y(k);, indici da 0
			
			if (mode.contains("q"))
			{	// nargin=2
				//b=0.25*(y(k+1)-y(k-1));
				//a=y(k)-2*b-y(k-1);
	            //j=(a>0);            % j=0 on a plateau
				double [] b = new double[k.length+1]; // indici da 1
				double [] a = new double[k.length+1]; // indici da 1
				boolean [] j = new boolean[k.length+1]; // indici da 1
				for (int i=0; i<k.length; i++) // i indicizza k
				{
					b[i+1] = 0.25 * (y[k[i]-1+1] - y[k[i]-1-1]);
					a[i+1] = y[k[i]-1] -2*b[i+1] - y[k[i]-1-1];
					j[i+1] = a[i+1]>0; // j=0 on a plateau
				}
				// v(j)=y(k(j))+b(j).^2./a(j);
				for (int i=1; i<j.length; i++) // i indiciza j, per indicizzare v devo sottrarre 1
					if (j[i])
						v[i-1] = y[k[i-1]-1] + b[i]*b[i]/a[i];
				// From now, k is used for also for double values, so I change name
				// k(j)=k(j)+b(j)./a(j);
				// k(~j)=k(~j)+(fq(k(~j))-rs(k(~j)))/2;
				kk = MyMath.todouble(k); // indici da 0
				for (int i=1; i<j.length; i++) // i indicizza j, quindi per kk devo togliere 1
					if (j[i])
						kk[i-1] = kk[i-1] + b[i]/a[i]; // These kk are now double
					else
						kk[i-1] = kk[i-1] + (fq[(int)(kk[i-1]-1)] - rs[(int)kk[i-1]])/2; // These kk at right side are still integers 			
			}
			else
			{
				// nargin=2
			}
		}
		
		// add first and last samples if requested
		if (ny>1)
		{
			if (mode.contains("f") && (y[0]>y[1])) {} // not implemented
			if (mode.contains("l") && (y[ny-1-1]<y[ny-1])) {} // not implemented
		
			// now purge nearby peaks - note that the decision about which peaks to
		    // delete is not unique
			
			if (mode.contains("m"))
			{
				// not implemented
			}
			else if (nargin>2) // && numel(w)==1 && w>0
			{
				// not implemented
			}
		}
		else if ((ny>0) && (mode.contains("f") || mode.contains("l")))
		{
			// not implemented
		}
		if (mode.contains("v")) {} // not implemented
			
		//if ~nargout ...

		// At the end, I have to restore java indexin in the content of k
		if (kk!=null)
			MyMath.minusSelf(kk, 1);
	}

}
