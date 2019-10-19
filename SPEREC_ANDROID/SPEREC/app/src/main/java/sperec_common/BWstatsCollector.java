package sperec_common;

import java.util.ArrayList;

import myMath.MyMatrix;
import myMath.MyMatrix.VectorDim;

public interface BWstatsCollector {

	public BWStatistics getBWStats(VectorDim required_vector_dim) throws Exception;
	
	//public BWStatistics collectBWstats(MixtureModel ubm, String inDir, String fileListFullPath, int ufv, VectorDim required_vector_dim) throws Exception;
	
	public BWStatistics collectBWstats(MixtureModel ubm, ArrayList<MyMatrix> spkSessions, VectorDim required_vector_dim) throws Exception;
}
