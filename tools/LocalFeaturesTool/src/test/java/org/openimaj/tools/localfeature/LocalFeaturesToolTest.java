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
package org.openimaj.tools.localfeature;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Tests for the LocalFeaturesTool
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class LocalFeaturesToolTest {
	/**
	 * Temporary folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File tmpImageFile;
	private File tmpNormImageFile;
	private MBFImage loaded;
	private MBFImage normalised;

	/**
	 * Setup tests
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		final InputStream is = this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg");
		tmpImageFile = folder.newFile("cat.jpg");
		tmpNormImageFile = folder.newFile("catIntensityNormalised.jpg");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tmpImageFile);
			final byte[] arr = new byte[1024];
			int read = is.read(arr);
			while (read != -1) {
				try {
					fos.write(arr, 0, read);
					read = is.read(arr);
				} catch (final Exception e) {
					System.out.println(e);
				}
			}
		} finally {
			fos.close();
		}

		loaded = ImageUtilities.readMBF(tmpImageFile);
		normalised = Transforms.RGB_TO_RGB_NORMALISED(loaded);

		ImageUtilities.write(loaded, "jpg", tmpImageFile);
		ImageUtilities.write(normalised.getBand(1), "jpg", tmpNormImageFile);
		System.out.println("Image out: " + tmpImageFile);
		System.out.println("Normalised Image out: " + tmpNormImageFile);
	}

	/**
	 * Test that DoG keypoints can be created
	 * 
	 * @throws IOException
	 */
	@Test
	public void testKeypointGeneration() throws IOException {
		final File tmpKeypointFile = folder.newFile("keypoint-testKeypointGeneration.key");
		final File tmpASCIIKeypointFile = folder.newFile("keypoint-testKeypointGeneration2.key");
		String[] args = null;
		args = new String[] {
				"-i", tmpImageFile.getAbsolutePath(),
				"-o", tmpKeypointFile.getAbsolutePath()
		};
		Extractor.main(args);

		args = new String[] {
				"-a",
				"-i", tmpImageFile.getAbsolutePath(),
				"-o", tmpASCIIKeypointFile.getAbsolutePath()
		};
		Extractor.main(args);

		final List<Keypoint> binary = MemoryLocalFeatureList.read(tmpKeypointFile, Keypoint.class);
		final List<Keypoint> ascii = MemoryLocalFeatureList.read(tmpASCIIKeypointFile, Keypoint.class);
		for (int i = 0; i < binary.size(); i++) {
			assertTrue(Arrays.equals(binary.get(i).ivec, ascii.get(i).ivec));
		}
	}

	/**
	 * Test transform
	 * 
	 * @throws IOException
	 */
	@Test
	public void testKeypointImageTransform() throws IOException {
		final File tmpKeypointFile = folder.newFile("keypoint-testKeypointImageTransform.key");
		final File tmpResizedKeypointFile = folder.newFile("keypointResized-testKeypointImageTransform.key");
		tmpResizedKeypointFile.delete();

		String[] args = null;
		args = new String[] {
				"-i", tmpImageFile.getAbsolutePath(),
				"-o", tmpKeypointFile.getAbsolutePath()
		};
		Extractor.main(args);

		args = new String[] {
				"-i", tmpImageFile.getAbsolutePath(),
				"-o", tmpResizedKeypointFile.getAbsolutePath(),
				"-a",
				"-m", "SIFT",
				"-it", "RESIZE_MAX",
				"-dmax", "1024"
		};
		Extractor.main(args);

		assertTrue(tmpResizedKeypointFile.exists());
	}
}
