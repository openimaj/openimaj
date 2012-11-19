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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.writing.WritingScheme;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangNTriples;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Triple;

/**
 * This writer queues a set of RDF triples in a kestrel queue as storm
 * {@link Tuple} instances defined by the {@link WritingScheme} used. The
 * triples are written as NTriple strings by default, but other serialisations
 * can be specified
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class KestrelTupleWriter implements Sink<Triple> {

	protected final static Logger logger = Logger
			.getLogger(KestrelTupleWriter.class);

	private List<InputStream> tripleSources;
	private KestrelThriftClient client;

	private String[] queues;

	/**
	 * @param url
	 *            the source of triples
	 * @throws IOException
	 */
	public KestrelTupleWriter(URL url) throws IOException {
		InputStream tripleSource = url.openStream();
		this.tripleSources.add(tripleSource);
	}

	/**
	 * @param stream
	 *            the source of triples
	 * @throws IOException
	 */
	public KestrelTupleWriter(InputStream stream) throws IOException {
		this.tripleSources.add(stream);
	}

	/**
	 * Read tuples into a kestrel queue from a list of URLs simultaniously
	 * @param urlList
	 * @throws IOException
	 */
	public KestrelTupleWriter(ArrayList<URL> urlList) throws IOException {
		this.tripleSources = new ArrayList<InputStream>();
		for (URL url : urlList) {
			this.tripleSources.add(url.openStream());
		}
	}

	/**
	 * Write the triples from the URL to the {@link KestrelServerSpec} to the
	 * queue
	 *
	 * @param spec
	 * @param queues
	 * @throws TException
	 * @throws IOException
	 */
	public void write(KestrelServerSpec spec, String... queues) throws TException,
			IOException {
		logger.debug("Opening kestrel client");
		this.client = new KestrelThriftClient(spec.host, spec.port);
		this.queues = queues;
		logger.debug("Deleting the old queue");
		for (String queue : queues) {
			client.delete_queue(queue);
		}
		Parallel.forEach(this.tripleSources, new Operation<InputStream>(){
			@Override
			public void perform(InputStream tripleSource) {
				LangNTriples parser = RiotReader.createParserNTriples(tripleSource, KestrelTupleWriter.this);
				parser.parse();
				logger.debug("Finished parsing");
			}
		});

	}

	@Override
	public void close() {
	}

	@Override
	public abstract void send(Triple item);

	@Override
	public void flush() {
		logger.debug("Queue flushed");
	}

	/**
	 * @return the next {@link KestrelThriftClient} instance ready to be written
	 *         to
	 */
	public KestrelThriftClient getNextClient() {
		return this.client;
	}

	/**
	 * @return the list of queues to write to
	 */
	public String[] getQueues() {
		return this.queues;
	}

}
