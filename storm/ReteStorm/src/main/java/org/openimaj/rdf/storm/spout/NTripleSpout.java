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
package org.openimaj.rdf.storm.spout;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.storm.spout.SimpleSpout;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangNTriples;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * Given a URL, This spout creates a stream of triples formatted to Storm fields according to a Jena RETE <-> Storm translator.
 * Based on the {@link NTriplesSpout} by Sina Samangooei <ss@ecs.soton.ac.uk>
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class NTripleSpout extends SimpleSpout implements Sink<Triple> {

	private String nTriplesURL;
	private LangNTriples parser;

	/**
	 * the fields outputted when triples are contained
	 */
	private static final Fields PART_FIELDS = new Fields("?s","?p","?o");
	private static final TriplePattern template = new TriplePattern(Node.createVariable("?s"),
																	Node.createVariable("?p"),
																	Node.createVariable("?o"));

	/**
	 * @param nTriplesURL
	 *            source of the ntriples
	 * 
	 */
	public NTripleSpout(String nTriplesURL) {
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
			Graph graph = new GraphMem();
			graph.add(parser.next());
			try {
				this.collector.emit(StormReteBolt.asValues(graph,template,new ArrayList<Node>()));
			} catch (Exception e) {
				
			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(PART_FIELDS);
	}

	@Override
	public void close() {
		super.close();
	}

	/**
	 * @return the fields representing the triples outputted
	 */
	public Fields getFields() {
		return PART_FIELDS;
	}

	@Override
	public void send(Triple item) {
		System.out.println("Sent a triple!");
	}

	@Override
	public void flush() {
	}

}