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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class HomographyModelTest {
	private MBFImage pallet;
	private Rectangle square;
	private ArrayList<Point2d> randomPoints;
	private int squareWidth;
	private int squareHeight;
	private int squareX;
	private int squareY;
	@Before public void setup(){
		
		squareX = 50;
		squareY = 50;
		squareWidth = 100;
		squareHeight = 100;
		int nPoints = 100;
		square = new Rectangle(squareX ,squareY ,squareWidth ,squareHeight );
		randomPoints = new ArrayList<Point2d>();
		
		int[] randomX = RandomData.getRandomIntArray(1,nPoints, squareX+1, squareX + squareWidth - 1)[0];
		int[] randomY = RandomData.getRandomIntArray(1,nPoints, squareX+1, squareX + squareWidth - 1)[0];
		for(int i = 0; i < nPoints ; i++){
			randomPoints.add(new Point2dImpl(randomX[i],randomY[i]));
		}
	}
	
	@Test public void testRandomSquareTransform()
	{
		
		Matrix trans = Matrix.identity(3, 3);
		trans = TransformUtilities.centeredRotationMatrix(Math.PI/3.0, 2*(squareX + squareWidth/2) , 2*(squareY + squareHeight/2) ).times(trans);
		trans = TransformUtilities.translateMatrix(200, 200).times(trans);
//		trans = TransformUtilities.rotationMatrix(Math.PI/3.0);
		
		ArrayList<Point2d> transformedPoints = new ArrayList<Point2d>();
		List<IndependentPair<Point2d,Point2d>> pairs = new ArrayList<IndependentPair<Point2d,Point2d >>();
		Random r = new Random();
		int error = 5;
		float stoppingCondition = 0.8f;
		for(Point2d randomPoint : randomPoints){
			Point2dImpl pointTrans = ((Point2dImpl)randomPoint).transform(trans);
			
			pointTrans.setX(pointTrans.getX() + (r.nextFloat()) * error * (r.nextBoolean() ? -1 : 1));
			pointTrans.setY(pointTrans.getY() + (r.nextFloat()) * error * (r.nextBoolean() ? -1 : 1));
			
			transformedPoints.add(pointTrans);
			pairs.add(new IndependentPair<Point2d, Point2d>(randomPoint,pointTrans));
		}
		
		
		JFrame frame = null;
		int i = 0;
		while(i++  < 10){
			pallet = new MBFImage(new FImage[]{new FImage(500,500),new FImage(500,500),new FImage(500,500)});
			HomographyModel model = new HomographyModel((float) Math.sqrt(2*error*error)*2);
			model.estimate(pairs);
			pallet.drawPolygon(this.square.asPolygon().transform(model.getTransform()), 1,RGBColour.ORANGE);
			RANSAC<Point2d,Point2d> fitterNormal = new RANSAC<Point2d,Point2d>(model,1500,new RANSAC.PercentageInliersStoppingCondition(stoppingCondition),false);
			Matrix fitterNormalTransform  = null;
			if(fitterNormal.fitData(pairs))
			{
				fitterNormalTransform  = model.getTransform().copy();
				pallet.drawPolygon(this.square.asPolygon().transform(fitterNormalTransform), 1,RGBColour.YELLOW);
							
				List<? extends IndependentPair<Point2d, Point2d>> inlierPairs = fitterNormal.getBestInliers(pairs);
				System.out.println("Number of best inliers for recalculation: " + inlierPairs.size());
				model.estimate(inlierPairs);
				Matrix fitterRefitTransform = model.getTransform().copy();
				pallet.drawPolygon(this.square.asPolygon().transform(fitterRefitTransform), 1,RGBColour.CYAN);
				assertTrue(inlierPairs.size() >50);
				
			}
			
			
			pallet.drawPolygon(this.square.asPolygon(), 1,RGBColour.RED);
			pallet.drawPoints(randomPoints, RGBColour.GREEN, 1);
			
			pallet.drawPolygon(this.square.asPolygon().transform(trans), 1,RGBColour.RED);
			pallet.drawPoints(transformedPoints, RGBColour.GREEN, 1);
			
			pallet = MatchingUtilities.drawMatches(pallet, pairs, RGBColour.WHITE);
			
//			frame = DisplayUtilities.display(pallet,frame);
//			frame.toString();
		}
		
	}
	
	public static void main(String args[]){
		HomographyModelTest test = new HomographyModelTest();
		test.setup();
		test.testRandomSquareTransform();
	}
}
