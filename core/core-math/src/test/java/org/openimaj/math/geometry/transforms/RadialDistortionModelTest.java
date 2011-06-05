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

import org.junit.Before;
import org.junit.Test;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.util.pair.IndependentPair;

public class RadialDistortionModelTest {

	private Point2d[] training;
	private Point2dImpl middle;

	@Before public void setup(){
		training = new Point2d[]{
			new Point2dImpl(125,287),
			new Point2dImpl(151,292),
			new Point2dImpl(195,296),
			new Point2dImpl(244,292),
			new Point2dImpl(275,286),
		};
		
		middle = new Point2dImpl(200,200);
		
		for(int i = 0 ; i < training.length; i++){
			training[i].setX(middle.x - training[i].getX() );
			training[i].setY(middle.y - training[i].getY() );
		}
	}
	
	@Test public void testRadialModel(){
		Line2d line = new Line2d(training[0],training[training.length-1]);
		RadialDistortionModel model = new RadialDistortionModel(8);
		List<IndependentPair<Point2d,Point2d>> pairs = new ArrayList<IndependentPair<Point2d,Point2d>>();
		for(int i = 1; i < training.length -1 ; i++){
			IndependentPair<Point2d, Point2d> pair = RadialDistortionModel.getRadialIndependantPair(line, training[i]);
			pairs.add(pair);
		}
		
		model.estimate(pairs);
		model.matrixK.print(5, 5);
		for(int i = 1; i < training.length -1 ; i++){
			System.out.println(training[i] + " predicted to: " + model.predict(training[i]));
		}
		
		assertTrue(model.calculateError(pairs) < 1);
	}
	
//	@Test public void testRadialTransform(){
//		Matrix kMatrix = new Matrix(new double[][]{
//				{1,0.2,0.2,1}
//		});
//		
//		RadialDistortionModel model = new RadialDistortionModel(8,1,1);
//		model.setKMatrix(kMatrix);
//		
//		for(int i = 0; i < 10; i++){
//			for(int j = 0; j < 10; j++){
//				Point2d point = new Point2dImpl(i,j);
//				Point2d warpedCameraPoint = model.reverse(point);
//				Point2d unwarpedCameraPoint = model.predict(warpedCameraPoint);
//				System.out.println(point + "->" + warpedCameraPoint + "->" + unwarpedCameraPoint);
//			}
//		}
//	}
}
