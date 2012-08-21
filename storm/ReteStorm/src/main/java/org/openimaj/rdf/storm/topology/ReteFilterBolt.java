package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
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
public class ReteFilterBolt extends ReteBolt  {

	/**
	 *
	 */
	private static final long serialVersionUID = -7182898087972882187L;
	private RETEClauseFilter clauseNode = null;
	private int numVars;
	private Node[] clauseNodes;

	/**
	 * Given a
	 *
	 * @param clause
	 * @param numVars
	 */
	public ReteFilterBolt(TriplePattern clause, int numVars) {
		this.clauseNodes = new Node[]{clause.getSubject(),clause.getPredicate(),clause.getObject()};
		this.numVars = numVars;

	}

	/**
	 * @return the components of this clause which are variables
	 */
	public List<Node_RuleVariable> getClauseVars() {
		ArrayList<Node_RuleVariable> tempClauseVars = new ArrayList<Node_RuleVariable>(numVars);
		RETEClauseFilter.compile(new TriplePattern(clauseNodes[0], clauseNodes[1], clauseNodes[2]), numVars, tempClauseVars);
		return tempClauseVars;
	}

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void execute(Tuple input) {

	}

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		ArrayList<Node_RuleVariable> tempClauseVars = new ArrayList<Node_RuleVariable>(numVars);
		this.clauseNode = RETEClauseFilter.compile(new TriplePattern(clauseNodes[0], clauseNodes[1], clauseNodes[2]), numVars, tempClauseVars);
		this.clauseNode.setContinuation(this);
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
		// TODO Auto-generated method stub

	}

}
