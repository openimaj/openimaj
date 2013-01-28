package org.openimaj.rdf.storm.eddying.routing;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;

import scala.actors.threadpool.Arrays;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public class ExampleStormGraphRouter extends StormGraphRouter {

	private static final long serialVersionUID = 2093846846614800493L;
	private static Logger logger = Logger.getLogger(ExampleStormGraphRouter.class);
	
	private List<String> sortedStems;
	private Map<String,String> stems; 
	
	@SuppressWarnings("unchecked")
	public ExampleStormGraphRouter(Map<String,String> stems){
		this.stems = stems;
		this.sortedStems = (List<String>)Arrays.asList(stems.keySet().toArray()); 
		Collections.sort(sortedStems);
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
		Iterator<String> iter = sortedStems.iterator();
		String stem = "no stems";
		while (iter.hasNext()){
			stem = iter.next();
		}
		String[] parts = stems.get(stem).split(",");
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
		Node object = parts.length < 3 || parts[2].equals("")
						? null
						: parts[2].startsWith("<") || parts[2].startsWith("http://")
							? Node.createURI(parts[2])
							: Node.createLiteral(parts[2]);
		
		if (!g.find(subject,predicate,object).hasNext()){
			boolean flag = false;
			iter = sortedStems.iterator();
			while (iter.hasNext()) {
				stem = iter.next();
				if (flag) {
					subject = parts[0].equals("")
									? null
									: parts[0].startsWith("<") || parts[0].startsWith("http://")
										? Node.createURI(parts[0])
										: Node.createLiteral(parts[0]);
					predicate = parts[1].equals("")
									? null
									: parts[1].startsWith("<") || parts[1].startsWith("http://")
										? Node.createURI(parts[1])
										: Node.createLiteral(parts[1]);
					object = parts.length < 3 || parts[2].equals("")
									? null
									: parts[2].startsWith("<") || parts[2].startsWith("http://")
										? Node.createURI(parts[2])
										: Node.createLiteral(parts[2]);
					Values vals = new Values();
					vals.add(g.find(subject,predicate,object).next().getObject());
					vals.add(Node.createVariable("p"));
					vals.add(Node.createVariable("o"));
					vals.add(Action.probe);
					vals.add(isAdd);
					vals.add(g);
					vals.add(timestamp);
					logger.debug(String.format("\nRouting triple: %s %s %s\nto SteM: %s",
											   vals.get(0),
											   vals.get(1),
											   vals.get(2),
											   stem));
					this.collector.emit(stem, anchor, vals);
					this.collector.ack(anchor);
					return;
				}
				if (flag = anchor.getSourceComponent().equals(stem)){
					parts = stems.get(stem).split(",");
				}
			}
		} else if (g.size() < stems.size()){
			int missing = stems.size() - g.size();
			iter = sortedStems.iterator();
			for (int i = 0; i < missing; i++)
				stem = iter.next();
			parts = stems.get(iter.next()).split(",");
			subject = parts[0].equals("")
							? null
							: parts[0].startsWith("<") || parts[0].startsWith("http://")
								? Node.createURI(parts[0])
								: Node.createLiteral(parts[0]);
			predicate = parts[1].equals("")
							? null
							: parts[1].startsWith("<") || parts[1].startsWith("http://")
								? Node.createURI(parts[1])
								: Node.createLiteral(parts[1]);
			object = parts.length < 3 || parts[2].equals("")
							? null
							: parts[2].startsWith("<") || parts[2].startsWith("http://")
								? Node.createURI(parts[2])
								: Node.createLiteral(parts[2]);
			Values vals = new Values();
			vals.add(Node.createVariable("s"));
			vals.add(Node.createVariable("p"));
			vals.add(g.find(subject,predicate,object).next().getSubject());
			vals.add(Action.probe);
			vals.add(isAdd);
			vals.add(g);
			vals.add(timestamp);
			logger.debug(String.format("\nRouting triple: %s %s %s\nto SteM: %s",
									   vals.get(0),
									   vals.get(1),
									   vals.get(2),
									   stem));
			this.collector.emit(stem, anchor, vals);
			this.collector.ack(anchor);
			return;
		}else{
			System.out.println(g.toString()+" Completed at: "+timestamp+"\nTook "+(new Date().getTime() - timestamp)+" milliseconds.");
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
