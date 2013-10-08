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

import org.openimaj.pgm.util.Corpus;

import cern.jet.random.engine.MersenneTwister;

/**
 * Initialisation strategies for the beta matrix in the maximum liklihood LDA.
 * Specifically implementors are expected to initialise the sufficient statistics
 * of beta (i.e. topicWord and topicTotal s.t. Beta_ij = topicWord_ij / topicTotal_i
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface LDABetaInitStrategy{
	/**
	 * Given a model and the corpus initialise the model's sufficient statistics
	 * @param model
	 * @param corpus
	 */
	public void initModel(LDAModel model, Corpus corpus);
	
	/**
	 * initialises beta randomly s.t. each each topicWord >= 1 and < 2
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class RandomBetaInit implements LDABetaInitStrategy{
		private MersenneTwister random;
		
		/**
		 * unseeded random
		 */
		public RandomBetaInit() {
			random = new MersenneTwister();
		}
		
		/**
		 * seeded random
		 * @param seed
		 */
		public RandomBetaInit(int seed) {
			random = new MersenneTwister(seed);
		}
		@Override
		public void initModel(LDAModel model, Corpus corpus) {
			for (int topicIndex = 0; topicIndex < model.ntopics; topicIndex++) {
				for (int wordIndex = 0; wordIndex < corpus.vocabularySize(); wordIndex++) {
					double topicWord = 1 + random.nextDouble();
					model.incTopicWord(topicIndex,wordIndex,topicWord);
					model.incTopicTotal(topicIndex, topicWord);
				}
			}
		}
	}
}