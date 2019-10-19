package myDSP.wavelet;

import myMath.MyMath;

public class Wextend {

	public static double [] get(String type, String mode, double [] x, int lf) throws Exception {
		return get(type, mode, x, lf, "b");
	}
	
	public static double [] get(String type, String mode, double [] x, int lf, String location) throws Exception {
		//x  = wextend('1D','per',x,lf/2); // type,mode,x,lf,location
		
		if (!type.toLowerCase().equals("1d"))
			throw new Exception("Wextend: type "+type + " not supported");
		
		int sx = x.length;
		
		if (!mode.equals("per"))
				throw new Exception("Wextend: mode "+ mode + " not supported");
		
		double [] xx = null;
		// Periodization.
		if (MyMath.rem(sx, 2) >0) {
			xx = new double [sx+1];
			System.arraycopy(x, 0, xx, 0, sx);
			xx[sx] = x[sx-1];
			sx++;
		}
		else
			xx = x;
		
		int [] I = getPerIndices(sx, lf, location);
		double [] xxx = new double [I.length];
		for (int i=0; i<xxx.length; i++)
			xxx[i] = xx[I[i]];
		
		return xxx;
	}
	
	/**
	 * 
	 * @param lx
	 * @param lf
	 * @param location
	 * @return
	 * @throws Exception
	 */
	private static int [] getPerIndices(int lx, int lf, String location) throws Exception {
		if (!location.equals("b"))
			throw new Exception("Wextend: location "+ location + " not supported");
		
		int [] I = new int[lf + lx  +lf];
		int j=0;
		for (int i = lx-lf+1; i<=lx; i++)
			I[j++] = i;
		for (int i=1; i<=lx; i++)
			I[j++] = i;
		for (int i=1; i<=lf; i++)
			I[j++] = i;

		if (lx<lf)
		{
			for (int i=0; i<I.length; i++) {
				
				I[i] = MyMath.mod(I[i], lx);
				if (I[i]==0)
					I[i] = lx;

				I[i]--; // to index java array
			}
		}
		else
			for (int i=0; i<I.length; i++) 
				I[i]--; // to index java array
		
		return I;		
	}
}
