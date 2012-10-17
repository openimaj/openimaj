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
package org.openimaj.rdf.storm.spout;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.storm.spout.SimpleSpout;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangNTriples;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * Given a URL, This spout creates a stream of triples
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@SuppressWarnings("rawtypes")
public class NTriplesSpout extends SimpleSpout implements Sink<Triple> {

	/**
	 *
	 */
	private static final long serialVersionUID = -110531170333631644L;
	private String nTriplesURL;
	private LangNTriples parser;

	/**
	 * the field outputted when triples are contained
	 */
	public static final Fields TRIPLES_FIELD = new Fields("triples");

	/**
	 * @param nTriplesURL
	 *            source of the ntriples
	 * 
	 */
	public NTriplesSpout(String nTriplesURL) {
		this.nTriplesURL = nTriplesURL;
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		URL url;
		try {
			url = new URL(this.nTriplesURL);
			parser = RiotReader.createParserNTriples(url.openStream(), this);
		} catch (Exception e) {
		}
	}

	@Override
	public void nextTuple() {
		if (parser.hasNext()) {
			this.collector.emit(asValue(parser.next()));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(TRIPLES_FIELD);
	}

	@Override
	public void close() {
		super.close();
	}

	/**
	 * @return the fields representing the triples outputted
	 */
	public Fields getFields() {
		return TRIPLES_FIELD;
	}

	/**
	 * Given a tuple generated from an {@link NTriplesSpout}, create a Jena
	 * {@link Triple} instance
	 * 
	 * @param input
	 * @return Jena {@link Triple} instance from the Tuple's fields
	 */
	@SuppressWarnings("unchecked")
	public static Triple asTriple(Tuple input) {
		return ((List<Triple>) input.getValueByField("triples")).get(0);
	}

	/**
	 * Given a Jena {@link Triple} construct a {@link Values} instance which is
	 * the subject, predicate and value of the triple calling
	 * {@link Node#toString()}
	 * 
	 * @param t
	 * @return a Values instances
	 */
	public static Values asValue(Triple t) {
		List<Triple> list = new ArrayList<Triple>();
		list.add(t);
		return new Values(list);
	}

	@Override
	public void send(Triple item) {
		System.out.println("Sent a triple!");
	}

	@Override
	public void flush() {
	}

}
