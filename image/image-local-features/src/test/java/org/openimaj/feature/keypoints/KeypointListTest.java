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
package org.openimaj.feature.keypoints;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.local.list.StreamLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;

/**
 * Test a list of keypoints
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KeypointListTest {
	/**
	 * Temp folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	DoGSIFTEngine engine;
	FImage im;
	LocalFeatureList<Keypoint> keys;

	/**
	 * Load a single image and find its keypoints
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		engine = new DoGSIFTEngine();
		im = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
		keys = engine.findFeatures(im);
	}

	/**
	 * Test the ASIFT keypoint engine, write the keypoints and load them, are
	 * they identical
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAffineSimulationKeypointList() throws IOException {
		final ASIFTEngine engine = new ASIFTEngine();
		final File binary = folder.newFile("kpt-testAffineSimulationKeypointList.tmp");

		final LocalFeatureList<AffineSimulationKeypoint> allKeys = engine.findFeatures(im);
		IOUtils.writeBinary(binary, allKeys);

		final LocalFeatureList<AffineSimulationKeypoint> fklA = FileLocalFeatureList.read(binary,
				AffineSimulationKeypoint.class);

		for (int i = 0; i < fklA.size(); i++) {
			assertTrue(Arrays.equals(fklA.get(i).ivec, allKeys.get(i).ivec));
			final AffineSimulationKeypoint first = fklA.get(i);
			final AffineSimulationKeypoint second = allKeys.get(i);
			assertTrue(Math.abs(first.x - second.x) < 0.01);
			assertTrue(Math.abs(first.y - second.y) < 0.01);
			assertTrue(Math.abs(first.affineParams.theta - second.affineParams.theta) < 0.01);
			assertTrue(Math.abs(first.affineParams.tilt - second.affineParams.tilt) < 0.01);
		}
	}

	/**
	 * ead and write the keypoints from an image into binary and ascii
	 * 
	 * @throws IOException
	 */
	@Test
	public void io_test() throws IOException {
		final File ascii = folder.newFile("kpt.ascii");
		IOUtils.writeASCII(ascii, keys);

		final File binary = folder.newFile("kpt.bin");
		IOUtils.writeBinary(binary, keys);

		final LocalFeatureList<Keypoint> fklA = FileLocalFeatureList.read(ascii, Keypoint.class);

		final LocalFeatureList<Keypoint> fklB = FileLocalFeatureList.read(binary, Keypoint.class);
		final File ascii2 = folder.newFile("kpt.tmp");
		IOUtils.writeASCII(ascii2, fklB);

		final LocalFeatureList<Keypoint> fklBA = FileLocalFeatureList.read(ascii, Keypoint.class);

		assertEquals(fklA, fklBA);

		ascii.delete();
		ascii2.delete();
		binary.delete();
	}

	/**
	 * See if sublists work (as well as saving sublists)
	 * 
	 * @throws IOException
	 */
	@Test
	public void subListTest() throws IOException {
		final File binary = folder.newFile("kpt-subListTest.tmp");
		IOUtils.writeBinary(binary, keys);

		final LocalFeatureList<Keypoint> fklB = FileLocalFeatureList.read(binary, Keypoint.class);

		final List<Keypoint> sl1 = keys.subList(2, 4);
		final List<Keypoint> sl2 = fklB.subList(2, 4);

		assertArrayEquals(sl1.toArray(), sl2.toArray());

		assertEquals(sl1, sl2);
		assertArrayEquals(sl1.toArray(), sl2.toArray());

		final List<Keypoint> sl3 = sl1.subList(0, 1);
		final List<Keypoint> sl4 = sl2.subList(0, 1);

		assertEquals(sl3, sl4);
		assertArrayEquals(sl1.subList(1, 2).toArray(), sl2.subList(1, 2).toArray());

		binary.delete();
	}

	/**
	 * Another ublist test
	 * 
	 * @throws IOException
	 */
	@Test
	public void subListTest2() throws IOException {
		final File ascii = folder.newFile("kpt-subListTest2.tmp");
		IOUtils.writeASCII(ascii, keys);

		final LocalFeatureList<Keypoint> kl = MemoryLocalFeatureList.read(ascii, Keypoint.class);

		final LocalFeatureList<Keypoint> fklB = FileLocalFeatureList.read(ascii, Keypoint.class);

		final List<Keypoint> sl1 = kl.subList(2, 4);
		final List<Keypoint> sl2 = fklB.subList(2, 4);

		assertEquals(sl1, sl2);
		assertArrayEquals(sl1.toArray(), sl2.toArray());

		final List<Keypoint> sl3 = sl1.subList(0, 1);
		final List<Keypoint> sl4 = sl2.subList(0, 1);

		assertEquals(sl3, sl4);
		assertArrayEquals(sl1.subList(1, 2).toArray(), sl2.subList(1, 2).toArray());

		ascii.delete();
	}

	/**
	 * Selecting random keypoints and random sublists
	 * 
	 * @throws IOException
	 */
	@Test
	public void randomSubListTest() throws IOException {
		final File binary = folder.newFile("kpt-randomSubListTest.bin");
		IOUtils.writeBinary(binary, keys);

		final File ascii = folder.newFile("kpt-randomSubListTest2.ascii");
		IOUtils.writeASCII(ascii, keys);

		final LocalFeatureList<Keypoint> fklB = FileLocalFeatureList.read(binary, Keypoint.class);
		final LocalFeatureList<Keypoint> fklA = FileLocalFeatureList.read(ascii, Keypoint.class);

		final List<Keypoint> sl1 = keys.randomSubList(3);
		final List<Keypoint> sl2 = fklB.randomSubList(3);
		final List<Keypoint> sl3 = fklA.randomSubList(3);

		assertEquals(sl1.size(), 3);
		assertEquals(sl2.size(), 3);
		assertEquals(sl3.size(), 3);

		assertEquals(sl1.toArray().length, 3);
		assertEquals(sl2.toArray().length, 3);
		assertEquals(sl3.toArray().length, 3);

		assertEquals(sl1.subList(0, 1).size(), 1);
		assertEquals(sl2.subList(0, 1).size(), 1);
		assertEquals(sl3.subList(0, 1).size(), 1);

		// List<Keypoint> allRandom = fklB.randomSubList(fklB.size());
		// assertEquals(allRandom.size(),fklB.size());

		binary.delete();
		ascii.delete();
	}

	/**
	 * Allow the streaming of keypoints from a file
	 * 
	 * @throws IOException
	 */
	@Test
	public void streamTest() throws IOException {
		final File binary = folder.newFile("kpt.tmp");
		IOUtils.writeBinary(binary, keys);

		FileInputStream fis = new FileInputStream(binary);
		LocalFeatureList<Keypoint> kl = StreamLocalFeatureList.read(fis, Keypoint.class);

		assertEquals(keys.size(), kl.size());
		assertEquals(keys, kl);

		binary.delete();

		final File ascii = folder.newFile("kpt2.tmp");
		fis = new FileInputStream(ascii);
		IOUtils.writeASCII(ascii, keys);
		kl = StreamLocalFeatureList.read(fis, Keypoint.class);
		// MemoryLocalFeatureList<Keypoint> memKL = new
		// MemoryLocalFeatureList<Keypoint>(kl);
		final MemoryLocalFeatureList<Keypoint> memKL2 = MemoryLocalFeatureList.read(ascii, Keypoint.class);
		assertEquals(memKL2.size(), kl.size());
		assertEquals(memKL2, kl);

		ascii.delete();
	}
}
