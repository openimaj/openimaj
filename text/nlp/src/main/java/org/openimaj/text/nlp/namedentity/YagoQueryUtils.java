package org.openimaj.text.nlp.namedentity;

import org.apache.commons.lang.StringEscapeUtils;

public class YagoQueryUtils {

	public static String YAGO_SPARQL_ENDPOINT = "http://lod.openlinksw.com/sparql";
	public static String WORDNET_ORGANISATION_URI="http://yago-knowledge.org/resource/wordnet_organization_108008335";
	public static String WORDNET_ENTERPRISE_URI="http://yago-knowledge.org/resource/wordnet_enterprise_108056231";
	public static String WORDNET_COMPANY_URI="http://yago-knowledge.org/resource/wordnet_company_108058098";
	public static String[] WORDNET_ORGANISATION_ROOT_URIS = new String[]{
		"http://yago-knowledge.org/resource/wordnet_adhocracy_108009239",
			"http://yago-knowledge.org/resource/wordnet_affiliate_108009478",
			"http://yago-knowledge.org/resource/wordnet_bureaucracy_108009659",
			"http://yago-knowledge.org/resource/wordnet_nongovernmental_organization_108009834",
			//"http://yago-knowledge.org/resource/wordnet_fiefdom_108048625",
			//"http://yago-knowledge.org/resource/wordnet_line_of_defense_108048743",
			//"http://yago-knowledge.org/resource/wordnet_line_organization_108048948",
			"http://yago-knowledge.org/resource/wordnet_association_108049401",
			"http://yago-knowledge.org/resource/wordnet_polity_108050385",
			"http://yago-knowledge.org/resource/wordnet_quango_108050484",
			"http://yago-knowledge.org/resource/wordnet_institution_108053576",
			"http://yago-knowledge.org/resource/wordnet_enterprise_108056231",
			"http://yago-knowledge.org/resource/wordnet_defense_108064130",
			"http://yago-knowledge.org/resource/wordnet_establishment_108075847",
			//"http://yago-knowledge.org/resource/wordnet_fire_brigade_108121394",
			"http://yago-knowledge.org/resource/wordnet_company_108187033",
			//"http://yago-knowledge.org/resource/wordnet_unit_108189659",
			//"http://yago-knowledge.org/resource/wordnet_force_108208016",
			"http://yago-knowledge.org/resource/wordnet_union_108233056",
			//"http://yago-knowledge.org/resource/wordnet_musical_organization_108246613",
			"http://yago-knowledge.org/resource/wordnet_party_108256968",
			//"http://yago-knowledge.org/resource/wordnet_machine_108264583",
			//"http://yago-knowledge.org/resource/wordnet_machine_108264759",
			"http://yago-knowledge.org/resource/wordnet_professional_organization_108266070",
			"http://yago-knowledge.org/resource/wordnet_alliance_108293982",
			"http://yago-knowledge.org/resource/wordnet_federation_108303504",
			//"http://yago-knowledge.org/resource/wordnet_hierarchy_108376051",
			//"http://yago-knowledge.org/resource/wordnet_deputation_108402442",
			//"http://yago-knowledge.org/resource/wordnet_blue_108480847",
			//"http://yago-knowledge.org/resource/wordnet_grey_108481009",
			//"http://yago-knowledge.org/resource/wordnet_host_108481184",
			//"http://yago-knowledge.org/resource/wordnet_pool_108481369"
	};

	/*
	 * Methods to return paramatised sparql query strings.
	 */

	public static String isCalledAlliasQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?alias WHERE {"
				+ "?fact rdf:predicate <http://yago-knowledge.org/resource/isCalled> ."
				+ "?fact rdf:object   ?alias ." + "?fact rdf:subject <"
				+ companyURI + "> }";
	}

	public static String labelAlliasQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?alias WHERE {" + " <" + companyURI
				+ "> rdfs:label ?alias ." + "}";
	}

	public static String wordnetCompanyQuery() {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?company WHERE {"
				+ " ?company rdf:type <http://yago-knowledge.org/resource/wordnet_company_108058098> . "
				+ "}";
	}

	public static String subClassWordnetCompanyQuery() {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?company WHERE {"
				+ " ?subclass rdfs:subClassOf <http://yago-knowledge.org/resource/wordnet_company_108058098> . "
				+ " ?company rdf:type ?subclass . " + "}";
	}
	
	public static String ownsContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE { " + "?fact rdf:object <"
				+ companyURI + "> . " + "?fact rdf:predicate owns ."
				+ "?fact rdf:subject ?context}";
	}

	public static String createdContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {"
				+ "?fact rdf:subject <"
				+ companyURI
				+ "> . "
				+ "?fact rdf:predicate <http://yago-knowledge.org/resource/created> ."
				+ "?fact rdf:object ?context}";
	}

	public static String anchorContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {"
				+ "<"
				+ companyURI
				+ "> <http://yago-knowledge.org/resource/hasWikipediaAnchorText> ?context "
				+ "}";
	}

	public static String wikiURLContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {"
				+ "<"
				+ companyURI
				+ "> <http://yago-knowledge.org/resource/hasWikipediaUrl> ?context "
				+ "}";
	}

	public static String factObjectsQuery(String subjectURI,
			String predicateURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?object WHERE { " + "?f rdf:subject <" + subjectURI
				+ "> . " + "?f rdf:predicate " + predicateURI + " . "
				+ "?f rdf:object ?object}";
	}

	public static String factSubjectsQuery(String objectURI,
			String predicateURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?subject WHERE { " + "?f rdf:subject ?subject . "
				+ "?f rdf:predicate " + predicateURI + " . "
				+ "?f rdf:object <" + objectURI + ">}";
	}

	public static String yagoLiteralToString(String literal) {
		return StringEscapeUtils.unescapeJava(literal.substring(0,
				literal.indexOf("^^http")));
	}

	public static String yagoResourceToString(String resource) {
		return resource.substring(resource.lastIndexOf("/") + 1)
				.replaceAll("_", " ").trim();
	}

	public static void main(String[] args) {
		String apple = "http://yago-knowledge.org/resource/Apple_Inc.";
		// System.out.println(isCalledAlliasQuery(apple)); //works
		// System.out.println(labelAlliasQuery(apple)); //works
		// System.out.println(wordnetCompanyQuery()); //works		
		// System.out.println(subClassWordnetCompanyQuery()); //works
		// System.out.println(ownsContextQuery(apple)); /** Does not work **/
		// System.out.println(createdContextQuery(apple)); //works
		// System.out.println(anchorContextQuery(apple)); /** Does not work **/
		// System.out.println(wikiURLContextQuery(apple)); //
		System.out.println(factSubjectsQuery("http://yago-knowledge.org/resource/wikicategory_Low-cost_airlines", "rdfs:subClassOf")); //works
		//System.out.println(factObjectsQuery("http://yago-knowledge.org/resource/wikicategory_Low-cost_airlines", "rdfs:subClassOf")); //works

	}

}
