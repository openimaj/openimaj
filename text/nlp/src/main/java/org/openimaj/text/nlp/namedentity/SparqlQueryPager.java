package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * A class to handle the page iteration through a set of results from a sparql endpoint query.
 * Will increase the page size until failure, then resume on last successful page size.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SparqlQueryPager {
	private String endPoint;
	private int increaseFactor = 5;
	
	/**
	 * @param endPoint = URL string of endpoint
	 */
	public SparqlQueryPager(String endPoint){
		this.endPoint=endPoint;
	}
	
	/**
	 * Process a query in the form of a string.
	 * @param queryString = SELECT with no LIMIT or OFFSET clauses.
	 * @return List of com.hp.hpl.jena.query.QuerySolution.
	 */
	public ArrayList<QuerySolution> pageQuery(String queryString){
		int rollBacks=0;
		ArrayList<QuerySolution> result = new ArrayList<QuerySolution>();
		int currentOffset = 0;
		int currentChunkSize = 500;		
		while(true){
			String pageQueryString = queryString+" OFFSET "+currentOffset+" LIMIT "+currentChunkSize;
			ArrayList<QuerySolution> subResult = fetch(pageQueryString);
			if(subResult==null){
				if(rollBacks>2)return result;
				currentChunkSize=currentChunkSize/increaseFactor;
				rollBacks++;
				continue;
			}
			else if(subResult.size()<currentChunkSize){
				result.addAll(subResult);
				break;
			}
			else{
				result.addAll(subResult);
				currentOffset+=currentChunkSize;
				currentChunkSize*=increaseFactor;
			}
		}
		return result;
	}

	private ArrayList<QuerySolution> fetch(String pageQueryString) {
		ArrayList<QuerySolution> result = new ArrayList<QuerySolution>();
		Query q = QueryFactory.create(pageQueryString);
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
			e.printStackTrace();
			return null;
		}		
		finally {
			qexec.close();
		}		
		return result;
	}

}
