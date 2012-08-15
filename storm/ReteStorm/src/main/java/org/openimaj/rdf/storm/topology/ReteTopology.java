package org.openimaj.rdf.storm.topology;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.spout.NTriplesSpout;
import org.openimaj.storm.bolt.CountingEmittingBolt;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEClauseFilter;

/**
 * Given a set of rules, construct a RETE topology such that filter (alpha)
 * nodes and join (beta) nodes are filtering bolts
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteTopology {
	private static final String DEBUG_BOLT = "debugBolt";
	private static final String TRIPLE_SPOUT = "tripleSpout";
	private static final String RDFS_RULES = "/org/openimaj/rdf/rules/rdfs-fb-tgc-noresource.rules";

	static{
		Logger.getRootLogger().setLevel(Level.ERROR);
	}

	private String rulesSource;

	/**
	 * Construct a Rete topology using the default RDFS rules
	 */
	public ReteTopology() {
		rulesSource = RDFS_RULES;
	}

	/**
	 * Using specified rules, construct a RETE storm topology
	 * @return a storm topology
	 */
	public StormTopology buildTopology() {
		TopologyBuilder builder = new TopologyBuilder();
		NTriplesSpout tripleSpout = new NTriplesSpout("file:///Users/ss/Datasets/NTriples/eswc2006.nt");
		builder.setSpout(TRIPLE_SPOUT, tripleSpout, 1);
//		builder.setBolt(DEBUG_BOLT, new PrintingBolt(), 1).shuffleGrouping(TRIPLE_SPOUT);
		Fields tripleFields = tripleSpout.getFields();
		CountingEmittingBolt countingEmittingBolt1 = new CountingEmittingBolt(tripleFields);
//		CountingEmittingBolt countingEmittingBolt2 = new CountingEmittingBolt(tripleSpout.getFields());
		builder.setBolt("blah1", countingEmittingBolt1, 1).shuffleGrouping(TRIPLE_SPOUT).shuffleGrouping("blah1");

//		List<Rule> rules = loadRules();
//		compileBolts(builder,rules);

		return builder.createTopology();
	}

	/**
	 * Given a set of rules and a Storm {@link TopologyBuilder} compile
	 * a set of ReteBolts which encompass the filter, joins and actions
	 * of the rules
	 *
	 * @param builder
	 * @param rules
	 */
	private void compileBolts(TopologyBuilder builder, List<Rule> rules) {
		for (Rule rule : rules) {
			int numVars = rule.getNumVars();
			boolean[] seenVar = new boolean[numVars];
			for (int i = 0; i < rule.bodyLength(); i++) {
				Object clause = rule.getBodyElement(i);
				if(clause instanceof TriplePattern){
					ArrayList<Node> clauseVars = new ArrayList<Node>(numVars);
					RETEClauseFilter clauseNode = RETEClauseFilter.compile((TriplePattern)clause, numVars, clauseVars);
					Node predicate = ((TriplePattern)clause).getPredicate();

				}
			}
		}
	}

	private List<Rule> loadRules() {
		InputStream instream = ReteTopology.class.getResourceAsStream(rulesSource);
		@SuppressWarnings("unchecked")
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader( new BufferedReader(new InputStreamReader(instream))));
		return rules;
	}

	/**
	 * run the rete topology
	 *
	 * @param args
	 * @throws InvalidTopologyException
	 * @throws AlreadyAliveException
	 */
	public static void main(String args[]) throws AlreadyAliveException, InvalidTopologyException {
		ReteTopology reteTopology = new ReteTopology();

		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		LocalCluster cluster = new LocalCluster();
		StormTopology topology = reteTopology.buildTopology();
		cluster.submitTopology("reteTopology", conf, topology);
		Utils.sleep(10000);
		cluster.killTopology("reteTopology");
		cluster.shutdown();

	}
}
