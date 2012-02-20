package org.openimaj.math.geometry.shape.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.Triangle;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Tests for {@link ProcrustesAnalysis}
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class GeneralisedProcrustesAnalysisTest {

	/**
	 * Test alignment by generating random transforms
	 * and then recovering them
	 */
	@Test
	public void testAlignment() {
		Uniform rnd = new Uniform(new MersenneTwister(1));
		List<PointList> shapes = new ArrayList<PointList>();
		
		for (int i=0; i<2; i++) {
			shapes.add(randomTriangle(rnd));
		}

		System.out.println(shapes);
		GeneralisedProcrustesAnalysis.alignPoints(shapes, 0.1f);
		System.out.println(shapes);
	}

	PointList randomTriangle(Uniform rnd) {
		return new Triangle(
			new Point2dImpl(rnd.nextFloatFromTo(0, 200), rnd.nextFloatFromTo(0, 200)), 
			new Point2dImpl(rnd.nextFloatFromTo(0, 200), rnd.nextFloatFromTo(0, 200)),
			new Point2dImpl(rnd.nextFloatFromTo(0, 200), rnd.nextFloatFromTo(0, 200))
		).asPolygon();
	}
}
