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
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
