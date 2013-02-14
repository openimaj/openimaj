package org.openimaj.rdf.storm.eddying.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.stems.StormSteMQueue;
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
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class SingleQueryPolicyStormGraphRouter extends StormGraphRouter {

	private static final long serialVersionUID = 4342744138230718341L;
	protected final static Logger logger = Logger.getLogger(SingleQueryPolicyStormGraphRouter.class);
	protected static int[] FACTORIALS = {0,1,2,6,24,120,720};
	

	private String query;
	
	/**
	 * @param q
	 */
	public SingleQueryPolicyStormGraphRouter(String q){
		this.query = q;
	}
	
	private int varCount;
	private TriplePattern[] pattern;
	private Map<TripleMatch,Integer> stemStats;
	private Map<TripleMatch,Integer> stemRefs;
	
	protected void prepare(){
		Rule rule = Rule.parseRule(query);
		varCount = rule.getNumVars();
		stemRefs = new HashMap<TripleMatch,Integer>();
		try{
			int count = 0;
			pattern = (TriplePattern[]) rule.getBody();
			for (TriplePattern tp : pattern){
				stemStats.put(tp.asTripleMatch(), count++);
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
		PriorityQueue<TriplePattern> stemQueue = new PriorityQueue<TriplePattern>(this.pattern.length, new Comparator<TriplePattern>(){
			@Override
			public int compare(TriplePattern arg0, TriplePattern arg1) {
				return SingleQueryPolicyStormGraphRouter.this.stemStats.get(arg0.asTripleMatch())
						- SingleQueryPolicyStormGraphRouter.this.stemStats.get(arg1.asTripleMatch());
			}
		});
		
		List<Node[]> envs = new ArrayList<Node[]>();
		List<Node[]> newEnvs;
		envs.add(new Node[varCount]);
		
		// TODO take into account strict triple patterns that could occlude relaxed triple patterns.  
		// Iterate over all triple patterns in the graph pattern
		for (TriplePattern current : this.pattern){
			newEnvs = new ArrayList<Node[]>();
			for (Node[] env : envs){
				//Iterate over all triples in the graph that match the SteM of the current triple pattern.
				ExtendedIterator<Triple> matchingTriples = g.find(current.asTripleMatch());
				boolean accountedFor = false;
				if (accountedFor = matchingTriples.hasNext()){
					//Initialise subject, object and predicate according to the current environment.
					Node subject = current.getSubject().isVariable() ? env[((Node_RuleVariable) current.getSubject()).getIndex()] : current.getSubject(),
						 predicate = current.getPredicate().isVariable() ? env[((Node_RuleVariable) current.getPredicate()).getIndex()] : current.getPredicate(),
						 object = current.getObject().isVariable() ? env[((Node_RuleVariable) current.getObject()).getIndex()] : current.getObject();
					//Initialise a variable describing SteM uses unaccounted for with regards to this triple pattern
					int count = stemRefs.get(current.asTripleMatch());
					//For each triple that fits the stem, see if it matches the triple pattern (including any previously fixed bindings in the current environment),
					//then subtract one from the number of unaccounted for uses.
					while (matchingTriples.hasNext()){
						Triple match = matchingTriples.next();
						//If the current triple matches the triple pattern within the binding environment, then create a new environment with the relevant, previously
						//empty bindings bound with the new values in the triple.
						if ((subject == null || match.getSubject().equals(subject))
								&& (predicate == null || match.getPredicate().equals(predicate))
								&& (object == null || match.getObject().equals(object))){
							Node[] newEnv = Arrays.copyOf(env, varCount);
							if (subject == null){
								newEnv[((Node_RuleVariable) current.getSubject()).getIndex()] = match.getSubject();
							}
							if (predicate == null){
								newEnv[((Node_RuleVariable) current.getPredicate()).getIndex()] = match.getPredicate();
							}
							if (object == null){
								newEnv[((Node_RuleVariable) current.getObject()).getIndex()] = match.getObject();
							}
							//Add the new environment to the list of new environments
							newEnvs.add(newEnv);
						}
						//Whether the triple matched within the environment or not, decrement the count of unaccounted for SteM uses.
						count--;
					}
					//If there are unaccounted for uses of the SteM, make a note of it.
					accountedFor = !(count > 0);
				}
				//If there are unaccounted for uses of the SteM, create a new environment that does not fill this triple pattern,
				//and add this triple pattern to the set of viable triple patterns to route to.
				if (!accountedFor){
					newEnvs.add(env);
					// TODO sort out SteM selection: need to store relevant environment, not just SteM.
					// TODO sort out SteM selection: need to fully qualify a pattern environment before deciding which SteMs are viable.
					stemQueue.add(current);
				}
			}
			//Make the new set of environments (those that have led to dead ends removed, new branches from the most recent triple pattern added)
			//the base set of environments.
			envs = newEnvs;
		}
		
		// By this stage the probing graph has been verified against the current pattern for all environments.
		// Check to see if the probing graph matches the current pattern completely
		// (only requires checking that the graph and the pattern are the same size)
		if (g.size() == pattern.length) {
			this.reportCompletePattern(envs);
			return;
		}
		
		//TODO select a stem to send to, then send a probe request to it, satisfying as many environments as possible.
	}
	
	private void reportCompletePattern(List<Node[]> bindings) {
		System.out.println(String.format("Pattern %s complete, with bindings:", (Object) pattern));
		for (Node[] binding : bindings)
			System.out.println(binding.toString());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		Set<TripleMatch> stems = new HashSet<TripleMatch>();
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
	
	// INNER CLASSES
	
	public static class SQPEddyStubStormGraphRouter extends EddyStubStormGraphRouter {

		private static final long serialVersionUID = -1974101140071769900L;

		public SQPEddyStubStormGraphRouter(List<String> eddies) {
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
