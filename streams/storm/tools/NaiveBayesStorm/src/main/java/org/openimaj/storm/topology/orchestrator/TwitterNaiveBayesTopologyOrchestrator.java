package org.openimaj.storm.topology.orchestrator;

import java.util.List;

import org.openimaj.kestrel.KestrelServerSpec;

import backtype.storm.generated.StormTopology;
import backtype.storm.scheme.StringScheme;
import backtype.storm.spout.UnreliableKestrelThriftSpout;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterNaiveBayesTopologyOrchestrator implements StormTopologyOrchestrator{

	private List<KestrelServerSpec> serverList;
	private String queue;

	@Override
	public StormTopology buildTopology() {
		UnreliableKestrelThriftSpout spout = new UnreliableKestrelThriftSpout(serverList,new StringScheme(),queue);
		// TwitterBolt bolt = new TwitterBolt();
		return null;
	}

}
