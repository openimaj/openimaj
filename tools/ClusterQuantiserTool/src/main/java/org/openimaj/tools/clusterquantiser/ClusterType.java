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
package org.openimaj.tools.clusterquantiser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.Cluster;
import org.openimaj.ml.clustering.kmeans.HByteKMeans;
import org.openimaj.ml.clustering.kmeans.HIntKMeans;
import org.openimaj.ml.clustering.kmeans.HKMeansMethod;
//import org.openimaj.ml.clustering.kmeans.IKMeansElkan;
//import org.openimaj.ml.clustering.kmeans.IKMeansLloyd;
//import org.openimaj.ml.clustering.kmeans.IKMeansLloydVariance;
import org.openimaj.ml.clustering.rforest.IntRandomForest;
import org.openimaj.tools.clusterquantiser.fastkmeans.FastByteKMeansInitialisers;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatch;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatchByteDataSource;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatchIntDataSource;
import org.openimaj.util.array.ByteArrayConverter;

import org.openimaj.ml.clustering.kmeans.fast.FastByteKMeansCluster;
import org.openimaj.ml.clustering.kmeans.fast.FastIntKMeansCluster;
import org.openimaj.ml.clustering.random.RandomByteCluster;
import org.openimaj.ml.clustering.random.RandomIntCluster;
import org.openimaj.ml.clustering.random.RandomSetByteCluster;
import org.openimaj.ml.clustering.random.RandomSetIntCluster;

/**
 * Different clustering algorithms
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 */
public enum ClusterType implements CmdLineOptionsProvider {
	/**
	 * Randomly sampled centroids (with replacement; the same centroid might be picked multiple times)
	 */
	RANDOM {
		@Override
		public ClusterTypeOp getOptions() {
			return new RandomOp();
		}
	},
	/**
	 * Randomly sampled centroids (without replacement; a centroid can only be picked once)
	 */
	RANDOMSET{
		@Override
		public ClusterTypeOp getOptions() {
			return new RandomSetOp();
		}
	},
	/**
	 * Fast (possibly approximate) batched K-Means
	 */
	FASTMBKMEANS {
		@Override
		public ClusterTypeOp getOptions() {
			return new FastMBKMeansOp();
		}
	},
	/**
	 * Fast (possibly approximate) K-Means
	 */
	FASTKMEANS {
		@Override
		public ClusterTypeOp getOptions() {
			return new FastKMeansOp();
		}
	},
	/**
	 * Hierarchical K-Means
	 */
	HKMEANS {
		@Override
		public ClusterTypeOp getOptions() {
			return new HKMeansOp();
		}
	},
	/**
	 * Random forest
	 */
	RFOREST {
		@Override
		public ClusterTypeOp getOptions() {
			return new RForestOp();
		}
	}
	;
	
	/**
	 * Options for each {@link ClusterType}.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static abstract class ClusterTypeOp {
		/**
		 * The precision of the clusters
		 */
		@Option(name="--precision", aliases="-p", required=false, usage="Specify the cluster percision if supported")
		public Precision precision = Precision.BYTE;
	
		/**
		 * Create clusters from data
		 * @param data
		 * @return clusters
		 */
		public abstract Cluster<?,?> create(byte[][] data) ;
	
		/**
		 * Create clusters from data
		 * @param batches
		 * @return clusters
		 */
		public Cluster<?,?> create(List<SampleBatch> batches) {
			return null;
		}
	
		/**
		 * @return options
		 */
		public Map<String,String> getOptionsMap() {
			return new HashMap<String,String>();
		}
	
		/**
		 * Set options
		 * @param options
		 */
		public void setOptionsMap(Map<String,String> options) {
		
		}

