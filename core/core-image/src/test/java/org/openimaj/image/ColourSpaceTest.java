package org.openimaj.image;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.image.colour.ColourSpace;

/**
 * Test some colour space conversion
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ColourSpaceTest {
	/**
	 * @throws Exception
	 */
	@Test
	public void testRGBtoRGBA() throws Exception {
		MBFImage img = ImageUtilities.readMBF(ColourSpaceTest.class.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		MBFImage imgConv = ColourSpace.convert(img, ColourSpace.RGBA);
		assertTrue(img.getBounds().equals(imgConv.getBounds()));
		assertTrue(img.equals(ColourSpace.convert(imgConv,ColourSpace.RGB)));
	}
}
