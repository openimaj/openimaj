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
