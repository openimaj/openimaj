package org.openimaj.rdf.storm.spout;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.openimaj.storm.spout.SimpleSpout;
import org.semanticweb.yars.nx.parser.NxParser;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

/**
 * Given a URL, This spout creates a stream of triples
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@SuppressWarnings("rawtypes")
public class NTriplesSpout extends SimpleSpout{

	/**
	 *
	 */
	private static final long serialVersionUID = -110531170333631644L;
	private String nTriplesURL;
	private NxParser nxp;
	private InputStream stream;
	/**
	 * the fields outputted by this spout
	 */
	public static Fields FIELDS = new Fields("subject","predicate","object");

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
			stream = url.openStream();
			nxp = new NxParser(stream);
		} catch (Exception e) {
		}
	}
	@Override
	public void nextTuple() {
		if(nxp.hasNext()){
			Object[] ns = nxp.next();
			this.collector.emit(new Values(ns));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(FIELDS);
	}
	@Override
	public void close() {
		super.close();
		try {
			stream.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @return the fields representing the triples outputted
	 */
	public Fields getFields() {
		return FIELDS;
	}

	/**
	 * Given a tuple generated from an {@link NTriplesSpout}, create
	 * a Jena {@link Triple} instance
	 * @param input
	 * @return Jena {@link Triple} instance from the Tuple's fields
	 */
	public static Triple asTriple(Tuple input) {
		Node subject = toJenaNode(input.getValue(0));
		Node predicate = toJenaNode(input.getValue(1));
		Node value = toJenaNode(input.getValue(2));;
		Triple t = new Triple(subject, predicate, value);
		return t;
	}

	private static Node toJenaNode(Object value) {
		return NodeCreateUtils.create(value.toString());
	}

	/**
	 * Given a Jena {@link Triple} construct a {@link Values} instance which
	 * is the subject, predicate and value of the triple calling {@link Node#toString()}
	 * @param t
	 * @return a Values instances
	 */
	public static Values asValue(Triple t) {
		return new Values(t.getSubject().toString(),t.getPredicate().toString(),t.getObject().toString());
	}


}
