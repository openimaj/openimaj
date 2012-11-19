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
package org.openimaj.kestrel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.writing.GraphWritingScheme;
import org.openimaj.kestrel.writing.WritingScheme;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

/**
 * This writer queues a set of RDF triples in a kestrel queue as storm
 * {@link Tuple} instances defined by the {@link WritingScheme} used. The
 * triples are written as NTriple strings by default, but other serialisations
 * can be specified
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GraphKestrelTupleWriter extends KestrelTupleWriter {

	protected final static Logger logger = Logger
			.getLogger(GraphKestrelTupleWriter.class);
	private WritingScheme scheme;

	/**
	 * @param url
	 *            the source of triples
	 * @throws IOException
	 */
	public GraphKestrelTupleWriter(URL url) throws IOException {
		super(url);
		this.scheme = new GraphWritingScheme();
	}

	/**
	 * @param stream
	 *            the source of triples
	 * @throws IOException
	 */
	public GraphKestrelTupleWriter(InputStream stream) throws IOException {
		super(stream);
		this.scheme = new GraphWritingScheme();
	}

	/**
	 * see {@link KestrelTupleWriter#KestrelTupleWriter(ArrayList)}
	 * @param urlList
	 * @throws IOException
	 */
	public GraphKestrelTupleWriter(ArrayList<URL> urlList) throws IOException {
		super(urlList);
		this.scheme = new GraphWritingScheme();
	}

	long triplesCount = 1;

	@Override
	public synchronized void send(Triple item) {
		Graph graph = GraphFactory.createGraphMem();
		graph.add(item);
		if(triplesCount % 1000 == 0){
			logger.debug("Triples written: " + triplesCount);
		}
		triplesCount++;
		List<Object> tripleList = StormReteBolt.asValues(true, graph, 0l);
		byte[] serialised = this.scheme.serialize(tripleList);

		try {
			for (String queue : this.getQueues()) {
				this.getNextClient().put(queue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
			}
		} catch (TException e) {
			logger.error("Failed to add");
		}
	}

}
