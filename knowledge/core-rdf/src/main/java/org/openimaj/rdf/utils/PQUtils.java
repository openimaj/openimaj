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
package org.openimaj.rdf.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Some tools for playing with {@link ParameterizedSparqlString} instances
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PQUtils {

	/**
	 * Set a {@link ParameterizedSparqlString} literal
	 *
	 * @param pss
	 * @param strings
	 * @param geo
	 */
	public static void setPSSLiteral(ParameterizedSparqlString pss, String[] strings, double[] geo) {
		for (int i = 0; i < strings.length; i++) {
			if (geo == null) {
				setNull(pss, strings[i]);
			}
			else {
				pss.setLiteral(strings[i], geo[i]);
			}
		}
	}

	/**
	 * Set a {@link ParameterizedSparqlString} null
	 *
	 * @param pss
	 * @param name
	 */
	public static void setNull(ParameterizedSparqlString pss, String name) {
		pss.setLiteral(name, Node.NULL.toString());
	}

	/**
	 * Set a {@link ParameterizedSparqlString} literal
	 *
	 * @param pss
	 * @param name
	 * @param d
	 */
	public static void setPSSLiteral(ParameterizedSparqlString pss, String name, double d) {
		pss.setLiteral(name, d);
	}

	/**
	 * Set a {@link ParameterizedSparqlString} literal
	 *
	 * @param pss
	 * @param name
	 * @param d
	 */
	public static void setPSSLiteral(ParameterizedSparqlString pss, String name, int d) {
		pss.setLiteral(name, d);
	}

	/**
	 * Set a {@link ParameterizedSparqlString} literal
	 *
	 * @param pss
	 * @param name
	 * @param lit
	 */
	public static void setPSSLiteral(ParameterizedSparqlString pss, String name, String lit) {
		if (lit != null)
			pss.setLiteral(name, lit);
		else
			setNull(pss, name);

	}

	/**
	 * @param pss
	 * @param name
	 * @param iri
	 */
	public static void setPSSIri(ParameterizedSparqlString pss, String name, String iri) {
		if (iri != null)
			pss.setIri(name, iri);
		else
			setNull(pss, name);
	}

	/**
	 * @param pss
	 * @param name
	 * @param iri
	 */
	public static void setPSSResource(ParameterizedSparqlString pss, String name, Resource iri) {
		if (iri != null)
			pss.setParam(name, iri);
		else
			setNull(pss, name);
	}

	/**
	 * Construct a {@link ParameterizedSparqlString}
	 * 
	 * @param query
	 * @param m
	 * @return the {@link ParameterizedSparqlString}
	 */
	public static ParameterizedSparqlString constructPQ(String query, Model m) {
		final ParameterizedSparqlString pss = new ParameterizedSparqlString(query, m);
		return pss;
	}
}
