package test;

import java.util.ArrayList;

import sperec_common.BWStatistics;
import sperec_common.BWstatsCollector_MSR;
import sperec_common.GPLDA_Models;
import sperec_common.MyMatrix;
import sperec_common.MyMatrix.VectorDim;
import sperec_common.POPREF_MODEL;
import sperec_common.SPEREC_UBM_IV_GPLDA;
import sperec_common.SpeakerModel;

/**
 * This class is useful to test some SPEREC_UBM_IV_GPLDA method with the equivaent in MATLAB, in order to check if java version computes well.
 * @author FS
 *
 */
public class MATLAB_SPEREC_UBM_IV_GPLDA extends SPEREC_UBM_IV_GPLDA {
	
	public POPREF_MODEL getPopRefModel() { // For debug purpose
		return this.POP;
	}
	
	
	@Override
	protected SpeakerModel computeSpeakerModel(ArrayList<MyMatrix> spkSessions) throws Exception {
		
		SpeakerModel spkModel = super.computeSpeakerModel(spkSessions);
		{
			//[n, f] = compute_bw_stats(fea, ubm);
			BWStatistics bwJava = new BWstatsCollector_MSR("NF").collectBWstats(POP.ubm, spkSessions, VectorDim.ROW_VECTORS);
				
			BWStatistics bwMatlab = MATLAB.compute_bw_stats_testJava(spkSessions, POP.ubm);
			double[][] Nmatlab = bwMatlab.N.getArray();
			double[][] Fmatlab = bwMatlab.F.getArray();
			
			double en = TEST.compare_matrices("\tNstats: ", bwJava.N, Nmatlab);
			double ef = TEST.compare_matrices("\tFstats: ", bwJava.F, Fmatlab);
			
			System.out.println("\t\t Nstats: normInf = " + en + "  , FStats: norminf = " + ef);
			
		}
		
		return spkModel;
		
	}

	/**
	 * This method invokes the method: SPEREC_UBM_IV_GPLDA.score_gplda_trials_STAGE_2
	 * The latter is the same of the matlab function       score_gplda_trials_STAGE_2_testJava
	 */
	protected MyMatrix score_gplda(MyMatrix model_iv, MyMatrix test_iv) throws Exception {
		
		MyMatrix resJava = super.score_gplda(model_iv, test_iv);
		
		{ // Debug section
			double scoreJava = resJava.get(0, 0);
		
			GPLDA_Models g = this.POP.gplda.getModels();

			double scoreMatlab = MATLAB.score_gplda_trials_STAGE_2_testJava(g.Mcol, g.W.transpose(), g.Uk.transpose(), g.Q_hat, g.Lambda, model_iv, test_iv);		
			double diff = scoreJava - scoreMatlab;
			double err = Math.abs(diff)/scoreMatlab;
			System.out.println("\t\tScore Java: " + scoreJava + " ; Score Matlab: " + scoreMatlab + " ; diff = " + diff + " ;  err(%) = " + 100.0*err + " %");
		}
		return resJava;
		
	}
	
}
