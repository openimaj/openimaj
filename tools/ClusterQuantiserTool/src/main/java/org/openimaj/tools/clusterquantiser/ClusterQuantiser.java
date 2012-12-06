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

import gnu.trove.list.array.TIntArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.data.RandomData;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.IntCentroidsResult;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.assignment.hard.KDTreeByteEuclideanAssigner;
import org.openimaj.ml.clustering.assignment.hard.KDTreeIntEuclideanAssigner;
import org.openimaj.time.Timer;
import org.openimaj.tools.clusterquantiser.ClusterType.ClusterTypeOp;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatch;
import org.openimaj.util.array.ByteArrayConverter;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;

/**
 * A tool for clustering and quantising local features.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ClusterQuantiser {
	/**
	 * create new clusters
	 * 
	 * @param options
	 * @return clusters
	 * @throws Exception
	 */
	public static SpatialClusters<?> do_create(ClusterQuantiserOptions options) throws Exception {
		final File treeFile = new File(options.getTreeFile());
		final ClusterTypeOp clusterType = options.getClusterType();

		SpatialClusters<?> cluster = null;

		// perform sampling if required
		if (options.isBatchedSampleMode()) {
			cluster = clusterType.create(do_getSampleBatches(options));
			IOUtils.writeBinary(treeFile, cluster);
		} else {
			final byte[][] data = do_getSamples(options);
			System.err.printf("Using %d records\n", data.length);
			cluster = clusterType.create(data);
			System.err.println("Writing cluster file to " + treeFile);
			IOUtils.writeBinary(treeFile, cluster);
		}
		return cluster;

	}

	/**
	 * Get sample batches
	 * 
	 * @param options
	 * @return batches
	 * @throws IOException
	 */
	public static List<SampleBatch> do_getSampleBatches(ClusterQuantiserOptions options) throws IOException {
		if (options.isSamplesFileMode()) {
			try {
				System.err.println("Attempting to read sample batch file...");
				return SampleBatch.readSampleBatches(options.getSamplesFile());
			} catch (final Exception e) {
				System.err.println("... Failed! ");
				return null;
			}
		}

		final List<SampleBatch> batches = new ArrayList<SampleBatch>();
		final List<File> input_files = options.getInputFiles();
		final FileType type = options.getFileType();
		final int n_input_files = input_files.size();
		final List<Header> headers = new ArrayList<Header>(n_input_files);

		// read the headers and count the total number of features
		System.err.printf("Reading input %8d / %8d", 0, n_input_files);
		int totalFeatures = 0;
		final int[] cumSum = new int[n_input_files + 1];
		for (int i = 0; i < n_input_files; i++) {
			final Header h = type.readHeader(input_files.get(i));

			totalFeatures += h.nfeatures;
			cumSum[i + 1] = totalFeatures;
			headers.add(h);

			System.err.printf("\r%8d / %8d", i + 1, n_input_files);
		}

		System.err.println();
		final int samples = options.getSamples();
		if (samples <= 0 || samples > totalFeatures) {
			System.err.printf(
					"Samples requested %d larger than total samples %d...\n",
					samples, totalFeatures);

			for (int i = 0; i < n_input_files; i++) {
				if (cumSum[i + 1] - cumSum[i] == 0)
					continue;
				final SampleBatch sb = new SampleBatch(type, input_files.get(i),
						cumSum[i], cumSum[i + 1]);
				batches.add(sb);
				System.err.printf("\rConstructing sample batches %8d / %8d", i,
						n_input_files);
			}
			System.err.println();
			System.err.println("Done...");
		} else {
			System.err.println("Shuffling and sampling ...");
			// generate sample unique random numbers between 0 and totalFeatures
			int[] rndIndices = null;
			if (options.getRandomSeed() == -1)
				rndIndices = RandomData.getUniqueRandomInts(samples, 0,
						totalFeatures);
			else
				rndIndices = RandomData.getUniqueRandomInts(samples, 0,
						totalFeatures, new Random(options.getRandomSeed()));
			System.err.println("Done! Extracting features required");
			final TIntArrayList intraFileIndices = new TIntArrayList();
			for (int j = 0, s = 0; j < n_input_files; j++) {
				intraFileIndices.clear();

				// go through samples and find ones belonging to this doc
				for (int i = 0; i < samples; i++) {
					final int idx = rndIndices[i];

					if (idx >= cumSum[j] && idx < cumSum[j + 1]) {
						intraFileIndices.add(idx - cumSum[j]);
					}
				}

				if (intraFileIndices.size() > 0) {
					final SampleBatch sb = new SampleBatch(type, input_files.get(j),
							s, s + intraFileIndices.size(),
							intraFileIndices.toArray());
					batches.add(sb);
					s += intraFileIndices.size();
					System.err.printf("\r%8d / %8d", s, samples);
				}

			}
			System.err.println();
		}
		if (batches.size() > 0 && options.getSamplesFile() != null) {
			System.err.println("Writing samples file...");
			SampleBatch.writeSampleBatches(batches, options.getSamplesFile());
		}
		return batches;
	}

	/**
	 * Get samples
	 * 
	 * @param options
	 * @return samples
	 * @throws IOException
	 */
	public static byte[][] do_getSamples(ClusterQuantiserOptions options)
			throws IOException
	{

		byte[][] data = null;
		if (options.isSamplesFileMode()) {
			data = options.getSampleKeypoints();
		} else {
			final List<File> input_files = options.getInputFiles();
			final FileType type = options.getFileType();
			final int n_input_files = input_files.size();
			final List<Header> headers = new ArrayList<Header>(n_input_files);

			// read the headers and count the total number of features
			System.err.printf("Reading input %8d / %8d", 0, n_input_files);
			int totalFeatures = 0;
			final int[] cumSum = new int[n_input_files + 1];
			for (int i = 0; i < n_input_files; i++) {
				final Header h = type.readHeader(input_files.get(i));

				totalFeatures += h.nfeatures;
				cumSum[i + 1] = totalFeatures;
				headers.add(h);

				System.err.printf("\r%8d / %8d", i + 1, n_input_files);
			}

			System.err.println();
			final int samples = options.getSamples();
			if (samples <= 0 || samples > totalFeatures) {
				System.err
						.printf("Samples requested %d larger than total samples %d...\n",
								samples, totalFeatures);
				// no sampled requested or more samples requested than features
				// exist
				// so use all features
				data = new byte[totalFeatures][];

				for (int i = 0, j = 0; i < n_input_files; i++) {
					final byte[][] fd = type.readFeatures(input_files.get(i));

					for (int k = 0; k < fd.length; k++) {
						data[j + k] = fd[k];
						System.err.printf("\r%8d / %8d", j, totalFeatures);
					}
					j += fd.length;
				}
			} else {
				System.err.println("Shuffling and sampling ...");

				data = new byte[samples][];

				// generate sample unique random numbers between 0 and
				// totalFeatures
				int[] rndIndices = null;
				if (options.getRandomSeed() == -1)
					rndIndices = RandomData.getUniqueRandomInts(samples, 0,
							totalFeatures);
				else
					rndIndices = RandomData.getUniqueRandomInts(samples, 0,
							totalFeatures, new Random(options.getRandomSeed()));
				System.err.println("Done! Extracting features required");
				final TIntArrayList intraFileIndices = new TIntArrayList();
				for (int j = 0, s = 0; j < n_input_files; j++) {
					intraFileIndices.clear();

					// go through samples and find ones belonging to this doc
					for (int i = 0; i < samples; i++) {
						final int idx = rndIndices[i];

						if (idx >= cumSum[j] && idx < cumSum[j + 1]) {
							intraFileIndices.add(idx - cumSum[j]);
						}
					}

					if (intraFileIndices.size() > 0) {
						final byte[][] f = type.readFeatures(input_files.get(j),
								intraFileIndices.toArray());
						for (int i = 0; i < intraFileIndices.size(); i++, s++) {
							data[s] = f[i];
							System.err
									.printf("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%8d / %8d",
											s + 1, samples);
						}
					}

				}
				System.err.println();
			}
			if (data != null && options.getSamplesFile() != null) {
				System.err.println("Writing samples file...");
				final FileOutputStream fos = new FileOutputStream(
						options.getSamplesFile());
				final ObjectOutputStream dos = new ObjectOutputStream(fos);
				dos.writeObject(data);
				dos.close();
			}
		}
		return data;
	}

	/**
	 * Print info about clusters
	 * 
	 * @param options
	 * @throws IOException
	 */
	public static void do_info(AbstractClusterQuantiserOptions options)
			throws IOException
	{
		final SpatialClusters<?> cluster = IOUtils.read(new File(options.getTreeFile()), options.getClusterClass());
		System.out.println("Cluster loaded...");
		System.out.println(cluster);
	}

	/**
	 * Quantise features
	 * 
	 * @param cqo
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void do_quant(ClusterQuantiserOptions cqo) throws IOException, InterruptedException {
		final ExecutorService es = Executors.newFixedThreadPool(cqo.getConcurrency(), new DaemonThreadFactory());

		final List<QuantizerJob> jobs = QuantizerJob.getJobs(cqo);

		System.out.format("Using %d processors\n", cqo.getConcurrency());
		es.invokeAll(jobs);
		es.shutdown();
	}

	static class QuantizerJob implements Callable<Boolean> {
		SpatialClusters<?> tree;
		HardAssigner<?, ?, ?> assigner;

		List<File> inputFiles;
		// FileType fileType;
		// String extension;
		// private ClusterType ctype;
		// private File outputFile;
		private ClusterQuantiserOptions cqo;
		private String commonRoot;

		static int count = 0;
		static int total;

		static synchronized void incr() {
			count++;
			System.err.printf(
					"\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%8d / %8d", count,
					total);
		}

		protected QuantizerJob(ClusterQuantiserOptions cqo, SpatialClusters<?> tree, HardAssigner<?, ?, ?> assigner)
				throws IOException
		{
			this.cqo = cqo;
			this.tree = tree;
			this.inputFiles = cqo.getInputFiles();
			this.commonRoot = cqo.getInputFileCommonRoot();
			this.assigner = assigner;
		}

		protected QuantizerJob(ClusterQuantiserOptions cqo,
				List<File> inputFiles, SpatialClusters<?> clusters, HardAssigner<?, ?, ?> assigner) throws IOException
		{
			this.cqo = cqo;
			this.tree = clusters;
			this.inputFiles = inputFiles;
			this.commonRoot = cqo.getInputFileCommonRoot();
			this.assigner = assigner;
		}

		public static List<QuantizerJob> getJobs(ClusterQuantiserOptions cqo)
				throws IOException
		{

			final List<QuantizerJob> jobs = new ArrayList<QuantizerJob>(
					cqo.getConcurrency());
			final int size = cqo.getInputFiles().size() / cqo.getConcurrency();

			final SpatialClusters<?> clusters = IOUtils.read(new File(cqo.getTreeFile()), cqo.getClusterClass());

			HardAssigner<?, ?, ?> assigner;
			if (!cqo.exactQuant) {
				assigner = clusters.defaultHardAssigner();
			} else {
				if (clusters instanceof ByteCentroidsResult)
					assigner = new KDTreeByteEuclideanAssigner((ByteCentroidsResult) clusters);
				else
					assigner = new KDTreeIntEuclideanAssigner((IntCentroidsResult) clusters);
			}

			QuantizerJob.count = 0;
			QuantizerJob.total = cqo.getInputFiles().size();
			for (int i = 0; i < cqo.getConcurrency() - 1; i++) {
				final QuantizerJob job = new QuantizerJob(cqo, cqo.getInputFiles().subList(i * size, (i + 1) * size),
						clusters, assigner);
				jobs.add(job);
			}
			// add remaining
			final QuantizerJob job = new QuantizerJob(cqo,
					cqo.getInputFiles().subList((cqo.getConcurrency() - 1) * size,
							cqo.getInputFiles().size()), clusters, assigner);
			jobs.add(job);

			return jobs;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Boolean call() throws Exception {
			for (int i = 0; i < inputFiles.size(); i++) {
				try {
					File outFile = new File(inputFiles.get(i)
							+ cqo.getExtension());
					if (cqo.getOutputFile() != null)
						outFile = new File(cqo.getOutputFile().getAbsolutePath() // /output
								+ File.separator // /
								+ outFile.getAbsolutePath().substring(this.commonRoot.length())); // /filename.out
					if (outFile.exists() && outFile.getTotalSpace() > 0) {
						incr();
						continue;
					}
					final FeatureFile input = cqo.getFileType().read(inputFiles.get(i));
					PrintWriter pw = null;
					// Make the parent directory if you need to
					if (!outFile.getParentFile().exists()) {
						if (!outFile.getParentFile().mkdirs())
							throw new IOException("couldn't make output directory: " + outFile.getParentFile());
					}
					final Timer t = Timer.timer();
					try {
						pw = new PrintWriter(new FileWriter(outFile));
						pw.format("%d\n%d\n", input.size(),
								tree.numClusters());
						// int [] clusters = new int[input.size()];
						for (final FeatureFileFeature fff : input) {
							int cluster = -1;
							if (tree.getClass().getName().contains("Byte"))
								cluster = ((HardAssigner<byte[], ?, ?>) assigner).assign(fff.data);
							else
								cluster = ((HardAssigner<int[], ?, ?>) assigner).assign(ByteArrayConverter
										.byteToInt(fff.data));
							pw.format("%s %d\n", fff.location.trim(), cluster);
						}
					} catch (final IOException e) {
						e.printStackTrace();
						throw new Error(e); // IO error when writing - die.
					} finally {
						if (pw != null) {
							pw.flush();
							pw.close();
							input.close();
						}

					}
					t.stop();
					if (cqo.printTiming()) {
						System.out.println("Took: " + t.duration());
					}

				} catch (final Exception e) {
					// Error processing an individual file; print error then
					// continue
					e.printStackTrace();
					System.err.println("Error processing file:"
							+ inputFiles.get(i));
					System.err
							.println("(Exception was " + e.getMessage() + ")");
				}

				// System.err.printf("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%8d / %8d",
				// i+1, input_files.size());
				incr();
			}
			// System.out.println();
			return true;
		}
	}

	/**
	 * Prepare options
	 * 
	 * @param args
	 * @return prepared options
	 * @throws InterruptedException
	 * @throws CmdLineException
	 */
	public static ClusterQuantiserOptions mainOptions(String[] args)
			throws InterruptedException, CmdLineException
	{
		final ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
		options.prepare();

		return options;
	}

	/**
	 * The main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			final ClusterQuantiserOptions options = mainOptions(args);

			final List<File> inputFiles = options.getInputFiles();

			if (options.getVerbosity() >= 0 && !options.isInfoMode())
				System.err
						.printf("We have %d input files\n", inputFiles.size());

			if (options.isCreateMode()) {
				do_create(options);
			} else if (options.isInfoMode()) {
				do_info(options);
			} else if (options.isQuantMode()) {
				do_quant(options);
			} else if (options.getSamplesFile() != null
					&& inputFiles.size() > 0)
			{
				if (options.isBatchedSampleMode()) {
					do_getSampleBatches(options);
				} else {
					do_getSamples(options);
				}
			}
		} catch (final CmdLineException cmdline) {
			System.err.print(cmdline);
		} catch (final IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
