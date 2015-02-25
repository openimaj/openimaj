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
package org.openimaj.image.processing.restoration.inpainting;

import java.util.PriorityQueue;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.morphology.Dilate;
import org.openimaj.image.processing.morphology.StructuringElement;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Abstract base class for inpainting algorithms based on the Fast Marching
 * Method (FMM) for selecting the order of pixels to paint.
 * <p>
 * FMM is used for computing the evolution of boundary moving in a direction
 * <i>normal</i> to itself. Formally, FMM approximates the solution to the <a
 * href="http://en.wikipedia.org/wiki/Eikonal_equation">Eikonal function</a>
 * using an <a href="http://en.wikipedia.org/wiki/Upwind_scheme">upwind
 * scheme<a>.
 * <p>
 * The core algorithm implemented by the {@link #performInpainting(Image)}
 * method follows these steps:
 * <ul>
 * <li>Extract the pixel with the smallest distance value (t) in the BAND
 * pixels.</li>
 * <li>Update its flag value as KNOWN.</li>
 * <li>March the boundary inwards by adding new points.</li>
 * <ul>
 * <li>If they are either UNKNOWN or BAND, compute its t value using the Eikonal
 * function for all the 4 quadrants.</li>
 * <li>If flag is UNKNOWN:</li>
 * <ul>
 * <li>Change it to BAND.</li>
 * <li>Inpaint the pixel.</li>
 * </ul>
 * <li>Select the min value and assign it as the t value of the pixel.</li>
 * <li>Insert this new value in the heap.</li> </ul> </ul>
 * <p>
 * The {@link #inpaint(int, int, Image)} method must be implemented by
 * subclasses to actually perform the inpainting operation
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
@References(references = {
		@Reference(
				type = ReferenceType.Article,
				author = { "Telea, Alexandru" },
				title = "An Image Inpainting Technique Based on the Fast Marching Method.",
				year = "2004",
				journal = "J. Graphics, GPU, & Game Tools",
				pages = { "23", "34" },
				url = "http://dblp.uni-trier.de/db/journals/jgtools/jgtools9.html#Telea04",
				number = "1",
				volume = "9",
				customData = {
						"biburl", "http://www.bibsonomy.org/bibtex/2b0bf54e265d011a8e1fe256e6fcf556b/dblp",
						"ee", "http://dx.doi.org/10.1080/10867651.2004.10487596",
						"keywords", "dblp"
				}
		),
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "J. A. Sethian" },
				title = "A Fast Marching Level Set Method for Monotonically Advancing Fronts",
				year = "1995",
				booktitle = "Proc. Nat. Acad. Sci",
				pages = { "1591", "", "1595" }
		)
})
@SuppressWarnings("javadoc")
public abstract class AbstractFMMInpainter<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends
		AbstractImageMaskInpainter<IMAGE>
{
	private static final int[][] DELTAS = new int[][] { { 0, -1 }, { -1, 0 }, { 0, 1 }, { 1, 0 } };

	/**
	 * Flag for pixels with a known value
	 */
	protected static byte KNOWN = 0;

	/**
	 * Flag for pixels on the boundary
	 */
	protected static byte BAND = 1;

	/**
	 * Flag for pixels with an unknown value
	 */
	protected static byte UNKNOWN = 2;

	/**
	 * The working flag image
	 */
	protected byte[][] flag;

	/**
	 * The space-time map (T)
	 */
	protected FImage timeMap;

	/**
	 * The working heap of pixels to process next
	 */
	protected PriorityQueue<FValuePixel> heap;

	@Override
	protected void initMask() {
		final FImage outside = mask.process(new Dilate(StructuringElement.CROSS), true);

		flag = new byte[mask.height][mask.width];
		timeMap = new FImage(outside.width, outside.height);

		heap = new PriorityQueue<FValuePixel>(10, FValuePixel.ValueComparator.INSTANCE);

		for (int y = 0; y < mask.height; y++) {
			for (int x = 0; x < mask.width; x++) {
				final int band = (int) (outside.pixels[y][x] - mask.pixels[y][x]);
				flag[y][x] = (byte) ((2 * outside.pixels[y][x]) - band);

				if (flag[y][x] == UNKNOWN)
					timeMap.pixels[y][x] = Float.MAX_VALUE;

				if (band != 0) {
					heap.add(new FValuePixel(x, y, timeMap.pixels[y][x]));
				}
			}
		}
	}

	/**
	 * Solve a step of the Eikonal equation.
	 *
	 * @param x1
	 *            x-position of first pixel (diagonally adjacent to the second)
	 * @param y1
	 *            y-position of first pixel (diagonally adjacent to the second)
	 * @param x2
	 *            x-position of second pixel (diagonally adjacent to the first)
	 * @param y2
	 *            y-position of second pixel (diagonally adjacent to the first)
	 * @return the time/distance value of the pixel at (x2, y1)
	 */
	protected float solveEikonalStep(int x1, int y1, int x2, int y2)
	{
		float soln = Float.MAX_VALUE;

		final float t1 = timeMap.pixels[y1][x1];
		final float t2 = timeMap.pixels[y2][x2];

		if (flag[y1][x1] == KNOWN) {
			if (flag[y2][x2] == KNOWN) {
				final float r = (float) Math.sqrt(2 - (t1 - t2) * (t1 - t2));
				float s = (t1 + t2 - r) * 0.5f;

				if (s >= t1 && s >= t2) {
					soln = s;
				} else {
					s += r;

					if (s >= t1 && s >= t2) {
						soln = s;
					}
				}
			} else {
				soln = 1 + t1;
			}
		} else if (flag[y2][x2] == KNOWN) {
			soln = 1 + t2;
		}

		return soln;
	}

	@Override
	public void performInpainting(IMAGE image) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		while (!heap.isEmpty()) {
			final FValuePixel pix = heap.poll();
			final int x = pix.x;
			final int y = pix.y;
			flag[y][x] = KNOWN;

			if ((x <= 1) || (y <= 1) || (x >= width - 2) || (y >= height - 2))
				continue;

			for (final int[] p : DELTAS) {
				final int xp = p[0] + x, yp = p[1] + y;

				if (flag[yp][xp] != KNOWN) {
					timeMap.pixels[yp][xp] = Math.min(Math.min(Math.min(
							solveEikonalStep(xp - 1, yp, xp, yp - 1),
							solveEikonalStep(xp + 1, yp, xp, yp - 1)),
							solveEikonalStep(xp - 1, yp, xp, yp + 1)),
							solveEikonalStep(xp + 1, yp, xp, yp + 1));

					if (flag[yp][xp] == UNKNOWN) {
						flag[yp][xp] = BAND;

						heap.offer(new FValuePixel(xp, yp, timeMap.pixels[yp][xp]));

						inpaint(xp, yp, image);
					}
				}
			}
		}
	}

	/**
	 * Inpaint the specified pixel of the given image.
	 *
	 * @param x
	 *            the x-ordinate of the pixel to paint
	 * @param y
	 *            the y-ordinate of the pixel to paint
	 * @param image
	 *            the image
	 */
	protected abstract void inpaint(int x, int y, IMAGE image);
}
