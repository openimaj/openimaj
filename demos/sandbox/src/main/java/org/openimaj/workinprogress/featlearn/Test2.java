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
package org.openimaj.workinprogress.featlearn;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.CIFAR10Dataset;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.matrix.algorithm.whitening.ZCAWhitening;
import org.openimaj.math.statistics.normalisation.PerExampleMeanCenter;
import org.openimaj.ml.clustering.kmeans.SphericalKMeans;
import org.openimaj.ml.clustering.kmeans.SphericalKMeansResult;

public class Test2 {
	public static void main(String[] args) throws IOException {
		System.out.println("start");
		final RandomPatchSampler<MBFImage> sampler = new RandomPatchSampler<MBFImage>(
				CIFAR10Dataset.getTrainingImages(CIFAR10Dataset.MBFIMAGE_READER),
				8, 8, 400000);
		final List<MBFImage> patches = sampler.getPatches();
		System.out.println("stop");

		final double[][] data = new double[patches.size()][];
		for (int i = 0; i < data.length; i++)
			data[i] = patches.get(i).getDoublePixelVector();

		// final PCAWhitening whitening = new PCAWhitening();
		final ZCAWhitening whitening = new ZCAWhitening(0.1, new PerExampleMeanCenter());
		whitening.train(data);
		final double[][] wd = whitening.whiten(data);

		final SphericalKMeans skm = new SphericalKMeans(1600, 10);
		final SphericalKMeansResult res = skm.cluster(wd);
		final MBFImage tmp = new MBFImage(40 * (8 + 1) + 1, 40 * (8 + 1) + 1);
		tmp.fill(RGBColour.WHITE);
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				final MBFImage patch = new MBFImage(res.centroids[i * 40 + j], 8, 8, 3, false);
				tmp.drawImage(patch, i * (8 + 1) + 1, j * (8 + 1) + 1);
			}
		}
		tmp.subtractInplace(-1.5f);
		tmp.divideInplace(3f);
		DisplayUtilities.display(tmp);
	}
}
