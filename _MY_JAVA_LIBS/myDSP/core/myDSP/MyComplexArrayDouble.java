package myDSP;

import java.util.Arrays;

import myMath.MyMath;


public class MyComplexArrayDouble {
	
	/**
	 * Buffer: the array is stored as: Re[0], Im[0], Re[1], Im[1],...., Re[s-1], Im[s-1]
	 * where s = buffer.length/2 = number of elements
	 * Values are in double format
	 */
	protected double [] buffer;
	
	/**
	 * Getter for the internal buffer
	 * @return the internal buffer
	 */
	public double [] getBuffer() {
		return buffer;
	}
	
	/**
	 * Build the complex array given the buffer
	 * @param buffer a memory chunk containing the complex array to be set
	 */
	public MyComplexArrayDouble(double [] buffer) {
		this.buffer = buffer;
	}
	
	/**
	 * Build the complex array given reals and imaginary parts
	 * @param re the array of real parts of the complex array
	 * @param im the array of imaginary parts of the complex array
	 */
	public MyComplexArrayDouble(double [] re, double [] im) {
		this.buffer = new double[2*re.length];
		int j=0;
		for (int i=0; i<re.length; i++)
		{
			this.buffer[j++] = re[i];
			this.buffer[j++] = im[i];
		}
	}
	
	/**
	 * Allocate room for a complex array of n elements 
	 * @param n the number of elements
	 */
	public MyComplexArrayDouble(int n) {
		this.buffer = new double[2*n];
	}
	
	// --------------------------- OPERATIONS
	
	/**
	 * Add a single value to the whole array, then store the result in itself
	 * @param re the real part of the complex number to be added to every elements of the current complex sequence
	 * @param im the imaginary part of the complex number to be added to every elements of the current complex sequence
	 * @return this instance (useful for cascade)
	 */
	public MyComplexArrayDouble addSelf(double re, double im) {
		
		int j=0;
		for (int i=0; i<this.size(); i++)
		{
			buffer[j++] += re;
			buffer[j++] += im;
		}
		return this;
	}
	
	public MyComplexArrayDouble addSelf(MyComplexArrayDouble m) {
		
		for (int i=0; i<this.buffer.length; i++)
			this.buffer[i] += m.buffer[i];
		
		return this;
	}
	
	public MyComplexArrayDouble minusSelf(MyComplexArrayDouble m) {
		
		for (int i=0; i<this.buffer.length; i++)
			this.buffer[i] -= m.buffer[i];
		
		return this;
	}
	
	public MyComplexArrayDouble minus(MyComplexArrayDouble m) {
		
		double [] dst = new double[this.buffer.length];
		double [] mb = m.buffer;
		for (int i=0; i<this.buffer.length; i++)
			dst[i] = this.buffer[i] - mb[i];
		return new MyComplexArrayDouble(dst);
	}

	
	public MyComplexArrayDouble flip() {
		
		int numEl = this.size();
		double [] b = new double[this.buffer.length];
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
		return new MyComplexArrayDouble(b);
	}
	/**
	 * 
	 * @return the number of elements
	 */
	public int size() {
		return buffer.length/2;
	}
	
	public MyComplexArrayDouble cat(MyComplexArrayDouble y) {
		double [] b = MyMath.cat(this.buffer, y.buffer);
		return new MyComplexArrayDouble(b);		
	}
	
	/**
	 * Clone method
	 */
	public MyComplexArrayDouble clone() {
		return new MyComplexArrayDouble(buffer.clone());
	}
	
	public MyComplexArrayDouble conj() {
		double [] buffer = this.buffer.clone();
		for (int i=0; i<buffer.length; i+=2)
			buffer[i+1] = -buffer[i+1];
		return new MyComplexArrayDouble(buffer);
	}
	public MyComplexArrayDouble conjSelf() {
		double [] buffer = this.buffer;
		for (int i=0; i<buffer.length; i+=2)
			buffer[i+1] = -buffer[i+1];
		return this;
	}
	
	/**
	 * 
	 * @return the double array of real parts
	 */
	public double [] getReal() {
		int n2 = buffer.length/2;
		double re[] = new double[n2];
		for (int i=0; i<n2; i++)
			re[i] = buffer[2*i];
	
		return re;
	}
	
	/**
	 * Get the real part of the i-th element of the current array
	 * @param i the position of the element to be returned
	 * @return the float real part of the element i-th
	 */
	public double getReal(int i) {
		return buffer[2*i];
	}
	
	/**
	 * Get all the imaginary parts of the current array
	 * @return a float array of imaginary parts
	 */
	public double [] getImag() {
		int n2 = buffer.length/2;
		double im[] = new double[n2];
		for (int i=0; i<n2; i++)
			im[i] = buffer[2*i+1];

		return im;
	}
	
	/** Get the imaginary part of the i-th element of the current array
	 * @param i the position of the element to be returned
	 * @return the float imaginary part of the element i-th
	 */
	public double getImag(int i) {
		return buffer[2*i+1];
	}
	
	/**
	 * Return amplitude (modulus)
	 * @return the array of amplitudes
	 */
	public double [] getAbs() {
		int n2 = buffer.length/2;
		double mod[] = new double[n2];
		for (int i=0; i<n2; i++) {
			double re = buffer[2*i];
			double im = buffer[2*i+1];
			mod [i] = (double) Math.sqrt(re*re + im*im);
		}
		return mod;
	}
	
