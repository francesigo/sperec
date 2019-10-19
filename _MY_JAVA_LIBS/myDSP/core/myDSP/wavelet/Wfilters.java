package myDSP.wavelet;

public class Wfilters {

	
	public static double [][] getFilters(String wname, String type) throws Exception {
		
		double [][]varargout = new double[2][];
		
		if (!type.equals("d"))
			throw new Exception("Wfilters: unsupported wavelet filter type");
		if (!(wname.equals("sym8")))
			throw new Exception ("Wfilters: "+ wname + " wavelet not yet supported");
		int wtype = 1; // orth. wavelet
		if ((wtype!=1) && (wtype!=2))
			throw new Exception("Wfilters : wavelet type must be orthogonal or biorthogonal");
		
		double F[] = Symwavf.getfilter(wname);
		Orthfilt OF = null;
		if (wtype==1)
			OF = new Orthfilt(F);
		else
			throw new Exception("Wfilters: wavelet type not supported yet");
		
		String o = type.substring(0, 1).toLowerCase();
		switch (o) {
		case "d": varargout[0] = OF.LO_D; varargout[1] = OF.HI_D; break;
		case "r": varargout[0] = OF.LO_R; varargout[1] = OF.HI_R; break;
		case "l": varargout[0] = OF.LO_D; varargout[1] = OF.LO_R; break;
		case "h": varargout[0] = OF.HI_D; varargout[1] = OF.HI_R; break;
		}
		
		return varargout;
		
	}
}
