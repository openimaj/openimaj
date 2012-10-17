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
package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.NTripleWritingScheme;
import org.openimaj.rdf.storm.topology.bolt.KestrelReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.bolt.ReteFilterBolt;

import backtype.storm.spout.KestrelThriftSpout;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.TopologyBuilder;


/**
 * The {@link KestrelReteTopologyBuilder} is mostly the same as a {@link NTriplesReteTopologyBuilder}
 * but allows for integration and use of kestrel queues for output of triples fed to the network as well
 * as infered triples.
 *
 * This culminates in the replacement of the {@link ReteConflictSetBolt} with the {@link KestrelReteConflictSetBolt}
 * and a slight variation to the default connection of {@link ReteFilterBolt} instances
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelReteTopologyBuilder extends BaseReteTopologyBuilder {
	private KestrelServerSpec spec;
	private String outputQueue;
	private String inputQueue;

	/**
	 * @param spec the kestrel server to connect to
	 * @param inputQueue the queue which the rete network will read again
	 * @param outputQueue the output queue
	 */
	public KestrelReteTopologyBuilder(KestrelServerSpec spec, String inputQueue, String outputQueue) {
		this.spec = spec;
		this.outputQueue = outputQueue;
		this.inputQueue = inputQueue;
	}

	@Override
	public ReteConflictSetBolt constructConflictSetBolt(ReteTopologyBuilderContext context) {
		return new KestrelReteConflictSetBolt(this.spec, this.inputQueue,this.outputQueue);
	}

	@Override
	public void connectFilterBolt(ReteTopologyBuilderContext context,String name, IRichBolt bolt) {
		BoltDeclarer midBuild = context.builder.setBolt(name, bolt);
		// Just add it to the source, it doesn't need to be attached to the conflict set instance directly
		midBuild.shuffleGrouping(context.source);
	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder) {
		KestrelThriftSpout tripleSpout = new KestrelThriftSpout(spec.host, spec.port, inputQueue, new NTripleWritingScheme());
		builder.setSpout(NTriplesReteTopologyBuilder.TRIPLE_SPOUT, tripleSpout, 1);
		return NTriplesReteTopologyBuilder.TRIPLE_SPOUT;
	}
}
