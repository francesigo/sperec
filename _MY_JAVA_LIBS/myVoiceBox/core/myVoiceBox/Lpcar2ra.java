package myVoiceBox;

/**
 * Convert ar filter to inverse filter autocorrelation coefs. RA=(AR)
 * @author FS
 *
 */
public class Lpcar2ra {

	static public double [][] exe(double [][] ar) {
		int nf = ar.length;
		int p1 = ar[0].length; //[nf,p1]=size(ar);
		double [][] ra = new double[nf][p1]; //ra=zeros(nf,p1);
		
		
		for (int i=1; i<=p1; i++) // for i=1:p1
		{
			//ra(:,i)=sum(ar(:,1:p1+1-i).*ar(:,i:p1),2);
			for (int r=1; r<=nf; r++)
			{
				double s = 0.0;
				for (int c=1; c<=p1+1-i; c++)
					s += ar[r-1][c-1] * ar[r-1][i+c-2];

				ra[r-1][i-1] = s;
			}
		}
		return ra;
	}
	
	static public double [] exe(double [] ar) {
		
		double [][] x = new double[][] {ar};
		double [][] y = exe(x);
		return y[0];		
	}
}
