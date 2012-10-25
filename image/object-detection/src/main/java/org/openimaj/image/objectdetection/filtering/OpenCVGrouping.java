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
package org.openimaj.image.objectdetection.filtering;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectIntPair;

/**
 * Filter to perform the grouping of detection rectangles in the way OpenCV
 * does. Basically the groups are formed by grouping together all the rectangles
 * that overlap by a certain amount. For each group, the mean rectangle is
 * calculated. Groups that have too little support are removed, as are groups
 * that are enclosed by other groups.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public final class OpenCVGrouping implements DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> {
	/**
	 * The default eps value for determining whether two rectangles overlap
	 * enough to be considered as being of the same group.
	 */
	public static final float DEFAULT_EPS = 0.2f;

	/**
	 * The default value for the minimum number of rectangles required within a
	 * group.
	 */
	public static final int DEFAULT_MINIMUM_SUPPORT = 3;

	float eps;
	int minSupport;

	/**
	 * Construct a new {@link OpenCVGrouping} with the given parameters.
	 * 
	 * @param eps
	 *            The eps value for determining whether two rectangles overlap
	 *            enough to be considered as being of the same group.
	 * @param minSupport
	 *            The minimum number of rectangles required within a group.
	 *            Groups with less than this number of rectangles will be
	 *            removed.
	 */
	public OpenCVGrouping(float eps, int minSupport) {
		this.eps = eps;
		this.minSupport = minSupport;
	}

	/**
	 * Construct a new {@link OpenCVGrouping} with the given minimum support
	 * number. The {@link #DEFAULT_EPS} value is used for the eps.
	 * 
	 * @param minSupport
	 *            The minimum number of rectangles required within a group.
	 *            Groups with less than this number of rectangles will be
	 *            removed.
	 */
	public OpenCVGrouping(int minSupport) {
		this(DEFAULT_EPS, minSupport);
	}

	/**
	 * Construct a new {@link OpenCVGrouping} with the default values of
	 * {@link #DEFAULT_EPS} for the eps and {@link #DEFAULT_MINIMUM_SUPPORT} for
	 * the support.
	 */
	public OpenCVGrouping() {
		this(DEFAULT_EPS, DEFAULT_MINIMUM_SUPPORT);
	}

	@Override
	public List<ObjectIntPair<Rectangle>> apply(List<Rectangle> input) {
		final int[] classes = new int[input.size()];
		final int nClasses = partition(input, classes);

		// Compute the mean rectangle per class
		final Rectangle[] meanRects = new Rectangle[nClasses]; // mean rect
																// storage per
																// class
		final int[] rectCounts = new int[nClasses]; // number of rectangles per
													// class
		for (int i = 0; i < nClasses; i++) {
			meanRects[i] = new Rectangle(0, 0, 0, 0);
		}

		for (int i = 0; i < classes.length; i++) {
			final int cls = classes[i];

			meanRects[cls].x += input.get(i).x;
			meanRects[cls].y += input.get(i).y;
			meanRects[cls].width += input.get(i).width;
			meanRects[cls].height += input.get(i).height;
			rectCounts[cls]++;
		}

		for (int i = 0; i < nClasses; i++) {
			final Rectangle r = meanRects[i];
			final float s = 1.0f / rectCounts[i];
			meanRects[i] = new Rectangle(Math.round(r.x * s), Math.round(r.y * s), Math.round(r.width * s),
					Math.round(r.height * s));
		}

		// now filter out any classes that have too few rectangles, or is a
		// small rectangles inclosed by another class.
		final List<ObjectIntPair<Rectangle>> rectList = new ArrayList<ObjectIntPair<Rectangle>>();
		for (int i = 0; i < nClasses; i++) {
			final Rectangle r1 = meanRects[i];
			final int n1 = rectCounts[i];

			if (n1 <= minSupport)
				continue;

			// filter out small face rectangles inside large rectangles
			int j;
			for (j = 0; j < nClasses; j++) {
				final int n2 = rectCounts[j];

				if (j == i || n2 <= minSupport)
					continue;
				final Rectangle r2 = meanRects[j];

				final int dx = Math.round(r2.width * eps);
				final int dy = Math.round(r2.height * eps);

				if (i != j &&
						r1.x >= r2.x - dx &&
						r1.y >= r2.y - dy &&
						r1.x + r1.width <= r2.x + r2.width + dx &&
						r1.y + r1.height <= r2.y + r2.height + dy &&
						(n2 > Math.max(3, n1) || n1 < 3))
					break;
			}

			if (j == nClasses) {
				rectList.add(new ObjectIntPair<Rectangle>(r1, n1));
			}
		}

		return rectList;
	}

	private int partition(List<Rectangle> rects, int[] classes) {
		int numClasses = 0;

		for (int i = 0; i < rects.size(); i++) {
			boolean found = false;
			for (int j = 0; j < i; j++) {
				if (equals(rects.get(j), rects.get(i))) {
					found = true;
					classes[i] = classes[j];
				}
			}
			if (!found) {
				classes[i] = numClasses;
				numClasses++;
			}
		}

		return numClasses;
	}

	private boolean equals(Rectangle r1, Rectangle r2) {
		final float delta = eps * (Math.min(r1.width, r2.width) + Math.min(r1.height, r2.height)) * 0.5f;

		return (Math.abs(r1.x - r2.x) <= delta &&
					Math.abs(r1.y - r2.y) <= delta &&
					Math.abs(r1.x + r1.width - r2.x - r2.width) <= delta && Math.abs(r1.y + r1.height - r2.y - r2.height) <= delta);
	}
}
