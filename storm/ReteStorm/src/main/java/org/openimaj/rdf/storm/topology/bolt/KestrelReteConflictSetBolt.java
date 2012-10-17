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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.NTripleWritingScheme;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelReteConflictSetBolt extends ReteConflictSetBolt {
	protected final static Logger logger = Logger.getLogger(KestrelReteConflictSetBolt.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -2288621098852277643L;
	private String outputQueue;
	private KestrelThriftClient client;
	private NTripleWritingScheme scheme;
	private int spec_port;
	private String spec_host;
	private String inputQueue;

	/**
	 * Hold the kestrel server and queues to emit to.
	 * 
	 * @param spec
	 * @param inputQueue
	 * @param outputQueue
	 */
	public KestrelReteConflictSetBolt(KestrelServerSpec spec, String inputQueue, String outputQueue) {
		this.spec_host = spec.host;
		this.spec_port = spec.port;
		this.outputQueue = outputQueue;
		this.inputQueue = inputQueue;
	}

	@Override
	protected void prepare() {
		try {
			client = new KestrelThriftClient(spec_host, spec_port);
			scheme = new NTripleWritingScheme();
		} catch (TException e) {

		}

	}

	@Override
	protected void emitTriple(Tuple input, Triple t) {
		if (client != null) {
			logger.debug(String.format("Adding triple %s to queue %s and %s", t.toString(), this.inputQueue, this.outputQueue));
			List<Object> tripleList = Arrays.asList((Object) t);
			byte[] serialised = this.scheme.serialize(tripleList);
			try {
				this.client.put(this.outputQueue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
				this.client.put(this.inputQueue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
			} catch (TException e) {
				logger.error("Failed to write to client: " + e.getMessage());
			}
		}
	}

}
