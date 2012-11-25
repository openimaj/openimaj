package org.openimaj.rdf.storm.sparql.topology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

import eu.larkc.csparql.streams.formats.TranslationException;

public class LSBenchTopologyTest extends SPARQLTopologyTest {

	/**
	 * Query4 with known results
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testQuery4() throws TranslationException, IOException {
		String sparqlSource = "/lsbench/query4.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("post1"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2077"));
		e.add(Var.alloc("post2"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2074"));
		e.add(Var.alloc("tag"), Node.createLiteral("Antonacci"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("post1"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2077"));
		e.add(Var.alloc("post2"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2075"));
		e.add(Var.alloc("tag"), Node.createLiteral("Antonacci"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("post1"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2074"));
		e.add(Var.alloc("post2"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2077"));
		e.add(Var.alloc("tag"), Node.createLiteral("Antonacci"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("post1"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2075"));
		e.add(Var.alloc("post2"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2077"));
		e.add(Var.alloc("tag"), Node.createLiteral("Antonacci"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("post1"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2074"));
		e.add(Var.alloc("post2"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2075"));
		e.add(Var.alloc("tag"), Node.createLiteral("Antonacci"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("post1"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2075"));
		e.add(Var.alloc("post2"), Node.createURI("http://www.ins.cwi.nl/sib/post/po2074"));
		e.add(Var.alloc("tag"), Node.createLiteral("Antonacci"));
		expectedValues.add(e);
		performQuery(sparqlSource, "/lsbench_data/query4_posts.ntriples", expectedValues);
	}
}
