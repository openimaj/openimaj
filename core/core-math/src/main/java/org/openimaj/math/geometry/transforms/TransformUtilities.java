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
package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * A collection of static methods for creating transform matrices.  
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class TransformUtilities {
	
	/**
	 * Construct a rotation about 0, 0.
	 * @param rot The amount of rotation in radians.
	 * @return The rotation matrix.
	 */
	public static Matrix rotationMatrix(double rot) {
		Matrix matrix = Matrix.constructWithCopy(new double[][] {
				{Math.cos(rot),-Math.sin(rot),0},
				{Math.sin(rot),Math.cos(rot),0},
				{0,0,1},
		});
		return matrix;
	}
	
	/**
	 * Construct a translation.
	 * @param x The amount to translate in the x-direction. 
	 * @param y The amount to translate in the y-direction.
	 * @return The translation matrix.
	 */
	public static Matrix translateMatrix(float x, float y) {
		Matrix matrix = Matrix.constructWithCopy(new double[][] {
				{1,0,x},
				{0,1,y},
				{0,0,1},
		});
		return matrix;
	}
	
	/**
	 * Construct a rotation about the centre of the rectangle defined
	 * by width and height (i.e. width/2, height/2).
	 * @param rot The amount of rotation in radians.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle. 
	 * @return The rotation matrix.
	 */
	public static Matrix centeredRotationMatrix(double rot, int width, int height) {
		int halfWidth = (int) Math.round(width/2);
		int halfHeight = (int) Math.round(height/2);
		
		return rotationMatrixAboutPoint(rot, halfWidth, halfHeight);
	}
	
	/**
	 * Construct a rotation about the centre of the rectangle defined
	 * by width and height (i.e. width/2, height/2).
	 * @param rot The amount of rotation in radians.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle. 
	 * @return The rotation matrix.
	 */
	public static Matrix rotationMatrixAboutPoint(double rot, float tx, float ty) {
		return Matrix.identity(3,3).
			times(translateMatrix(tx,ty)).
			times(rotationMatrix(rot)).
			times(translateMatrix(-tx,-ty));
	}
	
	/**
	 * Construct an affine transform using a least-squares
	 * fit of the provided point pairs. There must be at least
	 * 3 point pairs for this to work.
	 * 
	 * @param data Data to calculate affine matrix from.
	 * @return an affine transform matrix.
	 */
	public static Matrix affineMatrix(List<Pair<Point2d>> data) {
		Matrix A, W=null;
		int i, j;
		Matrix transform = new Matrix(3,3);

		transform.set(2, 0, 0);
		transform.set(2, 1, 0);
		transform.set(2, 2, 1);

		//Solve Ax=0
		A = new Matrix(data.size()*2, 7);

		for ( i=0, j=0; i<data.size(); i++, j+=2) {
			float x1 = data.get(i).firstObject().getX();
			float y1 = data.get(i).firstObject().getY();
			float x2 = data.get(i).secondObject().getX();
			float y2 = data.get(i).secondObject().getY();

			A.set(j, 0, x1);	
			A.set(j, 1, y1);	
			A.set(j, 2, 1);
			A.set(j, 3, 0);
			A.set(j, 4, 0);
			A.set(j, 5, 0);
			A.set(j, 6, -x2);

			A.set(j+1, 0, 0);	
			A.set(j+1, 1, 0);	
			A.set(j+1, 2, 0);
			A.set(j+1, 3, x1);
			A.set(j+1, 4, y1);
			A.set(j+1, 5, 1);
			A.set(j+1, 6, -y2);
		}

		//This is a hack to use MJT instead -- jama's svd is broken
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(A.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);

			W = new Matrix(svd.getVt().numRows(), 1);

			for (i=0; i<svd.getVt().numRows(); i++) {
				W.set(i, 0, svd.getVt().get(6, i)); //do transpose here too!
			}	
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
		}
		//End hack

		//build matrix
		transform.set(0,0, W.get(0,0) / W.get(6,0));
		transform.set(0,1, W.get(1,0) / W.get(6,0));
		transform.set(0,2, W.get(2,0) / W.get(6,0));

		transform.set(1,0, W.get(3,0) / W.get(6,0));
		transform.set(1,1, W.get(4,0) / W.get(6,0));
		transform.set(1,2, W.get(5,0) / W.get(6,0));
		
		return transform;
	}

	/**
	 * Construct a scaling transform with the given amounts of scaling.
	 * @param d Scaling in the x-direction.
	 * @param e Scaling in the y-direction.
	 * @return a scaling matrix.
	 */
	public static Matrix scaleMatrix(double d, double e) {
		Matrix scaleMatrix = new Matrix(new double[][] {
				{d,0,0},
				{0,e,0},
				{0,0,1},
		});
		return scaleMatrix;
	}
}
