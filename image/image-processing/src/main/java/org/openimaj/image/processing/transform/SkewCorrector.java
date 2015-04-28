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
/**
 *
 */
package org.openimaj.image.processing.transform;

import java.util.Collection;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.HoughLines;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.line.Line2d;

import Jama.Matrix;

/**
 * Uses the Hough transform (for lines) to attempt to find the skew of the image
 * and unskews it using a basic skew transform.
 *
 * @see "http://javaanpr.sourceforge.net/anpr.pdf"
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 12 Aug 2011
 *
 */
public class SkewCorrector implements ImageProcessor<FImage>
{
	private static final boolean DEBUG = true;

	/**
	 * Accuracy is a multiplier for the number of degrees in one bin of the
	 * HoughLines transform
	 */
	private int accuracy = 1;

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(final FImage image)
	{
		final CannyEdgeDetector cad = new CannyEdgeDetector();
		final FImage edgeImage = image.process(cad).inverse();

		// Detect Lines in the image
		final HoughLines hl = new HoughLines(360 * this.accuracy);
		edgeImage.analyseWith(hl);

		if (SkewCorrector.DEBUG)
			this.debugLines(edgeImage, Matrix.identity(3, 3),
					"Detection of Horizontal Lines",
					hl.getBestLines(2));

		// ---------------------------------------------------------------
		// First rotate the image such that the prevailing lines
		// are horizontal.
		// ---------------------------------------------------------------
		// Find the prevailing angle
		double rotationAngle = hl.calculatePrevailingAngle();

		FImage rotImg = null;
		FImage outImg = null;
		if (rotationAngle == Double.MIN_VALUE)
		{
			System.out.println("WARNING: Detection of rotation angle failed.");
			rotImg = edgeImage.clone();
			outImg = image.clone();
		}
		else
		{
			rotationAngle -= 90;
			rotationAngle %= 360;

			if (SkewCorrector.DEBUG)
				System.out.println("Rotational angle: " + rotationAngle);

			rotationAngle *= 0.0174532925;

			// Rotate so that horizontal lines are horizontal
			final Matrix rotationMatrix = new Matrix(new double[][] {
					{ Math.cos(-rotationAngle), -Math.sin(-rotationAngle), 0 },
					{ Math.sin(-rotationAngle), Math.cos(-rotationAngle), 0 },
					{ 0, 0, 1 }
			});

			// We use a projection processor as we need our
			// background pixels to be white.
			rotImg = ProjectionProcessor.project(edgeImage, rotationMatrix, 1f).
					process(new OtsuThreshold());

			// We need to return a proper image (not the edge image), so we
			// process that here too.
			outImg = ProjectionProcessor.project(image, rotationMatrix, 0f);
		}

		if (SkewCorrector.DEBUG)
			DisplayUtilities.display(outImg, "Rotated Image");

		// ---------------------------------------------------------------
		// Now attempt to make the verticals vertical by shearing
		// ---------------------------------------------------------------
		// Re-process with the Hough lines
		rotImg.analyseWith(hl);

		final float shearAngleRange = 20;

		if (SkewCorrector.DEBUG)
			this.debugLines(rotImg, Matrix.identity(3, 3), "Detection of Vertical Lines",
					hl.getBestLines(2, -shearAngleRange, shearAngleRange));

		// Get the prevailing angle around vertical
		double shearAngle = hl.calculatePrevailingAngle(-shearAngleRange, shearAngleRange);

		if (shearAngle == Double.MIN_VALUE)
		{
			System.out.println("WARNING: Detection of shear angle failed.");
		}
		else
		{
			shearAngle %= 360;

			if (SkewCorrector.DEBUG)
				System.out.println("Shear angle = " + shearAngle);

			shearAngle *= 0.0174532925;

			// Create a shear matrix
			final Matrix shearMatrix = new Matrix(new double[][] {
					{ 1, Math.tan(shearAngle), 0 },
					{ 0, 1, 0 },
					{ 0, 0, 1 }
			});

			// Process the image to unshear it.
			// FImage unshearedImage = rotImg.transform( shearMatrix );
			outImg = outImg.transform(shearMatrix);
		}

		if (SkewCorrector.DEBUG)
			DisplayUtilities.display(outImg, "Final Image");

		image.internalAssign(outImg);
	}

	/**
	 * Helper function to display the image with lines
	 *
	 * @param i
	 * @param hl
	 * @param tf
	 * @param title
	 * @param lines
	 */
	private void debugLines(final FImage i, final Matrix tf, final String title,
			final Collection<Line2d> lines)
	{
		// Create an image showing where the lines are
		final MBFImage output = new MBFImage(i.getWidth(),
				i.getHeight(), 3);
		final MBFImageRenderer r = output.createRenderer(); // RenderHints.ANTI_ALIASED
		// );
		r.drawImage(i, 0, 0);

		for (final Line2d l : lines)
		{
			final Line2d l2 = l.transform(tf).lineWithinSquare(output.getBounds());

			// l2 can be null if it doesn't intersect with the image
			if (l2 != null)
			{
				System.out.println(l2);
				r.drawLine(l2, 2, RGBColour.RED);
			}
		}

		DisplayUtilities.display(output, title);
	}

	/**
	 * Set the accuracy of the skew corrector. The value here is a multiplier
	 * for the number of degrees that are in a single bin of the Hough Transform
	 * for lines. The default is 1 which means that the Hough Transform can
	 * detect 360 degrees. If the accuracy is set to 2, the Hough Transform can
	 * detect 720 distinct directional angles (accuracy is half a degree).
	 *
	 * @param accuracy
	 *            The accuracy of the skew corrector
	 */
	public void setAccuracy(final int accuracy)
	{
		this.accuracy = accuracy;
	}
}
