package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashSet;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class SparqlTransitiveClosure {
	
	private String endPoint;
	public static String LEAF = "leaf";

	public SparqlTransitiveClosure(String endPoint) {
		super();
		this.endPoint = endPoint;
	}
	
	public HashSet<String> getAllTransitiveLeavesOf(String rootEntity, String transitiveRelationship, String leafRelationship){
		return scoureTheTree(rootEntity, transitiveRelationship, leafRelationship);
	}

	private HashSet<String> scoureTheTree(String rootEntity,
			String transitiveRelationship, String leafRelationship) {
		ArrayList<QuerySolution> subNodes = getSubNodes(rootEntity,transitiveRelationship);
		HashSet<String> result = getLeaves(rootEntity, leafRelationship);
		for(QuerySolution soln : subNodes){
			result.addAll(scoureTheTree(soln.getResource("subject").getURI(),transitiveRelationship,leafRelationship));
		}
		return result;
	}

	private HashSet<String> getLeaves(String rootEntity,
			String leafRelationship) {
		HashSet<String> result = new HashSet<String>();
		Query q = QueryFactory.create(YagoQueryUtils.factSubjectsQuery(rootEntity, leafRelationship));
		QueryExecution qexec =QueryExecutionFactory.sparqlService(endPoint, q);
		ResultSet results;
		//System.out.println("--------"+rootEntity+"------------");
		try {
			results = qexec.execSelect();		
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				result.add(soln.getResource("subject").getURI());
				//System.out.println(soln.getResource("subject").getURI());
			}
		}
		catch(Exception e){
			return result;
		}		
		finally {
			qexec.close();
		}		
		return result;
	}

	private ArrayList<QuerySolution> getSubNodes(String rootEntity,
			String transitiveRelationship) {
		ArrayList<QuerySolution> result = new ArrayList<QuerySolution>();
		Query q = QueryFactory.create(YagoQueryUtils.factSubjectsQuery(rootEntity, transitiveRelationship));
		QueryExecution qexec =QueryExecutionFactory.sparqlService(endPoint, q);
		ResultSet results;
		try {
			results = qexec.execSelect();		
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				result.add(soln);				
			}
		}
		catch(Exception e){
			return result;
		}		
		finally {
			qexec.close();
		}		
		return result;
	}
	
	public static void main(String[] args) {
		SparqlTransitiveClosure st = new SparqlTransitiveClosure(YagoQueryUtils.YAGO_SPARQL_ENDPOINT);
		HashSet<String> res = st.getAllTransitiveLeavesOf(YagoQueryUtils.WORDNET_COMPANY_URI, "rdfs:subClassOf", "rdf:type");
		System.out.println(res.size());
	}

}
