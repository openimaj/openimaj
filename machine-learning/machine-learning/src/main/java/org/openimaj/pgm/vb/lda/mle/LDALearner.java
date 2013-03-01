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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.special.Gamma;
import org.openimaj.math.util.MathUtils;
import org.openimaj.pgm.util.Corpus;
import org.openimaj.pgm.util.Document;
import org.openimaj.util.array.SparseIntArray.Entry;

/**
 * An implementation of variational inference LDA which can be saved and loaded
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LDALearner {
	private int ntopics;
	private Map<LDAConfig, Object> config = new HashMap<LDAConfig, Object>();

	enum LDAConfig {
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
		},
		VAR_MAX_ITERATIONS {
			@Override
			public Integer defaultValue() {
				return 10;
			}
		},
		INIT_STRATEGY {

			@Override
			public LDABetaInitStrategy defaultValue() {
				return new LDABetaInitStrategy.RandomBetaInit();
			}

		},
		EM_CONVERGED {

			@Override
			public Double defaultValue() {
				return 1e-5;
			}

		},
		VAR_EM_CONVERGED {

			@Override
			public Double defaultValue() {
				return 1e-5;
			}

		};
		public abstract Object defaultValue();
	}

	/**
	 * @param ntopics
	 */
	public LDALearner(int ntopics) {
		this.ntopics = ntopics;
	}

	/**
	 * @param key
	 * @return the configuration parameter value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfig(LDAConfig key) {
		final T val = (T) this.config.get(key);
		if (val == null)
			return (T) key.defaultValue();
		return val;
	}

	/**
	 * initiates the EM algorithm on documents in the corpus
	 * 
	 * @param corpus
	 */
	public void estimate(Corpus corpus) {
		performEM(corpus);
	}

	private void performEM(Corpus corpus) {
		// some variables
		final double initialAlpha = (Double) this.getConfig(LDAConfig.ALPHA);
		final LDABetaInitStrategy initStrat = this.getConfig(LDAConfig.INIT_STRATEGY);

		// initialise the first state
		final LDAModel state = new LDAModel(this.ntopics);
		state.prepare(corpus);
		state.setAlpha(initialAlpha);
		initStrat.initModel(state, corpus);

		final LDAVariationlState vstate = new LDAVariationlState(state);
		while (modelConverged(vstate.state)) {
			final LDAModel nextState = vstate.state.newInstance();
			nextState.setAlpha(initialAlpha);
			for (final Document doc : corpus.getDocuments()) {
				vstate.prepare(doc);
				performE(doc, vstate); // updates the variation parameters given
										// the current Beta
				performM(doc, vstate, nextState); // updates the nextState given
													// the variational
													// parameters
				nextState.likelihood += vstate.likelihood;
			}
			nextState.iteration++;
			vstate.state = nextState;
		}
	}

	private LDAVariationlState performE(Document doc, LDAVariationlState vstate) {
		vstate.prepare(doc);
		while (!variationalStateConverged(vstate)) {
			int docWordIndex = 0;
			for (final Entry wordCount : doc.getVector().entries()) {
				double phiSum = 0;
				final int word = wordCount.index;
				final int count = wordCount.value;
				for (int topicIndex = 0; topicIndex < vstate.phi.length; topicIndex++) {
					vstate.oldphi[topicIndex] = vstate.phi[docWordIndex][topicIndex];
					// If this word has been seen in this class before
					if (vstate.state.topicWord[topicIndex][docWordIndex] > 0) {
						// Update phi
						// Remember this phi is actually the same value for
						// every instance of thisparticular word.
						// Whenever phi is actually used there is likely to be a
						// multiplication by the number of times this particular
						// word appears in this document
						// From eqn 16 in blei 2003
						// The sum gamma cancels when the exact phi for a given
						// word is calculated
						final double logBeta =
								Math.log(vstate.state.topicWord[topicIndex][word]) -
										Math.log(vstate.state.topicTotal[topicIndex]);
						vstate.phi[docWordIndex][topicIndex] =
								logBeta +
										Gamma.digamma(vstate.varGamma[topicIndex]);
					} else {
						// if not, \Beta_wi = ETA (very small) so log \Beta_wi
						// ~= -100 (ETA = 10-34)
						vstate.phi[docWordIndex][topicIndex] = Gamma.digamma(vstate.varGamma[topicIndex]) - 100;
					}
					if (topicIndex == 0) {
						phiSum = vstate.phi[docWordIndex][topicIndex];
					} else {
						// we need phiSum = Sum_K_i{phi}, log phiSum = log
						// Sum_K_i{phi}.
						// what we have is log phi
						// we must calculate log (a + b) from log(a) and log(b).
						// The normaliser for eqn 16
						phiSum = MathUtils.logSum(phiSum,
								vstate.phi[docWordIndex][topicIndex]);
					}
				}
				for (int topicIndex = 0; topicIndex < vstate.phi.length; topicIndex++) {
					// Replace log phi with the normalised phi
					// normalise a given word's phi summing over all i in eqn 16
					vstate.phi[docWordIndex][topicIndex] = Math.exp(
							vstate.phi[docWordIndex][topicIndex] - phiSum
							);
					// update gamma incrementally (eqn 17 blei 2003)
					// - take away the old phi,
					// - add the new phi,
					// - do this N times for the number of times this particular
					// word appears in this document
					vstate.varGamma[topicIndex] += count
							* (vstate.phi[docWordIndex][topicIndex] - vstate.oldphi[topicIndex]);
				}
				docWordIndex++;
			}
			vstate.oldLikelihood = vstate.likelihood;
			vstate.likelihood = computeLikelihood(doc, vstate);
			vstate.iteration++;
		}
		return vstate;
	}

	private boolean modelConverged(LDAModel model) {
		final double EM_CONVERGED = (Double) this.getConfig(LDAConfig.EM_CONVERGED);
		final int MAX_ITER = (Integer) this.getConfig(LDAConfig.MAX_ITERATIONS);
		// if likelihood ~= oldLikelihood then this value will approach 0.
		final double converged = (model.likelihood - model.oldLikelihood) / model.oldLikelihood;
		final boolean liklihoodSettled = ((converged < EM_CONVERGED) || (model.iteration <= 2));
		final boolean maxIterExceeded = model.iteration > MAX_ITER;

		return liklihoodSettled || maxIterExceeded;
	}

	private boolean variationalStateConverged(LDAVariationlState vstate) {
		final double EM_CONVERGED = (Double) this.getConfig(LDAConfig.VAR_EM_CONVERGED);
		final int MAX_ITER = (Integer) this.getConfig(LDAConfig.VAR_MAX_ITERATIONS);
		// if likelihood ~= oldLikelihood then this value will approach 0.
		final double converged = (vstate.likelihood - vstate.oldLikelihood) / vstate.oldLikelihood;
		final boolean liklihoodSettled = ((converged < EM_CONVERGED) || (vstate.iteration <= 2));
		final boolean maxIterExceeded = vstate.iteration > MAX_ITER;

		return liklihoodSettled || maxIterExceeded;
	}

	/**
	 * Given the current state of the variational parameters, update the maximum
	 * liklihood beta parameter by updating its sufficient statistics
	 * 
	 * @param d
	 * @param vstate
	 * @param nextState
	 */
	private void performM(Document d, LDAVariationlState vstate, LDAModel nextState) {

		for (final Entry entry : d.values.entries()) {
			for (int topicIndex = 0; topicIndex < ntopics; topicIndex++) {
				final int wordIndex = entry.index;
				final int count = entry.value;
				nextState.incTopicWord(topicIndex, wordIndex, count * vstate.phi[wordIndex][topicIndex]);
				nextState.incTopicTotal(topicIndex, count);
			}
		}
	}

	/**
	 * Calculates a lower bound for the log liklihood of a document given
	 * current parameters. When this is maximised it minimises the KL divergence
	 * between the the variation posterior and the true posterior.
	 * 
	 * The derivation can be seen in the appendix of Blei's LDA paper 2003
	 * 
	 * @param doc
	 * @param vstate
	 * @return the likelihood
	 */
	public double computeLikelihood(Document doc, LDAVariationlState vstate) {
		double likelihood = 0;

		// Prepare some variables we need
		double sumVarGamma = 0;
		double sumDiGamma = 0;
		for (int topicIndex = 0; topicIndex < ntopics; topicIndex++) {
			sumVarGamma += vstate.varGamma[topicIndex];
			vstate.digamma[topicIndex] = Gamma
					.digamma(vstate.varGamma[topicIndex]);
			sumDiGamma += vstate.digamma[topicIndex];
		}
		// first we sum the parameters which don't rely on iteration through the
		// classes or
		// iteration through the documents

		likelihood += Gamma.logGamma(vstate.state.alpha * ntopics) - // eqn (15)
																		// line
																		// 1
				Gamma.logGamma(vstate.state.alpha) * ntopics + // eqn (15) line
																// 1
				Gamma.logGamma(sumVarGamma); // eqn (15) line 4
		for (int topicIndex = 0; topicIndex < ntopics; topicIndex++) {
			// Now add the things that just need an interation over k
			// eqn (15) line 4
			final double topicGammaDiff = vstate.digamma[topicIndex] - sumDiGamma;
			likelihood += Gamma.logGamma(vstate.varGamma[topicIndex]) - (vstate.varGamma[topicIndex] - 1)
					* topicGammaDiff;
			int wordIndex = 0;
			for (final Entry wordCount : doc.getVector().entries()) {
				final int word = wordCount.index;
				final int count = wordCount.value;
				final double logBeta = Math.log(
						vstate.state.topicWord[topicIndex][word]) -
						Math.log(vstate.state.topicTotal[topicIndex]
								);
				likelihood +=
						// Count because these sums are over N and
						// the sum of the counts of each unique word is == N
						count * (
								// Each of these lines happens to multiply by
								// the current word's phi
								vstate.phi[wordIndex][topicIndex] * (
								// eqn (15) line 2
								topicGammaDiff +
										// eqn (15) line 3
										count * logBeta -
								// eqn (15) line 5
								Math.log(vstate.phi[wordIndex][topicIndex]
											)
								)
								);
				wordIndex++;
			}
		}
		return likelihood;
	}
}
