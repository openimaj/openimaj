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

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.QuerySolutionSerializer;
import org.openimaj.rdf.storm.sparql.topology.builder.group.StaticDataFileNTriplesSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;

import backtype.storm.LocalCluster;
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

import eu.larkc.csparql.streams.formats.TranslationException;

public class SPARQLTopologyTest {
	protected static final String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX ex: <http://example.com/> PREFIX xs: <http://www.w3.org/2001/XMLSchema#>";
	private static long TOPOLOGY_SLEEP_TIME = 6000;
	/**
	 *
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	protected class CheckableQuery {
		public CheckableQuery(String query, String variable, String... values) {
			this.query = query;
			this.variable = variable;
			this.values = values;
		}

		String query;
		String variable;
		String[] values;
	}

	protected boolean checkBinding(ResultSet results, Var var, String... strings) {
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

	protected ResultSet executeQuery(String querystring, Model model) {
		Query query = QueryFactory.create(querystring);
		System.out.println("Running query: " + query);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet select = qe.execSelect();
		return select;
	}

	protected void performQuery(String sparqlSource, String testFile, List<?> expectedValues, String... staticSources) throws TranslationException, IOException {
		File f = folder.newFile("STORM");
		f.delete();
		f.mkdirs();
		Map<String, String> replaceStringMap = new HashMap<String, String>();
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
			System.out.println(sols);
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

	protected Model readAllTriples(StaticDataFileNTriplesSPARQLReteTopologyBuilder topologyBuilder) {
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

	protected List<Binding> readAllResults(StaticDataFileNTriplesSPARQLReteTopologyBuilder topologyBuilder)
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

	protected File fileFromStream(InputStream stream) throws IOException {
		File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		writer.close();
		return f;
	}
}
