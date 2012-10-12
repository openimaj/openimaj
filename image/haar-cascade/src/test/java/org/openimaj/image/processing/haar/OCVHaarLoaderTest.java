package org.openimaj.image.processing.haar;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests for the OpenCV haar cascade loader
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class OCVHaarLoaderTest {
	String[] goodCascades = {
			"haarcascade_eye_tree_eyeglasses.xml",
			"haarcascade_eye.xml",
			"haarcascade_frontalface_alt_tree.xml",
			"haarcascade_frontalface_alt.xml",
			"haarcascade_frontalface_alt2.xml",
			"haarcascade_frontalface_default.xml",
			"haarcascade_fullbody.xml",
			"haarcascade_lefteye_2splits.xml",
			"haarcascade_lowerbody.xml",
			"haarcascade_mcs_eyepair_big.xml",
			"haarcascade_mcs_eyepair_small.xml",
			"haarcascade_mcs_lefteye.xml",
			"haarcascade_mcs_mouth.xml",
			"haarcascade_mcs_nose.xml",
			"haarcascade_mcs_righteye.xml",
			"haarcascade_mcs_upperbody.xml",
			"haarcascade_profileface.xml",
			"haarcascade_righteye_2splits.xml",
			"haarcascade_upperbody.xml"
	};

	String badCascade = "lbpcascade_frontalface.xml";

	/**
	 * Test the internal read method
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadNode() throws IOException {
		for (final String c : goodCascades) {
			OCVHaarLoader.readXPP(OCVHaarLoader.class.getResourceAsStream(c));
		}
	}

	/**
	 * Test the conversion to proper objects
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRead() throws IOException {
		for (final String c : goodCascades) {
			OCVHaarLoader.read(OCVHaarLoader.class.getResourceAsStream(c));
		}
	}

	/**
	 * Test that reading the new style cascade throws an exception
	 * 
	 * @throws IOException
	 */
	@Test(expected = IOException.class)
	public void testReadNodeBad() throws IOException {
		OCVHaarLoader.readXPP(OCVHaarLoader.class.getResourceAsStream(badCascade));
	}
}
