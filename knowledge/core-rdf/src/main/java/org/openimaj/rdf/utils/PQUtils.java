package org.openimaj.rdf.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Some toots for playing with {@link ParameterizedSparqlString} instances
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class PQUtils {

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

	public static void setNull(ParameterizedSparqlString pss, String name) {
		pss.setLiteral(name, Node.NULL.toString());
	}

	public static void setPSSLiteral(ParameterizedSparqlString pss, String name, double d) {
		pss.setLiteral(name, d);
	}

	public static void setPSSLiteral(ParameterizedSparqlString pss, String name, int d) {
		pss.setLiteral(name, d);
	}

	public static void setPSSLiteral(ParameterizedSparqlString pss, String name, String lit) {
		if (lit != null)
			pss.setLiteral(name, lit);
		else
			setNull(pss, name);

	}

	public static void setPSSIri(ParameterizedSparqlString pss, String name, String iri) {
		if (iri != null)
			pss.setIri(name, iri);
		else
			setNull(pss, name);
	}

	public static ParameterizedSparqlString constructPQ(String query, Model m) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString(query, m);
		return pss;
	}
}
