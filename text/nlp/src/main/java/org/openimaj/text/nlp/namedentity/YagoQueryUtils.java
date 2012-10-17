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
package org.openimaj.text.nlp.namedentity;

import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Collection of uris and tools for accessing yago
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoQueryUtils {
	private static String PREFIX = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
	/**
	 * The openlinksw sparql endpoint
	 */
	public static String YAGO_SPARQL_ENDPOINT = "http://lod.openlinksw.com/sparql";
	/**
	 * The wordnet organisation URI
	 */
	public static String WORDNET_ORGANISATION_URI = "http://yago-knowledge.org/resource/wordnet_organization_108008335";
	/**
	 * wordnet enterprise uri
	 */
	public static String WORDNET_ENTERPRISE_URI = "http://yago-knowledge.org/resource/wordnet_enterprise_108056231";
	/**
	 * wordnet company uri
	 */
	public static String WORDNET_COMPANY_URI = "http://yago-knowledge.org/resource/wordnet_company_108058098";
	/**
	 * the various kinds of wordnet organisation
	 */
	public static String[] WORDNET_ORGANISATION_ROOT_URIS = new String[] {
			"http://yago-knowledge.org/resource/wordnet_adhocracy_108009239",
			"http://yago-knowledge.org/resource/wordnet_affiliate_108009478",
			"http://yago-knowledge.org/resource/wordnet_bureaucracy_108009659",
			"http://yago-knowledge.org/resource/wordnet_nongovernmental_organization_108009834",
			// "http://yago-knowledge.org/resource/wordnet_fiefdom_108048625",
			// "http://yago-knowledge.org/resource/wordnet_line_of_defense_108048743",
			// "http://yago-knowledge.org/resource/wordnet_line_organization_108048948",
			"http://yago-knowledge.org/resource/wordnet_association_108049401",
			"http://yago-knowledge.org/resource/wordnet_polity_108050385",
			"http://yago-knowledge.org/resource/wordnet_quango_108050484",
			"http://yago-knowledge.org/resource/wordnet_institution_108053576",
			"http://yago-knowledge.org/resource/wordnet_enterprise_108056231",
			"http://yago-knowledge.org/resource/wordnet_defense_108064130",
			"http://yago-knowledge.org/resource/wordnet_establishment_108075847",
			// "http://yago-knowledge.org/resource/wordnet_fire_brigade_108121394",
			"http://yago-knowledge.org/resource/wordnet_company_108187033",
			// "http://yago-knowledge.org/resource/wordnet_unit_108189659",
			// "http://yago-knowledge.org/resource/wordnet_force_108208016",
			"http://yago-knowledge.org/resource/wordnet_union_108233056",
			// "http://yago-knowledge.org/resource/wordnet_musical_organization_108246613",
			"http://yago-knowledge.org/resource/wordnet_party_108256968",
			// "http://yago-knowledge.org/resource/wordnet_machine_108264583",
			// "http://yago-knowledge.org/resource/wordnet_machine_108264759",
			"http://yago-knowledge.org/resource/wordnet_professional_organization_108266070",
			"http://yago-knowledge.org/resource/wordnet_alliance_108293982",
			"http://yago-knowledge.org/resource/wordnet_federation_108303504",
	// "http://yago-knowledge.org/resource/wordnet_hierarchy_108376051",
	// "http://yago-knowledge.org/resource/wordnet_deputation_108402442",
	// "http://yago-knowledge.org/resource/wordnet_blue_108480847",
	// "http://yago-knowledge.org/resource/wordnet_grey_108481009",
	// "http://yago-knowledge.org/resource/wordnet_host_108481184",
	// "http://yago-knowledge.org/resource/wordnet_pool_108481369"
	};

	/*
	 * Methods to return paramatised sparql query strings.
	 */

	/**
	 * @param companyURI
	 * @return a SPARQL query to find the "iscalled" fact for a companyURI
	 */
	public static String isCalledAlliasQuery(String companyURI) {
		return PREFIX + "SELECT ?alias WHERE {" + "?fact rdf:predicate <http://yago-knowledge.org/resource/isCalled> ."
				+ "?fact rdf:object   ?alias ." + "?fact rdf:subject <" + companyURI + "> }";
	}

	/**
	 * @param companyURI
	 * @return query for company aliases via rdfs:label
	 */
	public static String labelAlliasQuery(String companyURI) {
		return PREFIX + "SELECT ?alias WHERE {" + " <" + companyURI + "> rdfs:label ?alias ." + "}";
	}

	/**
	 * @return wordnet company SPARQL using {@link #WORDNET_COMPANY_URI}
	 */
	public static String wordnetCompanyQuery() {
		return PREFIX + "SELECT ?company WHERE {" + " ?company rdf:type <" + WORDNET_COMPANY_URI + "> . " + "}";
	}

	/**
	 * @return things which are subclasses of {@link #WORDNET_COMPANY_URI}
	 */
	public static String subClassWordnetCompanyQuery() {
		return PREFIX + "SELECT ?company WHERE {" + " ?subclass rdfs:subClassOf <" + WORDNET_COMPANY_URI + "> . "
				+ " ?company rdf:type ?subclass . " + "}";
	}

	/**
	 * @param companyURI
	 * @return the subject of facts with object compnayURI and predicate owns
	 */
	public static String ownsContextQuery(String companyURI) {
		return PREFIX + "SELECT ?context WHERE { " + "?fact rdf:object <" + companyURI + "> . "
				+ "?fact rdf:predicate owns ." + "?fact rdf:subject ?context}";
	}

	/**
	 * @param companyURI
	 * @return the object of facts with subject companyURI and predicate created
	 */
	public static String createdContextQuery(String companyURI) {
		return PREFIX + "SELECT ?context WHERE {" + "?fact rdf:subject <" + companyURI + "> . "
				+ "?fact rdf:predicate <http://yago-knowledge.org/resource/created> ." + "?fact rdf:object ?context}";
	}

	/**
	 * @param companyURI
	 * @return the subject of facts with object companyURI and predicate
	 *         hasWikipediaAnchorText
	 */
	public static String anchorContextQuery(String companyURI) {
		return PREFIX + "SELECT ?context WHERE {" + "<" + companyURI
				+ "> <http://yago-knowledge.org/resource/hasWikipediaAnchorText> ?context " + "}";
	}

	/**
	 * @param companyURI
	 * @return the subject of facts with object companyURI and predicate
	 *         hasWikipediaUrl
	 */
	public static String wikiURLContextQuery(String companyURI) {
		return PREFIX + "SELECT ?context WHERE {" + "<" + companyURI
				+ "> <http://yago-knowledge.org/resource/hasWikipediaUrl> ?context " + "}";
	}

	/**
	 * @param subjectURI
	 * @param predicateURI
	 * @return the object of facts with subject subjectURI and predicate
	 *         predicateURI
	 */
	public static String factObjectsQuery(String subjectURI, String predicateURI) {
		return PREFIX + "SELECT ?object WHERE { " + "?f rdf:subject <" + subjectURI + "> . " + "?f rdf:predicate "
				+ predicateURI + " . " + "?f rdf:object ?object}";
	}

	/**
	 * @param subjectURI
	 * @param predicateURI
	 * @return the object of a triple with subjectURI and predicateURI
	 */
	public static String tripleObjectsQuery(String subjectURI, String predicateURI) {
		return PREFIX + "SELECT ?object WHERE { " + subjectURI + " " + predicateURI + " ?object}";
	}

	/**
	 * @param objectURI
	 * @param predicateURI
	 * @return the subject with objectURI and predicateURI
	 */
	public static String factSubjectsQuery(String objectURI, String predicateURI) {
		return PREFIX + "SELECT ?subject WHERE { " + "?f rdf:subject ?subject . " + "?f rdf:predicate " + predicateURI
				+ " . " + "?f rdf:object <" + objectURI + ">}";
	}

	/**
	 * @param objectURI
	 * @param predicateURI
	 * @return the subject with objectURI and predicateURI
	 */
	public static String tripleSubjectsQuery(String objectURI, String predicateURI) {
		return PREFIX + "SELECT ?subject WHERE { ?subject " + predicateURI + " " + objectURI + "}";
	}

	/**
	 * @param variableNameToPredicate
	 * @param subjectUri
	 * @return retrieve all the predicate values for a given subjectUri. Each
	 *         predicate variable can be named separately
	 */
	public static String multiTripleObjectsQuery(Map<String, String> variableNameToPredicate, String subjectUri) {
		final StringBuffer sb = new StringBuffer();
		sb.append(PREFIX + "SELECT * WHERE {");
		for (final String varName : variableNameToPredicate.keySet()) {
			sb.append("<" + subjectUri + "> " + variableNameToPredicate.get(varName) + " ?" + varName + " . ");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * @param variableNameToPredicate
	 * @param subjectUri
	 * @return all the facts/objects whose subjects are subjectUri and which
	 *         have the predicates defined in variableNameToPredicate
	 */
	public static String multiFactObjectsQuery(Map<String, String> variableNameToPredicate, String subjectUri) {
		final StringBuffer sb = new StringBuffer();
		sb.append(PREFIX + "SELECT * WHERE {");
		final int fcount = 0;
		for (final String varName : variableNameToPredicate.keySet()) {
			sb.append("?fact" + fcount + " rdf:subject <" + subjectUri + "> . " + "?fact" + fcount + " rdf:predicate "
					+ variableNameToPredicate.get(varName) + " . " + "?fact" + fcount + " rdf:object ?" + varName + " . ");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * @param literal
	 * @return turns ^^ literals to strings
	 */
	public static String yagoLiteralToString(String literal) {
		return StringEscapeUtils.unescapeJava(literal.substring(0, literal.indexOf("^^http")));
	}

	/**
	 * @param resource
	 * @return yago resources by name
	 */
	public static String yagoResourceToString(String resource) {
		return resource.substring(resource.lastIndexOf("/") + 1).replaceAll("_", " ").trim();
	}

	/**
	 * lightweight test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//final String apple = "http://yago-knowledge.org/resource/Apple_Inc.";
		// System.out.println(isCalledAlliasQuery(apple)); //works
		// System.out.println(labelAlliasQuery(apple)); //works
		// System.out.println(wordnetCompanyQuery()); //works
		// System.out.println(subClassWordnetCompanyQuery()); //works
		// System.out.println(ownsContextQuery(apple)); /** Does not work **/
		// System.out.println(createdContextQuery(apple)); //works
		// System.out.println(anchorContextQuery(apple)); /** Does not work **/
		// System.out.println(wikiURLContextQuery(apple)); //
		//System.out.println(factSubjectsQuery("http://yago-knowledge.org/resource/wikicategory_Low-cost_airlines",
		//		"rdfs:subClassOf")); // works
		// System.out.println(factObjectsQuery("http://yago-knowledge.org/resource/wikicategory_Low-cost_airlines",
		// "rdfs:subClassOf")); //works

	}

}
