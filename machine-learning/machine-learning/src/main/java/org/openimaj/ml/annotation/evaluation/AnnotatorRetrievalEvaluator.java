package org.openimaj.ml.annotation.evaluation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.RetrievalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEvaluator;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.Annotator;

//given a trained annotator and a set of test documents WITH ground truth
//annotations, evaluate the effectiveness of auto-annotation
//as a retrieval experiment.
public class AnnotatorRetrievalEvaluator<O, A, R, T extends Annotated<O, A> & Identifiable> extends RetrievalEvaluator<R, T, A>{
	
	public AnnotatorRetrievalEvaluator(Annotator<O, A, ?> annotator, Dataset<T> testData, RetrievalAnalyser<R, A, T> analyser) {
		super(null, annotator.getAnnotations(), null, analyser);
		
		this.relevant = computeRelevant(queries, testData);
		this.engine = null;//TODO
	}

	private Map<A, Set<T>> computeRelevant(Collection<A> queries, Dataset<T> testData) {
		// TODO Auto-generated method stub
		return null;
	}
}
