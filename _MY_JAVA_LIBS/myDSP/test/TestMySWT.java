package myDSP.test;

import java.util.ArrayList;

import com.mathworks.matlab.types.Struct;

import myDSP.wavelet.Dyadup;
import myDSP.wavelet.MySWT;
import myDSP.wavelet.Wconv1;
import myDSP.wavelet.Wextend;
import myDSP.wavelet.Wfilters;
import myDSP.wavelet.Wkeep1;
import myMath.MyMath;
import myMatlabConnection.MyMatlabConnection;

public class TestMySWT {
	
	MyMatlabConnection matlab = null;
	
	public TestMySWT() throws Exception {
		matlab = new MyMatlabConnection();
	}
	
	static class MySWT_DEVEL {

		public double [][] SWA;
		public double [][] SWD;
		
		public MySWT_DEVEL() {
			SWA = null;
			SWD = null;
		}
		
		public void execute(double [] x, int n, String wname, MyMatlabConnection matlab) throws Exception {
			
			//swt(y, levels, 'sym8')
			int s = x.length;
			double pow = Math.pow(2, n);
			
			if ( s % pow > 0)
				throw new Exception (this.getClass().getName() + ": invalid input length");
			
			if (!(wname.equals("sym8")))
				throw new Exception (this.getClass().getName() + ": " + wname + " wavelet not yet supported");
			
			int wtype = 1; // orth. wavelet
			
			if ((wtype!=1) && (wtype!=2))
				throw new Exception(this.getClass().getName() + ": wavelet type must be orthogonal or biorthogonal");
			
			double [][] ff = Wfilters.getFilters(wname, "d");
			double [] lo = ff[0];
			double [] hi = ff[1];
			
			// Compute stationary wavelet coefficients.
			int evenoddVal = 0;
			int evenLEN    = 1;
			
			double [][] swd = new double[n][];
			double [][] swa = new double[n][];
			
			for (int k=0; k<n; k++)
			{
				System.out.println("k=" + k);
				 // Extension.
				 int   lf = lo.length;
				 double [] xe  = Wextend.get("1D", "per", x, lf/2);
				 
				 MyMath.compare_vectors("wextend: ", xe, (double [])matlab.eng.feval("wextend", (double)1, "per", x, lf/2), 1e-20);
				 
				 // Decomposition
				 double [] temp1 = Wconv1.get(xe,hi);
				 swd[k] = Wkeep1.get(temp1, s, lf+1);
				 MyMath.compare_vectors("wconv1: ", temp1, (double [])matlab.eng.feval("wconv1", xe, hi), 1e-20);
				 MyMath.compare_vectors("wkeep1: ", swd[k], (double [])matlab.eng.feval("wkeep1", temp1, s, (double)(lf+1)), 1e-20);
				 
				 
				 swa[k] = Wkeep1.get(Wconv1.get(xe,lo), s, lf+1);
				 
				 // upsample filters.
				 double [] newlo = Dyadup.get(lo, evenoddVal, evenLEN);
				 MyMath.compare_vectors("Dyadup: ", newlo, (double [])matlab.eng.feval("dyadup", lo, (double)evenoddVal, (double)evenLEN), 1e-20);
				 lo = newlo;
				 
				 double [] newhi = Dyadup.get(hi, evenoddVal, evenLEN);
				 MyMath.compare_vectors("Dyadup: ", newhi, (double [])matlab.eng.feval("dyadup", hi, (double)evenoddVal, (double)evenLEN), 1e-20);
				 hi = newhi;
				 
				 // New value of x
				 x = swa[k];
			}
			
			SWA = swa;
			SWD = swd;
		}
	}
	
	static class S {
		double mean;
		double std;
		double ske;
		double kur;
		double ene;
		double ent;
		double cv; // used in FG5 only
		
		/**
		 * Return the features of S as array of double
		 * @return
		 */
		public double [] getFea() {
			return new double [] {mean, std, ske, kur, ene, ent};
		}
		
