package org.openimaj.math.matrix;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestGramSchmidt {
	
	private static final double eps = 0.00001;

	@Test
	public void randomTest() throws Exception{
		Random r = new Random(1);
		GramSchmidtProcess gsp = new GramSchmidtProcess(1);
		for (int i = 0; i < 1000; i++) {
			int dim = r.nextInt(100)+2;
			double[] random = randomVec(dim,r);
			
			Vector[] orthonormal = gsp.apply(random);
			try{
				assertAllUnequalAndOrthogonal(orthonormal);
			}catch(Exception e){
				for (Vector vector : orthonormal) {
					System.out.println(vector);
				}
				throw e;
			}
			
		}
	}

	private void assertAllUnequalAndOrthogonal(Vector[] orthonormal) throws Exception {
		for (int i = 0; i < orthonormal.length; i++) {
			for (int j = i+1; j < orthonormal.length; j++) {
				double diff = orthonormal[i].copy().add(-1, orthonormal[j]).norm(Norm.TwoRobust);
				if(Math.abs(diff) < eps) throw new Exception(String.format("%s and %s are too close!",orthonormal[i],orthonormal[j]));
				double dot = orthonormal[i].dot(orthonormal[j]);
				if(dot>eps) throw new Exception(String.format("%s and %s are not othogonal, dot = %2.5f",orthonormal[i],orthonormal[j],dot));
			}
		}
	}

	private double[] randomVec(int dim, Random r) {
		double[] ret = new double[dim];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = r.nextDouble();
		}
		return ret;
	}

}
