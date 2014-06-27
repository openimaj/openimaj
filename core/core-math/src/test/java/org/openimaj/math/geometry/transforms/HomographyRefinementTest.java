package org.openimaj.math.geometry.transforms;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Tests for {@link HomographyRefinement}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HomographyRefinementTest {
	/**
	 * Test that the refinement actually produces lower error than the initial
	 * estimate
	 */
	@Test
	public void performTests() {
		final Matrix correctH = new Matrix(new double[][] {
				{ 60.07530, -3.66575, 59.65353 },
				{ -1.19148, 61.88654, 439.01489 },
				{ -0.01007, -0.00660, 1.00000 } });

		final List<Point2d> image1 = makeCorrect(1000);
		final List<Point2d> image2 = transformWithNoise(image1, correctH);

		final List<IndependentPair<Point2d, Point2d>> data = IndependentPair.pairList(image1, image2);

		final Matrix initial = TransformUtilities.homographyMatrixNorm(data);
		for (final HomographyRefinement type : HomographyRefinement.values()) {
			final Matrix improved = type.refine(initial, data);

			assertTrue(type.computeError(improved, data) <= type.computeError(initial, data));
			assertTrue(type.computeError(improved, data) <= type.computeError(correctH, data));
		}
	}

	private static List<Point2d> transformWithNoise(List<Point2d> correctPoints,
			Matrix correctH)
	{
		final List<Point2d> pts = new ArrayList<Point2d>();
		final Random rng = new Random(0);
		for (final Point2d correct : correctPoints) {
			final Point2dImpl pt = (Point2dImpl) correct.transform(correctH);

			pt.x += rng.nextGaussian();
			pt.y += rng.nextGaussian();

			pts.add(pt);
		}

		return pts;
	}

	private static List<Point2d> makeCorrect(int n) {
		final List<Point2d> pts = new ArrayList<Point2d>();

		for (int i = 0; i < n; i++)
			pts.add(Point2dImpl.createRandomPoint());

		return pts;
	}
}
