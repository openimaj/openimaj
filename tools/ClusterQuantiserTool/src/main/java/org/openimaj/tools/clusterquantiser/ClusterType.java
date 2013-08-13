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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.ByteNearestNeighbours;
import org.openimaj.knn.ByteNearestNeighboursExact;
import org.openimaj.knn.IntNearestNeighbours;
import org.openimaj.knn.IntNearestNeighboursExact;
import org.openimaj.knn.NearestNeighboursFactory;
import org.openimaj.knn.approximate.ByteNearestNeighboursKDTree;
import org.openimaj.knn.approximate.IntNearestNeighboursKDTree;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.IntCentroidsResult;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.clustering.kmeans.HierarchicalByteKMeans;
import org.openimaj.ml.clustering.kmeans.HierarchicalByteKMeansResult;
import org.openimaj.ml.clustering.kmeans.HierarchicalIntKMeans;
import org.openimaj.ml.clustering.kmeans.HierarchicalIntKMeansResult;
import org.openimaj.ml.clustering.kmeans.IntKMeans;
import org.openimaj.ml.clustering.kmeans.KMeansConfiguration;
import org.openimaj.ml.clustering.random.RandomByteClusterer;
import org.openimaj.ml.clustering.random.RandomIntClusterer;
import org.openimaj.ml.clustering.random.RandomSetByteClusterer;
import org.openimaj.ml.clustering.random.RandomSetIntClusterer;
import org.openimaj.ml.clustering.rforest.IntRandomForest;
import org.openimaj.tools.clusterquantiser.fastkmeans.ByteKMeansInitialisers;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatch;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatchByteDataSource;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatchIntDataSource;
import org.openimaj.util.array.ByteArrayConverter;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;

