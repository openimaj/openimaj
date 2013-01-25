package org.openimaj.rdf.storm.eddying.routing;

import java.util.Map;

import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public class ExampleStormGraphRouter extends StormGraphRouter {

	private Map<String,String> stems; 
	
	public ExampleStormGraphRouter(Map<String,String> stems){
		this.stems = stems;
	}
	
	@Override
	protected void prepare() {
		
	}

	@Override
	protected long routingTimestamp(long stamp1, long stamp2) {
		return stamp1 > stamp2 ? stamp1 : -1;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
			long timestamp) {
		boolean flag = false;
		String[] parts = {"","",""};
		for (String stem : stems.keySet()) {
			if (flag) {
				Node subject = parts[0].equals("")
								? null
								: parts[0].startsWith("<") || parts[0].startsWith("http://")
									? Node.createURI(parts[0])
									: Node.createLiteral(parts[0]);
				Node predicate = parts[1].equals("")
								? null
								: parts[1].startsWith("<") || parts[1].startsWith("http://")
									? Node.createURI(parts[1])
									: Node.createLiteral(parts[1]);
				Node object = parts[2].equals("")
								? null
								: parts[2].startsWith("<") || parts[2].startsWith("http://")
									? Node.createURI(parts[2])
									: Node.createLiteral(parts[2]);
				Values vals = new Values();
				vals.add(g.find(subject,predicate,object).next().getObject());
				vals.add(Node.createVariable("?p"));
				vals.add(Node.createVariable("?o"));
				vals.add(action);
				vals.add(isAdd);
				vals.add(g);
				vals.add(timestamp);
				this.collector.emit(stem, anchor, vals);
				this.collector.ack(anchor);
				return;
			}
			if (flag = anchor.getSourceComponent().equals(stem)){
				parts = stems.get(stem).split(",");
			}
		}
		if (flag) {
			System.out.println(g.toString());
		} else {
			System.out.println("Should never get here!");
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		for (String stem : stems.keySet())
			declarer.declareStream(stem, new Fields("s","p","o",
													Component.action.toString(),
													Component.isAdd.toString(),
													Component.graph.toString(),
													Component.timestamp.toString()));
	}

}
