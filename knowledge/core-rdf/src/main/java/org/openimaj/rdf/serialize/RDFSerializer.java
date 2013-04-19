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
package org.openimaj.rdf.serialize;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javassist.Modifier;

import org.openimaj.util.pair.IndependentPair;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
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
import org.springframework.core.GenericCollectionTypeResolver;

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
 * create predicates based on the field name, so field {@code member} will become
 * predicate {@code hasMember}. Complex fields are serialised into subgraphs and those
 * graphs have URIs automatically generated for them based on the URI of the object
 * in which they exist and their name. For example:
 * <code><pre>
 * 	http://example.com/MyObject
 * 		http://example.com/MyObject_hasMember
 * 		http://example.com/MyObject_member.
 * </pre></code>
 * <p>
 * The {@link #serialize(Object, String)} method requires the URI of the object
 * to be serialised. This must be decided by the caller and passed in. It is
 * used to construct URIs for complex fields and predicates (if not given). So,
 * an object with URI <code>http://example.com/object</code> and a complex field
 * called <code>field</code> will end up with a triple that links the object to a
 * subgraph representing the complex object as so: <code><pre>
 * 		http://example.com/object :hasField http://example.com/object_field
 * 	</pre></code>
 * The name of the subgraph is based on the URI of the object and the name of
 * the field. The predicate is also automatically generated from the name of the
 * field. Note that this means you may need to be careful about the names
 * of the fields. For example, if an object had a complex field
 * <code>name_first</code> but also had a complex field <code>name</code> that
 * itself had a complex field <code>first</code> it's possible the same URI may be
 * generated for both subgraphs.
 * <p>
 * Primitive types will be typed with XSD datatypes. For example:
 * <code><pre>
 * 	example:field example:hasNumber "20"^^xsd:integer
 * </pre></code>
 * <p>
 * Lists and collections are output in one of two ways. By default they are
 * output as separate triples in an unordered way:
 * <code><pre>
 * 		@Predicate("http://example.com/hasString")
 * 		String[] strings = new String[] { "one", "two" };
 * </pre></code>
 * ...will be output, by default, as:
 * <code><pre>
 * 	http://example.com/object
 * 			http://example.com/hasString "one";
 * 			http://example.com/hasString "two".
 * </pre></code>
 * <p>
 * Alternatively, the {@link RDFCollection} annotation can be used.
 * When this annotation is used, they are encoded using RDF sequences;
 * that is as subgraphs where the items have the predicates
 * <code>rdf:_1, rdf:_2..., rdf:_n</code> and the type <code>rdf:Seq</code>.
 * This retains the same order for the collection as when serialised.
 * So the above example would be output as:
 * <code><pre>
 *	http://example.com/object
 *		http://example.com/hasString http://example.com/strings .
 *	http://example.com/strings
 *		rdf:type	rdf:Seq;
 *		rdf:_1	"one";
 *		rdf:_2	"two".
 * </pre></code>
 * <p>
 * By default the serialisation will also output a triple that gives the class
 * name of the object which is being serialised. If you do not want this, use
 * the {@link #setOutputClassNames(boolean)} to turn it off, although this may
 * cause the deserialization to stop working, if the original fields in the
 * object are defined as non-concrete classes (e.g. List or Collection
 * rather than an ArrayList). In this case, the deserialiser attempts to look
 * for this triple to find the actual class that was serialised.
 * <p>
 * The {@link RelationList} annotation allows collections of independent pairs
 * of URI and Object can be sent to the RDF graph directly without validation.
 * This is not deserialisable as there's no way for the derserialiser to know
 * which triples belong in this collection.
 * <p>
 * The {@link TripleList} annotation allows members that are collections of
 * OpenRDF {@link Statement} objects to be sent to the RDF graph directly.
 * Again, this is not deserialisable as there's no way for the derserialiser to know
 * which triples belong in this collection.
 * <p>
 * This class also provides an unserialisation routine for converting an RDF
 * graph back into an object graph. Given a string that is an RDF representation
 * of a graph (serialised with the serialiser), it will return the object.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 11 Sep 2012
 * @version $Author$, $Revision$, $Date$
 */
public class RDFSerializer
{
	/** URI used for temporary graph when loading into a store */
	public static final String RDF_OPENIMAJ_TMP_GRAPH =
			"http://rdf.openimaj.org/tmp/";

	/** Predicate for giving the class name */
	public static final String RDF_OPENIMAJ_P_CLASSNAME
		= "http://rdf.openimaj.org/hasClassName/";

	/** Predicate for unnamed collections */
	public static final String RDF_OPENIMAJ_P_COLLECTIONITEM
		= "http://rdf.openimaj.org/hasCollectionItem/";

	/** Whether to try to create predicates for unannotated fields */
	protected boolean autoPredicate = false;

	/** Whether to output class names */
	protected boolean outputClassNames = true;

	/**
	 * During a serialization, this field contains all the graphs which have
	 * already been written, avoiding duplicate entries in the output as well as
	 * avoiding infinite loops when cycles occur
	 */
	private HashSet<URI> knownGraphs = null;

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
	public RDFSerializer( final boolean autoPredicate )
	{
		this.autoPredicate = autoPredicate;
	}

	/**
	 * Serialize the given object as RDF.
	 *
	 * @param objectToSerialize The object to serialize.
	 * @param uri The URI of the object to serialize.
	 * @return Returns the URI of the object (this may be different to the one
	 *         that's passed in)
	 */
	public URI serialize( final Object objectToSerialize, final String uri )
	{
		this.knownGraphs = new HashSet<URI>();
		final URI i = this.serializeAux( objectToSerialize, uri );
		return i;
	}

	/**
	 * Serialize the given object as RDF.
	 *
	 * @param objectToSerialize The object to serialize.
	 * @param uri The URI of the object to serialize.
	 * @return Returns the URI of the object (this may be different to the one
	 *         that's passed in)
	 */
	public URI serializeAux( final Object objectToSerialize, final String uri )
	{
		return this.serializeAux( objectToSerialize, uri, true );
	}

	/**
	 * Serialize the given object as RDF. This is specifically designed for
	 * calling by the process outputting collections.
	 *
	 * @param objectToSerialize The object to serialize.
	 * @param uri The URI of the object to serialize.
	 * @param outputCollectionObjects Whether to output items in a collection
	 * @return Returns the URI of the object (this may be different to the one
	 *         that's passed in)
	 */
	public URI serializeAux( final Object objectToSerialize, final String uri,
			final boolean outputCollectionObjects )
	{
		// The subject (the object to serialize) won't change, so
		// we'll just create the URI node once.
		URIImpl subject = new URIImpl( uri );

		// Find the object URI
		subject = this.getObjectURI( objectToSerialize, subject );

		// Check whether we've already serialized this object. If we have
		// we just return, otherwise we add it to our memory of serialized
		// objects so that we won't try again.
		if( this.knownGraphs.contains( subject ) )
			return subject;

		this.knownGraphs.add( subject );

		// Output the class name of the object to serialise
		if( this.outputClassNames )
			this.addTriple( new StatementImpl( subject, new URIImpl(
					RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME ), this
					.checkPrimitive( objectToSerialize.getClass().getName() ) ) );

		// Check whether there's a semantic type for this object
		final RDFType typeAnnotation = objectToSerialize.getClass()
				.getAnnotation( RDFType.class );

		// If there is a type anotation, add it as a triple in the graph.
		if( typeAnnotation != null )
			this.addTriple( new StatementImpl( subject, RDF.TYPE, new URIImpl(
					typeAnnotation.value() ) ) );

		// If this top-level object is a collection, we obviously
		// have no predicate for all the items in the collection,
		// so we will output them all linked to the subject URI by
		// way of the OpenIMAJ hasCollectionItem predicate. This functionality
		// can be disabled by passing in outputCollectionObjects as false,
		// which the processCollection() method does when serializing collections.
		// It does this because it will have already output the list items for
		// a collection object, but will have recursed here to allow the other
		// fields in the collection object to be serialized.
		if( objectToSerialize instanceof Collection && outputCollectionObjects )
		{
			this.processCollection( subject, new URIImpl(RDFSerializer.RDF_OPENIMAJ_P_COLLECTIONITEM),
					subject, "", objectToSerialize, false );

			// We will still carry on and serialize the other parts
			// of this object...
		}

		// Get all the fields
		final List<Field> fields = this.getAllFields( objectToSerialize );

		// Loop through the fields and output them one at a time
		for( final Field field : fields )
		{
			// We won't output static members
			if( Modifier.isStatic( field.getModifiers() ) )
				continue;

			// System.out.println( "====== Field "+field+" ============");

			try
			{
				// Get the value of the field
				field.setAccessible( true );
				final Object oo = field.get( objectToSerialize );

				// Special fields have annotations which mean they will be
				// output in some other way, as defined in the outputSpecial()
				// method. If this field is not one of those, we'll output
				// in the normal way.
				if( !this.outputSpecial( oo, field, subject ) )
				{
					// Get the predicate name (may be null if if cannot be
					// created either due to a lack of the @Predicate
					// annotation or because autoPredicate is false
					final URIImpl predicate = this.getPredicateName( field, uri );

					// If the predicate is null, we can't output this object.
					// Otherwise, we'll go ahead and output it.
					if( predicate != null )
						this.processObject( subject, predicate, field.getName(), oo,
							field.getAnnotation( RDFCollection.class ) != null );
				}
			}
			catch( final Exception e )
			{
				System.out.println( "Error reflecting " + field );
				e.printStackTrace();
			}
		}

		return subject;
	}

	/**
	 * 	Serialises a single object into the graph using the given
	 * 	subject and predicate in the triple.
	 *
	 *	@param subject The URI of the subject being serialised
	 *	@param predicate The URI of the predicate for this member
	 *	@param field The name of the field being serialised
	 *	@param oo The object being serialised
	 *	@param asCollection Whether the field should be output as
	 *		an {@link RDFCollection}
	 */
	private void processObject( final URIImpl subject, final URIImpl predicate,
			final String fieldName, final Object oo, final boolean asCollection )
	{
		// Get the URI of the subject (the object we're serialising)
		final String uri = subject.stringValue();

		// If the value of the object to output is not null, we go ahead
		// and serialise the object.
		if( oo != null && predicate != null )
		{
			// This will be the value of the object we're outputting.
			// It'll be a URI for complex objects.
			Value object;

			// This value will give whether we're outputting a collection object.
			// That also includes Arrays.
			boolean isCollective = false;

			// Check if we should output a primitive value. If so, we're done.
			// Otherwise, we'll need to do some more complex analysis...
			if( (object = this.checkPrimitive( oo )) == null )
			{
				// Get a URI for this object
				final URIImpl objectURI = this.getObjectURI(
						oo, new URIImpl(
							subject.stringValue() + "_" + fieldName ) );

				// If oo is an array, we'll call the processArray() method
				// to output it. The object becomes a URI to the array subgraph.
				if( oo.getClass().isArray() )
				{
					isCollective = true;
					object = this.processArray( subject, predicate, objectURI,
							fieldName, oo, asCollection );
				}
				else
				// If we have a collection of things, we'll output
				// them as an RDF linked-list. The object becomes a URI
				// to the collection's subgraph.
				if( oo instanceof Collection<?> )
				{
					isCollective = true;
					object = this.processCollection( subject, predicate, objectURI,
							fieldName, oo, asCollection );
				}
				// Not a primitive, array or collection?  Must be a
				// regular object, so we'll recurse this process with
				// the value of the field.
				else
				{
					// The URI is the uri of the subject concatenated
					// with the name of the field from which this value
					// was taken.
					object = new URIImpl( uri + "_"
							+ fieldName );

					// Here's the recursive call to the process
					object = this.serializeAux( oo, object.stringValue() );
				}
			}

			// We don't need to add this triple if the triples are a
			// are collection that's been output separately
			if( !isCollective || (isCollective && asCollection) )
			{
				// Create a triple and send it to the serializer
				final Statement t = new StatementImpl( subject,
						predicate, object );
				this.addTriple( t );
			}
		}
	}

	/**
	 * 	Processes an array object outputting triples for the entire array.
	 * 	Returns the Value linking to this array.
	 *
	 *	@param subject The URI of the object to serialise
	 *	@param predicate The predicate between the subject and this array
	 *	@param collectionURI The URI of the collection subgraph
	 *	@param field The field in the object that's the array
	 *	@param arrayObject The array object
	 *	@param asCollection Whether to output as an RDF Collection
	 *	@return The object linking to this array
	 */
	private Value processArray( final URIImpl subject, final URIImpl predicate,
			final URIImpl collectionURI, final String fieldName,
			final Object arrayObject, final boolean asCollection )
	{
		// Loop through all the array elements and output them separately.
		for( int count = 0; count < Array.getLength( arrayObject ); )
		{
			// Get the array element value..
			final Object o = Array.get( arrayObject, count );

			// Call the processListitemObject to output the actual value.
			// We call this method rather than the serializeAux() method because
			// we need to deal with the various methods for outputting collections
			// in one single place (e.g. as a collection, or an RDF sequence, etc.)
			count = this.processListItemObject( subject, predicate, collectionURI,
					count, o, asCollection );
		}

		return collectionURI;
	}

	/**
	 * 	Processes a collection object.
	 *
	 *	@param subject The URI of the object we're serializing
	 *	@param predicate The predicate for this collection
	 *	@param collectionURI The URI of the collection subgraph
	 *	@param field The field in the object that is the collection
	 *	@param collectionObject The collection object
	 *	@param asCollection Whether to output as an RDF collection
	 *	@return The object created for this collection
	 */
	private Value processCollection( final URIImpl subject, final URIImpl predicate,
			final URIImpl collectionURI, final String fieldName,
			final Object collectionObject, final boolean asCollection )
	{
		// Loop through all the collection items outputting them one at a time.
		int count = 1;
		for( final Object o : (Collection<?>) collectionObject )
		{
			// We call this method rather than the serializeAux() method because
			// we need to deal with the various methods for outputting collections
			// in one single place (e.g. as a collection, or an RDF sequence, etc.)
			count = this.processListItemObject( subject, predicate, collectionURI,
					count, o, asCollection );
		}

		// We also need to serialize the object itself because if
		// The collection is actually a subclass that contains
		// @Predicate annotations we need to go ahead and
		// serialise those too; so we recurse here.
		final Value object = this.serializeAux( collectionObject,
				collectionURI.stringValue(), false );

		return object;
	}

	/**
	 * A method that's called during the processing of a list of items to write
	 * a single item.
	 *
	 * @param subject The URI of the object in which this collection exists
	 * @param predicate The predicate of the collection in the original graph
	 * @param collectionURI The URI of the collection in the graph
	 * @param listCounter The current counter in the list (1-based index)
	 * @param listItemObject The object to be serialised
	 * @param asSequence Whether to output as an RDF Sequence
	 * 			or as individual triples
	 * @return the next counter in the list
	 */
	private int processListItemObject(
			URIImpl subject, URIImpl predicate,
			final URIImpl collectionURI,
			final int listCounter, final Object listItemObject,
			final boolean asSequence )
	{
		// If we're outputting as a sequence, then we must
		// alter the predicate and therefore the subject
		if( asSequence )
		{
			// If we're outputting as an RDF sequence the predicate
			// becomes the rdf:_n counter predicate and the subject
			// will be the URI of the collection
			predicate = new URIImpl( RDF.NAMESPACE + "_" + listCounter );
			subject = collectionURI;
		}

		// Check whether the list item is a primitive. If it is, its
		// value will be output directly into the collection, otherwise
		// we need to output a subgraph URI instead.
		Value oo = null;
		oo = this.checkPrimitive( listItemObject );

		// If list item isn't a primitive, get a URI for it.
		if( oo == null )
		{
			// Get the URI for the list item object.
			oo = this.getObjectURI( listItemObject,
				new URIImpl( collectionURI.stringValue()
						+ "_listItem"+listCounter ) );

			// We're here because the list item is not a primitive - it's a
			// complex object or a collection; in which case we must
			// link to a subgraph and output that subgraph.
			this.addTriple( new StatementImpl( subject, predicate, oo ) );

			// Now we serialize the object into the graph
			this.serializeAux( listItemObject, oo.stringValue() );
		}
		// Output the primitive triple
		else
		{
			this.addTriple( new StatementImpl( subject, predicate, oo ) );
		}

		return listCounter + 1;
	}

	/**
	 * Returns a predicate name for the given field.
	 *
	 * @param field The field
	 * @param uri The URI of the object
	 * @return A predicate URI, either generated from the @Predicate annotation
	 *         or from the field name
	 */
	private URIImpl getPredicateName( final Field field, final String uri )
	{
		// Get the predicate annotation, if there is one
		final Predicate predicateAnnotation = field
				.getAnnotation( Predicate.class );

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
			if( this.autoPredicate )
				predicate = new URIImpl( uri + "_has"
						+ field.getName().substring( 0, 1 ).toUpperCase()
						+ field.getName().substring( 1 ) );
		}

		return predicate;
	}

	/**
	 * Checks whether the given object is a primitive type and, if so, will
	 * return a Node that encodes it. Otherwise NULL is returned.
	 *
	 * @param o The object to check
	 * @return a Node or NULL
	 */
	private Value checkPrimitive( final Object o )
	{
		if( o instanceof String ) return new LiteralImpl( o.toString() );

		if( o instanceof Integer )
			return new ValueFactoryImpl().createLiteral( (Integer) o );

		if( o instanceof Float )
			return new ValueFactoryImpl().createLiteral( (Float) o );

		if( o instanceof Double )
			return new ValueFactoryImpl().createLiteral( (Double) o );

		if( o instanceof URI || o instanceof URL || o instanceof java.net.URI )
			return new URIImpl( o.toString() );

		return null;
	}

	/**
	 * Returns a list of declared fields from the whole object tree.
	 *
	 * @param o The object
	 * @return A list of fields
	 */
	private List<Field> getAllFields( final Object o )
	{
		final ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> objectToGetFieldsFrom = o.getClass();
		do
		{
			fields.addAll( Arrays.asList( objectToGetFieldsFrom
					.getDeclaredFields() ) );
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
	public void setOutputClassNames( final boolean tf )
	{
		this.outputClassNames = tf;
	}

	/**
	 * Set whether to attempt to output all fields from the objects, not just
	 * those annotated with {@link Predicate}.
	 *
	 * @param tf TRUE to attempt to find predicates for all members.
	 */
	public void setAutoPredicate( final boolean tf )
	{
		this.autoPredicate = tf;
	}

	/**
	 * Unserializes an object from the given RDF string (with the given format)
	 * into the given object.
	 *
	 * @param <T> Type of object being unserialised
	 *
	 * @param objectToUnserialize The object to populate
	 * @param objectRootURI The URI that gives the root of the object graph
	 * @param rdf The RDF string
	 * @param rdfFormat The format of the RDF in the string
	 * @return The populated object or NULL if an error occurs
	 */
	public <T> T unserialize( final T objectToUnserialize,
			final String objectRootURI, final String rdf,
			final RDFFormat rdfFormat )
	{
		try
		{
			// We'll read the RDF into a memory store. So create that store
			// here.
			final Repository repo = new SailRepository( new MemoryStore() );
			repo.initialize();

			// Read the RDF into the store
			final RepositoryConnection connection = repo.getConnection();
			final StringReader sr = new StringReader( rdf );
			final String graphURI = RDFSerializer.RDF_OPENIMAJ_TMP_GRAPH;
			connection.add( sr, graphURI, rdfFormat );

			// Now unserialize the object
			return this.unserialize( objectToUnserialize, objectRootURI, repo );
		}
		catch( final RepositoryException e )
		{
			e.printStackTrace();
			return null;
		}
		catch( final RDFParseException e )
		{
			e.printStackTrace();
			return null;
		}
		catch( final IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Unserializes an object from an RDF graph that is rooted at the given URI.
	 *
	 * @param <T> Type of object being unserialised
	 *
	 * @param objectToUnserialize The object to populate
	 * @param objectRootURI The URI that gives the root of the object graph
	 * @param repo The repository storing the RDF graph
	 * @return The populated object or NULL if an error occurs
	 */
	public <T> T unserialize( final T objectToUnserialize,
			final String objectRootURI, final Repository repo )
	{
		// Can't do anything if the object is null
		if( objectToUnserialize == null )
		{
			System.err.println( "Unserialize error: given object is null" );
			return null;
		}

		// If our starting object is a collection, then there will be no
		// predicate for us to work with. So we'll get the items from the
		// collection using the statically defined predicate
		// RDF_OPENIMAJ_P_COLLECTIONITEM. We will still need to deserialise
		// the object after this, in case the collection has any
		// predicated members that need to be deserialised.
		if( objectToUnserialize instanceof Collection<?> )
			this.extractCollectionDirect( (Collection<?>)objectToUnserialize,
					objectRootURI, repo );

		try
		{
			final RepositoryConnection connection = repo.getConnection();

			// Get the fields of the object's class
			final Field[] fields = objectToUnserialize.getClass().getFields();

			// Loop through the fields
			for( final Field field : fields )
			{
//				System.out.println( "=========== Field "+field.getName()+" ============= ");

				try
				{
					// Get the name of the predicate for this field
					final URIImpl predicateName = this.getPredicateName( field,
							objectRootURI );

					// If we can't determine a predicate, we don't unserialize it
					if( predicateName != null )
					{
						// If it's a collection, we'll do something special
						if( Collection.class.isAssignableFrom( field.getType() ) )
						{
							this.unserializeCollection( field, objectToUnserialize,
									objectRootURI, repo, predicateName );
						}
						else
						// Same goes for if it's an array.
						if( ((Class<?>) field.getType()).isArray() )
						{
							this.unserializeArray( objectToUnserialize,
									objectRootURI, repo, field, predicateName );
						}
						// If we don't know what it is, we'll treat it as
						// an unknown (RDF) serializable object and recurse
						else
						{
							// Query the RDF graph for the triples that represent
							// this field in the graph. If there are more
							// than one, the first will be used.
							try
							{
								final String queryString = "SELECT ?o WHERE {<"
										+ objectRootURI + "> <" + predicateName
										+ "> ?o.}";
								final TupleQuery tupleQuery = connection
										.prepareTupleQuery( QueryLanguage.SPARQL,
												queryString );
								final TupleQueryResult result = tupleQuery
										.evaluate();

								// We only want the first result because we know
								// it's not a collection
								if( result.hasNext() )
								{
									try
									{
										final BindingSet bindingSet = result.next();
										final Value objectValue = bindingSet
												.getValue( "o" );

										// We have a value for the field. Now what
										// we do with it depends on the field itself.
										field.setAccessible( true );
										field.set( objectToUnserialize,
											this.getFieldValue(
												field.getGenericType(),
												objectValue, repo,
												field.getName(),
												objectRootURI ) );
									}
									catch( final IllegalArgumentException e )
									{
										e.printStackTrace();
									}
									catch( final IllegalAccessException e )
									{
										e.printStackTrace();
									}
								}
								else
								{
									// RDF Graph did not have a value for the field
								}
							}
							catch( final MalformedQueryException e )
							{
								e.printStackTrace();
							}
							catch( final QueryEvaluationException e )
							{
								e.printStackTrace();
							}
						}
					}
				}
				catch( final IllegalArgumentException e )
				{
					e.printStackTrace();
				}
				catch( final IllegalAccessException e )
				{
					e.printStackTrace();
				}
			}

			connection.close();
		}
		catch( final RepositoryException e )
		{
			e.printStackTrace();
		}
		catch( final SecurityException e )
		{
			e.printStackTrace();
		}

		return objectToUnserialize;
	}

	/**
	 *	@param objectToUnserialize
	 *	@param objectRootURI
	 *	@param repo
	 */
	@SuppressWarnings( "unchecked" )
	private <T extends Collection<?>> void extractCollectionDirect(
			final T objectToUnserialize,
			final String objectRootURI, final Repository repo )
	{
		final Class<?> collectionType =
				GenericCollectionTypeResolver.getCollectionType(
						objectToUnserialize.getClass() );

		// TODO: This needs to be sorted out. We can't pass null.
		// Unserialize the collection items.
		final Object[] seq = this.extractCollectionObjects(
				objectRootURI, repo, collectionType,
				"", objectRootURI,
				RDFSerializer.RDF_OPENIMAJ_P_COLLECTIONITEM );

		((Collection<Object>)objectToUnserialize).clear();
		for( int i = 0; i < seq.length; i++ )
			((Collection<Object>)objectToUnserialize).add( seq[i] );
	}

	/**
	 * 	Unserializes an array object from the graph.
	 *
	 *	@param objectToUnserialize The object in which there is an array field
	 *	@param objectRootURI The URI of the object in which there is an array field
	 *	@param repo The repository containing the RDF graph
	 *	@param field The field that is the array
	 *	@param predicateName The name of the predicate for the array items
	 *	@throws IllegalAccessException If the field cannot be set
	 */
	private <T> void unserializeArray( final T objectToUnserialize,
			final String objectRootURI, final Repository repo, final Field field,
			final URIImpl predicateName ) throws IllegalAccessException
	{
		final Class<?> componentType = field.getType().getComponentType();

		// Go get all the array objects
		@SuppressWarnings( "unchecked" )
		final T[] seq = (T[])this
				.extractCollectionObjects( objectRootURI, repo,
						componentType, field.getName(),
						objectRootURI, predicateName
								.stringValue() );

		// Set the field up
		field.setAccessible( true );
		field.set( objectToUnserialize, seq );
	}

	/**
	 * 	Unserializes a collection object from the graph. This method is mainly
	 * 	dealing with setting up the appropriate collection instance before calling
	 * 	{@link #extractCollectionObjects(String, Repository, Class, Field, String, String)}
	 * 	to actually get the objects.
	 *
	 *	@param field The field which is the collection
	 *	@param objectToUnserialize The object in which the field exists
	 *	@param objectRootURI The URI of the object in which the field exists
	 *	@param repo The repository containing the RDF graph
	 *	@param predicateURI The name of the predicate for the collection items.
	 */
	private void unserializeCollection( final Field field,
			final Object objectToUnserialize, final String objectRootURI,
			final Repository repo, final URIImpl predicateURI )
	{
		// If we have a collection object, then we can do something
		// a bit different here. We know it's a collection, so we
		// simply iterate through the sequence getting each item in
		// turn and deserialize it.
		if( Collection.class.isAssignableFrom( field.getType() ) )
		{
			try
			{
				// We get the class from the object that we're populating
				Class<?> cls = field.getType();

				// Attempt to instantiate the new object.
				// This may fail if the object does not have a
				// default or accessible constructor. In which case
				// we'll ignore the object here.
				Object newInstance;
				try
				{
					newInstance = cls.newInstance();
				}
				catch( final InstantiationException e )
				{
					cls = (Class<?>)this.getObjectClass(
						objectRootURI+"_"+field.getName(), repo );

					// If we can't get a class to instantiate,
					// we cannot do anything. Probably the field was null.
					if( cls == null ) return;

					newInstance = cls.newInstance();
				}

				// Cast to a collection
				@SuppressWarnings( "unchecked" )
				final Collection<Object> collection =
						(Collection<Object>) newInstance;

				// We must clear the collection here. We will populate
				// it will everything that was in it when it was created,
				// and if the constructor adds stuff to the collection,
				// this will end up with a collection that is not the same
				// as the original.
				collection.clear();

				// Get the collection of the type
				Class<?> collectionType = null;
				collectionType = GenericCollectionTypeResolver
						.getCollectionFieldType( field );

				// Now unserialise the collection.
				final Object[] seq = this.extractCollectionObjects(
						objectRootURI, repo, collectionType,
						field.getName(), objectRootURI, predicateURI.stringValue() );

				// If we have some stuff, then put it into the field.
				if( seq != null )
				{
					// Add all the extracted objects into the collection
					for( int i = 0; i < seq.length; i++ )
						collection.add( seq[i] );

					// Set the field value to the new collection
					field.setAccessible( true );
					field.set( objectToUnserialize, collection );
				}
			}
			catch( final SecurityException e )
			{
				e.printStackTrace();
			}
			catch( final IllegalArgumentException e )
			{
				e.printStackTrace();
			}
			catch( final InstantiationException e1 )
			{
				e1.printStackTrace();
			}
			catch( final IllegalAccessException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println( "WARNING: Unserialize collection called for" +
					" something that's not a collection.");
		}
	}

	/**
	 * Returns an object extracted from the OpenRDF {@link Value} for this field.
	 * If it's a primitive type, the object will be that primitive type.
	 * Otherwise, the object will be deserialised and its reference passed back.
	 *
	 * @param type The type of the field
	 * @param value The RDF value object for the field.
	 * @param repo The RDF Graph repository in use.
	 * @param fieldName The field name
	 * @param subjectURI The URI of the object
	 * @return The deserialised object.
	 */
	private Object getFieldValue( final Type type, final Value value,
			final Repository repo, final String fieldName,
			final String subjectURI )
	{
		try
		{
			// ---- String value ----
			if( type.equals( String.class ) )
			{
				return value.stringValue();
			}
			// ---- URI or URL values ----
			else if( type.equals( java.net.URI.class ) )
			{
				try
				{
					return new java.net.URI( value.toString() );
				}
				catch( final URISyntaxException e )
				{
					e.printStackTrace();
				}
			}
			// ---- URI or URL values ----
			else if( type.equals( URL.class ) )
			{
				try
				{
					return new URL( value.toString() );
				}
				catch( final MalformedURLException e )
				{
					e.printStackTrace();
				}
			}
			// ---- Integer values ----
			else if( type.equals( Integer.class )
					|| type.equals( int.class ) )
			{
				return Integer.parseInt( value.stringValue() );
			}
			// ---- Double values ----
			else if( type.equals( Double.class )
					|| type.equals( double.class ) )
			{
				return Double.parseDouble( value.stringValue() );
			}
			// ---- Float values ----
			else if( type.equals( Float.class )
					|| type.equals( float.class ) )
			{
				return Float.parseFloat( value.stringValue() );
			}
			// ---- Other complex objects ----
			else
			{
				// The object is not a default type that we understand.
				// So what we must do is try to instantiate the object,
				// then attempt to deserialize that object, then set the field
				// in this object.
				try
				{
					if( value instanceof URI )
					{
						String objectURI = value.stringValue();

						// Try and look up the className predicate of the
						// object.
						Type type2 = this.getObjectClass( objectURI, repo );
						if( type2 == null )
							type2 = this.getObjectClass( objectURI = subjectURI
									+ "_" + fieldName, repo );

						// Attempt to instantiate the new object.
						// This may fail if the object does not have a
						// default or accessible constructor.
						final Object newInstance = ((Class<?>) type2)
								.newInstance();

						// Now recurse the unserialization down the object tree,
						// by attempting to unserialize the given object.
						return this.unserialize( newInstance, objectURI, repo );
					}
					else
					{
						System.out
								.println( "WARNING: I don't know what to do with "
										+ value );
					}
				}
				catch( final InstantiationException e )
				{
					e.printStackTrace();
				}
			}
		}
		catch( final IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		catch( final IllegalAccessException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 	Returns whether the collection given by collectionURI is an RDF sequence
	 * 	or not. It does this by asking the SPARQL store whether the URI has the
	 * 	type rdf:Seq.
	 *
	 *	@param repo The repository containing the graph
	 *	@param collectionURI The URI of the collection to check
	 *	@return TRUE if the collection is of type rdf:Seq; FALSE otherwise or
	 *		if an error occurs.
	 */
	private boolean isRDFSequence( final Repository repo, final String collectionURI )
	{
		// Before we retrieve the objects from the sequence, we'll first
		// double check that it really is a sequence. If it's not (it's a
		// collection of unordered triples), then we'll treat it differently.
		try
		{
			final RepositoryConnection c = repo.getConnection();
			final String queryString = "ASK {<" + collectionURI + "> <"
					+ RDF.TYPE + "> <" + RDF.SEQ + ">}";
			final BooleanQuery query = c.prepareBooleanQuery(
					QueryLanguage.SPARQL, queryString );
			return query.evaluate();
		}
		catch( final RepositoryException e1 )
		{
			e1.printStackTrace();
		}
		catch( final MalformedQueryException e1 )
		{
			e1.printStackTrace();
		}
		catch( final QueryEvaluationException e1 )
		{
			e1.printStackTrace();
		}

		return false;
	}

	/**
	 * 	Returns a list of objects that have been deserialised from an unordered
	 * 	collection or an rdf:Seq in the graph. A test is made to determine which
	 * 	type the collection is, and it will be dealt with in the appropriate way.
	 * 	The method, if it succeeds, always returns an array of objects.
	 *
	 *	@param collectionURI The URI of the collection from which to get items
	 *	@param repo The repository containing the RDF graph
	 *	@param componentType The Java type of each component in the graph
	 *	@param field The collection field to be set
	 *	@param subject The URI of the object in which the collection is a member
	 *	@param predicate The predicate that maps the collection to the object
	 *	@return An array of deserialised objects
	 */
	@SuppressWarnings( "unchecked" )
	private <T> T[] extractCollectionObjects( final String collectionURI,
			final Repository repo, final Class<T> componentType,
			final String fieldName,	final String subject, final String predicate )
	{
		// This will be the output of the method - an array of
		// all the objects in order. May contain nulls.
		T[] sequence = null;

		// Check whether the collection is a sequence. If it is, then we
		// can get the collection of objects and put them into an appropriate
		// array
		if( this.isRDFSequence( repo, collectionURI ) )
		{
			// We'll get all the results into this map to start with.
			// It maps an index (in the sequence) to the binding set from the query
			final HashMap<Integer, BindingSet> tmpMap =
					new HashMap<Integer, BindingSet>();

			try
			{
				// Extract the objects from the RDF sequence
				final int max = this.extractRDFSequenceObjects(
						collectionURI, repo, tmpMap );

				// If there was no sequence object, we'll return
				if( max < 0 )
					return null;

				// So we've processed and stored all the results. Now we need to
				// make sure our output array is big enough for all the results.
				sequence = (T[]) Array.newInstance( componentType, max );

				// Now loop through all the values and poke them into the array.
				// Note that we convert the indices to 0-based (RDF.Seq are
				// 1-based indices).
				for( final int i : tmpMap.keySet() )
					sequence[i-1] = (T)this.getFieldValue(
							componentType,
							tmpMap.get( i ).getValue( "o" ),
							repo, fieldName,
							collectionURI );
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
		}
		else
		{
			sequence = this.getUnorderedObjects(
					collectionURI, repo, componentType,
					fieldName, subject, predicate );
		}

		return sequence;
	}

	/**
	 * 	Extracts a set of objects from an RDF sequence and returns a map
	 * 	containing the objects mapped to their index.
	 *
	 *	@param collectionURI The URI of the collection to extract
	 *	@param repo The repository containing the RDF graph
	 *	@param objectMap The map of the sequence objects
	 *	@return The maximum index of a sequence object extracted
	 *	@throws RepositoryException
	 *	@throws MalformedQueryException
	 *	@throws QueryEvaluationException
	 */
	private int extractRDFSequenceObjects( final String collectionURI,
		final Repository repo, final HashMap<Integer, BindingSet> objectMap )
			throws RepositoryException, MalformedQueryException,
			QueryEvaluationException
	{
		int max = -1;

		// First we select all the links from the collectionURI
		// out using the SPARQL query:
		//		SELECT <collectionURI> ?p ?o
		// ordered by the predicate name, as we're expecting those
		// to be rdf:_1, rdf:_2, etc. etc.
		final RepositoryConnection c = repo.getConnection();
		final String queryString = "SELECT ?p ?o WHERE {<" + collectionURI
				+ "> ?p ?o} ORDER BY DESC(?p)";
		final TupleQuery tupleQuery = c.prepareTupleQuery(
				QueryLanguage.SPARQL, queryString );

		// This actually does the query to the store...
		final TupleQueryResult result = tupleQuery.evaluate();

		// Loop through the results...
		while( result.hasNext() )
		{
			try
			{
				final BindingSet bs = result.next();

				// If the predicate is a sequence number (starts with rdf:_)
				// then we parse the integer into the index variable.
				// If it's not a NumberFormatException is thrown
				// (and caught and ignored because it's clearly not an
				// RDF sequence URI)
				final int index = Integer.parseInt(
					bs.getValue( "p" ).stringValue()
					.substring("http://www.w3.org/1999/02/22-rdf-syntax-ns#_"
							.length() ) );

				// Just be sure we're doing something sensible.
				if( index >= 0 )
				{
					// Stick it in the map.
					objectMap.put( index, bs );

					// Store the maximum index
					max = Math.max( index, max );
				}
			}
			catch( final NumberFormatException e )
			{
				// If we get a NFE then it's probably not a sequence number.
			}
			catch( final StringIndexOutOfBoundsException e )
			{
				// If we get a SOOBE then it's probably because the
				// predicate
				// is not a sequence number.
			}
		}
		return max;
	}

	/**
	 * Returns a list of unserialised objects that were unserialised from an
	 * unordered list of triples in RDF
	 *
	 * @param sequenceURI The URI of the triples
	 * @param repo The repository
	 * @param fieldType The field type
	 * @param field The field
	 * @return An array of objects, in an arbitrary order.
	 */
	@SuppressWarnings( "unchecked" )
	private <T> T[] getUnorderedObjects( final String sequenceURI,
			final Repository repo, final Class<T> fieldType,
			final String fieldName,
			final String subjectURI, final String predicate )
	{
		try
		{
			// First select all the objects that link the main object
			// with the collection objects via the collection predicate.
			// We use the SPARQL query:
			//		SELECT ?o WHERE { <subjectURI> <predicate> ?o . }
			// The results are in any order.
			final RepositoryConnection c = repo.getConnection();
			final String queryString = "SELECT ?o WHERE {<" + subjectURI
					+ "> <" + predicate + "> ?o}";
			final TupleQuery tupleQuery = c.prepareTupleQuery(
					QueryLanguage.SPARQL, queryString );

			// Evaluate the query, to get the results.
			final TupleQueryResult result = tupleQuery.evaluate();

			// We'll aggregate all the objects into this general list.
			final ArrayList<T> objs = new ArrayList<T>();
			int n = 0;
			while( result.hasNext() )
			{
				final BindingSet bs = result.next();
				final Value oo = bs.getBinding( "o" ).getValue();

				// Get the value of the field if it's a primitive, or
				// it's URI if it's a complex object and add it to
				// the list.
				objs.add( (T)this.getFieldValue( fieldType,
						oo, repo, fieldName, sequenceURI ) );
				n++;
			}

			// Copy the values into an array
			final T[] arr = (T[])Array.newInstance( fieldType, n );
			for( int i = 0; i < arr.length; i++ )
				arr[i] = objs.get(i);

			return arr;
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

		return null;
	}

	/**
	 * Attempts to find the correct class for the object URI given. If a class
	 * name cannot be found in the repository, then the field is used to attempt
	 * to instantiate a class.
	 *
	 * @param objectURI The URI of the object in the repo
	 * @param repo The RDF repository
	 * @return A class object.
	 */
	private Type getObjectClass( final String objectURI, final Repository repo )
	{
		String queryString = null;
		try
		{
			final RepositoryConnection c = repo.getConnection();

			queryString = "SELECT ?o WHERE {<" + objectURI + "> <"
					+ RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME + "> ?o.}";
			final TupleQuery tupleQuery = c.prepareTupleQuery(
					QueryLanguage.SPARQL, queryString );
			final TupleQueryResult result = tupleQuery.evaluate();

//			System.out.println( queryString );

			// We'll look at all the results until we find a class we can
			// instantiate. Of course, we expect there to be only one in
			// reality.
			Class<?> clazz = null;
			boolean found = false;
			while( !found && result.hasNext() )
			{
				final Value value = result.next().getValue( "o" );

				try
				{
					// Try to find the class with the given name
					clazz = Class.forName( value.stringValue() );

					// If the above succeeds, then we are done
					found = true;
				}
				catch( final ClassNotFoundException e )
				{
					e.printStackTrace();
				}
			}

			// Close the repo connection.
			c.close();

			// Return the class if we have one.
			if( clazz != null )
			{
//				System.out.println( clazz );
				return clazz;
			}

		}
		catch( final RepositoryException e )
		{
			System.out.println( "Processing: " + queryString );
			e.printStackTrace();
		}
		catch( final MalformedQueryException e )
		{
			System.out.println( "Processing: " + queryString );
			e.printStackTrace();
		}
		catch( final QueryEvaluationException e )
		{
			System.out.println( "Processing: " + queryString );
			e.printStackTrace();
		}

		// Can't determine a class from the repository? Then we'll fall back
		// to the field's type.
		return null;
	}

	/**
	 * Returns a URI for the given object. If it cannot determine one, it will
	 * return the default URI. It attempts to determine the object's URI
	 * by looking for a getURI() method in the object. If it has one, it invokes
	 * it and uses the return value as the object's URI, otherwise it will use
	 * the default URI passed in via the method parameters.
	 *
	 * @param obj The object
	 * @param defaultURI A default value for the URI
	 * @return A URI for the object
	 */
	public URIImpl getObjectURI( final Object obj, final URIImpl defaultURI )
	{
		// Check whether the object has a getURI() method. If so, then
		// what we'll do is this: we'll call the getURI() method to retrieve the
		// URI of the object and use that as the subject URI instead of the
		// uri that's passed in via the method parameters.
		try
		{
			final Method method = obj.getClass().getMethod( "getURI" );

			// We'll call the method and use the toString() method to
			// get the URI as a string. We'll instantiate a new URIImpl with it.
			final URIImpl subject = new URIImpl( method.invoke( obj,
					(Object[]) null ).toString() );

			return subject;
		}
		catch( final NoSuchMethodException e1 )
		{
		}
		catch( final SecurityException e1 )
		{
			e1.printStackTrace();
		}
		catch( final IllegalAccessException e )
		{
			e.printStackTrace();
		}
		catch( final IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		catch( final InvocationTargetException e )
		{
			e.printStackTrace();
		}

		return defaultURI;
	}

	/**
	 * Checks whether the field value is a special field. If so, it will output
	 * it using a separate device than the main serialization loop. Otherwise
	 * the method returns FALSE and the main loop continues.
	 * <p>
	 * A special field is one which has some annotation that requires the
	 * output be performed in a separate way. One example of this is the
	 * {@link TripleList} annotation which forces the outputs of triples
	 * directly from the value of the object. Similarly for the
	 * {@link RelationList} which forces the output of triples from the
	 * pairs stored in the object's value.
	 *
	 * @param fieldValue the value of the field
	 * @param field The field definition
	 * @return
	 */
	private boolean outputSpecial( final Object fieldValue, final Field field,
			final URIImpl subjectURI )
	{
		// Check whether this field is a triple list. If it is, we'll take
		// the triples from the field (assuming it's the right type) and
		// bang them into the triple store.
		if( field.getAnnotation( TripleList.class ) != null )
		{
			if( fieldValue instanceof Collection )
			{
				for( final Object o : (Collection<?>) fieldValue )
				{
					if( o instanceof Statement )
						this.addTriple( (Statement) o );
				}
			}
			return true; // stop the main loop processing this field
		}
		else
		// If the field is a relation list, process each in turn
		if( field.getAnnotation( RelationList.class ) != null )
		{
			if( fieldValue instanceof Collection )
			{
				int count = 0;
				for( final Object o : (Collection<?>) fieldValue )
				{
					if( o instanceof IndependentPair<?, ?> )
					{
						final IndependentPair<?, ?> ip = (IndependentPair<?, ?>) o;

						Value ooo;
						if( (ooo = this.checkPrimitive( ip.getSecondObject() )) != null )
							this.addTriple( new StatementImpl(
									subjectURI,
									new URIImpl( ip.getFirstObject().toString() ),
									ooo ) );
						else
						{
							final URI subjU = this.serializeAux(
									ip.getSecondObject(), subjectURI + "_"
											+ field.getName() + "_" + count++ );
							this.addTriple( new StatementImpl(
									subjectURI,
									new URIImpl( ip.getFirstObject().toString() ),
									subjU ) );
						}
					}
					else
						this.serializeAux( o,
								subjectURI + "_" + field.getName() + "_"
										+ count++ );
				}
			}
			return true; // stop the main loop processing this field
		}

		return false; // continue on the main loop
	}

	/**
	 * Adds a single triple to some RDF serializer.
	 *
	 * @param t The triple to add
	 */
	public void addTriple( final Statement t )
	{
		// Default implementation does nothing. Subclasses should override
		// this method and do something useful with created triples.
		// This method is not abstract just so users can create this object
		// for unserialization.
	}
}
