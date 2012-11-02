package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.OpenIMAJ;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Tests for {@link BinnedImageHistogramAnalyser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BinnedImageHistogramAnalyserTest {
	FImage image;
	BinnedImageHistogramAnalyser analyser;

	/**
	 * Setup tests
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());
		analyser = new BinnedImageHistogramAnalyser(64);
		image.analyseWith(analyser);
	}

	/**
	 * Test that the global histogram produced with {@link HistogramAnalyser}
	 * matches the one produced with {@link BinnedImageHistogramAnalyser}.
	 */
	@Test
	public void testFullHistogram() {
		final Histogram hist1 = HistogramAnalyser.getHistogram(image, analyser.nbins);
		final Histogram hist2 = analyser.computeHistogram(0, 0, image.width, image.height);

		assertArrayEquals(hist1.values, hist2.values, 0.0001);
	}

	/**
	 * Test that the windowed histogram produced with {@link HistogramAnalyser}
	 * matches the one produced with {@link BinnedImageHistogramAnalyser}.
	 */
	@Test
	public void testWindowedHistogram() {
		final Rectangle roi = new Rectangle(50, 10, 100, 100);

		final Histogram hist1 = HistogramAnalyser.getHistogram(image.extractROI(roi), analyser.nbins);
		final Histogram hist2 = analyser.computeHistogram(roi);

		assertArrayEquals(hist1.values, hist2.values, 0.0001);
	}
}
