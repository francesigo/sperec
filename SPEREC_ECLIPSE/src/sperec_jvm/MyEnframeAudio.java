package sperec_jvm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import sperec_jvm.audioinputsream.IMyAudioInputStream;
import sperec_jvm.audioinputsream.MyAudioSpecs;

public class MyEnframeAudio {
	private final IMyAudioInputStream iMyAudioInputStream;
	private final MyAudioSpecs myAudioSpecs;
	
	private int iWindowSizeSamples;
	private double dWindowSizeSec;
	private int iWindowSizeBytes;
	
	private int iFrameIncrementSamples;
	private double dFrameIncrementSec;
	private int iFrameIncrementBytes;
	private double dOverlapFactor;
	
	//-----
	int bytesProcessed;
	int bytesToSkip;
	boolean zeroPadFirstBuffer=false;
	boolean zeroPadLastBuffer;
	boolean stopped;
	
	float [] faDestBuffer;
	public byte [] baDestBuffer;
	
	public byte [] getByteBuffer() {
		return baDestBuffer;
	}
	
	public MyEnframeAudio(final IMyAudioInputStream iMyAudioInputStream_, double dReqFrameIncrementSec, double dReqOverlapFactor) {
		iMyAudioInputStream = iMyAudioInputStream_;
		
		myAudioSpecs = iMyAudioInputStream.getAudioSpecs();
		
		double dSampleRate = (double)myAudioSpecs.getSampleRate();
		int iBytesPerSample = myAudioSpecs.getBytesPerSample();
		
		// Compute window size
    	iWindowSizeSamples = (int)Math.round(dReqFrameIncrementSec*dReqOverlapFactor*dSampleRate);
    	dWindowSizeSec     = (double)iWindowSizeSamples/dSampleRate;
    	iWindowSizeBytes   = iWindowSizeSamples*iBytesPerSample;
    	
    	// Compute frame increment
    	iFrameIncrementSamples = (int)Math.round(dReqFrameIncrementSec*dSampleRate);
    	dFrameIncrementSec = (double)iFrameIncrementSamples/dSampleRate;
    	iFrameIncrementBytes = iFrameIncrementSamples*iBytesPerSample;
    	
    	dOverlapFactor = dWindowSizeSec/iFrameIncrementSamples;
    	
    	//---
    	stopped = false;
		bytesToSkip = 0;
		zeroPadLastBuffer = true;
		
		faDestBuffer = new float[iWindowSizeSamples]; // Però non credo che servano tutti e due
		baDestBuffer = new byte[iWindowSizeBytes];
		bytesProcessed = 0;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int readNextBlock() throws IOException {
		boolean isFirstBuffer = (bytesProcessed ==0 || bytesProcessed == bytesToSkip);
		final int offsetInBytes;	
		final int offsetInSamples;
		final int bytesToRead;
		int iSampleOverlap = iWindowSizeSamples - iFrameIncrementSamples;
		int iByteOverlap = iWindowSizeBytes - iFrameIncrementBytes;
		//Determine the amount of bytes to read from the stream
		if(isFirstBuffer && !zeroPadFirstBuffer){
			//If this is the first buffer and we do not want to zero pad the
			//first buffer then read a full buffer
			bytesToRead = iWindowSizeBytes;
			// With an offset in bytes of zero;
			offsetInBytes = 0;
			offsetInSamples=0;
		} else {
			//In all other cases read the amount of bytes defined by the step size
			bytesToRead = iFrameIncrementBytes;
			offsetInBytes = iByteOverlap; //byteOverlap;
			offsetInSamples = iSampleOverlap; //floatOverlap;
		}
		
		if(!isFirstBuffer && faDestBuffer.length == iSampleOverlap + iFrameIncrementSamples ){
			System.arraycopy(faDestBuffer, iFrameIncrementSamples, faDestBuffer, 0, iSampleOverlap);
		}
		
		int totalBytesRead = 0; // Total amount of bytes read
		int bytesRead=0; // The amount of bytes read from the stream during one iteration
		boolean endOfStream = false; // Is the end of the stream reached?
		// Always try to read the 'bytesToRead' amount of bytes.
		// unless the stream is closed (stopped is true) or no bytes could be read during one iteration 
		while(!stopped && !endOfStream && totalBytesRead<bytesToRead) {
			try{
				bytesRead = iMyAudioInputStream.read(baDestBuffer, offsetInBytes + totalBytesRead , bytesToRead - totalBytesRead);
			} catch(IndexOutOfBoundsException e) {
						// The pipe decoder generates an out of bounds if end
						// of stream is reached. Ugly hack...
						bytesRead = -1;
			}
			if(bytesRead == -1){
				// The end of the stream is reached if the number of bytes read during this iteration equals -1
				endOfStream = true;
			}else{
				// Otherwise add the number of bytes read to the total 
				totalBytesRead += bytesRead;
			}
		}
		
		if(endOfStream){
			// Could not read a full buffer from the stream, there are two options:
			if(zeroPadLastBuffer) {
				//Make sure the last buffer has the same length as all other buffers and pad with zeros
				for(int i = offsetInBytes + totalBytesRead; i < baDestBuffer.length; i++) {
					baDestBuffer[i] = 0;
				}
							
				convert(baDestBuffer, offsetInBytes, faDestBuffer, offsetInSamples, iFrameIncrementSamples);
			} else {
				// Send a smaller buffer through the chain.
				byte[] audioByteBufferContent = baDestBuffer;
				baDestBuffer = new byte[offsetInBytes + totalBytesRead];
				for(int i = 0 ; i < baDestBuffer.length ; i++){
					baDestBuffer[i] = audioByteBufferContent[i];
				}
				int totalSamplesRead = totalBytesRead/myAudioSpecs.getBytesPerSample();//format.getFrameSize();
				faDestBuffer = new float[offsetInSamples + totalBytesRead/myAudioSpecs.getBytesPerSample()]; //format.getFrameSize()];
				convert(baDestBuffer, offsetInBytes, faDestBuffer, offsetInSamples, totalSamplesRead);				
			}			
		} else if(bytesToRead == totalBytesRead) {
			// The expected amount of bytes have been read from the stream.
			if(isFirstBuffer && !zeroPadFirstBuffer){
				convert(baDestBuffer, 0, faDestBuffer, 0, faDestBuffer.length);
			}else{
				convert(baDestBuffer, offsetInBytes, faDestBuffer, offsetInSamples, iFrameIncrementSamples);
			}
		} else if(!stopped) {
			// If the end of the stream has not been reached and the number of bytes read is not the
			// expected amount of bytes, then we are in an invalid state; 
			throw new IOException(String.format("The end of the audio stream has not been reached and the number of bytes read (%d) is not equal "
					+ "to the expected amount of bytes(%d).", totalBytesRead,bytesToRead));
		}
		
		
		// Makes sure AudioEvent contains correct info.
		/*audioEvent.setFloatBuffer(audioFloatBuffer);
		audioEvent.setOverlap(offsetInSamples);*/
		
		if (totalBytesRead>0) {
			bytesProcessed+=totalBytesRead;
		}
		return totalBytesRead;
	}
	
	private void convert(byte [] ba, int baOffset, float [] fa, int faOffset, int numSamples) {
			final ByteBuffer byteBuffer = ByteBuffer.wrap(ba, baOffset, numSamples*myAudioSpecs.getBytesPerSample()).order(ByteOrder.LITTLE_ENDIAN);
	        final ShortBuffer shorts = byteBuffer.asShortBuffer();
	        for (int i = 0; i < numSamples; i++) {
	            final short raw = shorts.get(i);
	            final float amplitude = (float) raw / (float) Short.MAX_VALUE;
	            faDestBuffer[faOffset + i] = amplitude;
	        }
		}
	
}
