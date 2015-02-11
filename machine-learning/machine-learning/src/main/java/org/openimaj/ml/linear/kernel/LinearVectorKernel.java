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
package org.openimaj.ml.linear.kernel;

import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.openimaj.math.matrix.GramSchmidtProcess;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LinearVectorKernel implements VectorKernel{

	@Override
	public Double apply(IndependentPair<double[], double[]> in) {
		double[] first = in.firstObject();
		double[] second = in.secondObject();
		return new DenseVector(first,false).dot(new DenseVector(second,false));
	}

	/**
	 * On the plane
	 * @param supports
	 * @param weights 
	 * @param bias
	 * @param setValues the dimentions of the point set to 0
	 * @return a point on the plane
	 */
	public static double[] getPlanePoint(List<double[]> supports, List<Double> weights, double bias, double ... setValues) {
		if(supports.size() == 0) throw new RuntimeException("Can't estimate plane point without supports");
		
		double[] w = getDirection(supports, weights);
		double[] x = new double[w.length];
		double resid = 0;
		int index = 0;
		for (int i = 0; i < w.length; i++) {
			
			if(Double.isNaN(setValues[i])){
				index = i;
			} else {
				resid += (setValues[i] * w[i]);
				x[i] = setValues[i];
			}
		}
		if(w[index] == 0) return new double[w.length];
		x[index] = (bias + resid)/ -w[index];
		return x;
	}

	/**
	 * @param supports
	 * @param weights 
	 * @return the vectors defining the plane
	 */
	public static Vector[] getPlaneDirections(List<double[]> supports, List<Double> weights) {
		double[] dir = getDirection(supports, weights);
		int ind = 0;
//		for (int i = 0; i < weights.size(); i++) {
//			double[] ds = supports.get(i);
//			System.out.println("Support " + ind++ + ": " + weights.get(i) + " * " +Arrays.toString(ds) );
//		}
//		System.out.println("Number of supports: " + supports.size() + " direction: " + Arrays.toString(dir));
		Vector[] all = GramSchmidtProcess.perform(dir);
		Vector[] ret = new Vector[all.length-1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = all[i+1];
		}
		return ret;
	}
	

	public static double[] getDirection(List<double[]> supports, List<Double> weights) {
		Vector ret = null;
		for (int i = 0; i < supports.size(); i++) {
			double[] sup = supports.get(i);
			double weight = weights.get(i);
			DenseVector scale = new DenseVector(sup).scale(weight);
			if(ret == null){
				ret = scale;
			} else{
				ret.add(scale);
			}
		}
		double[] retdata = new DenseVector(ret).getData();
		return retdata;
	}

}
