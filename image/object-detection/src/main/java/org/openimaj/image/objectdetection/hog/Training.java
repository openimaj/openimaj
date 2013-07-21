package org.openimaj.image.objectdetection.hog;

import java.io.File;
import java.io.IOException;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
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

		final LiblinearAnnotator<DoubleFV, Boolean> ann = new LiblinearAnnotator<DoubleFV, Boolean>(
				new IdentityFeatureExtractor<DoubleFV>(), Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 100, 0.01, true);
		ann.train(trainingData);
		hogClassifier.classifier = ann;

		IOUtils.writeToFile(hogClassifier, new File("initial-classifier.dat"));

		// final HOGDetector detector = new HOGDetector(hogClassifier, 1.2f);
		//
		// final ListDataset<FImage> negImages =
		// INRIAPersonDataset.getNegativeTrainingImages(ImageUtilities.FIMAGE_READER);
		// final List<IntObjectPair<Rectangle>> extraNegatives = new
		// ArrayList<IntObjectPair<Rectangle>>();
		// for (int i = 0; i < negImages.numInstances(); i++) {
		// final FImage image = negImages.get(i);
		//
		// final List<Rectangle> rects = detector.detect(image);
		// if (rects != null) {
		// for (final Rectangle r : rects) {
		// extraNegatives.add(new IntObjectPair<Rectangle>(i, r));
		// }
		// }
		// }
		//
		// List<FImage> hardExamples = new AbstractList<FImage>() {
		//
		// int lastImageId = -1;
		// FImage lastImage;
		//
		// @Override
		// public FImage get(int index) {
		// final IntObjectPair<Rectangle> p = extraNegatives.get(index);
		//
		// if (p.first != lastImageId) {
		// lastImageId = p.first;
		// lastImage = negImages.get(p.first);
		// }
		//
		// return lastImage.extractROI(p.second);
		// }
		//
		// @Override
		// public int size() {
		// return extraNegatives.size();
		// }
		// };
		//
		// final int[] indices = RandomData.getUniqueRandomInts(2000, 0,
		// hardExamples.size());
		// Arrays.sort(indices);
		// hardExamples = new AcceptingListView<FImage>(hardExamples, indices);
		//
		// final List<FImage> extendedNegatives = new
		// ConcatenatedList<FImage>(trainingImages.get(false), hardExamples);
		// final GroupedDataset<Boolean, ListDataset<FImage>, FImage>
		// extendedTrainingImages = new MapBackedDataset<Boolean,
		// ListDataset<FImage>, FImage>();
		// extendedTrainingImages.put(true, trainingImages.get(true));
		// extendedTrainingImages.put(false, new
		// ListBackedDataset<FImage>(extendedNegatives));
		//
		// final GroupedDataset<Boolean, ListDataset<DoubleFV>, DoubleFV>
		// extendedTrainingData = DatasetExtractors
		// .createLazyFeatureDataset(extendedTrainingImages, new
		// Extractor(hogClassifier));
		//
		// ann = new LiblinearAnnotator<DoubleFV, Boolean>(
		// new IdentityFeatureExtractor<DoubleFV>(), Mode.MULTICLASS,
		// SolverType.L2R_L2LOSS_SVC, 100, 0.01, true);
		// ann.train(extendedTrainingData);
		// hogClassifier.classifier = ann;

		int c = 0, p = 0;
		for (final FImage i : INRIAPersonDataset.getPositiveTrainingImages(ImageUtilities.FIMAGE_READER)) {
			hogClassifier.prepare(i);

			final int offsetX = (i.width - 64) / 2;
			final int offsetY = (i.height - 128) / 2;

			p += hogClassifier.classify(new Rectangle(offsetX, offsetY, 64, 128)) ? 1 : 0;
			c++;
		}
		System.out.println(p + "/" + c);

		IOUtils.writeToFile(hogClassifier, new File("final-classifier.dat"));
	}
}
