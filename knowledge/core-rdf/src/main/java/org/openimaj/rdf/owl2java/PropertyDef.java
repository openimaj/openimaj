package org.openimaj.rdf.owl2java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *	Represents the definition of a property of a class.
 *
 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 29 Oct 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class PropertyDef
{
	/** The URI of this property */
	protected URI uri;

	/** The comment on this property */
	protected String comment;

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.uri.getLocalName();
	}

	/**
	 * 	Outputs a Java definition for the property, including a comment
	 * 	if there is a comment for the property. The comment will be formatted
	 * 	slightly differently if it's very long.
	 *
	 * 	@param prefix
	 * 	@param generateAnnotations
	 *
	 *	@return A string containing a Java definition
	 */
	public String toJavaDefinition( final String prefix, final boolean generateAnnotations )
	{
		String s = "";

		if( this.comment != null )
		{
			if( this.comment.length() < 80 )
				s += "\n"+prefix+"/** "+this.comment+" */\n";
			else
				s += "\n"+prefix+"/** "+
					WordUtils.wrap( this.comment, 70 ).replaceAll( "\\r?\\n", "\n"+prefix+"    " )
					+" */\n";
		}

		if( generateAnnotations )
			s += prefix+"@Predicate(\""+this.uri+"\")\n";

		s += prefix+"public String "+this.uri.getLocalName() + ";";

		if( this.comment != null || generateAnnotations ) s += "\n";

		return s;
	}

	/**
	 * 	Generates setters and getters for the property.
	 *
	 * 	@param prefix
	 * 	@param implementations
	 * 	@param delegationObject
	 *	@return A string containing setters and getters
	 */
	public String toSettersAndGetters( final String prefix, final boolean implementations,
			final String delegationObject )
	{
		final String pName = this.uri.getLocalName().substring( 0, 1 ).toUpperCase()+
					this.uri.getLocalName().substring( 1 );

		String s = "";

		s += prefix+"public String get"+pName+"()";

		if( implementations )
		{
			s += "\n";
			s += prefix+"{\n";
			if( delegationObject != null )
					s += prefix+"\treturn "+delegationObject+".get"+pName+"();\n";
			else	s += prefix+"\treturn this."+this.uri.getLocalName()+";\n";
			s += prefix+"}\n";
		}
		else
			s += ";\n";
		s += prefix+"\n";

		s += prefix+"public void set"+pName+"( String "+this.uri.getLocalName()+" )";

		if( implementations )
		{
			s += "\n";
			s += prefix+"{\n";
			if( delegationObject != null )
					s += prefix+"\t"+delegationObject+".set"+pName+"( "+
						this.uri.getLocalName()+" );\n";
			else	s += prefix+"\tthis."+this.uri.getLocalName()+" = "+this.uri.getLocalName()+";\n";
			s += prefix+"}\n";
		}
		else
			s += ";\n";

		return s;
	}

	/**
	 *	For a given class URI, gets the properties of the class
	 *
	 *	@param uri A class URI
	 *	@param conn A repository containing the class definition
	 *	@return
	 *	@throws RepositoryException
	 *	@throws MalformedQueryException
	 *	@throws QueryEvaluationException
	 */
	static List<PropertyDef> loadProperties( final URI uri, final RepositoryConnection conn )
			throws RepositoryException,	MalformedQueryException, QueryEvaluationException
	{
		// SPARQL query to get the properties and property comments
		final String query = "SELECT ?property ?comment WHERE "+
					"{ ?property <http://www.w3.org/2000/01/rdf-schema#domain> <"+uri+">. "+
					"OPTIONAL { ?property <http://www.w3.org/2000/01/rdf-schema#comment> ?comment .}"+
				"}";

		// Prepare the query
		final TupleQuery preparedQuery = conn.prepareTupleQuery( QueryLanguage.SPARQL, query );

		// Execute the query
		final TupleQueryResult res = preparedQuery.evaluate();

		// Loop through the results
		final List<PropertyDef> properties = new ArrayList<PropertyDef>();
		while( res.hasNext() )
		{
			final BindingSet bindingSet = res.next();

			// Create a new PropertyDef and store the URI of the property
			final PropertyDef def = new PropertyDef();
			def.uri = (URI)bindingSet.getValue("property");

			// If there's a comment, store that too.
			if( bindingSet.getValue("comment") != null )
				def.comment = bindingSet.getValue("comment").stringValue();

			properties.add( def );
		}

		return properties;
	}
}
