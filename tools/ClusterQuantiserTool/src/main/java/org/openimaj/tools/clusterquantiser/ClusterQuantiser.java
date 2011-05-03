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

import gnu.trove.TIntArrayList;

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
import org.openimaj.ml.clustering.Cluster;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatch;
import org.openimaj.util.array.ByteArrayConverter;


public class ClusterQuantiser {
	public static Cluster<?,?> do_create(ClusterQuantiserOptions options)
			throws IOException {
		File treeFile = new File(options.getTreeFile());
		ClusterType clusterType = options.getClusterType();

		Cluster<?,?> cluster = null;

		// perform sampling if required
		if (options.isBatchedSampleMode()) {

			cluster = clusterType.create(do_getSampleBatches(options));
			IOUtils.writeBinary(treeFile, cluster);
		} else {
			byte[][] data = do_getSamples(options);
			System.err.printf("Using %d records\n", data.length);
			cluster = clusterType.create(data);
			System.err.println("Writing cluster file to " + treeFile);
			IOUtils.writeBinary(treeFile, cluster);
		}
		return cluster;

	}

	public static List<SampleBatch> do_getSampleBatches(
			ClusterQuantiserOptions options) throws IOException {
		if (options.isSamplesFileMode()) {
			try {
				System.err.println("Attempting to read sample batch file...");
				return SampleBatch.readSampleBatches(options.getSamplesFile());
			} catch (Exception e) {
				System.err.println("... Failed! ");
				return null;
			}
		}

		List<SampleBatch> batches = new ArrayList<SampleBatch>();
		List<File> input_files = options.getInputFiles();
		FileType type = options.getFileType();
		int n_input_files = input_files.size();
		List<Header> headers = new ArrayList<Header>(n_input_files);

		// read the headers and count the total number of features
		System.err.printf("Reading input %8d / %8d", 0, n_input_files);
		int totalFeatures = 0;
		int[] cumSum = new int[n_input_files + 1];
		for (int i = 0; i < n_input_files; i++) {
			Header h = type.readHeader(input_files.get(i));

			totalFeatures += h.nfeatures;
			cumSum[i + 1] = totalFeatures;
			headers.add(h);

			System.err.printf("\r%8d / %8d", i + 1, n_input_files);
		}

		System.err.println();
		int samples = options.getSamples();
		if (samples <= 0 || samples > totalFeatures) {
			System.err.printf(
					"Samples requested %d larger than total samples %d...\n",
					samples, totalFeatures);

			for (int i = 0; i < n_input_files; i++) {
				if (cumSum[i + 1] - cumSum[i] == 0)
					continue;
				SampleBatch sb = new SampleBatch(type, input_files.get(i),
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
			TIntArrayList intraFileIndices = new TIntArrayList();
			for (int j = 0, s = 0; j < n_input_files; j++) {
				intraFileIndices.clear();

				// go through samples and find ones belonging to this doc
				for (int i = 0; i < samples; i++) {
					int idx = rndIndices[i];

					if (idx >= cumSum[j] && idx < cumSum[j + 1]) {
						intraFileIndices.add(idx - cumSum[j]);
					}
				}

				if (intraFileIndices.size() > 0) {
					SampleBatch sb = new SampleBatch(type, input_files.get(j),
							s, s + intraFileIndices.size(),
							intraFileIndices.toNativeArray());
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

	public static byte[][] do_getSamples(ClusterQuantiserOptions options)
			throws IOException {

		byte[][] data = null;
		if (options.isSamplesFileMode()) {
			data = options.getSampleKeypoints();
		} else {
			List<File> input_files = options.getInputFiles();
			FileType type = options.getFileType();
			int n_input_files = input_files.size();
			List<Header> headers = new ArrayList<Header>(n_input_files);

			// read the headers and count the total number of features
			System.err.printf("Reading input %8d / %8d", 0, n_input_files);
			int totalFeatures = 0;
			int[] cumSum = new int[n_input_files + 1];
			for (int i = 0; i < n_input_files; i++) {
				Header h = type.readHeader(input_files.get(i));

				totalFeatures += h.nfeatures;
				cumSum[i + 1] = totalFeatures;
				headers.add(h);

				System.err.printf("\r%8d / %8d", i + 1, n_input_files);
			}

			System.err.println();
			int samples = options.getSamples();
			if (samples <= 0 || samples > totalFeatures) {
				System.err
						.printf("Samples requested %d larger than total samples %d...\n",
								samples, totalFeatures);
				// no sampled requested or more samples requested than features
				// exist
				// so use all features
				data = new byte[totalFeatures][];

				for (int i = 0, j = 0; i < n_input_files; i++) {
					byte[][] fd = type.readFeatures(input_files.get(i));

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
				TIntArrayList intraFileIndices = new TIntArrayList();
				for (int j = 0, s = 0; j < n_input_files; j++) {
					intraFileIndices.clear();

					// go through samples and find ones belonging to this doc
					for (int i = 0; i < samples; i++) {
						int idx = rndIndices[i];

						if (idx >= cumSum[j] && idx < cumSum[j + 1]) {
							intraFileIndices.add(idx - cumSum[j]);
						}
					}

					if (intraFileIndices.size() > 0) {
						byte[][] f = type.readFeatures(input_files.get(j),
								intraFileIndices.toNativeArray());
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
				FileOutputStream fos = new FileOutputStream(
						options.getSamplesFile());
				ObjectOutputStream dos = new ObjectOutputStream(fos);
				dos.writeObject(data);
			}
		}
		return data;
	}

	public static void do_info(AbstractClusterQuantiserOptions options)
			throws IOException {
		Cluster<?,?> cluster = IOUtils.read(new File(options.getTreeFile()),options.getClusterClass());
		cluster.optimize(false);
		System.out.println("Cluster loaded...");
//		if (options.getOtherInfoFile() != null) {
//			Cluster<?,?> otherCluster = IOUtils.read(
//					new File(options.getOtherInfoFile()),
//					options.getOtherInfoClass());
//			System.out.println("Other cluster loaded...");
//			int[] pushedTo = cluster.push(otherCluster.getClusters());
//			System.out.println("Other cluster pushed...");
//			byte[][] clusterCenters = (byte[][]) cluster.getClusters();
//			byte[][] otherClusterCenters = (byte[][]) otherCluster
//					.getClusters();
//			System.out.println("Calculating difference...");
//			float totalDiff = 0.0f;
//			for (int i = 0; i < pushedTo.length; i++) {
//				totalDiff += Math.sqrt(byteDiff(otherClusterCenters[i],clusterCenters[pushedTo[i]]));
//			}
//			totalDiff = (float) totalDiff/pushedTo.length;
//			System.out.println("Total Difference: " + totalDiff);
//		} else {
		System.out.println(cluster);
//		}
	}

//	private static float byteDiff(byte[] b1, byte[] b2) {
//		float distance = 0;
//		for (int i = 0; i < b1.length; i++) {
//			distance += Math.pow(b1[i] - b2[i], 2);
//		}
//		return distance;
//	}

	public static void do_quant(ClusterQuantiserOptions cqo)
			throws IOException, InterruptedException {
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors());

		List<QuantizerJob> jobs = QuantizerJob.getJobs(cqo);

		System.out.format("Using %d processors\n", cqo.getConcurrency());
		es.invokeAll(jobs);
		System.out.println();
		es.shutdown();
	}

	static class QuantizerJob implements Callable<Boolean> {
		Cluster<?,?> tree;
		List<File> inputFiles;
		// FileType fileType;
		// String extension;
		// private ClusterType ctype;
		// private File outputFile;
		private ClusterQuantiserOptions cqo;

		static int count = 0;
		static int total;

		static synchronized void incr() {
			count++;
			System.err.printf(
					"\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b%8d / %8d", count,
					total);
		}

		protected QuantizerJob(ClusterQuantiserOptions cqo, Cluster<?,?> tree)
				throws IOException {
			this.cqo = cqo;
			this.tree = tree;
			this.inputFiles = cqo.getInputFiles();
		}

		protected QuantizerJob(ClusterQuantiserOptions cqo,
				List<File> inputFiles, Cluster<?,?> tree) throws IOException {
			this.cqo = cqo;
			this.tree = tree;
			this.inputFiles = inputFiles;
		}

		public static List<QuantizerJob> getJobs(ClusterQuantiserOptions cqo)
				throws IOException {

			List<QuantizerJob> jobs = new ArrayList<QuantizerJob>(
					cqo.getConcurrency());
			int size = cqo.getInputFiles().size() / cqo.getConcurrency();
			Cluster<?,?> tree = IOUtils.read(new File(cqo.getTreeFile()),cqo.getClusterClass());
			tree.optimize(cqo.exactQuant);

			QuantizerJob.count = 0;
			QuantizerJob.total = cqo.getInputFiles().size();
			for (int i = 0; i < cqo.getConcurrency() - 1; i++) {
				QuantizerJob job = new QuantizerJob(cqo, cqo.getInputFiles()
						.subList(i * size, (i + 1) * size), tree);
				jobs.add(job);
			}
			// add remaining
			QuantizerJob job = new QuantizerJob(cqo, cqo.getInputFiles()
					.subList((cqo.getConcurrency() - 1) * size,
							cqo.getInputFiles().size()), tree);
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
						outFile = new File(cqo.getOutputFile()
								.getAbsolutePath()
								+ File.separator
								+ outFile.getName());
					if (outFile.exists() && outFile.getTotalSpace() > 0) {
						incr();
						continue;
					}
					FeatureFile input = cqo.getFileType().read(inputFiles.get(i));
					PrintWriter pw = null;

					try {
						pw = new PrintWriter(new FileWriter(outFile));
						pw.format("%d\n%d\n", input.size(),
								tree.getNumberClusters());
						// int [] clusters = new int[input.size()];
						for (FeatureFileFeature fff : input) {
							int cluster = -1;
							if (tree.getClusters() instanceof byte[][])
								cluster = ((Cluster<?,byte[]>)tree).push_one(fff.data);
							else
								cluster = ((Cluster<?,int[]>)tree).push_one(ByteArrayConverter.byteToInt(fff.data));
							pw.format("%s %d\n", fff.location.trim(), cluster);
						}
					} catch (IOException e) {
						e.printStackTrace();
						throw new Error(e); // IO error when writing - die.
					} finally {
						if (pw != null) {
							pw.flush();
							pw.close();
							input.close();
						}

					}

				} catch (Exception e) {
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

	public static ClusterQuantiserOptions mainOptions(String[] args)
			throws InterruptedException, CmdLineException {
		ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
		options.prepare();

		return options;
	}

	public static void main(String[] args) throws InterruptedException,
			CmdLineException {
		ClusterQuantiserOptions options = mainOptions(args);
		try {
			List<File> inputFiles = options.getInputFiles();

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
					&& inputFiles.size() > 0) {
				if (options.isBatchedSampleMode()) {
					do_getSampleBatches(options);
				} else {
					do_getSamples(options);
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
