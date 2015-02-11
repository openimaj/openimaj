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
package org.openimaj.image.processing.morphology;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.algorithm.MaxFilter;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Morphological dilation of connected components and (assumed binary) FImages.
 * See {@link MaxFilter} for greyscale dilation.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Dilate implements ConnectedComponentProcessor, KernelProcessor<Float, FImage> {
	protected StructuringElement element;
	protected int cx;
	protected int cy;
	protected int sw;
	protected int sh;

	/**
	 * Construct the dilate operator with the given structuring element
	 *
	 * @param se
	 *            the structuring element
	 */
	public Dilate(StructuringElement se) {
		this.element = se;

		final int[] sz = se.size();
		sw = sz[0];
		sh = sz[1];
		cx = sw / 2;
		cy = sh / 2;
	}

	/**
	 * Construct the dilate operator with a BOX structuring element
	 */
	public Dilate() {
		this(StructuringElement.BOX);
	}

	@Override
	public void process(ConnectedComponent cc) {
		// Dilate a connected component
		final Rectangle cc_bb = cc.calculateRegularBoundingBox();

		final Set<Pixel> newPixels = new HashSet<Pixel>();
		for (int j = (int) (cc_bb.y - sh); j <= cc_bb.y + sh + cc_bb.height; j++) {
			for (int i = (int) (cc_bb.x - sw); i <= cc_bb.x + sw + cc_bb.width; i++) {
				final Pixel p = new Pixel(i, j);

				if (element.intersect(p, cc.getPixels()).size() >= 1) {
					newPixels.add(p);
				}
			}
		}

		cc.getPixels().addAll(newPixels);
	}

	@Override
	public int getKernelHeight() {
		return sh;
	}

	@Override
	public int getKernelWidth() {
		return sw;
	}

	@Override
	public Float processKernel(FImage patch) {
		for (final Pixel p : element.positive) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 1) {
				return 1f;
			}
		}

		for (final Pixel p : element.negative) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh && patch.pixels[py][px] == 0)
				return 1f;
		}

		return patch.pixels[cy][cx];
	}

	/**
	 * Apply dilation some number of times to an image with the default
	 * {@link StructuringElement#BOX} element
	 *
	 * @param img
	 *            the image
	 * @param times
	 *            the number of times to apply the dilation
	 */
	public static void dilate(FImage img, int times) {
		final Dilate d = new Dilate();
		for (int i = 0; i < times; i++)
			img.processInplace(d);
	}
}
