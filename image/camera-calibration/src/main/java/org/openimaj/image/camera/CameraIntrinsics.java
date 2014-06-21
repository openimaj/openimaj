package org.openimaj.image.camera;

import org.openimaj.math.geometry.point.Point2d;

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
	 * Construct with an empty matrix.
	 */
	public CameraIntrinsics() {
		this.calibrationMatrix = new Matrix(3, 3);
	}

	/**
	 * Construct with the given matrix.
	 * 
	 * @param m
	 *            the matrix
	 */
	public CameraIntrinsics(Matrix m) {
		this.calibrationMatrix = m;
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
	 * the homography defined by the extrinsic parameters of a camera).
	 * 
	 * @param p
	 *            the projected point
	 * @return the distorted point
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

	@Override
	public String toString() {
		return String
				.format("fx: %2.2f; fy: %2.2f; sk: %2.6f; u0: %2.2f; v0: %2.2f; k1: %2.6f; k2: %2.6f; k3: %2.6f; p1: %2.6f; p2: %2.6f",
						getFocalLengthX(), getFocalLengthY(), getSkewFactor(), getPrincipalPointX(),
						getPrincipalPointY(), k1, k2, k3, p1, p2);
	}
}
