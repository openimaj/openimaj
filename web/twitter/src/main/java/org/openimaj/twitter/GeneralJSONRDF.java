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
package org.openimaj.twitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.io.FileUtils;
import org.openimaj.rdf.utils.PQUtils;
import org.openimaj.twitter.USMFStatus.Link;
import org.openimaj.twitter.USMFStatus.User;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Holds an internal Jena Graph of the USMF status. The default language used is
 * NTriples
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class GeneralJSONRDF extends GeneralJSON {

	enum Variables {
		SERVICE("service"),
		SOCIAL_EVENT("socialEvent"),
		USER("user"),
		PERSON("person"),
		PERSON_NAME("realname"),
		PERSON_LOC("location"),
		PERSON_LAT("lat"),
		PERSON_LONG("long"),
		USER_NAME("username"),
		USER_ID("osnid"),
		USER_LANG("userlanguage"),
		PERSON_LANG("personlanguage"),
		USER_DESC("description"),
		USER_AVATAR("useravatar"),
		USER_SITE("website"),
		USER_PROF("profile"),
		USER_FOLLOWERS("subscribers"),
		USER_FOLLOWING("subscriptions"),
		SOURCE_URL("sourceURL"),
		TEXT("text"),
		DESC("description"),
		CAT("category"),
		FAV("favourites"),
		USER_POSTS("postings"), LINK("link"), KEYWORD("keyword"), ;
		public String name;

		private Variables(String name) {
			this.name = name;
		}

	}

	// private static final String ITEM_QUERY_FILE =
	// "/org/openimaj/twitter/rdf/usmf_query.sparql";
	private static final String INSERT_ITEM_QUERY_FILE = "/org/openimaj/twitter/rdf/insert_usmf_query.sparql";
	private static final String DELETE_TM_NULL = "/org/openimaj/twitter/rdf/delete_null.sparql";
	private static final String LINK_INSERT_QUERY_FILE = "/org/openimaj/twitter/rdf/insert_usmf_links_query.sparql";
	private static final String KEYWORDS_INSERT_QUERY_FILE = "/org/openimaj/twitter/rdf/insert_usmf_keywords_query.sparql";
	private static final String TOUSERS_INSERT_QUERY_FILE = "/org/openimaj/twitter/rdf/insert_usmf_touser_query.sparql";;
	private static Map<String, String> queryCache;

	static {
		SysRIOT.wireIntoJena();
	}

	private Model m;
	private Resource eventIRI;
	private static String baseModelString;
	static {
		try {
			baseModelString = FileUtils.readall(GeneralJSONRDF.class.getResourceAsStream("rdf/base_usmf.rdf"));
			System.out.println("Read in base model!");
		} catch (final IOException e) {

		}
	}
	private static final Map<String, RDFAnalysisProvider> providers = new HashMap<String, RDFAnalysisProvider>();

	/**
	 * Registers an analysis provider to be used when some analysis key is met
	 * 
	 * @param analysis
	 * @param analysisProvider
	 */
	public static void registerRDFAnalysisProvider(String analysis, RDFAnalysisProvider analysisProvider) {
		analysisProvider.init();
		providers.put(analysis, analysisProvider);
	}

	@Override
	public void readASCII(final Scanner in) throws IOException {
		final StringBuffer b = new StringBuffer();
		while (in.hasNext()) {
			b.append(in.next());
		}
		final InputStream stream = new ByteArrayInputStream(b.toString().getBytes("UTF-8"));
		m = ModelFactory.createDefaultModel();
		m.read(stream, "", "NTRIPLES");
		m.close();
	}

	@Override
	public void fillUSMF(USMFStatus status) {
		throw new UnsupportedOperationException("Not supported yet");
	}

	private static String queryCache(String queryFile) {
		if (queryCache == null) {
			queryCache = new HashMap<String, String>();
		}
		String q = queryCache.get(queryFile);
		if (q == null) {
			queryCache.put(queryFile, q = readQuery(queryFile));
		}
		return q;
	}

	private static String readQuery(String qf) {
		try {
			return FileUtils.readall(GeneralJSONRDF.class.getResourceAsStream(qf));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void fromUSMF(USMFStatus status) {
		prepareModel();
		// m.add(
		// ResourceFactory.createResource("dc:wangSub"),
		// ResourceFactory.createProperty("dc:wangPre"),
		// "wangObj"
		// );
		ParameterizedSparqlString pss = PQUtils.constructPQ(queryCache(INSERT_ITEM_QUERY_FILE), m);
		this.eventIRI = m.createResource(generateSocialEventIRI(status));
		PQUtils.setPSSResource(pss, Variables.SOCIAL_EVENT.name, eventIRI);
		PQUtils.setPSSLiteral(pss, Variables.SERVICE.name, status.service);
		addUserParameters(pss, status.user, status);
		PQUtils.setPSSLiteral(pss, Variables.SOURCE_URL.name, status.source);
		PQUtils.setPSSLiteral(pss, Variables.TEXT.name, status.text);
		PQUtils.setPSSLiteral(pss, Variables.DESC.name, status.description);
		PQUtils.setPSSLiteral(pss, Variables.CAT.name, status.category);
		PQUtils.setPSSLiteral(pss, Variables.FAV.name, status.favorites);
		UpdateAction.execute(pss.asUpdate(), m);
		pss = PQUtils.constructPQ(readQuery(TOUSERS_INSERT_QUERY_FILE), m);
		// the inreply user

		// the mentioned users
		for (final User key : status.to_users) {
			PQUtils.setPSSResource(pss, Variables.SOCIAL_EVENT.name, eventIRI);
			addUserParameters(pss, key, status);
			UpdateAction.execute(pss.asUpdate(), m);
			pss.clearParams();
		}
		pss = PQUtils.constructPQ(readQuery(LINK_INSERT_QUERY_FILE), m);
		PQUtils.setPSSResource(pss, Variables.SOCIAL_EVENT.name, eventIRI);
		for (final Link link : status.links) {
			PQUtils.setPSSLiteral(pss, Variables.LINK.name, link.href);
			UpdateAction.execute(pss.asUpdate(), m);
		}
		pss = PQUtils.constructPQ(readQuery(KEYWORDS_INSERT_QUERY_FILE), m);
		PQUtils.setPSSResource(pss, Variables.SOCIAL_EVENT.name, eventIRI);
		for (final String key : status.keywords) {
			PQUtils.setPSSLiteral(pss, Variables.KEYWORD.name, key);
			UpdateAction.execute(pss.asUpdate(), m);
		}

		cleanupModel();
		status.fillAnalysis(this);
	}

	private void cleanupModel() {
		final UpdateRequest del = PQUtils.constructPQ(readQuery(DELETE_TM_NULL), m).asUpdate();
		UpdateAction.execute(del, m);
	}

	private void addUserParameters(ParameterizedSparqlString pss, User user, USMFStatus status) {
		PQUtils.setPSSIri(pss, Variables.USER.name, createUserIRI(status, user));
		PQUtils.setPSSIri(pss, Variables.PERSON.name, createPersonIRI(status, user));
		PQUtils.setPSSLiteral(pss, Variables.PERSON_NAME.name, user.real_name);
		PQUtils.setPSSLiteral(pss, Variables.PERSON_LOC.name, user.location);
		PQUtils.setPSSLiteral(pss, new String[] { Variables.PERSON_LAT.name, Variables.PERSON_LONG.name }, user.geo);
		PQUtils.setPSSLiteral(pss, Variables.USER_NAME.name, user.name);
		PQUtils.setPSSLiteral(pss, Variables.USER_ID.name, user.id);
		PQUtils.setPSSLiteral(pss, Variables.USER_LANG.name, user.language);
		PQUtils.setPSSLiteral(pss, Variables.PERSON_LANG.name, user.language);
		PQUtils.setPSSLiteral(pss, Variables.USER_DESC.name, user.description);
		PQUtils.setPSSLiteral(pss, Variables.USER_AVATAR.name, user.avatar);
		PQUtils.setPSSLiteral(pss, Variables.USER_SITE.name, user.website);
		PQUtils.setPSSLiteral(pss, Variables.USER_PROF.name, user.profile);
		PQUtils.setPSSLiteral(pss, Variables.USER_FOLLOWERS.name, user.subscribers);
		PQUtils.setPSSLiteral(pss, Variables.USER_FOLLOWING.name, user.subscriptions);
		PQUtils.setPSSLiteral(pss, Variables.USER_POSTS.name, user.postings);
	}

	@Override
	public void
			writeASCIIAnalysis(PrintWriter outputWriter, List<String> selectiveAnalysis, List<String> selectiveStatus)
	{
		if (selectiveAnalysis == null) {
			selectiveAnalysis = new ArrayList<String>();
			selectiveAnalysis.addAll(this.analysis.keySet());
		}
		for (final String ana : selectiveAnalysis) {
			final RDFAnalysisProvider prov = providers.get(ana);
			if (prov == null)
				continue;
			prov.addAnalysis(m, eventIRI, this);
		}

		// m.write(System.out, "N-TRIPLES");
		m.write(outputWriter, "N-TRIPLES");
	}

	private String createUserIRI(USMFStatus status, User user) {
		return String.format("%s%s/user/%s", m.getNsPrefixURI("tm"), status.service, (long) user.id);
	}

	private String createPersonIRI(USMFStatus status, User user) {
		return String.format("%s%s/person/%s", m.getNsPrefixURI("tm"), status.service, (long) user.id);
	}

	private String generateSocialEventIRI(USMFStatus status) {

		return String.format("%s%s/%s", m.getNsPrefixURI("tm"), status.service, status.id);
	}

	private synchronized void prepareModel() {
		m = ModelFactory.createDefaultModel();
		m.read(new StringReader(baseModelString), "");
		// m.read(GeneralJSONRDF.class.getResourceAsStream("rdf/base_usmf.rdf"),
		// "");
	}

	@Override
	public GeneralJSON instanceFromString(String line) {
		GeneralJSONRDF jsonInstance = null;
		try {
			jsonInstance = new GeneralJSONRDF();
			jsonInstance.m.read(new StringReader(line), "");
		} catch (final Throwable e) {
			throw new RuntimeException(e);
		}
		return jsonInstance;
	}
}
