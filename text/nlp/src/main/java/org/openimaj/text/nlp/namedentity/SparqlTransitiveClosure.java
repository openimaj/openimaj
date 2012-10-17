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
import java.util.HashSet;

import com.hp.hpl.jena.query.QuerySolution;

/**
 * Given an endpoint, resolve transitive relationships of a root entity.
 * Practically speaking this is a class is mainly used to allow for RDF rule:
 * rdfs11 and rdfs9 (see: http://www.w3.org/TR/rdf-mt/)
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SparqlTransitiveClosure {

	private final SparqlQueryPager pager;

	/**
	 * @param endPoint
	 *            instantiate a {@link SparqlQueryPager} ready to explore
	 *            transitive relationships
	 */
	public SparqlTransitiveClosure(String endPoint) {
		super();
		pager = new SparqlQueryPager(endPoint);
	}

	/**
	 * Given a root entity, return a hash set of all transitive relationships up
	 * until a leaf nodes
	 * 
	 * @param rootEntity
	 * @param transitiveRelationship
	 * @param leafRelationship
	 * @return a set of transitive relationships
	 */
	public HashSet<String> getAllTransitiveLeavesOf(String rootEntity, String transitiveRelationship,
			String leafRelationship)
	{
		return scoureTheTree(rootEntity, transitiveRelationship, leafRelationship);
	}

	private HashSet<String> scoureTheTree(String rootEntity, String transitiveRelationship, String leafRelationship) {
		final ArrayList<QuerySolution> subNodes = getSubNodes(rootEntity, transitiveRelationship);
		final HashSet<String> result = getLeaves(rootEntity, leafRelationship);
		for (final QuerySolution soln : subNodes) {
			result.addAll(scoureTheTree(soln.getResource("subject").getURI(), transitiveRelationship, leafRelationship));
		}
		return result;
	}

	private HashSet<String> getLeaves(String rootEntity, String leafRelationship) {
		final HashSet<String> result = new HashSet<String>();
		final ArrayList<QuerySolution> pageResults = pager.pageQuery(YagoQueryUtils.factSubjectsQuery(rootEntity,
				leafRelationship));
		// System.out.println("--------"+rootEntity+"------------");
		for (final QuerySolution soln : pageResults) {
			result.add(soln.getResource("subject").getURI());
			// System.out.println(soln.getResource("subject").getURI());
		}
		return result;
	}

	private ArrayList<QuerySolution> getSubNodes(String rootEntity, String transitiveRelationship) {
		final ArrayList<QuerySolution> result = pager.pageQuery(YagoQueryUtils.factSubjectsQuery(rootEntity,
				transitiveRelationship));
		return result;
	}

	/**
	 * a subclasof test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final SparqlTransitiveClosure st = new SparqlTransitiveClosure(YagoQueryUtils.YAGO_SPARQL_ENDPOINT);
		final HashSet<String> res = st.getAllTransitiveLeavesOf(YagoQueryUtils.WORDNET_COMPANY_URI, "rdfs:subClassOf",
				"rdf:type");
		System.out.println(res.size());
	}

}
