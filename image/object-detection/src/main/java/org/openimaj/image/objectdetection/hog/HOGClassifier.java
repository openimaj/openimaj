package org.openimaj.image.objectdetection.hog;

import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.gradient.HOG;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

public class HOGClassifier {
	int width;
	int height;
	HOG hogExtractor;
	Annotator<DoubleFV, Boolean> classifier;

	public void prepare(FImage image) {
		hogExtractor.analyseImage(image);
	}

	public double classify(Rectangle current) {
		final Histogram fv = hogExtractor.getFeatureVector(current);

		final List<ScoredAnnotation<Boolean>> res = classifier.annotate(fv);

		if (res.get(0).annotation) {
			return res.get(0).confidence;
		} else {
			return 1 - res.get(0).confidence;
		}
	}
}
