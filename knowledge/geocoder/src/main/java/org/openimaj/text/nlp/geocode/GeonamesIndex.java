/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.text.nlp.geocode;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), Jonathan Hare
 *         (jsh2@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 * 
 */
public class GeonamesIndex {
	private static final int N_ROWS = 10;

	private static final String[] DEFAULT_FIELDS = new String[] { "*,score" };

	/** Logging */
	private static Logger log = LoggerFactory.getLogger(GeonamesIndex.class);

	/** Solr file names */
	private static String SOLR_CONFIG = "solrconfig.xml";
	private static String SOLR_SCHEMA = "schema.xml";

	/** Solr index */
	private CoreContainer solrContainer;
	private EmbeddedSolrServer solrIndex;

	private static GeonamesIndex instance;

	private GeonamesIndex() {
		// Find the Solr home
		String solrHome = System.getProperty("geonames.solr.home");
		if (solrHome == null) {
			log.error("No 'geonames.solr.home' provided!");
			return;
		}
		// Validate on a basic level
		File solrDir = new File(solrHome);
		if (solrDir == null || !solrDir.exists() || !solrDir.isDirectory()) {
			log.error("SOLR_HOME does not exist, or is not a directory: '{}'",solrHome);
			return;
		}
		try {

			this.solrIndex = buildSolrIndex(solrHome);
		} catch (Exception ex) {
			log.error("\n... Solr failed to load!");
			log.error("Stack trace: ", ex);
			log.error("\n=============");
		}
	}

	/**
	 * @return get the {@link GeonamesIndex} instance
	 */
	public static synchronized GeonamesIndex instance() {
		if (instance == null) {
			instance = new GeonamesIndex();
		}
		return instance;
	}

	private EmbeddedSolrServer buildSolrIndex(String home) throws ParserConfigurationException, IOException, SAXException {
		SolrConfig solrConfig = new SolrConfig(home, SOLR_CONFIG, null);
		IndexSchema schema = new IndexSchema(solrConfig, SOLR_SCHEMA, null);

		solrContainer = new CoreContainer(new SolrResourceLoader(
				SolrResourceLoader.locateSolrHome()));
		CoreDescriptor descriptor = new CoreDescriptor(solrContainer, "",
				solrConfig.getResourceLoader().getInstanceDir());
		descriptor.setConfigName(solrConfig.getResourceName());
		descriptor.setSchemaName(schema.getResourceName());
		SolrCore solrCore = new SolrCore(null, solrConfig.getDataDir(),solrConfig, schema, descriptor);
		
		solrContainer.register("cheese", solrCore, false);
		return new EmbeddedSolrServer(solrContainer, "cheese");
	}

	/**
	 * @param query
	 * @return The response
	 * @throws SolrServerException 
	 */
	public QueryResponse query(String query) throws SolrServerException {
		return query(query, N_ROWS, DEFAULT_FIELDS, null);
	}
	
	/**
	 * @param query
	 * @param limit 
	 * @return The response
	 * @throws SolrServerException 
	 */
	public QueryResponse query(String query, int limit) throws SolrServerException {
		return query(query, limit, DEFAULT_FIELDS, null);
	}

	/**
	 * @param query
	 * @param nRows
	 * @param fields
	 * @param filter
	 * @return the response
	 * @throws SolrServerException 
	 */
	public QueryResponse query(String query, int nRows, String[] fields,String filter) throws SolrServerException {
		SolrQuery q = new SolrQuery();
		q.setQuery(query);
		q.setRows(nRows);
		q.setFields(fields);
		if (filter != null) {
			q.setFilterQueries(filter);
		}
		q.setFacet(false);
		return solrIndex.query(q);
	}
}
