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
package org.openimaj.image.camera;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.transform.RemapProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * The intrinsic parameters of a camera (focal length in x and y; skew;
 * principal point in x and y; 3-term radial distorion; 2-term tangential
 * distortion).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CameraIntrinsics {
	/**
	 * The camera calibration matrix
	 */
	public Matrix calibrationMatrix;

	/**
	 * First radial distortion term
	 */
	public double k1;

	/**
	 * Second radial distortion term
	 */
	public double k2;

	/**
	 * Third radial distortion term
	 */
	public double k3;

	/**
	 * First tangential distortion term
	 */
	public double p1;

	/**
	 * Second tangential distortion term
	 */
	public double p2;

	/**
	 * The image width of the camera in pixels
	 */
	public int width;

	/**
	 * The image height of the camera in pixels
	 */
	public int height;

	/**
	 * Construct with the given matrix.
	 * 
	 * @param m
	 *            the matrix
	 * @param width
	 *            the image width of the camera in pixels
	 * @param height
	 *            the image height of the camera in pixels
	 */
	public CameraIntrinsics(Matrix m, int width, int height) {
		this.calibrationMatrix = m;
		this.width = width;
		this.height = height;
	}

	/**
	 * Get the focal length in the x direction (in pixels)
	 * 
	 * @return fx
	 */
	public double getFocalLengthX() {
		return calibrationMatrix.get(0, 0);
	}

	/**
	 * Get the focal length in the y direction (in pixels)
	 * 
	 * @return fy
	 */
	public double getFocalLengthY() {
		return calibrationMatrix.get(1, 1);
	}

	/**
	 * Get the x-ordinate of the principal point (in pixels).
	 * 
	 * @return cx
	 */
	public double getPrincipalPointX() {
		return calibrationMatrix.get(0, 2);
	}

	/**
	 * Get the y-ordinate of the principal point (in pixels).
	 * 
	 * @return cy
	 */
	public double getPrincipalPointY() {
		return calibrationMatrix.get(1, 2);
	}

	/**
	 * Set the focal length in the x direction (in pixels)
	 * 
	 * @param fy
	 *            the focal length in the x direction
	 */
	public void setFocalLengthX(double fy) {
		calibrationMatrix.set(0, 0, fy);
	}

	/**
	 * Set the focal length in the y direction (in pixels)
	 * 
	 * @param fy
	 *            the focal length in the y direction
	 */
	public void setFocalLengthY(double fy) {
		calibrationMatrix.set(1, 1, fy);
	}

	/**
	 * Set the x-ordinate of the principal point (in pixels).
	 * 
	 * @param cx
	 *            the y-ordinate of the principal point
	 */
	public void setPrincipalPointX(double cx) {
		calibrationMatrix.set(0, 2, cx);
	}

	/**
	 * Set the y-ordinate of the principal point (in pixels).
	 * 
	 * @param cy
	 *            the y-ordinate of the principal point
	 */
	public void setPrincipalPointY(double cy) {
		calibrationMatrix.set(1, 2, cy);
	}

	/**
	 * Set the skew factor.
	 * 
	 * @param skew
	 *            the skew.
	 */
	public void setSkewFactor(double skew) {
		calibrationMatrix.set(0, 1, skew);
	}

	/**
	 * Get the skew factor.
	 * 
	 * @return skew
	 */
	public double getSkewFactor() {
		return calibrationMatrix.get(0, 1);
	}

	/**
	 * Apply the radial and tangential distortion of this camera to the given
	 * projected point (presumably computed by projecting a world point through
	 * the homography defined by the extrinsic parameters of a camera). The
	 * point is modified in-place.
	 * 
	 * @param p
	 *            the projected point
	 * @return the input point (with the distortion added)
	 */
	public Point2d applyDistortion(Point2d p) {
		final double dx = (p.getX() - getPrincipalPointX()) / getFocalLengthX();
		final double dy = (p.getY() - getPrincipalPointY()) / getFocalLengthY();
		final double r2 = dx * dx + dy * dy;
		final double r4 = r2 * r2;
		final double r6 = r2 * r2 * r2;

		// radial component
		double tx = dx * (k1 * r2 + k2 * r4 + k3 * r6);
		double ty = dy * (k1 * r2 + k2 * r4 + k3 * r6);

		// tangential component
		tx += 2 * p1 * dx * dy + p2 * (r2 + 2 * dx * dx);
		ty += p1 * (r2 + 2 * dy * dy) + 2 * p2 * dx * dy;

		p.translate((float) tx, (float) ty);
		return p;
	}

	/**
	 * Compute a scaled set of intrinsic parameters based on the given image
	 * size.
	 * 
	 * @param newWidth
	 *            the target image size
	 * @param newHeight
	 *            the target image width
	 * @return the new scaled intrinsics
	 */
	public CameraIntrinsics getScaledIntrinsics(int newWidth, int newHeight) {
		final double sx = (double) newWidth / (double) width;
		final double sy = (double) newHeight / (double) height;

		final Matrix m = TransformUtilities.scaleMatrix(sx, sy).times(this.calibrationMatrix);

		final CameraIntrinsics newCam = new CameraIntrinsics(m, newWidth, newHeight);
		newCam.k1 = k1;
		newCam.k2 = k2;
		newCam.k3 = k3;
		newCam.p1 = p1;
		newCam.p2 = p2;

		return newCam;
	}

	@Override
	public String toString() {
		return String
				.format("fx: %2.2f; fy: %2.2f; sk: %2.6f; u0: %2.2f; v0: %2.2f; k1: %2.6f; k2: %2.6f; k3: %2.6f; p1: %2.6f; p2: %2.6f",
						getFocalLengthX(), getFocalLengthY(), getSkewFactor(), getPrincipalPointX(),
						getPrincipalPointY(), k1, k2, k3, p1, p2);
	}

	/**
	 * Build a {@link RemapProcessor} capable of correcting the radial and
	 * tangential distortion of this camera.
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @return the processor for undistorting the image
	 */
	public RemapProcessor buildUndistortionProcessor() {
		return buildUndistortionProcessor(width, height);
	}

	/**
	 * Build a {@link RemapProcessor} capable of correcting the radial and
	 * tangential distortion of this camera.
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @param width
	 *            the target width of images produced by the processor
	 * @param height
	 *            the target height of images produced by the processor
	 * @return the processor for undistorting the image
	 */
	public RemapProcessor buildUndistortionProcessor(int width, int height) {
		final FImage[] map = buildUndistortionMap(width, height);
		return new RemapProcessor(map[0], map[1]);
	}

	/**
	 * Build a {@link RemapProcessor} capable of correcting the radial and
	 * tangential distortion of this camera. The distortion map is computed such
	 * that the warped image will appear as if it were viewed through the
	 * undistorted <code>target</code> camera, rather than this one.
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @param width
	 *            the target width of images produced by the processor
	 * @param height
	 *            the target height of images produced by the processor
	 * @param target
	 *            the target camera
	 * @return the processor for undistorting the image
	 */
	public RemapProcessor buildUndistortionProcessor(int width, int height, CameraIntrinsics target) {
		final FImage[] map = buildUndistortionMap(width, height, target);
		return new RemapProcessor(map[0], map[1]);
	}

	/**
	 * Build a {@link RemapProcessor} capable of correcting the radial and
	 * tangential distortion of this camera. The distortion map is computed such
	 * that the warped image will appear as if it were viewed through the
	 * undistorted <code>target</code> camera rather than this one, and the
	 * 3x3matrix <code>R</code> controls the rectification (i.e. the relative
	 * change in position of the camera in world space).
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @param width
	 *            the target width of images produced by the processor
	 * @param height
	 *            the target height of images produced by the processor
	 * @param target
	 *            the target camera
	 * @param R
	 *            the rectification matrix
	 * @return the processor for undistorting the image
	 */
	public RemapProcessor buildRectifiedUndistortionProcessor(int width, int height, CameraIntrinsics target, Matrix R) {
		final FImage[] map = buildRectifiedUndistortionMap(width, height, target, R);
		return new RemapProcessor(map[0], map[1]);
	}

	/**
	 * Build the distortion map, which for every un-distorted point in the given
	 * size image contains the x and y ordinates of the corresponding distorted
	 * point. The resultant map can be used with a {@link RemapProcessor} to
	 * undistort imaaes.
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @param width
	 *            the desired width; typically the same as the image width used
	 *            to calibrate the camera
	 * @param height
	 *            the desired height; typically the same as the image height
	 *            used to calibrate the camera
	 * @return the distortion map
	 */
	public FImage[] buildUndistortionMap(final int width, final int height) {
		final FImage xords = new FImage(width, height);
		final FImage yords = new FImage(width, height);

		final double px = this.getPrincipalPointX();
		final double py = this.getPrincipalPointY();
		final double fx = this.getFocalLengthX();
		final double fy = this.getFocalLengthY();

		for (int v = 0; v < height; v++) {
			final double y = (v - py) / fy;

			for (int u = 0; u < width; u++) {
				final double x = (u - px) / fx;

				final double r2 = x * x + y * y;
				final double r4 = r2 * r2;
				final double r6 = r2 * r2 * r2;

				// radial component
				double tx = x * (k1 * r2 + k2 * r4 + k3 * r6);
				double ty = y * (k1 * r2 + k2 * r4 + k3 * r6);

				// tangential component
				tx += 2 * p1 * x * y + p2 * (r2 + 2 * x * x);
				ty += p1 * (r2 + 2 * y * y) + 2 * p2 * x * y;

				xords.pixels[v][u] = (float) ((x + tx) * fx + px);
				yords.pixels[v][u] = (float) ((y + ty) * fy + py);
			}
		}

		return new FImage[] { xords, yords };
	}

	/**
	 * Build the distortion map, which for every un-distorted point in the given
	 * size image contains the x and y ordinates of the corresponding distorted
	 * point. The resultant map can be used with a {@link RemapProcessor} to
	 * undistort imaaes. The distortion map is computed such that the warped
	 * image will appear as if it were viewed through the undistorted
	 * <code>target</code> camera, rather than this one.
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @param width
	 *            the desired width; typically the same as the image width used
	 *            to calibrate the camera
	 * @param height
	 *            the desired height; typically the same as the image height
	 *            used to calibrate the camera
	 * @param target
	 *            the target camera intrinsics
	 * @return the distortion map
	 */
	public FImage[] buildUndistortionMap(final int width, final int height, CameraIntrinsics target) {
		final FImage xords = new FImage(width, height);
		final FImage yords = new FImage(width, height);

		final double pxp = target.getPrincipalPointX();
		final double pyp = target.getPrincipalPointY();
		final double fxp = target.getFocalLengthX();
		final double fyp = target.getFocalLengthY();

		final double px = this.getPrincipalPointX();
		final double py = this.getPrincipalPointY();
		final double fx = this.getFocalLengthX();
		final double fy = this.getFocalLengthY();

		for (int v = 0; v < height; v++) {
			final double y = (v - pyp) / fyp;

			for (int u = 0; u < width; u++) {
				final double x = (u - pxp) / fxp;

				final double r2 = x * x + y * y;
				final double r4 = r2 * r2;
				final double r6 = r2 * r2 * r2;

				// radial component
				double tx = x * (k1 * r2 + k2 * r4 + k3 * r6);
				double ty = y * (k1 * r2 + k2 * r4 + k3 * r6);

				// tangential component
				tx += 2 * p1 * x * y + p2 * (r2 + 2 * x * x);
				ty += p1 * (r2 + 2 * y * y) + 2 * p2 * x * y;

				xords.pixels[v][u] = (float) ((x + tx) * fx + px);
				yords.pixels[v][u] = (float) ((y + ty) * fy + py);
			}
		}

		return new FImage[] { xords, yords };
	}

	/**
	 * Build the rectified distortion map, which for every un-distorted point in
	 * the given size image contains the x and y ordinates of the corresponding
	 * rectifed distorted point. The resultant map can be used with a
	 * {@link RemapProcessor} to undistort imaaes. The distortion map is
	 * computed such that the warped image will appear as if it were viewed
	 * through the undistorted <code>target</code> camera rather than this one,
	 * and the 3x3matrix <code>R</code> controls the rectification (i.e. the
	 * relative change in position of the camera in world space).
	 * <p>
	 * <b>Note:</b> Skew is currently assumed to be zero
	 * 
	 * @param width
	 *            the desired width; typically the same as the image width used
	 *            to calibrate the camera
	 * @param height
	 *            the desired height; typically the same as the image height
	 *            used to calibrate the camera
	 * @param target
	 *            the target camera intrinsics
	 * @param R
	 *            the rectification matrix
	 * @return the distortion map
	 */
	public FImage[] buildRectifiedUndistortionMap(final int width, final int height, CameraIntrinsics target, Matrix R) {
		final FImage xords = new FImage(width, height);
		final FImage yords = new FImage(width, height);

		final double pxp = target.getPrincipalPointX();
		final double pyp = target.getPrincipalPointY();
		final double fxp = target.getFocalLengthX();
		final double fyp = target.getFocalLengthY();

		final double px = this.getPrincipalPointX();
		final double py = this.getPrincipalPointY();
		final double fx = this.getFocalLengthX();
		final double fy = this.getFocalLengthY();

		final Matrix Rinv = R.inverse();
		final Matrix tmp = new Matrix(3, 1);
		tmp.set(2, 0, 1);

		for (int v = 0; v < height; v++) {
			double y = (v - pyp) / fyp;

			for (int u = 0; u < width; u++) {
				double x = (u - pxp) / fxp;

				tmp.set(0, 0, x);
				tmp.set(1, 0, y);
				final Matrix pro = Rinv.times(tmp);
				x = pro.get(0, 0) / pro.get(2, 0);
				y = pro.get(1, 0) / pro.get(2, 0);

				final double r2 = x * x + y * y;
				final double r4 = r2 * r2;
				final double r6 = r2 * r2 * r2;

				// radial component
				double tx = x * (k1 * r2 + k2 * r4 + k3 * r6);
				double ty = y * (k1 * r2 + k2 * r4 + k3 * r6);

				// tangential component
				tx += 2 * p1 * x * y + p2 * (r2 + 2 * x * x);
				ty += p1 * (r2 + 2 * y * y) + 2 * p2 * x * y;

				xords.pixels[v][u] = (float) ((x + tx) * fx + px);
				yords.pixels[v][u] = (float) ((y + ty) * fy + py);
			}
		}

		return new FImage[] { xords, yords };
	}

	/**
	 * Undistort the given image by removing the radial and tangential
	 * distortions of this camera. It is assumed that the input image has the
	 * same dimensions as images produced by this {@link CameraIntrinsics}.
	 * <p>
	 * This method is inefficient if you need to process many images as the
	 * distortion map is computed each time. For more efficient operation, use
	 * {@link #buildUndistortionProcessor()} to make a reusable
	 * {@link RemapProcessor} capable of efficiently undistorting multiple
	 * images.
	 * 
	 * @param image
	 *            the image to undistort
	 * @return the undistorted image
	 */
	public <I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> I undistort(I image) {
		final RemapProcessor proc = buildUndistortionProcessor(image.getWidth(), image.getHeight());
		return image.process(proc);
	}
}
