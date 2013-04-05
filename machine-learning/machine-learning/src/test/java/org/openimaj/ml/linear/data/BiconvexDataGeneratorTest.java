package org.openimaj.ml.linear.data;

import static org.junit.Assert.*;
import gov.sandia.cognition.math.matrix.Matrix;

import org.junit.Test;
import org.openimaj.util.pair.Pair;

public class BiconvexDataGeneratorTest {
	
	@Test
	public void testBiconvex(){
		BiconvexDataGenerator gen = new BiconvexDataGenerator();
		System.out.println(gen.getW());
		for (int i = 0; i < 100; i++) {
			Pair<Matrix> xy = gen.generate();
			Matrix x = xy.firstObject();
			Matrix y = xy.secondObject();
			
			double error = SSE(gen, x, y);
			assertEquals(error, 0,0.1);
		}
	}

	private double SSE(BiconvexDataGenerator gen, Matrix x, Matrix y) {
		double sumError = y.minus(
				gen.getU().transpose().times(x.transpose()).times(gen.getW())
		).sumOfColumns().sum();
		return sumError;
	}
}
