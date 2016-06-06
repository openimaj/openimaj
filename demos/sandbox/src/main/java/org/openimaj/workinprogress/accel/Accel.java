package org.openimaj.workinprogress.accel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.ArrayBackedVideo;
import org.openimaj.video.processing.motion.GridMotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimatorAlgorithm;

public class Accel {
	public static void main(String[] args) throws IOException, InterruptedException {
		final FImage[] sequence = new FImage[3];
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] = ImageUtilities.readF(new File("/Users/jon/pendulum+circle/frame_" + (i + 10) + ".png"));
		}

		final MotionEstimatorAlgorithm.TEMPLATE_MATCH alg = new MotionEstimatorAlgorithm.TEMPLATE_MATCH();
		final MotionEstimator e = new GridMotionEstimator(new ArrayBackedVideo<FImage>(sequence),
				alg, 10, 10, true);

		e.getNextFrame();
		e.getNextFrame();
		final Map<Point2d, Point2d> mv1 = e.getMotionVectors();
		e.getNextFrame();
		final Map<Point2d, Point2d> mv2 = e.getMotionVectors();

		drawVectors(sequence, mv1);
		drawVectors(sequence, mv2);

		final Map<Point2d, Point2d> accel = new HashMap<Point2d, Point2d>();
		for (final Entry<Point2d, Point2d> p : mv1.entrySet()) {
			final Point2d from = p.getKey();
			final Point2d to = p.getValue().copy();
			to.translate(from);

			final Point2d v1 = p.getValue();
			Point2d v2 = mv2.get(to);
			if (v2 == null)
				v2 = new Point2dImpl(0, 0);

			final Point2d acc = v2.copy().minus(v1);
			accel.put(to, acc);
		}

		drawVectors(sequence, accel);
	}

	private static void drawVectors(final FImage[] sequence, final Map<Point2d, Point2d> mv1) {
		final FImage fr = new FImage(sequence[0].width, sequence[0].height);
		for (final Entry<Point2d, Point2d> p : mv1.entrySet()) {
			final Point2d from = p.getKey();
			final Point2d to = p.getValue().copy();
			to.translate(from);

			if (!from.equals(to))
				fr.drawLine(from, to, 1f);
		}
		DisplayUtilities.display(fr);
	}
}
