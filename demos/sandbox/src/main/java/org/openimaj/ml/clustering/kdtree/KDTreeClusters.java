package org.openimaj.ml.clustering.kdtree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.demos.FastKDTree;
import org.openimaj.demos.FastKDTree.KDTreeNode;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.pair.IntDoublePair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class KDTreeClusters extends IndexClusters implements SpatialClusters<double[]> {


	private final class KDHardTreeAssigner implements HardAssigner<double[], double[], IntDoublePair> {
		
		private Map<int[], Integer> clusterToIndex;

		public KDHardTreeAssigner() {
			this.clusterToIndex = new HashMap<int[],Integer>();
			for (int i = 0; i < KDTreeClusters.this.leaves.size(); i++) {
				this.clusterToIndex.put(leaves.get(i), i);
			}
		}
		
		@Override
		public int numDimensions() {
			return dims;
		}

		@Override
		public int[] assign(double[][] data) {
			int[] ret = new int[data.length];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = assign(data[i]);
			}
			return ret;
		}

		@Override
		public int assign(double[] data) {
			KDTreeNode toFollow = tree.root;
			while(toFollow.indices == null){
				if(data[toFollow.discriminantDimension] < toFollow.discriminant){
					toFollow = toFollow.left;
				}
				else{
					toFollow = toFollow.right;
				}
			}
			return this.clusterToIndex.get(toFollow.indices);
		}

		@Override
		public void assignDistance(double[][] data, int[] indices, double[] distances) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IntDoublePair assignDistance(double[] data) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return numClusters();
		}
	}

	private List<int[]> leaves;
	private int dims;
	private FastKDTree tree;

	/**
	 * @param tree the KDTree which represents the clusters
	 * @param dims 
	 */
	public KDTreeClusters(FastKDTree tree, int dims) {
		this.tree = tree;
		this.leaves = tree.findLeafIndices();
		this.dims = dims;
		this.clusters = new int[leaves.size()][];
		for (int i = 0; i < clusters.length; i++) {
			clusters[i] = leaves.get(i);
			this.nEntries += this.clusters[i].length;
		}
		
	}

	@Override
	public int numDimensions() {
		return dims;
	}

	@Override
	public int numClusters() {
		return leaves.size();
	}

	@Override
	public HardAssigner<double[], ?, ?> defaultHardAssigner() {
		return new KDHardTreeAssigner();
	}
	@Override
	public void readASCII(Scanner in) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public String asciiHeader() { throw new UnsupportedOperationException(); }
	
	@Override
	public void readBinary(DataInput in) throws IOException{ throw new UnsupportedOperationException(); }
	
	@Override
	public byte[] binaryHeader() { throw new UnsupportedOperationException(); }
	
	@Override
	public void writeASCII(PrintWriter out) throws IOException { throw new UnsupportedOperationException(); }
	
	@Override
	public void writeBinary(DataOutput out) throws IOException { throw new UnsupportedOperationException(); }

}
