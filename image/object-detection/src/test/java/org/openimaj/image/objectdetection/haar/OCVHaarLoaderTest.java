/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.objectdetection.haar;

import java.io.IOException;

import org.junit.Test;
import org.openimaj.image.objectdetection.haar.OCVHaarLoader;

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
