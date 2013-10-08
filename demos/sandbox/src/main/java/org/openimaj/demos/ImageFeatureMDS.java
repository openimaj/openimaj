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
package org.openimaj.demos;

import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.MultidimensionalScaling;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.FlickrAPIToken;
import org.openimaj.util.pair.IndependentPair;

public class ImageFeatureMDS {
	public static void main(String[] args) throws Exception {
		final FlickrAPIToken token = DefaultTokenFactory.getInstance().getToken(FlickrAPIToken.class);
		final int numImages = 20;

		final FlickrImageDataset<MBFImage> dataset = FlickrImageDataset.create(ImageUtilities.MBFIMAGE_READER, token,
				"colorful", numImages);

		dataset.getPhotos().set(1, dataset.getPhoto(0));

		final DoubleFV[] features = new DoubleFV[numImages];
		for (int i = 0; i < numImages; i++) {
			features[i] = extractFeature(dataset.get(i));
		}

		final SimilarityMatrix matrix = new SimilarityMatrix(numImages);
		for (int i = 0; i < numImages; i++) {
			matrix.setIndexValue(i, dataset.getID(i));
			final DoubleFV fi = features[i];

			for (int j = 0; j < numImages; j++) {
				final DoubleFV fj = features[j];

				matrix.set(i, j, fi.compare(fj, DoubleFVComparison.COSINE_SIM));
			}
		}

		System.out.println(matrix);

		final MultidimensionalScaling mds = new MultidimensionalScaling();
		mds.process(matrix);
		System.out.println(mds.getPoints());

		final MBFImage img = new MBFImage(1000, 1000, ColourSpace.RGB);
		for (final IndependentPair<String, Point2d> pt : mds.getPoints()) {
			// img.drawPoint(pt.getSecondObject(), RGBColour.RED, 3);

			final int idx = dataset.indexOfID(pt.firstObject());
			final MBFImage thumb = ImageUtilities.readMBF(new URL(dataset.getPhoto(idx).getThumbnailUrl()));
			img.drawImage(thumb, pt.getSecondObject().transform(TransformUtilities.scaleMatrix(1000, 1000)));
		}
		DisplayUtilities.display(img);
	}

	static DoubleFV extractFeature(MBFImage image) {
		final HistogramModel model = new HistogramModel(4, 4, 4);

		model.estimateModel(image);

		return model.histogram.normaliseFV();
	}
}
