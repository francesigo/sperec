package myDSP;

import java.util.Arrays;

import myMath.MyMath;


public class MyComplexArrayFloat {
	
	/**
	 * Buffer: the array is stored as: Re[0], Im[0], Re[1], Im[1],...., Re[s-1], Im[s-1]
	 * where s = buffer.length/2 = number of elements
	 */
	protected float [] buffer;
	
	public float [] getBuffer() {
		return buffer;
	}
	
	/**
	 * Create a new instance given an input pre-set buffer
	 * @param buffer the buffer containing the complex array
	 */
	public MyComplexArrayFloat(float [] buffer) {
		this.buffer = buffer;
	}
	
	/**
	 * Create a new instance given real parts and imaginary parts of the complex numbers
	 * @param re array of input real parts
	 * @param im array of input imaginary parts
	 */
	public MyComplexArrayFloat(float [] re, float [] im) {
		this.buffer = new float[2*re.length];
		int j=0;
		for (int i=0; i<re.length; i++)
		{
			this.buffer[j++] = re[i];
			this.buffer[j++] = im[i];
		}
	}
	
	/**
	 * Create a new empty (zero filled) instance of a complex sequence
	 * @param n the number of elements of the sequence
	 */
	public MyComplexArrayFloat(int n) {
		this.buffer = new float[2*n];
	}
	
	public MyComplexArrayFloat addSelf(float re, float im) {
		
		int j=0;
		for (int i=0; i<this.size(); i++)
		{
			buffer[j++] += re;
			buffer[j++] += im;
		}
		return this;
	}
	
	public MyComplexArrayFloat addSelf(MyComplexArrayFloat m) {
		
		for (int i=0; i<this.buffer.length; i++)
			this.buffer[i] += m.buffer[i];
		
		return this;
	}
	
	public MyComplexArrayFloat minusSelf(MyComplexArrayFloat m) {
		
		for (int i=0; i<this.buffer.length; i++)
			this.buffer[i] -= m.buffer[i];
		
		return this;
	}
	
	public MyComplexArrayFloat minus(MyComplexArrayFloat m) {
		
		float [] dst = new float[this.buffer.length];
		float [] mb = m.buffer;
		for (int i=0; i<this.buffer.length; i++)
			dst[i] = this.buffer[i] - mb[i];
		return new MyComplexArrayFloat(dst);
	}

	
	public MyComplexArrayFloat flip() {
		
		int numEl = this.size();
		float [] b = new float[this.buffer.length];
		for (int i=0; i<numEl; i++)
		{
			int k = numEl-1-i;
			int src_real_i = 2*k;
			int src_imag_i = src_real_i+1;
			int dst_real_i = 2*i;
			int dst_imag_i = dst_real_i+1;
			b[dst_real_i] = buffer[src_real_i];
			b[dst_imag_i] = buffer[src_imag_i];
		}
		return new MyComplexArrayFloat(b);
	}
	/**
	 * 
	 * @return the number of elements
	 */
	public int size() {
		return buffer.length/2;
	}
	
	public MyComplexArrayFloat cat(MyComplexArrayFloat y) {
		float [] b = MyMath.cat(this.buffer, y.buffer);
		return new MyComplexArrayFloat(b);		
	}
	
	/**
	 * Clone method
	 */
	public MyComplexArrayFloat clone() {
		return new MyComplexArrayFloat(buffer.clone());
	}
	
	public MyComplexArrayFloat conj() {
		float [] buffer = this.buffer.clone();
		for (int i=0; i<buffer.length; i+=2)
			buffer[i+1] = -buffer[i+1];
		return new MyComplexArrayFloat(buffer);
	}
	public MyComplexArrayFloat conjSelf() {
		float [] buffer = this.buffer;
		for (int i=0; i<buffer.length; i+=2)
			buffer[i+1] = -buffer[i+1];
		return this;
	}
	
