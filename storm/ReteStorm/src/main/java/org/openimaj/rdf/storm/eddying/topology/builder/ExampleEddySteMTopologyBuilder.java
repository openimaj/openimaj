package org.openimaj.rdf.storm.eddying.topology.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openimaj.rdf.storm.eddying.eddies.StormEddyBolt;
import org.openimaj.rdf.storm.eddying.routing.ExampleStormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.SingleQueryPolicyStormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.Action;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteFilterBolt;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.storm.spout.SimpleSpout;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class ExampleEddySteMTopologyBuilder extends TopologyBuilder {
	
	private String[] subjects = {"0","1","2","3","4","5","6","7","8","9"},
					 predicates = {"pred0",
								   "pred1",
								   "pred2"};
	
	public Config initialiseConfig(){
		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(2);
		JenaStormUtils.registerSerializers(conf);
		return conf;
	}
	
	public void build(){
		final String spoutname = "spout";
		final String eddyname = "eddy";
		final String alphaprefix = "alpha";
		final String transprefix = "trans";
		final String stemprefix = "stem";
		
		// Spout
		SimpleSpout spout = new ExampleNTriplesSpout(subjects,predicates);
		
		// Alpha Network
		StormReteFilterBolt[] filters = new StormReteFilterBolt[predicates.length];
		AlphaToStemTranslatorBolt[] translators = new AlphaToStemTranslatorBolt[predicates.length];
		for (int i = 0; i < filters.length; i++){
			List<ClauseEntry> clause= new ArrayList<ClauseEntry>();
			ClauseEntry triple = new TriplePattern(Node.createVariable("A"),
												   Node.createLiteral(predicates[i]),
												   Node.createVariable("B"));
			clause.add(triple);
			filters[i] = new StormReteFilterBolt(new Rule(clause, clause));
			translators[i] = new AlphaToStemTranslatorBolt();
		}
		
		// SteMs
		Map<String,String> stemMap = new HashMap<String,String>();
		StormSteMBolt[] stems = new StormSteMBolt[predicates.length];
		for (int i = 0; i < stems.length; i++) {
			List<String> eddies = new ArrayList<String>();
			eddies.add(eddyname);
			stems[i] = new StormSteMBolt(stemprefix+i,new SingleQueryPolicyStormGraphRouter.SQPESStormGraphRouter(eddies));
			stemMap.put(stemprefix+i, ","+predicates[i]+",");
		}
		
		// Eddy
		StormEddyBolt eddy = new StormEddyBolt(new ExampleStormGraphRouter(stemMap));
		
		// Construct Topology
		this.setSpout(spoutname, spout);
		BoltDeclarer eddyDeclarer = this.setBolt(eddyname, eddy);
		for (int i = 0; i < predicates.length; i++){
			this.setBolt(alphaprefix+i, filters[i]).shuffleGrouping(spoutname);
			this.setBolt(transprefix+i, translators[i]).shuffleGrouping(alphaprefix+i);
			this.setBolt(stemprefix+i, stems[i]).fieldsGrouping(eddyname, stemprefix+i, new Fields("s","p","o"))
												.fieldsGrouping(transprefix+i, new Fields("s","p","o"));
			eddyDeclarer.shuffleGrouping(stemprefix+i,eddyname);
		}
	}
	
}

class ExampleNTriplesSpout extends SimpleSpout {

	private static final long serialVersionUID = 57751357468487569L;

	private Random random;
	private String[] subjects, predicates;
	
	public ExampleNTriplesSpout(String[] subs, String[] preds){
		subjects = subs;
		predicates = preds;
	}
	
	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
					 SpoutOutputCollector collector){
		super.open(conf, context, collector);
		random = new Random();
	}
	
	@Override
	public void nextTuple() {
		Utils.sleep(100);
		Triple t = new Triple(Node.createLiteral(subjects[random.nextInt(subjects.length)]),
							  Node.createLiteral(predicates[random.nextInt(predicates.length)]),
							  Node.createLiteral(subjects[random.nextInt(subjects.length)]));
		Graph graph = new GraphMem();
		graph.add(t);
		try {
			this.collector.emit(StormReteBolt.asValues(true,graph,new Date().getTime()));
		} catch (Exception e) {

		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(StormReteBolt.declaredFields(0));
	}
	
}

class AlphaToStemTranslatorBolt extends BaseRichBolt {

	private static final long serialVersionUID = 527786537447539815L;
	private OutputCollector collector;
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf,
						TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		Values vals = new Values();
		Graph g = (Graph) input.getValueByField(Component.graph.toString());
		Triple t = g.find(null,null,null).next();
		
		vals.add(t.getSubject());
		vals.add(t.getPredicate());
		vals.add(t.getObject());
		
		vals.add(Action.build);
		vals.add(input.getBooleanByField(Component.isAdd.toString()));
		vals.add(g);
		vals.add(input.getLongByField(Component.timestamp.toString()));
		
		this.collector.emit(input, vals);
		this.collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("s","p","o",
									Component.action.toString(),
									Component.isAdd.toString(),
									Component.graph.toString(),
									Component.timestamp.toString()));
	}
	
}