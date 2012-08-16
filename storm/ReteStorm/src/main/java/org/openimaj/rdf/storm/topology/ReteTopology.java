package org.openimaj.rdf.storm.topology;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.spout.NTriplesSpout;
import org.openimaj.storm.bolt.CountingEmittingBolt;
import org.openimaj.storm.bolt.PrintingBolt;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

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

	static {
		Logger.getRootLogger().setLevel(Level.ERROR);
	}

	private String rulesSource;
	private int unnamedRules = 0;

	/**
	 * Construct a Rete topology using the default RDFS rules
	 */
	public ReteTopology() {
		rulesSource = RDFS_RULES;
	}

	/**
	 * Using specified rules, construct a RETE storm topology
	 *
	 * @return a storm topology
	 */
	public StormTopology buildTopology() {
		TopologyBuilder builder = new TopologyBuilder();
		NTriplesSpout tripleSpout = new NTriplesSpout("file:///Users/ss/Datasets/NTriples/eswc2006.nt");
		builder.setSpout(TRIPLE_SPOUT, tripleSpout, 1);
		builder.setBolt(DEBUG_BOLT, new PrintingBolt(), 1).shuffleGrouping(TRIPLE_SPOUT);
		// compileCountingEmittingBolt(builder,tripleSpout.getFields());

		List<Rule> rules = loadRules();
		compileBolts(builder, TRIPLE_SPOUT, rules);

		return builder.createTopology();
	}

	@SuppressWarnings("unused")
	private void compileCountingEmittingBolt(TopologyBuilder builder, Fields fields) {
		CountingEmittingBolt countingEmittingBolt1 = new CountingEmittingBolt(fields);
		builder.setBolt("blah1", countingEmittingBolt1, 1).shuffleGrouping(TRIPLE_SPOUT).shuffleGrouping("blah1");
	}

	/**
	 * Given a set of rules and a Storm {@link TopologyBuilder} compile a set of
	 * ReteBolts which encompass the filter, joins and actions of the rules
	 *
	 * @param builder
	 * @param rules
	 */
	private void compileBolts(TopologyBuilder builder, String source, List<Rule> rules) {
		for (Rule rule : rules) {
			int numVars = rule.getNumVars();

			// The rule name, bolts take the form of ruleName_(BODY|HEAD)_count
			String ruleName = rule.getName();
			if (ruleName == null)
				ruleName = nextRuleName();

			// These are the "alpha" nodes of the rule
			Map<String, ReteFilterBolt> filters = new HashMap<String, ReteFilterBolt>();
			// Extract all the filter clauses
			for (int i = 0; i < rule.bodyLength(); i++) {
				Object clause = rule.getBodyElement(i);
				if (clause instanceof TriplePattern) {
					String boltName = String.format("%s_filter_%d", ruleName, i);
					ReteFilterBolt filterBolt = new ReteFilterBolt((TriplePattern) clause, numVars);
					filters.put(boltName, filterBolt);
				}
			}

			// Now construct the beta networks (this could be optimised)
			boolean priorIsFilter = true;
			String prior = null;
			boolean[] seenVar = new boolean[numVars];
			int joinNumber = 0;
			for (Entry<String, ReteFilterBolt> reteFilterBolt : filters.entrySet()) {
				String current = reteFilterBolt.getKey();
				List<Node_RuleVariable> clauseVars = reteFilterBolt.getValue().getClauseVars();
				ArrayList<Byte> matchIndices = new ArrayList<Byte>(numVars);
				for (Iterator<Node_RuleVariable> iv = clauseVars.iterator(); iv.hasNext();) {
					int varIndex = iv.next().getIndex();
					if (seenVar[varIndex])
						matchIndices.add(new Byte((byte) varIndex));
					seenVar[varIndex] = true;
				}
				if (prior == null) {
					prior = current;
				} else {
					// Construct a ReteJoinBolt which knows which variables it
					// should map on
					String boltName = String.format("%s_join_%d", ruleName, joinNumber++);
					ReteJoinBolt reteJoinBolt = new ReteJoinBolt(prior, current, matchIndices);

				}
			}
		}
	}

	private String nextRuleName() {
		unnamedRules += 1;
		return String.format("unnamed_rule_%d", unnamedRules);
	}

	private List<Rule> loadRules() {
		InputStream instream = ReteTopology.class.getResourceAsStream(rulesSource);
		@SuppressWarnings("unchecked")
		List<Rule> rules = Rule.parseRules(Rule
				.rulesParserFromReader(new BufferedReader(new InputStreamReader(instream))));
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
