package org.openimaj.math.geometry.shape;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2dImpl;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;

public class PointDistributionModelTest {
	MersenneTwister twister = new MersenneTwister();
	
	PointList randomTriangle(Normal rndX, Normal rndY) {
		return new Triangle(
			new Point2dImpl(0 + (float)rndX.nextDouble(), 0 + (float)rndY.nextDouble()), 
			new Point2dImpl(1 + (float)rndX.nextDouble(), 1 + (float)rndY.nextDouble()),
			new Point2dImpl(1 + (float)rndX.nextDouble(), 0 + (float)rndY.nextDouble())
		).asPolygon();
	}
	
	List<PointList> generateSamples() {
		List<PointList> list = new ArrayList<PointList>();
		
		Normal rndX = new Normal(0, 0.2, twister);
		Normal rndY = new Normal(0, 0.02, twister);
		
		for (int i=0; i<100; i++) {
			list.add(randomTriangle(rndX, rndY));
		}
		
		return list;
	}
	
	@Test
	public void testFitting() {
		List<PointList> samples = generateSamples();
		PointDistributionModel pdm = new PointDistributionModel(samples);
		pdm.setNumComponents(2);
		
		for (int i=0; i<10; i++) {
			double [] scaling = {twister.uniform(-2, 2), twister.uniform(-2, 2)};
			PointList newShape = pdm.generateNewShape(scaling);
			double[] predictedScaling = pdm.fitModel(newShape);
		
			assertArrayEquals(scaling, predictedScaling, 0.01);
		}
	}
}
