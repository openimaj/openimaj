package org.openimaj.image.objectdetection.hog;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.objectdetection.AbstractMultiScaleObjectDetector;
import org.openimaj.math.geometry.shape.Rectangle;

public class HOGDetector extends AbstractMultiScaleObjectDetector<FImage, Rectangle> {
	protected float scaleFactor = 1.2f;
	protected HOGClassifier classifier;

	public HOGDetector(HOGClassifier classifier, float scaleFactor) {
		this.classifier = classifier;
		this.scaleFactor = scaleFactor;
	}

	public HOGDetector(HOGClassifier classifier) {
		this.classifier = classifier;
	}

	@Override
	public List<Rectangle> detect(FImage image) {
		final List<Rectangle> results = new ArrayList<Rectangle>();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		classifier.prepare(image);

		// compute the number of scales to test and the starting factor
		int nFactors = 0;
		int startFactor = 0;
		for (float factor = 1; factor * classifier.width < imageWidth &&
				factor * classifier.height < imageHeight; factor *= scaleFactor)
		{
			final float width = factor * classifier.width;
			final float height = factor * classifier.height;

			if (width < minSize || height < minSize) {
				startFactor++;
			}

			if (maxSize > 0 && (width > maxSize || height > maxSize)) {
				break;
			}

			nFactors++;
		}

		// run the detection at each scale
		float factor = (float) Math.pow(scaleFactor, startFactor);
		for (int scaleStep = startFactor; scaleStep < nFactors; factor *=
				scaleFactor, scaleStep++)
		{
			final float ystep = 8 * factor;
			final int windowWidth = (int) (factor * classifier.width);
			final int windowHeight = (int) (factor * classifier.height);

			// determine the spatial range, taking into account any ROI.
			final int startX = (int) (roi == null ? 0 : Math.max(0, roi.x));
			final int startY = (int) (roi == null ? 0 : Math.max(0, roi.y));
			final int stopX = Math.round(
					(roi == null ? imageWidth : Math.min(imageWidth, roi.x + roi.width)) - windowWidth);
			final int stopY = Math.round((((roi == null ? imageHeight : Math.min(imageHeight, roi.y +
					roi.height)) - windowHeight)));

			detectAtScale(startX, stopX, startY, stopY, ystep, windowWidth, windowHeight, results);
		}

		return results;
	}

	/**
	 * Perform detection at a single scale. Subclasses may override this to
	 * customise the spatial search. The given starting and stopping coordinates
	 * take into account any region of interest set on this detector.
	 * 
	 * @param startX
	 *            the starting x-ordinate
	 * @param stopX
	 *            the stopping x-ordinate
	 * @param startY
	 *            the starting y-ordinate
	 * @param stopY
	 *            the stopping y-ordinate
	 * @param ystep
	 *            the amount to step
	 * @param windowWidth
	 *            the window width at the current scale
	 * @param windowHeight
	 *            the window height at the current scale
	 * @param results
	 *            the list to store detection results in
	 */
	protected void detectAtScale(final int startX, final int stopX, final int startY,
			final int stopY, final float ystep, final int windowWidth, final int windowHeight,
			final List<Rectangle> results)
	{
		final Rectangle current = new Rectangle();

		for (int iy = startY; iy < stopY; iy += ystep) {
			for (int ix = startX; ix < stopX; ix += ystep) {
				current.x = ix;
				current.y = iy;
				current.width = windowWidth;
				current.height = windowHeight;

				if (classifier.classify(current)) {
					results.add(current.clone());
				}
			}
		}
	}
}
