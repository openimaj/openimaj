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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink.FileSink.QuerySolutionSerializer;
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
	private static long TOPOLOGY_SLEEP_TIME = 2000;
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
		performQuery(sparqlSource, expectedValues);

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
		performQuery(sparqlSource, expectedValues);
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
		performQuery(sparqlSource, expectedValues);
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
		BindingMap e = new BindingMap();
		e.add(Var.alloc("given"), NodeFactory.createLiteralNode("Bob", "", ""));
		e.add(Var.alloc("family"), NodeFactory.createLiteralNode("Smith", "", ""));
		expectedValues.add(e);
		expectedValues.add(new BindingMap()); // and an empty binding!
		performQuery(sparqlSource, expectedValues);
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
		performQuery(sparqlSource, expectedValues);
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
		BindingMap e = new BindingMap();
		e.add(Var.alloc("given"), NodeFactory.createLiteralNode("Bob", "", ""));
		e.add(Var.alloc("family"), NodeFactory.createLiteralNode("Smith", "", ""));
		expectedValues.add(e);
		expectedValues.add(new BindingMap()); // and an empty binding!
		performQuery(sparqlSource, expectedValues);
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
		performQuery(sparqlSource, expectedValues);
	}

	/**
	 *
	 * @throws IOException
	 * @throws TranslationException
	 */
	@Test
	public void testReteTopologyStaticData() throws IOException, TranslationException {
		String sparqlSource = "/test.userpost.csparql";
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator
				.createTopologyBuilder(
						new StaticDataFileNTriplesSPARQLReteTopologyBuilder(
								"file:///Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/osn_users.nt"),
						ReteTopologyBuilder.class.getResourceAsStream(sparqlSource)
				);
		final LocalCluster cluster = new LocalCluster();
		System.out.println(orchestrator);
		cluster.submitTopology("reteTopology", orchestrator.getConfiguration(), orchestrator.buildTopology());
		Utils.sleep(TOPOLOGY_SLEEP_TIME);
		cluster.killTopology("reteTopology");
		cluster.shutdown();
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

	@SuppressWarnings("unchecked")
	private void performQuery(String sparqlSource, List<?> expectedValues) throws TranslationException, IOException,
			FileNotFoundException
	{
		StaticDataFileNTriplesSPARQLReteTopologyBuilder topologyBuilder = new StaticDataFileNTriplesSPARQLReteTopologyBuilder();
		topologyBuilder.setQuerySolutionSerializerMode(QuerySolutionSerializer.RDF_NTRIPLES);
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
				topologyBuilder,
				ReteTopologyBuilder.class.getResourceAsStream(sparqlSource)
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
