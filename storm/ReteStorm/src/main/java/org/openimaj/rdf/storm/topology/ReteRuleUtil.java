/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.topology;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETENode;

/**
 * Collection of utils for parsing Jena {@link Rule} instances and constructing
 * various {@link RETENode} instances
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteRuleUtil {
	protected final static Logger logger = Logger.getLogger(ReteRuleUtil.class);

	/**
	 * Given an individual rule, extract the specific clause from the body and
	 * the variable assignments of the clause
	 *
	 * @param ruleString
	 * @param clauseIndex
	 * @return the clause and the list of variables in the clause
	 */
	public static IndependentPair<RETEClauseFilter, ArrayList<Node>> compileRuleExtractClause(String ruleString,
			int clauseIndex)
	{
		Rule rule = Rule.parseRule(ruleString);
		int numVars = rule.getNumVars();
		TriplePattern clausePattern = (TriplePattern) rule.getBody()[clauseIndex];
		ArrayList<Node> tempClauseVars = new ArrayList<Node>(numVars);
		RETEClauseFilter filter = RETEClauseFilter.compile(clausePattern, numVars, tempClauseVars);
		return IndependentPair.pair(filter, tempClauseVars);
	}

	/**
	 * Given an individual rule, extract the specific clause from the body and
	 * return the pattern
	 *
	 * @param ruleString
	 * @param clauseIndex
	 * @return the clause entry
	 */
	public static ClauseEntry extractRuleBodyIndex(String ruleString, int clauseIndex) {
		Rule rule = Rule.parseRule(ruleString);
		ClauseEntry clausePattern = rule.getBody()[clauseIndex];
		return clausePattern;
	}


}
