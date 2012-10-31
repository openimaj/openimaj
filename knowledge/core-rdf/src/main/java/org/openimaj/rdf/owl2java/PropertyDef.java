package org.openimaj.rdf.owl2java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.PropertyException;

import org.apache.commons.lang.WordUtils;
import org.openimaj.rdf.serialize.Predicate;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.model.MemBNode;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementList;

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
	/** A map of XML Schema types to Java types */
	protected static HashMap<URI,String> typeMap = new HashMap<URI,String>();
	
	/** A map of XML Schema types to imports */
	protected static HashMap<URI,String> importMap = new HashMap<URI,String>();
	
	/** We'll set up the importMap and typeMap here */
	static
	{
		// --------- Import Maps -------- //
		// Some of the XMLSchema types will need specific imports for them to
		// work.  For example, xsd:dateTime will become a DateTime.  This requires
		// an import (or a mapping). Other types may need to be mapped (below)
		// and imported (using imports defined here). Note that the generator
		// will add ".*" to the end of the import strings.
		PropertyDef.importMap.put( 
			new URIImpl("http://www.w3.org/2001/XMLSchema#date"), 
			"java.util" );
		
		PropertyDef.importMap.put(
			new URIImpl("http://www.w3.org/2001/XMLSchema#dateTime"), 
			"org.joda.time" );
		
		// ----------- Type Maps ---------- //
		// XMLSchema types will be converted into Java types by capitalising the
		// first letter of the XMLSchema type (and removing the namespace).
		// So simple types will just work -> string=String, float=Float.
		// Some won't work int=Integer.  It may be necessary to map some
		// other URIs to specific types here.
		PropertyDef.typeMap.put( 
			new URIImpl("http://www.w3.org/2001/XMLSchema#int"), 
			"Integer" );
	}

	/**
	 * 	The type of the property.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 30 Oct 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	protected enum PropertyType
	{
		/** The property is an ObjectProperty; that is, the property links
		 * 	individuals (instances) to other individuals */
		OBJECT,
		
		/** The property is a DataTypeProperty; that is, the property links
		 * 	individuals (instances) to data values */
		DATATYPE
	}
	
	/** The URI of this property */
	protected URI uri;

	/** The comment on this property */
	protected String comment;
	
	/** The type of this property */
	protected PropertyType type = PropertyType.DATATYPE;
	
	/** The range of the property (for Object properties) */
	protected List<URI> range = new ArrayList<URI>();
	
	/** The domain of the property */
	protected List<URI> domain = new ArrayList<URI>();

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.uri.getLocalName();
	}
	
	/**
	 * 	Returns the import required for the Java declaration of this
	 * 	property. If no import is required, then null will be returned.
	 *	@return The import type as a string.
	 */
	public String needsImport()
	{
		if( this.range.size() == 0 || this.range.size() > 1 )
			return null;
		System.out.println( this.range.get(0)+" needs import "+PropertyDef.importMap.get( this.range.get(0) ) );
		return PropertyDef.importMap.get( this.range.get(0) );
	}

	/**
	 *	Returns the Java declaration type for this property 
	 *	@return A string
	 */
	public String getDeclarationType()
	{
		// The default type of the property will be a string.
		String valueType = "String";
		
		// If this is an object property, we'll have to go away and try to find
		// out the type of the range of the property.
		if( this.range.size() > 0 )
		{
			// Set the type of the declaration based on the range of the property
			if( this.range.size() == 1 )
			{
				final URI rangeURI = this.range.get(0);
				
				// If there's a mapping in typeMap, we'll use the mapped value instead.
				if( PropertyDef.typeMap.get( rangeURI ) != null )
						valueType = PropertyDef.typeMap.get( rangeURI );
				// Otherwise, capitalise the name of the type and use that
				else	valueType = Generator.getTypeName( rangeURI );
			}
			// If there's multiple ranges, we'll just use Object
			else	valueType = "Object";
		}

		return valueType;
	}
	
	/**
	 * 	Outputs a Java definition for the property, including a comment
	 * 	if there is a comment for the property. The comment will be formatted
	 * 	slightly differently if it's very long. If generateAnnotations is true,
	 * 	then a {@link Predicate} annotation will be generated for each declaration
	 * 	containing the URI of the property. DataType properties will be encoded
	 * 	as Strings and Object properties will be declared as their appropriate
	 * 	type. 
	 *
	 * 	@param prefix The String prefix to add to all lines in the generated code
	 * 	@param generateAnnotations Whether to generate @@Predicate annotations
	 *
	 *	@return A string containing a Java definition
	 */
	public String toJavaDefinition( final String prefix, final boolean generateAnnotations )
	{
		final String valueType = this.getDeclarationType();
		String s = "";

		// Put a comment in front of the declaration if we have some text
		// to put in it.
		if( this.comment != null )
		{
			if( this.comment.length() < 80 )
				s += "\n"+prefix+"/** "+this.comment+" */\n";
			else
				s += "\n"+prefix+"/** "+
					WordUtils.wrap( this.comment, 70 ).replaceAll( "\\r?\\n", "\n"+prefix+"    " )
					+" */\n";
		}

		// Add the @Predicate annotation if we're doing that
		if( generateAnnotations )
			s += prefix+"@Predicate(\""+this.uri+"\")\n";

		// This is the declaration of the variable
		s += prefix+"public "+valueType+" "+this.uri.getLocalName() + ";";

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
		final String valueType = this.getDeclarationType();
		final String pName = Generator.getTypeName( this.uri );

		String s = "";

		s += prefix+"public "+valueType+" get"+pName+"()";

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

		s += prefix+"public void set"+pName+"( "+valueType+" "+this.uri.getLocalName()+" )";

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
	 *	@return A list of {@link PropertyException}
	 *	@throws RepositoryException
	 *	@throws MalformedQueryException
	 *	@throws QueryEvaluationException
	 */
	static List<PropertyDef> loadProperties( final URI uri, final RepositoryConnection conn )
			throws RepositoryException,	MalformedQueryException, QueryEvaluationException
	{
		// SPARQL query to get the properties and property comments
		final String query = 
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"prefix owl: <http://www.w3.org/2002/07/owl#> " +
				"SELECT ?property ?type ?comment ?range ?domain ?listNode WHERE "+
					"{ ?property rdfs:domain <"+uri+">. "+
					"  ?property rdf:type ?type. "+
					"  OPTIONAL { ?property rdfs:comment ?comment .} "+
					"  OPTIONAL { ?property rdfs:range ?range. } "+
					"  OPTIONAL { ?property rdfs:domain ?domain. } "+
					"  OPTIONAL { ?domain owl:unionOf ?listNode. } "+
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

			// Set the type of the property
			if( bindingSet.getValue( "type" ).stringValue().equals(
						"http://www.w3.org/2002/07/owl#ObjectProperty" ) )
				def.type = PropertyType.OBJECT;
			else	
			if( bindingSet.getValue( "type" ).stringValue().equals(
					"http://www.w3.org/2002/07/owl#DatatypeProperty" ) )
				def.type = PropertyType.DATATYPE;
			else
				// Other types are currently unsupported (ignored)
				continue;
			
			// If there's a comment, store that too.
			if( bindingSet.getValue("comment") != null )
				def.comment = bindingSet.getValue("comment").stringValue();

			// Set the domain of the property
			if( bindingSet.getValue("domain") != null )
			{
				final Value v = bindingSet.getValue( "domain" );
				if( v instanceof URI )
					def.domain.add( (URI)v ); 
				else
				// BNodes are used to store lists of URIs
				if( v instanceof MemBNode )
				{
					final MemBNode m = (MemBNode)bindingSet.getBinding( "listNode" );
					if( m != null )
						def.domain.addAll( PropertyDef.getURIListBNode( m ) );
				}
			}
			
			// Set the domain of the property
			if( bindingSet.getValue("range") != null )
			{
				final Value v = bindingSet.getValue( "range" );
				if( v instanceof URI )
					def.range.add( (URI)v );
				else
				// BNodes are used to store lists of URIs
				if( v instanceof MemBNode )
				{
					final MemBNode m = (MemBNode)bindingSet.getBinding( "listNode" );
					if( m != null )
						def.range.addAll( PropertyDef.getURIListBNode( m ) );
				}
			}

			properties.add( def );

//			System.out.println( "Property: "+def.toString() );
//			System.out.println( "    -> Range: "+def.range );
//			System.out.println( "    -> Domain: "+def.domain );
		}
			
		res.close();
		
		return properties;
	}
	
	/**
	 * 	For a given URI that represents a node in an RDF Collection, 
	 * 	will returns all the items in the list.
	 * 
	 * 	@param listNode The URI of the list node
	 * 	@param conn The repository connection
	 * 	@return A list of URIs from the RDF list
	 */
	protected static List<URI> getURIList( final URI listNode, final RepositoryConnection conn )
	{
		final List<URI> uris = new ArrayList<URI>();
		try
		{
			// SPARQL 1.1
			final String sparql = "SELECT * WHERE { "+listNode+" rdf:rest*/rdf:first ?value. }";
			
			final TupleQuery tq = conn.prepareTupleQuery( QueryLanguage.SPARQL, sparql );
			final TupleQueryResult res = tq.evaluate();

			while( res.hasNext() )
			{
				final BindingSet bs = res.next();
				final Binding value = bs.getBinding( "value" );
				if( value instanceof URI )
					uris.add( (URI)value );
			}
			
			res.close();
		}
		catch( final RepositoryException e )
		{
			e.printStackTrace();
		}
		catch( final MalformedQueryException e )
		{
			e.printStackTrace();
		}
		catch( final QueryEvaluationException e )
		{
			e.printStackTrace();
		}
		
		return uris;
	}
	
	/**
	 * 	Gets a list of URIs from a bnode that's part of an RDF Collection.
	 *	@param bNode The Bnode
	 *	@return
	 */
	protected static List<URI> getURIListBNode( final MemBNode bNode )
	{
		final List<URI> list = new ArrayList<URI>();
		PropertyDef.getURIListBNode( bNode, list );
		return list;
	}
	
	/**
	 * 	Gets a list of URIs from a bnode that's part of an RDF Collection.
	 *	@param bNode The Bnode
	 *	@param list the list to fill
	 */
	private static void getURIListBNode( final MemBNode bNode, final List<URI> list )
	{
		final MemStatementList ssl = bNode.getSubjectStatementList();
		MemBNode nextNode = null;
		for( int i = 0; i < ssl.size(); i++ )
		{
			final MemStatement statement = ssl.get(i);
			if( statement.getPredicate().stringValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#first") )
				if( statement.getObject() instanceof URI )
					list.add( (URI)statement.getObject() );
			
			if( statement.getPredicate().stringValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest") )
				if( statement.getObject() instanceof MemBNode )
					nextNode = (MemBNode)statement.getObject();
		}
		
		// Recurse down the list
		if( nextNode != null )
			PropertyDef.getURIListBNode( nextNode, list );
	}
}
