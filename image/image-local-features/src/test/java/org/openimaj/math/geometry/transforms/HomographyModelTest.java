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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Test the homography model
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HomographyModelTest {
	private MBFImage pallet;
	private Rectangle square;
	private ArrayList<Point2d> randomPoints;
	private int squareWidth;
	private int squareHeight;
	private int squareX;
	private int squareY;
	private int nPoints;
	/**
	 * Create a random set of points in a square area
	 */
	@Before public void setup(){
		squareX = 50;
		squareY = 50;
		squareWidth = 100;
		squareHeight = 100;
		nPoints = 6;
		square = new Rectangle(squareX ,squareY ,squareWidth ,squareHeight );
		randomPoints = new ArrayList<Point2d>();
		
		int[] randomX = RandomData.getRandomIntArray(1,nPoints, squareX+1, squareX + squareWidth - 1, 0)[0];
		int[] randomY = RandomData.getRandomIntArray(1,nPoints, squareX+1, squareX + squareWidth - 1, 0)[0];
		for(int i = 0; i < nPoints ; i++){
			randomPoints.add(new Point2dImpl(randomX[i],randomY[i]));
		}
	}
	
	/**
	 * Transform the square by a random transform and see if you can find the square again by using a RANSAC
	 * model and a homography model. Apply some error the points once transformed to make things a bit more interesting
	 */
	@Test public void testRandomSquareTransform()
	{
		
		Matrix trans = Matrix.identity(3, 3);
		trans = TransformUtilities.centeredRotationMatrix(Math.PI/3.0, 2*(squareX + squareWidth/2) , 2*(squareY + squareHeight/2) ).times(trans);
		trans = TransformUtilities.translateMatrix(200, 200).times(trans);
//		trans = TransformUtilities.rotationMatrix(Math.PI/3.0);
		
		ArrayList<Point2d> transformedPoints = new ArrayList<Point2d>();
		final List<IndependentPair<Point2d,Point2d>> pairs = new ArrayList<IndependentPair<Point2d,Point2d >>();
		Random r = new Random();
		int error = 3;
		float stoppingCondition = 0.8f;
		for(Point2d randomPoint : randomPoints){
			Point2dImpl pointTrans = ((Point2dImpl)randomPoint).transform(trans);
			
			pointTrans.setX(pointTrans.getX() + (r.nextFloat()) * error * (r.nextBoolean() ? -1 : 1));
			pointTrans.setY(pointTrans.getY() + (r.nextFloat()) * error * (r.nextBoolean() ? -1 : 1));
			
			transformedPoints.add(pointTrans);
			pairs.add(new IndependentPair<Point2d, Point2d>(randomPoint,pointTrans));
		}
		
		int i = 0;
		while(i++  < 10){
			pallet = new MBFImage(new FImage[]{new FImage(500,500),new FImage(500,500),new FImage(500,500)});
			MBFImageRenderer renderer = pallet.createRenderer();
			
			HomographyModel model = new HomographyModel(((float) Math.sqrt(2*error*error)*2) + 1);
			model.estimate(pairs);
			renderer.drawPolygon(this.square.asPolygon().transform(model.getTransform()), 1,RGBColour.ORANGE);
			RANSAC<Point2d,Point2d> fitterNormal = new RANSAC<Point2d,Point2d>(model,1500,new RANSAC.PercentageInliersStoppingCondition(stoppingCondition),false);
			Matrix fitterNormalTransform  = null;
			renderer.drawPolygon(this.square.asPolygon(), 1,RGBColour.RED);
			renderer.drawPolygon(this.square.asPolygon().transform(trans), 1,RGBColour.RED);
			renderer.drawPoints(randomPoints, RGBColour.GREEN, 3);
			renderer.drawPoints(transformedPoints, RGBColour.GREEN, 3);
			if(fitterNormal.fitData(pairs))
			{
				fitterNormalTransform  = model.getTransform().copy();
				renderer.drawPolygon(this.square.asPolygon().transform(fitterNormalTransform), 1,RGBColour.YELLOW);
							
				List<? extends IndependentPair<Point2d, Point2d>> inlierPairs = fitterNormal.getBestInliers(pairs);
				System.out.println("Number of best inliers for recalculation: " + inlierPairs.size());
				model.estimate(inlierPairs);
				Matrix fitterRefitTransform = model.getTransform().copy();
				renderer.drawPolygon(this.square.asPolygon().transform(fitterRefitTransform), 1,RGBColour.CYAN);
				assertTrue(inlierPairs.size() >= nPoints * stoppingCondition);
				
			}
			else{
				fitterNormalTransform  = model.getTransform().copy();
				List<? extends IndependentPair<Point2d, Point2d>> mData = fitterNormal.getModelConstructionData();
				for (IndependentPair<Point2d, Point2d> independentPair : mData) {
//					Point2d a = independentPair.firstObject();
					Point2d b = independentPair.secondObject();
					renderer.drawPoint(b, RGBColour.PINK, 4);
				}
				renderer.drawPolygon(this.square.asPolygon().transform(fitterNormalTransform), 1,RGBColour.GREEN);
			}
		}
		
	}
	
	/**
	 * 
	 */
	@Test public void testKnownDifficult(){
		MBFImage image = new MBFImage(400,400,3);
		MBFImageRenderer renderer = image.createRenderer();
		double [] A = {217.0, 128.0, 1, 200.0, 124.0, 1, 210.0, 118.0, 1, 180.0, 116.0, 1 };
		double [] B = {215.0, 89.0, 1, 198.0, 84.0, 1, 208.0, 76.0, 1, 180.0, 76.0, 1 };
//		double [] B = {
//			A[0], A[1]+20, A[2],
//			A[3], A[4]+20, A[5],
//			A[6], A[7]+20, A[8],
//			A[9], A[10]+20, A[11]
//		};
		
		Rectangle squareA = new Rectangle(180,116,40,20);

		List<IndependentPair<Point2d, Point2d>> data = new ArrayList<IndependentPair<Point2d, Point2d>>();
		
		for (int i=0; i<A.length; i+=3) {
			Point2dImpl pA = new Point2dImpl((float)A[i], (float)A[i+1]);
			Point2dImpl pB = new Point2dImpl((float)B[i], (float)B[i+1]);
			renderer.drawPoint(pA, RGBColour.GREEN, 3);
			renderer.drawPoint(pB, RGBColour.RED, 3);
			renderer.drawLine(new Line2d(pA, pB), 1,RGBColour.WHITE);
			data.add(new IndependentPair<Point2d,Point2d>(pA, pB));
		}
		
		for (IndependentPair<Point2d,Point2d> p : data) {
			System.out.println(p.firstObject().getX() + " " + p.firstObject().getY()); 
		}
		for (IndependentPair<Point2d,Point2d> p : data) {
			System.out.println(p.secondObject().getX() + " " + p.secondObject().getY());
		}
		
		float error = 0.5f;
		
		HomographyModel model = new HomographyModel(((float) Math.sqrt(2*error*error)*2) + 1);
		model.estimate(data);
		model.getTransform().print(5, 5);
		renderer.drawPolygon(squareA.asPolygon(),1,RGBColour.RED);
		renderer.drawPolygon(squareA.asPolygon().transform(model.getTransform()), 1,RGBColour.ORANGE);
	}
}
