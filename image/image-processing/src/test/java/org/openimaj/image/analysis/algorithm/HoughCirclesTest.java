package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.HoughCircles.WeightedCircle;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.math.geometry.shape.Circle;

/**
 * Tests for {@link HoughCircles}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class HoughCirclesTest {
	/**
	 * Test
	 */
	@Test
	public void testCircle() {
		final int imgWidthHeight = 200;

		final FImage circleImage = new FImage(imgWidthHeight, imgWidthHeight);
		final Circle c = new Circle(imgWidthHeight / 2 + 3, imgWidthHeight / 2 + 1, imgWidthHeight / 4);
		circleImage.drawShapeFilled(c, 1f);

		final CannyEdgeDetector det = new CannyEdgeDetector();
		final FImage edgeImage = circleImage.process(det);

		final HoughCircles circ = new HoughCircles(5, imgWidthHeight, 5, 360);
		edgeImage.analyseWith(circ);

		final List<WeightedCircle> best = circ.getBest(1);
		final WeightedCircle b = best.get(0);

		assertTrue(b.equals(c));
	}
}
