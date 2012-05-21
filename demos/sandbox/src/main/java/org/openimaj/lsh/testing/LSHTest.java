package org.openimaj.lsh.testing;

import org.openimaj.data.DoubleArrayBackedDataSource;
import org.openimaj.data.RandomData;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.lsh.DoubleNearestNeighboursLSH;
import org.openimaj.lsh.functions.DoubleEuclidean;

public class LSHTest {
	public static void main(String[] args) {
		double[][] data = RandomData.getRandomDoubleArray(1000000, 100, -50, 50, 1);
		double[][] queries = RandomData.getRandomDoubleArray(100, 100, -50, 50, 2);

//		for (int i=0; i<data.length; i++) {
//			for (int j=0; j<data[0].length; j++) {
//				data[i][j] = data[i][j] > 0.5 ? 1 : 0;
//			}
//		}
//
//		for (int i=0; i<queries.length; i++) {
//			for (int j=0; j<queries[0].length; j++) {
//				queries[i][j] = queries[i][j] > 0.5 ? 1 : 0;
//			}
//		}

		int nFunctions = 20;
		int ntables = 4;
		DoubleNearestNeighboursLSH<DoubleEuclidean> lsh = new DoubleNearestNeighboursLSH<DoubleEuclidean>(new DoubleEuclidean(), 1, ntables, nFunctions, new DoubleArrayBackedDataSource(data));

		DoubleNearestNeighboursExact exact = new DoubleNearestNeighboursExact(data, DoubleFVComparison.EUCLIDEAN);

		int correct = 0;
		for (double[] q : queries) {
			double [][] qus = {q};

			int[] lshargmins = {0};
			double [] lshmins = {0};
			lsh.searchNN(qus, lshargmins, lshmins);

			int[] exactargmins = {0};
			double [] exactmins = {0};
			exact.searchNN(qus, exactargmins, exactmins);

			System.out.println(lshargmins[0] + " " + exactargmins[0]);
			
			if (lshargmins[0] == exactargmins[0]) correct++;
		}
		
		System.out.println((double)correct / (double)queries.length);
	}
}
