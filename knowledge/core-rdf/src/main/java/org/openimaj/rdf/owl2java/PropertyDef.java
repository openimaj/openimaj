package org.openimaj.rdf.owl2java;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class PropertyDef {

	URI uri;
	String comment;

	@Override
	public String toString() {
		return uri.getLocalName() + "\n";
	}

	static List<PropertyDef> loadProperties(URI uri, RepositoryConnection conn) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException
	{
		final String query = "SELECT Property, Comment "
				+ "FROM {Property} rdfs:domain {<" + uri + ">}; "
				+ "[ rdfs:comment {Comment} ]";

		final TupleQuery preparedQuery = conn.prepareTupleQuery(QueryLanguage.SERQL, query);
		final TupleQueryResult res = preparedQuery.evaluate();

		final List<PropertyDef> properties = new ArrayList<PropertyDef>();
		while (res.hasNext()) {
			final BindingSet bindingSet = res.next();

			final PropertyDef def = new PropertyDef();
			def.uri = (URI) bindingSet.getValue("Property");

			if (bindingSet.getValue("Comment") != null)
				def.comment = bindingSet.getValue("Comment").toString();

			properties.add(def);
		}

		return properties;
	}
}
