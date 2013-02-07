package org.openimaj.rdf.storm.eddying.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.stems.StormSteMQueue;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class SingleQueryPolicyStormGraphRouter extends StormGraphRouter {

	protected final static Logger logger = Logger.getLogger(SingleQueryPolicyStormGraphRouter.class);

	private String query;
	
	/**
	 * @param q
	 */
	public SingleQueryPolicyStormGraphRouter(String q){
		this.query = q;
	}
	
	private int varCount;
	private Map<TriplePattern,Integer> patternStats;
	private Map<TripleMatch,Integer> stemRefs;
	
	protected void prepare(){
		Rule rule = Rule.parseRule(query);
		varCount = rule.getNumVars();
		patternStats = new HashMap<TriplePattern,Integer>();
		stemRefs = new HashMap<TripleMatch,Integer>();
		try{
			int count = 0;
			TriplePattern[] pattern = (TriplePattern[]) rule.getBody();
			for (TriplePattern tp : pattern){
				patternStats.put(tp,count++);
				if (stemRefs.containsKey(tp.asTripleMatch()))
					stemRefs.put(tp.asTripleMatch(), stemRefs.get(tp.asTripleMatch()) + 1);
				else
					stemRefs.put(tp.asTripleMatch(), 1);
			}
		} catch (ClassCastException e){}
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long timestamp) {
		PriorityQueue<TriplePattern> stemQueue = new PriorityQueue<TriplePattern>(this.patternStats.size(), new Comparator<TriplePattern>(){
			@Override
			public int compare(TriplePattern arg0, TriplePattern arg1) {
				return SingleQueryPolicyStormGraphRouter.this.patternStats.get(arg0)
						- SingleQueryPolicyStormGraphRouter.this.patternStats.get(arg1);
			}
		});
		stemQueue.addAll(this.patternStats.keySet());
		
		Node[] env = new Node[varCount];
		Node subject,predicate,object;
		 
		for (TriplePattern current = stemQueue.poll(); !stemQueue.isEmpty(); current = stemQueue.poll()){
				
			ExtendedIterator<Triple> matchingTriples = g.find(current.asTripleMatch());
			if (matchingTriples.hasNext()){
				Triple match = matchingTriples.next();
//				if (){
//					
//				}
			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
	}
	
	// INNER CLASSES
	
	public static class SQPESStormGraphRouter extends EddyStubStormGraphRouter {

		public SQPESStormGraphRouter(List<String> eddies) {
			super(eddies);
		}
		
		protected void prepare(){
			
		}

		@Override
		protected void distributeToEddies(Tuple anchor, Values vals) {
			String source = anchor.getSourceComponent();
			if (this.eddies.contains(source)){
				logger.debug(String.format("\nRouting back to Eddy %s for %s", source, vals.get(0).toString()));
				this.collector.emit(source, anchor, vals);
			}else
				for (String eddy : this.eddies){
					logger.debug(String.format("\nRouting on to Eddy %s for %s from %s", eddy, vals.get(0).toString(), source));
					this.collector.emit(eddy, anchor, vals);
				}
			this.collector.ack(anchor);
		}
		
	}

}
