package myChart;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import myMath.MyMath;

public class MyChart extends JFrame{


	public JFreeChart chart;
	
	public void plot(double [] y) throws Exception {
		double [] x = MyMath.linspace(0, y.length-1, y.length);
		plot(x, y); 
	}
	// Single serie
	public void plot(double [] x, double [] y) throws Exception {
		//super("");
		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		XYSeries s1 = createSeries("", x, y);
		XYDataset ds1 = createDataset(s1);
		JPanel p = this.createChartPanel(ds1, "", "", "");
		this.add(p);
		XYPlot plot = this.chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		this.setVisible(true);
	}
	
	
	public MyChart(String chartTitle) {
		super(chartTitle);

		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public void add(JPanel chartPanel) {
		add(chartPanel, BorderLayout.CENTER);
	}

	/**
	 * 
	 * @param dataset
	 * @param chartTitle
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @return
	 */
	public JPanel createChartPanel(XYDataset dataset, String chartTitle, String xAxisLabel, String yAxisLabel) {
	 
	    chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset);
	 
	    return new ChartPanel(chart);
	}

	public static XYDataset createDataset() throws Exception {
		
		 return new XYSeriesCollection();
	}
	
	public static XYDataset createDataset(XYSeries series1) throws Exception {
		
		 XYSeriesCollection dataset = new XYSeriesCollection();
		 
		 dataset.addSeries(series1);
		 
		 return dataset;
	}

	
	/**
	 * 
	 * @param sName
	 * @param x
	 * @param y
	 * @return
	 * @throws Exception
	 */
	public static XYSeries createSeries(String sName, double []x, double [] y) throws Exception {
		
		if (x.length!=y.length)
			 throw new Exception("ERROR: mismatch length");
		 
		 XYSeries series1 = new XYSeries(sName);
		 for (int i=0; i<x.length; i++)
			 series1.add(x[i], y[i]);
		 
		 return series1;
	}
	
	/**
	 * 
	 * @param sName
	 * @param x
	 * @return
	 * @throws Exception
	 */
	public static XYSeries createSeries(String sName, double []x) throws Exception {

		 XYSeries series1 = new XYSeries(sName);
		 for (int i=0; i<x.length; i++)
			 series1.add(i, x[i]);
		 
		 return series1;
	}
	
	public static void main(String[] args) {
		/*
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MyChart().setVisible(true);
			}
		});
		*/
	}
}

