package org.openimaj.experiment.evaluation.retrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.lemurproject.ireval.IREval;
import org.lemurproject.ireval.SetRetrievalEvaluator;
import org.lemurproject.ireval.RetrievalEvaluator.Document;
import org.lemurproject.ireval.RetrievalEvaluator.Judgment;
import org.openimaj.experiment.dataset.Identifiable;

/**
 * An evaluator suitable for any kind of result document from
 * the {@link RetrievalEngine}. Retrieval statistics are calculated
 * using {@link IREval}. 
 * 
 * If the retrieved documents are instances of {@link Ranked}, then
 * the rank is determined through {@link Ranked#getRank()}, otherwise
 * the rank of each result document is automatically
 * determined from its position in the results list. 
 * 
 * Similarly, if the retrieved documents are instances of {@link Scored}, then
 * the score is determined through {@link Scored#getScore()}, otherwise
 * the score of each result document is set as 1.0/rank.
 * 
 * If the queries are {@link Identifiable}, then the query ids in
 * the outputted {@link SetRetrievalEvaluator} are the ID of the 
 * query; if the queries are not {@link Identifiable}, then the
 * {@link Object#toString()} method is used instead. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <Q> Type of query
 * @param <D> Type of document
 */
public class IREvalAnalyser<Q, D extends Identifiable> implements RetrievalAnalyser<SetRetrievalEvaluator, Q, D> {
	protected TreeMap<String, ArrayList<Document>> convertResults(Map<Q, List<D>> results) {
		TreeMap<String, ArrayList<Document>> allRankings = new TreeMap< String, ArrayList<Document>>();
		
		for (Entry<Q, List<D>> entry : results.entrySet()) {
			String key;
			
			if (entry.getKey() instanceof Identifiable)
				key = ((Identifiable)entry.getKey()).getID();
			else 
				key = entry.getKey().toString();
			
			ArrayList<Document> docs = new ArrayList<Document>();
			for (int i=0; i<entry.getValue().size(); i++) {
				D doc = entry.getValue().get(i);
				
				int rnk = i+1;
				if (doc instanceof Ranked) rnk = ((Ranked)doc).getRank();
				
				double score = (1.0 / (double)rnk);
				if (doc instanceof Scored) score = ((Scored)doc).getScore();
				
				
				docs.add(new Document(doc.getID(), rnk, score));
			}
			
			allRankings.put(key, docs);
		}
		
		return allRankings;
	}
	
	protected TreeMap<String, ArrayList<Judgment>> convertRelevant(Map<Q, Set<D>> relevant) {
		TreeMap<String, ArrayList<Judgment>> allJudgments = new TreeMap< String, ArrayList<Judgment>>();
		
		for (Entry<Q, Set<D>> entry : relevant.entrySet()) {
			String key;
			
			if (entry.getKey() instanceof Identifiable)
				key = ((Identifiable)entry.getKey()).getID();
			else 
				key = entry.getKey().toString();
			
			ArrayList<Judgment> docs = new ArrayList<Judgment>();
			for (D doc : entry.getValue()) {
				docs.add(new Judgment(doc.getID(), 1));
			}
			
			allJudgments.put(key, docs);
		}
		
		return allJudgments;
	}
	
	@Override
	public SetRetrievalEvaluator analyse(Map<Q, List<D>> results, Map<Q, Set<D>> relevant) {
		return IREval.create(convertResults(results), convertRelevant(relevant));
	}
}
