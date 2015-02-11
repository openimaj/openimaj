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
package org.openimaj.ml.linear.data;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.openimaj.math.matrix.GramSchmidtProcess;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LinearPerceptronDataGenerator implements DataGenerator<double[],PerceptronClass>{

	
	private Vector origin;
	private Vector direction;
	private Random rng;
	private double range;
	private int dims;
	private double prop;
	private double error = 0;

	
	/**
	 * @param range the range of values
	 * @param dims the number of dimentions
	 * @param prop for both the selection of the origin and selection of the direction of the line of seperate, one dimention is chosen to be limited to the middle of range by this proportion
	 */
	public LinearPerceptronDataGenerator(double range, int dims, double prop) {
		this(range,dims,prop,-1);
	}
	
	/**
	 * The range for each dimension
	 * @param range 
	 * @param dims 
	 * @param prop 
	 * @param seed 
	 */
	public LinearPerceptronDataGenerator(double range, int dims, double prop, int seed) {
		this.range = range;
		this.dims = dims;
		this.prop = prop;
		if(seed < 0){
			this.rng = new Random();			
		} else {
			this.rng = new Random(seed);
		}
		
		// limited dimention 
		int limitedDim = rng.nextInt(dims);
		double validRange = this.range * prop;
		double start = (this.range - validRange)/2.;
		
		double[] startPoint = new double[dims];
		double[] endPoint = new double[dims];
		
		double[] originPoint = new double[dims];
		
		for (int i = 0; i < endPoint.length; i++) {
			if(i == limitedDim){
				startPoint[i] = 0;
				endPoint[i] = range;
				originPoint[i] = start + rng.nextDouble() * validRange;
			} else {
				startPoint[i] = start + rng.nextDouble() * validRange;
				endPoint[i] = start + rng.nextDouble() * validRange;
				originPoint[i] = rng.nextDouble() * range;
			}
		}
		
		this.direction = (DenseVector) new DenseVector(endPoint).add(-1, new DenseVector(startPoint));
		this.origin = new DenseVector(originPoint);
		
	}

	private double nextRandomValue() {
		return rng.nextDouble() * range;
	}

	
	@Override
	public IndependentPair<double[], PerceptronClass> generate() {
		double decide = Math.signum(rng.nextDouble() - 0.5);
		if(decide == 0) decide = 1;
		PerceptronClass dec = PerceptronClass.fromSign(decide);
		while(true){			
			double[] randomPoint = new double[this.dims];
			
			for (int i = 0; i < randomPoint.length; i++) {
				randomPoint[i] = nextRandomValue(); 
			}
			
			Vector v = new DenseVector(randomPoint);
			v.add(-1, origin);
			Vector d = direction.copy();
			double dot = v.dot(d);
			double sgn = Math.signum(dot);
			if(sgn == 0) sgn = 1;
			if(rng.nextDouble() <this.error){
				sgn = -sgn; 
			}
			PerceptronClass sgnClass = PerceptronClass.fromSign(sgn);
			if(sgnClass.equals(dec)) {
				return IndependentPair.pair(randomPoint, sgnClass);
			}
		}
	}

	public Vector getOrigin() {
		return this.origin;
	}
	
	public Vector getNormDirection() {
		return this.direction;
	}

	public Vector[] getPlane() {
		Vector[] allInclusive = new GramSchmidtProcess().apply(new DenseVector(direction).getData());
		Vector[] ret = new Vector[allInclusive.length - 1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = allInclusive[i+1];
		}
		return ret;
	}

	public void setError(double d) {
		this.error  = d;
	}

}
