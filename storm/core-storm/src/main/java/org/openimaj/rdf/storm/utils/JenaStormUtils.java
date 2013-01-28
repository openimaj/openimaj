/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.util.pair.IndependentPair;

import backtype.storm.Config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * A collections to tools for letting Jena play nicely with Storm
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
 *         (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 * 
 */
public class JenaStormUtils {

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_URI extends Serializer<Node_URI> {

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
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TemplateSerialiser extends Serializer<Template> {

		@Override
		public void write(Kryo kryo, Output output, Template object) {
			BasicPattern bgp = object.getBGP();
			output.writeInt(bgp.size());
			for (Triple triple : bgp) {
				kryo.writeClassAndObject(output, triple);
			}
		}

		@Override
		public Template read(Kryo kryo, Input input, Class<Template> type) {
			BasicPattern bgp = new BasicPattern();
			int count = input.readInt();
			for (int i = 0; i < count; i++) {
				bgp.add((Triple) kryo.readClassAndObject(input));
			}
			return new Template(bgp);
		}

	}

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_Literal extends Serializer<Node_Literal> {

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
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_Blank extends Serializer<Node_Blank> {

		@Override
		public void write(Kryo kryo, Output output, Node_Blank object) {
			String blankNodeString = object.toString();
			output.writeString(blankNodeString);
		}

		@Override
		public Node_Blank read(Kryo kryo, Input input, Class<Node_Blank> type) {
			String label = input.readString();
			Node_Blank retNode = (Node_Blank) Node.createAnon(AnonId.create(label));
			return retNode;
		}

	}
	
	/**
	 * @author David Monks<dm11g08@ecs.soton.ac.uk>
	 * 
	 */
	public static class NodeSerialiser_Variable extends Serializer<Node_Variable> {

		@Override
		public void write(Kryo kryo, Output output, Node_Variable object) {
			String blankNodeString = object.toString();
			output.writeString(blankNodeString);
		}

		@Override
		public Node_Variable read(Kryo kryo, Input input, Class<Node_Variable> type) {
			String label = input.readString();
			Node_Variable retNode = (Node_Variable) Node.createVariable(label.replaceFirst("\\?", ""));
			return retNode;
		}

	}

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TripleSerialiser extends Serializer<Triple> {

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
	 * 
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static class GraphSerialiser extends Serializer<Graph> {

		@Override
		public void write(Kryo kryo, Output output, Graph object) {
			output.writeInt(object.size());
			Iterator<Triple> it = object.find(null, null, null);
			while (it.hasNext()) {
				Triple next = it.next();
				kryo.writeClassAndObject(output, next);
			}
		}

		@Override
		public Graph read(Kryo kryo, Input input, Class<Graph> type) {
			int size = input.readInt();
			Graph graph = null;
			graph = new GraphMem();
			List<Triple> overflow = new ArrayList<Triple>();
			for (int i = 0; i < size; i++) {
				Object obj = kryo.readClassAndObject(input);
				try {
					graph.add((Triple) obj);
				} catch (AddDeniedException ex) {
					overflow.add((Triple) obj);
				}
			}
			Iterator<Triple> it = overflow.iterator();
			while (!overflow.isEmpty()) {
				if (!it.hasNext())
					it = overflow.iterator();
				try {
					graph.add(it.next());
					it.remove();
				} catch (AddDeniedException ex) {
				}
			}
			return graph;
		}

	}

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_ARRAY extends Serializer<Node[]> {

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
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class KestrelServerSpec_Serializer extends Serializer<KestrelServerSpec> {

		@Override
		public void write(Kryo kryo, Output output, KestrelServerSpec object) {
			output.writeString(object.host);
			output.writeInt(object.port);
		}

		@Override
		public KestrelServerSpec read(Kryo kryo, Input input, Class<KestrelServerSpec> type) {
			return new KestrelServerSpec(input.readString(), input.readInt());
		}

	}

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class RuleSerializer extends Serializer<Rule> {

		@Override
		public void write(Kryo kryo, Output output, Rule object) {
			output.writeString(object.toString());
		}

		@Override
		public Rule read(Kryo kryo, Input input, Class<Rule> type) {
			return Rule.parseRule(input.readString());
		}

	}

	/**
	 * @param conf
	 *            register some Jena serialisers to this configuration
	 */
	public static void registerSerializers(Config conf) {
		conf.registerSerialization(Node[].class, NodeSerialiser_ARRAY.class);
		conf.registerSerialization(Node_URI.class, NodeSerialiser_URI.class);
		conf.registerSerialization(Node_Literal.class, NodeSerialiser_Literal.class);
		conf.registerSerialization(Node_Blank.class, NodeSerialiser_Blank.class);
		conf.registerSerialization(Node_Variable.class, NodeSerialiser_Variable.class);
		conf.registerSerialization(Triple.class, TripleSerialiser.class);
		conf.registerSerialization(ArrayList.class);
		conf.registerSerialization(KestrelServerSpec.class, KestrelServerSpec_Serializer.class);
		conf.registerSerialization(Rule.class, RuleSerializer.class);
		conf.registerSerialization(Graph.class, GraphSerialiser.class);
		conf.registerSerialization(GraphMem.class, GraphSerialiser.class);
		conf.registerSerialization(MultiUnion.class, GraphSerialiser.class);
		conf.registerSerialization(Template.class, TemplateSerialiser.class);
		conf.registerSerialization(ElementFilter.class);
		// conf.registerSerialization(Node_NULL.class);
		// conf.registerSerialization(Node_Blank.class);
	}

	private static List<IndependentPair<Class<?>, Class<? extends Serializer<?>>>> initSerializers() {
		return new ArrayList<IndependentPair<Class<?>, Class<? extends Serializer<?>>>>();
	}
}
