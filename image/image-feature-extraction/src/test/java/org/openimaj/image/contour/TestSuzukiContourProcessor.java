package org.openimaj.image.contour;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Tests for the {@link SuzukiContourProcessor} direct from the paper
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestSuzukiContourProcessor {
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFig3() throws Exception {
//		FImage img = new FImage(new float[][]{
//			new float[]{0,0,0,0,0,0,0,0,0,0,0,0},
//			new float[]{0,0,1,1,1,1,1,1,1,0,0,0},
//			new float[]{0,0,1,0,0,1,0,0,1,0,1,0},
//			new float[]{0,0,1,0,0,1,0,0,1,0,0,0},
//			new float[]{0,0,1,1,1,1,1,1,1,0,0,0},
//			new float[]{0,0,0,0,0,0,0,0,0,0,0,0},
//		});
//		
//		SuzukiContourProcessor proc = new SuzukiContourProcessor();
//		FImage imgC = img.clone();
//		proc.processImage(imgC);
//		
//		FImage expected = new FImage(new float[][]{
//			new float[]{0,0, 0,0,0, 0,0,0, 0,0, 0, 0},
//			new float[]{0,0, 2,2,2, 2,2,2,-2,0, 0, 0},
//			new float[]{0,0,-3,0,0,-4,0,0,-2,0,-5, 0},
//			new float[]{0,0,-3,0,0,-4,0,0,-2,0, 0, 0},
//			new float[]{0,0, 2,2,2, 2,2,2,-2,0, 0, 0},
//			new float[]{0,0, 0,0,0, 0,0,0, 0,0, 0, 0},
//		});
//		
//		assertTrue(imgC.equals(expected));
	}
}