		/**
		 * @return java class representing clusters
		 */
		public abstract Class<? extends Cluster<?, ?>> getClusterClass();
	}	
	
	private static class RForestOp extends ClusterTypeOp {
		@Option(name="--decisions", aliases="-d", required=true, usage="Specify number of random decisions to be made per tree.", metaVar="NUMBER")
		private int decisions = 32;
		
		@Option(name="--number-of-trees", aliases="-nt", required=true, usage="Specify number of random trees", metaVar="NUMBER")
		private int ntrees = 32;
		
		@Override
		public Cluster<IntRandomForest,int[]> create(byte[][] data) {
			IntRandomForest rf = new IntRandomForest(ntrees,decisions);
			rf.train(ByteArrayConverter.byteToInt(data));
			return rf;
		}
		
		@Override
		public Class<? extends Cluster<?,?>> getClusterClass() {
			return IntRandomForest.class;
		}
	}
	
	private static class HKMeansOp extends ClusterTypeOp {
		@Option(name="--depth", aliases="-d", required=true, usage="Specify depth of tree in create mode.", metaVar="NUMBER")
		private int depth = 6;
		
		@Option(name="--clusters", aliases="-k", required=true, usage="Specify number of clusters per level.", metaVar="NUMBER")
		private int K = 10;
		
		@Option(name="--kmeans-type", aliases="-kt", required=false, usage="The type of kmeans algorithm to use.", metaVar="NUMBER")
		private HKMeansMethod kmeansType = HKMeansMethod.FASTKMEANS_EXACT;
		
		@Override
		public Cluster<?,?> create(byte[][] data) {
			if(this.precision == Precision.BYTE)
			{
				HByteKMeans tree = new HByteKMeans(kmeansType );
				tree.init(data[0].length, K, depth);

				System.err.printf("Building vocabulary tree\n");
				tree.train(data);
				return tree;
			}
			else
			{
				HIntKMeans tree = new HIntKMeans(kmeansType );
				tree.init(data[0].length, K, depth);

				System.err.printf("Building vocabulary tree\n");
				tree.train(ByteArrayConverter.byteToInt(data));
				return tree;
			}
			
		}
	
		@Override
		public Class<? extends Cluster<?,?>> getClusterClass() {
			if(this.precision == Precision.BYTE)
				return HByteKMeans.class;
			else
				return HIntKMeans.class;
			
		}
	}

	private static class FastKMeansOp extends ClusterTypeOp {
		@Option(name="--clusters", aliases="-k", required=true, usage="Specify number of clusters per level.", metaVar="NUMBER")
		private int K = 10;
		
		@Option(name="--iterations", aliases="-itr", required=false, usage="Specify number of iterations.", metaVar="NUMBER")
		private int I = 30;
		
		@Option(name="--batch-size", aliases="-b", required=false, usage="Specify size of each batch for each iteration.", metaVar="NUMBER")
		private int B = 50000;
		
		@Option(name="--num-checks", aliases="-nc", required=false, usage="Specify number of checks for each kd-tree.", metaVar="NUMBER")
		private int NC = 768;
		
		@Option(name="--num-trees", aliases="-nt", required=false, usage="Specify number of kd-trees.", metaVar="NUMBER")
		private int NT = 8;
		
		@Option(name="--exact-nn", aliases="-ex", required=false, usage="Specify whether to use exact nearest neighbours.", metaVar="BOOLEAN")
		private boolean E = false;
		
		@Option(name="--fastkmeans-threads", aliases="-jj", required=false, usage="Specify the number of threads to use to train centroids.", metaVar="NUMBER")
		private int jj = Runtime.getRuntime().availableProcessors();
		
		@Option(name="--cluster-random-seed", aliases="-crs", required=false, usage="Specify a seed for the random data selection.", metaVar="NUMBER")
		private long seed = -1;
		
		@Option(name = "--cluster-init", aliases = "-cin", required = false, usage = "Specify the type of file to be read.", handler = ProxyOptionHandler.class)
		public FastByteKMeansInitialisers clusterInit = FastByteKMeansInitialisers.RANDOM;
		
		
		@Override
		public Cluster<?,?> create(List<SampleBatch> batches){
			System.err.println("Constructing a FASTKMEANS cluster");
			Cluster<?,?> c = null;
			System.err.println("Constructing a fastkmeans worker: ");
			if(this.precision == Precision.BYTE)
			{
				SampleBatchByteDataSource ds;
				try {
					ds = new SampleBatchByteDataSource(batches);
					ds.setSeed(seed);
					c = new FastByteKMeansCluster(ds.numDimensions(), K, E, NT, NC, B, I,jj);
					((FastByteKMeansCluster)c).seed(seed);
					clusterInit.setClusterInit((FastByteKMeansCluster) c);
					((FastByteKMeansCluster)c).train(ds);
				} catch (Exception e) {
				}
				
			}
			else
			{
				SampleBatchIntDataSource ds;
				try {
					ds = new SampleBatchIntDataSource(batches);
					ds.setSeed(seed);
					c = new FastIntKMeansCluster(ds.numDimensions(), K, E, NT, NC, B, I,jj);
					((FastIntKMeansCluster)c).seed(seed);
					((FastIntKMeansCluster)c).train(ds);
				} catch (IOException e) {
				}
				
			}
			return c;
		}
		
		@Override
		public Cluster<?,?> create(byte[][] data) {
			Cluster<?,?> c = null;
			if(this.precision == Precision.BYTE)
			{
				c = new FastByteKMeansCluster(data[0].length, K, E, NT, NC, B, I,jj);
				((FastByteKMeansCluster)c).seed(seed);
				try {
					clusterInit.setClusterInit((FastByteKMeansCluster) c);
				} catch (Exception e) {
				}
				((FastByteKMeansCluster)c).train(data);
			}
			else
			{
				c = new FastIntKMeansCluster(data[0].length, K, E, NT, NC, B, I,jj);
				((FastIntKMeansCluster)c).seed(seed);
				((FastIntKMeansCluster)c).train(ByteArrayConverter.byteToInt(data));
			}

			return c;
		}
		
		@Override
		public Class<? extends Cluster<?,?>> getClusterClass() {
			if(this.precision == Precision.BYTE)
				return FastByteKMeansCluster.class;
			else
				return FastIntKMeansCluster.class;			
		}
	}
	
	private static class FastMBKMeansOp extends ClusterTypeOp {
		@Option(name="--clusters", aliases="-k", required=true, usage="Specify number of clusters per level.", metaVar="NUMBER")
		private int K = 10;
		
		@Option(name="--iterations", aliases="-itr", required=false, usage="Specify number of iterations.", metaVar="NUMBER")
		private int I = 30;
		
		@SuppressWarnings("unused")
		@Option(name="--mini-batch-size", aliases="-mb", required=false, usage="Specify size of each mini-batch for each iteration.", metaVar="NUMBER")
		private int M = 10000;
		
		@Option(name="--batch-size", aliases="-b", required=false, usage="Specify size of each batch for each iteration.", metaVar="NUMBER")
		private int B = 50000;
		
		@Option(name="--num-checks", aliases="-nc", required=false, usage="Specify number of checks for each kd-tree.", metaVar="NUMBER")
		private int NC = 768;
		
		@Option(name="--num-trees", aliases="-nt", required=false, usage="Specify number of kd-trees.", metaVar="NUMBER")
		private int NT = 8;
		
		@Option(name="--exact-nn", aliases="-ex", required=false, usage="Specify whether to use exact nearest neighbours.", metaVar="NUMBER")
		private boolean E = false;
		
		@Option(name="--fastkmeans-threads", aliases="-jj", required=false, usage="Specify the number of threads to use to train centroids.", metaVar="NUMBER")
		private int jj = Runtime.getRuntime().availableProcessors();
		
		@Override
		public Cluster<FastIntKMeansCluster,int[]> create(byte[][] data) {
			FastIntKMeansCluster c = null;
			c = new FastIntKMeansCluster(data[0].length, K , E, NT, NC, B, I,jj);
			c.train(ByteArrayConverter.byteToInt(data));
			return c;
		}

		@Override
		public Class<? extends Cluster<?,?>> getClusterClass() {
			return FastIntKMeansCluster.class;
		}
	}
	
	private static class RandomSetOp extends ClusterTypeOp {
		@Option(name="--clusters", aliases="-k", required=false, usage="Specify number of clusters per level.", metaVar="NUMBER")
		private int K = -1;
		
		@Option(name="--cluster-random-seed", aliases="-crs", required=false, usage="Specify a seed for the random data selection.", metaVar="NUMBER")
		private int seed = -1;
		
		@Override
		public Cluster<?,?> create(byte[][] data) {
			if(this.precision == Precision.BYTE){
				RandomSetByteCluster c = null;
				c = new RandomSetByteCluster(data[0].length,K);
				if(seed >= 0) c.setSeed(seed);
				System.err.printf("Building BYTE vocabulary tree\n");
				c.train(data);
				return c;
			}
			else{
				RandomSetIntCluster c = null;
				c = new RandomSetIntCluster(data[0].length,K);
				if(seed >= 0) c.setSeed(seed);
				System.err.printf("Building INT vocabulary tree\n");
				c.train(ByteArrayConverter.byteToInt(data));
				return c;
			}
		}
		
		@Override
		public Cluster<?,?> create(List<SampleBatch> batches){
		
			if(this.precision == Precision.BYTE)
			{
				SampleBatchByteDataSource ds = null;
				try {
					ds = new SampleBatchByteDataSource(batches);
					ds.setSeed(seed);
				} catch (IOException e) {
				}
				RandomSetByteCluster c = null;
				c = new RandomSetByteCluster(ds.numDimensions(),K);
				c.train(ds);
				return c;
			}
			else
			{
				SampleBatchIntDataSource ds = null;
				try {
					ds = new SampleBatchIntDataSource(batches);
					ds.setSeed(seed);
				} catch (IOException e) {
				}
				RandomSetIntCluster c = null;
				c = new RandomSetIntCluster(ds.numDimensions(),K);
				c.train(ds);
				return c;
			}
		}
		
		
		@Override
		public Class<? extends Cluster<?,?>> getClusterClass() {
			if(this.precision == Precision.BYTE)
				return RandomByteCluster.class;
			else
				return RandomIntCluster.class;
		}
	}
	
	private static class RandomOp extends ClusterTypeOp {
		@Option(name="--clusters", aliases="-k", required=false, usage="Specify number of clusters per level.", metaVar="NUMBER")
		private int K = -1;
		
		@Option(name="--cluster-random-seed", aliases="-crs", required=false, usage="Specify a seed for the random data selection.", metaVar="NUMBER")
		private int seed = -1;
		
		@Override
		public Cluster<?,?> create(byte[][] data) {
			if(this.precision == Precision.BYTE){
				RandomByteCluster c = null;
				c = new RandomByteCluster(data[0].length,K);
				if(seed >= 0) c.setSeed(seed);
				System.err.printf("Building BYTE vocabulary tree\n");
				c.train(data);
				return c;
			}
			else{
				RandomIntCluster c = null;
				c = new RandomIntCluster(data[0].length,K);
				if(seed >= 0) c.setSeed(seed);
				System.err.printf("Building INT vocabulary tree\n");
				c.train(ByteArrayConverter.byteToInt(data));
				return c;
			}
			
		}

		@Override
		public Class<? extends Cluster<?,?>> getClusterClass() {
			if(this.precision == Precision.BYTE)
				return RandomByteCluster.class;
			else
				return RandomIntCluster.class;
		}
	}
	
	/**
	 * Guess the type of the clusters based on the file header
	 * @param oldout
	 * @return guessed type
	 */
	public static ClusterTypeOp sniffClusterType(File oldout)  {
		for (ClusterType c : ClusterType.values()) {
			for (Precision p : Precision.values()) {
				ClusterTypeOp opts = (ClusterTypeOp) c.getOptions();
				opts.precision = p;
				
				try{
					if (IOUtils.readable(oldout, opts.getClusterClass()))
						return opts;
				} catch(Exception e) {
					
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Guess the type of the clusters based on the file header
	 * @param oldout
	 * @return guessed type
	 */
	public static ClusterTypeOp sniffClusterType(BufferedInputStream oldout)  {
		for (ClusterType c : ClusterType.values()) {
			for (Precision p : Precision.values()) {
				ClusterTypeOp opts = (ClusterTypeOp) c.getOptions();
				opts.precision = p;
				
				try {
					if (IOUtils.readable(oldout, opts.getClusterClass())) {
						return opts;
					} 
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
}
