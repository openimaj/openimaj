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
