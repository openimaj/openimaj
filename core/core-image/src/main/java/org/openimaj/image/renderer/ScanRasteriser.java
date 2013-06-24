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
package org.openimaj.image.renderer;

import java.util.Arrays;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;

/**
 * Implementation of the scan-line rasterisation algorithm for filling polygons.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ScanRasteriser {
	/**
	 * Listen for scans.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static interface ScanLineListener {
		/**
		 * Process the given scan
		 * 
		 * @param x1
		 *            starting x (inclusive)
		 * @param x2
		 *            ending x (inclusive)
		 * @param y
		 *            the y ordinate
		 */
		public abstract void process(int x1, int x2, int y);
	}

	private ScanRasteriser() {
		// not instantiable
	}

	/**
	 * The scan-fill algorithm. Scans are reported via the
	 * {@link ScanLineListener}.
	 * 
	 * @param p
	 *            the connected points to rasterise
	 * @param listener
	 *            the {@link ScanLineListener}
	 */
	public static void scanFill(List<Point2d> p, ScanLineListener listener) {
		int nScans;

		final int n = p.size();
		int miny = Integer.MAX_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			final Point2d pt = p.get(i);

			miny = Math.min(miny, Math.round(pt.getY()));
			maxy = Math.max(maxy, Math.round(pt.getY()));
		}

		final float[] scans = new float[n];

		for (int y = miny; y <= maxy; y++) {
			nScans = 0;

			for (int i = 0; i < n; i++) {
				final int index1, index2;
				if (i == 0) {
					index1 = n - 1;
					index2 = 0;
				} else {
					index1 = i - 1;
					index2 = i;
				}

				final Point2d p1 = p.get(index1);
				final Point2d p2 = p.get(index2);

				float y1 = p1.getY();
				float y2 = p2.getY();

				float x1, x2;
				if (y1 < y2) {
					x1 = p1.getX();
					x2 = p2.getX();
				} else if (y1 > y2) {
					y2 = p1.getY();
					y1 = p2.getY();
					x2 = p1.getX();
					x1 = p2.getX();
				} else {
					continue;
				}

				if ((y >= y1) && (y < y2)) {
					scans[nScans++] = (y - y1) * (x2 - x1) / (y2 - y1) + x1;
				} else if ((y == maxy) && (y > y1) && (y <= y2)) {
					scans[nScans++] = (y - y1) * (x2 - x1) / (y2 - y1) + x1;
				}
			}

			Arrays.sort(scans, 0, nScans);

			for (int i = 0; i < nScans; i += 2) {
				if (i + 1 < nScans)
					listener.process(Math.round(scans[i]), Math.round(scans[i + 1]), y);
			}
		}
	}
}
