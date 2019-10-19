package myDSP;

/**
 * Calculate energy waveform
 * @author FS
 *
 */
public class MyTeager {

	static public double [] process(double [] z) {
		
		int k = z.length;
		double [] y = new double [k];
		for (int n=1; n<k-1; n++)
			y[n] = Math.abs(z[n])*Math.abs(z[n]) - z[n+1]*z[n-1]; //y(2:k-1,:)=z(2:k-1,:).*conj(z(2:k-1,:))-z(3:k,:).*conj(z(1:k-2,:))

		y[0] = 2*y[1]-y[2]; 	//y(1,:)=2*y(2,:)-y(3,:);             % linearly interpolate the end points

		y[k-1] = 2*y[k-2]-y[k-3]; //y(k,:)=2*y(k-1,:)-y(k-2,:);
		
		return y;    
	}
}
