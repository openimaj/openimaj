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
