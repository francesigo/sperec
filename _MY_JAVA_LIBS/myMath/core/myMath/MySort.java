package myMath;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MySort {
	
	public enum SortDir {ASCENDING, DESCENDING }
	
	/**
	 * March 2018
	 * @param <T>
	 */
	public static <T> void sortself(List<T> data, List<Double> keys,  SortDir sortDir) {
		
		final int sgn = (sortDir==SortDir.ASCENDING)? 1: -1;
		
		if (data.size() != keys.size() )
			throw new IllegalArgumentException("Number of data should match the number of keys.");
		
		// Build a linked list of pairs, to be used for sorting 
		LinkedList<Pair<T, Double>> sortedEV = new LinkedList<Pair<T, Double>>();
		for (int i = 0; i < data.size(); ++i)
			sortedEV.add(new Pair<T, Double>(data.get(i), keys.get(i)));
		
		// Actual sort
		Collections.sort(sortedEV, new Comparator<Pair<T, Double>>() {
			public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		
		// Now replace
		data.clear();
		keys.clear();
				
		for (int i=0; i<sortedEV.size(); i++)
		{
			data.add(sortedEV.get(i).a);
			keys.add(sortedEV.get(i).b);
		}		
	}

	/**
	 * March2018
	 * @return a pair of sorted data and sorted keys
	 */
	public static <T> Pair<T[], Double[]> sort2(T[] data, Double [] keys,  SortDir sortDir) {
		
		final int sgn = (sortDir==SortDir.ASCENDING)? 1: -1;
		
		if (data.length != keys.length )
			throw new IllegalArgumentException("Number of rows should match the number of keys.");
		
		// Build a linked list of pairs, to be used for sorting 
		LinkedList<Pair<T, Double>> sortedEV = new LinkedList<Pair<T, Double>>();
		for (int i = 0; i < data.length; ++i)
			sortedEV.add(new Pair<T, Double>(data[i], keys[i]));
		
		// Actual sort
		Collections.sort(sortedEV, new Comparator<Pair<T, Double>>() {
			public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		
		T[] sortedData = (T[])new Object[data.length];
		Double[] sortedKeys = new Double[data.length];
		
		for (int i=0; i<data.length; i++)
		{
			sortedData[i] = sortedEV.get(i).a;
			sortedKeys[i] = sortedEV.get(i).b;
		}
		
		return new Pair(sortedData, sortedKeys);
	}
	
	/**
	 * 
	 * @param data
	 * @param keys
	 * @param sortDir
	 * @return
	 */
	public static <T> T[] sort(T[] data, double [] keys,  SortDir sortDir) {
		
		final int sgn = (sortDir==SortDir.ASCENDING)? 1: -1;
		
		if (data.length != keys.length )
			throw new IllegalArgumentException("Number of rows should match the number of keys.");
		
		LinkedList<Pair<T, Double>> sortedEV = new LinkedList<Pair<T, Double>>();
		for (int i = 0; i < data.length; ++i)
			sortedEV.add(new Pair<T, Double>(data[i], keys[i]));
		
		Collections.sort(sortedEV, new Comparator<Pair<T, Double>>() {
			public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		

		T[] sortedData = (T[])new Object[data.length];
		for (int i=0; i<data.length; i++)
			sortedData[i] = sortedEV.get(i).a;
		
		
		return sortedData;
	}
	
	/**
	 * 
	 * @param data
	 * @param keys
	 * @param sgn
	 * @return
	 */
	private static LinkedList<Pair<double [], Double>> sortRows(double [][] data, double [] keys, final int sgn) {
		
		if (data.length != keys.length )
			throw new IllegalArgumentException("Number of rows should match the number of keys.");
		
		LinkedList<Pair<double [], Double>> sortedEV = new LinkedList<Pair<double [], Double>>();
		for (int i = 0; i < data.length; ++i)
			sortedEV.add(new Pair<double [], Double>(data[i], keys[i]));
				
		Collections.sort(sortedEV, new Comparator<Pair<double [], Double>>() {
			public int compare(Pair<double[], Double> o1, Pair<double[], Double> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		
		return sortedEV;
	}
	
	/**
	 * 
	 * @param data
	 * @param keys
	 * @return
	 */
	public static LinkedList<Pair<double [], Double>> sortRowsDescending(double [][] data, double [] keys) {
		return sortRows(data, keys, -1);
	}
	
	/**
	 * 
	 * @param data
	 * @param keys
	 * @return
	 */
	public static LinkedList<Pair<double [], Double>> sortRowsAscending(double [][] data, double [] keys) {
		return sortRows(data, keys, 1);
	}
	
	
	// -------------------------------------------Keys are short------------------------------------------
		
	public static void rowsAscending(double [][] data, short [] keys, double [][] sortedData, short [] sortedKeys) {
		sortRows(data, keys, sortedData, sortedKeys, 1);
	}
	
	public static void rowsDescending(double [][] data, short [] keys, double [][] sortedData, short [] sortedKeys) {
		sortRows(data, keys, sortedData, sortedKeys, -1);
	}
	
	private static void sortRows(double [][] data, short [] keys,  double [][] sortedData, short [] sortedKeys, final int sgn) {
		
		if (data.length != keys.length )
			throw new IllegalArgumentException("Number of rows should match the number of keys.");
		
		LinkedList<Pair<double [], Short>> sortedEV = new LinkedList<Pair<double [], Short>>();
		for (int i = 0; i < data.length; ++i)
			sortedEV.add(new Pair<double [], Short>(data[i], keys[i]));
				
		Collections.sort(sortedEV, new Comparator<Pair<double [], Short>>() {
			public int compare(Pair<double[], Short> o1, Pair<double[], Short> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		
		Iterator<Pair<double [], Short>> it = sortedEV.iterator();
		for (int i = 0; i < sortedEV.size(); ++i) {
			Pair<double [], Short> p = it.next();
			sortedData[i] = p.a;
			sortedKeys[i] = p.b;
		}
	}
	
	/**
	 * @param keys
	 * @param sortDir
	 * @return
	 */
	public static int[] sort_and_replace(Short [] keys, SortDir sortDir) {
		
		final int sgn = (sortDir==SortDir.ASCENDING)? 1: -1;
				
		LinkedList<Pair<Integer, Short>> sortedEV = new LinkedList<Pair<Integer, Short>>();
		for (int i = 0; i < keys.length; ++i)
			sortedEV.add(new Pair<Integer, Short>(i, keys[i]));
				
		Collections.sort(sortedEV, new Comparator<Pair<Integer, Short>>() {
			public int compare(Pair<Integer, Short> o1, Pair<Integer, Short> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		
		int[] sortedIndexes = new int[keys.length];
		Iterator<Pair<Integer, Short>> it = sortedEV.iterator();
		for (int i = 0; i < sortedEV.size(); ++i) {
			Pair<Integer, Short> p = it.next();
			sortedIndexes[i] = p.a;
			keys[i] = p.b;
		}
		
		return sortedIndexes;
	}
	
	/**
	 * 
	 * @param keys
	 * @param sortDir
	 * @return
	 */
	public static int[] sort_and_replace(double [] keys, SortDir sortDir) {
		
		final int sgn = (sortDir==SortDir.ASCENDING)? 1: -1;
				
		LinkedList<Pair<Integer, Double>> sortedEV = new LinkedList<Pair<Integer, Double>>();
		for (int i = 0; i < keys.length; ++i)
			sortedEV.add(new Pair<Integer, Double>(i, keys[i]));
				
		Collections.sort(sortedEV, new Comparator<Pair<Integer, Double>>() {
			public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
					return sgn * (int)Math.signum(o1.b - o2.b);
			}
		});
		
		int[] sortedIndexes = new int[keys.length];
		Iterator<Pair<Integer, Double>> it = sortedEV.iterator();
		for (int i = 0; i < sortedEV.size(); ++i) {
			Pair<Integer, Double> p = it.next();
			sortedIndexes[i] = p.a;
			keys[i] = p.b;
		}
		
		return sortedIndexes;
	}
	
	
	//-------------------------- TEST FUNCTIONS
	public static void test_sort_and_replace() {
		Short [] spk_labs= { 50, 20, 20, 10, 20, 30, 30, 40, 10, 5, 40, 40, 60, 5, 50, 50, 60};
		int [] index_asc = sort_and_replace(spk_labs, MySort.SortDir.ASCENDING);
		int [] index_des = sort_and_replace(spk_labs, MySort.SortDir.DESCENDING);
	}
}
