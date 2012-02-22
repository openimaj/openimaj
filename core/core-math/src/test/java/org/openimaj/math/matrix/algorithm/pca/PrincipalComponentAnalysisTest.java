package org.openimaj.math.matrix.algorithm.pca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.util.FloatArrayStatsUtils;

import Jama.Matrix;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Abstract base for all {@link PrincipalComponentAnalysis} test classes
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public abstract class PrincipalComponentAnalysisTest {
	private MersenneTwister mt = new MersenneTwister();
	protected PrincipalComponentAnalysis pca;
	
	/**
	 * init the pca
	 */
	@Before
	public void init() {
		this.pca = createPCA();
	}
	
	protected abstract PrincipalComponentAnalysis createPCA();
	
	protected Matrix makeData2d(int samples, double sdx, double sdy, double r) {
		Matrix m = new Matrix(samples, 2);
	
		Matrix rot = TransformUtilities.rotationMatrix(r);
		
		Normal rndX = new Normal(0, sdx, mt);
		Normal rndY = new Normal(0, sdy, mt);
		
		for (int i = 0; i < samples; i++) {
			Point2dImpl pt = new Point2dImpl((float)rndX.nextDouble(), (float)rndY.nextDouble());
			pt = pt.transform(rot);
			
			m.set(i, 0, pt.x);
			m.set(i, 1, pt.y);
		}
		
		return m;
	}
	
	/**
	 * Generate some random 2d gaussian data with high variance 
	 * in one dimension with a random rotation and check that the pca
	 * removes the rotation
	 */
	@Test
	public void testPCA1() {
		Uniform u = new Uniform(mt);
		double stdx = 10;
		double stdy = 1;
		
		for (int i=0; i<10; i++) {
			double rotation = u.nextDoubleFromTo(0, Math.PI);
			Matrix m = makeData2d(1000, stdx, stdy, rotation);

			pca.learnBasis(m);
			pca.basis.print(5, 5);
			
			assertTrue(
					Math.abs(Math.cos(rotation) - pca.basis.get(0, 0)) < 0.1 ||
					Math.abs(Math.cos(rotation - Math.PI) - pca.basis.get(0, 0)) < 0.1
			);
			
			Matrix projected = pca.project(m);
			
			float [] xs = new float[projected.getRowDimension()];
			float [] ys = new float[projected.getRowDimension()];
			
			for (int j=0; j<projected.getRowDimension(); j++) {
				xs[j] = (float) projected.get(j, 0);
				ys[j] = (float) projected.get(j, 1);
			}
			
			assertEquals(FloatArrayStatsUtils.std(xs), stdx, 1.0);
			assertEquals(FloatArrayStatsUtils.std(ys), stdy, 1.0);
		}
	}
}
