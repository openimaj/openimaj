package org.openimaj.rdf.storm.eddying;

import org.junit.Test;
import org.openimaj.rdf.storm.eddying.topology.builder.ExampleEddySteMTopologyBuilder;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.utils.Utils;

public class EddyingTopologyTest {

	@Test
	public void test() {
		ExampleEddySteMTopologyBuilder builder = new ExampleEddySteMTopologyBuilder();
		builder.build();
		Config conf = builder.initialiseConfig();

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, builder.createTopology());
		Utils.sleep(10000);
		cluster.killTopology("test");
		cluster.shutdown();
	}

}
