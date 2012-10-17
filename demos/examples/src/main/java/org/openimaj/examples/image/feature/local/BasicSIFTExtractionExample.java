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
package org.openimaj.examples.image.feature.local;

import java.io.IOException;

import org.openimaj.OpenIMAJ;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;

/**
 * Example showing how to extract SIFT features from an image and write them to
 * {@link System#out} in a format compatible with David Lowe's <a
 * href="http://www.cs.ubc.ca/~lowe/keypoints/">keypoint</a> tool.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BasicSIFTExtractionExample {
	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 *             if the image can't be read
	 */
	public static void main(String[] args) throws IOException {
		// read an image
		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		// create the extractor - this can be reused (and is thread-safe)
		final DoGSIFTEngine engine = new DoGSIFTEngine();

		// find interest points in the image and extract SIFT descriptors
		final LocalFeatureList<Keypoint> keypoints = engine.findFeatures(image);

		// print the interest points to stdout
		IOUtils.writeASCII(System.out, keypoints);
	}
}
