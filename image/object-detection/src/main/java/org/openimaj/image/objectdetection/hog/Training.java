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
package org.openimaj.image.objectdetection.hog;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.data.RandomData;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.feature.DatasetExtractors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.dense.gradient.HOG;
import org.openimaj.image.feature.dense.gradient.binning.FlexibleHOGStrategy;
import org.openimaj.image.objectdetection.datasets.INRIAPersonDataset;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.ConcatenatedList;
import org.openimaj.util.pair.IntObjectPair;

import de.bwaldvogel.liblinear.SolverType;

public class Training {
	static class Extractor implements FeatureExtractor<DoubleFV, FImage> {
		HOGClassifier hogClassifier;

		Extractor(HOGClassifier hogClassifier) {
			this.hogClassifier = hogClassifier;
		}

		@Override
		public DoubleFV extractFeature(FImage image) {
			final int offsetX = (image.width - 64) / 2;
			final int offsetY = (image.height - 128) / 2;
			hogClassifier.hogExtractor.analyseImage(image);

			final Histogram f = hogClassifier.hogExtractor.getFeatureVector(new Rectangle(offsetX,
					offsetY, 64, 128));

			return f;
		}
	}

	public static void main(String[] args) throws IOException {
		final HOGClassifier hogClassifier = new HOGClassifier();
		hogClassifier.width = 64;
		hogClassifier.height = 128;

		final FlexibleHOGStrategy strategy = new FlexibleHOGStrategy(8, 16, 2);
		hogClassifier.hogExtractor = new HOG(9, false, FImageGradients.Mode.Unsigned, strategy);

		final GroupedDataset<Boolean, ListDataset<FImage>, FImage> trainingImages = INRIAPersonDataset.getTrainingData();
		final GroupedDataset<Boolean, ListDataset<DoubleFV>, DoubleFV> trainingData = DatasetExtractors
				.createLazyFeatureDataset(trainingImages, new Extractor(hogClassifier));

		LiblinearAnnotator<DoubleFV, Boolean> ann = new LiblinearAnnotator<DoubleFV, Boolean>(
				new IdentityFeatureExtractor<DoubleFV>(), Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 0.01, 0.01, 1, true);
		ann.train(trainingData);
		hogClassifier.classifier = ann;

		IOUtils.writeToFile(hogClassifier, new File("initial-classifier.dat"));

		final HOGDetector detector = new HOGDetector(hogClassifier, 1.2f);

		final ListDataset<FImage> negImages =
				INRIAPersonDataset.getNegativeTrainingImages(ImageUtilities.FIMAGE_READER);
		final List<IntObjectPair<Rectangle>> extraNegatives = new
				ArrayList<IntObjectPair<Rectangle>>();
		for (int i = 0; i < negImages.numInstances(); i++) {
			final FImage image = negImages.get(i);

			final List<Rectangle> rects = detector.detect(image);
			if (rects != null) {
				for (final Rectangle r : rects) {
					extraNegatives.add(new IntObjectPair<Rectangle>(i, r));
				}
			}
		}

		List<FImage> hardExamples = new AbstractList<FImage>() {

			int lastImageId = -1;
			FImage lastImage;

			@Override
			public FImage get(int index) {
				final IntObjectPair<Rectangle> p = extraNegatives.get(index);

				if (p.first != lastImageId) {
					lastImageId = p.first;
					lastImage = negImages.get(p.first);
				}

				return lastImage.extractROI(p.second);
			}

			@Override
			public int size() {
				return extraNegatives.size();
			}
		};

		final int[] indices = RandomData.getUniqueRandomInts(2000, 0,
				hardExamples.size());
		Arrays.sort(indices);
		hardExamples = new AcceptingListView<FImage>(hardExamples, indices);

		final List<FImage> extendedNegatives = new
				ConcatenatedList<FImage>(trainingImages.get(false), hardExamples);
		final GroupedDataset<Boolean, ListDataset<FImage>, FImage> extendedTrainingImages = new MapBackedDataset<Boolean,
				ListDataset<FImage>, FImage>();
		extendedTrainingImages.put(true, trainingImages.get(true));
		extendedTrainingImages.put(false, new
				ListBackedDataset<FImage>(extendedNegatives));

		final GroupedDataset<Boolean, ListDataset<DoubleFV>, DoubleFV> extendedTrainingData = DatasetExtractors
				.createLazyFeatureDataset(extendedTrainingImages, new
						Extractor(hogClassifier));

		ann = new LiblinearAnnotator<DoubleFV, Boolean>(
				new IdentityFeatureExtractor<DoubleFV>(), Mode.MULTICLASS,
				SolverType.L2R_L2LOSS_SVC, 0.01, 0.01, 1, true);
		ann.train(extendedTrainingData);
		hogClassifier.classifier = ann;

		int c = 0, p = 0;
		for (final FImage i : INRIAPersonDataset.getPositiveTrainingImages(ImageUtilities.FIMAGE_READER)) {
			hogClassifier.prepare(i);

			final int offsetX = (i.width - 64) / 2;
			final int offsetY = (i.height - 128) / 2;

			p += hogClassifier.classify(new Rectangle(offsetX, offsetY, 64, 128)) > 0.5 ? 1 : 0;
			c++;
		}
		System.out.println(p + "/" + c);

		IOUtils.writeToFile(hogClassifier, new File("final-classifier.dat"));
	}
}
