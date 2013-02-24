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