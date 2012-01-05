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
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.pair.IndependentPair;
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
	 * Given two points, get a transform matrix that takes points from point a to point b
	 * @param from from this point
	 * @param to to this point
	 * @return transform matrix
	 */
	public static Matrix translateToPointMatrix(Point2d from, Point2d to) {
		Matrix matrix = Matrix.constructWithCopy(new double[][] {
				{1,0,to.minus(from).getX()},
				{0,1,to.minus(from).getY()},
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
	
	public static Matrix scaleMatrixAboutPoint(double sx, double sy, int tx, int ty) {
		return Matrix.identity(3,3).
		times(translateMatrix(tx,ty)).
		times(scaleMatrix(sx,sy)).
		times(translateMatrix(-tx,-ty));
	}
	
	public static Matrix scaleMatrixAboutPoint(double sx,double sy, Point2d point) {
		return Matrix.identity(3,3).
			times(translateMatrix(point.getX(),point.getY())).
			times(scaleMatrix(sx,sy)).
			times(translateMatrix(-point.getX(),-point.getY()));
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
	public static Matrix affineMatrix(List<? extends IndependentPair<Point2d, Point2d>> data) {
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

	/**
	 * Normalises the points such that each matched point is centered about the origin and also scaled be be within
	 * Math.sqrt(2) of the origin. This corrects for some errors which occured when distances between matched points were 
	 * extremely large.
	 * @param data
	 * @return
	 */
	private static Pair<Matrix> getNormalisations(List<? extends IndependentPair<Point2d, Point2d>> data) {
		Point2dImpl firstMean = new Point2dImpl(0,0), secondMean = new Point2dImpl(0,0);
		for(IndependentPair<Point2d,Point2d> pair : data){
			firstMean.x += pair.firstObject().getX();
			firstMean.y += pair.firstObject().getY();
			secondMean.x += pair.secondObject().getX();
			secondMean.y += pair.secondObject().getY();
		}
		
		firstMean.x /=data.size();
		firstMean.y /=data.size();
		secondMean.x /=data.size();
		secondMean.y /=data.size();
		
		// Calculate the std
		Point2dImpl firstStd = new Point2dImpl(0,0), secondStd = new Point2dImpl(0,0);
		
		for(IndependentPair<Point2d,Point2d> pair : data){
			firstStd.x += Math.pow(firstMean.x - pair.firstObject().getX(),2);
			firstStd.y += Math.pow(firstMean.y - pair.firstObject().getY(),2);
			secondStd.x += Math.pow(secondMean.x - pair.secondObject().getX(),2);
			secondStd.y += Math.pow(secondMean.y - pair.secondObject().getY(),2);
		}
		
		firstStd.x = firstStd.x < 0.00001 ? 1.0f : firstStd.x;
		firstStd.y = firstStd.y < 0.00001 ? 1.0f : firstStd.y;
		
		secondStd.x = secondStd.x < 0.00001 ? 1.0f : secondStd.x;
		secondStd.y = secondStd.y < 0.00001 ? 1.0f : secondStd.y;
		
		firstStd.x = (float) (Math.sqrt(2) / Math.sqrt(firstStd.x / (data.size()-1)));
		firstStd.y = (float) (Math.sqrt(2) / Math.sqrt(firstStd.y / (data.size()-1)));
		secondStd.x = (float) (Math.sqrt(2) / Math.sqrt(secondStd.x / (data.size()-1)));
		secondStd.y = (float) (Math.sqrt(2) / Math.sqrt(secondStd.y / (data.size()-1)));
		
		Matrix firstMatrix = new Matrix(new double[][]{
				{firstStd.x,0,-firstMean.x * firstStd.x},
				{0,firstStd.y,-firstMean.y * firstStd.y},
				{0,0,1},
		});
		
		Matrix secondMatrix = new Matrix(new double[][]{
				{secondStd.x,0			,-secondMean.x * secondStd.x},
				{0			,secondStd.y,-secondMean.y * secondStd.y},
				{0			,0			,1},
		});
		
		
		return new Pair<Matrix>(firstMatrix,secondMatrix);
	}
	
	public static Matrix fundamentalMatrix(List<? extends IndependentPair<Point2d, Point2d>> data) {
		Matrix A, W=null;
		
		Pair<Matrix> normalisations = getNormalisations(data);
		A = new Matrix(data.size(), 9);
		for (int i = 0; i < data.size(); i++) {
			Point2d p1 = data.get(i).firstObject().transform(normalisations.firstObject());
			Point2d p2 = data.get(i).secondObject().transform(normalisations.secondObject());
			
			float x1_1 = p1.getX(); // u
			float x1_2 = p1.getY(); // v
			float x2_1 = p2.getX(); // u'
			float x2_2 = p2.getY(); // v'
			
			A.set(i, 0, x2_1 * x1_1); // u' * u
			A.set(i, 1, x2_1 * x1_2); // u' * v
			A.set(i, 2, x2_1); // u'
			A.set(i, 3, x2_2 * x1_1); // v' * u
			A.set(i, 4, x2_2 * x1_2); // v' * v
			A.set(i, 5, x2_2); // v'
			A.set(i, 6, x1_1); // u
			A.set(i, 7, x1_2); // v
			A.set(i, 8, 1); // 1
		}
		
		//This is a hack to use MJT instead
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(A.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
			
			W = new Matrix(svd.getVt().numRows(), 1);
			
			for (int i=0; i<svd.getVt().numRows(); i++) {
				W.set(i, 0, svd.getVt().get(8, i)); //do transpose here too!
			}	
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
			return null;
		}
		//End hack

		Matrix fundamental = new Matrix(3,3);
		fundamental.set(0,0, W.get(0,0));
		fundamental.set(0,1, W.get(1,0));
		fundamental.set(0,2, W.get(2,0));
		fundamental.set(1,0, W.get(3,0));
		fundamental.set(1,1, W.get(4,0));
		fundamental.set(1,2, W.get(5,0));
		fundamental.set(2,0, W.get(6,0));
		fundamental.set(2,1, W.get(7,0));
		fundamental.set(2,2, W.get(8,0));
		
		fundamental = MatrixUtils.reduceRank(fundamental, 2);
		fundamental = normalisations.secondObject().transpose().times(fundamental).times(normalisations.firstObject());
		return fundamental;
	}
	
	public static Matrix homographyMatrix(List<? extends IndependentPair<Point2d, Point2d>> data) {
		Matrix A, W=null;
		int i, j;
		
		Pair<Matrix> normalisations = getNormalisations(data);
		
		A = new Matrix(data.size()*2, 9);
		
		for ( i=0, j=0; i<data.size(); i++, j+=2 ) {
			Point2d p1 = data.get(i).firstObject().transform(normalisations.firstObject());
			Point2d p2 = data.get(i).secondObject().transform(normalisations.secondObject());
			float x1 = p1.getX();
			float y1 = p1.getY();
			float x2 = p2.getX();
			float y2 = p2.getY();
			
			A.set(j, 0, x1);			//x
			A.set(j, 1, y1);			//y
			A.set(j, 2, 1);				//1
			A.set(j, 3, 0);				//0
			A.set(j, 4, 0);				//0
			A.set(j, 5, 0);				//0
			A.set(j, 6, -(x2*x1));		//-x'*x
			A.set(j, 7, -(x2*y1));		//-x'*y
			A.set(j, 8, -(x2));			//-x'
			
			A.set(j+1, 0, 0);			//0
			A.set(j+1, 1, 0);			//0
			A.set(j+1, 2, 0);			//0
			A.set(j+1, 3, x1);			//x
			A.set(j+1, 4, y1);			//y
			A.set(j+1, 5, 1);			//1
			A.set(j+1, 6, -(y2*x1));	//-y'*x
			A.set(j+1, 7, -(y2*y1));	//-y'*y
			A.set(j+1, 8, -(y2));		//-y'
		}
		
		/*
		 * The JAMA SVD method seems to be broken in some cases (m<n),
		 * like when we are trying to do a four-point estimate...*/
//		SingularValueDecomposition svd = A.svd();
//		W = svd.getV().getMatrix(0, 8, 8, 8);
		/* */
		
		//This is a hack to use MJT instead
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(A.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
			
			W = new Matrix(svd.getVt().numRows(), 1);
			
			for (i=0; i<svd.getVt().numRows(); i++) {
				W.set(i, 0, svd.getVt().get(8, i)); //do transpose here too!
			}	
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
			return null;
		}
		//End hack

		Matrix homography = new Matrix(3,3);
		homography.set(0,0, W.get(0,0));
		homography.set(0,1, W.get(1,0));
		homography.set(0,2, W.get(2,0));
		homography.set(1,0, W.get(3,0));
		homography.set(1,1, W.get(4,0));
		homography.set(1,2, W.get(5,0));
		homography.set(2,0, W.get(6,0));
		homography.set(2,1, W.get(7,0));
		homography.set(2,2, W.get(8,0));
		
		homography = normalisations.secondObject().inverse().times(homography).times(normalisations.firstObject());
		//it probably makes sense to rescale the matrix here by 1 / tf[2][2], unless tf[2][2] == 0
		if (Math.abs(homography.get(2,2)) > 0.000001) {
			double tmp = homography.get(2,2);
			
			for (i=0; i<3; i++) {
				for (j=0; j<3; j++) {
					if (Math.abs(homography.get(i, j)) < 10e-10) {
						homography.set(i, j, 0.0);
					} else {
						homography.set(i, j, homography.get(i, j) / tmp);
					}
				}
			}
		}
		
		return homography;
	}
	/**
	 * Given a point x and y, calculate the 2x2 affine transform component of the 3x3 homography provided such that:
	 * 
	 * H = AH_p
	 * H = {
	 * 		{h11,h12,h13},
	 * 		{h21,h22,h23},
	 * 		{h31,h32,h33}
	 * }
	 * H_p = {
	 * 		{1,0,0},
	 * 		{0,1,0},
	 * 		{h31,h32,1}
	 * }
	 * A = {
	 * 		{a11,a12,a13},
	 * 		{a21,a22,a23},
	 * 		{0,0,1}
	 * }
	 * 
	 * so 
	 * @param x
	 * @param y
	 * @param homography
	 * @return
	 */
	public static Matrix homographyToAffine( Matrix homography){
		double h11 = homography.get(0, 0);
		double h12 = homography.get(0, 1);
		double h13 = homography.get(0, 2);
		double h21 = homography.get(1, 0);
		double h22 = homography.get(1, 1);
		double h23 = homography.get(1, 2);
		double h31 = homography.get(2, 0);
		double h32 = homography.get(2, 1);
		double h33 = homography.get(2, 2);
		
		Matrix affine = new Matrix(3,3);
		affine.set(0, 0, h11 - (h13 * h31) / h33);
		affine.set(0, 1, h12 - (h13 * h32) / h33);
		affine.set(0, 2, h13 / h33);
		affine.set(1, 0, h21 - (h23 * h31) / h33);
		affine.set(1, 1, h22 - (h23 * h32) / h33);
		affine.set(1, 2, h23 / h33);
		affine.set(2, 0, 0);
		affine.set(2, 1, 0);
		affine.set(2, 2, 1);
		
		return affine;
	}
	public static Matrix homographyToAffine(Matrix homography, double x, double y) {
		double h11 = homography.get(0, 0);
		double h12 = homography.get(0, 1);
		double h13 = homography.get(0, 2);
		double h21 = homography.get(1, 0);
		double h22 = homography.get(1, 1);
		double h23 = homography.get(1, 2);
		double h31 = homography.get(2, 0);
		double h32 = homography.get(2, 1);
		double h33 = homography.get(2, 2);
		
		Matrix affine = new Matrix(3,3);
		double fxdx = h11/(h31*x + h32*y +h33) - (h11*x + h12*y +h13)*h31/Math.pow((h31*x + h32*y +h33),2);
		double fxdy = h12/(h31*x + h32*y +h33) - (h11*x + h12*y +h13)*h32/Math.pow((h31*x + h32*y +h33),2);

		double fydx = h21/(h31*x + h32*y +h33) - (h21*x + h22*y +h23)*h31/Math.pow((h31*x + h32*y +h33),2);
		double fydy = h22/(h31*x + h32*y +h33) - (h21*x + h22*y +h23)*h32/Math.pow((h31*x + h32*y +h33),2);
		affine.set(0, 0, fxdx);
		affine.set(0, 1, fxdy);
		affine.set(0, 2, 0);
		affine.set(1, 0, fydx);
		affine.set(1, 1, fydy);
		affine.set(1, 2, 0);
		affine.set(2, 0, 0);
		affine.set(2, 1, 0);
		affine.set(2, 2, 1);
		
		return affine;
	}
	
	public static Matrix makeTransform(Rectangle from, Rectangle to) {
		Point2d trans = to.getTopLeft().minus(from.getTopLeft());
		double scaleW = to.getWidth() / from.getWidth();
		double scaleH = to.getHeight() / from.getHeight();
		
		return TransformUtilities.translateMatrix(trans.getX(), trans.getY()).times(TransformUtilities.scaleMatrix(scaleW, scaleH));
	}
}
