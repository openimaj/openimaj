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
package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.GraphWritingScheme;
import org.openimaj.kestrel.writing.PlainNTriplesGraphWritingScheme;
import org.openimaj.kestrel.writing.WritingScheme;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.KestrelConflictSetSink;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.QuerySolutionSerializer;
import org.openimaj.rdf.storm.sparql.topology.builder.SPARQLReteTopologyBuilderContext;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;
import org.openimaj.rdf.storm.spout.NTriplesSpout;

import backtype.storm.Config;
import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.spout.UnreliableKestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;
import eu.larkc.csparql.parser.StreamInfo;

/**
 * The {@link KestrelStaticDataSPARQLReteTopologyBuilder} provides triples
 * from URI
 * streams via the {@link NTriplesSpout}.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelStaticDataSPARQLReteTopologyBuilder extends StaticDataSPARQLReteTopologyBuilder {
	private static final Logger logger = Logger.getLogger(KestrelStaticDataSPARQLReteTopologyBuilder.class);
	private static final String TRIPLE_SPOUT = "tripleSource";
	File wang;
	private Map<String, StaticRDFDataset> staticDataSources;
	private List<KestrelServerSpec> streamDataSources;
	private String inputQueue;
	private String outputQueue;
	private QuerySolutionSerializer qss;
	private boolean reliableSpout;
	private String ackQueue;
	private boolean plainNTriples;

	/**
	 * Whether the spout used should be unreliable
	 */
	public static final String RETE_TOPOLOGY_KESTREL_UNRELIABLE = "topology.rete.kestrel.unreliable";
	/**
	 * defualts to false and therefore {@link KestrelThriftSpout} is used
	 */
	public static final boolean RETE_TOPOLOGY_KESTREL_UNRELIABLE_DEFAULT = false;
	/**
	 * The queue which acknowledgment statistics useful for throughput analysis
	 * are published.
	 * This might be ignored if the spout doesn't support this
	 */
	public static final String RETE_TOPOLOGY_KESTREL_ACK_QUEUE = "topology.rete.kestrel.ack_queue";
	/**
	 * The default ackStats queue
	 */
	public static final String RETE_TOPOLOGY_KESTREL_ACK_QUEUE_DEFAULT = "ackStatsQueue";
	/**
	 * Queue contains plain N-Triples or isAdd,timeStamp,nTriples. In the second
	 * case timestamp is set to system.currenttime
	 */
	public static final String RETE_TOPOLOGY_KESTREL_PLAIN_TRIPLES = "topology.rete.kestrel.contains_plain_triples";

	/**
	 * @param streamDataSources
	 *            locations of the kestrel queues
	 * @param inputQueue
	 *            the input queue (reads in ntriples)
	 * @param outputQueue
	 *            the output queue (where to spit out bindings or triples)
	 * @param staticDataSources
	 *            the static datasources to involve in this query
	 * @param config
	 */
	public KestrelStaticDataSPARQLReteTopologyBuilder(
			List<KestrelServerSpec> streamDataSources,
			String inputQueue, String outputQueue,
			Map<String, StaticRDFDataset> staticDataSources, Config config) {
		this.streamDataSources = streamDataSources;
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		this.staticDataSources = staticDataSources;
		Boolean unreliable = (Boolean) config.get(RETE_TOPOLOGY_KESTREL_UNRELIABLE);
		if (unreliable == null)
			unreliable = RETE_TOPOLOGY_KESTREL_UNRELIABLE_DEFAULT;
		this.ackQueue = (String) config.get(RETE_TOPOLOGY_KESTREL_ACK_QUEUE);
		this.reliableSpout = !unreliable;
		Object plainTriplesObj = config.get(RETE_TOPOLOGY_KESTREL_PLAIN_TRIPLES);
		if (plainTriplesObj == null) {
			plainTriplesObj = false;
		}
		else {
			plainNTriples = (Boolean) plainTriplesObj;
		}

	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams) {
		//		StreamInfo stream = streams.iterator().next();

		WritingScheme scheme = null;
		if (this.plainNTriples)
			scheme = new PlainNTriplesGraphWritingScheme();
		else
			scheme = new GraphWritingScheme();
		if (this.reliableSpout) {
			List<String> hosts = new ArrayList<String>();
			int port = -1;
			for (KestrelServerSpec serverSpec : this.streamDataSources) {
				hosts.add(serverSpec.host);
				port = serverSpec.port;
			}

			KestrelThriftSpout spout = new KestrelThriftSpout(hosts, port, this.inputQueue, scheme);
			builder.setSpout(TRIPLE_SPOUT, spout, this.getSpoutBoltParallelism());
		}
		else {
			UnreliableKestrelThriftSpout spout = new UnreliableKestrelThriftSpout(streamDataSources, scheme, this.inputQueue);
			spout.setAckQueue(this.ackQueue);
			builder.setSpout(TRIPLE_SPOUT, spout, this.getSpoutBoltParallelism());
		}

		return TRIPLE_SPOUT;
	}

	@Override
	public StormSPARQLReteConflictSetBoltSink conflictSetSink() {
		KestrelConflictSetSink sink = new KestrelConflictSetSink(streamDataSources, outputQueue, qss);
		return sink;
	}

	@Override
	public List<StaticRDFDataset> staticDataSources(SPARQLReteTopologyBuilderContext context) {
		List<StaticRDFDataset> ret = new ArrayList<StaticRDFDataset>();

		for (Entry<String, StaticRDFDataset> staticRDFURI : this.staticDataSources.entrySet()) {
			ret.add(staticRDFURI.getValue());
		}
		return ret;
	}

	/**
	 * @return the output file
	 */
	public File getOutputFile() {
		return wang;
	}

	/**
	 * @param qss
	 *            the method to write queries
	 */
	public void setQuerySolutionSerializerMode(QuerySolutionSerializer qss) {
		this.qss = qss;
	}

}
