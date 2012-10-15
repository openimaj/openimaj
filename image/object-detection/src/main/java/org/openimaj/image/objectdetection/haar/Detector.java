package org.openimaj.image.objectdetection.haar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.objectdetection.AbstractMultiScaleObjectDetector;
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.time.Timer;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class Detector extends AbstractMultiScaleObjectDetector<FImage, Rectangle> {
	StageTreeClassifier cascade;
	float scaleFactor = 1.1f;

	int minStep = 1;
	int maxStep = 2;
	int stepDelta = -2;

	public Detector(StageTreeClassifier cascade) {
		super(Math.max(cascade.width, cascade.height), 0);

		this.cascade = cascade;
	}

	protected void detectAtScale(final SummedSqTiltAreaTable sat, final int startX, final int stopX, final int startY,
			final int stopY, final float ystep, final int windowWidth, final int windowHeight, List<Rectangle> results)
	{
		for (int iy = startY; iy < stopY; iy++) {
			final int y = Math.round(iy * ystep);

			for (int ix = startX, xstep = 0; ix < stopX; ix += xstep) {
				final int x = Math.round(ix * ystep);

				final int result = cascade.classify(sat, x, y);

				if (result > 0) {
					results.add(new Rectangle(x, y, windowWidth, windowHeight));
				}

				xstep = result < stepDelta ? minStep : maxStep;
				// xstep = result < 0 && result > -2 ? minStep : maxStep;
			}
		}
	}

	@Override
	public List<Rectangle> detect(FImage image) {
		final List<Rectangle> results = new ArrayList<Rectangle>();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		final SummedSqTiltAreaTable sat = new SummedSqTiltAreaTable(image, cascade.hasTiltedFeatures);

		int nFactors = 0;
		int startFactor = 0;
		for (float factor = 1; factor * cascade.width < imageWidth - 10 &&
				factor * cascade.height < imageHeight - 10; factor *= scaleFactor)
		{
			final float width = factor * cascade.width;
			final float height = factor * cascade.height;

			if (width < minSize || height < minSize) {
				startFactor++;
			}

			if (maxSize > 0 && (width > maxSize || height > maxSize)) {
				break;
			}

			nFactors++;
		}

		float factor = (float) Math.pow(scaleFactor, startFactor);
		for (int scaleStep = startFactor; scaleStep < nFactors; factor *= scaleFactor, scaleStep++) {
			final float ystep = Math.max(2, factor);

			final int windowWidth = (int) (factor * cascade.width);
			final int windowHeight = (int) (factor * cascade.height);

			final int startX = 0;
			final int startY = 0;
			final int stopX = Math.round(((imageWidth - windowWidth)) / ystep);
			final int stopY = Math.round(((imageHeight - windowHeight)) / ystep);

			// prepare the cascade for this scale
			cascade.setScale(factor);

			detectAtScale(sat, startX, stopX, startY, stopY, ystep, windowWidth, windowHeight, results);
		}

		return results;
	}

	public static void main(String[] args) throws IOException {
		// final FImage test = ImageUtilities.readF(new
		// File("/Users/jsh2/Desktop/face.jpg"));

		final StageTreeClassifier cascade = OCVHaarLoader.read(StageTreeClassifier.class
				.getResourceAsStream("haarcascade_frontalface_alt2.xml"));

		final Detector det = new Detector(cascade);
		det.setMinimumDetectionSize(80);

		// final SqRotSummedAreaTable sat = new SqRotSummedAreaTable(test,
		// false);
		// cascade.matches(sat, 171, 38, 4.1772494f);
		//
		// final List<Rectangle> rects = det.detect(test, 80);
		// System.out.println(rects);
		//
		// final MBFImage cimg = new MBFImage(test.clone(), test.clone(),
		// test.clone());
		// for (final Rectangle r : rects) {
		// cimg.drawShape(r, RGBColour.RED);
		// }
		//
		// DisplayUtilities.display(cimg);

		final OpenCVGrouping grp = new OpenCVGrouping();

		VideoDisplay.createVideoDisplay(new VideoCapture(640, 480)).addVideoListener(new VideoDisplayListener<MBFImage>()
		{
			@Override
			public void beforeUpdate(MBFImage frame) {
				final Timer t = Timer.timer();
				List<Rectangle> rects = det.detect(frame.flatten());
				rects = grp.apply(rects);
				System.out.println(t.duration());

				for (final Rectangle r : rects) {
					frame.drawShape(r, RGBColour.RED);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
