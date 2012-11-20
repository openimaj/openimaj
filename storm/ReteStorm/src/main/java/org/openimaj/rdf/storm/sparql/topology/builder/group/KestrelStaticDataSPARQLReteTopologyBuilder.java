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
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.KestrelConflictSetSink;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.QuerySolutionSerializer;
import org.openimaj.rdf.storm.sparql.topology.builder.SPARQLReteTopologyBuilderContext;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;
import org.openimaj.rdf.storm.spout.NTriplesSpout;

import backtype.storm.spout.KestrelThriftSpout;
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

	/**
	 * @param streamDataSources
	 *            locations of the kestrel queues
	 * @param inputQueue
	 *            the input queue (reads in ntriples)
	 * @param outputQueue
	 *            the output queue (where to spit out bindings or triples)
	 * @param staticDataSources
	 *            the static datasources to involve in this query
	 */
	public KestrelStaticDataSPARQLReteTopologyBuilder(
			List<KestrelServerSpec> streamDataSources,
			String inputQueue, String outputQueue,
			Map<String, StaticRDFDataset> staticDataSources) {
		this.streamDataSources = streamDataSources;
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		this.staticDataSources = staticDataSources;

	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder, Set<StreamInfo> streams) {
		//		StreamInfo stream = streams.iterator().next();
		List<String> hosts = new ArrayList<String>();
		int port = -1;
		for (KestrelServerSpec serverSpec : this.streamDataSources) {
			hosts.add(serverSpec.host);
			port = serverSpec.port;
		}

		KestrelThriftSpout spout = new KestrelThriftSpout(hosts, port, this.inputQueue, new GraphWritingScheme());
		builder.setSpout(TRIPLE_SPOUT, spout, 1);

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
