package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.image.FImage;

/**
 * Tests for {@link SummedSqTiltAreaTable}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SummedSqTiltAreaTableTests {
	/**
	 * Compare all SAT implementations to ensure consistent results.
	 */
	@Test
	public void testCompare() {
		final int sz = 100;
		final FImage image = new FImage(RandomData.getRandomFloatArray(sz - 1, sz - 1, 0f, 1f));

		final SummedAreaTable sat = new SummedAreaTable(image);
		final SummedSqAreaTable sqsat = new SummedSqAreaTable(image);
		final SummedSqTiltAreaTable sqsat2 = new SummedSqTiltAreaTable(image, false);
		final SummedSqTiltAreaTable sqtsat = new SummedSqTiltAreaTable(image, true);

		assertEquals(sz, sat.data.width);
		assertEquals(sz, sat.data.height);

		assertEquals(sz, sqsat.sum.width);
		assertEquals(sz, sqsat.sum.height);
		assertEquals(sz, sqsat.sqSum.width);
		assertEquals(sz, sqsat.sqSum.height);

		assertEquals(sz, sqsat2.sum.width);
		assertEquals(sz, sqsat2.sum.height);
		assertEquals(sz, sqsat2.sqSum.width);
		assertEquals(sz, sqsat2.sqSum.height);
		assertEquals(null, sqsat2.tiltSum);

		assertEquals(sz, sqtsat.sum.width);
		assertEquals(sz, sqtsat.sum.height);
		assertEquals(sz, sqtsat.sqSum.width);
		assertEquals(sz, sqtsat.sqSum.height);
		assertEquals(sz, sqtsat.tiltSum.width);
		assertEquals(sz, sqtsat.tiltSum.height);

		for (int y = 0; y < sz; y++) {
			for (int x = 0; x < sz; x++) {
				assertEquals(sat.data.pixels[y][x], sqsat.sum.pixels[y][x], 0.1f);
				assertEquals(sat.data.pixels[y][x], sqsat2.sum.pixels[y][x], 0.1f);
				assertEquals(sat.data.pixels[y][x], sqtsat.sum.pixels[y][x], 0.1f);

				assertEquals(sqsat.sqSum.pixels[y][x], sqsat2.sqSum.pixels[y][x], 0.1f);
				assertEquals(sqsat.sqSum.pixels[y][x], sqtsat.sqSum.pixels[y][x], 0.1f);
			}
		}
	}

	@Test
	public void testData() {
		final FImage image = new FImage(new float[][] {
				{ 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }
		});

		final SummedSqTiltAreaTable sqtsat = new SummedSqTiltAreaTable(image, true);

		System.out.println(image);
		System.out.println(sqtsat.sum);
		System.out.println(sqtsat.sqSum);
		System.out.println(sqtsat.tiltSum);
	}
}
