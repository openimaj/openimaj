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
package org.openimaj.image.feature.local.keypoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * Helpers for visualising (SIFT) interest points.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 * @param <Q>
 */
public class KeypointVisualizer<T, Q extends Image<T, Q> & SinglebandImageProcessor.Processable<Float, FImage, Q>> {
	Q image;
	List<? extends Keypoint> keypoints;

	/**
	 * Construct the visualiser with the given image and keypoints.
	 * 
	 * @param image
	 *            the image
	 * @param keys
	 *            the keypoints
	 */
	public KeypointVisualizer(Q image, List<? extends Keypoint> keys) {
		this.image = image;
		this.keypoints = keys;
	}

	/**
	 * Extract the oriented sampling patches used in the construction of the
	 * keypoints. The patches are normalised to the given fixed size.
	 * 
	 * @param dim
	 *            the patch size.
	 * @return images depicting the sampling patches of each feature
	 */
	public Map<Keypoint, Q> getPatches(int dim) {
		final Map<Keypoint, Q> patches = new HashMap<Keypoint, Q>();
		final Map<Float, Q> blurred = new HashMap<Float, Q>();

		for (final Keypoint k : keypoints) {
			// blur image
			if (!blurred.containsKey(k.scale)) {
				blurred.put(k.scale, image.process(new FGaussianConvolve(k.scale)));
			}
			final Q blur = blurred.get(k.scale);

			// make empty patch
			final int sz = (int) (2 * 2 * 3 * k.scale);
			final Q patch = image.newInstance(sz, sz);

			// extract pixels
			for (int y = 0; y < sz; y++) {
				for (int x = 0; x < sz; x++) {
					final double xbar = x - sz / 2.0;
					final double ybar = y - sz / 2.0;

					final double xx = (xbar * Math.cos(-k.ori) + ybar * Math.sin(-k.ori)) + k.x;
					final double yy = (-xbar * Math.sin(-k.ori) + ybar * Math.cos(-k.ori)) + k.y;

					patch.setPixel(x, y, blur.getPixelInterp(xx, yy));
				}
			}

			patches.put(k, patch.processInplace(new ResizeProcessor(dim, dim)));
		}

		return patches;
	}

	/**
	 * Draw the sampling boxes on an image.
	 * 
	 * @param boxColour
	 *            the sampling box colour
	 * @param circleColour
	 *            the scale-circle colour
	 * @return an image showing the features.
	 */
	public Q drawPatches(T boxColour, T circleColour) {
		return drawPatchesInplace(image.clone(), keypoints, boxColour, circleColour);
	}

	/**
	 * Draw the SIFT features onto an image. The features are visualised as
	 * circles with orientation lines showing the scale and orientation, and
	 * with oriented squares showing the sampling region.
	 * 
	 * The colours of the squares and circles is controlled individually.
	 * Setting the colour to null will cause the shape not to be displayed.
	 * 
	 * The sizes of the drawn shapes assume the default SIFT settings described
	 * by Lowe. If the parameters used to find the keypoints have been changed,
	 * then the features might not be drawn at the correct size.
	 * 
	 * @param <T>
	 *            the pixel type
	 * @param <Q>
	 *            the image type
	 * @param image
	 *            the image to draw on
	 * @param keypoints
	 *            the features to draw
	 * @param boxColour
	 *            the colour of the sampling boxes
	 * @param circleColour
	 *            the colour of the scale circle
	 * @return the input image
	 */
	public static <T, Q extends Image<T, Q> & SinglebandImageProcessor.Processable<Float, FImage, Q>>
			Q
			drawPatchesInplace(Q image, List<? extends Keypoint> keypoints, T boxColour, T circleColour)
	{
		final ImageRenderer<T, Q> renderer = image.createRenderer();

		for (final Keypoint k : keypoints) {
			if (boxColour != null) {
				renderer.drawPolygon(getSamplingBox(k), boxColour);
			}

			if (circleColour != null) {
				renderer.drawLine((int) k.x, (int) k.y, -k.ori, (int) k.scale * 5, circleColour);
				renderer.drawShape(new Circle(k.x, k.y, k.scale), circleColour);
			}
		}

		return image;
	}

	/**
	 * Draw the centre point of the keypoints on an image
	 * 
	 * @param col
	 *            the colour to draw with
	 * @return the image
	 */
	public Q drawCenter(T col) {
		final Q output = image.clone();
		final ImageRenderer<T, Q> renderer = output.createRenderer();

		renderer.drawPoints(keypoints, col, 2);
		return output;
	}

	/**
	 * Get the sampling area of an single feature as a polygon.
	 * 
	 * @param k
	 *            the feature
	 * @return the polygon representing the sampling area.
	 */
	public static Polygon getSamplingBox(Keypoint k) {
		return getSamplingBox(k, 0);
	}

	/**
	 * Get the sampling area of an single feature as a polygon.
	 * 
	 * @param k
	 *            the feature
	 * @param scincr
	 *            the scaling factor to apply to the sampling area
	 * @return the polygon representing the sampling area.
	 */
	public static Polygon getSamplingBox(Keypoint k, float scincr) {
		final List<Point2d> vertices = new ArrayList<Point2d>();

		vertices.add(new Point2dImpl(k.x - (scincr + 2 * 3 * k.scale), k.y - (scincr + 2 * 3 * k.scale)));
		vertices.add(new Point2dImpl(k.x + (scincr + 2 * 3 * k.scale), k.y - (scincr + 2 * 3 * k.scale)));
		vertices.add(new Point2dImpl(k.x + (scincr + 2 * 3 * k.scale), k.y + (scincr + 2 * 3 * k.scale)));
		vertices.add(new Point2dImpl(k.x - (scincr + 2 * 3 * k.scale), k.y + (scincr + 2 * 3 * k.scale)));

		final Polygon poly = new Polygon(vertices);

		poly.rotate(new Point2dImpl(k.x, k.y), -k.ori);

		return poly;
	}
}
