package org.openimaj.ml.linear.experiments;

import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer>{

	private double[] arr;

	public ArrayIndexComparator(double[] wordWeights) {
		arr = wordWeights;
	}

	@Override
	public int compare(Integer arg0, Integer arg1) {
		return Double.compare(Math.abs(arr[arg0]), Math.abs(arr[arg1]));
	}

	public static Integer[] integerRange(double[] wordWeights) {
		Integer[] rng = new Integer[wordWeights.length];
		for (int i = 0; i < rng.length; i++) {
			rng[i] = i;
		}
		return rng;
	}

}
