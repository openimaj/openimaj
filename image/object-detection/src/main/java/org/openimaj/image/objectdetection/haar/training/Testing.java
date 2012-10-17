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
package org.openimaj.image.objectdetection.haar.training;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;
import org.openimaj.image.objectdetection.haar.HaarFeature;
import org.openimaj.image.objectdetection.haar.training.StumpClassifier.WeightedLearner;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.pair.ObjectFloatPair;

public class Testing {
	List<HaarFeature> features;
	List<SummedSqTiltAreaTable> positive = new ArrayList<SummedSqTiltAreaTable>();
	List<SummedSqTiltAreaTable> negative = new ArrayList<SummedSqTiltAreaTable>();

	void createFeatures() {
		features = HaarFeatureType.generateFeatures(20, 20, HaarFeatureType.BASIC);

		final float invArea = 1f / (18 * 18);
		for (final HaarFeature f : features) {
			f.setScale(1, invArea);
		}
	}

	void loadPositive() throws IOException {
		final String base = "/Users/jsh2/Data/att_faces/s%d/%d.pgm";

		for (int j = 1; j <= 40; j++) {
			for (int i = 1; i <= 10; i++) {
				final File file = new File(String.format(base, j, i));

				FImage img = ImageUtilities.readF(file);
				img = img.extractCenter(70, 70);
				img = ResizeProcessor.resample(img, 20, 20);
				DisplayUtilities.displayName(img, "face");
				positive.add(new SummedSqTiltAreaTable(img, true));
			}
		}
	}

	void loadNegative() throws IOException {
		final File dir = new File(
				"/Volumes/Raid/face_databases/haartraining/tutorial-haartraining.googlecode.com/svn/trunk/data/negatives/");

		for (final File f : dir.listFiles()) {
			if (f.getName().endsWith(".jpg")) {
				FImage img = ImageUtilities.readF(f);

				final int minwh = Math.min(img.width, img.height);

				img = img.extractCenter(minwh, minwh);
				img = ResizeProcessor.resample(img, 20, 20);

				negative.add(new SummedSqTiltAreaTable(img, true));
			}
		}
	}

	void perform() throws IOException {
		createFeatures();
		loadPositive();
		loadNegative();

		System.out.println("+ve: " + positive.size());
		System.out.println("-ve: " + negative.size());
		System.out.println("features: " + features.size());

		final CachedTrainingData data = new CachedTrainingData(positive,
				negative, features);
		final WeightedLearner wl = new StumpClassifier.WeightedLearner();
		final float[] weights = new float[data.numInstances()];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1f / weights.length;
		}

		final ObjectFloatPair<StumpClassifier> c = wl.learn(data, weights);

		System.out.println(c.second);
		System.out.println(c.first.dimension);
		System.out.println(c.first.sign);
		System.out.println(c.first.threshold);

		final HaarFeature feature = features.get(c.first.dimension);

		int posCorrect = 0;
		int posIncorrect = 0;
		for (final SummedSqTiltAreaTable sat : positive) {
			final float response = feature.computeResponse(sat, 0, 0);

			if (response > c.first.threshold)
				posCorrect++;
			else
				posIncorrect++;
		}

		int negCorrect = 0;
		int negIncorrect = 0;
		for (final SummedSqTiltAreaTable sat : negative) {
			final float response = feature.computeResponse(sat, 0, 0);

			if (response < c.first.threshold)
				negCorrect++;
			else
				negIncorrect++;
		}

		System.out.println(posCorrect);
		System.out.println(posIncorrect);
		System.out.println(negCorrect);
		System.out.println(negIncorrect);
	}

	public static void main(String[] args) throws IOException {
		new Testing().perform();
	}
}
