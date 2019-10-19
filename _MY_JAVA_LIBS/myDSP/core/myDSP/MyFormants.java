package myDSP;

import myMath.MyMath;
import myVoiceBox.Lpcar2fm;
import myVoiceBox.Lpcauto;

public class MyFormants {

	private Lpcar2fm lpcar2fm;
	private double [] times = null; // Optional: can be null, or time instants. This is set by the application
	
	public double [] getTimes() {
		return times;
	}
	public void setTimes(double [] times) {
		this.times = times;
	}
	public double [][] getNormalizedFormantFrequencies() {
		return lpcar2fm.ff;
	}
	public void setNormalizedFormantFrequencies(double [][] ff) {
		lpcar2fm.ff = ff;
	}
	
	public int [] getNumberOfFormants() {
		return lpcar2fm.nn;
	}
	public void setNumberOfFormants(int [] nn) {
		lpcar2fm.nn =nn;
	}
	
	public double [][] getFormantAmplitudes () {
		return lpcar2fm.aa;
	}
	public void setFormantAmplitudes (double [][] aa) {
		lpcar2fm.aa = aa;
	}
	
	public double [][] getFormantBandwidths () {
		return lpcar2fm.bb;
	}
	public void setFormantBandwidths (double [][] bb) {
		lpcar2fm.bb = bb;
	}
	
	public int getNumberOfFrames () {
		return lpcar2fm.nn.length;
	}
	
	public MyFormants(float [][] frames, int p) throws Exception {
		Lpcauto lpcauto = Lpcauto.fromFloatFramed(frames, p);
		double[][]ar = lpcauto.getAR();
		double [] e = lpcauto.getE();
		lpcar2fm = new Lpcar2fm(ar, -500);
		// The formants (ff are in normalized Hz, must be multiplicated by the sample rate/2)
	}

	public MyFormants() {
		lpcar2fm = new Lpcar2fm();
	}
	
	/**
	 * Select a subset of formant values
	 * @param keep the array of boolean indexes of samples to be selected
	 * @param maxNumFF the max formant (e.g. 4, will select F1, F2, F3, F4)
	 * @return a new MyFormant instance
	 * @throws Exception
	 */
	public MyFormants select(boolean [] keep, int maxNumFF) throws Exception {
		
		if (getNumberOfFrames() != keep.length)
			throw new Exception("Mismatch");
		
		int [] keepids = MyMath.find(keep);
		return select(keepids, maxNumFF);
	}
		
	/**
	 * Select a subset of formant values
	 * @param keepids the array of integer indexes of samples to be selected
	 * @param maxNumFF the max formant (e.g. 4, will select F1, F2, F3, F4)
	 * @return a new MyFormant instance
	 * @throws Exception
	 */
	public MyFormants select(int [] keepids, int maxNumFF) throws Exception {
		
		int totkeep = keepids.length;
		double [][] src_ff = this.getNormalizedFormantFrequencies(); // numFrames x ...
		double [][] src_aa = this.getFormantAmplitudes();
		double [][] src_bb = this.getFormantBandwidths();
		int [] src_nn = this.getNumberOfFormants();

		double [][] ff = new double[totkeep][maxNumFF];
		double [][] aa = new double[totkeep][maxNumFF];
		double [][] bb = new double[totkeep][maxNumFF];
		int [] nn = new int[totkeep];
		for (int j=0; j<totkeep; j++)
		{
			int id = keepids[j];
			int nf = (src_nn[id]<maxNumFF) ? src_nn[id] : maxNumFF;
			
			System.arraycopy(src_ff[id], 0, ff[j], 0, nf);
			System.arraycopy(src_aa[id], 0, aa[j], 0, nf);
			System.arraycopy(src_bb[id], 0, bb[j], 0, nf);
			nn[j] = nf;
		}
			
		MyFormants newForm = new MyFormants();
		newForm.setFormantAmplitudes(aa);
		newForm.setFormantBandwidths(bb);
		newForm.setNormalizedFormantFrequencies(ff);
		newForm.setNumberOfFormants(nn);
		if (times!=null)
			newForm.times = MyMath.select(times, keepids);
		
		return newForm;
	}
	
	
	/**
	 * Select a subset of formant values
	 * @param keep the array of boolean indexes of samples to be selected
	 * @return a new MyFormant instance
	 * @throws Exception
	 */
	public MyFormants select(boolean [] keep) throws Exception {
			
		if (getNumberOfFrames() != keep.length)
			throw new Exception("Mismatch");

		int [] keepids = MyMath.find(keep);
		return select(keepids);
	}

	/**
	 * Select a subset of formant values
	 * @param keepids the array of integer indexes of samples to be selected
	 * @return a new MyFormant instance
	 * @throws Exception
	 */
	public MyFormants select(int [] keepids) throws Exception {
		
		int totkeep = keepids.length;
		double [][] src_ff = this.getNormalizedFormantFrequencies(); // numFrames x ...
		double [][] src_aa = this.getFormantAmplitudes();
		double [][] src_bb = this.getFormantBandwidths();
		int [] src_nn = getNumberOfFormants();

		double [][] ff = new double[totkeep][];
		double [][] aa = new double[totkeep][];
		double [][] bb = new double[totkeep][];
		int [] nn = new int[totkeep];
		
		for (int j=0; j<totkeep; j++)
		{
			int id = keepids[j];
			
			ff[j] = src_ff[id];
			aa[j] = src_aa[id];
			bb[j] = src_bb[id];
			nn[j] = src_nn[id];
		}
		
		MyFormants newForm = new MyFormants();
		newForm.setFormantAmplitudes(aa);
		newForm.setFormantBandwidths(bb);
		newForm.setNormalizedFormantFrequencies(ff);
		newForm.setNumberOfFormants(nn);
		
		return newForm;
	}
}
