package org.openimaj.kestrel.writing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.RiotWriter;

import backtype.storm.tuple.Fields;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NTripleWritingScheme implements WritingScheme{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2734506908903229738L;
//	private KryoValuesSerializer serializer;
//	private KryoValuesDeserializer deserializer;
	
	@Override
	public byte[] serialize(List<Object> objects) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Graph graph = GraphFactory.createGraphMem();
		for (Object object : objects) {
			Triple triple = (Triple)object;
			graph.add(triple);
		}
		RiotWriter.writeTriples(os, graph);
		try {
			os.flush();
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] ret = os.toByteArray();
		
		return ret;
	}
	@Override
	public List<Object> deserialize(byte[] ser) {
		ByteArrayInputStream bais = new ByteArrayInputStream(ser);
		final Object triples = new ArrayList<Object>();
		RiotReader.createParserNTriples(bais, new Sink<Triple>() {
			
			@Override
			public void close() {
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void send(Triple item) {
				((List<Object>)triples).add(item);
			}
			
			@Override
			public void flush() {
			}
		}).parse();
		return Arrays.asList(triples);
	}
	@Override
	public Fields getOutputFields() {
		return new Fields("triples");
	}
}
