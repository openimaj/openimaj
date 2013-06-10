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
package org.openimaj.rdf.storm.tool.topology;

import org.kohsuke.args4j.Option;
import org.openimaj.storm.tool.StormToolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;

/**
 * The local topology for testing. Allows the specification of sleep time etc.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LocalTopologyMode implements TopologyMode {
	private static final int DEFAULT_SLEEP_TIME = 10000;
	private final static Logger logger = LoggerFactory.getLogger(LocalTopologyMode.class);
	/**
	 * Time to wait in local mode
	 */
	@Option(
			name = "--sleep-time",
			aliases = "-st",
			required = false,
			usage = "How long the local topology should wait while processing happens, -1 waits forever",
			metaVar = "STRING")
	public long sleepTime = DEFAULT_SLEEP_TIME;
	private LocalCluster cluster;

	@Override
	public void submitTopology(StormToolOptions options) throws Exception {
		logger.debug("Configuring topology");
		Config conf = options.prepareConfig();
		logger.debug("Instantiating cluster");
		this.cluster = new LocalCluster();
		logger.debug("Constructing topology");
		StormTopology topology = options.constructTopology();
		logger.debug("Submitting topology");
		cluster.submitTopology(options.topologyName(), conf, topology);
	}

	@Override
	public void finish(StormToolOptions options) throws Exception {
		try {
			if (sleepTime < 0) {
				logger.debug("Waiting forever");
				while (true) {
					Utils.sleep(DEFAULT_SLEEP_TIME);
				}
			} else {
				logger.debug("Waiting " + sleepTime + " milliseconds");
				Utils.sleep(sleepTime);

			}
		} finally {
			logger.debug("Killing topology");
			cluster.killTopology(options.topologyName());
			logger.debug("Shutting down cluster");
			cluster.shutdown();
			options.topologyCleanup();
		}
	}

}
