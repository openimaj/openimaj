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
package org.openimaj.rdf.storm.topology.bolt;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.spout.NTriplesSpout;
import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * The conflict set recieves bindings and rules. Given a rule's head the
 * assumption is made that the head should fire and this is done currently the
 * conflict set supports heads which are triples
 *
 * The conflict set is where an inference graph should be maintained if
 * required, more interesting stuff probably happens down stream so for now we
 * don't support this
 *
 * In most Rete networks there is explicitly 1 and only 1 {@link ReteConflictSetBolt} instance
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteConflictSetBolt extends ReteBolt {

	protected final static Logger logger = Logger.getLogger(ReteConflictSetBolt.class);

	/**
	 *
	 */
	private static final long serialVersionUID = -8483623791662413868L;

	@Override
	public void execute(final Tuple input) {
		String ruleString = (String) input.getValueByField("rule");
		logger.debug(String.format("Conflict set resolving rule: %s", ruleString));
		Rule rule = Rule.parseRule(ruleString);
		Node[] snodes = (Node[]) input.getValueByField("bindings");
		BindingEnvironment env = new BindingVector(snodes);

		for (int i = 0; i < rule.headLength(); i++) {
			Object hClause = rule.getHeadElement(i);
			if (hClause instanceof TriplePattern) {
				Triple t = env.instantiate((TriplePattern) hClause);
				if (!t.getSubject().isLiteral()) {
					// Only add the result if it is legal at the RDF level.
					// E.g. RDFS rules can create assertions about literals
					// that we can't record in RDF

					// Further in Jena's RETE implementation 2 extra things are checked at this point
					// 1) is this an add or remove?
						// we only support add at the moment
					// 2) has the triple already been emitted
						// we hold no context, we understand no stream, we emit regardless! perhaps a window here?
					emitTriple(input,t);
					collector.ack(input);
				}
			}
			else if (hClause instanceof Functor ) {
				Functor f = (Functor)hClause;
                Builtin imp = f.getImplementor();
				ReteTopologyRuleContext context = new ReteTopologyRuleContext(rule, env){
					@Override
					public void add(Triple t) {
						emitTriple(input,t);
					}
				};
				try{
					if (imp != null) {
						imp.headAction(f.getBoundArgs(env), f.getArgLength(), context);
					} else {
						logger.error("Invoking undefined Functor " + f.getName() +" in " + rule.toShortString());
					}
				} finally{
					collector.ack(input);
				}
			}
//			else if (hClause instanceof Rule) {
//				Rule r = (Rule) hClause;
//				if (r.isBackward()) {
//					if (isAdd) {
//						infGraph.addBRule(r.instantiate(env));
//					} else {
//						infGraph.deleteBRule(r.instantiate(env));
//					}
//				} else {
//					throw new ReasonerException("Found non-backward subrule : " + r);
//				}
//			}
		}
	}

	/**
	 * called when a new {@link Triple} is infered and should be emitted.
	 * The default behaviour is to emit the {@link Triple} as a tuple in the {@link NTriplesSpout#TRIPLES_FIELD} field.
	 * @param input
	 *
	 * @param t
	 */
	protected void emitTriple(Tuple input, Triple t) {
		logger.debug(String.format("Emitting tripple: %s",t.toString()));
		this.collector.emit(input,NTriplesSpout.asValue(t));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(NTriplesSpout.TRIPLES_FIELD);
	}

	@Override
	protected void prepare() {
		// do nothing
	}

}
