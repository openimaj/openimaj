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
package org.openimaj.ml.linear.learner.perceptron;


import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.matrix.MeanVector;
import org.openimaj.ml.linear.kernel.VectorKernel;
/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MeanCenteredProjectron extends Projectron{
	MeanVector mv = new MeanVector();
	/**
	 * @param kernel 
	 * 
	 */
	public MeanCenteredProjectron(VectorKernel kernel) {
		super(kernel);
	}
	
	/**
	 * @param kernel
	 * @param eta
	 */
	public MeanCenteredProjectron(VectorKernel kernel, double eta) {
		super(kernel, eta);
	}
	
	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		mv.update(xt);
		super.update(xt, yt, yt_prime);
	}
	@Override
	public double[] correct(double[] in) {
		return center(in);
	}
	
	private double[] center(double[] xt) {
		double[] mvec = mv.vec();
		double[] ret = new double[xt.length];
		if(mvec == null) return ret;
		
		for (int i = 0; i < mvec.length; i++) {
			ret[i] = xt[i] - mvec[i];
		}
		return ret;
	}
	
	@Override
	public List<double[]> getSupports() {
		List<double[]> pre = super.getSupports();
		List<double[]> ret = new ArrayList<double[]>();
		for (double[] ds : pre) {
			ret.add(correct(ds));
		}
		return ret;
	}
	
	public double[] getMean() {
		return mv.vec();
	}
}
