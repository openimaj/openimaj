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
package org.openimaj.image.processing.algorithm;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;

/**
 * Methods and statically defined templates for defining the support of local
 * image filters.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FilterSupport {

	/**
	 * Offsets for using a 3x3 cross shaped mask to select pixels for computing
	 * median.
	 */
	public static final Set<Pixel> CROSS_3x3 = new HashSet<Pixel>();
	static {
		CROSS_3x3.add(new Pixel(0, -1));
		CROSS_3x3.add(new Pixel(-1, 0));
		CROSS_3x3.add(new Pixel(0, 0));
		CROSS_3x3.add(new Pixel(1, 0));
		CROSS_3x3.add(new Pixel(0, 1));
	}

	/**
	 * Offsets for using a 3x3 blocked shaped mask to select pixels for
	 * computing median.
	 */
	public static final Set<Pixel> BLOCK_3x3 = createBlockSupport(3, 3);

	/**
	 * Create a a rectangular support.
	 * 
	 * @param width
	 *            the width of the support
	 * @param height
	 *            the height of the support
	 * @return the support
	 */
	public static final Set<Pixel> createBlockSupport(final int width, final int height) {
		final HashSet<Pixel> indices = new HashSet<Pixel>(width * height);

		final int startX = -width / 2;
		final int startY = -height / 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				indices.add(new Pixel(startX + x, startY + y));
			}
		}

		return indices;
	}

	/**
	 * Test whether the given support is a centred block
	 * 
	 * @param support
	 *            the support
	 * @return true if block, false otherwise
	 */
	public final static boolean isBlockSupport(final Set<Pixel> support) {
		final int sw = getSupportWidth(support);
		final int sh = getSupportHeight(support);

		return sw * sh == support.size() && isCentred(support);
	}

	private static boolean isCentred(Set<Pixel> support) {
		final ConnectedComponent cc = new ConnectedComponent(support);
		final Pixel cp = cc.calculateCentroidPixel();
		return cp.x == 0 && cp.y == 0;
	}

	/**
	 * Get the width of the support region
	 * 
	 * @param support
	 *            the region
	 * @return the width
	 */
	public final static int getSupportWidth(final Set<Pixel> support) {
		int min = Integer.MAX_VALUE;
		int max = -Integer.MAX_VALUE;

		for (final Pixel p : support) {
			min = Math.min(min, p.x);
			max = Math.max(max, p.x);
		}

		return max - min + 1;
	}

	/**
	 * Get the height of the support region
	 * 
	 * @param support
	 *            the region
	 * @return the height
	 */
	public final static int getSupportHeight(final Set<Pixel> support) {
		int min = Integer.MAX_VALUE;
		int max = -Integer.MAX_VALUE;

		for (final Pixel p : support) {
			min = Math.min(min, p.y);
			max = Math.max(max, p.y);
		}

		return max - min + 1;
	}
}
