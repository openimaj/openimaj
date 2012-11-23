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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.QuerySolutionSerializer;
import org.openimaj.rdf.storm.sparql.topology.builder.group.StaticDataFileNTriplesSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;

import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.ResultBinding;
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
public class ReteTopologyTest {
	private static final String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX ex: <http://example.com/> PREFIX xs: <http://www.w3.org/2001/XMLSchema#>";
	private static long TOPOLOGY_SLEEP_TIME = 3000;
	/**
	 *
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * prepare the output
	 *
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {

	}


	private File fileFromStream(InputStream stream) throws IOException {
		File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
		PrintWriter writer = new PrintWriter(f,"UTF-8");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		String line = null;
		while((line = reader.readLine()) != null){
			writer.println(line);
		}
		writer.flush(); writer.close();
		return f;
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
		performQuery(sparqlSource,"/test.rdfs", expectedValues);

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
		performQuery(sparqlSource, "/test.union.rdfs",expectedValues);
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
		performQuery(sparqlSource, "/test.multiple.rdfs",expectedValues);
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
		performQuery(sparqlSource, "/test.rdfs",expectedValues);
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
		performQuery(sparqlSource, "/test.aggregate.rdfs",expectedValues);
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
		e.add(Var.alloc("sumprice"), NodeFactory.intToNode(25));
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
		performQuery(sparqlSource, "/test.aggregate.rdfs",expectedValues);
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
		performQuery(sparqlSource, "/test.groupby.rdfs",expectedValues);
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
		performQuery(sparqlSource,"/osn_posts.nt", expectedValues, "file://" + staticData.getAbsolutePath());
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
		File staticData = fileFromStream(ReteTopologyBuilder.class.getResourceAsStream("/osn_users.nt"));
		performQuery(sparqlSource, "/osn_posts.nt", expectedValues, "file://" + staticData.getAbsolutePath());
	}

	class CheckableQuery {
		public CheckableQuery(String query, String variable, String... values) {
			this.query = query;
			this.variable = variable;
			this.values = values;
		}

		String query;
		String variable;
		String[] values;
	}

	private boolean checkBinding(ResultSet results, Var var, String... strings) {
		List<Binding> bindings = new ArrayList<Binding>();
		while (results.hasNext()) {
			bindings.add(results.nextBinding());
		}
		if (bindings.size() != strings.length)
		{
			System.out.println("The bindings length did not match!");
			return false;
		}
		Set<String> allowed = Sets.newHashSet(strings);
		for (Binding binding : bindings) {
			System.out.println("Testing binding: " + binding);
			boolean found = false;
			Node bound = binding.get(var);
			for (String string : allowed) {
				if (bound.toString().equals(string))
					found = true;
			}
			if (!found) {
				System.out.println("The requested binding was not found!");
				return false;
			}
		}
		return true;
	}

	private ResultSet executeQuery(String querystring, Model model) {
		Query query = QueryFactory.create(querystring);
		System.out.println("Running query: " + query);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet select = qe.execSelect();
		return select;
	}

	private void performQuery(String sparqlSource, String testFile, List<?> expectedValues, String... staticSources) throws TranslationException, IOException {
		File f = folder.newFile("STORM");
		f.delete();
		f.mkdirs();
		Map<String, String> replaceStringMap = new HashMap<String,String>();
		File file = fileFromStream(ReteTopologyTest.class.getResourceAsStream(testFile));
		replaceStringMap.put("TEST_FILE", "file://" + file.getAbsolutePath());
		StaticDataFileNTriplesSPARQLReteTopologyBuilder topologyBuilder = new StaticDataFileNTriplesSPARQLReteTopologyBuilder(f, staticSources);
		topologyBuilder.setQuerySolutionSerializerMode(QuerySolutionSerializer.RDF_NTRIPLES);
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
				topologyBuilder,
				ReteTopologyBuilder.class.getResourceAsStream(sparqlSource),
				replaceStringMap
				);
		final LocalCluster cluster = new LocalCluster();
		System.out.println(orchestrator);
		cluster.submitTopology("reteTopology", orchestrator.getConfiguration(), orchestrator.buildTopology());
		Utils.sleep(TOPOLOGY_SLEEP_TIME);
		cluster.killTopology("reteTopology");
		cluster.shutdown();
		Query simpleQuery = orchestrator.getQuery().simpleQuery;
		if (simpleQuery.isSelectType()) {
			List<Binding> sols = readAllResults(topologyBuilder);
			assertTrue(sols.containsAll(expectedValues));
			assertTrue(expectedValues.containsAll(sols));
		} else if (simpleQuery.isConstructType()) {
			Model sols = readAllTriples(topologyBuilder);
			@SuppressWarnings("unchecked")
			List<CheckableQuery> checkQ = (List<CheckableQuery>) expectedValues;
			for (CheckableQuery checkableQuery : checkQ) {

				ResultSet results = executeQuery(checkableQuery.query, sols);
				assertTrue(checkBinding(results, Var.alloc(checkableQuery.variable), checkableQuery.values));
			}

		}
	}

	private Model readAllTriples(StaticDataFileNTriplesSPARQLReteTopologyBuilder topologyBuilder) {
		File files = topologyBuilder.getOutputFile();
		System.out.println("Reading results form: " + files);
		Polyadic retModel = new MultiUnion();
		for (File f : files.listFiles()) {
			final Model model = ModelFactory.createDefaultModel();
			model.read(f.toURI().toString(), null, "N-TRIPLES");
			retModel.addGraph(model.getGraph());
		}
		return ModelFactory.createModelForGraph(retModel);

	}

	private List<Binding> readAllResults(StaticDataFileNTriplesSPARQLReteTopologyBuilder topologyBuilder)
			throws FileNotFoundException
	{
		File files = topologyBuilder.getOutputFile();
		System.out.println("Reading results form: " + files);
		List<Binding> sols = new ArrayList<Binding>();
		for (File f : files.listFiles()) {
			final Model model = ModelFactory.createDefaultModel();
			model.read(f.toURI().toString(), null, "N-TRIPLES");
			ResultSet rs = ResultSetFactory.fromRDF(model);
			while (rs.hasNext()) {
				sols.add(((ResultBinding) rs.next()).getBinding());
			}
		}
		return sols;
	}
}
