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
package org.openimaj.pgm.vb.lda;

import gov.sandia.cognition.statistics.distribution.DirichletDistribution;

import java.util.Map;

import org.openimaj.pgm.util.Corpus;
import org.openimaj.pgm.util.Document;

/**
 * An implementation of variational inference LDA which can be saved and loaded
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LDAModel {
	private int ntopics;
	private double[][] beta;
	private DirichletDistribution thetaDirichlet;
	private Map<LDAConfig,Object> config;
	enum LDAConfig{
		MAX_ITERATIONS {
			@Override
			public Integer defaultValue() {
				return 10;
			}
		},
		ALPHA {
			@Override
			public Double defaultValue() {
				return 0.3d;
			}
		}, VAR_MAX_ITERATIONS {
			@Override
			public Integer defaultValue() {
				return 10;
			}
		};
		public abstract Object defaultValue();
	}

	private class LDAState{
		int iteration = 0;
		boolean converged = false;
		private double[][] beta;
		private DirichletDistribution thetaDirichlet;
		private double alpha;

		public LDAState() {

		}

		private void initModel(Corpus corpus) {
			this.beta = new double[LDAModel.this.ntopics][corpus.getVocabularySize()];
			this.alpha = (Double)LDAModel.this.getConfig(LDAConfig.ALPHA);

		}

		public boolean converged() {
			boolean maxIter = iteration >= (Integer)LDAModel.this.getConfig(LDAConfig.MAX_ITERATIONS);
			return converged || maxIter;
		}
	}

	private class LDAVariationlState{
		boolean converged = false;
		double[][] phi;
		double[] var_gamma;
		double phisum, liklihood;
		double likelihood_old = 0;
		double[] oldphi = new double[LDAModel.this.ntopics];
		int var_iter;
		double[] digamma_gam = new double[LDAModel.this.ntopics];
		private LDAState state;

		public LDAVariationlState(LDAState state, int ndocs) {
			this.phi = new double[ndocs][LDAModel.this.ntopics];
			this.var_gamma = new double[LDAModel.this.ntopics];
			this.state = state;
		}

		public boolean converged(){
			boolean maxIter = var_iter >= (Integer)LDAModel.this.getConfig(LDAConfig.VAR_MAX_ITERATIONS);
			return converged || maxIter;
		}

		public void reinitVarInit(){
		}
	}

	/**
	 * @param ntopics
	 */
	public LDAModel(int ntopics) {
		this.ntopics = ntopics;
	}

	public Object getConfig(LDAConfig key) {
		Object val = this.config.get(key);
		if(val == null) return key.defaultValue();
		return val;
	}

	/**
	 * initiates the EM algorithm on documents in the corpus
	 * @param corpus
	 */
	public void estimate(Corpus corpus){
		LDAState state = new LDAState();
		performEM(corpus,state);
	}



	private void performEM(Corpus corpus, LDAState state) {

		while(!state.converged()){
			performE(corpus, state);
			performM(state);
		}
	}


	private void performE(Corpus corpus, LDAState state) {
//		LDAVariationlState vstate = new LDAVariationlState(corpus.getDocuments().size());
		for (Document doc : corpus.getDocuments()) {

		}
	}

	private void performM(LDAState state) {
		// TODO Auto-generated method stub

	}

}