	/**
	 * 
	 * @return the float array of real parts
	 */
	public float [] getRealFloat() {
		int n2 = buffer.length/2;
		float re[] = new float[n2];
		for (int i=0; i<n2; i++)
			re[i] = buffer[2*i];
	
		return re;
	}
	
	/**
	 * Get the real part of the i-th element of the current array
	 * @param i the position of the element to be returned
	 * @return the float real part of the element i-th
	 */
	public float getRealFloat(int i) {
		return buffer[2*i];
	}
	
	/**
	 * Get all the imaginary parts of the current array
	 * @return a float array of imaginary parts
	 */
	public float [] getImagFloat() {
		int n2 = buffer.length/2;
		float im[] = new float[n2];
		for (int i=0; i<n2; i++)
			im[i] = buffer[2*i+1];

		return im;
	}
	
	/** Get the imaginary part of the i-th element of the current array
	 * @param i the position of the element to be returned
	 * @return the float imaginary part of the element i-th
	 */
	public float getImagFloat(int i) {
		return buffer[2*i+1];
	}
	
	/**
	 * Return amplitude (modulus)
	 * @return the array of amplitude values
	 */
	public float [] getAbsFloat() {
		int n2 = buffer.length/2;
		float mod[] = new float[n2];
		for (int i=0; i<n2; i++) {
			float re = buffer[2*i];
			float im = buffer[2*i+1];
			mod [i] = (float) Math.sqrt(re*re + im*im);
		}
		return mod;
	}
	
	/**
	 * Return amplitude squared
	 * @return amplitude squared
	 */
	public float [] getAbs2Float() {
		int n2 = buffer.length/2;
		float mod[] = new float[n2];
		for (int i=0; i<n2; i++) {
			float re = buffer[2*i];
			float im = buffer[2*i+1];
			mod [i] = (float) (re*re + im*im);
		}
		return mod;
	}
	
	public double [] getAbs2Double() {
		int n2 = buffer.length/2;
		double mod[] = new double[n2];
		for (int i=0; i<n2; i++) {
			float re = buffer[2*i];
			float im = buffer[2*i+1];
			mod [i] = (double) (re*re + im*im);
		}
		return mod;
	}
	
	/**
	 * Set real part and imaginary part at the provided position in the current array
	 * @param index the position of the element to be updated to the new value
	 * @param re real part to be set
	 * @param im imaginary part to be set
	 */
	public void set(int index, float re, float im) {
		buffer[2*index] = re;
		buffer[2*index+1] = im;
	}
	
	public void fill(float v) {
		Arrays.fill(buffer, v);
	}
	public void fill(float re, float im) {
		for (int i=0; i<size(); i++)
			set(i, re, im);
	}
	
	
	/**
	 * Select elements, indexes are included. Fast way
	 * @param fromIndex start indexing from this value
	 * @param toIndex up to this value
	 * @return the new sequence
	 */
	public MyComplexArrayFloat select(int fromIndex, int toIndex) {
		int n = toIndex-fromIndex+1;
		float [] buf = new float[2*n];

		System.arraycopy(this.buffer, fromIndex*2, buf, 0, 2*n);
		return new MyComplexArrayFloat(buf);	
	}
	
	/**
	 * Select a subset of the current array, according to an indexing
	 * @param fromIndex start indexing from this value
	 * @param toIndex up to this value
	 * @param step the step in the indexing; the indexing will be fromIndex, fromIndex+step, fromIndex+2*step etc..
	 * @return the new sequence
	 * @throws Exception
	 */
	public MyComplexArrayFloat select(int fromIndex, int toIndex, int step) throws Exception {
		
		if ( (fromIndex<0) || (toIndex<0) || ((fromIndex!=toIndex) && (step==0)) || ((fromIndex<toIndex) && (step<0)) || ((fromIndex>toIndex) && (step>0)))
			throw new Exception("llegal arguments in sum");
		
		if ((toIndex>fromIndex) && (step==1)) // Fast way
			return select(fromIndex, toIndex);
		
		MyComplexArrayFloat Y = new MyComplexArrayFloat(this.size());
		float [] R = this.getRealFloat();
		float [] I = this.getImagFloat();
		if (step>0)
			for (int i=fromIndex; i<=toIndex; i+=step)
				Y.set(i, R[i], I[i]);
		else
			for (int i=fromIndex; i>=toIndex; i+=step)
				Y.set(i, R[i], I[i]);
		return Y;
		
	}
	
