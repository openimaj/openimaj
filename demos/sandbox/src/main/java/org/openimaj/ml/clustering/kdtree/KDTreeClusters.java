/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.clustering.kdtree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.pair.IntDoublePair;
import org.openimaj.util.tree.DoubleKDTree;
import org.openimaj.util.tree.DoubleKDTree.KDTreeNode;

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
	private DoubleKDTree tree;

	/**
	 * @param tree the KDTree which represents the clusters
	 * @param dims 
	 */
	public KDTreeClusters(DoubleKDTree tree, int dims) {
		this.tree = tree;
		this.leaves = tree.leafIndices();
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
