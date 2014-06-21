package org.openimaj.image.camera;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.Point3d;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * A model of the extrinsic parameters of a pinhole camera (translation in 3d
 * space, and 3d rotation matrix), coupled with the camera's intrinsic
 * parameters.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Camera {
	/**
	 * The intrinsic parameters of this camera
	 */
	public CameraIntrinsics intrinsicParameters;

	/**
	 * The rotation of this camera in world coordinates
	 */
	public Matrix rotation;

	/**
	 * The position of this camera in world coordinates
	 */
	public Point3d translation;

	/**
	 * Compute the homography of this camera to the z=0 plane based on it's
	 * parameters: H = KA, where A = [r1 r2 t] and r1 and r2 are the first and
	 * second columns of the rotation matrix, and K is the calibration matrix.
	 * 
	 * @return the camera's homography
	 */
	public Matrix computeHomography() {
		final Matrix A = rotation.copy();
		A.set(0, 2, translation.getX());
		A.set(1, 2, translation.getY());
		A.set(2, 2, translation.getZ());

		final Matrix H = intrinsicParameters.calibrationMatrix.times(A);
		MatrixUtils.times(H, 1.0 / H.get(2, 2));

		return H;
	}

	/**
	 * Project a 2d point (technically a 3d point on the z=0 world plane)
	 * 
	 * @param pt
	 *            the point to project in world coordinates
	 * @return the image coordinates
	 */
	public Point2d project(Point2d pt) {
		final Matrix H = this.computeHomography();

		final Point2d p = pt.transform(H);

		return intrinsicParameters.applyDistortion(p);
	}

	/**
	 * Project a 3d point onto the image plane
	 * 
	 * @param pt
	 *            the point to project in world coordinates
	 * @return the image coordinates
	 */
	public Point2d project(Point3d pt) {
		final Matrix ptm = new Matrix(new double[][] { { pt.getX() }, { pt.getY() }, { pt.getZ() }, { 1 } });
		final double[][] rv = rotation.getArray();
		final Matrix Rt = new Matrix(new double[][] {
				{ rv[0][0], rv[0][1], rv[0][2], translation.getX() },
				{ rv[1][0], rv[1][1], rv[1][2], translation.getY() },
				{ rv[2][0], rv[2][1], rv[2][2], translation.getZ() },
		});
		final Matrix ARt = intrinsicParameters.calibrationMatrix.times(Rt);

		final Matrix pr = ARt.times(ptm);

		final Point2dImpl p = new Point2dImpl(pr.get(0, 0) / pr.get(2, 0), pr.get(1, 0) / pr.get(2, 0));

		return intrinsicParameters.applyDistortion(p);
	}
}
