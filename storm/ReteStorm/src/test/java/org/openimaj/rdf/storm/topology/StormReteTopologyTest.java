/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
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
package org.openimaj.rdf.storm.topology;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.FileUtils;
import org.openimaj.rdf.storm.topology.builder.NTriplesFileOutputStormReteTopologyBuilder;
import org.openjena.riot.SysRIOT;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Test a set of Jena production rules constructed in a distributable
 * {@link StormTopology}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 * 
 */
public class StormReteTopologyTest {

	private static final String PREFIX = "PREFIX rdfs:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX example:<http://example.com/> ";
	/**
	 *
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File output;
	private File input;

	/**
	 * prepare the output
	 * 
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		this.output = folder.newFile("output.ntriples");
		this.input = folder.newFile("input.ntriples");
	}

	/**
	 * Load the nTriples file from /test.rdfs and the rules from /test.rules
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReteTopology() throws IOException {
		InputStream inputStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
		FileUtils.copyStreamToFile(inputStream, this.input);

		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		conf.setFallBackOnJavaSerialization(false);
		conf.setSkipMissingKryoRegistrations(false);
		String inURL = this.input.toURI().toURL().toString();
		String outPath = this.output.getAbsolutePath();
		URL outURL = new File(outPath).toURI().toURL();
		// Only addition by David Monks is to change the TopologyBuilder from
		// NTriplesFileOutputReteTopologyBuilder to NTriplesFileOutputStormReteTopologyBuilder.
		NTriplesFileOutputStormReteTopologyBuilder builder = new NTriplesFileOutputStormReteTopologyBuilder(inURL, outPath);
		InputStream ruleStream = ReteTopologyTest.class.getResourceAsStream("/test.rules");
		StormTopology topology = RuleReteStormTopologyFactory.buildTopology(conf, builder, ruleStream);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("reteTopology", conf, topology);

		Utils.sleep(5000);
		SysRIOT.wireIntoJena();
		final Model model = ModelFactory.createDefaultModel();
		model.read(outURL.toString(), null, "N-TRIPLES");

		// Both steve and john are drivers and thus human beings
		ResultSet results = executeQuery(PREFIX + "SELECT ?person WHERE {?person rdfs:type example:HumanBeing.}", model);
		assertTrue(checkBinding(results, Var.alloc("person"), "http://example.com/John", "http://example.com/Steve"));

		// steve has been in 11 accidents and is thus a dangerous driver,
		// john is not dangerous
		results = executeQuery(PREFIX + "SELECT ?person WHERE {?person rdfs:type example:DangerousDriver.}", model);
		assertTrue(checkBinding(results, Var.alloc("person"), "http://example.com/Steve"));

		// john is an eligable driver, having no accidents, a certificate and
		// being a driver
		results = executeQuery(PREFIX + "SELECT ?person WHERE {?person rdfs:type example:EligibleDriver.}", model);
		assertTrue(checkBinding(results, Var.alloc("person"), "http://example.com/John"));

		cluster.killTopology("reteTopology");
		cluster.shutdown();
	}

	private boolean checkBinding(ResultSet results, Var var, String... strings) {
		List<Binding> bindings = new ArrayList<Binding>();
		while (results.hasNext()) {
			bindings.add(results.nextBinding());
		}
		if (bindings.size() != strings.length)
			return false;
		Set<String> allowed = Sets.newHashSet(strings);
		for (Binding binding : bindings) {
			boolean found = false;
			Node bound = binding.get(var);
			for (String string : allowed) {
				if (bound.toString().equals(string))
					found = true;
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	private ResultSet executeQuery(String querystring, Model model) {
		Query query = QueryFactory.create(querystring);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet select = qe.execSelect();
		return select;
	}
}
