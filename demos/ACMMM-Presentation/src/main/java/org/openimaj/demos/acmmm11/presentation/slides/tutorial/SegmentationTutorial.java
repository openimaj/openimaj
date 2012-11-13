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
package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactFloatAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.video.Video;

/**
 * Slide showing segmentation using k-means clustering
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */

public class SegmentationTutorial extends TutorialPanel {
	private static final long serialVersionUID = 1L;

	private FloatCentroidsResult cluster;

	/**
	 * Default constructor
	 * 
	 * @param capture
	 * @param width
	 * @param height
	 */
	public SegmentationTutorial(Video<MBFImage> capture, int width, int height) {
		super("Posterisation with K-Means", capture, width, height);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		final MBFImage space = ColourSpace.convert(toDraw, ColourSpace.CIE_Lab);

		if (cluster == null)
			cluster = clusterPixels(space);

		if (cluster == null)
			return;

		final float[][] centroids = cluster.getCentroids();

		final ExactFloatAssigner assigner = new ExactFloatAssigner(cluster);

		for (int y = 0; y < space.getHeight(); y++) {
			for (int x = 0; x < space.getWidth(); x++) {
				final float[] pixel = space.getPixelNative(x, y);
				final int centroid = assigner.assign(pixel);
				space.setPixelNative(x, y, centroids[centroid]);
			}
		}

		toDraw.internalAssign(ColourSpace.convert(space, ColourSpace.RGB));
	}

	private FloatCentroidsResult clusterPixels(MBFImage toDraw) {
		final float[][] testP = toDraw.getBand(0).pixels;
		float sum = 0;

		for (int i = 0; i < testP.length; i++)
			for (int j = 0; j < testP[i].length; j++)
				sum += testP[i][j];

		if (sum == 0)
			return null;

		final FloatKMeans k = FloatKMeans.createExact(3, 2);
		final float[][] imageData = toDraw.getPixelVectorNative(new float[toDraw.getWidth() * toDraw.getHeight() * 3][3]);

		return k.cluster(imageData);
	}
}
