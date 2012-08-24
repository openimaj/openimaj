package org.openimaj.lsh.testing;

import org.openimaj.data.DoubleArrayBackedDataSource;
import org.openimaj.data.RandomData;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.lsh.DoubleNearestNeighboursLSH;
import org.openimaj.lsh.functions.DoubleArrayPStableGaussianFactory;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.composition.SimpleComposition;
import org.openimaj.util.hash.modifier.ModuloModifier;

import cern.jet.random.engine.MersenneTwister;

public class LSHTest {
	public static void main(String[] args) {
		final int dims = 50;
		final double[][] queries = RandomData.getRandomDoubleArray(100, dims, 0, 1, 2);
		final double[][] data = new double[10000][];

		final MersenneTwister mt = new MersenneTwister();
		final DoubleNearestNeighboursExact qexact = new DoubleNearestNeighboursExact(queries,
				DoubleFVComparison.EUCLIDEAN);
		final double maxDist = 0.5;
		for (int i = 0; i < queries.length; i++) {
			double dst = 1;
			while (dst > maxDist) {
				data[i] = RandomData.getRandomDoubleArray(dims, 0, 1, mt);

				final int[] argmins = { 0 };
				final double[] mins = { 0 };
				qexact.searchNN(new double[][] { data[i] }, argmins, mins);
				dst = mins[0];
			}
			System.out.println(dst);
		}

		for (int i = queries.length; i < data.length; i++) {
			double dst = 0;
			while (dst < maxDist) {
				data[i] = RandomData.getRandomDoubleArray(dims, 0, 1, mt);

				final int[] argmins = { 0 };
				final double[] mins = { 0 };
				qexact.searchNN(new double[][] { data[i] }, argmins, mins);
				dst = mins[0];
			}
		}

		// for (int i=0; i<data.length; i++) {
		// for (int j=0; j<data[0].length; j++) {
		// data[i][j] = data[i][j] > 0.5 ? 1 : 0;
		// }
		// }
		//
		// for (int i=0; i<queries.length; i++) {
		// for (int j=0; j<queries[0].length; j++) {
		// queries[i][j] = queries[i][j] > 0.5 ? 1 : 0;
		// }
		// }

		final int range = 1017881;
		final int nFunctions = 20;
		final int ntables = 4;
		final int ndims = 128;
		final int w = 8;

		final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>()
		{
			@Override
			public HashFunction<double[]> create() {
				return new ModuloModifier<double[]>(
						new SimpleComposition<double[]>(
								new DoubleArrayPStableGaussianFactory(ndims, mt, w),
								nFunctions
							),
						range
					);
				}
		};

		final DoubleArrayBackedDataSource ds = new DoubleArrayBackedDataSource(data);
		final DoubleNearestNeighboursLSH<DoubleArrayPStableGaussianFactory> lsh = new DoubleNearestNeighboursLSH<DoubleArrayPStableGaussianFactory>(
				factory, ntables, ds);

		final DoubleNearestNeighboursExact exact = new DoubleNearestNeighboursExact(data, DoubleFVComparison.EUCLIDEAN);

		int correct = 0;
		for (final double[] q : queries) {
			final double[][] qus = { q };

			final int[] lshargmins = { 0 };
			final double[] lshmins = { 0 };
			lsh.searchNN(qus, lshargmins, lshmins);

			final int[] exactargmins = { 0 };
			final double[] exactmins = { 0 };
			exact.searchNN(qus, exactargmins, exactmins);

			System.out.println(lshargmins[0] + " " + exactargmins[0]);

			if (lshargmins[0] == exactargmins[0])
				correct++;
		}

		System.out.println((double) correct / (double) queries.length);
	}
}
