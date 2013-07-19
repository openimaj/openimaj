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
	public static void main(String[] args) throws IOException {
		final HOGClassifier hogClassifier = new HOGClassifier();

		final FlexibleHOGStrategy strategy = new FlexibleHOGStrategy(8, 16, 2);
		hogClassifier.hogExtractor = new HOG(9, false, FImageGradients.Mode.Unsigned, strategy);

		final GroupedDataset<Boolean, ListDataset<DoubleFV>, DoubleFV> trainingData = DatasetExtractors
				.createLazyFeatureDataset(INRIAPersonDataset.getTrainingData(),
						new FeatureExtractor<DoubleFV, FImage>() {
							int i = 0;

							@Override
							public DoubleFV extractFeature(FImage image) {
								System.out.println("Extracting Feature " + (i++));

								final int offsetX = (image.width - 64) / 2;
								final int offsetY = (image.height - 128) / 2;
								hogClassifier.hogExtractor.analyseImage(image);

								final Histogram f = hogClassifier.hogExtractor.getFeatureVector(new Rectangle(offsetX,
										offsetY, 64, 128));

								return f;
							}
						});

		final LiblinearAnnotator<DoubleFV, Boolean> ann = new LiblinearAnnotator<DoubleFV, Boolean>(
				new IdentityFeatureExtractor<DoubleFV>(), Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 0.01, 0.01, true);
		ann.train(trainingData);
		hogClassifier.classifier = ann;

		IOUtils.writeToFile(hogClassifier, new File("initial-classifier.dat"));
	}
}
