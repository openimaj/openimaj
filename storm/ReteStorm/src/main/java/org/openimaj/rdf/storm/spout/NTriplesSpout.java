package org.openimaj.rdf.storm.spout;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.openimaj.storm.spout.SimpleSpout;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangNTriples;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * Given a URL, This spout creates a stream of triples
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@SuppressWarnings("rawtypes")
public class NTriplesSpout extends SimpleSpout implements Sink<Triple>{

	/**
	 *
	 */
	private static final long serialVersionUID = -110531170333631644L;
	private String nTriplesURL;
	private LangNTriples parser;
	/**
	 * the fields outputted by this spout
	 */
	public static Fields FIELDS = new Fields("subject","predicate","object");
	/**
	 * the field outputted when triples are contained
	 */
	public static final Fields TRIPLES_FIELD = new Fields("triples");

	/**
	 * @param nTriplesURL source of the ntriples
	 *
	 */
	public NTriplesSpout(String nTriplesURL){
		this.nTriplesURL = nTriplesURL;
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		URL url;
		try {
			url = new URL(this.nTriplesURL);
			parser = RiotReader.createParserNTriples(url.openStream(), this);
		} catch (Exception e) {
		}
	}
	@Override
	public void nextTuple() {
		if(parser.hasNext()){
			Triple t = parser.next();
			this.collector.emit(new Values(t));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(TRIPLES_FIELD);
	}
	@Override
	public void close() {
		super.close();
	}

	/**
	 * @return the fields representing the triples outputted
	 */
	public Fields getFields() {
		return TRIPLES_FIELD;
	}

	/**
	 * Given a tuple generated from an {@link NTriplesSpout}, create
	 * a Jena {@link Triple} instance
	 * @param input
	 * @return Jena {@link Triple} instance from the Tuple's fields
	 */
	@SuppressWarnings("unchecked")
	public static Triple asTriple(Tuple input) {
		if(input.getFields().size() == 1){
			return ((List<Triple>)input.getValueByField("triples")).get(0);
		}
		else{			
			Node subject = toJenaNode(input.getValue(0));
			Node predicate = toJenaNode(input.getValue(1));
			Node value = toJenaNode(input.getValue(2));;
			Triple t = new Triple(subject, predicate, value);
			return t;
		}
	}

	private static Node toJenaNode(Object value) {
		return (Node) value; 
	}

	/**
	 * Given a Jena {@link Triple} construct a {@link Values} instance which
	 * is the subject, predicate and value of the triple calling {@link Node#toString()}
	 * @param t
	 * @return a Values instances
	 */
	public static Values asValue(Triple t) {
		return new Values(t.getSubject(),t.getPredicate(),t.getObject());
	}

	@Override
	public void send(Triple item) {
		System.out.println("Sent a triple!");
	}

	@Override
	public void flush() {}


}
