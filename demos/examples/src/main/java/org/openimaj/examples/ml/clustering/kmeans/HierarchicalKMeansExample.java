package org.openimaj.examples.ml.clustering.kmeans;

import java.util.Arrays;

import org.openimaj.data.RandomData;
import org.openimaj.ml.clustering.assignment.hard.HierarchicalByteHardAssigner;
import org.openimaj.ml.clustering.kmeans.HKMeansMethod;
import org.openimaj.ml.clustering.kmeans.HierarchicalByteKMeans;
import org.openimaj.ml.clustering.kmeans.HierarchicalByteKMeans.Node;

/**
 * Example showing how to use the Hierarchical KMeans clustering algorithm with
 * in-memory data. The example sets up the clustering algorithm, generates some
 * uniform random data to cluster, and runs the clustering algorithm. The
 * resultant clusters are printed, and the example also shows how to perform
 * cluster assignment (i.e. vector quantisation) in which vectors are assigned
 * to their closest cluster.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HierarchicalKMeansExample {
	/**
	 * Main method for the example.
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String[] args) {
		// Set up the variables needed to define the clustering operation
		final int dimensionality = 2;
		final int numItems = 10000;
		final int clustersPerNode = 4;
		final int depth = 2;

		// Create the clusterer; there are specific types for all kinds of data
		// (we're using byte data here).
		final HierarchicalByteKMeans kmeans = new HierarchicalByteKMeans(
				HKMeansMethod.FASTKMEANS_EXACT, dimensionality, clustersPerNode, depth);

		// Generate some random data to cluster
		final byte[][] data = RandomData.getRandomByteArray(numItems, dimensionality, Byte.MIN_VALUE, Byte.MAX_VALUE);

		// Perform the clustering
		kmeans.cluster(data);

		// Print the generated hierarchy
		printNode(kmeans.getRoot(), 0);

		// Get an assigner to assign vectors to the closest cluster:
		final HierarchicalByteHardAssigner assigner = kmeans.defaultHardAssigner();

		// Now investigate which cluster each original data item belonged to:
		for (int i = 0; i < 10; i++) {
			final byte[] vector = data[i];

			// Each leaf-node of the hierarchy is assigned a unique index number
			// between 0 and the total number of leaf-nodes.
			final int globalClusterNumber = assigner.assign(vector);

			// We can also get the path taken down the tree terms of the node
			// number at each level of depth. At each level the index number is
			// between 0 and clustersPerNode.
			final int[] path = kmeans.getPath(globalClusterNumber);

			System.out.format("%s was assigned to cluster %d with path %s\n", Arrays.toString(vector),
					globalClusterNumber,
					Arrays.toString(path));
		}
	}

	/**
	 * Recursively print the tree of cluster centroids to {@link System#out}.
	 * 
	 * @param node
	 *            the node to start from
	 * @param indent
	 *            the amount to indent the current line
	 */
	static void printNode(Node node, int indent) {
		final byte[][] centroids = node.kmeans.getCentroids();
		final Node[] children = node.children;

		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				for (int j = 0; j < indent; j++)
					System.out.print("\t");

				System.out.println(Arrays.toString(centroids[i]));
				printNode(children[i], indent + 1);
			}
		} else {
			for (int i = 0; i < centroids.length; i++) {
				for (int j = 0; j < indent; j++)
					System.out.print("\t");

				System.out.println(Arrays.toString(centroids[i]));
			}
		}
	}
}
