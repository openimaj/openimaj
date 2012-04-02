package org.openimaj.experiment.evaluation.retrieval.analysers;

import org.lemurproject.ireval.SetRetrievalEvaluator;
import org.openimaj.experiment.evaluation.AnalysisResult;

public class IREvalResult implements AnalysisResult {
	SetRetrievalEvaluator eval;
	
	public IREvalResult(SetRetrievalEvaluator sre) {
		this.eval = sre;
	}
	
	@Override
	public String toString() {
		return eval.toString();
	}
}
