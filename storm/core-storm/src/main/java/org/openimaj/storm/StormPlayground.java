package org.openimaj.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */

public class StormPlayground {
	static{
		Logger.getRootLogger().setLevel(Level.FATAL);
	}
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class RandomFieldSpout extends BaseRichSpout{
		private static final String FIELD_TEMPLATE = "field_%d";
		private int nFields;
		private int rootRandomSeed;
		private ArrayList<Uniform> randomGenerators;
		private SpoutOutputCollector collector;
		private int min;
		private int max;

		public RandomFieldSpout(int nFields, int rootRandomSeed, int min, int max) {
			this.nFields = nFields;
			this.rootRandomSeed = rootRandomSeed;
			this.min = min;
			this.max = max;
		}

		public RandomFieldSpout(int nFields, int min, int max) {
			this(nFields,0,min,max);
		}

		@Override
		public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
			this.randomGenerators = new ArrayList<Uniform>();
			for (int i = 0; i < nFields; i++) {
				this.randomGenerators.add(new Uniform(min,max,new MersenneTwister(rootRandomSeed + i)));
			}
			this.collector = collector;
		}

		@Override
		public void nextTuple() {
			this.collector.emit(generate());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

		private Values generate() {
			Values ret = new Values();
			for (Uniform r : this.randomGenerators) {
				ret.add(r.nextIntFromTo(min, max));
			}
			return ret;
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(getFields());
		}

		public Fields getFields() {
			List<String> fieldNames = new ArrayList<String>();
			for (int i = 0; i < nFields; i++) {
				fieldNames.add(String.format(FIELD_TEMPLATE, i));
			}
			return new Fields(fieldNames);
		}

	}

	public static class JoinBolt extends BaseRichBolt{

		@Override
		public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		}

		@Override
		public void execute(Tuple input) {
			System.out.println(this.toString() + ": " + input);
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields());

		}

		public static void connectNewBolt(TopologyBuilder builder) {
			JoinBolt b = new JoinBolt();
			builder.setBolt("joinBolt", b,2).fieldsGrouping("randomSpout1", new Fields("field_0")).fieldsGrouping("randomSpout2", new Fields("field_1"));
		}

	}


	public static void main(String[] args) {
		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		conf.setFallBackOnJavaSerialization(false);
		conf.setSkipMissingKryoRegistrations(false);
		LocalCluster cluster = new LocalCluster();
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("randomSpout1", new RandomFieldSpout(2,0, 0, 1)); // (nfields,seed,min,max)
		builder.setSpout("randomSpout2", new RandomFieldSpout(2,10, 0, 1)); // (nfields,seed,min,max)
		JoinBolt.connectNewBolt(builder);
		StormTopology topology = builder.createTopology();
		cluster.submitTopology("playTopology", conf, topology);
		Utils.sleep(10000);
		cluster.killTopology("playTopology");
		cluster.shutdown();

	}
}
