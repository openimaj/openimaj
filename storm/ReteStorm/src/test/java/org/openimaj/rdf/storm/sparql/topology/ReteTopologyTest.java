/**
\ * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.rdf.storm.sparql.topology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;

import backtype.storm.generated.StormTopology;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.util.NodeFactory;

import eu.larkc.csparql.streams.formats.TranslationException;

/**
 * Test the {@link StormTopology} construction from a CSPARQL query
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ReteTopologyTest extends SPARQLTopologyTest {

	/**
	 * prepare the output
	 * 
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {

	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopology() throws IOException, TranslationException {
		String sparqlSource = "/test.group.csparql";

		List<CheckableQuery> expectedValues = new ArrayList<CheckableQuery>();
		String query = PREFIX + "SELECT ?d WHERE{?d rdf:type ex:EligibleDriver}";
		expectedValues.add(new CheckableQuery(
				query,
				"d",
				"http://example.com/John"
				));
		performQuery(sparqlSource, "/test.rdfs", expectedValues);

	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyUNION() throws IOException, TranslationException {
		String sparqlSource = "/test.union.csparql";
		List<CheckableQuery> expectedValues = new ArrayList<CheckableQuery>();
		String query = PREFIX + "SELECT ?d WHERE{?d rdf:type ex:EligibleDriver}";
		expectedValues.add(new CheckableQuery(
				query,
				"d",
				"http://example.com/John",
				"http://example.com/Steve"
				));
		performQuery(sparqlSource, "/test.union.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyMultiple() throws IOException, TranslationException {
		String sparqlSource = "/test.multiple.csparql";
		List<CheckableQuery> expectedValues = new ArrayList<CheckableQuery>();
		String query = PREFIX + "SELECT ?d WHERE{ex:John rdf:type ?d}";
		expectedValues.add(new CheckableQuery(
				query,
				"d",
				"http://example.com/Steve"
				));
		query = PREFIX + "SELECT ?d WHERE{?d rdf:type ex:Steve}";
		expectedValues.add(new CheckableQuery(
				query,
				"d",
				"http://example.com/Alan",
				"http://example.com/John"
				));
		performQuery(sparqlSource, "/test.multiple.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyOptional() throws IOException, TranslationException {
		String sparqlSource = "/test.optional.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("given"), NodeFactory.createLiteralNode("Bob", "", ""));
		e.add(Var.alloc("family"), NodeFactory.createLiteralNode("Smith", "", ""));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("given"), NodeFactory.createLiteralNode("", "", ""));
		e.add(Var.alloc("family"), NodeFactory.createLiteralNode("", "", ""));
		expectedValues.add(e);
		performQuery(sparqlSource, "/test.optional.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyFilters() throws IOException, TranslationException {
		String sparqlSource = "/test.filter.csparql";
		List<CheckableQuery> expectedValues = new ArrayList<CheckableQuery>();
		String query = PREFIX + "SELECT ?d WHERE{?d rdf:type ex:GoodDriver}";
		expectedValues.add(new CheckableQuery(
				query,
				"d",
				"http://example.com/John"
				));
		performQuery(sparqlSource, "/test.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyAggregate() throws IOException, TranslationException {
		String sparqlSource = "/test.aggregate.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("org"), NodeFactory.parseNode("<http://books.example/org1>"));
		e.add(Var.alloc("totalPrice"), NodeFactory.intToNode(18));
		e.add(Var.alloc("avgPrice"), NodeFactory.createLiteralNode("12", "", "http://www.w3.org/2001/XMLSchema#decimal"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("org"), NodeFactory.parseNode("<http://books.example/org1>"));
		e.add(Var.alloc("totalPrice"), NodeFactory.intToNode(12));
		e.add(Var.alloc("avgPrice"), NodeFactory.createLiteralNode("12", "", "http://www.w3.org/2001/XMLSchema#decimal"));
		expectedValues.add(e);
		performQuery(sparqlSource, "/test.aggregate.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyAggregateCountStar() throws IOException, TranslationException {
		String sparqlSource = "/test.aggregate.countstar.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("sumprice"), NodeFactory.intToNode(24));
		e.add(Var.alloc("count"), NodeFactory.intToNode(4));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("sumprice"), NodeFactory.intToNode(18));
		e.add(Var.alloc("count"), NodeFactory.intToNode(3));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("sumprice"), NodeFactory.intToNode(12));
		e.add(Var.alloc("count"), NodeFactory.intToNode(2));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("sumprice"), NodeFactory.intToNode(6));
		e.add(Var.alloc("count"), NodeFactory.intToNode(1));
		expectedValues.add(e);
		performQuery(sparqlSource, "/test.aggregate.countstar.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyBindGroupBy() throws IOException, TranslationException {
		String sparqlSource = "/test.groupby.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		expectedValues.add(BindingFactory.binding(Var.alloc("p"), NodeFactory.intToNode(42)));
		expectedValues.add(BindingFactory.binding(Var.alloc("p"), NodeFactory.intToNode(23)));
		performQuery(sparqlSource, "/test.groupby.rdfs", expectedValues);
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyStaticData() throws IOException, TranslationException {
		String sparqlSource = "/test.userpost.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("postcontent"), NodeFactory.createLiteralNode("out on January 5 2007 by the Chinese police against a suspected East Turkestan Islamic Movement training", "", ""));
		e.add(Var.alloc("user1"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u941>"));
		e.add(Var.alloc("createDate"), NodeFactory.parseNode("\"2010-02-01T11:49:51Z\"^^xsd:dateTime"));
		e.add(Var.alloc("friend"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u627>"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("postcontent"), NodeFactory.createLiteralNode("Christine Cris Bonacci is an Australian-born producer songwriter and musician", "", ""));
		e.add(Var.alloc("user1"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u941>"));
		e.add(Var.alloc("createDate"), NodeFactory.parseNode("\"2010-02-01T10:25:05Z\"^^xsd:dateTime"));
		e.add(Var.alloc("friend"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u59>"));
		expectedValues.add(e);
		File staticData = fileFromStream(ReteTopologyBuilder.class.getResourceAsStream("/osn_users.nt"));
		performQuery(sparqlSource, "/osn_posts.nt", expectedValues, "file://" + staticData.getAbsolutePath());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologySubquery() throws IOException, TranslationException {
		String sparqlSource = "/test.userpost.subquery.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("postcontent"), NodeFactory.createLiteralNode("out on January 5 2007 by the Chinese police against a suspected East Turkestan Islamic Movement training", "", ""));
		e.add(Var.alloc("user1"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u941>"));
		e.add(Var.alloc("createDate"), NodeFactory.parseNode("\"2010-02-01T11:49:51Z\"^^xsd:dateTime"));
		e.add(Var.alloc("friend"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u627>"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("postcontent"), NodeFactory.createLiteralNode("Christine Cris Bonacci is an Australian-born producer songwriter and musician", "", ""));
		e.add(Var.alloc("user1"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u941>"));
		e.add(Var.alloc("createDate"), NodeFactory.parseNode("\"2010-02-01T10:25:05Z\"^^xsd:dateTime"));
		e.add(Var.alloc("friend"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u59>"));
		expectedValues.add(e);
		File staticData = fileFromStream(ReteTopologyBuilder.class.getResourceAsStream("/osn_users.nt"));
		performQuery(sparqlSource, "/osn_posts.nt", expectedValues, "file://" + staticData.getAbsolutePath());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologySubquery_complex() throws IOException, TranslationException {
		String sparqlSource = "/test.userpost.subquery.complex.csparql";
		List<Binding> expectedValues = new ArrayList<Binding>();
		BindingMap e = new BindingHashMap();
		e.add(Var.alloc("postcontent"), NodeFactory.createLiteralNode("out on January 5 2007 by the Chinese police against a suspected East Turkestan Islamic Movement training", "", ""));
		e.add(Var.alloc("user1"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u941>"));
		e.add(Var.alloc("createDate"), NodeFactory.parseNode("\"2010-02-01T11:49:51Z\"^^xsd:dateTime"));
		e.add(Var.alloc("friend"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u627>"));
		expectedValues.add(e);
		e = new BindingHashMap();
		e.add(Var.alloc("postcontent"), NodeFactory.createLiteralNode("Christine Cris Bonacci is an Australian-born producer songwriter and musician", "", ""));
		e.add(Var.alloc("user1"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u941>"));
		e.add(Var.alloc("createDate"), NodeFactory.parseNode("\"2010-02-01T10:25:05Z\"^^xsd:dateTime"));
		e.add(Var.alloc("friend"), NodeFactory.parseNode("<http://www.ins.cwi.nl/sib/user/u59>"));
		expectedValues.add(e);
		File staticData = fileFromStream(ReteTopologyBuilder.class.getResourceAsStream("/osn_users.nt"));
		performQuery(sparqlSource, "/osn_posts.nt", expectedValues, "file://" + staticData.getAbsolutePath());
	}
}
