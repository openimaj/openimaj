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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;
import org.openimaj.ml.clustering.IntCentroidsResult;
import org.openimaj.tools.clusterquantiser.samplebatch.SampleBatch;

/**
 * Tests for {@link ClusterQuantiser}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ClusterQuantiserTest {
	/**
	 * Temp folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	String[] keyFiles = new String[] {
			new URI(this.getClass().getResource("keys/ukbench00004.key").toString()).getPath(),
			new URI(this.getClass().getResource("keys/ukbench00005.key").toString()).getPath(),
			new URI(this.getClass().getResource("keys/ukbench00006.key").toString()).getPath(),
	};

	/**
	 * Default constructor added so that the {@link URISyntaxException} can be
	 * thrown if the keyFiles member initialisation fails.
	 * 
	 * @throws URISyntaxException
	 */
	public ClusterQuantiserTest() throws URISyntaxException {

	}

	/**
	 * Test samples file
	 * 
	 * @throws CmdLineException
	 * @throws IOException
	 */
	@Test
	public void testSamplesFile() throws CmdLineException, IOException {
		final File samplesOutFile = folder.newFile("samples-testSamplesFile.out");
		samplesOutFile.delete();

		String[] args = new String[] { "-t", "LOWE_KEYPOINT_ASCII", "-k", "2", "-d", "1", "-s", "5", "-sf", samplesOutFile
				.getAbsolutePath(), keyFiles[0], keyFiles[1], keyFiles[2] };
		ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
		options.prepare();
		final byte[][] samples = ClusterQuantiser.do_getSamples(options);

		args = new String[] { "-ct", "HKMEANS", "-k", "2", "-d", "1", "-c", folder.newFile("HKMEANS-testSamplesFile.voc")
				.getAbsolutePath(), "-sf", samplesOutFile.getAbsolutePath() };
		options = new ClusterQuantiserOptions(args);
		options.prepare();
		options.loadSamplesFile();
		final byte[][] gotSamples = options.getSampleKeypoints();
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < samples[i].length; j++) {
				assertTrue(samples[i][j] == gotSamples[i][j]);
			}
		}
	}

	/**
	 * test batch samples
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBatchSamplesFile() throws Exception {
		final File samplesOutFile = folder.newFile("testBatchSamplesFile.out");
		samplesOutFile.delete();

		String[] args = new String[] { "-bs", "-rs", "0", "-t", "LOWE_KEYPOINT_ASCII", "-k", "1", "-d", "1", "-sf", samplesOutFile
				.getAbsolutePath(), "-s", "5", keyFiles[0], keyFiles[1], keyFiles[2] };
		ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
		options.prepare();
		final List<SampleBatch> sampleBatchesSaved = ClusterQuantiser.do_getSampleBatches(options);
		options = new ClusterQuantiserOptions(args);
		options.prepare();
		final List<SampleBatch> sampleBatchesLoaded = ClusterQuantiser.do_getSampleBatches(options);

		for (int i = 0; i < sampleBatchesSaved.size(); i++) {
			assertTrue(sampleBatchesLoaded.get(i).equals(sampleBatchesSaved.get(i)));
		}

		final File clusterOutFile = folder.newFile("cluster-testBatchSamplesFile.out");

		args = new String[] { "-c", clusterOutFile.getAbsolutePath(), "-bs", "-ct", "FASTKMEANS", "-sf", samplesOutFile
				.getAbsolutePath(), "-s", "1", "-k", "1", "-b", "3", "-itr", "3", "-crs", "0", "-p", "INT" };
		options = new ClusterQuantiserOptions(args);
		options.prepare();
		final IntCentroidsResult generatedCluster = (IntCentroidsResult) ClusterQuantiser.do_create(options);
		System.out.println(generatedCluster);

		// Compare to non batch sampled cluster
		final File nonBSclusterOutFile = folder.newFile("cluster-testBatchSamplesFile2.out");
		args = new String[] { "-c", nonBSclusterOutFile.getAbsolutePath(), "-rs", "0", "-ct", "FASTKMEANS", "-t", "LOWE_KEYPOINT_ASCII", "-s", "5", "-k", "2", "-crs", "0", keyFiles[0], keyFiles[1], keyFiles[2] };
		options = new ClusterQuantiserOptions(args);
		options.prepare();
		// FastIntKMeans generatedNonBSCluster = (FastIntKMeans)
		// ClusterQuantiser.do_create(options);
		// assertTrue(generatedNonBSCluster.equals(generatedCluster));
	}

	/**
	 * Test info mode
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInfo() throws Exception {
		final File cFile = folder.newFile("RANDOM-testInfo.voc");
		String[] args = new String[] { "-ct", "RANDOM", "-c", cFile.getAbsolutePath(), "-t", "LOWE_KEYPOINT_ASCII", "-s", "5", "-k", "5", keyFiles[0], keyFiles[1], keyFiles[2] };
		ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
		options.prepare();
		ClusterQuantiser.do_create(options);
		args = new String[] { "-ct", "RANDOM", "-if", cFile.getAbsolutePath() };
		options = new ClusterQuantiserOptions(args);
		options.prepare();
		ClusterQuantiser.do_info(options);
	}

	/**
	 * Test colour quantisation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testColourQuantisation() throws Exception {
		try {
			final File cFile = folder.newFile("RANDOMSET-testColourQuantisation.voc");
			final File oFile = folder.newFile("quantised-testColourQuantisation.output");
			// File kFile = new File(new
			// URI(this.getClass().getResource("testColour.bkey").toString()).getPath());
			final File kFileSub1 = new File(
					new URI(this.getClass().getResource("sub1/testColour.bkey").toString()).getPath());
			final File kFileSub2 = new File(
					new URI(this.getClass().getResource("sub2/testColour.bkey").toString()).getPath());
			oFile.delete();
			String[] args = new String[] { "-ct", "RANDOMSET", "-c", cFile.getAbsolutePath(), "-t", "LOWE_KEYPOINT_ASCII", "-s", "5", "-k", "5", keyFiles[0], keyFiles[1], keyFiles[2] };
			ClusterQuantiser.main(args);
			args = new String[] { "-q", cFile.getAbsolutePath(), "-o", oFile.getAbsolutePath(), "-t", "BINARY_KEYPOINT", "-k", "5", "-d", "1", kFileSub1
					.getAbsolutePath(), kFileSub2.getAbsolutePath() };
			final ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
			options.prepare();
			ClusterQuantiser.do_quant(options);

			// Now read the quantised features and the normal keypoints
			File firstFileFound = null;
			final ArrayList<File> filesToSearch = new ArrayList<File>();
			filesToSearch.add(oFile);
			while (firstFileFound == null && filesToSearch.size() > 0)
				for (final File file : filesToSearch.remove(0).listFiles()) {
					if (file.isDirectory())
						filesToSearch.add(file);
					else {
						firstFileFound = file;
						break;
					}
				}
			final FileLocalFeatureList<QuantisedKeypoint> qList =
					FileLocalFeatureList.read(firstFileFound,
							QuantisedKeypoint.class);
			final FileLocalFeatureList<Keypoint> fList = FileLocalFeatureList.read(kFileSub1, Keypoint.class);

			// Do some data conversion (make sure the types are all good)
			qList.asDataArray(new int[qList.size()][]);
			fList.asDataArray(new byte[fList.size()][]);

			// Test they have the same number of local keypoints (best guess at
			// correctness)
			assertTrue(qList.size() == fList.size());
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
	}

	//
	/**
	 * Test affine sift
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAffineEnriched() throws Exception {
		final File cFile = folder.newFile("RANDOMSET.voc");
		final File oFile = folder.newFile("ASIFTENRICHED_quantised.output");
		final File kFile = new File(
				new URI(this.getClass().getResource("testAsiftEnriched.bkey").toString()).getPath());
		oFile.delete();
		String[] args = new String[] { "-ct", "RANDOMSET", "-c", cFile.getAbsolutePath(), "-t", "LOWE_KEYPOINT_ASCII", "-s", "5", "-k", "5", keyFiles[0], keyFiles[1], keyFiles[2] };
		ClusterQuantiser.main(args);
		args = new String[] { "-q", cFile.getAbsolutePath(), "-o", oFile.getAbsolutePath(), "-t", "ASIFTENRICHED_BINARY", "-k", "5", "-d", "1", kFile
				.getAbsolutePath() };
		System.out.println(Arrays.toString(args));
		final ClusterQuantiserOptions options = new ClusterQuantiserOptions(args);
		options.prepare();
		ClusterQuantiser.do_quant(options);
	}
}
