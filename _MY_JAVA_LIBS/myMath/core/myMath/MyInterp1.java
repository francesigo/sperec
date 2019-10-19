package myMath;

//import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
//import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * It is just a convenient wrapper for the Apache commons' LinearInterpolator class
 * @author FS
 *
 */
public class MyInterp1 {

	
	/*public static double[] interp1(double[] sourceX, double[] noisyY, double[] estimateX) {

		if(sourceX.length < 2)
			return new double[sourceX.length];
		
		LinearInterpolator interpolator = new LinearInterpolator();		
		PolynomialSplineFunction estimateFunc = interpolator.interpolate(sourceX, noisyY);
		double[] result = new double[estimateX.length];
		for(int i =0; i < estimateX.length;i++)
		{
			if(estimateFunc.isValidPoint(estimateX[i]))
			{
				result[i] = estimateFunc.value(estimateX[i]);
			}
			else
			{
				result[i] = Double.NaN;
			}
		}
		return result ;
	}*/
	
	// suppongo x ordinato, xi pure
	public static double[] interp1(double[] x, double[] y, double[] xi) {
		
		int Ni = xi.length;

		int isx = 0;
		int idx = 0;
		
		double xmin = x[0];
		double xmax = x[x.length-1];
		
		double [] res = new double [Ni];
		
		for (int i=0; i<Ni; i++ )
		{
			double p = xi[i];
			if ((p<xmin) || (p>xmax))
			{
				res[i] = Double.NaN;
			}
			else
			{
				// Need to find the isx starting from the current value
				while ((isx+1<x.length) && (x[isx+1]<=p))
					isx++;

				if (x[isx] == p) {
					res[i] = y[isx];
				}
				else
				{
					// Right index
					idx = isx+1;

					double xsx = x[isx]; // Left point
					double xdx = x[idx]; // Right point
					double dex = xdx-xsx; // Interval amplitude
					double w1 = (xdx-p)/dex; // Weight

					double ysx = y[isx]; // Left ordinate
					double ydx = y[idx]; // Right ordinate

					double yi = ysx * w1 + ydx * (1-w1); // Interpolation result
					res[i] = yi;
				}
			}
		}

		return res;		
	}
	
	// ===================================== Float version
	
	/*public static float [] interp1(double[] sourceX, float[] noisyY, double[] estimateX) {
		
		// Convert float[] to double []
		double [] y = new double[noisyY.length];
		for (int i =0; i<y.length; i++)
			y[i] = noisyY[i];
		
		double [] d = interp1(sourceX, y, estimateX);
		
		// Convert double[] to float []
		float [] yy = new float[y.length];
		for (int i=0; i<yy.length; i++)
			yy[i] = (float)d[i];
		
		return yy;
	}*/

	
}
