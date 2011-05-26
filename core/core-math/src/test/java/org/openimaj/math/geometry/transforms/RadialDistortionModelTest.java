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
}
