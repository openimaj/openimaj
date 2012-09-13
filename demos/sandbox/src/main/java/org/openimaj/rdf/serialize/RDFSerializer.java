/**
 * 
 */
package org.openimaj.rdf.serialize;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.vocabulary.XSD;

/**
 *	The RDFSerializer is used to serialise an object to RDF. It will serialise
 *	the object deeply. This class itself does not output any specific RDF
 *	representation but generates triples which is gives to the abstract
 *	method {@link #addTriple(Statement)}. This method must be overridden in
 *	a subclass to provide the actual representation output.
 *	<p>
 *	For example, to output to Turtle, you might use the OpenRDF TurtleWriter
 *	to form a representation of the RDF graph:
 *	<p><code><pre>
 *		StringWriter sw = new StringWriter();
 *		final TurtleWriter tw = new TurtleWriter( sw );
 *		RDFSerializer rs = new RDFSerializer()
 *		{
 *			public void addTriple( Statement s )
 *			{
 *				tw.handleStatement( s );
 *			}
 *		};
 *		rs.serialize( myObject );
 *		System.out.println( sw.toString() );
 *	</pre></code>
 *	<p>
 *	By default the class will only produce triples for field which have been
 *	annotated with the {@link Predicate} annotation. The annotation gives the URI
 *	used to link the object to its field in the triple. If you wish to attempt to
 *	serialise unannotated fields, then you should use the constructor that
 *	takes a boolean, passing true: {@link #RDFSerializer(boolean)}. This will
 *	then create predicates based on the field name, so field "name" will become
 *	predicate "hasName". Complex fields are serialised into subgraphs and those
 *	graphs have URIs automatically generated for them.
 *	<p>
 *	The {@link #serialize(Object, String)} method requires the URI of the 
 *	object to be serialised. This must be decided by the caller and passed in.
 *	It is used to construct URIs for complex fields and predicates (if not given).
 *	So, an object with URI <code>http://example.com/object</code> and a complex field
 *	<code>name</code> will end up with a triple that links the object to a subgraph
 *	representing the complex object as so:
 *	<code><pre>
 *		http://example.com/object :hasName http://example.com/object_name
 *	</pre></code>
 *	The name of the subgraph is based on the URI of the object and the name of
 *	the field. The predicate is automatically generated from the name of the field
 *	also. Note that this means you may need to be careful about the names of the
 *	fields. For example, if the object had a complex field <code>name_first</code> 
 *	and also had a complex field <code>name</code> that had a complex field
 *	<code>first</code> it's possible the same URI may be generated for separate
 *	subgraphs.  
 *	<p>
 *	Primitive types will be typed with XSD datatypes.
 *	<p>
 *	Lists and collections are output in the same way. They are encoded using RDF
 *	sequences; that is as subgraphs where the items have the predicates
 *	<code>rdf:_1, rdf:_2..., rdf:_n</code> and the type <code>rdf:Seq</code>.
 *	<p>
 *	By default the serialisation will also output a triple that gives the class name
 *	of the object which is being serialised. If you do not want this, use the
 *	{@link #setOutputClassNames(boolean)} to turn it off. 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Sep 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class RDFSerializer
{
	/** Predicate for giving the class name */
	public static final String RDF_OPENIMAJ_P_CLASSNAME = 
			"http://rdf.openimaj.org/hasClassName/";
	
	/** Whether to try to create predicates for unannotated fields */
	protected boolean autoPredicate = false;
	
	/** Whether to output class names */
	protected boolean outputClassNames = true;
	
	/**
	 * 	Default constructor
	 */
	public RDFSerializer()
	{
		this( false );
	}
	
	/**
	 * 	Constructor that determines whether to create predicates
	 * 	automatically when the {@link Predicate} annotation does not
	 * 	exist.
	 * 
	 *	@param autoPredicate Whether to automatically create predicates
	 */
	public RDFSerializer( boolean autoPredicate )
	{
		this.autoPredicate = autoPredicate;
	}
	
	/**
	 * 	Serialize the given object as RDF.
	 * 
	 *	@param objectToSerialize The object to serialize.
	 * 	@param uri The URI of the object to serialize. 
	 */
	public void serialize( Object objectToSerialize, String uri )
	{
		// The subject (the object to serialize) won't change, so
		// we'll just create the URI node once.
		URIImpl subject = new URIImpl( uri );
		
		// Output the class name of the object to serialise
		if( outputClassNames )
			addTriple( new StatementImpl( subject,
					new URIImpl( RDF_OPENIMAJ_P_CLASSNAME ),
					checkPrimitive( objectToSerialize.getClass().getName() ) 
			) );
		
		// Get all the fields
		List<Field> fields = getAllFields( objectToSerialize );
		
		// Loop through the fields and output them
		for( Field field : fields )
		{
//			System.out.println( "====== Field "+field+" ============");
			
			try
			{
				// Get the value of the field
				field.setAccessible( true );
				Object oo = field.get( objectToSerialize );
	
				// If it's not null, we'll output it
				if( oo != null )
				{
					// Get the predicate annotation, if there is one
					Predicate predicateAnnotation = 
							field.getAnnotation( Predicate.class );

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
						if( autoPredicate )
							predicate = new URIImpl( uri+"_has"+
									field.getName().substring(0,1).toUpperCase()+
									field.getName().substring(1) );
					}
					
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
							// so that we can deal with it as a collection below.
							if( oo.getClass().isArray() )
							{
								object = new URIImpl( uri+"_"+field.getName() );
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
								object = new URIImpl( uri+"_"+field.getName() );
								int count = 1;
								for( Object o : (Collection<?>)obj )
									count = processLoop( uri, field, count, o );
							}
							else
							{
								// Try to serialise the object
								object = new URIImpl( uri+"_"+field.getName() );
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
				System.out.println( "Error reflecting "+field );
				e.printStackTrace();
			}
		}
	}

	/**
	 * 	A method that's called during the processing of a list of items
	 * 	to write a single item.
	 * 
	 *	@param objectURI The uri of the current object which contains the list
	 *	@param field The field being processed
	 *	@param listCounter The current counter in the list (1-based index)
	 *	@param listItemObject The object to be serialised
	 *	@return the next counter in the list
	 */
	private int processLoop( String objectURI, Field field, 
			int listCounter, Object listItemObject )
	{
		// The URI of the list object
		String u = objectURI+"_"+field.getName();
		URIImpl p = new URIImpl( RDF.NAMESPACE+"_"+listCounter );
		
		// If we're dealing with the first item in the list, we'll
		// put the RDF type (sequence) into the output
		if( listCounter == 1 )
			addTriple( new StatementImpl( 
					new URIImpl(u), RDF.TYPE, RDF.SEQ ) );
		
		// If the values in the collection are primitives
		// we will simply add them in a list, otherwise
		// we'll attempt to serialise them separately.
		Value ooo;
		if( (ooo = checkPrimitive( listItemObject )) != null )
		{
			// If it's primitive, we simply output
			// as a sequence item: (:object rdf:_n primitive)
			StatementImpl s = new StatementImpl( 
				new URIImpl(u), p, ooo );
			addTriple( s );
		}
		else
		{
			// If it's a complex field, we'll output the sequence
			// item as a URI to the subgraph, then we'll create
			// the subgraph by recursing.
			StatementImpl s = new StatementImpl( 
					new URIImpl(u), p, new URIImpl(u+"_"+listCounter) );
			addTriple(s);
			serialize( listItemObject, u+"_"+listCounter );
		}
		
		return listCounter+1;
	}
	
	/**
	 * 	Checks whether the given object is a primitive type and,
	 * 	if so, will return a Node that encodes it. Otherwise NULL is
	 * 	returned.
	 * 
	 *	@param o The object to check
	 *	@return a Node or NULL
	 */
	private Value checkPrimitive( Object o )
	{
		if( o instanceof String )
			return new LiteralImpl( o.toString() );

		if( o instanceof Integer )
			return new LiteralImpl( o.toString(), XSD.integer.getURI() );

		if( o instanceof Float )
			return new LiteralImpl( o.toString(), XSD.xfloat.getURI() );

		if( o instanceof Double )
			return new LiteralImpl( o.toString(), XSD.xdouble.getURI() );
	
		if( o instanceof URI || o instanceof URL )
			return new URIImpl( o.toString() );
	
//		System.err.println( "Cannot understand "+o+" ("+o.toString()+")");
		return null;
	}
	
	/**
	 * 	Returns a list of declared fields from the whole object tree.
	 *	@param o The object
	 *	@return A list of fields
	 */
	@SuppressWarnings( "unchecked" )
	private List<Field> getAllFields( Object o )
	{
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> objectToGetFieldsFrom = o.getClass();
		do
		{
			fields.addAll( (List<Field>)Arrays.asList( 
					objectToGetFieldsFrom.getDeclaredFields() ) );
			objectToGetFieldsFrom = objectToGetFieldsFrom.getSuperclass();
		}
		while( !objectToGetFieldsFrom.getSimpleName().equals("Object") );
			
		return fields;
	}
	
	/**
	 * 	Set whether to output class names as triples.
	 *	@param tf TRUE to output class name triples.
	 */
	public void setOutputClassNames( boolean tf )
	{
		this.outputClassNames = tf;
	}
	
	/**
	 * 	Set whether to attempt to output all fields from the objects, not
	 * 	just those annotated with {@link Predicate}.
	 *	@param tf TRUE to attempt to find predicates for all members.
	 */
	public void setAutoPredicate( boolean tf )
	{
		this.autoPredicate = tf;
	}
	
	/**
	 *	Adds a single triple to some RDF serializer. 
	 *	@param t The triple to add
	 */
	public abstract void addTriple( Statement t );
}
