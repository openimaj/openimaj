package org.openimaj.text.nlp.namedentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Backed by a {@link SparqlTransitiveClosure}, find the set of transitive URIs
 * following the rdfs:subClassOf properties ending with the rdf:type property.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoEntityFinder {
	SparqlTransitiveClosure st;

	/**
	 * instantiate the {@link SparqlTransitiveClosure} using the
	 * {@link YagoQueryUtils#YAGO_SPARQL_ENDPOINT}
	 */
	public YagoEntityFinder() {
		st = new SparqlTransitiveClosure(YagoQueryUtils.YAGO_SPARQL_ENDPOINT);
	}

	/**
	 * @param rootURIs
	 * @return for a list of URIs return the set of transitive URIs
	 */
	public Set<String> getLeafURIsFor(List<String> rootURIs) {
		final HashSet<String> result = new HashSet<String>();
		for (final String uri : rootURIs) {
			result.addAll(st.getAllTransitiveLeavesOf(uri, "rdfs:subClassOf", "rdf:type"));
		}
		return result;
	}

	/**
	 * @param rootURI
	 * @return for a single URI return the set of transitive URIs
	 */
	public Set<String> getLeafURIsFor(String rootURI) {
		return st.getAllTransitiveLeavesOf(rootURI, "rdfs:subClassOf", "rdf:type");
	}

}
