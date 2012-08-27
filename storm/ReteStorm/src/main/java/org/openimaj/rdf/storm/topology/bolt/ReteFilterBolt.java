package org.openimaj.rdf.storm.topology.bolt;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.spout.NTriplesSpout;
import org.openimaj.rdf.storm.topology.ReteRuleUtil;
import org.openimaj.util.pair.IndependentPair;

import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * A Storm {@link IRichBolt} which encapsulates the functionality of
 * {@link RETEClauseFilter} instances
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteFilterBolt extends ReteBolt {

	protected final static Logger logger = Logger.getLogger(ReteFilterBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = -7182898087972882187L;
	private RETEClauseFilter clauseNode = null;
	private String ruleString;
	private int clauseIndex;
	private BindingVector toFire;

	/**
	 * This filter holds a {@link Rule} and a clause index
	 * @param rule
	 * @param clauseIndex
	 */
	public ReteFilterBolt(Rule rule, int clauseIndex) {
		this.ruleString = rule.toString();
		this.clauseIndex = clauseIndex;
	}

	/**
	 * @return the components of this clause which are variables
	 */
	public ArrayList<Node> getClauseVars() {
		IndependentPair<RETEClauseFilter,ArrayList<Node>> filterClauseVars = ReteRuleUtil.compileRuleExtractClause(ruleString, clauseIndex);
		return filterClauseVars.getSecondObject();
	}

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void execute(Tuple input) {
		this.toFire = null;
		if(logger.isDebugEnabled()){
			ClauseEntry clauseEntry = ReteRuleUtil.extractRuleBodyIndex(ruleString, clauseIndex);
			logger.debug(String.format("Executing: %s",clauseEntry));
		}
		Triple t = NTriplesSpout.asTriple(input);
		logger.debug(String.format("Filter recieved triple: %s",t));
		this.clauseNode.fire(t, true);
		if(toFire == null){
			logger.debug(String.format("Rule did not fire"));
			collector.ack(input);
			return; // did not match the filter, quit!
		}
		logger.debug(String.format("Rule fired!"));
		this.emitBinding(input,toFire);
		collector.ack(input);
		this.toFire = null;
	}

	@Override
	public void prepare() {
		IndependentPair<RETEClauseFilter,ArrayList<Node>> filterClauseVars = ReteRuleUtil.compileRuleExtractClause(ruleString, clauseIndex);
		this.clauseNode = filterClauseVars.firstObject();
		this.clauseNode.setContinuation(this);

	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
		if(isAdd){
			this.toFire = env;
		}
	}

}
