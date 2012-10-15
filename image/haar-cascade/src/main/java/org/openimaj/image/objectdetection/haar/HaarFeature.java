package org.openimaj.image.objectdetection.haar;

import java.util.List;

import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;

/**
 * Class describing a Haar-like feature. The features are typically built from
 * two or three overlapping rectangles, and can represent edges, lines and
 * centre-surround features.
 * <p>
 * The response of applying the feature to a specific point on an image (with a
 * specific scaling) can be efficiently calculated using summed area tables.
 * <p>
 * Internally this implementation caches a scaled version of each rectangle for
 * a given detection scale.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public abstract class HaarFeature {
	WeightedRectangle[] rects;

	private final float correctionFactor;
	protected WeightedRectangle[] cachedRects;

	/**
	 * Construct a new feature
	 * 
	 * @param rects
	 * @param correctionFactor
	 */
	private HaarFeature(WeightedRectangle[] rects, final float correctionFactor) {
		this.rects = rects;
		this.correctionFactor = correctionFactor;

		cachedRects = new WeightedRectangle[rects.length];
		for (int i = 0; i < cachedRects.length; i++) {
			cachedRects[i] = new WeightedRectangle(0, 0, 0, 0, 0);
		}
	}

	final void updateCaches(StageTreeClassifier cascade) {
		setScale(cascade.cachedScale, cascade.cachedInvArea);
	}

	/**
	 * Set the current detection scale, setting up the internal caches
	 * appropriately.
	 * 
	 * @param scale
	 *            the scale
	 * @param invArea
	 *            the inverse of the detector area
	 */
	public final void setScale(float scale, float invArea) {
		double sum0 = 0;
		double area0 = 0;

		int base_w = Integer.MAX_VALUE;
		int base_h = Integer.MAX_VALUE;
		int new_base_w = 0;
		int new_base_h = 0;
		int kx;
		int ky;
		boolean flagx = false;
		boolean flagy = false;
		int x0 = 0;
		int y0 = 0;

		final WeightedRectangle firstArea = rects[0];
		for (final WeightedRectangle r : rects) {
			if ((r.width - 1) >= 0) {
				base_w = Math.min(base_w, (r.width - 1));
			}
			if ((r.x - firstArea.x - 1) >= 0) {
				base_w = Math.min(base_w, (r.x - firstArea.x - 1));
			}
			if ((r.height - 1) >= 0) {
				base_h = Math.min(base_h, (r.height - 1));
			}
			if ((r.y - firstArea.y - 1) >= 0) {
				base_h = Math.min(base_h, (r.y - firstArea.y - 1));
			}
		}

		base_w += 1;
		base_h += 1;
		kx = firstArea.width / base_w;
		ky = firstArea.height / base_h;

		if (kx <= 0) {
			flagx = true;
			new_base_w = Math.round(firstArea.width * scale) / kx;
			x0 = Math.round(firstArea.x * scale);
		}

		if (ky <= 0) {
			flagy = true;
			new_base_h = Math.round(firstArea.height * scale)
					/ ky;
			y0 = Math.round(firstArea.y * scale);
		}

		for (int k = 0; k < rects.length; k++) {
			final WeightedRectangle r = rects[k];
			int x;
			int y;
			int width;
			int height;
			float correction_ratio;

			if (flagx) {
				x = (r.x - firstArea.x) * new_base_w / base_w + x0;
				width = r.width * new_base_w / base_w;
			} else {
				x = Math.round(r.x * scale);
				width = Math.round(r.width * scale);
			}

			if (flagy) {
				y = (r.y - firstArea.y) * new_base_h / base_h + y0;
				height = r.height * new_base_h / base_h;
			} else {
				y = Math.round(r.y * scale);
				height = Math.round(r.height * scale);
			}

			correction_ratio = correctionFactor * invArea;

			cachedRects[k].weight = (rects[k].weight * correction_ratio);
			cachedRects[k].x = x;
			cachedRects[k].y = y;
			cachedRects[k].width = width;
			cachedRects[k].height = height;

			if (k == 0) {
				area0 = width * height;
			} else {
				sum0 += cachedRects[k].weight * width * height;
			}
		}

		cachedRects[0].weight = (float) (-sum0 / area0);
	}

	/**
	 * Compute the response of this feature at the given location. The scale of
	 * the feature must have previously been set through a call to
	 * {@link #setScale(float, float)} (this is only required once per scale).
	 * 
	 * @param sat
	 *            the summed area table(s). If there are tilted features, then
	 *            this must include the tilted SAT.
	 * @param x
	 *            the x-ordinate for the window being tested
	 * @param y
	 *            the y-ordinate for the window being tested
	 * @return the response to the feature
	 */
	public abstract float computeResponse(SummedSqTiltAreaTable sat, int x, int y);

	static class TiltedFeature extends HaarFeature {
		public TiltedFeature(WeightedRectangle[] rects) {
			super(rects, 2f);
		}

		@Override
		public float computeResponse(SummedSqTiltAreaTable sat, int rx, int ry) {
			float total = 0;
			for (int i = 0; i < cachedRects.length; i++) {
				final WeightedRectangle rect = cachedRects[i];

				final int x = rx + rect.x;
				final int y = ry + rect.y;
				final int width = rect.width;
				final int height = rect.height;

				final float p0 = sat.tiltSum.pixels[y][x];
				final float p1 = sat.tiltSum.pixels[y + height][x - height];
				final float p2 = sat.tiltSum.pixels[y + width][x + width];
				final float p3 = sat.tiltSum.pixels[y + width + height][x + width - height];

				final float regionSum = p0 - p1 - p2 + p3;

				total += regionSum * rect.weight;
			}

			return total;
		}
	}

	static class NormalFeature extends HaarFeature {
		public NormalFeature(WeightedRectangle[] rects) {
			super(rects, 1f);
		}

		@Override
		public float computeResponse(SummedSqTiltAreaTable sat, int rx, int ry) {
			float total = 0;
			for (int i = 0; i < cachedRects.length; i++) {
				final WeightedRectangle rect = cachedRects[i];

				final int x = rx + rect.x;
				final int y = ry + rect.y;
				final int width = rect.width;
				final int height = rect.height;

				final int yh = y + height;
				final int xw = x + width;

				final float regionSum = sat.sum.pixels[yh][xw] - sat.sum.pixels[yh][x]
						- sat.sum.pixels[y][xw] + sat.sum.pixels[y][x];

				total += regionSum * rect.weight;
			}

			return total;
		}
	}

	/**
	 * Create a feature from the given data. The specific type of feature
	 * created depends on whether or not the feature is tilted.
	 * 
	 * @param rectList
	 *            the rectangles defining the feature
	 * @param tilted
	 *            is the feature tilted?
	 * @return the new {@link HaarFeature} object.
	 */
	public static HaarFeature create(List<WeightedRectangle> rectList, boolean tilted) {
		final WeightedRectangle[] rects = rectList.toArray(new WeightedRectangle[rectList.size()]);

		if (tilted)
			return new TiltedFeature(rects);

		return new NormalFeature(rects);
	}
}
