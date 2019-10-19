package myVoiceBox;

import myMath.MyMath;

/**
 * Convert autoregressive coefficients to reflection coefficients
 *
 */
public class Lpcar2rf {

	
	/**
	 * Compute reflection coefficients from autoregressive coefficients, frame by frame
	 * @param ar Autoregressive coefficients, frame by frame: ar[0] are the coefficients of the first frame, etc.
	 * @return Reflection coefficients with rf[i][0]=1 for each i
	 */
	public static double [][]exe (double [][] ar) {
		int nf = ar.length;
		double [][] rf = new double[nf][];
		
		for (int i=0; i<nf; i++)
			rf[i] = exe(ar[i]);
		
		return rf;
	}
	
	
	/**
	 * Compute reflection coefficients from autoregressive coefficients on a single frame
	 * @param ar Autoregressive coefficients
	 * @return Reflection coefficients with rf[0]=1
	 */
	public static double [] exe (double [] ar) {
		
		int p1 = ar.length;
		double [] rf = new double[ar.length];
		
		if (p1==1)
			rf[0] = p1;
		else
		{
			// if any(ar(:,1)~=1) , ar=ar./ar(:,ones(1,p1)); end
			// Note that the input ar are unchanged
			ar = MyMath.times(ar, 1/ar[0]);
			
			rf = ar; // Just a name change;
			
			//sSystem.out.println(Arrays.toString(rf)); // debug
			
			for (int j=p1-1; j>=2; j--) { //for j = p1-1:-1:2
			 	   
				  double k = rf[j]; //k = rf(:,j+1);
				  double d = 1.0/(1-k*k); // d = (1-k.^2).^(-1);
				  
				  //wj=ones(1,j-1);
	      		  //rf(:,2:j) = (rf(:,2:j)-k(:,wj).*rf(:,j:-1:2)).*d(:,wj);
				  double [] temp = new double[j];
				  for (int i=2; i<=j; i++)
					  temp[i-1] = (rf[i-1] - k*rf[j+1-i])*d;
				  for (int i=2; i<=j; i++)
					  rf[i-1] = temp[i-1];
				  
				  //System.out.println(Arrays.toString(rf)); // debug
			}
		}
		return rf;
	}
}
