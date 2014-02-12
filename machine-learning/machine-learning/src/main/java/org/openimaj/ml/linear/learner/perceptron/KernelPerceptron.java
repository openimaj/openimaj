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

import java.util.List;

import org.openimaj.ml.linear.kernel.Kernel;
import org.openimaj.ml.linear.learner.OnlineLearner;

/**
 *
 * @param <INDEPENDANT>
 * @param <DEPENDANT>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public abstract class KernelPerceptron<INDEPENDANT, DEPENDANT> implements OnlineLearner<INDEPENDANT, DEPENDANT>{

	
	Kernel<INDEPENDANT> kernel;
	protected int errors;
	
	/**
	 * 
	 */
	public KernelPerceptron() {
	}
	
	/**
	 * @param kernel
	 */
	public KernelPerceptron(Kernel<INDEPENDANT> kernel) {
		this.kernel = kernel;
	}
	
	@Override
	public void process(INDEPENDANT xt, DEPENDANT yt) {
		DEPENDANT yt_prime = predict(xt);
		if(!yt_prime.equals(yt)){
			update(xt,yt,yt_prime);
			this.errors ++;
		}
	}

	/**
	 * When there is an error in prediction, update somehow
	 * @param xt
	 * @param yt
	 * @param yt_prime
	 */
	public abstract void update(INDEPENDANT xt, DEPENDANT yt, DEPENDANT yt_prime) ;
	
	/**
	 * @return the vectors that form the support
	 */
	public abstract List<INDEPENDANT> getSupports();
	/**
	 * @return the weights of the support vectors
	 */
	public abstract List<Double> getWeights();
	
	/**
	 * @return the bias
	 */
	public abstract double getBias();
	
	/**
	 * @return number of errors made
	 */
	public int getErrors(){
		return errors;
		
	}
	
}
