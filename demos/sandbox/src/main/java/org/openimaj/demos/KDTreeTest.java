package org.openimaj.demos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.util.iterator.TextLineIterable;
import org.openimaj.util.tree.AltDoubleKDTree2;

public class KDTreeTest {
	static List<double[]> readData() {
		final List<double[]> ll = new ArrayList<double[]>();

		for (final String s : new TextLineIterable(new File("/Users/jon/training_latlng"))) {
			final String[] p = s.split(" ");
			try {
				ll.add(new double[] { Double.parseDouble(p[2]), Double.parseDouble(p[1]) });
			} catch (final NumberFormatException nfe) {
			}
		}

		return ll;
	}

	static Comparator<double[]> comparator = new Comparator<double[]>() {
		@Override
		public int compare(double[] o1, double[] o2) {
			return Double.compare(o1[0], o2[0]);
		}
	};

	static List<double[]> bruteForceRangeSearch(List<double[]> data, double[] min, double[] max) {
		final List<double[]> ret = new ArrayList<double[]>();

		int start = Collections.binarySearch(data, min, comparator);
		int stop = Collections.binarySearch(data, max, comparator);

		if (start < 0)
			start = -(start + 1);
		if (stop < 0)
			stop = -(stop + 1);

		for (int i = start; i <= stop; i++) {
			final double[] d = data.get(i);

			if (d[1] >= min[1] && d[1] <= max[1]) {
				ret.add(d);
			}
		}

		return ret;
	}

	public static void main(String[] args) {
		final List<double[]> data = readData();
		final double r = 0.01;

		// final DoubleKDTree kdtree = new DoubleKDTree(data);
		final FastKDTree kdtree = new FastKDTree(data.toArray(new double[data.size()][]),
				new FastKDTree.RandomisedBestBinFirstMeanSplit());

		final AltDoubleKDTree2 altkdtree = new AltDoubleKDTree2(data);

		Collections.sort(data, comparator);
		System.out.println("starting");

		long bft = 0, kdt = 0, akdt = 0;
		for (int i = 0; i < 100000; i++) {
			final double[] pt = data.get(i);

			final double[] min = { pt[0] - r, pt[1] - r };
			final double[] max = { pt[0] + r, pt[1] + r };

			final long t1 = System.nanoTime();
			System.out.println(bruteForceRangeSearch(data, min, max).size());
			final long t2 = System.nanoTime();
			// kdtree.rangeSearch(min, max).size();
			System.out.println(kdtree.coordinateRangeSearch(min, max).size());
			final long t3 = System.nanoTime();
			System.out.println(altkdtree.rangeSearch(min, max).size());
			final long t4 = System.nanoTime();
			bft += t2 - t1;
			kdt += t3 - t2;
			akdt += t4 - t3;
		}
		System.out.println((bft / 1e9) + " " + (kdt / 1e9) + " " + (akdt / 1e9));
	}
}
