package org.openimaj.math.geometry.shape.algorithm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Tests for {@link ProcrustesAnalysis}
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ProcrustesAnalysisTest {

	/**
	 * Test alignment by generating random transforms
	 * and then recovering them
	 */
	@Test
	public void testAlignment() {
		Uniform rnd = new Uniform(new MersenneTwister(1));
		
		for (int i=0; i<10; i++) {
			doRandomTest(rnd);
		}
	}
	
	protected void doRandomTest(Uniform rnd) {
		PointList pl = new PointList(
				new Point2dImpl(-1,-1), new Point2dImpl(1,-1), new Point2dImpl(1,1), new Point2dImpl(-1,1) 	
		);

		
		double tx = rnd.nextDoubleFromTo(-100, 100);
		double ty = rnd.nextDoubleFromTo(-100, 100);
		double r = rnd.nextDoubleFromTo(-Math.PI, Math.PI);
		double s = rnd.nextDoubleFromTo(0, 10);
		
		Matrix tf = TransformUtilities.translateMatrix(tx, ty).times(TransformUtilities.scaleMatrix(s, s)).times(TransformUtilities.rotationMatrix(r));
		PointList pl2 = pl.transform(tf);
		
		float odist = ProcrustesAnalysis.computeProcrustesDistance(pl, pl2);
		
		ProcrustesAnalysis pa = new ProcrustesAnalysis(pl);
		Matrix patf = pa.align(pl2);
		
		assertTrue(odist >= ProcrustesAnalysis.computeProcrustesDistance(pl, pl2));

		for (int i=0; i<pl.points.size(); i++) {
			assertEquals(pl.points.get(i).getX(), pl2.points.get(i).getX(), 0.0001);
			assertEquals(pl.points.get(i).getY(), pl2.points.get(i).getY(), 0.0001);
		}
		
		Matrix tfInv = tf.inverse();
		
		for (int i=0; i<3; i++) {
			assertArrayEquals(tfInv.getArray()[i], patf.getArray()[i], 0.0001);
		}
	}

}
