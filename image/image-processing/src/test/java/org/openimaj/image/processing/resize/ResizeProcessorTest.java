package org.openimaj.image.processing.resize;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Tests for the {@link ResizeProcessor}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ResizeProcessorTest {
	/**
	 * Test bounded zoom
	 * @throws Exception
	 */
	@Test
	public void testBoundedZoom() throws Exception {
		FImage image = ImageUtilities.readF(ResizeProcessorTest.class.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		
		FImage imageOut = new FImage(200,100);
		ResizeFilterFunction filter = new BSplineFilter();
		Rectangle inLoc = new Rectangle(30f,30f,100f,100f);
		Rectangle outLoc = imageOut.getBounds();
		int ret = ResizeProcessor.zoom(
				image, inLoc, 
				imageOut, outLoc, 
				filter, filter.getDefaultSupport()
		);
		assertTrue(ret != -1);
		
		imageOut = new FImage(20,20);
		outLoc = imageOut.getBounds();
		ret = ResizeProcessor.zoom(
				image, inLoc, 
				imageOut, outLoc, 
				filter, filter.getDefaultSupport()
		);
		
		assertTrue(ret != -1);
		// Now some speed tests, 10000 times with extract and 10000 times with new zoom
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			ResizeProcessor.zoom(
					image, inLoc, 
					imageOut, outLoc, 
					filter, filter.getDefaultSupport()
			);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken (inplace zoom): " + (end - start));
		start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			FImage imageExtracted = image.extractROI(inLoc); // Can't do it any other way with normal zoom
			ResizeProcessor.zoom(
					imageExtracted, 
					imageOut, 
					filter, filter.getDefaultSupport()
			);
		}
		end = System.currentTimeMillis();
		System.out.println("Time taken (extract/zoom): " + (end - start));
	}
}
