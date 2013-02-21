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
