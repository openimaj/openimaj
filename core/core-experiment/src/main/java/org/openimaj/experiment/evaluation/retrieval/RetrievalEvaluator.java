package org.openimaj.experiment.evaluation.retrieval;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.evaluation.Evaluator;

public class RetrievalEvaluator<R, D extends Identifiable, Q> implements Evaluator<Map<Q, List<D>>, R> {
	protected RetrievalEngine<D, Q> engine;
	protected Collection<Q> queries;
	protected Map<Q, Set<D>> relevant; //in the future we might want a model more like trec qrels with relevance levels
	protected RetrievalAnalyser<R, Q, D> analyser;
	
	public RetrievalEvaluator(RetrievalEngine<D, Q> engine, Collection<Q> queries, Map<Q, Set<D>> relevant, RetrievalAnalyser<R, Q, D> analyser) {
		this.engine = engine;
		this.queries = queries;
		this.relevant = relevant;
		this.analyser = analyser;
	}
	
	@Override
	public Map<Q, List<D>> evaluate() {
		Map<Q, List<D>> results = new HashMap<Q, List<D>>();
		
		for (Q query : queries) {
			results.put(query, engine.search(query));
		}
		
		return results;
	}
	
	@Override
	public R analyse(Map<Q, List<D>> results) {
		return analyser.analyse(results, relevant);
	}
	
	/**
	 * Write the ground-truth data in TREC QRELS format.
	 * @param os stream to write to
	 */
	public void writeQRELS(OutputStream os) {
		writeQRELS(new PrintStream(os));
	}
	
	/**
	 * Write the ground-truth data in TREC QRELS format.
	 * @param os stream to write to
	 */
	public void writeQRELS(PrintStream os) {
		int qnum = 0;
		for (Q query : queries) {
			String qid = "q" + qnum;
			
			for (D doc : relevant.get(query)) {
				os.format("%s %d %s %d", qid, 0, doc.getID(), 1);
			}
		}
	}
}
