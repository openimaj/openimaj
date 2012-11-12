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
package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.io.IOUtils;
import org.openimaj.rdf.storm.bolt.RETEStormNode;

import backtype.storm.topology.IRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphMap;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

/**
 * A Storm {@link IRichBolt} which encapsulates the functionality of
 * SPARQL {@link ElementFilter} parts of a query
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, Sina Samangooei
 *         <ss@ecs.soton.ac.uk>
 * 
 */
public class StormSPARQLFilterBolt extends StormSPARQLReteBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = -2941229666460288498L;
	protected final static Logger logger = Logger.getLogger(StormSPARQLFilterBolt.class);
	private byte[] filterBArr;
	private ElementFilter filter = null;

	/**
	 * @param filter
	 */
	public StormSPARQLFilterBolt(ElementFilter filter) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			DataOutputStream out = new DataOutputStream(baos);
			IOUtils.write(filter, out);
			out.flush();

			this.filterBArr = baos.toByteArray();
		} catch (IOException e) {
			logger.error("Couldn't serialize filter!");
		}
	}

	@Override
	public void execute(Tuple input) {
		Binding binding = tupleToBinding(input);
		logger.debug("Checking filter: " + this.filter + " with binding: " + binding);
		Expr expr = this.filter.getExpr();
		Graph g = extractGraph(input);
		DatasetGraph dsg = new DatasetGraphMap(extractGraph(input));
		FunctionEnv execCxt = new FunctionEnvBase(ARQ.getContext(), g, dsg);
		boolean isSat = expr.isSatisfied(binding, execCxt);
		logger.debug("Function satisfied? " + isSat);
		if (isSat) {
			// emit the tuple handed
			Values values = new Values();
			values.addAll(input.getValues());
			this.fire(values, extractIsAdd(input));
			this.emit(input);
		}
		acknowledge(input);
	}

	@Override
	public void prepare() {
		try {
			filter = IOUtils.read(new DataInputStream(new ByteArrayInputStream(this.filterBArr)));
		} catch (IOException e) {
			logger.error("Couldn't de-serialize the filter");
		}
	}

	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy, RETERuleContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the expected source
	 */
	public String getPrevious() {
		return this.sourceVariableMap.keySet().iterator().next();
	}

	@Override
	public int getVariableCount() {
		return this.sourceVariableMap.values().iterator().next().size();
	}

}
