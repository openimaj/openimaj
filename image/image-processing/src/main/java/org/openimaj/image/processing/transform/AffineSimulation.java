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
package org.openimaj.image.processing.transform;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.TransformUtilities;

/**
 * Utility methods to simulate affine transformations defined by a rotation and
 * tilt, or series of rotations and tilts.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            Concrete subclass of {@link Image}
 * @param <P>
 *            Pixel type
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Morel, Jean-Michel", "Yu, Guoshen" },
		title = "{ASIFT: A New Framework for Fully Affine Invariant Image Comparison}",
		year = "2009",
		journal = "SIAM J. Img. Sci.",
		publisher = "Society for Industrial and Applied Mathematics")
public abstract class AffineSimulation<I extends Image<P, I> & SinglebandImageProcessor.Processable<Float, FImage, I>, P>
{
	protected static final float PI = 3.141592654f;
	protected static final float InitialAntiAliasingSigma = 1.6f;

	private AffineSimulation() {
	}

	/**
	 * Compute the position of a point in an image given the position in the
	 * transformed image and the transform parameters.
	 * 
	 * @param pt
	 *            the point in the transformed image
	 * @param width
	 *            the width of the untransformed image
	 * @param height
	 *            the height of the untransformed image
	 * @param theta
	 *            the rotation
	 * @param t
	 *            the tilt
	 * @return the point mapped to the untransformed image
	 */
	public static Point2d transformToOriginal(Point2d pt, int width, int height, float theta, float t) {
		if (t == 1)
			return pt;

		return internalTransformToOriginal(pt, width, height, theta, t);
	}

	/**
	 * Compute the position of a point in an image given the position in the
	 * transformed image and the transform parameters.
	 * 
	 * @param pt
	 *            the point in the transformed image
	 * @param original
	 *            the original untransformed image
	 * @param theta
	 *            the rotation
	 * @param t
	 *            the tilt
	 * @return the point mapped to the untransformed image
	 */
	public static Point2d transformToOriginal(Point2d pt, Image<?, ?> original, float theta, float t) {
		if (t == 1)
			return pt;

		return internalTransformToOriginal(pt, original.getWidth(), original.getHeight(), theta, t);
	}

	/**
	 * Compute the position of a point in an image given the position in the
	 * transformed image and the transform parameters.
	 * 
	 * @param pt
	 *            the point in the transformed image
	 * @param width
	 *            the width of the untransformed image
	 * @param height
	 *            the height of the untransformed image
	 * @param params
	 *            the simulation parameters
	 * @return the point mapped to the untransformed image
	 */
	public static Point2d transformToOriginal(Point2d pt, int width, int height, AffineParams params) {
		return transformToOriginal(pt, width, height, params.theta, params.tilt);
	}

	/**
	 * Compute the position of a point in an image given the position in the
	 * transformed image and the transform parameters.
	 * 
	 * @param pt
	 *            the point in the transformed image
	 * @param original
	 *            the original untransformed image
	 * @param params
	 *            the simulation parameters
	 * @return the point mapped to the untransformed image
	 */
	public static Point2d transformToOriginal(Point2d pt, Image<?, ?> original, AffineParams params) {
		return transformToOriginal(pt, original.getWidth(), original.getHeight(), params.theta, params.tilt);
	}

	protected static Point2d internalTransformToOriginal(Point2d pt, int width, int height, float Rtheta, float t1)
	{
		float x_ori, y_ori;
		Rtheta = Rtheta * PI / 180;

		if (Rtheta <= PI / 2) {
			x_ori = 0;
			y_ori = (float) ((width) * Math.sin(Rtheta) / t1);
		} else {
			x_ori = (float) (-(width) * Math.cos(Rtheta) / 1);
			y_ori = (float) (((width) * Math.sin(Rtheta) + (height) * Math.sin(Rtheta - PI / 2)) / t1);
		}

		final float sin_Rtheta = (float) Math.sin(Rtheta);
		final float cos_Rtheta = (float) Math.cos(Rtheta);

		final Point2d ptout = pt.copy();

		/*
		 * project the coordinates of im1 to original image before tilt-rotation
		 * transform; get the coordinates with respect to the 'origin' of the
		 * original image before transform
		 */
		ptout.setX(pt.getX() - x_ori);
		ptout.setY(pt.getY() - y_ori);

		/* Invert tilt */
		ptout.setX(ptout.getX() * 1);
		ptout.setY(ptout.getY() * t1);

		/*
		 * Invert rotation (Note that the y direction (vertical) is inverse to
		 * the usual concention. Hence Rtheta instead of -Rtheta to inverse the
		 * rotation.)
		 */
		final float tx = cos_Rtheta * ptout.getX() - sin_Rtheta * ptout.getY();
		final float ty = sin_Rtheta * ptout.getX() + cos_Rtheta * ptout.getY();

		ptout.setX(tx);
		ptout.setY(ty);

		return ptout;
	}

	/**
	 * Transform the coordinates of the given points from a transformed image to
	 * the original space.
	 * 
	 * @param <Q>
	 *            Type of interest point list
	 * @param <T>
	 *            Type of interest point
	 * @param <I>
	 *            Type of {@link Image}
	 * @param points
	 *            the points
	 * @param original
	 *            the original untransformed image
	 * @param theta
	 *            the rotation
	 * @param tilt
	 *            the tilt
	 */
	public static <Q extends List<T>, T extends Point2d, I extends Image<?, I>> void transformToOriginal(Q points,
			I original, float theta, float tilt)
	{
		final List<T> keys_to_remove = new ArrayList<T>();
		float x_ori, y_ori;

		if (theta <= PI / 2) {
			x_ori = 0;
			y_ori = (float) ((original.getWidth()) * Math.sin(theta) / tilt);
		} else {
			x_ori = (float) (-(original.getWidth()) * Math.cos(theta) / 1);
			y_ori = (float) (((original.getWidth()) * Math.sin(theta) + (original.getHeight())
					* Math.sin(theta - PI / 2)) / tilt);
		}

		final float sin_Rtheta = (float) Math.sin(theta);
		final float cos_Rtheta = (float) Math.cos(theta);

		for (final T k : points) {
			/*
			 * project the coordinates of im1 to original image before
			 * tilt-rotation transform
			 */
			/*
			 * Get the coordinates with respect to the 'origin' of the original
			 * image before transform
			 */
			k.setX(k.getX() - x_ori);
			k.setY(k.getY() - y_ori);
			/* Invert tilt */
			k.setX(k.getX() * 1);
			k.setY(k.getY() * tilt);
			/*
			 * Invert rotation (Note that the y direction (vertical) is inverse
			 * to the usual concention. Hence Rtheta instead of -Rtheta to
			 * inverse the rotation.)
			 */
			final float tx = cos_Rtheta * k.getX() - sin_Rtheta * k.getY();
			final float ty = sin_Rtheta * k.getX() + cos_Rtheta * k.getY();

			k.setX(tx);
			k.setY(ty);

			if (tx <= 0 || ty <= 0 || tx >= original.getWidth() || ty >= original.getHeight()) {
				keys_to_remove.add(k);
			}
		}
		points.removeAll(keys_to_remove);
	}

	/**
	 * Compute the transformed images based on the given number of tilts.
	 * 
	 * @param image
	 *            the image to transform.
	 * @param numTilts
	 *            the number of tilts to simulate.
	 * @return the transformed images
	 * @throws IllegalArgumentException
	 *             if the number of tilts is < 1
	 */
	public static <I extends Image<P, I> & SinglebandImageProcessor.Processable<Float, FImage, I>, P>
			List<I> transformImage(I image, int numTilts)
	{
		if (numTilts < 1) {
			throw new IllegalArgumentException("Number of tilts num_tilt should be equal or larger than 1.");
		}

		final List<I> transformed = new ArrayList<I>();
		int num_rot1 = 0;

		final int num_rot_t2 = 10;
		final float t_min = 1;
		final float t_k = (float) Math.sqrt(2);

		for (int tt = 1; tt <= numTilts; tt++) {
			final float t = t_min * (float) Math.pow(t_k, tt - 1);

			if (t == 1) {
				transformed.add(image.clone());
			} else {
				num_rot1 = Math.round(num_rot_t2 * t / 2);

				if (num_rot1 % 2 == 1) {
					num_rot1 = num_rot1 + 1;
				}
				num_rot1 = num_rot1 / 2;

				final float delta_theta = PI / num_rot1;

				for (int rr = 1; rr <= num_rot1; rr++) {
					final float theta = delta_theta * (rr - 1);

					transformed.add(transformImage(image, theta, t));
				}
			}
		}

		return transformed;
	}

	/**
	 * Compute a single transformed image for a given rotation and tilt.
	 * 
	 * @param image
	 *            the image
	 * @param theta
	 *            the rotation angle
	 * @param t
	 *            the tilt amount
	 * @return the transformed image
	 */
	public static <I extends Image<P, I> & SinglebandImageProcessor.Processable<Float, FImage, I>, P> I transformImage(
			I image, float theta, float t)
	{
		final float t1 = 1;
		final float t2 = 1 / t;

		// Perform rotation
		final I image_rotated = ProjectionProcessor.project(image, TransformUtilities.rotationMatrix(-theta));

		// Perform anti-aliasing filtering by convolving with a Gaussian in the
		// vertical direction
		final float sigma_aa = InitialAntiAliasingSigma * t / 2;
		image_rotated.processInplace(new FImageConvolveSeparable(null, FGaussianConvolve.makeKernel(sigma_aa)));

		// Squash the image in the x and y direction by t1 and t2 to mimic tilt
		return ProjectionProcessor.project(image_rotated, TransformUtilities.scaleMatrix(t1, t2));
	}

	/**
	 * Compute a single transformed image for a given rotation and tilt.
	 * 
	 * @param image
	 *            the image
	 * @param params
	 *            the simulation parameters
	 * @return the transformed image
	 */
	public static <I extends Image<P, I> & SinglebandImageProcessor.Processable<Float, FImage, I>, P> I transformImage(
			I image, AffineParams params)
	{
		return transformImage(image, params.theta, params.tilt);
	}

}
