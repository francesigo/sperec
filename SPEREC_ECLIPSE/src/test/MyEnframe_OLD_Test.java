package test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import sperec_common.MyWavFile;
import sperec_common.WavFileException;
import sperec_jvm.MyEnframe_OLD;

public class MyEnframe_OLD_Test {

	/*************************************************************************************************/
	/*
     * A simple test of working.
     * Get a file WAV, and loop for each overlapped frame, concatenating in a buffer
     * (without knowing in advance the total number of samples.
     * Then save to disk.
     * Use BYTE
     */
	public static void test_enframe_4() throws IOException, WavFileException {
    	String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
    	String sAudioFilename = "AUDIO-01_cut-11025.wav";
    	String sOutAudioFolder = "C:/Users/FS/Desktop";
    	String sOutAudioFilename = sAudioFilename + "_TEST4.wav";
    			
    	MyWavFile oWriteWavFile = null;
    	    	
    	//Specs
    	double dReqFrameIncrementSec = 0.010; // The same of vadsohn...
    	double dReqOverlapFactor     = 2.0;
    	
    	String sAudioFileFullPath = sAudioFolder + "/" + sAudioFilename;

    	MyEnframe_OLD oMyEnframe = new MyEnframe_OLD(sAudioFileFullPath, dReqFrameIncrementSec, dReqOverlapFactor, MyEnframe_OLD.DataFormat.BYTE);
    	
    	int iNumChannels     = oMyEnframe.getNumChannels();
    	int iWindowSizeBytes = oMyEnframe.getWindowSizeBytes();
    	int iValidBits       = oMyEnframe.getValidBits();
    	int iNumOutSamples   = oMyEnframe.getOutputNumSamples();
    	int iNumOutBytes     = oMyEnframe.getOutputNumBytes();
    	double dSampleRate   = oMyEnframe.getSampleRate();
    	
    	// The destination buffer for overlapped frames
    	double [] daDestBuffer = new double [iNumOutSamples];
    	int iDestBufferOffset = 0;
    	
    	// The window for bytes operations
    	byte [] baWindowBuffer = new byte [iWindowSizeBytes];
    	
    	// To convert bytes into doubles
    	ByteBuffer bb;
    	ShortBuffer shorts;
		while(oMyEnframe.readFrame(baWindowBuffer) > 0) {
			bb = ByteBuffer.wrap(baWindowBuffer, 0, baWindowBuffer.length).order(ByteOrder.LITTLE_ENDIAN);
			shorts = bb.asShortBuffer();
			for (int i=0; i<shorts.capacity(); i++) {
				daDestBuffer[iDestBufferOffset++] = shorts.get(i) / (double)Short.MAX_VALUE;
			}
			System.out.println(""+iDestBufferOffset+" / "+ iNumOutSamples);
		}
		// Now iDestBufferOffset is the amount of bytes written into the buffer
		
		// Write as wav to disk
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
		oWriteWavFile = MyWavFile.newWavFile(new File(sOutAudioFullPath), iNumChannels, (long)iDestBufferOffset, iValidBits, (long)dSampleRate);
		oWriteWavFile.writeFrames(daDestBuffer, iDestBufferOffset);
    	oWriteWavFile.close();
    	
    	System.out.println("test_enframe_4: DONE");
    }
    
	/*
     * A simple test of working.
     * Get a file WAV, and loop for each overlapped frame, concatenating in a buffer
     * (without knowing in advance the total number of samples.
     * Then save to disk.
     * Use DOUBLE
     */
    public static void test_enframe_3() throws IOException, WavFileException {
    	String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
    	String sAudioFilename = "AUDIO-01_cut-11025.wav";
    	String sOutAudioFolder = "C:/Users/FS/Desktop";
    	String sOutAudioFilename = sAudioFilename + "_TEST3.wav";
    			
    	MyWavFile oWriteWavFile = null;
    	    	
    	//Specs
    	double dReqFrameIncrementSec = 0.010; // The same of vadsohn...
    	double dReqOverlapFactor     = 2.0;
    	
    	String sAudioFileFullPath = sAudioFolder + "/" + sAudioFilename;

    	MyEnframe_OLD oMyEnframe = new MyEnframe_OLD(sAudioFileFullPath, dReqFrameIncrementSec, dReqOverlapFactor);
    	
    	int iNumChannels       = oMyEnframe.getNumChannels();
    	int iWindowSizeSamples = oMyEnframe.getWindowSizeSamples();
    	int iValidBits         = oMyEnframe.getValidBits();
    	int iNumOutSamples     = oMyEnframe.getOutputNumSamples();
    	double dSampleRate     = oMyEnframe.getSampleRate();
    	
    	double [] daDestBuffer = new double [iNumOutSamples];
    	int iDestBufferOffset = 0;
    	
    	double [] daWindowBuffer = new double [iWindowSizeSamples];
    	
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;

		while(oMyEnframe.readFrame(daWindowBuffer) > 0) {
			System.arraycopy(daWindowBuffer, 0, daDestBuffer, iDestBufferOffset, daWindowBuffer.length);
			iDestBufferOffset += daWindowBuffer.length;
		}
		// Now iDestBufferOffset is the amount of samples written not the buffer
		
		// Write as wav to disk
		oWriteWavFile = MyWavFile.newWavFile(new File(sOutAudioFullPath), iNumChannels, (long)iDestBufferOffset, iValidBits, (long)dSampleRate);
		oWriteWavFile.writeFrames(daDestBuffer, iDestBufferOffset);
    	oWriteWavFile.close();
    	
    	System.out.println("test_enframe_3: DONE");
    }
    
    
	/*
     * A simple test of working. Get a file WAV, and loop for each overlapped frame: save to disk
     */
    public static void test_enframe_2() throws IOException, WavFileException {
    	String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
    	String sAudioFilename = "AUDIO-01_cut-11025.wav";
    	String sOutAudioFolder = "C:/Users/FS/Desktop";
    	String sOutAudioFilename = sAudioFilename + "_TEST2.wav";
    			
    	MyWavFile oWriteWavFile = null;
    	    	
    	//Specs
    	double dReqFrameIncrementSec = 0.010; // The same of vadsohn...
    	double dReqOverlapFactor     = 2.0;
    	
    	String sAudioFileFullPath = sAudioFolder + "/" + sAudioFilename;

    	MyEnframe_OLD oMyEnframe = new MyEnframe_OLD(sAudioFileFullPath, dReqFrameIncrementSec, dReqOverlapFactor);
    	
    	int iNumChannels       = oMyEnframe.getNumChannels();
    	int iWindowSizeSamples = oMyEnframe.getWindowSizeSamples();
    	int iValidBits         = oMyEnframe.getValidBits();
    	int iNumOutSamples     = oMyEnframe.getOutputNumSamples();
    	double dSampleRate     = oMyEnframe.getSampleRate();
    	
    	
    	double [] daWindowBuffer = new double [iWindowSizeSamples];

    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;

		oWriteWavFile = MyWavFile.newWavFile(new File(sOutAudioFullPath), iNumChannels, (long)iNumOutSamples, iValidBits, (long)dSampleRate);

		while(oMyEnframe.readFrame(daWindowBuffer) > 0) {
			oWriteWavFile.writeFrames(daWindowBuffer, iWindowSizeSamples);
		}
    	oWriteWavFile.close();
    	
    	System.out.println("test_enframe_2: DONE");
    }
   
