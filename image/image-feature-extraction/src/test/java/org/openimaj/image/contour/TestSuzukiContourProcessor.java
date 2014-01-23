package org.openimaj.image.contour;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Tests for the {@link SuzukiContourProcessor} direct from the paper
 * 
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
		final FImage img = new FImage(new float[][] {
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				new float[] { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
				new float[] { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0 },
				new float[] { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0 },
				new float[] { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
		});

		final FImage imgC = img.clone();
		final Border root = SuzukiContourProcessor.findContours(imgC);

		final FImage expected = new FImage(new float[][] {
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				new float[] { 0, 0, 2, 2, 2, 2, 2, 2, -2, 0, 0, 0 },
				new float[] { 0, 0, -3, 0, 0, -4, 0, 0, -2, 0, 5, 0 },
				new float[] { 0, 0, -3, 0, 0, -4, 0, 0, -2, 0, 0, 0 },
				new float[] { 0, 0, 2, 2, 2, 2, 2, 2, -2, 0, 0, 0 },
				new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
		});

		assertTrue(imgC.equals(expected));
		System.out.println(root);
	}

	public void testLargerFig3() throws Exception {
		final FImage img = new FImage(640, 360);

		final Rectangle bigOuter = new Rectangle(192, 108, 256, 144);
		final Rectangle leftHole = new Rectangle(202, 118, 113, 124);
		final Rectangle rightHole = new Rectangle(325, 118, 113, 124);

		final Rectangle smallOuter = new Rectangle(458, 160, 10, 10);

		img.drawShapeFilled(bigOuter, 1f);
		img.drawShapeFilled(leftHole, 0f);
		img.drawShapeFilled(rightHole, 0f);
		img.drawShapeFilled(smallOuter, 1f);

		DisplayUtilities.display(img);

		final Border root = SuzukiContourProcessor.findContours(img);

		final MBFImage imgC = new MBFImage(640, 360, ColourSpace.RGB);
		ContourRenderer.drawContours(imgC, root);
		DisplayUtilities.display(imgC);
	}

	public static void main(String[] args) throws Exception {
		final TestSuzukiContourProcessor wang = new TestSuzukiContourProcessor();
		wang.testLargerFig3();
	}
}
