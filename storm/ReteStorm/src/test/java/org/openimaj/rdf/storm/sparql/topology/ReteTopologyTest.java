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
package org.openimaj.rdf.storm.sparql.topology;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.rdf.storm.sparql.topology.builder.group.NTriplesSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;

import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;
import eu.larkc.csparql.streams.formats.TranslationException;

/**
 * Test the {@link StormTopology} construction from a CSPARQL query
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ReteTopologyTest {

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
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
				new NTriplesSPARQLReteTopologyBuilder(),
				ReteTopologyBuilder.class.getResourceAsStream(sparqlSource)
				);
		final LocalCluster cluster = new LocalCluster();
		System.out.println(orchestrator);
		cluster.submitTopology("reteTopology", orchestrator.getConfiguration(), orchestrator.buildTopology());
		Utils.sleep(10000);
		cluster.killTopology("reteTopology");
		cluster.shutdown();
	}
}
