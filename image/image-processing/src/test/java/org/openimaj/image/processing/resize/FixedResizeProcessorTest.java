package org.openimaj.image.processing.resize;

import java.io.IOException;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FixedResizeProcessorTest {
	/**
	 * @throws IOException
	 */
	@Test
	public void testFixedResize() throws IOException{
		final FImage image = ImageUtilities.readF(ResizeProcessorTest.class
				.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		long start,end;
		start = System.currentTimeMillis();
		FixedResizeProcessor frp = new FixedResizeProcessor(image, 500, 250);
		for (int i = 0; i < 10000; i++) {
			image.process(frp);
		}
		end = System.currentTimeMillis();
		System.out.println("Time taken (fixed): " + (end - start));
		
		ResizeProcessor rp = new ResizeProcessor(500, 250);
		start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			image.process(rp);
		}
		end = System.currentTimeMillis();
		System.out.println("Time taken (normal): " + (end - start));
	}
	
	public static void main(String[] args) throws IOException {
		new FixedResizeProcessorTest().testFixedResize();
	}

}
