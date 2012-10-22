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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;
import org.openimaj.image.objectdetection.haar.HaarFeature;
import org.openimaj.image.objectdetection.haar.HaarFeatureClassifier;
import org.openimaj.image.objectdetection.haar.Stage;
import org.openimaj.image.objectdetection.haar.StageTreeClassifier;
import org.openimaj.image.objectdetection.haar.ValueClassifier;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.ObjectFloatPair;

public class Testing {
	List<HaarFeature> features;
	List<SummedSqTiltAreaTable> positive = new ArrayList<SummedSqTiltAreaTable>();
	List<SummedSqTiltAreaTable> negative = new ArrayList<SummedSqTiltAreaTable>();

	void createFeatures(int width, int height) {
		features = HaarFeatureType.generateFeatures(width, height, HaarFeatureType.CORE);

		final float invArea = 1f / ((width - 2) * (height - 2));
		for (final HaarFeature f : features) {
			f.setScale(1, invArea);
		}
	}

	// void loadPositive(boolean tilted) throws IOException {
	// final String base = "/Users/jsh2/Data/att_faces/s%d/%d.pgm";
	//
	// for (int j = 1; j <= 40; j++) {
	// for (int i = 1; i <= 10; i++) {
	// final File file = new File(String.format(base, j, i));
	//
	// FImage img = ImageUtilities.readF(file);
	// img = img.extractCenter(50, 50);
	// img = ResizeProcessor.resample(img, 19, 19);
	// positive.add(new SummedSqTiltAreaTable(img, tilted));
	// }
	// }
	// }
	//
	// void loadNegative(boolean tilted) throws IOException {
	// final File dir = new File(
	// "/Volumes/Raid/face_databases/haartraining/tutorial-haartraining.googlecode.com/svn/trunk/data/negatives/");
	//
	// for (final File f : dir.listFiles()) {
	// if (f.getName().endsWith(".jpg")) {
	// FImage img = ImageUtilities.readF(f);
	//
	// final int minwh = Math.min(img.width, img.height);
	//
	// img = img.extractCenter(minwh, minwh);
	// img = ResizeProcessor.resample(img, 19, 19);
	//
	// negative.add(new SummedSqTiltAreaTable(img, tilted));
	// }
	// }
	// }

	void loadImage(File image, List<SummedSqTiltAreaTable> sats, boolean
			tilted) throws IOException
	{
		final FImage img = ImageUtilities.readF(image);

		sats.add(new SummedSqTiltAreaTable(img, false));
	}

	void loadPositive(boolean tilted) throws IOException {
		for (final File file : new File("/Volumes/Raid/face_databases/cbcl-faces/train/face").listFiles()) {
			if (file.getName().endsWith(".pgm")) {
				loadImage(file, positive, tilted);
			}
		}
	}

	void loadNegative(boolean tilted) throws IOException {
		for (final File file : new File("/Volumes/Raid/face_databases/cbcl-faces/train/non-face").listFiles()) {
			if (file.getName().endsWith(".pgm")) {
				loadImage(file, negative, tilted);
			}
		}
	}

	void perform() throws IOException {
		System.out.println("Creating feature set");
		createFeatures(19, 19);

		System.out.println("Loading positive images and computing SATs");
		loadPositive(false);

		System.out.println("Loading negative images and computing SATs");
		loadNegative(false);

		System.out.println("+ve: " + positive.size());
		System.out.println("-ve: " + negative.size());
		System.out.println("features: " + features.size());

		System.out.println("Computing cached feature sets");
		final CachedTrainingData data = new CachedTrainingData(positive, negative, features);

		System.out.println("Starting Training");
		final AdaBoost boost = new AdaBoost();
		final List<ObjectFloatPair<StumpClassifier>> ensemble = boost.learn(data, 100);

		System.out.println("Training complete. Ensemble has " + ensemble.size() + " classifiers.");

		for (float threshold = 3; threshold >= -3; threshold -= 0.1f) {
			System.out.println("Threshold = " + threshold);
			boost.printClassificationQuality(data, ensemble, threshold);
		}

		final HaarFeatureClassifier[] trees = new HaarFeatureClassifier[ensemble.size()];

		for (int i = 0; i < trees.length; i++) {
			final ObjectFloatPair<StumpClassifier> wc = ensemble.get(i);
			final StumpClassifier c = wc.first;
			final float alpha = wc.second;
			final float threshold = c.threshold;
			final float leftValue = c.sign > 0 ? -alpha : alpha; // right way
																	// around???
			final HaarFeature feature = features.get(c.dimension);

			final ValueClassifier left = new ValueClassifier(leftValue);
			final ValueClassifier right = new ValueClassifier(-leftValue);

			trees[i] = new HaarFeatureClassifier(feature, threshold, left, right);
		}

		final Stage root = new Stage(0, trees, null, null);
		final StageTreeClassifier classifier = new StageTreeClassifier(19, 19, "test cascade", false, root);

		final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("test-classifier.bin")));
		IOUtils.write(classifier, oos);
		oos.close();
	}

	public static void main(String[] args) throws IOException {
		new Testing().perform();
	}
}
