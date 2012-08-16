package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backtype.storm.topology.IRichBolt;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETESinkNode;

/**
 * A Storm {@link IRichBolt} which encapsulates the functionality of
 * {@link RETEClauseFilter} instances
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteFilterBolt implements RETESinkNode {

	private RETEClauseFilter clauseNode;
	private List<Node_RuleVariable> clauseVars;

	/**
	 * Given a
	 *
	 * @param clause
	 * @param numVars
	 */
	public ReteFilterBolt(TriplePattern clause, int numVars) {
		this.clauseVars = new ArrayList<Node_RuleVariable>(numVars);
		this.clauseNode = RETEClauseFilter.compile(clause, numVars, clauseVars);
		this.clauseNode.setContinuation(this);

	}

	/**
	 * @return the underlying {@link RETEClauseFilter} which this bolt filters for
	 */
	public RETEClauseFilter getClauseNode() {
		return clauseNode;
	}

	/**
	 * @return the components of this clause which are variables
	 */
	public List<Node_RuleVariable> getClauseVars() {
		return clauseVars;
	}

	@Override
	public RETENode clone(@SuppressWarnings("rawtypes") Map netCopy, RETERuleContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fire(BindingVector env, boolean isAdd) {
		// firing should prepare the emit in some way
	}

}
