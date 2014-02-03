package org.openimaj.ml.linear.data;

import no.uib.cipr.matrix.Vector;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestLinearPerceptronDataGenerator {
	
	/**
	 * 
	 */
	@Test
	public void testDataGenerator(){
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(100, 10, 0.3, 1);
		Vector origin = dg.getOrigin();
		Vector dir = dg.getNormDirection();
		Point2d lineStart = start(origin,dir);
		Point2d lineEnd = end(origin,dir);
		
		for (int i = 0; i < 100; i++) {
			IndependentPair<double[], PerceptronClass> pointClass = dg.generate();
			
			double[] pc = pointClass.firstObject();
			Point2dImpl point = new Point2dImpl((float)pc[0], (float)pc[1]);
			PerceptronClass cls = pointClass.getSecondObject();
			System.out.println(String.format("%s: %s",point,cls));
		}
	}

	private Point2d end(Vector origin, Vector dir) {
		Vector ret = origin.copy().add(1000, dir);
		return new Point2dImpl((float)ret.get(0),(float)ret.get(1));
	}

	private Point2d start(Vector origin, Vector dir) {
		Vector ret = origin.copy().add(-1000, dir);
		return new Point2dImpl((float)ret.get(0),(float)ret.get(1));
	}

}
