package sperec_common;

public class MyRingBuffer {
	private byte [] bb;
	private int iR = 0;
	private int iW = 0;
	private boolean producerRunning;
	private boolean verbose = false; // Ottobre 2018
	
	public MyRingBuffer() {
		this(0);
	}
	
	public MyRingBuffer(int iByteBufferCapacity) {
		iR = 0;
		iW = 0;
		producerRunning = true;
		bb = new byte [iByteBufferCapacity];
	}

	public void setVerbosity(boolean v) {
		verbose = v;
	}
	public int size() {
		return bb.length;
	}

	public void setCapacity(int iNewCapacity) {
		bb = new byte [iNewCapacity];
	}
	
	public synchronized void setProducerRunning(boolean b) {
		if (verbose)
			System.out.println("***** setProducerRunning to :" + b);
		producerRunning = b;
		notify();
	}

	public int getReadable() {
		return (iR>iW) ? bb.length-iR+iW : iW-iR;
	}
	
	public int getWriteable () {
		return (iR>iW) ? iR-iW : bb.length-iW+iR;
	}
	
	public int readAsync(byte [] dst, int offset, int num) {
		int bytesRead = 0;
		if (getReadable()>=num) {
			
			if (verbose)
				System.out.println("MyRingBuffer.readAsync "+ num + " bytes");
			
			if (iR+num < bb.length) {
				System.arraycopy(bb, iR, dst, offset, num);
				iR+=num;
			} else {
				System.arraycopy(bb, iR, dst, offset, bb.length-iR);
				System.arraycopy(bb, 0, dst, offset+bb.length-iR, num-(bb.length-iR));
				iR = num-(bb.length-iR);
			}
			bytesRead = num;
		}
		return bytesRead;
	}
	
	public synchronized int readSync(byte [] dst, int offset, int num) throws InterruptedException {
		int bytesRead = 0;
		while ((getReadable()<num) && producerRunning) {
			//System.out.println("MyRingBuffer.readSync: waiting...");
			wait();
			//System.out.println("MyRingBuffer.wait() return.");
		}
		if (producerRunning) {
			bytesRead = readAsync(dst, offset, num);
			notify();
		} else {
			bytesRead = readAsync(dst, offset, getReadable());
			if (bytesRead==0) {
				bytesRead=-1; // TO stop
			}
		}
		return bytesRead;
	}

	/**
	 *
	 * @param src
	 * @param offset
	 * @param num
	 * @return
	 */
	public int writeAsync(byte [] src, int offset, int num) {
		int bytesWritten = 0;
		if (getWriteable()>=num) {
			
			if (verbose)
				System.out.println("MyRingBuffer.writeAsync "+ num + " bytes");
			
			if (iW+num < bb.length) {
				System.arraycopy(src, offset, bb, iW, num);
				iW+=num;
			} else {
				System.arraycopy(src, offset, bb, iW, bb.length-iW);
				System.arraycopy(src, offset+bb.length-iW, bb, 0, num-(bb.length-iW));
				iW = num-(bb.length-iW);
			}
			bytesWritten = num;
		}
		return bytesWritten;
	}
	/**
	 * 
	 * @param src
	 * @param offset
	 * @param num
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized int writeSync(byte [] src, int offset, int num) throws InterruptedException {
		int bytesWritten = 0;
		if (num>0) {
			while (getWriteable()<num) {
				//System.out.println("MyRingBuffer.writeSync: waiting...");
				wait();
				//System.out.println("MyRingBuffer.writeSync: wait() return.");
			}
			bytesWritten = writeAsync(src, offset, num);
			notify();
		}
		return bytesWritten;
	}
	
	//-------------------
	public static void test1() {
		MyRingBuffer pc = new MyRingBuffer(1000);
		byte [] testBuffer = new byte[2000];

		
		System.out.println("Readable="+pc.getReadable()+", Writeable="+pc.getWriteable());
		pc.readAsync(testBuffer,  0, 120);
		System.out.println("Readable="+pc.getReadable()+", Writeable="+pc.getWriteable());
		pc.writeAsync(testBuffer,  0,  900);
		System.out.println("Readable="+pc.getReadable()+", Writeable="+pc.getWriteable());
		pc.readAsync(testBuffer,  0, 25);
		System.out.println("Readable="+pc.getReadable()+", Writeable="+pc.getWriteable());
		pc.writeAsync(testBuffer,  0,  120);
	}
}