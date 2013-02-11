package org.openimaj.rdf.storm.eddying.routing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
	private Map<TriplePattern[],Map<TripleMatch,Integer>> stemRefsByPattern;
	private Map<TriplePattern[],Integer> varCountByPattern;
	
	protected void prepare(){
		stems = new HashMap<TripleMatch,List<TriplePattern[]>>();
		stemRefsByPattern = new HashMap<TriplePattern[],Map<TripleMatch,Integer>>();
		varCountByPattern = new HashMap<TriplePattern[],Integer>();
		for (String query : queryStrings){
			try{
				Rule rule = Rule.parseRule(query);
				TriplePattern[] pattern = (TriplePattern[]) rule.getBody();
				varCountByPattern.put(pattern, rule.getNumVars());
				Map<TripleMatch,Integer> stemCounts = stemRefsByPattern.get(pattern);
				for (TriplePattern tp : pattern){
					Integer stemCount;
					try {
						stemCount = stemCounts.get(tp.asTripleMatch());
						if (stemCount == null) stemCount = 0;
					} catch (NullPointerException e) {
						stemCounts = new HashMap<TripleMatch,Integer>();
						stemRefsByPattern.put(pattern, stemCounts);
						stemCount = 0;
					}
					stemCounts.put(tp.asTripleMatch(), stemCount + 1);
					
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
	
	/**
	 * Produces a full map of patterns to sets of query environments.
	 * @return map of patterns and initial binding environments;
	 */
	private Map<TriplePattern[],List<Node[]>> generateInitialQueryEnvironments(){
		Map<TriplePattern[],List<Node[]>> toReturn = new HashMap<TriplePattern[],List<Node[]>>();
		for (TriplePattern[] tpa : varCountByPattern.keySet()){
			List<Node[]> nodeList = new ArrayList<Node[]>();
			nodeList.add(new Node[varCountByPattern.get(tpa)]);
			toReturn.put(tpa, nodeList);
		}
		return toReturn;
	}
	
	/**
	 * Construct a duplicate SteM reference map, noted per graph pattern.
	 * @return a copy of the router's SteM reference count map.
	 */
	private Map<TriplePattern[],Map<TripleMatch,Integer>> cloneSteMRefs(){
		Map<TriplePattern[],Map<TripleMatch,Integer>> stemCounts = new HashMap<TriplePattern[],Map<TripleMatch,Integer>>();
		for (TriplePattern[] pattern : this.stemRefsByPattern.keySet()){
			Map<TripleMatch,Integer> involvedIn = new HashMap<TripleMatch,Integer>();
			for (TripleMatch tm : this.stemRefsByPattern.get(pattern).keySet()) {
				involvedIn.put(tm, stemRefsByPattern.get(pattern).get(tm));
			}
			stemCounts.put(pattern, involvedIn);
		}
		return stemCounts;
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long timestamp) {
		// Construct a duplicate SteM usage map and map of environments.
		Map<TripleMatch,List<TriplePattern[]>> stems = cloneSteMMap();
		Map<TriplePattern[],List<Node[]>> envs = generateInitialQueryEnvironments();
		
		// (shortcut: keep a set of failed query patterns, add checked the set before performing the full check each time).
//		Set<TriplePattern[]> checkedMap = new HashSet<TriplePattern[]>();
		
		// Deduce options by removing potential queries from potential SteMs based on Graph provenance.
		// Compare every triple in the graph to every triple pattern in ExtendedIterator<T>ern in every SteM.
//		for (TripleMatch tm : stems.keySet()){
		Iterator<TriplePattern[]> patterns = stemRefsByPattern.keySet().iterator();
		patternLoop:
			for (TriplePattern[] pattern = patterns.next(); patterns.hasNext(); pattern = patterns.next()){
//				if (checkedMap.contains(pattern)){
//					for (TripleMatch tm : stems.keySet())
//						stems.get(tm).remove(pattern);
//					continue;
//				}
				// If the triple does not match any part of the query pattern, delete that query pattern from the SteM
				// and mark it as false in the map so it may be instantly deleted when encountered in other SteMs.
				Map<Triple,Boolean> matched = new HashMap<Triple,Boolean>();
				ExtendedIterator<Triple> it = g.find(null,null,null);
				for (Triple t = it.next(); it.hasNext(); t = it.next())
					// initialise flag to state the current triple has not yet matched against the current graph pattern in any environment
					matched.put(t, false);
				List<Node[]> newEnvs;
				for (TriplePattern current : pattern){
					newEnvs = new ArrayList<Node[]>();
					for (Node[] env : envs.get(pattern)){
						int count = stemRefsByPattern.get(pattern).get(current.asTripleMatch());
						it = g.find(current.asTripleMatch());
						for (Triple t = it.next(); it.hasNext(); t = it.next()){
							// When a query pattern remains valid, the relevant triple patterns should be updated, converting the
							// variable nodes to nodes bound to the matched values, so that later triples within the graph to be
							// routed are compared in the context of previous triples.
							//Initialise subject, object and predicate according to the current environment.
							Node subject = current.getSubject().isVariable() ? env[((Node_RuleVariable) current.getSubject()).getIndex()] : current.getSubject(),
								 predicate = current.getPredicate().isVariable() ? env[((Node_RuleVariable) current.getPredicate()).getIndex()] : current.getPredicate(),
								 object = current.getObject().isVariable() ? env[((Node_RuleVariable) current.getObject()).getIndex()] : current.getObject();
							if ((subject == null || t.getSubject().equals(subject))
									&& (predicate == null || t.getPredicate().equals(predicate))
									&& (object == null || t.getObject().equals(object))){
								Node[] newEnv = Arrays.copyOf(env, varCountByPattern.get(pattern));
								if (subject == null){
									newEnv[((Node_RuleVariable) current.getSubject()).getIndex()] = t.getSubject();
								}
								if (predicate == null){
									newEnv[((Node_RuleVariable) current.getPredicate()).getIndex()] = t.getPredicate();
								}
								if (object == null){
									newEnv[((Node_RuleVariable) current.getObject()).getIndex()] = t.getObject();
								}
								//   - When a triple matches more than one triple pattern within a query pattern, duplicate the original
								//     for as many matches as there are, then bind the triple into the query patterns as normal, only
								//     bind the triple into the first occurrence in the first duplicate, the second in the second, and so on.
								//Add the new environment to the list of new environments
								newEnvs.add(newEnv);
								matched.put(t, true);
							}
							count--;
						}
						if (count > 0){
							newEnvs.add(env);
						}else{
							stems.get(current.asTripleMatch()).remove(pattern);
						}
					}
					if (newEnvs.size() > 0)
						envs.put(pattern, newEnvs);
					else {
						for (TripleMatch tm : stems.keySet())
							stems.get(tm).remove(pattern);
						continue patternLoop;
					}
				}
				boolean accountedFor = true;
				for (boolean flag : matched.values())
					accountedFor &= flag;
				if (!accountedFor){
					for (TripleMatch tm : stems.keySet())
						stems.get(tm).remove(pattern);
				}
			}
//		}
		
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
