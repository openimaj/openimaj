package org.openimaj.rdf.storm.eddying.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class MultiQueryPolicyStormGraphRouter extends StormGraphRouter {
	
	private List<String> queryStrings;
	
	/**
	 * @param qs
	 */
	public MultiQueryPolicyStormGraphRouter(List<String> qs){
		this.queryStrings = qs;
		/*
		 * Separate the queries into their component triples, represented
		 * by filtered SteMs.
		 * 
		 * Decide which SteMs are part of which queries
		 */
	}
	
	private Map<TripleMatch,List<TriplePattern[]>> stems;
	
	protected void prepare(){
		stems = new HashMap<TripleMatch,List<TriplePattern[]>>();
		for (String query : queryStrings){
			try{
				TriplePattern[] pattern = (TriplePattern[]) Rule.parseRule(query).getBody();
				for (TriplePattern tp : pattern){
					List<TriplePattern[]> involvedIn = stems.get(tp.asTripleMatch());
					try {
						involvedIn.add(pattern);
					} catch (NullPointerException e) {
						involvedIn = new ArrayList<TriplePattern[]>();
						stems.put(tp.asTripleMatch(), involvedIn);
						involvedIn.add(pattern);
					}
				}
			} catch (ClassCastException e){}
		}
	}
	
	/**
	 * Construct a duplicate SteM usage map.
	 * 
	 * This one can be edited down to the {@link TriplePattern} level, i.e. entries in the Map can be safely removed or changed,
	 * {@link TriplePattern} arrays can be removed from the {@link List} or edited, but {@link TriplePattern}s within the arrays must be
	 * replaced rather than edited directly.
	 * @return a copy of the router's SteM map.
	 */
	private Map<TripleMatch,List<TriplePattern[]>> cloneSteMMap(){
		Map<TripleMatch,List<TriplePattern[]>> stems = new HashMap<TripleMatch,List<TriplePattern[]>>();
		Map<TriplePattern[],TriplePattern[]> patterns = new HashMap<TriplePattern[],TriplePattern[]>();
		for (TripleMatch tm : this.stems.keySet()){
			List<TriplePattern[]> involvedIn = new ArrayList<TriplePattern[]>();
			for (TriplePattern[] oldPattern : this.stems.get(tm)) {
				TriplePattern[] newPattern = patterns.get(oldPattern);
				if (newPattern == null){
					newPattern = new TriplePattern[oldPattern.length];
					for (int i = 0; i < oldPattern.length; i++)
						newPattern[i] = oldPattern[i];
					patterns.put(oldPattern, newPattern);
				}
				involvedIn.add(newPattern);
			}
			stems.put(tm, involvedIn);
		}
		return stems;
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long timestamp) {
		// Construct a duplicate SteM usage map
		Map<TripleMatch,List<TriplePattern[]>> stems = cloneSteMMap();
		
		// (shortcut: keep a map of query pattern to boolean, add checked query patterns and whether they
		// passed or not, then query the map before performing the full check each time).
		Map<TriplePattern[],Boolean> checkedMap = new HashMap<TriplePattern[],Boolean>();
		
		// Deduce options by removing potential queries from potential SteMs based on Graph provenance.
		// Compare every triple in the graph to every triple pattern in every query pattern in every SteM.
		// If the triple does not match any part of the query pattern, delete that query pattern from the SteM
		// and mark it as false in the map so it may be instantly deleted when encountered in other SteMs.
		// If the triple matches the current SteM's triple pattern, then check ALL query patterns, and also
		// delete any query pattern within which the triple does not match two distinct triple patterns.
		//   - Do not mark these query patterns false, this is simply representing that the graph should not
		//     be routed back to that SteM, not that the graph is no longer valid for that query.
		// When a query pattern remains valid, the relevant triple patterns should be updated, converting the
		// variable nodes to nodes bound to the matched values, so that later triples within the graph to be
		// routed are compared in the context of previous triples.
		//   - When a triple matches more than one triple pattern within a query pattern, duplicate the original
		//     for as many matches as there are, then bind the triple into the query patterns as normal, only
		//     bind the triple into the first occurrence in the first duplicate, the second in the second, and so on.
		
		// From the options, decide which SteM(s) to route the graph to.
		// Use a metric involving predicted SteM selectivity, average processing time per SteM, as well as the
		// number of queries each SteM is involved in.
		// This metric is yet to be decided as yet.
		// Once a SteM has been selected, delete the query patterns that SteM contributes to from all SteM lists,
		// then select the best SteM from the updated set, remove its query patterns, and so on until all viable
		// queries have been progressed.
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
	}
	
	// INNER CLASSES
	
		public static class MQPESStormGraphRouter extends EddyStubStormGraphRouter {

			public MQPESStormGraphRouter(List<String> eddies) {
				super(eddies);
			}
			
			protected void prepare(){
				
			}

			@Override
			protected void distributeToEddies(Tuple anchor, Values vals) {
				for (String eddy : this.eddies)
					this.collector.emit(eddy, anchor, vals);
			}
			
		}

}
