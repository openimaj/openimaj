package org.openimaj.image.objectdetection.hog;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.gradient.HOG;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.ml.annotation.Annotator;

public class HOGClassifier {
	int width;
	int height;
	HOG hogExtractor;
	Annotator<DoubleFV, Boolean> classifier;

	public void prepare(FImage image) {
		hogExtractor.analyseImage(image);
	}

	public boolean classify(Rectangle current) {
		final Histogram fv = hogExtractor.getFeatureVector(current);

		return classifier.annotate(fv).get(0).annotation;
	}
}
