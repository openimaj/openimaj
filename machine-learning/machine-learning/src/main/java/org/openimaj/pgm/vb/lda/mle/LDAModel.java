package org.openimaj.pgm.vb.lda.mle;

import org.openimaj.pgm.util.Corpus;

/**
 * Holds the sufficient statistics for a maximum liklihood LDA
 * as well as a single value for alpha (the parameter of the topic 
 * dirichlet prior)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LDAModel{
	/**
	 * the dirichelet perameter for every dimension of the topic multinomial prior
	 */
	public double alpha;
	/**
	 * The maximum likelihood sufficient statistics for estimation of Beta.
	 * This is the number of times a given word is in a given topic
	 */
	public double[][] topicWord;
	/**
	 * The maximum likelihood sufficient statistics for estimation of Beta.
	 * This is the number of words total in a given topic
	 */
	public double[] topicTotal;
	/**
	 * number of topics
	 */
	public int ntopics;
	int iteration;
	double likelihood,oldLikelihood;

	/**
	 * @param ntopics the number of topics in this LDA model
	 */
	public LDAModel(int ntopics) {
		this.ntopics = ntopics;
	}

	/**
	 * initialises the sufficient statistic holder based on ntopics and
	 * the {@link Corpus#vocabularySize()}. Alpha remains at 0
	 * @param corpus 
	 */
	public void prepare(Corpus corpus) {
		this.topicWord = new double[ntopics][corpus.vocabularySize()];
		this.topicTotal = new double[ntopics];
		this.alpha = 0;
		this.iteration = 0;
		this.likelihood = 0;
		this.oldLikelihood = Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * initialises the sufficient statistic holder based on ntopics and
	 * the vocabularySize. Alpha remains at 0
	 * @param vocabularySize
	 */
	public void prepare(int vocabularySize) {
		this.topicWord = new double[ntopics][vocabularySize];
		this.topicTotal = new double[ntopics];
		this.alpha = 0;
	}

	/**
	 * Increment a topic and word index by d. The totals are left untouched
	 * @param topicIndex
	 * @param wordIndex
	 * @param d
	 */
	public void incTopicWord(int topicIndex, int wordIndex, double d) {
		this.topicWord[topicIndex][wordIndex] += d;
	}
	
	/**
	 * Increment a topic and word index by d. The totals are left untouched
	 * @param topicIndex
	 * @param d
	 */
	public void incTopicTotal(int topicIndex, double d) {
		this.topicTotal[topicIndex] += d;
	}

	/**
	 * @param initialAlpha the alpha parameter for the topic multinomial dirichelet prior
	 */
	public void setAlpha(double initialAlpha) {
		this.alpha = initialAlpha;
	}

	/**
	 * This method also swaps the likelihoods (i.e. oldLikelihood == likelihood, likelhood = 0)
	 * @return a blank copy with unset alpha matching the current model's configuration 
	 */
	public LDAModel newInstance() {
		LDAModel ret = new LDAModel(ntopics);
		ret.prepare(this.topicWord[0].length);
		ret.iteration = this.iteration;
		ret.likelihood = 0;
		ret.oldLikelihood = this.likelihood;
		return ret;
	}
}