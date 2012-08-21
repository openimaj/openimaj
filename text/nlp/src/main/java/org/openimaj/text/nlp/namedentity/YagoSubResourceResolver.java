package org.openimaj.text.nlp.namedentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a wrapper for {@link SparqlTransitiveClosure} that specifically gets
 * rdf:type leaves from rdfs:subClassOf transitive relationships
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoSubResourceResolver {
	SparqlTransitiveClosure st;

	/**
	 * Default Constructor
	 */
	public YagoSubResourceResolver() {
		st = new SparqlTransitiveClosure(YagoQueryUtils.YAGO_SPARQL_ENDPOINT);
	}

	/**
	 * @param rootURIs
	 * @return leaf uris
	 */
	public Set<String> getLeafURIsFor(List<String> rootURIs) {
		HashSet<String> result = new HashSet<String>();
		for (String uri : rootURIs) {
			result.addAll(st.getAllTransitiveLeavesOf(uri, "rdfs:subClassOf",
					"rdf:type"));
		}
		return result;
	}

	/**
	 * @param rootURI
	 * @return leaf uris
	 */
	public Set<String> getLeafURIsFor(String rootURI) {
		return st.getAllTransitiveLeavesOf(rootURI, "rdfs:subClassOf",
				"rdf:type");
	}

}
