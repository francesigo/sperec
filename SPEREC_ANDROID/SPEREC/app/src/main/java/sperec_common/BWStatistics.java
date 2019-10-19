package sperec_common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import myMath.MyMatrix;

/*
 * Just put together N and F. No need to put methods here, other than I/O
 * WARNING: TODO : S support is incomplete
 */
public class BWStatistics {
	
	public MyMatrix N = null; // During GMM training: 1 x nmix			After BWcollector: nfiles x nmix
	public MyMatrix F = null; // During GMM training: feaDim x nmix		After BWcollector: nfiles x (feadim x nmix), i.e. a collection of supervectors of F statistics
	public MyMatrix S = null; // During GMM training: feaDim x nmix		After BWcollector: TO BE DEFINED
	
	
	public BWStatistics copy() {
		
		BWStatistics bwout = new BWStatistics();
		bwout.N = N.copy();
		bwout.F = F.copy();
		bwout.S = S.copy();
		return bwout;
	}
	
	/**
	 * Accumulate (sum) BW stats
	 * @param bw
	 */
	void plusEquals(BWStatistics bw) {
		
		N.plusEquals(bw.N);
		F.plusEquals(bw.F);
		S.plusEquals(bw.S);
	}
	
	/**
	 * Just write down the two matrices, using their own methods.
	 * @param os: the OutputStream object where to save
	 * @throws IOException
	 */
	public void write(OutputStream os) throws IOException {
		N.write(os);
		F.write(os);
		// Put code for S here
	}
	
	/**
	 * 
	 * @param file: the File object where to save
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		OutputStream os = (file == null ? System.out : new FileOutputStream(file));
		write(os);
		os.flush();
		os.close();
	}
	
	/**
	 * 
	 * @param filePath: the path of the file were to save.
	 * @throws IOException
	 */
	public void writeToFile(String filePath) throws IOException {
		writeToFile(new File(filePath));
	}
	
	/**
	 * Just read the two matrices, using their own methods and return the a BWStatistics instance
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static BWStatistics readFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		BWStatistics bw = new BWStatistics();
		bw.N = MyMatrix.readFromInputStream(is);
		bw.F = MyMatrix.readFromInputStream(is);
		// Put code for S here
		
		is.close();
		return bw;
	}
	
	/**
	 * 
	 * @param filepath
	 * @throws IOException 
	 */
	public static BWStatistics readFromFile(String filepath) throws IOException {
		return readFromFile(new File(filepath));
	}
}