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
package org.openimaj.image.analysis.algorithm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.FValuePixel;

/**
 * Tests for template matcher
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TemplateMatchingTest {
	FImage image;
	FImage template;
	
	/**
	 * Setup the test data
	 * @throws IOException 
	 */
	@Before
	public void setup() throws IOException {
		image = ImageUtilities.readF(getClass().getResourceAsStream("/org/openimaj/image/data/bird.png"));
		template = image.extractROI(100, 100, 100, 100);
	}
	
	/**
	 * Compare the two template matcher implementations against one another 
	 */
	@Test
	public void compareTest() {
		for (TemplateMatcher.Mode sMode : TemplateMatcher.Mode.values()) {
			TemplateMatcher sMatcher = new TemplateMatcher(template, sMode);
			sMatcher.analyseImage(image);
			FValuePixel[] sBestResponses = sMatcher.getBestResponses(5);
			FImage sResponse = sMatcher.getResponseMap().normalise();
			
			FourierTemplateMatcher.Mode fMode = FourierTemplateMatcher.Mode.valueOf(sMode.toString());
			FourierTemplateMatcher fMatcher = new FourierTemplateMatcher(template, fMode);
			fMatcher.analyseImage(image);
			FValuePixel[] fBestResponses = fMatcher.getBestResponses(5);
			FImage fResponse = fMatcher.getResponseMap().normalise();
			
			System.out.println(sMode);
			
			assertEquals(fMode.scoresAscending(), sMode.scoresAscending());
			
			assertEquals(fBestResponses.length, sBestResponses.length);
			for (int i=0; i<fBestResponses.length; i++) {
				assertEquals(fBestResponses[i].x, sBestResponses[i].x);
				assertEquals(fBestResponses[i].y, sBestResponses[i].y);
			}
			
			assertEquals(fResponse.width, sResponse.width);
			assertEquals(fResponse.height, sResponse.height);
			
			for (int y=0; y<fResponse.height; y++) {
				for (int x=0; x<fResponse.width; x++) {
					assertEquals(fResponse.pixels[y][x], sResponse.pixels[y][x], 0.15);
				}
			}
		}
	}
}
