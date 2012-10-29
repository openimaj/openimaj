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
package org.openimaj.image.feature.local.affine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.transform.AffineParams;
import org.openimaj.image.processing.transform.AffineSimulation;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.ScaleSpacePoint;

/**
 * Base class for local feature detectors/extractors that use affine simulations
 * in order to increase detections and improve performance with respect to
 * affine change.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <Q>
 *            Type of interest point list
 * @param <T>
 *            Type of interest point
 * @param <I>
 *            Concrete subclass of {@link Image}
 * @param <P>
 *            Pixel type
 * 
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Morel, Jean-Michel", "Yu, Guoshen" },
		title = "{ASIFT: A New Framework for Fully Affine Invariant Image Comparison}",
		year = "2009",
		journal = "SIAM J. Img. Sci.",
		publisher = "Society for Industrial and Applied Mathematics")
public abstract class AffineSimulationExtractor<Q extends List<T>, T extends ScaleSpacePoint, I extends Image<P, I> & SinglebandImageProcessor.Processable<Float, FImage, I>, P>
{
	protected static final float PI = 3.141592654f;
	protected float BorderFact = 6 * (float) Math.sqrt(2);

	/**
	 * The list of all detected interest points
	 */
	public Q allInterestPoints;

	/**
	 * The detected interest points, grouped by simulation
	 */
	public Map<AffineParams, Q> mappedInterestPoints;

	/**
	 * The list of simulation parameters in the order the simulations were
	 * performed
	 */
	public List<AffineParams> simulationOrder;

	/**
	 * Detect and describe the local features in the given (transformed) image.
	 * The returned features should be in the coordinate system of the given
	 * image; they will be automatically filtered and transformed back to the
	 * original coordinate system.
	 * 
	 * @param image
	 *            the image in which to detect features
	 * @return the local interest features
	 */
	protected abstract Q detectFeatures(I image);

	/**
	 * Construct a new list capable of holding the extracted features.
	 * 
	 * @return a new list
	 */
	protected abstract Q newList();

	/**
	 * Get the list of all the detected features
	 * 
	 * @return the list of all detected features
	 */
	public Q getFeatures() {
		return allInterestPoints;
	}

	/**
	 * Detect features in the given image, computing the simulations based on
	 * the given number of tilts.
	 * 
	 * @param image
	 *            the image
	 * @param num_of_tilts
	 *            the number of tilt simulations
	 * @throws IllegalArgumentException
	 *             if the number of tilts < 1
	 */
	public void detectFeatures(I image, int num_of_tilts) {
		if (num_of_tilts < 1) {
			throw new IllegalArgumentException("Number of tilts num_tilt should be equal or larger than 1.");
		}

		// setup the storage
		allInterestPoints = newList();
		mappedInterestPoints = new HashMap<AffineParams, Q>();
		simulationOrder = new ArrayList<AffineParams>();

		final int num_rot_t2 = 10; // num rotations at final tilt
		final float t_min = 1;
		final float t_k = (float) Math.sqrt(2);

		for (int tt = 1; tt <= num_of_tilts; tt++) {
			final float t = t_min * (float) Math.pow(t_k, tt - 1);
			AffineParams addedParams = null;
			if (t == 1) {
				addedParams = new AffineParams(0, t);
				// lowe_sift_yu_nodouble_v1(image_tmp1,keypoints,display,verb);
				final Q keypoints = detectFeatures(image.clone());

				/* Store the number of keypoints */
				mappedInterestPoints.put(addedParams, keypoints);

				/* Store the keypoints */
				allInterestPoints.addAll(keypoints);
				simulationOrder.add(addedParams);
			} else {
				int num_rots = Math.round(num_rot_t2 * t / 2);

				if (num_rots % 2 == 1) {
					num_rots = num_rots + 1;
				}
				num_rots = num_rots / 2;

				final float delta_theta = PI / num_rots;

				for (int rr = 1; rr <= num_rots; rr++) {
					final float theta = delta_theta * (rr - 1);

					final I image_tmp1 = AffineSimulation.transformImage(image, theta, t);
					final Q keypoints = detectFeatures(image_tmp1);

					filterEdgesTransformed(keypoints, theta, image, 1.0f / t);

					AffineSimulation.transformToOriginal(keypoints, image, theta, t);

					addedParams = new AffineParams(theta, t);
					mappedInterestPoints.put(addedParams, keypoints);
					allInterestPoints.addAll(keypoints);
					simulationOrder.add(addedParams);
				}
			}
		}
	}

	/**
	 * Detect features from a single simulation.
	 * 
	 * @param image
	 *            the image
	 * @param params
	 *            the simulation parameters
	 * @return the detected features
	 */
	public Q detectFeatures(I image, AffineParams params) {
		return detectFeatures(image, params.theta, params.tilt);
	}

	/**
	 * Detect features from a single simulation.
	 * 
	 * @param image
	 *            the image
	 * @param theta
	 *            the rotation
	 * @param tilt
	 *            the amount of tilt
	 * 
	 * @return the detected features
	 */
	public Q detectFeatures(I image, float theta, float tilt) {
		final I image_tmp1 = AffineSimulation.transformImage(image, theta, tilt);

		final Q keypoints = detectFeatures(image_tmp1);

		filterEdgesTransformed(keypoints, theta, image, 1.0f / tilt);
		AffineSimulation.transformToOriginal(keypoints, image, theta, tilt);

		return keypoints;
	}

	protected void filterEdgesTransformed(Q keypoints, float theta, I image, float t2) {
		float x1, y1, x2, y2, x3, y3, x4, y4;
		final List<T> keys_to_remove = new ArrayList<T>();

		/* Store the keypoints */
		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();
		for (int cc = 0; cc < keypoints.size(); cc++) {
			/*
			 * check if the keypoint is located on the boundary of the
			 * parallelogram
			 */
			/* coordinates of the keypoint */
			final float x0 = keypoints.get(cc).getX();
			final float y0 = keypoints.get(cc).getY();
			final float scale1 = keypoints.get(cc).getScale();

			final float sin_theta = (float) Math.sin(theta);
			final float cos_theta1 = (float) Math.cos(theta);

			/* the coordinates of the 4 corners of the parallelogram */
			if (theta <= PI / 2.0) {
				/* theta1 = theta * PI / 180; */
				x1 = imageHeight * sin_theta;
				y1 = 0;
				y2 = imageWidth * sin_theta;
				x3 = imageWidth * cos_theta1;
				x4 = 0;
				y4 = imageHeight * cos_theta1;
				x2 = x1 + x3;
				y3 = y2 + y4;

				/*
				 * Note that the vertical direction goes from top to bottom!!!
				 * The calculation above assumes that the vertical direction
				 * goes from the bottom to top. Thus the vertical coordinates
				 * need to be reversed!!!
				 */
				y1 = y3 - y1;
				y2 = y3 - y2;
				y4 = y3 - y4;
				y3 = 0;
			} else {
				/* theta1 = theta * PI / 180; */
				y1 = -imageHeight * cos_theta1;
				x2 = imageHeight * sin_theta;
				x3 = 0;
				y3 = imageWidth * sin_theta;
				x4 = -imageWidth * cos_theta1;
				y4 = 0;
				x1 = x2 + x4;
				y2 = y1 + y3;

				/*
				 * Note that the vertical direction goes from top to bottom!!!
				 * The calculation above assumes that the vertical direction
				 * goes from the bottom to top. Thus the vertical coordinates
				 * need to be reversed!!!
				 */
				y1 = y2 - y1;
				y3 = y2 - y3;
				y4 = y2 - y4;
				y2 = 0;
			}

			y1 = y1 * t2;
			y2 = y2 * t2;
			y3 = y3 * t2;
			y4 = y4 * t2;

			/*
			 * the distances from the keypoint to the 4 sides of the
			 * parallelogram
			 */
			final float d1 = (float) (Math.abs((x2 - x1) * (y1 - y0) - (x1 - x0) * (y2 - y1)) / Math.sqrt((x2 - x1)
					* (x2 - x1) + (y2 - y1) * (y2 - y1)));
			final float d2 = (float) (Math.abs((x3 - x2) * (y2 - y0) - (x2 - x0) * (y3 - y2)) / Math.sqrt((x3 - x2)
					* (x3 - x2) + (y3 - y2) * (y3 - y2)));
			final float d3 = (float) (Math.abs((x4 - x3) * (y3 - y0) - (x3 - x0) * (y4 - y3)) / Math.sqrt((x4 - x3)
					* (x4 - x3) + (y4 - y3) * (y4 - y3)));
			final float d4 = (float) (Math.abs((x1 - x4) * (y4 - y0) - (x4 - x0) * (y1 - y4)) / Math.sqrt((x1 - x4)
					* (x1 - x4) + (y1 - y4) * (y1 - y4)));

			final float BorderTh = BorderFact * scale1;
			if ((d1 < BorderTh) || (d2 < BorderTh) || (d3 < BorderTh) || (d4 < BorderTh)) {
				keys_to_remove.add(keypoints.get(cc));
			}
		}
		keypoints.removeAll(keys_to_remove);
	}

	/**
	 * get the detected interest points, grouped by simulation
	 * 
	 * @return The detected interest points, grouped by simulation
	 */
	public Map<AffineParams, Q> getKeypointsMap() {
		return mappedInterestPoints;
	}
}
