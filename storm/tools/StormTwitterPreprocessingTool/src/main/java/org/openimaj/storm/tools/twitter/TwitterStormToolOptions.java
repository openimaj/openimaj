/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.storm.tools.twitter;

import java.io.IOException;

import org.apache.thrift7.TException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.storm.scheme.StringScheme;
import org.openimaj.storm.tool.StormToolOptions;
import org.openimaj.storm.tools.twitter.bolts.TweetPreprocessingBolt;
import org.openimaj.storm.utils.KestrelUtils;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.UnreliableKestrelThriftSpout;
import backtype.storm.topology.TopologyBuilder;

final class TwitterStormToolOptions extends StormToolOptions {
	@Option(
			name = "--topology-name",
			aliases = "-tn",
			required = false,
			usage = "The name of the topology being submitted. If not provided defaults to <ruleLanguage>_topology_<launchTimeInMillis>",
			metaVar = "STRING")
	public String topologyName = "twitterTopology";

	@Option(
			name = "--output-expire-time",
			aliases = "-expire",
			required = false,
			usage = "The name of the topology being submitted. If not provided defaults to <ruleLanguage>_topology_<launchTimeInMillis>",
			metaVar = "STRING")
	public int expire = 0;
	private Config preparedConfig;

	@Option(name = "--twitter-tool-options", aliases = "-twitter", required = false, usage = "Arguments to hand to each twitter preprocessing bolt", metaVar = "STRING")
	private String twitterOptions = null;
	private String inputQueue;
	private String outputQueue;

	TwitterStormToolOptions(String[] args) {
		super(args);
	}

	@Override
	public Config prepareConfig() {
		if (preparedConfig == null) {
			preparedConfig = new Config();
			preparedConfig.setMaxSpoutPending(500);
			preparedConfig.setNumWorkers(numberOfWorkers);
			preparedConfig.setFallBackOnJavaSerialization(false);
			preparedConfig.setSkipMissingKryoRegistrations(false);
			JenaStormUtils.registerSerializers(preparedConfig);
		}

		return preparedConfig;
	}

	@Override
	public StormTopology constructTopology() {
		TopologyBuilder b = new TopologyBuilder();
		b.setSpout("twitterKestrelSpout", new UnreliableKestrelThriftSpout(kestrelSpecList, new StringScheme("tweet"), inputQueue));
		TweetPreprocessingBolt bolt = new TweetPreprocessingBolt(outputQueue, kestrelHosts, twitterOptions.split(" "));
		bolt.setExpireTime(this.expire);
		b.setBolt("preprocessing", bolt).shuffleGrouping("twitterKestrelSpout");
		return b.createTopology();
	}

	@Override
	public String topologyName() {
		return topologyName;
	}

	@Override
	public void topologyCleanup() {

	}

	@Override
	public String getExtractUsageInfo() {
		return "";
	}

	@Override
	public void validate(CmdLineParser parser) throws CmdLineException, IOException {
		this.inputQueue = this.getInput();
		if (inputQueue == null)
			inputQueue = "twitterInputQueue";
		this.outputQueue = this.getOutput();
		if (outputQueue == null)
			outputQueue = "twitterOutputQueue";
		//
		//		try {
		//			prepareQueues();
		//		} catch (TException e) {
		//			// couldn't delete queues
		//		}
	}

	/**
	 * @throws TException
	 */
	public void prepareQueues() throws TException {
		for (KestrelServerSpec ks : this.kestrelSpecList) {
			KestrelUtils.deleteQueues(ks, inputQueue, outputQueue);
		}
	}
}