package sperec_common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

import myIO.IOClass;
import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;

public class GPLDA_Models {
	public int nphi;
	public MyMatrix Phi;
	public MyMatrix Sigma;
	public MyMatrix W;
	public double [] Mcol;
	
	//% Sigona: optimization
	public MyMatrix Lambda;
	public MyMatrix Uk; //% Matrix
	public MyMatrix Q_hat; //% Matrix
	
	public void writeToFile(File file) throws IOException {
		OutputStream os = (file == null ? System.out : new FileOutputStream(file));
		writeToOutputStream(os);
		os.flush();
		os.close();
	}
	
	public void writeToOutputStream(OutputStream os) throws IOException {
		Phi.write(os);
		Sigma.write(os);
		W.write(os);
		IOClass.writeInt (os, Mcol.length, ByteOrder.LITTLE_ENDIAN);
		IOClass.writeDoubleArray(os, Mcol, ByteOrder.LITTLE_ENDIAN);
		
		//% Sigona: optimization
		Lambda.write(os);
		Uk.write(os); //% Matrix
		Q_hat.write(os); //% Matrix
	}
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static GPLDA_Models readFromInputStream(InputStream is) throws IOException {
		
		GPLDA_Models gplda = new GPLDA_Models();
		
		MyMatrix Phi = MyMatrix.readFromInputStream(is);
		if (Phi.getVectorDim()==VectorDim.COLUMN_VECTORS) {
			Phi = Phi.transpose();
		}
		assert(Phi.getVectorDim()==VectorDim.ROW_VECTORS); // Must be ndim x nphi
		gplda.Phi = Phi;
		gplda.Sigma = MyMatrix.readFromInputStream(is);
		gplda.W = MyMatrix.readFromInputStream(is);
		
		// Read the length of the vector
		int len = IOClass.readInt(is, ByteOrder.LITTLE_ENDIAN);
		
		gplda.Mcol = new double[len];
		/*if (!IOClass.readDoubleArray(is, gplda.Mcol, ByteOrder.LITTLE_ENDIAN))
			throw new IOException("Could not read the GPLDA model");*/
		if (IOClass.readDoubleArray(is, gplda.Mcol, ByteOrder.LITTLE_ENDIAN) < gplda.Mcol.length)
			throw new IOException("Could not read the GPLDA model");
		
		//% Sigona: optimization
		gplda.Lambda = MyMatrix.readFromInputStream(is);
		gplda.Uk = MyMatrix.readFromInputStream(is);
		gplda.Q_hat = MyMatrix.readFromInputStream(is);

		return gplda;
		
	}
}