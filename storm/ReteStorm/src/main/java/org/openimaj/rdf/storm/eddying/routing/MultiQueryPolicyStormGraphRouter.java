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

import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
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
	}
	
	private Map<TripleMatch,List<TriplePattern[]>> stems;
	private Map<TripleMatch,Integer> stemStats;
	private Map<TriplePattern[],Map<TripleMatch,Integer>> stemRefsByPattern;
	private Map<TriplePattern[],Integer> varCountByPattern;
	
	protected void prepare(){
		stems = new HashMap<TripleMatch,List<TriplePattern[]>>();
		stemStats = new HashMap<TripleMatch,Integer>();
		stemRefsByPattern = new HashMap<TriplePattern[],Map<TripleMatch,Integer>>();
		varCountByPattern = new HashMap<TriplePattern[],Integer>();
		// TODO: proper SteM metrics.
		int count = 0;
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
						// TODO: proper SteM metrics.
						stemStats.put(tp.asTripleMatch(),count++);
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
	
//	/**
//	 * Construct a duplicate SteM reference map, noted per graph pattern.
//	 * @return a copy of the router's SteM reference count map.
//	 */
//	private Map<TriplePattern[],Map<TripleMatch,Integer>> cloneSteMRefs(){
//		Map<TriplePattern[],Map<TripleMatch,Integer>> stemCounts = new HashMap<TriplePattern[],Map<TripleMatch,Integer>>();
//		for (TriplePattern[] pattern : this.stemRefsByPattern.keySet()){
//			Map<TripleMatch,Integer> involvedIn = new HashMap<TripleMatch,Integer>();
//			for (TripleMatch tm : this.stemRefsByPattern.get(pattern).keySet()) {
//				involvedIn.put(tm, stemRefsByPattern.get(pattern).get(tm));
//			}
//			stemCounts.put(pattern, involvedIn);
//		}
//		return stemCounts;
//	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long timestamp) {
		// Construct a duplicate SteM usage map and map of environments.
		Map<TripleMatch,List<TriplePattern[]>> tempSteMs = cloneSteMMap();
		Map<TriplePattern[],List<Node[]>> envs = generateInitialQueryEnvironments();
		
		Iterator<TriplePattern[]> patterns = envs.keySet().iterator();
		// Iterate over all graph patterns managed by this Eddy.
		patternLoop:
			for (TriplePattern[] pattern = patterns.next(); patterns.hasNext(); pattern = patterns.next()){
				// Initialise a map of probing graph triples to boolean flags, to denote that the triple has been matched in the current pattern. 
				Map<Triple,Boolean> probeMatched = new HashMap<Triple,Boolean>();
				ExtendedIterator<Triple> it = g.find(null,null,null);
				for (Triple t = it.next(); it.hasNext(); t = it.next())
					// Initialise flag to state the current triple has not yet matched against the current graph pattern in any environment
					probeMatched.put(t, false);
				// Declare a list of environments, representing the environments resulting from the matching of the next triple pattern.
				List<Node[]> newEnvs;
				// Iterate over all triple patterns in the current graph pattern
				for (TriplePattern current : pattern){
					// Initialise the list of new environments.
					newEnvs = new ArrayList<Node[]>();
					// evaluate the current triple pattern in all applicable environments for the current graph pattern
					for (Node[] env : envs.get(pattern)){
						// Initialise subject, object and predicate according to the current environment.
						Node subject = current.getSubject().isVariable() ? env[((Node_RuleVariable) current.getSubject()).getIndex()] : current.getSubject(),
							 predicate = current.getPredicate().isVariable() ? env[((Node_RuleVariable) current.getPredicate()).getIndex()] : current.getPredicate(),
							 object = current.getObject().isVariable() ? env[((Node_RuleVariable) current.getObject()).getIndex()] : current.getObject();
						// Initialise a variable describing SteM uses unaccounted for with regards to this triple pattern in the current graph pattern
						int count = stemRefsByPattern.get(pattern).get(current.asTripleMatch());
						// Iterate over all triples in the graph that match the SteM of the current triple pattern.
						it = g.find(current.asTripleMatch());
						for (Triple t = it.next(); it.hasNext(); t = it.next()){
							// If the current triple matches the triple pattern within the binding environment, then create a new environment with the relevant, previously
							// empty bindings bound with the new values in the triple.
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
								// Add the new environment to the list of new environments
								newEnvs.add(newEnv);
								probeMatched.put(t, true);
							}
							// Whether the triple matched within the environment or not, decrement the count of unaccounted for SteM uses.
							count--;
						}
						if (count > 0){
							// If there are unaccounted for uses of the SteM, create a new environment that does not fill this triple pattern.
							newEnvs.add(env);
						}else{
							// If there are no unaccounted for uses of the SteM, remove the graph pattern from the list of patterns in which the SteM is involved.
							// This is because this pattern can gain nothing from further probing into this SteM.
							tempSteMs.get(current.asTripleMatch()).remove(pattern);
						}
					}
					if (newEnvs.size() > 0)
						// If matching the probing graph with the current triple pattern has produced some updated binding environments,
						// replace the old list of binding environments for the pattern with the updated environments.
						envs.put(pattern, newEnvs);
					else {
						// If the matching process for the current triple pattern has produced no binding environments, the pattern has been invalidated,
						// and is removed from the set of possible patterns in the lists of all SteMs involved in it, as well as the map of patterns to binding environments.
						for (TripleMatch tm : tempSteMs.keySet())
							tempSteMs.get(tm).remove(pattern);
						patterns.remove();
						// exit the current iteration of the loop through all patterns
						continue patternLoop;
					}
				}
				// Iterate through the matched-flags of all probing graph triples, setting an "accounted for" flag to false if any has not been matched.
				boolean accountedFor = true;
				for (boolean flag : probeMatched.values())
					accountedFor &= flag;
				if (!accountedFor){
					// If the matching process for the current graph pattern has left some triples from the probing graph unmatched, the pattern is invalid,
					// and is removed from the set of possible patterns in the lists of all SteMs involved in it, as well as the map of patterns to binding environments.
					for (TripleMatch tm : tempSteMs.keySet())
						tempSteMs.get(tm).remove(pattern);
					patterns.remove();
					// exit the current iteration of the loop through all patterns
					continue patternLoop;
				}
				
				// By this stage the probing graph has been verified against the current pattern.
				// Check to see if the probing graph matches the current pattern completely
				// (only requires checking that the graph and the pattern are the same size).
				// Report a completed pattern, then remove it from each of the SteMs involved in it and the map of binding environments. 
				if (g.size() == pattern.length) {
					this.reportCompletePattern(pattern, envs.get(pattern));
					for (TripleMatch tm : tempSteMs.keySet())
						tempSteMs.get(tm).remove(pattern);
					patterns.remove();
					// exit the current iteration of the loop through all patterns
					continue patternLoop;
				}
			}
		
		// TODO
		// From the options, decide which SteM(s) to route the graph to.
		// Use a metric involving predicted SteM selectivity, average processing time per SteM, as well as the
		// number of queries each SteM is involved in.
		// This metric is yet to be decided as yet.
		// Once a SteM has been selected, delete the query patterns that SteM contributes to from all SteM lists,
		// then select the best SteM from the updated set, remove its query patterns, and so on until all viable
		// queries have been progressed.
	}

	private void reportCompletePattern(TriplePattern[] pattern, List<Node[]> bindings) {
		System.out.println(String.format("Pattern %s complete, with bindings:", (Object) pattern));
		for (Node[] binding : bindings)
			System.out.println(binding.toString());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		Set<TripleMatch> stems = new HashSet<TripleMatch>();
		// Iterate through all query strings
		for (String query : queryStrings){
			try{
				// Convert query string to graph pattern
				TriplePattern[] pattern = (TriplePattern[]) Rule.parseRule(query).getBody();
				// Iterate through all triple patterns in the graph pattern
				for (TriplePattern tp : pattern){
					// If the stem that provides for the current query pattern hasn't been seen before
					TripleMatch tm = tp.asTripleMatch();
					if (stems.add(tm)){
						declarer.declareStream(String.format("%s,%s,%s",
																tm.getMatchSubject() == null ? "" : tm.getMatchSubject().toString(),
																tm.getMatchPredicate() == null ? "" : tm.getMatchPredicate().toString(),
																tm.getMatchObject() == null ? "" : tm.getMatchObject().toString()
															),
													new Fields("s","p","o",
																Component.action.toString(),
																Component.isAdd.toString(),
																Component.graph.toString(),
																Component.timestamp.toString()
															)
												);
					}
				}
			} catch (ClassCastException e){}
		}
	}
	
	// INNER CLASSES
	
		public static class MQPEddyStubStormGraphRouter extends EddyStubStormGraphRouter {

			public MQPEddyStubStormGraphRouter(List<String> eddies) {
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
