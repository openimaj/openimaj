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
				listener.process((int) scans[i], (int) scans[i + 1], y);
			}
		}
	}
}
