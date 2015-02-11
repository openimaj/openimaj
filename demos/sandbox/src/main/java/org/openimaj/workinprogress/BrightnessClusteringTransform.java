package org.openimaj.workinprogress;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.SummedAreaTable;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.util.array.ArrayUtils;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Implementation of the Brightness Clustering Transform.
 * <p>
 * FIXME: add references when available and move.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class BrightnessClusteringTransform implements ImageProcessor<FImage> {
	float sigma = 2;
	int maxVotes = 100000;
	int rangeMin = 3;
	int rangeMax = 7;
	Uniform rng = new Uniform(new MersenneTwister());

	@Override
	public void processImage(FImage image) {
		final SummedAreaTable sat = new SummedAreaTable(image);
		final FImage output = new FImage(image.width, image.height);

		for (int vote = 0; vote < maxVotes; vote++) {
			// First step
			int widthR = (int) Math.pow(2, rng.nextIntFromTo(rangeMin, rangeMax));
			int heightR = (int) Math.pow(2, rng.nextIntFromTo(rangeMin, rangeMax));
			int xR = rng.nextIntFromTo(0, image.width - widthR);
			int yR = rng.nextIntFromTo(0, image.height - heightR);

			// Second step
			while (widthR > 2 && heightR > 2) {
				final int hw = widthR / 2;
				final int hh = heightR / 2;

				final float[] r = {
						sat.calculateArea(xR, yR, xR + hw, yR + hh),
						sat.calculateArea(xR + hw, yR, xR + widthR, yR + hh),
						sat.calculateArea(xR, yR + hh, xR + hw, yR + heightR),
						sat.calculateArea(xR + hw, yR + hh, xR + widthR, yR + heightR),
				};

				final int[] f = ArrayUtils.indexSort(r);
				if (r[f[3]] == r[f[2]])
					break;

				final int maxIdx = ArrayUtils.maxIndex(r);

				widthR = hw;
				heightR = hh;
				if (maxIdx == 1) {
					xR = xR + hw;
				} else if (maxIdx == 2) {
					yR = yR + hh;
				} else if (maxIdx == 3) {
					xR = xR + hw;
					yR = yR + hh;
				}
			}

			// Third step
			final int xLoc = Math.round(xR + widthR / 2);
			final int yLoc = Math.round(yR + heightR / 2);
			output.pixels[yLoc][xLoc]++;
		}

		output.processInplace(new FGaussianConvolve(sigma));
		image.internalAssign(output.normalise());
	}

	// public static void main(String[] args) throws IOException {
	// // FImage image = ImageUtilities.readF(new
	// // File("/Users/jon/Pictures/Pictures/2007/02/17/IMG_1165.JPG"));
	// // image = ResizeProcessor.halfSize(image);
	// final FImage image = new FImage(400, 400);
	// image.drawShapeFilled(new Ellipse(200, 200, 20, 20, 0), 1f);
	//
	// DisplayUtilities.display(image);
	//
	// final FImage result = image.process(new BrightnessClusteringTransform());
	// DisplayUtilities.display(result);
	// }
}
