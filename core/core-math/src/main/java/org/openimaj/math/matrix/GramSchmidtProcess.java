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

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;

import org.openimaj.util.function.Function;

/**
 * Perform the Gram-Schmid process on a vector, returning the orthogonal basis set 
 * whose first vector is the input
 * 
 * http://zintegra.net/archives/738
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class GramSchmidtProcess implements Function<double[],Vector[]>{
	
	Random r = new Random();
	/**
	 * an unseeded random start
	 */
	public GramSchmidtProcess() {
	}
	/**
	 * Set the random seed of the purtubations
	 * @param seed
	 */
	public GramSchmidtProcess(int seed) {
		this.r = new Random(seed);
	}

	private Vector project(Vector v, Vector u){
		return u.copy().scale((v.dot(u) / u.dot(u)));
	}

	@Override
	public Vector[] apply(double[] in) {
		Vector[] vmat = new Vector[in.length];
		
		vmat[0] = new DenseVector(in);
		double norm = vmat[0].norm(Norm.Two);
		vmat[0].scale(1/norm);
		for (int j = 1; j < in.length; j++) {
			Vector randvec = randvec(vmat[0].size(),norm);
			vmat[j] = new DenseVector(vmat[0]).add(randvec);
			for (int i = 0; i < j; i++) {
				vmat[j].add(-1, project(vmat[j],vmat[i]));
			}
			vmat[j].scale(1/vmat[j].norm(Norm.Two));
		}
		return vmat;
	}

	private Vector randvec(int nvec, double d) {
		double[] ret = new double[nvec];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = d * r.nextDouble();
		}
		return new DenseVector(ret);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GramSchmidtProcess proc = new GramSchmidtProcess();
		
		Vector[] allvec = proc.apply(new double[]{0,0,1});
		for (Vector vector : allvec) {
			System.out.println(vector);
		}
	}
	/**
	 * @param dir
	 * @return construct and perform a {@link GramSchmidtProcess}
	 */
	public static Vector[] perform(double[] dir) {
		
		return new GramSchmidtProcess(0).apply(dir);
	}
	
}
