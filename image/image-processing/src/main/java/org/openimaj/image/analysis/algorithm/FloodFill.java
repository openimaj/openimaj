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

import java.util.LinkedHashSet;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Flood-fill of @link{FImage}s or @link{MBFImage}s.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <I>
 *            type of image
 */
public class FloodFill<I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>>
		implements
		ImageAnalyser<I>
{
	FImage flooded;
	Pixel startPixel;
	float threshold;

	/**
	 * Construct flood-fill processor with the given threshold and starting
	 * coordinate.
	 *
	 * @param x
	 *            x-coordinate of start pixel
	 * @param y
	 *            y-coordinate of start pixel
	 * @param threshold
	 *            threshold for determing whether a pixel should be flooded
	 */
	public FloodFill(int x, int y, float threshold) {
		this.startPixel = new Pixel(x, y);
		this.threshold = threshold;
	}

	/**
	 * Construct flood-fill processor with the given threshold and starting
	 * coordinate.
	 *
	 * @param startPixel
	 *            coordinate of start pixel
	 * @param threshold
	 *            threshold for determing whether a pixel should be flooded
	 */
	public FloodFill(Pixel startPixel, float threshold) {
		this.startPixel = startPixel;
		this.threshold = threshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void analyseImage(I image) {
		flooded = floodFill((Image<?, ?>) image, startPixel, threshold);
	}

	/**
	 * Get the binary flooded image map
	 * 
	 * @return flooded image
	 */
	public FImage getFlooded() {
		return flooded;
	}

	protected static <T> boolean accept(Image<T, ?> image, Pixel n, T initial, float threshold) {
		if (image instanceof FImage) {
			return Math.abs((Float) initial - (Float) image.getPixel(n.x, n.y)) < threshold;
		} else if (image instanceof MBFImage) {
			final Float[] finit = (Float[]) initial;
			final Float[] fpix = (Float[]) image.getPixel(n.x, n.y);
			float accum = 0;

			for (int i = 0; i < finit.length; i++)
				accum += (finit[i] - fpix[i]) * (finit[i] - fpix[i]);

			return Math.sqrt(accum) < threshold;
		} else {
			throw new RuntimeException("unsupported image type");
		}
	}

	/**
	 * Flood-fill an image from the given starting pixel position with the given
	 * threshold.
	 * 
	 * @param <T>
	 *            The pixel type of the image
	 * @param image
	 *            the image
	 * @param startx
	 *            the x-coordinate of the start pixel
	 * @param starty
	 *            the y-coordinate of the start pixel
	 * @param threshold
	 *            the threshold for determining with a pixel should be filled
	 * @return a binary @link{FImage} with filled pixels from the input set to 1
	 */
	public static <T> FImage floodFill(Image<T, ?> image, int startx, int starty, float threshold) {
		return floodFill(image, new Pixel(startx, starty), threshold);
	}

	/**
	 * Flood-fill an image from the given starting pixel position with the given
	 * threshold.
	 * 
	 * @param <T>
	 *            The pixel type of the image
	 * @param image
	 *            the image
	 * @param start
	 *            the start pixel
	 * @param threshold
	 *            the threshold for determining with a pixel should be filled
	 * @return a binary @link{FImage} with filled pixels from the input set to 1
	 */
	public static <T> FImage floodFill(Image<T, ?> image, Pixel start, float threshold) {
		final FImage output = new FImage(image.getWidth(), image.getHeight());

		// Flood-fill (node, target-color, replacement-color):
		// 1. Set Q to the empty queue.
		final LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();

		// 2. If the color of node is not equal to target-color, return.
		// if (image.pixels[start.y][start.x] == 0) return cc;
		final T initial = image.getPixel(start.x, start.y);

		// 3. Add node to Q.
		queue.add(start);

		// 4. For each element n of Q:
		while (queue.size() > 0) {
			// Pixel n = queue.poll();
			final Pixel n = queue.iterator().next();
			queue.remove(n);

			// 5. If the color of n is equal to target-color:
			if (accept(image, n, initial, threshold) && output.pixels[n.y][n.x] != 1) {
				// 6. Set w and e equal to n.
				int e = n.x, w = n.x;
				// 7. Move w to the west until the color of the node to the west
				// of w no longer matches target-color.
				while (w > 0 && accept(image, new Pixel(w - 1, n.y), initial, threshold))
					w--;

				// 8. Move e to the east until the color of the node to the east
				// of e no longer matches target-color.
				while (e < image.getWidth() - 1 && accept(image, new Pixel(e + 1, n.y), initial, threshold))
					e++;

				// 9. Set the color of nodes between w and e to
				// replacement-color.
				for (int i = w; i <= e; i++) {
					output.pixels[n.y][i] = 1;

					// 10. For each node n between w and e:
					final int north = n.y - 1;
					final int south = n.y + 1;
					// 11. If the color of the node to the north of n is
					// target-color, add that node to Q.
					if (north >= 0 && accept(image, new Pixel(i, north), initial, threshold)
							&& output.pixels[north][i] != 1)
						queue.add(new Pixel(i, north));
					// If the color of the node to the south of n is
					// target-color, add that node to Q.
					if (south < image.getHeight() && accept(image, new Pixel(i, south), initial, threshold)
							&& output.pixels[south][i] != 1)
						queue.add(new Pixel(i, south));
				}
				// 12. Continue looping until Q is exhausted.
			}
		}
		// 13. Return.
		return output;
	}
}
