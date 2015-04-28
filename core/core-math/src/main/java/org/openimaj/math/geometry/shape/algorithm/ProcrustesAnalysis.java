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
package org.openimaj.math.geometry.shape.algorithm;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Ordinary Procrustes Analysis between two sets of corresponding
 * {@link PointList}s. 
 * 
 * It is assumed that the number of points in the {@link PointList}s
 * is equal, and that their is a one-to-one correspondance between
 * the ith point in each list.
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProcrustesAnalysis {
	protected PointList reference;
	protected Point2d referenceCog;
	protected double scaling;
	protected boolean rotate = true;

	/**
	 * Construct the {@link ProcrustesAnalysis} with the given
	 * reference shape.
	 * 
	 * @param reference The reference shape.
	 */
	public ProcrustesAnalysis(PointList reference) {
		this(reference, false);
	}

	/**
	 * Construct the {@link ProcrustesAnalysis} with the given
	 * reference shape. The reference shape is optionally normalised
	 * to a standardised scale and translated to the origin. 
	 * 
	 * @param reference The reference shape.
	 * @param normalise if true, then the reference is normalised (changing
	 * 		the reference shape itself).
	 */
	public ProcrustesAnalysis(PointList reference, boolean normalise) {
		this.reference = reference;

		referenceCog = reference.calculateCentroid();
		scaling = computeScale(reference, referenceCog.getX(), referenceCog.getY());

		if (normalise) {
			reference.translate(-referenceCog.getX(), -referenceCog.getY());
			reference.scale((float) scaling);

			referenceCog.setX(0);
			referenceCog.setY(0);
			scaling = 1;
		}
	}

	protected static double computeScale(PointList pl, double tx, double ty) {
		double scale = 0;

		for (Point2d pt : pl) {
			double x = pt.getX() - tx;
			double y = pt.getY() - ty;

			scale += x*x + y*y;
		}

		scale = Math.sqrt(scale / pl.points.size());

		return 1.0 / scale;
	}

	/**
	 * Align the given shape to the reference. The alignment
	 * happens inplace, so the input points are modified. The
	 * matrix used to transform the points is returned.
	 * 
	 * @param toAlign transform matrix
	 * @return aligned points (reference to toAlign)
	 * @throws IllegalArgumentException if the input is null or has a
	 * 		different size to the reference.
	 */
	public Matrix align(PointList toAlign) {
		if (toAlign.points.size() != reference.points.size())
			throw new IllegalArgumentException("Point lists are different lengths");

		//translation
		Point2d cog = toAlign.calculateCentroid();
		Matrix trans = TransformUtilities.translateToPointMatrix(cog, this.referenceCog);
		toAlign.translate((float)trans.get(0,2), (float)trans.get(1,2));

		//scaling
		double scale = computeScale(toAlign, referenceCog.getX(), referenceCog.getY());
		float sf = (float)(scale / this.scaling);
		toAlign.scale(referenceCog, sf);

		//rotation
		double theta = 0;

		if (rotate) {
			float num = 0;
			float den = 0;
			final int count = reference.points.size();

			for (int i=0; i<count; i++) {
				Point2d p1 = reference.points.get(i);
				Point2d p2 = toAlign.points.get(i);

				float p1x = (float) (p1.getX());
				float p1y = (float) (p1.getY());

				float p2x = (float) (p2.getX());
				float p2y = (float) (p2.getY());

				num += p2x*p1y - p2y*p1x;
				den += p2x*p1x + p2y*p1y;
			}

			theta = Math.atan2(num, den);

			toAlign.rotate(this.referenceCog, theta);
		}

		//compute matrix
		Matrix scaleMat = TransformUtilities.scaleMatrixAboutPoint(sf, sf, this.referenceCog);
		Matrix rotMat = TransformUtilities.rotationMatrixAboutPoint(theta, this.referenceCog.getX(), this.referenceCog.getY());
		return rotMat.times(scaleMat).times(trans);
	}

	/**
	 * Compute the Procrustes Distance between two {@link PointList}s.
	 * If the distance is 0, then the shapes overlap exactly.
	 * 
	 * @param l1 first shape
	 * @param l2 second shape
	 * @return the calculated distance.
	 * @throws IllegalArgumentException if the input is null or has a
	 * 		different size to the reference.
	 */
	public static float computeProcrustesDistance(PointList l1, PointList l2) {
		if (l1.points.size() != l2.points.size())
			throw new IllegalArgumentException("Point lists are different lengths");

		final int count = l1.points.size();
		float distance = 0;

		for (int i=0; i<count; i++) {
			Point2d p1 = l1.points.get(i);
			Point2d p2 = l2.points.get(i);

			float dx = p1.getX() - p2.getX();
			float dy = p1.getY() - p2.getY();

			distance += dx*dx + dy*dy;
		}

		return (float) Math.sqrt(distance);
	}
}
