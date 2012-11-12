package org.openimaj.image.feature.dense.gradient;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.algorithm.BinnedImageHistogramAnalyser;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Implementation of the Histogram of Gradients (HOG) feature. This
 * implementation allows any kind of spatial layout to be used, and provides the
 * standard Rectangular (R-HOG) and Circular (C-HOG) strategies. Features can be
 * efficiently extracted for the whole image, or sub-region(s).
 * <p>
 * The original description of HOG describes a feature that essentially
 * describes local TEXTURE based on the histogram of all gradients in the patch
 * (like dense SIFT). Confusingly, a different feature called PHOG (Pyramid HOG)
 * was later proposed that is primarily a SHAPE descriptor. PHOG computes
 * HOG-like descriptors in a spatial pyramid; however it only counts gradients
 * belonging to strong edges (hence it why describes shape rather than texture).
 * Both these descriptors obviously have their merits, but it is also likely
 * that a SHAPE variant of the HOG and a FEATURE variant of the PHOG could also
 * be useful. With this in mind, this class can optionally be used to compute a
 * modified HOG feature which suppresses gradients at certain spatial locations
 * (i.e. those not on edges).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HistogramOfGradients implements ImageAnalyser<FImage> {
	public interface Strategy {
		public Histogram extract(BinnedImageHistogramAnalyser binData, FImage magnitudes, Rectangle region);
	}

	BinnedImageHistogramAnalyser histExtractor;
	Strategy strategy;
	FImage magnitudes;

	public HistogramOfGradients(BinnedImageHistogramAnalyser histExtractor, Strategy strategy) {
		this.histExtractor = histExtractor;
		this.strategy = strategy;
	}

	@Override
	public void analyseImage(FImage image) {
		final FImageGradients gm = FImageGradients.getGradientMagnitudesAndOrientations(image);
		this.analyse(gm.magnitudes, gm.orientations);
	}

	public void analyseImage(FImage image, FImage edges) {
		final FImageGradients gm = FImageGradients.getGradientMagnitudesAndOrientations(image);
		this.analyse(gm.magnitudes.multiplyInplace(edges), gm.orientations);
	}

	public void analyse(FImage magnitudes, FImage orientations) {
		histExtractor.analyseImage(orientations);
		this.magnitudes = magnitudes;
	}

	public Histogram extractFeature(Rectangle rectangle) {
		return strategy.extract(histExtractor, magnitudes, rectangle);
	}
}