/**
 * Different clustering algorithms
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public enum ClusterType implements CmdLineOptionsProvider {
	/**
	 * Randomly sampled centroids (with replacement; the same centroid might be
	 * picked multiple times)
	 */
	RANDOM {
		@Override
		public ClusterTypeOp getOptions() {
			return new RandomOp();
		}
	},
	/**
	 * Randomly sampled centroids (without replacement; a centroid can only be
	 * picked once)
	 */
	RANDOMSET {
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
	};

	/**
	 * Options for each {@link ClusterType}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static abstract class ClusterTypeOp {
		/**
		 * The precision of the clusters
		 */
		@Option(
				name = "--precision",
				aliases = "-p",
				required = false,
				usage = "Specify the cluster percision if supported")
		public Precision precision = Precision.BYTE;

		/**
		 * Create clusters from data
		 * 
		 * @param data
		 * @return clusters
		 * @throws Exception
		 */
		public abstract SpatialClusters<?> create(byte[][] data) throws Exception;

		/**
		 * Create clusters from data
		 * 
		 * @param batches
		 * @return clusters
		 * @throws Exception
		 */
		public SpatialClusters<?> create(List<SampleBatch> batches) throws Exception {
			return null;
		}

		/**
		 * @return options
		 */
		public Map<String, String> getOptionsMap() {
			return new HashMap<String, String>();
		}

		/**
		 * Set options
		 * 
		 * @param options
		 */
		public void setOptionsMap(Map<String, String> options) {

		}

		/**
		 * @return java class representing clusters
		 */
		public abstract Class<? extends SpatialClusters<?>> getClusterClass();
	}

	private static class RForestOp extends ClusterTypeOp {
		@Option(
				name = "--decisions",
				aliases = "-d",
				required = true,
				usage = "Specify number of random decisions to be made per tree.",
				metaVar = "NUMBER")
		private int decisions = 32;

		@Option(
				name = "--number-of-trees",
				aliases = "-nt",
				required = true,
				usage = "Specify number of random trees",
				metaVar = "NUMBER")
		private int ntrees = 32;

		@Override
		public SpatialClusters<int[]> create(byte[][] data) {
			final IntRandomForest rf = new IntRandomForest(ntrees, decisions);
			rf.cluster(ByteArrayConverter.byteToInt(data));
			return rf;
		}

		@Override
		public Class<? extends SpatialClusters<?>> getClusterClass() {
			return IntRandomForest.class;
		}
	}

	private static class HKMeansOp extends ClusterTypeOp {
		@Option(
				name = "--depth",
				aliases = "-d",
				required = true,
				usage = "Specify depth of tree in create mode.",
				metaVar = "NUMBER")
		private int depth = 6;

		@Option(
				name = "--clusters",
				aliases = "-k",
				required = true,
				usage = "Specify number of clusters per level.",
				metaVar = "NUMBER")
		private int K = 10;

		@Option(
				name = "--enable-approximate",
				aliases = "-ea",
				required = false,
				usage = "Enable the approximate k-means mode")
		private boolean exactMode = false;

		@Override
		public SpatialClusters<?> create(byte[][] data) {
			if (this.precision == Precision.BYTE) {
				final KMeansConfiguration<ByteNearestNeighbours, byte[]> kmc = new KMeansConfiguration<ByteNearestNeighbours, byte[]>();

				if (exactMode) {
					kmc.setNearestNeighbourFactory(new ByteNearestNeighboursExact.Factory());
				} else {
					kmc.setNearestNeighbourFactory(new ByteNearestNeighboursKDTree.Factory());
				}

				final HierarchicalByteKMeans tree = new HierarchicalByteKMeans(kmc, data[0].length, K, depth);

				System.err.printf("Building vocabulary tree\n");
				return tree.cluster(data);
			} else {
				final KMeansConfiguration<IntNearestNeighbours, int[]> kmc = new KMeansConfiguration<IntNearestNeighbours, int[]>();

				if (exactMode) {
					kmc.setNearestNeighbourFactory(new IntNearestNeighboursExact.Factory());
				} else {
					kmc.setNearestNeighbourFactory(new IntNearestNeighboursKDTree.Factory());
				}

				final HierarchicalIntKMeans tree = new HierarchicalIntKMeans(kmc, data[0].length, K, depth);

				System.err.printf("Building vocabulary tree\n");
				return tree.cluster(ByteArrayConverter.byteToInt(data));
			}
		}

		@Override
		public Class<? extends SpatialClusters<?>> getClusterClass() {
			if (this.precision == Precision.BYTE)
				return HierarchicalByteKMeansResult.class;
			else
				return HierarchicalIntKMeansResult.class;

		}
	}

	private static class FastKMeansOp extends ClusterTypeOp {
		@Option(
				name = "--clusters",
				aliases = "-k",
				required = true,
				usage = "Specify number of clusters per level.",
				metaVar = "NUMBER")
		private int K = 10;

		@Option(
				name = "--iterations",
				aliases = "-itr",
				required = false,
				usage = "Specify number of iterations.",
				metaVar = "NUMBER")
		private int I = 30;

		@Option(
				name = "--batch-size",
				aliases = "-b",
				required = false,
				usage = "Specify size of each batch for each iteration.",
				metaVar = "NUMBER")
		private int B = 50000;

		@Option(
				name = "--num-checks",
				aliases = "-nc",
				required = false,
				usage = "Specify number of checks for each kd-tree.",
				metaVar = "NUMBER")
		private int NC = 768;

		@Option(
				name = "--num-trees",
				aliases = "-nt",
				required = false,
				usage = "Specify number of kd-trees.",
				metaVar = "NUMBER")
		private int NT = 8;

		@Option(
				name = "--exact-nn",
				aliases = "-ex",
				required = false,
				usage = "Specify whether to use exact nearest neighbours.",
				metaVar = "BOOLEAN")
		private boolean E = false;

		@Option(
				name = "--fastkmeans-threads",
				aliases = "-jj",
				required = false,
				usage = "Specify the number of threads to use to train centroids.",
				metaVar = "NUMBER")
		private int jj = Runtime.getRuntime().availableProcessors();

		@Option(
				name = "--cluster-random-seed",
				aliases = "-crs",
				required = false,
				usage = "Specify a seed for the random data selection.",
				metaVar = "NUMBER")
		private long seed = -1;

		@Option(
				name = "--cluster-init",
				aliases = "-cin",
				required = false,
				usage = "Specify the type of file to be read.",
				handler = ProxyOptionHandler.class)
		public ByteKMeansInitialisers clusterInit = ByteKMeansInitialisers.RANDOM;
		public ByteKMeansInitialisers.Options clusterInitOp;

		private KMeansConfiguration<ByteNearestNeighbours, byte[]> confByte(int ndims) {
			NearestNeighboursFactory<? extends ByteNearestNeighbours, byte[]> assigner;
			final ExecutorService pool = Executors.newFixedThreadPool(jj, new DaemonThreadFactory());

			if (E) {
				assigner = new ByteNearestNeighboursExact.Factory();
			} else {
				assigner = new ByteNearestNeighboursKDTree.Factory(NT, NC);
			}

			final KMeansConfiguration<ByteNearestNeighbours, byte[]> conf = new KMeansConfiguration<ByteNearestNeighbours, byte[]>(
					K, assigner, I, B, pool);

			return conf;
		}

		private KMeansConfiguration<IntNearestNeighbours, int[]> confInt(int ndims) {
			NearestNeighboursFactory<? extends IntNearestNeighbours, int[]> assigner;
			final ExecutorService pool = Executors.newFixedThreadPool(jj, new DaemonThreadFactory());

			if (E) {
				assigner = new IntNearestNeighboursExact.Factory();
			} else {
				assigner = new IntNearestNeighboursKDTree.Factory(NT, NC);
			}

			final KMeansConfiguration<IntNearestNeighbours, int[]> conf = new KMeansConfiguration<IntNearestNeighbours, int[]>(
					K, assigner, I, B, pool);

			return conf;
		}

		@Override
		public SpatialClusters<?> create(List<SampleBatch> batches) throws Exception {
			System.err.println("Constructing a FASTKMEANS cluster");
			SpatialClusterer<?, ?> c = null;

			System.err.println("Constructing a fastkmeans worker: ");
			if (this.precision == Precision.BYTE) {
				final SampleBatchByteDataSource ds = new SampleBatchByteDataSource(batches);
				ds.setSeed(seed);

				c = new ByteKMeans(confByte(ds.numDimensions()));
				((ByteKMeans) c).seed(seed);
				clusterInitOp.setClusterInit((ByteKMeans) c);

				return ((ByteKMeans) c).cluster(ds);
			} else {
				final SampleBatchIntDataSource ds = new SampleBatchIntDataSource(batches);
				ds.setSeed(seed);

				c = new IntKMeans(confInt(ds.numDimensions()));
				((IntKMeans) c).seed(seed);

				return ((IntKMeans) c).cluster(ds);
			}
		}

		@Override
		public SpatialClusters<?> create(byte[][] data) throws Exception {
			SpatialClusterer<?, ?> c = null;
			if (this.precision == Precision.BYTE) {
				c = new ByteKMeans(confByte(data[0].length));
				((ByteKMeans) c).seed(seed);

				if (clusterInitOp == null)
					clusterInitOp = clusterInit.getOptions();

				clusterInitOp.setClusterInit((ByteKMeans) c);
				return ((ByteKMeans) c).cluster(data);
			} else {
				c = new IntKMeans(confInt(data[0].length));
				((IntKMeans) c).seed(seed);
				return ((IntKMeans) c).cluster(ByteArrayConverter.byteToInt(data));
			}
		}

		@Override
		public Class<? extends SpatialClusters<?>> getClusterClass() {
			if (this.precision == Precision.BYTE)
				return ByteCentroidsResult.class;
			else
				return IntCentroidsResult.class;
		}
	}

	private static class FastMBKMeansOp extends ClusterTypeOp {
		@Option(
				name = "--clusters",
				aliases = "-k",
				required = true,
				usage = "Specify number of clusters per level.",
				metaVar = "NUMBER")
		private int K = 10;

		@Option(
				name = "--iterations",
				aliases = "-itr",
				required = false,
				usage = "Specify number of iterations.",
				metaVar = "NUMBER")
		private int I = 30;

		@Option(
				name = "--mini-batch-size",
				aliases = "-mb",
				required = false,
				usage = "Specify size of each mini-batch for each iteration.",
				metaVar = "NUMBER")
		private int M = 10000;

		@Option(
				name = "--batch-size",
				aliases = "-b",
				required = false,
				usage = "Specify size of each batch for each iteration.",
				metaVar = "NUMBER")
		private int B = 50000;

		@Option(
				name = "--num-checks",
				aliases = "-nc",
				required = false,
				usage = "Specify number of checks for each kd-tree.",
				metaVar = "NUMBER")
		private int NC = 768;

		@Option(
				name = "--num-trees",
				aliases = "-nt",
				required = false,
				usage = "Specify number of kd-trees.",
				metaVar = "NUMBER")
		private int NT = 8;

		@Option(
				name = "--exact-nn",
				aliases = "-ex",
				required = false,
				usage = "Specify whether to use exact nearest neighbours.",
				metaVar = "NUMBER")
		private boolean E = false;

		@Option(
				name = "--fastkmeans-threads",
				aliases = "-jj",
				required = false,
				usage = "Specify the number of threads to use to train centroids.",
				metaVar = "NUMBER")
		private int jj = Runtime.getRuntime().availableProcessors();

		private KMeansConfiguration<IntNearestNeighbours, int[]> confInt(int ndims) {
			NearestNeighboursFactory<? extends IntNearestNeighbours, int[]> assigner;
			final ExecutorService pool = Executors.newFixedThreadPool(jj, new DaemonThreadFactory());

			if (E) {
				assigner = new IntNearestNeighboursExact.Factory();
			} else {
				assigner = new IntNearestNeighboursKDTree.Factory(NT, NC);
			}

			final KMeansConfiguration<IntNearestNeighbours, int[]> conf = new KMeansConfiguration<IntNearestNeighbours, int[]>(
					K, assigner, I, B, pool);

			return conf;
		}

		@Override
		public SpatialClusters<int[]> create(byte[][] data) {
			IntKMeans c = null;
			c = new IntKMeans(confInt(data[0].length));
			return c.cluster(ByteArrayConverter.byteToInt(data));
		}

		@Override
		public Class<? extends SpatialClusters<?>> getClusterClass() {
			return IntCentroidsResult.class;
		}
	}

	private static class RandomSetOp extends ClusterTypeOp {
		@Option(
				name = "--clusters",
				aliases = "-k",
				required = false,
				usage = "Specify number of clusters per level.",
				metaVar = "NUMBER")
		private int K = -1;

		@Option(
				name = "--cluster-random-seed",
				aliases = "-crs",
				required = false,
				usage = "Specify a seed for the random data selection.",
				metaVar = "NUMBER")
		private int seed = -1;

		@Override
		public SpatialClusters<?> create(byte[][] data) {
			if (this.precision == Precision.BYTE) {
				RandomSetByteClusterer c = null;
				c = new RandomSetByteClusterer(data[0].length, K);
				if (seed >= 0)
					c.setSeed(seed);

				System.err.printf("Building BYTE vocabulary tree\n");
				return c.cluster(data);
			} else {
				RandomSetIntClusterer c = null;
				c = new RandomSetIntClusterer(data[0].length, K);
				if (seed >= 0)
					c.setSeed(seed);

				System.err.printf("Building INT vocabulary tree\n");
				return c.cluster(ByteArrayConverter.byteToInt(data));
			}
		}

		@Override
		public SpatialClusters<?> create(List<SampleBatch> batches) throws Exception {

			if (this.precision == Precision.BYTE) {
				final SampleBatchByteDataSource ds = new SampleBatchByteDataSource(batches);
				ds.setSeed(seed);

				RandomSetByteClusterer c = null;
				c = new RandomSetByteClusterer(ds.numDimensions(), K);

				return c.cluster(ds);
			} else {
				final SampleBatchIntDataSource ds = new SampleBatchIntDataSource(batches);
				ds.setSeed(seed);

				RandomSetIntClusterer c = null;
				c = new RandomSetIntClusterer(ds.numDimensions(), K);

				return c.cluster(ds);
			}
		}

		@Override
		public Class<? extends SpatialClusters<?>> getClusterClass() {
			if (this.precision == Precision.BYTE)
				return ByteCentroidsResult.class;
			else
				return IntCentroidsResult.class;
		}
	}

	private static class RandomOp extends ClusterTypeOp {
		@Option(
				name = "--clusters",
				aliases = "-k",
				required = false,
				usage = "Specify number of clusters per level.",
				metaVar = "NUMBER")
		private int K = -1;

		@Option(
				name = "--cluster-random-seed",
				aliases = "-crs",
				required = false,
				usage = "Specify a seed for the random data selection.",
				metaVar = "NUMBER")
		private int seed = -1;

		@Override
		public SpatialClusters<?> create(byte[][] data) {
			if (this.precision == Precision.BYTE) {
				RandomByteClusterer c = null;
				c = new RandomByteClusterer(data[0].length, K);
				if (seed >= 0)
					c.setSeed(seed);

				System.err.printf("Building BYTE vocabulary tree\n");
				return c.cluster(data);
			} else {
				RandomIntClusterer c = null;
				c = new RandomIntClusterer(data[0].length, K);
				if (seed >= 0)
					c.setSeed(seed);

				System.err.printf("Building INT vocabulary tree\n");
				return c.cluster(ByteArrayConverter.byteToInt(data));
			}

		}

		@Override
		public Class<? extends SpatialClusters<?>> getClusterClass() {
			if (this.precision == Precision.BYTE)
				return ByteCentroidsResult.class;
			else
				return IntCentroidsResult.class;
		}
	}

	/**
	 * Guess the type of the clusters based on the file header
	 * 
	 * @param oldout
	 * @return guessed type
	 */
	public static ClusterTypeOp sniffClusterType(File oldout) {
		for (final ClusterType c : ClusterType.values()) {
			for (final Precision p : Precision.values()) {
				final ClusterTypeOp opts = (ClusterTypeOp) c.getOptions();
				opts.precision = p;

				try {
					if (IOUtils.readable(oldout, opts.getClusterClass()))
						return opts;
				} catch (final Exception e) {

				}
			}
		}

		return null;
	}

	/**
	 * Guess the type of the clusters based on the file header
	 * 
	 * @param oldout
	 * @return guessed type
	 */
	public static ClusterTypeOp sniffClusterType(BufferedInputStream oldout) {
		for (final ClusterType c : ClusterType.values()) {
			for (final Precision p : Precision.values()) {
				final ClusterTypeOp opts = (ClusterTypeOp) c.getOptions();
				opts.precision = p;

				try {
					if (IOUtils.readable(oldout, opts.getClusterClass())) {
						return opts;
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
