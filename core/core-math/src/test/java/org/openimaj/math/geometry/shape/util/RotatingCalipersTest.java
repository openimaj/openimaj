package org.openimaj.math.geometry.shape.util;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.math.geometry.shape.RotatedRectangle;

/**
 * Tests for rotating calipers
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class RotatingCalipersTest {
	/**
	 * Test wide rect rotation
	 */
	@Test
	public void testWide() {
		final RotatedRectangle rr = new RotatedRectangle(0, 0, 50, 100, 0);

		for (float angle = 0; angle < Math.PI / 2; angle += 0.01) {
			rr.rotation = angle;

			final RotatedRectangle rr2 = rr.asPolygon().minimumBoundingRectangle();

			Assert.assertEquals(rr.rotation, rr2.rotation, 0.1);
			Assert.assertEquals(rr.height, rr2.height, 0.1);
			Assert.assertEquals(rr.width, rr2.width, 0.1);
		}
	}

	/**
	 * Test tall rect rotation
	 */
	@Test
	public void testTall() {
		final RotatedRectangle rr = new RotatedRectangle(0, 0, 100, 50, 0);

		for (float angle = 0; angle < Math.PI / 2; angle += 0.01) {
			rr.rotation = angle;

			final RotatedRectangle rr2 = rr.asPolygon().minimumBoundingRectangle();

			Assert.assertEquals(rr.rotation, rr2.rotation, 0.1);
			Assert.assertEquals(rr.height, rr2.height, 0.1);
			Assert.assertEquals(rr.width, rr2.width, 0.1);
		}
	}

	/**
	 * Test square rotation
	 */
	@Test
	public void testSquare() {
		final RotatedRectangle rr = new RotatedRectangle(0, 0, 100, 100, 0);

		for (float angle = 0; angle < Math.PI / 2; angle += 0.01) {
			rr.rotation = angle;

			final RotatedRectangle rr2 = rr.asPolygon().minimumBoundingRectangle();

			Assert.assertEquals(rr.rotation, rr2.rotation, 0.1);
			Assert.assertEquals(rr.height, rr2.height, 0.1);
			Assert.assertEquals(rr.width, rr2.width, 0.1);
		}
	}
}