	/*
     * A simple test of working
     */
    public static void test_enframe_1() throws IOException, WavFileException {
    	String sAudioFolder = "C:/Users/FS/Google Drive/_DOTTORATO/Monologhi_001_150_11025Hz";
    	String sAudioFilename = "AUDIO-01_cut-11025.wav";
    	String sOutAudioFolder = "C:/Users/FS/Desktop";
    	String sOutAudioFilename = sAudioFilename + "_TEST1.wav";
    			
    	MyWavFile oWavfile = null;
    	MyWavFile oWriteWavFile = null;
    	
    	int iNumSamples = 0;
    	double dSampleRate = 0;
    	int iNumChannels = 0;
    	int iValidBits = 0;
    	
    	int iFrameIncrementSamples = 0;
    	double dFrameIncrementSec = 0;
    	double [] daSampleBuffer = null;
    	double dOverlapFactor = 0;
    	
    	int iWindowSizeSamples = 0;
    	double dWindowSizeSec = 0;
    	double [] daWindowBuffer = null;
    	
    	int iSourceBufferOffset = 0;
    	int iRemainingSamples = 0;
    	
    	//Specs
    	dFrameIncrementSec = 0.010; // The same of vadsohn...
    	dOverlapFactor = 2.0;
    	
    	String sAudioFileFullPath = sAudioFolder + "/" + sAudioFilename;
    	
    	try {
			oWavfile = MyWavFile.openWavFile(new File(sAudioFileFullPath));
		} catch (IOException | WavFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Some audio properties
    	iNumChannels = oWavfile.getNumChannels();
    	iValidBits = oWavfile.getValidBits();
    	iNumSamples = (int)oWavfile.getNumFrames();    	
    	dSampleRate = (double)oWavfile.getSampleRate();
    	
    	dWindowSizeSec = dFrameIncrementSec*dOverlapFactor;
    	iWindowSizeSamples = (int)Math.round(dWindowSizeSec*dSampleRate);
    	dWindowSizeSec = (double)iWindowSizeSamples/dSampleRate;
    	
    	iFrameIncrementSamples = (int)Math.round(dFrameIncrementSec*dSampleRate);
    	dFrameIncrementSec = (double)iFrameIncrementSamples/dSampleRate;
    	
    	
    	daSampleBuffer = new double[iNumSamples];
    	
    	try {
			oWavfile.readFrames(daSampleBuffer, iNumSamples);
			oWavfile.close();
		} catch (IOException | WavFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	String sOutAudioFullPath = sOutAudioFolder + "/" + sOutAudioFilename;
    	// Count the number of samples processed. Overlapping samples are counted more than once.
    	int iNumOfWindows = (int)Math.floorDiv(iNumSamples-iWindowSizeSamples, iFrameIncrementSamples)+1;
    	int iNumOutSamples = iNumOfWindows * iWindowSizeSamples;
    	
		oWriteWavFile = MyWavFile.newWavFile(new File(sOutAudioFullPath), iNumChannels, (long)iNumOutSamples, iValidBits, (long)dSampleRate);

    	iSourceBufferOffset = 0;
    	iRemainingSamples = iNumSamples;
    	daWindowBuffer = new double [iWindowSizeSamples];
    	while (iRemainingSamples>=iWindowSizeSamples) {
    		System.arraycopy(daSampleBuffer, iSourceBufferOffset, daWindowBuffer, 0, iWindowSizeSamples);
    		oWriteWavFile.writeFrames(daWindowBuffer, iWindowSizeSamples);
    		iSourceBufferOffset += iFrameIncrementSamples;
    		iRemainingSamples -= iWindowSizeSamples;
    	}
    	oWriteWavFile.close();
    	
    	System.out.println("test_enframe_1: DONE");
     
    }
    
	public static void main(String[] args) throws IOException, WavFileException {
    	System.out.println("main: START");
    	//test_enframe_1();
    	//test_enframe_2();
		//test_enframe_3();
    	test_enframe_4();
    	System.out.println("main: DONE");
    	
    }

}
