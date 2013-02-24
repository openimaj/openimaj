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