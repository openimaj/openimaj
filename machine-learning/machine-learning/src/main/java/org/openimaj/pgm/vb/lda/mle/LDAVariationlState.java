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
package org.openimaj.pgm.vb.lda.mle;

import org.openimaj.pgm.util.Document;
import org.openimaj.util.array.SparseIntArray.Entry;

/**
 * The state of the E step of the MLE LDA
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LDAVariationlState{
	/**
	 * the n'th unique word in a document's probability for each topic
	 */
	public double[][] phi;
	/**
	 * Useful for calculating the sumphi for a given document
	 */
	public double[] oldphi;
	/**
	 * the dirichlet parameter for the topic multinomials
	 */
	public double[] varGamma;
	
	/**
	 * The liklihood of the current topic sufficient statistics and variational parameters
	 */
	public double likelihood; 
	/**
	 * The old liklihood
	 */
	public double oldLikelihood;
	/**
	 * The current LDAModel (i.e. the current sufficient statistics
	 */
	public LDAModel state;
	/**
	 * Holds the first derivative of the gamma 
	 */
	public double[] digamma;
	
	
	int iteration;
	

	/**
	 * The variational state holds phi and gamma states as well as 
	 * information for convergence of the E step.
	 * @param state
	 */
	public LDAVariationlState(LDAModel state) {
		this.oldphi = new double[state.ntopics];
		this.varGamma = new double[state.ntopics];
		this.digamma = new double[state.ntopics];
		this.state = state;
	}

	/**
	 * initialises the phi and sets everything to 0
	 * @param doc
	 */
	public void prepare(Document doc){
		this.phi = new double[doc.countUniqueWords()][state.ntopics];
		likelihood = 0;
		oldLikelihood = Double.NEGATIVE_INFINITY;
		for (int topici = 0; topici < phi.length; topici++) {
			varGamma[topici] = this.state.alpha;
			digamma[topici] = 0; // used to calculate likelihood
			int wordi = 0;
			for (Entry wordCount : doc.getVector().entries()) {
				phi[wordi][topici] = 1f/this.state.ntopics;
				varGamma[topici] += (double)wordCount.value / this.state.ntopics;
				wordi++;
			}
		}
		this.iteration = 0;
	}
}