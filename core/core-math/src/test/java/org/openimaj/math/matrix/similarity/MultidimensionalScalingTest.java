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
package org.openimaj.math.matrix.similarity;

import org.junit.Test;

/**
 * Tests for Multidimensional Scaling
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MultidimensionalScalingTest {
	/**
	 * Test basic operation. Two points, A & B are close and the third point, C,
	 * is very different. The layout created by MDS should respect this.
	 */
	@Test
	public void testMDS1() {
		// final double[][] sims = { { 1, 0.1, 0, 0.99 }, { 0.1, 1, 0.01, 0 }, {
		// 0, 0.01, 1, 0 }, { 0.99, 0, 0, 1 } };
		// final String[] index = { "A", "B", "C", "D" };
		//
		// final SimilarityMatrix m = new SimilarityMatrix(index, sims);
		// final MultidimensionalScaling mds = new
		// MultidimensionalScaling(10000, 0.01, new Random(0L));
		// m.process(mds);
		//
		// System.out.println(m);
		// System.out.println(mds.getPoints());
		//
		// final Point2d ptA = mds.getPoint("A");
		// final Point2d ptB = mds.getPoint("B");
		// final Point2d ptC = mds.getPoint("C");
		//
		// final double AB = Line2d.distance(ptA, ptB);
		// final double AC = Line2d.distance(ptA, ptC);
		// final double BC = Line2d.distance(ptB, ptC);
		//
		// assertTrue(AB < AC);
		// assertTrue(AB < BC);
	}
}
