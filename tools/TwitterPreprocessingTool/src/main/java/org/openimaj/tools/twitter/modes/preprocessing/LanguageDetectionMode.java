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
package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.Map;

import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.language.LanguageDetector;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONRDF;
import org.openimaj.twitter.RDFAnalysisProvider;
import org.openimaj.twitter.USMFStatus;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateAction;

/**
 * A gateway class which loads and uses the #LanguageDetector
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LanguageDetectionMode extends TwitterPreprocessingMode<Map<String, Object>> {

	private LanguageDetector detector;
	final static String LANGUAGES = "langid";

	/**
	 * Loads the language detector
	 * 
	 * @throws IOException
	 */
	public LanguageDetectionMode() throws IOException {
		detector = new LanguageDetector();
	}

	@Override
	public Map<String, Object> process(USMFStatus twitterStatus) {
		Map<String, Object> language = null;
		try {
			language = detector.classify(twitterStatus.text).asMap();

		} catch (final Exception e) {
		}
		twitterStatus.addAnalysis(LANGUAGES, language);
		return language;

	}

	@Override
	public RDFAnalysisProvider rdfAnalysisProvider() {
		return new RDFAnalysisProvider() {
			private static final String DETECTED_LANGUAGE_INSERT_SPARQL = "/org/openimaj/tools/twiiter/rdf/detected_language_insert.sparql";
			private String query;

			@Override
			public void addAnalysis(Model m, Resource socialEvent, GeneralJSON analysisSource) {
				final Map<String, Object> analysis = analysisSource.getAnalysis(LANGUAGES);
				if (analysis == null)
					return;

				final ParameterizedSparqlString pss = new ParameterizedSparqlString(query); // wasteful?
																							// makes
																							// it
																							// threadsafe
																							// but
																							// is
																							// it
																							// bad?
				pss.setParam("socialEvent", socialEvent);
				final Resource langNode = m.createResource();
				pss.setParam("langid", langNode);
				pss.setLiteral("language", analysis.get("language").toString());
				pss.setLiteral("confidence", (Double) analysis.get("confidence"));
				UpdateAction.execute(pss.asUpdate(), m);
			}

			@Override
			public void init() {
				try {
					query = FileUtils.readall(GeneralJSONRDF.class.getResourceAsStream(DETECTED_LANGUAGE_INSERT_SPARQL));
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}

			}
		};
	}

	@Override
	public String getAnalysisKey() {
		return LANGUAGES;
	}
}