	//====================================== OPERATIONS ===================================
	
	/**
	 * Multiplies the buffer content with the array h of real numbers. The result is stored in the buffer in the usual format:
	 * The real part is stored at <code>2*i</code>, the imaginary part <code>2*i+i</code>
	 * @param h The array with real numbers.
	 * Data and other need to be the same length.
	 */
	public void timesRealSequenceSelf(float [] h) {
		
		assert buffer.length == 2*h.length;
		if(buffer.length!=2*h.length)
			throw new IllegalArgumentException("Both arrays must have same length");
		
		/*for (int i = 1; i < buffer.length-1; i+=2) {
			int realIndex = i;
			int imgIndex = i + 1;
			float tempReal = buffer[realIndex] * hh[realIndex] + -1 * buffer[imgIndex] * hh[imgIndex];
			float tempImg = buffer[realIndex] * hh[imgIndex] + buffer[imgIndex] * hh[realIndex];
			buffer[realIndex] = tempReal;
			buffer[imgIndex] = tempImg;
		}*/
		for (int k = 0; k < h.length; k++) {
			int realIndex = 2*k;
			int imgIndex = realIndex+1;
			float tempReal = buffer[realIndex] * h[k];
			float tempImg =  buffer[imgIndex] * h[k];
			buffer[realIndex] = tempReal;
			buffer[imgIndex] = tempImg;
		}
		
	}
	
	/**
	 * Element-wise complex array multiplication
	 * @param b the complex sequence to be multiplied by the current one
	 * @return the resulting complex sequence
	 */
	public MyComplexArrayFloat arrayTimes(MyComplexArrayFloat b) {
		
		if(this.size() != b.size())
			throw new IllegalArgumentException("Both arrays with imaginary numbers should be of equal length");
		
		float [] hh = b.buffer;
		float [] dst = new float[hh.length];
		
		for (int i = 0; i < buffer.length-1; i+=2)
		{
			int realIndex = i;
			int imgIndex = i + 1;
			float tempReal = buffer[realIndex] * hh[realIndex] + -1 * buffer[imgIndex] * hh[imgIndex];
			float tempImg = buffer[realIndex] * hh[imgIndex] + buffer[imgIndex] * hh[realIndex];
			dst[realIndex] = tempReal;
			dst[imgIndex] = tempImg;
		}
		return new MyComplexArrayFloat(dst);
	}
	
	/**
	 * 
	 * @param hh
	 */
	/*public void timesComplexSequenceSelf(float [] hh) {
		
		multiplyComplexSequences(this.buffer, hh);
	}*/
	
	/**
	 * x1 will store the result
	 * @param x1
	 * @param x2
	 */
	/*static private void multiplyComplexSequences(float [] buffer, float []hh) {
		assert buffer.length == hh.length;
		if(buffer.length!=hh.length){
			throw new IllegalArgumentException("Both arrays with imaginary numbers shouldb e of equal length");
		}
		for (int i = 1; i < buffer.length-1; i+=2) {
			int realIndex = i;
			int imgIndex = i + 1;
			float tempReal = buffer[realIndex] * hh[realIndex] + -1 * buffer[imgIndex] * hh[imgIndex];
			float tempImg = buffer[realIndex] * hh[imgIndex] + buffer[imgIndex] * hh[realIndex];
			buffer[realIndex] = tempReal;
			buffer[imgIndex] = tempImg;
		}
	}*/
	
	
}