		/**
		 * Compute mean, std, ske, kur, ene, ent, given the signal x
		 * @param x
		 * @throws Exception 
		 */
		public S(double [] x) throws Exception {
			// To save computation, better to compute altogheter
			mean = MyMath.mean(x);
			double acc_std = 0.0;
			double acc_ske = 0.0;
			double acc_kur = 0.0;
			ene = 0.0;
			
			int n = x.length;
			for (int i=0; i<n; i++) {
				double d = x[i]-mean;
				double d2 = d*d;
				double d3 = d*d2;
				double d4 = d*d3;
				
				acc_std += d2;
				acc_ske += d3;
				acc_kur += d4;
				
				ene += (x[i]*x[i]);
			}
			std = Math.sqrt(acc_std/(n-1));
			double sigma = Math.sqrt(acc_std/n);
			double sigma3 = sigma*sigma*sigma;
			double sigma4 = sigma*sigma3;
			ske = (acc_ske/n)/sigma3;
			kur = (acc_kur/n)/sigma4;
			
			// entropy
			ArrayList<Double> uniques = new ArrayList<Double>();
			ArrayList<Integer> counts = new ArrayList<Integer>();
			
			for (int i=0; i<n; i++)
			{
				double v = x[i];
				// Cercalo tra gli unici
				int index = -1;
				for (int j=0; j<uniques.size(); j++)
				{
					Double u = uniques.get(j);
					if (u.equals(v)) // // Found
					{
						counts.set(j, counts.get(j)+1); // Increment the count
						index = j; // i.e.: found
						break;
					}
				}
				if (index<0) // Not found
				{
					// add
					uniques.add(v);
					counts.add(1);
				}
			}
			ent = 0;
			for (int i =0 ; i<counts.size(); i++)
			{
				double p = (double)counts.get(i)/n;
				ent -= (p * Math.log(p));
			}
			ent /=Math.log(2);
			// entropy
			/*
			double [] c = new double[n];
			for (int i=0; i<n; i++)
				for(int j=0; j<n; j++)
					if (x[j]==x[i])
						c[i]++;
			
			double sp = MyMath.sum(c);
			ent = 0;
			for (int i=0; i<n; i++) {
				double p = c[i]/sp;
				ent -= (p * Math.log(p));
			}
			ent /=Math.log(2);
			*/
		}
	}
	
	
	

