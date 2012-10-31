/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
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
package org.openimaj.rdf.storm.topology.bolt;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.RETEStormNode;
import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A Storm {@link IRichBolt} which encapsulates the functionality of
 * {@link RETEClauseFilter} instances
 *
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class StormReteFilterBolt extends StormReteBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2941229666460288498L;
	protected final static Logger logger = Logger.getLogger(ReteFilterBolt.class);
	/**
	 *
	 */
	private TriplePattern filter;

	/**
	 * This filter holds a {@link Rule}.
	 * @param rule
	 */
	public StormReteFilterBolt(Rule rule) {
		super(rule);
	}

	@Override
	public void execute(Tuple input) {
		boolean isAdd = input.getBooleanByField(BASE_FIELDS[IS_ADD]);
		if(logger.isDebugEnabled()){
			logger.debug(String.format("Executing: %s",filter));
		}
		// Extract Jena Graph from Storm Tuple
		Graph graph = asGraph(input);
		// Extract Triples that match this Filter's pattern
		ExtendedIterator<Triple> it = graph.find(filter.asTripleMatch());
		// With each valid triple...
		while (it.hasNext()){
			Triple t = it.next();
			logger.debug(String.format("Filter passed triple: %s",t));
			
			// Create an ArrayList of Nodes of equal size to the number of expected variables,
			// plus all base ReteStorm fields (graph, isAdd, timestamp, etc, as defined in
			// FlexibleReteBolt)
			Values vals = new Values();
			vals.ensureCapacity(this.getVars().length + BASE_FIELDS.length);
			int varCount = 0;
			Map<String,Integer> vars = new HashMap<String,Integer>();
			
			// For each part of the triple, check if the Pattern declares it to be variable
			// (or a functor, in the case of Objects)
			if (filter.getSubject().isVariable()){
				// if it is a variable, insert its value into the array of Values
				vals.set(varCount, t.getSubject());
				vars.put(filter.getSubject().getName(),varCount++);
			}
			
			if (filter.getPredicate().isVariable())
				// For each subsequent variable, check that the variable has not already been
				// seen within this triple. 
				if (vars.containsKey(filter.getPredicate().getName())){
					// If it has and the values are different, then the Triple is not a match, so
					// do not fire, and move onto the next Triple.
					if ( ! t.getPredicate().sameValueAs( vals.get( vars.get( filter.getPredicate().getName() ) ) ) )
						continue;
				} else {
					// If the variable has not been seen before, process the node as with the Subject.
					vals.set(varCount, t.getPredicate());
					vars.put(filter.getPredicate().getName(),varCount++);
				}
			
			if (filter.getObject().isVariable())
				if (vars.containsKey(filter.getObject().getName())){
					if ( ! t.getObject().sameValueAs( vals.get( vars.get( filter.getObject().getName() ) ) ) )
						continue;
				} else {
					vals.set(varCount, t.getObject());
					vars.put(filter.getObject().getName(),varCount++);
				}
			else if (filter.getObject().isLiteral() && filter.getObject().getLiteralValue() instanceof Functor){
				// if the object is a functor, check each node in the functor to see if it is a variable,
				// and treat each as if it were a more traditional part of the Triple.
				Functor f = (Functor)filter.getObject().getLiteralValue();
				Functor functor = (Functor)t.getObject().getLiteralValue();
				for (int i = 0; i < f.getArgs().length; i++){
					Node n = f.getArgs()[i];
					if (n.isVariable())
						if (vars.containsKey(n.getName())){
							if ( ! functor.getArgs()[i].sameValueAs( vals.get( vars.get( n.getName() ) ) ) )
								continue;
						} else {
							vals.set(varCount, functor.getArgs()[i]);
							vars.put(n.getName(), varCount++);
						}
				}
			}
			
			// insert this Tuple's value of isAdd to be passed onto subscribing Bolts.
			vals.set(this.getVars().length + IS_ADD, isAdd);
			
			// in case this Triple has been extracted from a larger graph, create a new Graph
			// containing just this Triple.
			Graph g = new GraphMem();
			g.add(t);
			// insert the new graph into the array of Values
			vals.set(this.getVars().length + GRAPH, g);
			
			// fire the new values
			fire(vals,isAdd);
			// emit using the input Tuple as an anchor
			emit(input);
		}
		// Once all valid triples are extracted and fired individually, acknowledge the input tuple.
		acknowledge(input);
	}

	@Override
	public void prepare() {
		this.filter = (TriplePattern) this.getRule().getBodyElement(0);
	}

	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy,
			RETERuleContext context) {
		// TODO
		return null;
	}

}
