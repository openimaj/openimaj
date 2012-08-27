package org.openimaj.rdf.storm.utils;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_NULL;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

import backtype.storm.Config;

/**
 * A collections to tools for letting Jena play nicely with Storm
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaStromUtils {
	
	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class NodeSerialiser_URI extends Serializer<Node_URI>{

		@Override
		public void write(Kryo kryo, Output output, Node_URI object) {
			output.writeString(object.getURI());
		}

		@Override
		public Node_URI read(Kryo kryo, Input input, Class<Node_URI> type) {
			return (Node_URI) Node.createURI(input.readString());
		}
		
	}
	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class NodeSerialiser_Literal extends Serializer<Node_Literal>{

		@Override
		public void write(Kryo kryo, Output output, Node_Literal object) {
			LiteralLabel label = object.getLiteral();
			output.writeString(label.getLexicalForm());
			output.writeString(label.language());
			output.writeString(label.getDatatypeURI());
		}

		@Override
		public Node_Literal read(Kryo kryo, Input input, Class<Node_Literal> type) {
			String lexicalForm = input.readString();
			String langauge = input.readString();
			String datatypeURI = input.readString();
			RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
			return (Node_Literal) Node.createLiteral(lexicalForm, langauge, dtype);
			
		}
		
	}
	
	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class TripleSerialiser extends Serializer<Triple>{

		@Override
		public void write(Kryo kryo, Output output, Triple object) {
			Node s = object.getSubject();
			Node p = object.getPredicate();
			Node o = object.getObject();
			kryo.writeClassAndObject(output, s);
			kryo.writeClassAndObject(output, p);
			kryo.writeClassAndObject(output, o);
		}

		@Override
		public Triple read(Kryo kryo, Input input, Class<Triple> type) {
			Node s = (Node) kryo.readClassAndObject(input);
			Node p = (Node) kryo.readClassAndObject(input);
			Node o = (Node) kryo.readClassAndObject(input);
			return new Triple(s, p, o);
		}
		
	}
	
	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class NodeSerialiser_ARRAY extends Serializer<Node[]>{

		@Override
		public void write(Kryo kryo, Output output, Node[] object) {
			output.writeInt(object.length);
			for (Node node : object) {
				kryo.writeClassAndObject(output, node);
			}
		}

		@Override
		public Node[] read(Kryo kryo, Input input, Class<Node[]> type) {
			Node[] out = new Node[input.readInt()];
			for (int i = 0; i < out.length; i++) {
				out[i] = (Node) kryo.readClassAndObject(input);
			}
			return out;
		}
		
	}
	/**
	 * @param conf register some Jena serialisers to this configuration
	 */
	public static void registerSerializers(Config conf) {
		conf.registerSerialization(Node[].class, NodeSerialiser_ARRAY.class);
		conf.registerSerialization(Node_URI.class, NodeSerialiser_URI.class);
		conf.registerSerialization(Node_Literal.class, NodeSerialiser_Literal.class);
		conf.registerSerialization(Triple.class, TripleSerialiser.class);
		conf.registerSerialization(ArrayList.class);
//		conf.registerSerialization(Node_NULL.class);
//		conf.registerSerialization(Node_Blank.class);
	}

}
