package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.OpenIMAJ;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Tests for {@link BinnedImageHistogramAnalyser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class InterpolatingBinnedImageHistogramAnalyserTest {
	FImage image;
	InterpolatingBinnedImageHistogramAnalyser analyser;

	/**
	 * Setup tests
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());
		analyser = new InterpolatingBinnedImageHistogramAnalyser(64);
		image.analyseWith(analyser);
	}

	@Test
	public void testSinglePixel1() {
		final FImage i = new FImage(new float[][] { { 0.5f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(5);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0, 1, 0, 0 }, h.values, 0.001);
	}

	@Test
	public void testSinglePixel2() {
		final FImage i = new FImage(new float[][] { { 0.5f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(4);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0.5, 0.5, 0 }, h.values, 0.001);
	}

	@Test
	public void testSinglePixel3() {
		final FImage i = new FImage(new float[][] { { 0.0f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(4);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 1, 0, 0, 0 }, h.values, 0.001);
	}

	@Test
	public void testSinglePixel4() {
		final FImage i = new FImage(new float[][] { { 1.0f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(4);
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0, 0, 1 }, h.values, 0.001);
	}

	@Test
	public void testSinglePixelWrap() {
		final FImage i = new FImage(new float[][] { { 1.0f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(4);
		a.wrap = true;
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0.5, 0, 0, 0.5 }, h.values, 0.001);
	}

	@Test
	public void testSinglePixelWrap2() {
		final FImage i = new FImage(new float[][] { { 0.9f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(5);
		a.wrap = true;
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0, 0, 0, 0, 1 }, h.values, 0.001);
	}

	@Test
	public void testSinglePixelWrap3() {
		final FImage i = new FImage(new float[][] { { 0.95f } });

		final InterpolatingBinnedImageHistogramAnalyser a = new InterpolatingBinnedImageHistogramAnalyser(5);
		a.wrap = true;
		a.analyseImage(i);

		final Histogram h = a.computeHistogram(0, 0, 1, 1);
		assertArrayEquals(new double[] { 0.25, 0, 0, 0, 0.75 }, h.values, 0.001);
	}
}
