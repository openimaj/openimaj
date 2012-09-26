/**
 * 
 */
package org.openimaj.rdf.serialize;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * The RDFSerializer is used to serialise an object to RDF. It will serialise
 * the object deeply. This class itself does not output any specific RDF
 * representation but generates triples which is gives to the method
 * {@link #addTriple(Statement)}. This method must be overridden in a subclass
 * to provide the actual representation output.
 * <p>
 * For example, to output to Turtle, you might use the OpenRDF TurtleWriter to
 * form a representation of the RDF graph:
 * <p>
 * <code><pre>
 * 		StringWriter sw = new StringWriter();
 * 		final TurtleWriter tw = new TurtleWriter( sw );
 * 		RDFSerializer rs = new RDFSerializer()
 * 		{
 * 			public void addTriple( Statement s )
 * 			{
 * 				tw.handleStatement( s );
 * 			}
 * 		};
 * 		rs.serialize( myObject );
 * 		System.out.println( sw.toString() );
 * 	</pre></code>
 * <p>
 * By default the class will only produce triples for fields which have been
 * annotated with the {@link Predicate} annotation. The annotation gives the URI
 * used to link the object to its field in the triple. If you wish to attempt to
 * serialise unannotated fields, then you should use the constructor that takes
 * a boolean, passing true: {@link #RDFSerializer(boolean)}. This will then
 * create predicates based on the field name, so field "name" will become
 * predicate "hasName". Complex fields are serialised into subgraphs and those
 * graphs have URIs automatically generated for them.
 * <p>
 * The {@link #serialize(Object, String)} method requires the URI of the object
 * to be serialised. This must be decided by the caller and passed in. It is
 * used to construct URIs for complex fields and predicates (if not given). So,
 * an object with URI <code>http://example.com/object</code> and a complex field
 * <code>name</code> will end up with a triple that links the object to a
 * subgraph representing the complex object as so: <code><pre>
 * 		http://example.com/object :hasName http://example.com/object_name
 * 	</pre></code>
 * The name of the subgraph is based on the URI of the object and the name of
 * the field. The predicate is automatically generated from the name of the
 * field also. Note that this means you may need to be careful about the names
 * of the fields. For example, if the object had a complex field
 * <code>name_first</code> and also had a complex field <code>name</code> that
 * had a complex field <code>first</code> it's possible the same URI may be
 * generated for separate subgraphs.
 * <p>
 * Primitive types will be typed with XSD datatypes.
 * <p>
 * Lists and collections are output in the same way. They are encoded using RDF
 * sequences; that is as subgraphs where the items have the predicates
 * <code>rdf:_1, rdf:_2..., rdf:_n</code> and the type <code>rdf:Seq</code>.
 * <p>
 * By default the serialisation will also output a triple that gives the class
 * name of the object which is being serialised. If you do not want this, use
 * the {@link #setOutputClassNames(boolean)} to turn it off.
 * <p>
 * This class also provides an unserialisation routine for converting an RDF
 * graph back into an object graph.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 11 Sep 2012
 * @version $Author$, $Revision$, $Date$
 */
public class RDFSerializer
{
	/** Predicate for giving the class name */
	public static final String RDF_OPENIMAJ_P_CLASSNAME = "http://rdf.openimaj.org/hasClassName/";

	/** Whether to try to create predicates for unannotated fields */
	protected boolean autoPredicate = false;

	/** Whether to output class names */
	protected boolean outputClassNames = true;

	/**
	 * Default constructor
	 */
	public RDFSerializer()
	{
		this( false );
	}

	/**
	 * Constructor that determines whether to create predicates automatically
	 * when the {@link Predicate} annotation does not exist.
	 * 
	 * @param autoPredicate Whether to automatically create predicates
	 */
	public RDFSerializer( boolean autoPredicate )
	{
		this.autoPredicate = autoPredicate;
	}

	/**
	 * Serialize the given object as RDF.
	 * 
	 * @param objectToSerialize The object to serialize.
	 * @param uri The URI of the object to serialize.
	 */
	public void serialize( Object objectToSerialize, String uri )
	{
		// The subject (the object to serialize) won't change, so
		// we'll just create the URI node once.
		URIImpl subject = new URIImpl( uri );

		// Output the class name of the object to serialise
		if( outputClassNames )
			addTriple( new StatementImpl( subject, new URIImpl( RDF_OPENIMAJ_P_CLASSNAME ), checkPrimitive( objectToSerialize.getClass().getName() ) ) );

		// Get all the fields
		List<Field> fields = getAllFields( objectToSerialize );

		// Loop through the fields and output them
		for( Field field : fields )
		{
			// System.out.println( "====== Field "+field+" ============");

			try
			{
				// Get the value of the field
				field.setAccessible( true );
				Object oo = field.get( objectToSerialize );

				// If it's not null, we'll output it
				if( oo != null )
				{
					// Get the predicate name (may be null if if cannot be
					// created either due to a lack of the @Predicate
					// annotation or because autoPredicate is false
					final URIImpl predicate = getPredicateName( field, uri );

					// Check whether we've got something we're going
					// to actually output. (we may be null here if the field
					// was not annotated with @Predicate and autoPredicate is
					// false.
					if( predicate != null )
					{
						Value object;
						if( (object = checkPrimitive( oo )) == null )
						{
							Object obj = oo;

							// If oo is an array, we'll wrap it in an array list
							// so that we can deal with it as a collection
							// below.
							if( oo.getClass().isArray() )
							{
								object = new URIImpl( uri + "_" + field.getName() );
								for( int count = 0; count < Array.getLength( oo ); )
								{
									Object o = Array.get( oo, count );
									count = processLoop( uri, field, count, o );
								}
							}
							else
							// If we have a collection of things, we'll output
							// them as an RDF linked-list.
							if( obj instanceof Collection<?> )
							{
								object = new URIImpl( uri + "_" + field.getName() );
								Statement t = new StatementImpl( new URIImpl( object.stringValue() ), new URIImpl( RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME ),
										new LiteralImpl( obj.getClass().getName() ) );
								addTriple( t );
								int count = 1;
								for( Object o : (Collection<?>) obj )
									count = processLoop( uri, field, count, o );
							}
							else
							{
								// Try to serialise the object
								object = new URIImpl( uri + "_" + field.getName() );
								serialize( oo, object.stringValue() );
							}
						}

						// Create a triple and send it to the serializer
						Statement t = new StatementImpl( subject, predicate, object );
						addTriple( t );
					}
				}
			}
			catch( Exception e )
			{
				System.out.println( "Error reflecting " + field );
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns a predicate name for the given field.
	 * 
	 * @param field The field
	 * @param uri The URI of the object
	 * @return A predicate URI, either generated from the @Predicate annotation
	 *         or from the field name
	 */
	private URIImpl getPredicateName( Field field, String uri )
	{
		// Get the predicate annotation, if there is one
		final Predicate predicateAnnotation = field.getAnnotation( Predicate.class );

		URIImpl predicate = null;
		if( predicateAnnotation != null )
		{
			// Create a predicate URI for this predicate
			predicate = new URIImpl( predicateAnnotation.value() );
		}
		// Null predicate annotation?
		else
		{
			// Try to create a predicate for the unannotated field
			if( autoPredicate ) predicate = new URIImpl( uri + "_has" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 ) );
		}

		return predicate;
	}

	/**
	 * A method that's called during the processing of a list of items to write
	 * a single item.
	 * 
	 * @param objectURI The uri of the current object which contains the list
	 * @param field The field being processed
	 * @param listCounter The current counter in the list (1-based index)
	 * @param listItemObject The object to be serialised
	 * @return the next counter in the list
	 */
	private int processLoop( String objectURI, Field field, int listCounter, Object listItemObject )
	{
		// The URI of the list object
		String u = objectURI + "_" + field.getName();
		URIImpl p = new URIImpl( RDF.NAMESPACE + "_" + listCounter );

		// If we're dealing with the first item in the list, we'll
		// put the RDF type (sequence) into the output
		if( listCounter == 1 ) addTriple( new StatementImpl( new URIImpl( u ), RDF.TYPE, RDF.SEQ ) );

		// If the values in the collection are primitives
		// we will simply add them in a list, otherwise
		// we'll attempt to serialise them separately.
		Value ooo;
		if( (ooo = checkPrimitive( listItemObject )) != null )
		{
			// If it's primitive, we simply output
			// as a sequence item: (:object rdf:_n primitive)
			StatementImpl s = new StatementImpl( new URIImpl( u ), p, ooo );
			addTriple( s );
		}
		else
		{
			// If it's a complex field, we'll output the sequence
			// item as a URI to the subgraph, then we'll create
			// the subgraph by recursing.
			StatementImpl s = new StatementImpl( new URIImpl( u ), p, new URIImpl( u + "_" + listCounter ) );
			addTriple( s );
			serialize( listItemObject, u + "_" + listCounter );
		}

		return listCounter + 1;
	}

	/**
	 * Checks whether the given object is a primitive type and, if so, will
	 * return a Node that encodes it. Otherwise NULL is returned.
	 * 
	 * @param o The object to check
	 * @return a Node or NULL
	 */
	private Value checkPrimitive( Object o )
	{
		if( o instanceof String ) return new LiteralImpl( o.toString() );

		if( o instanceof Integer ) return new ValueFactoryImpl().createLiteral( (Integer) o );

		if( o instanceof Float ) return new ValueFactoryImpl().createLiteral( (Float) o );

		if( o instanceof Double ) return new ValueFactoryImpl().createLiteral( (Double) o );

		if( o instanceof URI || o instanceof URL ) return new URIImpl( o.toString() );

		return null;
	}

	/**
	 * Returns a list of declared fields from the whole object tree.
	 * 
	 * @param o The object
	 * @return A list of fields
	 */
	private List<Field> getAllFields( Object o )
	{
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> objectToGetFieldsFrom = o.getClass();
		do
		{
			fields.addAll( (List<Field>) Arrays.asList( objectToGetFieldsFrom.getDeclaredFields() ) );
			objectToGetFieldsFrom = objectToGetFieldsFrom.getSuperclass();
		}
		while( !objectToGetFieldsFrom.getSimpleName().equals( "Object" ) );

		return fields;
	}

	/**
	 * Set whether to output class names as triples.
	 * 
	 * @param tf TRUE to output class name triples.
	 */
	public void setOutputClassNames( boolean tf )
	{
		this.outputClassNames = tf;
	}

	/**
	 * Set whether to attempt to output all fields from the objects, not just
	 * those annotated with {@link Predicate}.
	 * 
	 * @param tf TRUE to attempt to find predicates for all members.
	 */
	public void setAutoPredicate( boolean tf )
	{
		this.autoPredicate = tf;
	}

	/**
	 * Unserializes an object from the given RDF string (with the given format)
	 * into the given object.
	 * 
	 * @param objectToUnserialize The object to populate
	 * @param objectRootURI The URI that gives the root of the object graph
	 * @param rdf The RDF string
	 * @param rdfFormat The format of the RDF in the string
	 * @return The populated object or NULL if an error occurs
	 */
	public <T> T unserialize( T objectToUnserialize, String objectRootURI, String rdf, RDFFormat rdfFormat )
	{
		try
		{
			// We'll read the RDF into a memory store. So create that store
			// here.
			Repository repo = new SailRepository( new MemoryStore() );
			repo.initialize();

			// Read the RDF into the store
			RepositoryConnection connection = repo.getConnection();
			StringReader sr = new StringReader( rdf );
			String graphURI = "http://onto.arcomem.eu/tmp/";
			connection.add( sr, graphURI, rdfFormat );

			return unserialize( objectToUnserialize, objectRootURI, repo );
		}
		catch( RepositoryException e )
		{
			e.printStackTrace();
			return null;
		}
		catch( RDFParseException e )
		{
			e.printStackTrace();
			return null;
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Unserializes an object from an RDF graph that is rooted at the given URI.
	 * 
	 * @param objectToUnserialize The object to populate
	 * @param objectRootURI The URI that gives the root of the object graph
	 * @param repo The repository storing the RDF graph
	 * @return The populated object or NULL if an error occurs
	 */
	public <T> T unserialize( T objectToUnserialize, String objectRootURI, Repository repo )
	{
		// Can't do anything if the object is null
		if( objectToUnserialize == null )
		{
			System.err.println( "Unserialize error: given object is null" );
			return null;
		}

		try
		{
			RepositoryConnection connection = repo.getConnection();

			// Get the fields of the object's class
			Field[] fields = objectToUnserialize.getClass().getFields();

			// Loop through the fields
			for( Field field : fields )
			{
				URIImpl predicateName = getPredicateName( field, objectRootURI );

				if( predicateName != null )
				{
					// Query the RDF graph for the triples that represent this
					// field in the graph. If there are more than one, the first
					// will
					// be used.
					try
					{
						String queryString = "SELECT ?o WHERE {<" + objectRootURI + "> <" + predicateName + "> ?o.}";
						TupleQuery tupleQuery = connection.prepareTupleQuery( QueryLanguage.SPARQL, queryString );
						TupleQueryResult result = tupleQuery.evaluate();

						// We only want the first result
						if( result.hasNext() )
						{
							try
							{
								BindingSet bindingSet = result.next();
								Value objectValue = bindingSet.getValue( "o" );

								// We have a value for the field. Now what we do
								// with it depends on the field itself.
								field.set( objectToUnserialize, getFieldValue( 
										field.getGenericType(), objectValue, repo ) );
							}
							catch( IllegalArgumentException e )
							{
								e.printStackTrace();
							}
							catch( IllegalAccessException e )
							{
								e.printStackTrace();
							}
						}
						else
						{
							// RDF Graph did not have a value for the field
						}
					}
					catch( MalformedQueryException e )
					{
						e.printStackTrace();
					}
					catch( QueryEvaluationException e )
					{
						e.printStackTrace();
					}
				}
			}

			connection.close();
		}
		catch( RepositoryException e )
		{
			e.printStackTrace();
		}
		catch( SecurityException e )
		{
			e.printStackTrace();
		}

		return objectToUnserialize;
	}

	/**
	 * Set the field in the given object to the given value, if possible.
	 * 
	 * @param field The field.
	 * @param value The RDF value object for the field.
	 * @param repo The RDF Graph repository in use.
	 */
	private Object getFieldValue( Type fieldType, Value value, Repository repo )
	{
//		System.out.println( "For field " + fieldType + ", field value: " + value );

		try
		{
			if( fieldType.equals( String.class ) )
			{
				return value.stringValue();
			}
			else if( fieldType.equals( URI.class ) )
			{
				try
				{
					return new URI( value.toString() );
				}
				catch( URISyntaxException e )
				{
					e.printStackTrace();
				}
			}
			else if( fieldType.equals( URL.class ) )
			{
				try
				{
					return new URL( value.toString() );
				}
				catch( MalformedURLException e )
				{
					e.printStackTrace();
				}
			}
			else if( fieldType.equals( Integer.class ) || fieldType.equals( int.class ) )
			{
				return Integer.parseInt( value.stringValue() );
			}
			else if( fieldType.equals( Double.class ) || fieldType.equals( double.class ) )
			{
				return Double.parseDouble( value.stringValue() );
			}
			else if( fieldType.equals( Float.class ) || fieldType.equals( float.class ) )
			{
				return Float.parseFloat( value.stringValue() );
			}
			else
			{
				// The object is not a default type that we understand.
				// So what we must do is try to instantiate the object,
				// then attempt to deserialize that object, then set the field
				// in this object.
				try
				{
					// Try and look up the className predicate of the object.
					Type type = getObjectClass( value.stringValue(), repo, fieldType );
					
					// Attempt to instantiate the new object.
					// This may fail if the object does not have a
					// default or accessible constructor.
					Object newInstance = ((Class<?>)type).newInstance();

					// If we have a collection object, then we can do something
					// a bit different here. We know it's a collection, so we
					// simply iterate through the sequence getting each item in
					// turn and deserialize it.
					if( newInstance instanceof Collection )
					{
						@SuppressWarnings( "unchecked" )
						Collection<Object> collection = (Collection<Object>) newInstance;

						Object[] seq = getSequenceObjects( value.stringValue(), 
								repo, ((ParameterizedType)fieldType).getActualTypeArguments()[0] );
						for( Object o : seq )
							collection.add( o );

						return collection;
					}
					else
					// Same goes for if it's an array.
					if( ((Class<?>)type).isArray() )
					{
						Object[] seq = getSequenceObjects( value.stringValue(), 
								repo, ((Class<?>)type).getComponentType() );
						return seq;
					}
					// If we don't know what it is, we'll treat it as
					// an unknown (RDF) serializable object and recurse
					else
					{
						// Now recurse the unserialization down the object tree,
						// by attempting to unserialize the given object.
						this.unserialize( newInstance, value.toString(), repo );
					}

					return newInstance;
				}
				catch( InstantiationException e )
				{
					e.printStackTrace();
				}
			}
		}
		catch( IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns a list of objects that have been deserialised from an RDF.Seq
	 * sequence with the given type.
	 * 
	 * @return A list of objects.
	 */
	private Object[] getSequenceObjects( String sequenceURI, Repository repo, Type fieldType )
	{
		// This will be the output of the method - an array of
		// all the objects in order. May contain nulls.
		Object[] sequence = null;

		// We'll get all the results into this map to start with.
		// It maps an index (in the sequence) to the binding set from the query
		HashMap<Integer, BindingSet> tmpMap = new HashMap<Integer, BindingSet>();

		try
		{
			int max = -1;

			RepositoryConnection c = repo.getConnection();

			String queryString = "SELECT ?p ?o WHERE {<" + sequenceURI + "> ?p ?o} ORDER BY DESC(?p)";
			TupleQuery tupleQuery = c.prepareTupleQuery( QueryLanguage.SPARQL, queryString );
			TupleQueryResult result = tupleQuery.evaluate();

			while( result.hasNext() )
			{
				try
				{
					BindingSet bs = result.next();

					// If the predicate is a sequence number, then we parse the
					// integer into the index variable. If it's not a
					// NumberFormatException is thrown (and caught)
					int index = Integer.parseInt( bs.getValue( "p" ).stringValue()
							.substring( "http://www.w3.org/1999/02/22-rdf-syntax-ns#_".length() ) );

					// Just be sure we're doing something sensible.
					if( index >= 0 )
					{
						// Stick it in the map.
						tmpMap.put( index, bs );

						// Store the maximum index
						max = Math.max( index, max );
					}
				}
				catch( NumberFormatException e )
				{
					// If we get a NFE then it's probably not a sequence number.
				}
				catch( StringIndexOutOfBoundsException e )
				{
					// If we get a SOOBE then it's probably because the predicate
					// is not a sequence number.
				}
			}

			// So we've processed and stored all the results. Now we need to
			// make sure our output array is big enough for all the results.
			sequence = new Object[max];

			// Now loop through all the values and poke them into the array.
			// Note that we convert the indices to 0-based (RDF.Seq are 
			// 1-based indices).
			for( int i : tmpMap.keySet() )
				sequence[i-1] = getFieldValue( fieldType, tmpMap.get( i ).getValue( "o" ), repo );
		}
		catch( RepositoryException e )
		{
			e.printStackTrace();
		}
		catch( MalformedQueryException e )
		{
			e.printStackTrace();
		}
		catch( QueryEvaluationException e )
		{
			e.printStackTrace();
		}

		return sequence;
	}

	/**
	 * Attempts to find the correct class for the object URI given. If a class
	 * name cannot be found in the repository, then the field is used to attempt
	 * to instantiate a class.
	 * 
	 * @param objectURI The URI of the object in the repo
	 * @param repo The RDF repository
	 * @param field The fallback field
	 * @return A class object.
	 */
	private Type getObjectClass( String objectURI, Repository repo, Type fieldType )
	{
		try
		{
			RepositoryConnection c = repo.getConnection();

			String queryString = "SELECT ?o WHERE {<" + objectURI + "> <" + RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME + "> ?o.}";
			TupleQuery tupleQuery = c.prepareTupleQuery( QueryLanguage.SPARQL, queryString );
			TupleQueryResult result = tupleQuery.evaluate();

			// We'll look at all the results until we find a class we can
			// instantiate. Of course, we expect there to be only one in
			// reality.
			Class<?> clazz = null;
			boolean found = false;
			while( !found && result.hasNext() )
			{
				Value value = result.next().getValue( "o" );

				try
				{
					// Try to find the class with the given name
					clazz = Class.forName( value.stringValue() );

					// If the above succeeds, then we are done
					found = true;
				}
				catch( ClassNotFoundException e )
				{
					e.printStackTrace();
				}
			}

			// Close the repo connection.
			c.close();

			// Return the class if we have one.
			if( clazz != null ) return clazz;
		}
		catch( RepositoryException e )
		{
			e.printStackTrace();
		}
		catch( MalformedQueryException e )
		{
			e.printStackTrace();
		}
		catch( QueryEvaluationException e )
		{
			e.printStackTrace();
		}

		// Can't determine a class from the repository? Then we'll fall back
		// to the field's type.
		return fieldType;
	}

	/**
	 * Adds a single triple to some RDF serializer.
	 * 
	 * @param t The triple to add
	 */
	public void addTriple( Statement t )
	{
		// Default implementation does nothing. Subclasses should override
		// this method and do something useful with created triples.
		// This method is not abstract just so users can create this object
		// for unserialization.
	}
}
