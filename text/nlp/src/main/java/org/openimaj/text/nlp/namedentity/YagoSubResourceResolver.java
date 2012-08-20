package org.openimaj.text.nlp.namedentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YagoSubResourceResolver {
	SparqlTransitiveClosure st;
	
	public YagoSubResourceResolver(){
		st = new SparqlTransitiveClosure(YagoQueryUtils.YAGO_SPARQL_ENDPOINT);
	}
	
	public Set<String> getLeafURIsFor(List<String> rootURIs){
		HashSet<String> result = new HashSet<String>();		
		for(String uri : rootURIs){			
			result.addAll(st.getAllTransitiveLeavesOf(uri, "rdfs:subClassOf", "rdf:type"));
		}
		return result;
	}
	
	public Set<String> getLeafURIsFor(String rootURI){
		return st.getAllTransitiveLeavesOf(rootURI, "rdfs:subClassOf", "rdf:type");
	}

}
