package org.openimaj.experiment.evaluation.retrieval;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.dataset.Identifiable;

public interface RetrievalAnalyser<R, Q, D extends Identifiable> {
	public R analyse(Map<Q, List<D>> results, Map<Q, Set<D>> relevant);
}