	public static void main(String[] args ) throws Exception {
		
		TestMySWT me = new TestMySWT();
		MyMatlabConnection matlab = me.matlab;
		
		double [] y = new double [] {93.36881166666394, 95.23412505715099, 104.24691221421787, 103.78170028979365, 102.93678868004204, 101.95355624236106, 100.9979843496918, 100.16750517081267, 99.53721642774924, 99.09462377458439, 98.86647237631803, 99.06216526962712, 99.81502185546071, 100.98029563252285, 102.44484021941676, 103.91801969865315, 97.24220511896536, 89.52037584658476, 90.33076258204639, 91.34904832904391, 92.5469783735009, 133.4262499779646, 133.99004058710872, 134.24070998061683, 134.08817606309253, 133.47239700785948, 132.45909139867368, 128.425767145711, 121.97748747278638, 119.81996343851526, 117.6656683251187, 115.721311020834, 114.21217393312288, 113.40562802771004, 113.34176908714095, 113.97220397375594, 115.08699574980616, 115.87234786793954, 115.8540735898251, 115.90932860409204, 115.88637712523857, 115.87478900675183, 116.07972738887284, 116.56790248050055, 117.20179391745133, 117.76796519947291, 118.0074789039615, 117.71469332046219, 116.85169983962606, 115.39555482731369, 113.2997881281851, 110.77152269436593, 108.30358359902897, 106.17406344200381, 104.53888375665753, 103.48004645233216, 103.02794768751133, 103.16227135219137, 103.83732892765116, 104.92021910865004, 106.26368935339346, 107.67334056579375, 108.9021477833779, 109.70084689156744, 109.756773719814, 109.08057463647069, 108.10889339188935, 107.12100411476258, 106.23086000538619, 105.63374642662797, 105.68020429783633, 106.57017996183194, 108.25311800172344, 110.46734195564025, 112.85148894946121, 114.9215076397657, 116.2829319718147, 116.72532850400219, 116.37840522658647, 115.46917396373965, 114.24018804776026, 112.0137393673804, 105.55065696416501, 103.401597964633, 89.3761935746578, 89.85875317831471, 90.50084589217525, 91.2615140424637, 92.08369022410186, 92.92474141098772, 93.7877804716056, 94.6276216361583, 95.30880788409141, 93.42521896075607, 94.42385941087768, 95.64276285352285, 97.19859127605774, 121.83813467048196, 134.10718746485276, 129.9989530341219, 124.81024646227281, 118.76836845380325, 112.30234059726877, 106.08716364888987, 100.78090058441552, 96.712637031042, 93.99792953464092, 92.60741959272349, 92.28179502288384, 92.69828371165296, 93.50661516076221, 94.50375157343535, 95.67981498040908, 96.97115716016872, 98.21929060950815, 99.11981958765409, 99.54298270492752, 99.50122814827716, 99.00472508047383, 98.3220660166829, 97.6058760314062, 96.9680265978372, 96.53868339562666, 96.39670932806948, 96.55071149650544, 96.95408310636073, 97.5282912813017, 98.2089624584441, 98.88411710578788, 94.854884447774, 92.63407406558208, 94.12821887349094, 95.64871999989137, 96.99544453439452, 98.06447896728051, 98.82655408445115, 99.32956861854626, 99.69934021533238, 100.11355406846073, 100.8499379998477, 102.17782369640696, 104.34677086823106, 107.60722441959399, 112.13836917294947, 117.84578061678536, 166.90921356640982, 171.53975496574634, 174.40362337938421, 175.35206094996863, 174.1294849027785, 171.23399069508986, 134.068438134851, 73.89387993554533, 73.94888433765072, 74.41605874951824, 75.2458699678875, 76.42018814723572, 77.94015610347881, 115.63125585608745, 172.81867286005794, 172.55996225010253, 165.142493189301, 159.552184169412, 151.86024970799582, 142.49817585576494, 132.73032616255477, 123.49054823814299, 115.3710692026401, 108.61376342686438, 103.30285224204317, 99.46293982091318, 96.87879791074715, 95.06353053897162, 93.8946220451567, 93.30259371982879, 93.02581601903415, 92.81006987203513, 92.5584385195603, 92.26979892502442, 92.00130198530263, 92.4381873412388, 94.9898031444415, 94.08677674252135, 93.14474554678272, 92.25481806769756, 91.56684293941441, 91.07458084244914, 90.68615098813052, 90.30489153386974, 89.75706900302575, 88.82018643959664, 87.50517627142838, 86.07566464024265, 84.9372886496775, 84.34246464488811, 84.50803789491329, 85.58052644413627, 87.4788930225995, 90.10423606332418, 93.32800834323176, 96.91400727071388, 100.45833740061009, 103.45109513806348, 105.5495440452726, 93.5504219950696, 96.12114976826156, 99.3069959126067, 102.7499620808429, 106.15967861904338, 109.3324625387852, 112.0503267998274, 114.44701829764048, 116.6219323570342, 118.81657036814907, 121.10002666554904, 123.27166471984599, 125.06540280527354, 126.3756027331089, 127.24542891228441, 127.52025263105548, 126.85957262511911, 125.13894835737744, 122.70816962538524, 119.85516618471499, 116.87075386095023, 113.97765990485925, 111.2549311390547, 108.73444281780014, 106.47261183317156, 104.58851830902341, 103.1944790932208, 102.43281588956252, 102.30633884762804, 119.29946492114698, 120.63291686858707, 121.77292497727612, 122.8702175685419, 123.82965190358541, 124.29719300160231, 124.35670467037431, 124.29796223857325, 124.14495180794421, 123.7890326723855, 123.35245583495586, 122.953545519946, 122.50898394708972, 121.83571399130031, 120.91279264947983, 119.85145965700441, 118.9682542057999, 118.46369369045004, 118.53409661592855, 119.24973458822672, 120.33497090106113, 121.57869238001848, 122.86097210710915};
		int levels = 8;
		String name = "sym8";
		
		//MySWT SWT = new MySWT();
		MySWT_DEVEL SWT = new MySWT_DEVEL();
		SWT.execute(y,  levels,  name, matlab);
		double [][] SWA = SWT.SWA;
		double [][] SWD = SWT.SWD;
		
		Struct MAT = matlab.eng.feval("mySwt", y);
		
		double [][] mswa = (double [][])MAT.get("SWA");
		double [][] mswd = (double [][])MAT.get("SWD");
		
		MyMath.compare("\nTestSWT: SWA: ", SWA ,  mswa, 1E-18);
		MyMath.compare("\nTestSWT: SWD: ", SWD ,  mswd, 1E-18);
		
		MyMath.compare_vectors("\nTestSWT: SWA[7]: ", SWA[7], mswa[7], 1E-18);
		
		S JS = new S(SWA[7]);
		S MS = new S(mswa[7]);
		
		MyMath.compare_vectors("\nTestSWT: S(SWA[7]): ", JS.getFea(), MS.getFea(), 1E-18);

		// Test skewness....
		double jske = MS.ske; 											// computed by java code on matlab data
		double mske = (double)matlab.eng.feval("skewness", mswa[7]); 	// computed by matlab conde on matlab data
		System.out.println("Skeness (j) = " + jske);
		System.out.println("Skeness (m) = " + mske);
		
		System.out.println("DONE");
	}
}
