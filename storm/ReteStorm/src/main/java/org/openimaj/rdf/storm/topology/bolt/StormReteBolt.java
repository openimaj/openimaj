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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormSinkNode;

import scala.actors.threadpool.Arrays;
import backtype.storm.spout.ISpout;
import backtype.storm.task.IBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 *
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class StormReteBolt extends BaseRichBolt implements RETEStormSinkNode {
//
//	/**
//	 * These are the non-rule-bound fields that all Rete bolts output alongside
//	 * the rule specific variable bindings.
//	 */
//	public static final String[] BASE_FIELDS = { "isAdd", "graph", "timestamp" };

	/**
	 * Meta-Data components of the storm values/fields list
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>, Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Component {

		/**
		 *
		 */
		isAdd
		,
		/**
		 *
		 */
		graph
		,
		/**
		 *
		 */
		timestamp;
		private static String[] strings;
		static{
			Component[] vals = Component.values();
			strings = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strings[i] = vals[i].toString();
			}
		}
		/**
		 * @return like {@link #values()} but {@link String} instances
		 */
		public static String[] strings() {
			return strings;
		}
	}

	private static final long serialVersionUID = -748651304134295713L;

	protected OutputCollector collector;
	protected TopologyContext context;
	@SuppressWarnings("rawtypes")
	protected Map stormConf;


	private Values toFire;
	private boolean active;

	private int[] usageStatistics = { 0, 0 };
	private double[] costStatistics = { 0, 0 };
	/**
	 * The constant value for accessing potential statistics.
	 */
	public static final int POTENTIAL = 0;
	/**
	 * The constant value for accessing implemented statistics.
	 */
	public static final int ACTUAL = 1;



	// ******** getting/setting ********

	/**
	 * @param statIndex
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void incrementUsage(int statIndex) throws ArrayIndexOutOfBoundsException {
		this.usageStatistics[statIndex]++;
	}

	/**
	 * @param statIndex
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void decrementUsage(int statIndex) throws ArrayIndexOutOfBoundsException {
		this.usageStatistics[statIndex]--;
	}

	/**
	 * @param statIndex
	 * @return int
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public int getUsage(int statIndex) throws ArrayIndexOutOfBoundsException {
		return this.usageStatistics[statIndex];
	}

	/**
	 * @param statIndex
	 * @return double
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public double getCost(int statIndex) throws ArrayIndexOutOfBoundsException {
		return this.costStatistics[statIndex];
	}

	// ******** Preparation ********
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.context = context;
		this.stormConf = stormConf;
		prepare();

		this.active = true;
	}

	/**
	 * Initialise the Bolt with complex fields only necessary during topology
	 * operation.
	 */
	public abstract void prepare();

	// ******** Node/Bolt Operation ********

	@Override
	public void fire(Values output, boolean isAdd) {
		this.toFire = output;
	}

	/**
	 * Emit the {@link Values} instance that has been prepared for firing, using
	 * the provided {@link Tuple} as the anchor.
	 *
	 * @param anchor
	 */
	protected void emit(Tuple anchor) {
		if (this.toFire != null) {
			this.collector.emit(anchor, toFire);
		}
	}

	/**
	 * Acknowledge the input {@link Tuple} as per Storm requirements, then set
	 * the toFire variable to null as per Jena.
	 *
	 * @param input
	 */
	protected void acknowledge(Tuple input) {
		this.collector.ack(input);
		this.toFire = null;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		List<String> fields = new ArrayList<String>();
		for (int i = 0; i < this.getVariableCount(); i++)
			fields.add("?" + i);
		fields.addAll(Arrays.asList(Component.strings()));
		declarer.declare(new Fields(fields));
	}

	/**
	 * To allow the variable name independant defenition of fields return the number
	 * of variables
	 * @return number of variables
	 */
	public abstract int getVariableCount();

	// ******** Value <-> Graph Conversion ********
	/**
	 * Given a tuple generated from an Storm {@link ISpout} or {@link IBolt}
	 * using the same class of RETEStormTranslator, create a Jena {@link Graph}
	 * instance.
	 *
	 * @param input
	 * @return List of one Jena {@link Triple} instance from the Tuple's fields
	 * @throws ClassCastException
	 */
	public static Graph asGraph(Tuple input) throws ClassCastException {
		return (Graph) input.getValueByField(Component.graph.toString());
	}

	/**
	 * Given a Jena {@link Graph} construct a {@link Values} instance which is
	 * the subject, predicate and object of the triple calling
	 * {@link Node#toString()}
	 *
	 * @param graph
	 * @param fieldsTemplate
	 * @param seen
	 * @return a {@link Values} instance
	 */
	public static Values asValues(Graph graph, TriplePattern fieldsTemplate, List<Node> seen) {
		Values values = new Values();

		boolean varSub = false, varPred = false, varObj = false;
		if (fieldsTemplate.getSubject().isVariable() && !seen.contains(fieldsTemplate.getSubject())) {
			varSub = true;
			seen.add(fieldsTemplate.getSubject());
		}
		if (fieldsTemplate.getPredicate().isVariable() && !seen.contains(fieldsTemplate.getPredicate())) {
			varPred = true;
			seen.add(fieldsTemplate.getPredicate());
		}
		if (fieldsTemplate.getObject().isVariable() && !seen.contains(fieldsTemplate.getObject())) {
			varObj = true;
			seen.add(fieldsTemplate.getObject());
		}

		ExtendedIterator<Triple> iter = graph.find(fieldsTemplate.asTripleMatch());
		while (iter.hasNext()) {
			Triple t = iter.next();

			if (varSub)
				values.add(t.getSubject());
			if (varPred)
				values.add(t.getPredicate());
			if (varObj)
				values.add(t.getObject());
		}

		return values;
	}

	/**
	 * @param fieldsTemplate
	 * @return String[]
	 */
	public static String[] extractJoinFields(List<ClauseEntry> fieldsTemplate) {
		ArrayList<String> fields = new ArrayList<String>();
		List<String> seen = new ArrayList<String>();
		String var;
		for (ClauseEntry ce : fieldsTemplate)
			if (ce instanceof TriplePattern) {
				TriplePattern tp = (TriplePattern) ce;
				if (tp.getSubject().isVariable()) {
					if (!seen.contains(var = tp.getSubject().getName()))
						seen.add(var);
					else if (!fields.contains(var = tp.getSubject().getName()))
						fields.add(var);
				}
				if (tp.getPredicate().isVariable()) {
					if (!seen.contains(var = tp.getPredicate().getName()))
						seen.add(var);
					else if (!fields.contains(var = tp.getPredicate().getName()))
						fields.add(var);
				}
				if (tp.getObject().isVariable()) {
					if (!seen.contains(var = tp.getObject().getName()))
						seen.add(var);
					else if (!fields.contains(var = tp.getObject().getName()))
						fields.add(var);
				} else if (tp.getObject().isLiteral() && tp.getObject().getLiteralValue() instanceof Functor)
					for (Node n : ((Functor) tp.getObject().getLiteralValue()).getArgs())
						if (n.isVariable()) {
							if (!seen.contains(var = n.getName()))
								seen.add(var);
							else if (!fields.contains(var = n.getName()))
								fields.add(var);
						}
			}

		fields.trimToSize();
		return fields.toArray(new String[0]);
	}

}
