package org.openimaj.math.matrix.similarity;

import org.junit.Test;
import org.openimaj.math.matrix.similarity.processor.MultidimensionalScaling;

/**
 * Tests for Multidimensional Scaling
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class MultidimensionalScalingTest {
	/**
	 * Test basic operation. Two points, A & B are close and
	 * the third point, C, is very different. The layout created
	 * by MDS should respect this. 
	 */
	@Test
	public void testMDS1() {
		double [][] sims = {{1, 0.1, 0}, {0.1, 1, 0.01}, {0, 0.01, 1}};
		String [] index = {"A", "B", "C"};

		SimilarityMatrix m = new SimilarityMatrix(index, sims);
		MultidimensionalScaling mds = new MultidimensionalScaling();
		m.process(mds);
		
//		Point2d ptA = mds.getPoint("A");
//		Point2d ptB = mds.getPoint("B");
//		Point2d ptC = mds.getPoint("C");
		
//		double AB = Line2d.distance(ptA, ptB);
//		double AC = Line2d.distance(ptA, ptC);
//		double BC = Line2d.distance(ptB, ptC);
		
//		assertTrue(AB < AC);
//		assertTrue(AB < BC);
	}
}
