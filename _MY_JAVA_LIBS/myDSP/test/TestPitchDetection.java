package myDSP.test;



import java.io.IOException;

import javax.swing.JPanel;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import myChart.MyChart;
import myDSP.MyFraming;
import myDSP.MyPitch;
import myDSP.MyPitchSettings;
import myDSP.MyVAD;
import myDSP.MyVadSettings;
import myDSP.Utterance;
import myIO.SampleFileLoader;
import myMath.MyMath;
import myMath.MyMatrix;


public class TestPitchDetection {

	public static void main (String [] args) throws Exception {
		
		String utteranceFilename = "dis-f1-b1.wav";

		Utterance u = SampleFileLoader.load(utteranceFilename);
		
		MyPitchSettings pitchSettings = new MyPitchSettings();
		pitchSettings.algorithmName = "YIN";
		MyVadSettings vadSettings = new MyVadSettings();
		vadSettings.vadPercentile = 25; // 25;

		// Framing
		MyFraming myFraming = new MyFraming(vadSettings);
		myFraming.process(u);
		
		// Normalizzo
		float [][] frames = myFraming.getFrames();
		if (!normalizeEnergySelf("mean-voiced", frames, vadSettings))
			return;	
		
		// VAD
		MyVAD myVad = new MyVAD();
		myVad.process(frames, vadSettings);
		
		// Solo frame voiced
		MyFraming voicedFraming = myFraming.select(myVad.voiced_indexes);
		
		//PitchDetector PD = PitchEstimationAlgorithm.YIN.getDetector((float)u.getSampleRate(), frames[0].length); // Ad esempio
		//PitchDetector PD = PitchEstimationAlgorithm.FFT_YIN.getDetector((float)u.sampleRate, frames[0].length); // Ad esempio
		
		MyPitch myPitch = new MyPitch(pitchSettings);
		myPitch.exe(null, (float)u.sampleRate, voicedFraming.getFrames());

		stdout(myPitch, voicedFraming.midTimes);
		
		// Filter
		int [] keepPitchedIds = MyMath.find(myPitch.isPitched);
		myPitch.selectSelf(keepPitchedIds);
		// Outlier detection
		//int [] noOutlierIds = myPitch.stdevThresholdSelf(2.0);
		//int [] keepFramesIds = MyMath.select(keepPitchedIds, noOutlierIds); // Indici dei frames che devo fare sopravvivere
		MyFraming myFramingCleanPitch = myFraming.select(keepPitchedIds);//MyFraming myFramingCleanPitch = myFraming.select(keepFramesIds);
		
		stdout(myPitch, myFramingCleanPitch.midTimes);
		
		
		
		//MyChart CH1 = new MyChart(myFramingCleanPitch.midTimes, myPitch.pitchValues);
		MyChart CH1 = new MyChart("TestPitchDetection: ");
		CH1.plot(myFramingCleanPitch.midTimes, myPitch.pitchValues);
		/*
		XYDataset ds1 = MyChart.createDataset();
		//XYSeries s1 = MyChart.createSeries("pitch", myFramingCleanPitch.midTimes, myPitch.pitchValues);
		//((XYSeriesCollection)ds1).addSeries(s1);
		MyChart CH = new MyChart("HELLO");
		JPanel p = CH.createChartPanel(ds1, "pippo", "time [s]", "Pitch [Hz]");
		CH.add(p);
		XYPlot plot = CH.chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		CH.setVisible(true);
		XYSeries s2 = MyChart.createSeries("midTimes", myFramingCleanPitch.midTimes);
		((XYSeriesCollection)ds1).addSeries(s2);
*/

		System.out.println("DONE");
		
	}
	

	
	/**
	 * 
	 * @param type
	 * @param frames
	 * @return
	 * @throws Exception 
	 */
	public static boolean normalizeEnergySelf(String type, float[][] frames, MyVadSettings FS) throws Exception {
		
		boolean ok = true;

		switch (type) {
		
		case "mean-voiced":
			
			MyVAD myVad = new MyVAD();
			myVad.process(frames, FS);
			double meanVoicedEnergy = MyMath.mean(myVad.voiced_energy);
			double scalingFactor = 1.0/Math.sqrt(meanVoicedEnergy);
			// Rescale the samples of all frames
			MyMath.timesSelf(frames, scalingFactor);			
			break;
			
		default:
			ok = false;
			break;
		}
		return ok;
	}
	
	static void stdout(MyPitch myPitch, double [] times) throws Exception {
		
		if (times.length != myPitch.pitchValues.length)
			throw new Exception("ERROR: length mismatch");
		
		for (int i = 0; i<times.length; i++ )
			System.out.println(" Frame #" + i + " (@ " + times[i] + " , is pitched: " + myPitch.isPitched[i] + " , pitch value = " + (int)myPitch.pitchValues[i] +  " Hz, prob.  "+ myPitch.pitchProbabilities[i]);  
		
	}
}
