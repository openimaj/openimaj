package org.openimaj.text.nlp.namedentity;

import org.apache.commons.lang.StringEscapeUtils;

public class YagoQueryUtils {
	
	public static String YAGO_SPARQL_ENDPOINT = "http://lod.openlinksw.com/sparql";
	
	/*
	 * Methods to return paramatised sparql query strings.
	 */

	public static String isCalledAlliasQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?alias WHERE {"
				+ "?fact rdf:predicate <http://yago-knowledge.org/resource/isCalled> ."
				+ "?fact rdf:object   ?alias ." 
				+ "?fact rdf:subject <"+ companyURI + "> }";
	}

	public static String labelAlliasQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?alias WHERE {"				
				+ " <"
				+ companyURI
				+ "> rdfs:label ?alias ."				
				+ "}";
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
				+ "SELECT ?context WHERE { " +
				"?fact rdf:object <"+companyURI+"> . " +
				"?fact rdf:predicate owns ." +
				"?fact rdf:subject ?context}";
	}

	public static String createdContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {" + 
				"?fact rdf:subject <"+companyURI+"> . " +
				"?fact rdf:predicate <http://yago-knowledge.org/resource/created> ." +
				"?fact rdf:object ?context}";
	}
	
	public static String anchorContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {" + 
				"<"+companyURI+"> <http://yago-knowledge.org/resource/hasWikipediaAnchorText> ?context " +
				"}";
	}
	
	public static String wikiURLContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {" + 
				"<"+companyURI+"> <http://yago-knowledge.org/resource/hasWikipediaUrl> ?context " +
				"}";
	}
	
	public static String yagoLiteralToString(String literal){
		return StringEscapeUtils.unescapeJava(literal.substring(0, literal.indexOf("^^http")));
	}
	
	public static String yagoResourceToString(String resource){
		return resource.substring(resource.lastIndexOf("/") + 1)
				.replaceAll("_", " ").trim();
	}
	
	
	public static void main(String[] args){
		String apple = "http://yago-knowledge.org/resource/Apple_Inc.";
		//System.out.println(isCalledAlliasQuery(apple)); //works
		//System.out.println(labelAlliasQuery(apple)); //works
		System.out.println(wordnetCompanyQuery()); //works
		//System.out.println(subClassWordnetCompanyQuery()); //works
		//System.out.println(ownsContextQuery(apple)); /** Does not work **/
		//System.out.println(createdContextQuery(apple)); //works
		//System.out.println(anchorContextQuery(apple)); /** Does not work **/
		//System.out.println(wikiURLContextQuery(apple)); //
		
	}

}
