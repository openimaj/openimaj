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
package org.openimaj.image.objectdetection.haar;

import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
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
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Viola, P.", "Jones, M." },
		title = "Rapid object detection using a boosted cascade of simple features",
		year = "2001",
		booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on",
		pages = { " I", "511 ", " I", "518 vol.1" },
		number = "",
		volume = "1",
		customData = {
				"keywords", " AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
				"doi", "10.1109/CVPR.2001.990517",
				"ISSN", "1063-6919 "
		})
public abstract class HaarFeature {
	/**
	 * The rectangles that make up this feature.
	 */
	public WeightedRectangle[] rects;

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

	/**
	 * Construct a feature with the given parameters.
	 * 
	 * @param tilted
	 *            is the feature tilted?
	 * @param x0
	 *            x-ordinate of top-left of first rectangle
	 * @param y0
	 *            y-ordinate of top-left of first rectangle
	 * @param w0
	 *            width of first rectangle
	 * @param h0
	 *            height of first rectangle
	 * @param wt0
	 *            weight of first rectangle
	 * @param x1
	 *            x-ordinate of top-left of second rectangle
	 * @param y1
	 *            y-ordinate of top-left of second rectangle
	 * @param w1
	 *            width of second rectangle
	 * @param h1
	 *            height of second rectangle
	 * @param wt1
	 *            weight of second rectangle
	 * @return the feature
	 */
	public static HaarFeature create(boolean tilted,
			int x0, int y0, int w0, int h0, float wt0,
			int x1, int y1, int w1, int h1, float wt1)
	{
		final WeightedRectangle[] rects = new WeightedRectangle[2];
		rects[0] = new WeightedRectangle(x0, y0, w0, h0, wt0);
		rects[1] = new WeightedRectangle(x1, y1, w1, h1, wt1);

		return tilted ? new TiltedFeature(rects) : new NormalFeature(rects);
	}

	/**
	 * Construct a feature with the given parameters.
	 * 
	 * @param tilted
	 *            is the feature tilted?
	 * @param x0
	 *            x-ordinate of top-left of first rectangle
	 * @param y0
	 *            y-ordinate of top-left of first rectangle
	 * @param w0
	 *            width of first rectangle
	 * @param h0
	 *            height of first rectangle
	 * @param wt0
	 *            weight of first rectangle
	 * @param x1
	 *            x-ordinate of top-left of second rectangle
	 * @param y1
	 *            y-ordinate of top-left of second rectangle
	 * @param w1
	 *            width of second rectangle
	 * @param h1
	 *            height of second rectangle
	 * @param wt1
	 *            weight of second rectangle
	 * @param x2
	 *            x-ordinate of top-left of third rectangle
	 * @param y2
	 *            y-ordinate of top-left of third rectangle
	 * @param w2
	 *            width of third rectangle
	 * @param h2
	 *            height of third rectangle
	 * @param wt2
	 *            weight of third rectangle
	 * @return the feature
	 */
	public static HaarFeature create(boolean tilted,
			int x0, int y0, int w0, int h0, float wt0,
			int x1, int y1, int w1, int h1, float wt1,
			int x2, int y2, int w2, int h2, float wt2)
	{
		final WeightedRectangle[] rects = new WeightedRectangle[3];
		rects[0] = new WeightedRectangle(x0, y0, w0, h0, wt0);
		rects[1] = new WeightedRectangle(x1, y1, w1, h1, wt1);
		rects[2] = new WeightedRectangle(x2, y2, w2, h2, wt2);

		return tilted ? new TiltedFeature(rects) : new NormalFeature(rects);
	}
}
