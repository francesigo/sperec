package sperec_common;

import java.io.IOException;
import java.io.OutputStream;

import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;

public interface MixtureModel {

	int getNumberOfComponents();
	
	int getFeatureDimension();
	
	boolean diagonal();
	
    MyMatrix getMuMatrix(VectorDim vdim);
    
    MyMatrix getSigmaDiagMatrix(VectorDim vdim);
    
    MyMatrix getWMatrix(VectorDim vdim);

	void writeToOutputStream(OutputStream os) throws IOException;
}
