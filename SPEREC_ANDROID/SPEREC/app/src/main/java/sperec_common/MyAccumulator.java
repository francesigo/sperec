package sperec_common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MyAccumulator {
	
	//public enum SetOrder { SORT, STABLE }
	
	/*private static class Counter {
		
		private double n = 0;
				
	
		 //Increment counts.

		void increment() { n++; }
		

		 // Get the number of observations contributing to this accumulator
		double getCount() { return n; }				
	}
*/
	
	/**
	 * Count the unique elements in the array keys, and return the count in the same order of the occurrence in keys
	 * @param keys
	 * @return
	 */
	public double [] count_unique_stable(Short [] keys) {
		ArrayList<Short> stats = new ArrayList<Short>();
		ArrayList<Double> counters = new ArrayList<Double>();
		int ind;
		double v;
		for (int i=0; i<keys.length; i++) {
			short c = keys[i];
			if (!stats.contains(c)) {
				stats.add(c);
				counters.add((double)0);
			}
			ind = stats.indexOf(c);
			v = counters.get(ind);
			counters.set( ind, ++v);
		}
		int unique_size = stats.size();
		double [] unique_keys = new double [unique_size];
		double [] unique_keys_counts = new double [unique_size];
		for (int i=0; i<unique_size; i++) {
			unique_keys[i] = stats.get(i);
			unique_keys_counts[i] = counters.get(i);
		}
		return unique_keys_counts;
		// By construction, unique_* are "stable"
	}
	
	
	public ArrayList<Double> get_unique_arraylist(double[] spk_counts) { // As in matlab, the unique elements are returned in sorted order
		ArrayList<Double> stats = new ArrayList<Double>();
		for (int i=0; i<spk_counts.length; i++) {
			double c = spk_counts[i];
			if (!stats.contains(c))
				stats.add(c);
		}
		int unique_size = stats.size();
		double [] unique_keys = new double [unique_size];
		for (int i=0; i<unique_size; i++)
			unique_keys[i] = stats.get(i);
		
		Collections.sort(stats, new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				return (int)Math.signum(o1 - o2);
			}
		});
		
		return stats;
	}
	
	public double [] get_unique_array(double[] spk_counts) { // As in matlab, the unique elements are returned in sorted order
		ArrayList<Double> stats = new ArrayList<Double>();
		for (int i=0; i<spk_counts.length; i++) {
			double c = spk_counts[i];
			if (!stats.contains(c))
				stats.add(c);
		}
		int unique_size = stats.size();
		double [] unique_keys = new double [unique_size];
		for (int i=0; i<unique_size; i++)
			unique_keys[i] = stats.get(i);
		
		Arrays.sort(unique_keys);
		return unique_keys;
	}
	
	
	/*
	public double [] count_unique_(short [] keys) {
		
		HashMap<Short, Counter> stats = new HashMap<Short, Counter>();
		for (int i=0; i<keys.length; i++) {
			short c = keys[i];
			if (!stats.containsKey(c))
				stats.put(c, new Counter());
			
			// build up class dependent stats
			stats.get(c).increment();
		}
		double [] count = new double [stats.size()];
		int ii = 0;
		for (Entry<Short, Counter> e : stats.entrySet())
			count[ii++] = e.getValue().getCount();
		
		return count;
	}
	*/
	
	//-------------- TEST FUNCTIONS
	public static double [] test_count_unique_stable() {
		Short [] spk_labs= { 50, 20, 20, 10, 20, 30, 30, 40, 10, 5, 40, 40, 60, 5, 50, 50, 60};
		double [] spk_count = new MyAccumulator().count_unique_stable(spk_labs);
		return spk_count;
	}
	
}