	/**
	 * Return amplitude squared
	 * @return amplitude squared
	 */
	public double [] getAbs2() {
		
		// The number of element in the array
		int n2 = buffer.length/2;
		
		// Allocate room for the final result
		double mod[] = new double[n2];
		
		for (int i=0; i<n2; i++) {
			double re = buffer[2*i];
			double im = buffer[2*i+1];
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
	public void set(int index, double re, double im) {
		buffer[2*index] = re;
		buffer[2*index+1] = im;
	}
	
	public void fill(double v) {
		Arrays.fill(buffer, v);
	}
	public void fill(double re, double im) {
		for (int i=0; i<size(); i++)
			set(i, re, im);
	}
	
	
	/**
	 * Select elements, indexes are included. Fast way
	 * @param fromIndex start indexing from this value
	 * @param toIndex up to this value
	 * @return the new sequence
	 */
	public MyComplexArrayDouble select(int fromIndex, int toIndex) {
		int n = toIndex-fromIndex+1;
		double [] buf = new double[2*n];

		System.arraycopy(this.buffer, fromIndex*2, buf, 0, 2*n);
		return new MyComplexArrayDouble(buf);	
	}
	
	/**
	 * Select a subset of the current array, according to an indexing
	 * @param fromIndex start indexing from this value
	 * @param toIndex up to this value
	 * @param step the step in the indexing; the indexing will be fromIndex, fromIndex+step, fromIndex+2*step etc..
	 * @return the new sequence
	 * @throws Exception
	 */
	public MyComplexArrayDouble select(int fromIndex, int toIndex, int step) throws Exception {
		
		if ( (fromIndex<0) || (toIndex<0) || ((fromIndex!=toIndex) && (step==0)) || ((fromIndex<toIndex) && (step<0)) || ((fromIndex>toIndex) && (step>0)))
			throw new Exception("llegal arguments in sum");
		
		if ((toIndex>fromIndex) && (step==1)) // Fast way
			return select(fromIndex, toIndex);
		
		MyComplexArrayDouble Y = new MyComplexArrayDouble(this.size());
		double [] R = this.getReal();
		double [] I = this.getImag();
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
	public void timesRealSequenceSelf(double [] h) {
		
		assert buffer.length == 2*h.length;
		if(buffer.length!=2*h.length)
			throw new IllegalArgumentException("Both arrays must have same length");
		
		/*for (int i = 1; i < buffer.length-1; i+=2) {
			int realIndex = i;
			int imgIndex = i + 1;
			double tempReal = buffer[realIndex] * hh[realIndex] + -1 * buffer[imgIndex] * hh[imgIndex];
			double tempImg = buffer[realIndex] * hh[imgIndex] + buffer[imgIndex] * hh[realIndex];
			buffer[realIndex] = tempReal;
			buffer[imgIndex] = tempImg;
		}*/
		for (int k = 0; k < h.length; k++) {
			int realIndex = 2*k;
			int imgIndex = realIndex+1;
			double tempReal = buffer[realIndex] * h[k];
			double tempImg =  buffer[imgIndex] * h[k];
			buffer[realIndex] = tempReal;
			buffer[imgIndex] = tempImg;
		}
		
	}
	
	/**
	 * Element-wise complex array multiplication
	 * @param b the complex sequence to be multiplied with the current one
	 * @return the complex sequence resuling from multiplication
	 */
	public MyComplexArrayDouble arrayTimes(MyComplexArrayDouble b) {
		
		if(this.size() != b.size())
			throw new IllegalArgumentException("Both arrays with imaginary numbers should be of equal length");
		
		double [] hh = b.buffer;
		double [] dst = new double[hh.length];
		
		for (int i = 0; i < buffer.length-1; i+=2)
		{
			int realIndex = i;
			int imgIndex = i + 1;
			double tempReal = buffer[realIndex] * hh[realIndex] + -1 * buffer[imgIndex] * hh[imgIndex];
			double tempImg = buffer[realIndex] * hh[imgIndex] + buffer[imgIndex] * hh[realIndex];
			dst[realIndex] = tempReal;
			dst[imgIndex] = tempImg;
		}
		return new MyComplexArrayDouble(dst);
	}
	
	/**
	 * 
	 * @param hh
	 */
	/*public void timesComplexSequenceSelf(double [] hh) {
		
		multiplyComplexSequences(this.buffer, hh);
	}*/
	
	/**
	 * x1 will store the result
	 * @param x1
	 * @param x2
	 */
	/*static private void multiplyComplexSequences(double [] buffer, double []hh) {
		assert buffer.length == hh.length;
		if(buffer.length!=hh.length){
			throw new IllegalArgumentException("Both arrays with imaginary numbers shouldb e of equal length");
		}
		for (int i = 1; i < buffer.length-1; i+=2) {
			int realIndex = i;
			int imgIndex = i + 1;
			double tempReal = buffer[realIndex] * hh[realIndex] + -1 * buffer[imgIndex] * hh[imgIndex];
			double tempImg = buffer[realIndex] * hh[imgIndex] + buffer[imgIndex] * hh[realIndex];
			buffer[realIndex] = tempReal;
			buffer[imgIndex] = tempImg;
		}
	}*/
	
	
}
