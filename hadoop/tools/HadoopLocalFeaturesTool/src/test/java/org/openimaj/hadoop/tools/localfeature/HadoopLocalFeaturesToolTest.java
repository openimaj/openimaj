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
package org.openimaj.hadoop.tools.localfeature;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Tests for the {@link HadoopLocalFeaturesTool}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HadoopLocalFeaturesToolTest {
	/**
	 * Temp folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File imageSeqFile;
	private ArrayList<Text> keys;

	/**
	 * Setup
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		imageSeqFile = folder.newFile("seq.images");
		final TextBytesSequenceFileUtility tbsfu = new TextBytesSequenceFileUtility(imageSeqFile.getAbsolutePath(), false);
		final InputStream[] inputs = new InputStream[] {
				this.getClass().getResourceAsStream("ukbench00000.jpg"),
				this.getClass().getResourceAsStream("ukbench00001.jpg"),
		};

		Text key;
		keys = new ArrayList<Text>();
		for (final InputStream input : inputs) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copyBytes(input, baos, new Configuration(), false);
			final BytesWritable bytesWriteable = new BytesWritable(baos.toByteArray());
			key = new Text(UUID.randomUUID().toString());

			keys.add(key);
			tbsfu.appendData(key, bytesWriteable);
		}
		tbsfu.close();
	}

	/**
	 * Test keypoint generation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testKeypointGeneration() throws Exception {
		// imageSeqFile.delete();
		final File featureSeqFile = folder.newFile("seq-testKeypointGeneration.features");
		final File featureSeqFileASCII = folder.newFile("seq-testKeypointGeneration.featuresASCII");
		// File featureSeqFileColourASCII = File.createTempFile("seq",
		// "featuresColourASCII");
		// File featureSeqFileColour = File.createTempFile("seq",
		// "featuresColour");
		final File featureSeqFileASIFT = folder.newFile("seq-testKeypointGeneration.featuresASIFT");
		featureSeqFile.delete();
		featureSeqFileASCII.delete();
		featureSeqFileASIFT.delete();
		// featureSeqFileColourASCII.delete();
		// featureSeqFileColour.delete();
		// File codebookFile = File.createTempFile("codebook", "temp");
		final File quantisedOutput = folder.newFile("seq-testKeypointGeneration.quantised");
		quantisedOutput.delete();

		HadoopLocalFeaturesTool.main(new String[] { "-D", "mapred.child.java.opts=\"-Xmx3000M\"", "-i",
				imageSeqFile.getAbsolutePath(), "-o", featureSeqFile.getAbsolutePath() });
		HadoopLocalFeaturesTool.main(new String[] { "-D", "mapred.child.java.opts=\"-Xmx3000M\"", "-i",
				imageSeqFile.getAbsolutePath(), "-o", featureSeqFileASCII.getAbsolutePath(), "-a" });
		// HadoopJKeypointsTool.main(new
		// String[]{"-D","mapred.child.java.opts=\"-Xmx3000M\"","-i",imageSeqFile.getAbsolutePath(),"-o",featureSeqFileASIFT.getAbsolutePath(),"-m","ASIFT"});
		// HadoopLocalFeaturesTool.main(new
		// String[]{"-D","mapred.child.java.opts=\"-Xmx3000M\"","-i",imageSeqFile.getAbsolutePath(),"-o",featureSeqFileColourASCII.getAbsolutePath(),"-a","-m","SIFT","-cm","INTENSITY_COLOUR"});
		// HadoopLocalFeaturesTool.main(new
		// String[]{"-D","mapred.child.java.opts=\"-Xmx3000M\"","-i",imageSeqFile.getAbsolutePath(),"-o",featureSeqFileColour.getAbsolutePath(),"-m","SIFT","-cm","INTENSITY_COLOUR"});

		assertTrue(featureSeqFile.exists());
		assertTrue(featureSeqFileASCII.exists());
		// assertTrue(featureSeqFileASIFT.exists());

		final LocalFeatureList<Keypoint> firstKPL = getKPLFromSequence(keys.get(0), featureSeqFile);
		final LocalFeatureList<Keypoint> secondKPL = getKPLFromSequence(keys.get(0), featureSeqFileASCII);

		assertTrue(firstKPL.size() == secondKPL.size());
		for (int i = 0; i < firstKPL.size(); i++) {
			assertTrue(Arrays.equals(firstKPL.get(i).ivec, secondKPL.get(i).ivec));
		}

		// LocalFeatureList<Keypoint> firstColourKPL =
		// getKPLFromSequence(keys.get(0),featureSeqFileColour);
		// LocalFeatureList<Keypoint> secondColourKPL =
		// getKPLFromSequence(keys.get(0),featureSeqFileColourASCII);

		// assertTrue(firstColourKPL.size() == secondColourKPL.size());
		// for(int i = 0; i < firstColourKPL.size(); i++){
		// assertTrue(Arrays.equals(firstColourKPL.get(i).ivec,secondColourKPL.get(i).ivec));
		// }

	}

	private LocalFeatureList<Keypoint> getKPLFromSequence(Text text, File featureSeqFile) throws IOException {
		final File keyOut = folder.newFile("out" + text.hashCode() + "-" + featureSeqFile.hashCode() + "Directory");
		keyOut.delete();
		final SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(
				SequenceFileUtility.getFilePaths(featureSeqFile.getAbsolutePath(), "part")[0].toUri(), true);
		utility.findAndExport(text, keyOut.toString(), 0);
		return MemoryLocalFeatureList.read(new File(keyOut.toString(), text.toString()), Keypoint.class);
	}

	/**
	 * Test image transform
	 * 
	 * @throws Exception
	 */
	@Test
	public void testKeypointImageTransform() throws Exception {
		final File featureSeqFile = folder.newFile("seq-testKeypointImageTransform.features");
		final File featureSeqFileDouble = folder.newFile("seq-testKeypointImageTransform2.features");

		HadoopLocalFeaturesTool.main(new String[] { "-D", "mapred.child.java.opts=\"-Xmx3000M\"", "-i",
				imageSeqFile.getAbsolutePath(), "-o", featureSeqFileDouble.getAbsolutePath(), "-m", "ASIFTENRICHED",
				"-rm" });
		HadoopLocalFeaturesTool.main(new String[] { "-D", "mapred.child.java.opts=\"-Xmx3000M\"", "-i",
				imageSeqFile.getAbsolutePath(), "-o", featureSeqFile.getAbsolutePath(), "-m", "ASIFTENRICHED", "-rm",
				"-nds" });

		final LocalFeatureList<Keypoint> firstKPL = getKPLFromSequence(keys.get(0), featureSeqFile);
		final LocalFeatureList<Keypoint> firstKPLDouble = getKPLFromSequence(keys.get(0), featureSeqFileDouble);
		assertTrue(firstKPL.size() < firstKPLDouble.size());
	}
}
