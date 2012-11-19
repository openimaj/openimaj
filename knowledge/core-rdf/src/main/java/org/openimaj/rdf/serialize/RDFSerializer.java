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
import java.lang.reflect.ParameterizedType;
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
 * Lists and collections are output in the same way. They are output as separate
 * triples in an unordered way, unless the {@link RDFCollection} annotation is
 * used. When this annotation is used, they are encoded using RDF
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
public class RDFSerializer {
	/** Predicate for giving the class name */
	public static final String RDF_OPENIMAJ_P_CLASSNAME = "http://rdf.openimaj.org/hasClassName/";

	/** Whether to try to create predicates for unannotated fields */
	protected boolean autoPredicate = false;

	/** Whether to output class names */
	protected boolean outputClassNames = true;

	/** 
	 * During a serialization, this field contains all the graphs which
	 * have already been written, avoiding duplicate entries in the output
	 * as well as avoiding infinite loops when cycles occur
	 */
	private HashSet<URI> knownGraphs = null;

	/**
	 * Default constructor
	 */
	public RDFSerializer() {
		this(false);
	}

	/**
	 * Constructor that determines whether to create predicates automatically
	 * when the {@link Predicate} annotation does not exist.
	 * 
	 * @param autoPredicate
	 *            Whether to automatically create predicates
	 */
	public RDFSerializer(final boolean autoPredicate) {
		this.autoPredicate = autoPredicate;
	}

	/**
	 * Serialize the given object as RDF.
	 * 
	 * @param objectToSerialize The object to serialize.
	 * @param uri The URI of the object to serialize.
	 * @return Returns the URI of the object (this may be different
	 * 	to the one that's passed in)
	 */
	public URI serialize(final Object objectToSerialize, final String uri)
	{
		this.knownGraphs  = new HashSet<URI>();
		final URI i = this.serializeAux( objectToSerialize, uri );
		return i;
	}
	
	/**
	 * Serialize the given object as RDF.
	 * 
	 * @param objectToSerialize The object to serialize.
	 * @param uri The URI of the object to serialize.
	 * @return Returns the URI of the object (this may be different
	 * 	to the one that's passed in)
	 */
	public URI serializeAux(final Object objectToSerialize, final String uri) 
	{
		// The subject (the object to serialize) won't change, so
		// we'll just create the URI node once.
		URIImpl subject = new URIImpl(uri);

		// Find the object URI
		subject = this.getObjectURI( objectToSerialize, subject );

		// Check whether we've already serialized this object. If we have
		// we just return, otherwise we add it to our memory of serialized
		// objects so that we won't try again.
		if( this.knownGraphs.contains( subject ) )
			return subject;
		this.knownGraphs.add( subject );
		
		// Output the class name of the object to serialise
		if (this.outputClassNames)
			this.addTriple(new StatementImpl(subject, 
					new URIImpl(RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME), 
					this.checkPrimitive(objectToSerialize
					.getClass().getName())));

		// Check whether there's a semantic type for this object
		final RDFType typeAnnotation = objectToSerialize.getClass().getAnnotation( RDFType.class );
		if( typeAnnotation != null )
		{
			this.addTriple( new StatementImpl( subject,
					RDF.TYPE, new URIImpl( typeAnnotation.value() )) );
		}
		
		// Get all the fields
		final List<Field> fields = this.getAllFields(objectToSerialize);

		// Loop through the fields and output them
		for (final Field field : fields) {
//			 System.out.println( "====== Field "+field+" ============");

			try {
				// Get the value of the field
				field.setAccessible(true);
				final Object oo = field.get(objectToSerialize);

				// If it's not null, and not a special one, we'll output it
				if( oo != null && !this.outputSpecial( oo, field, subject ) ) 
				{
					// Get the predicate name (may be null if if cannot be
					// created either due to a lack of the @Predicate
					// annotation or because autoPredicate is false
					final URIImpl predicate = this.getPredicateName(field, uri);
					
					// Determine whether there's an RDFCollection annotation
					final boolean asCollection = 
							field.getAnnotation( RDFCollection.class ) != null;
					
					// Check whether we've got something we're going
					// to actually output. (we may be null here if the field
					// was not annotated with @Predicate and autoPredicate is
					// false.
					if (predicate != null) {
						Value object;
						boolean isCollective = false;
						if( (object = this.checkPrimitive(oo)) == null) 
						{
							final Object obj = oo;

							// If oo is an array, we'll wrap it in an array list
							// so that we can deal with it as a collection
							// below.
							if( oo.getClass().isArray() ) 
							{
								isCollective = true;
								
								object = this.getObjectURI( obj, 
										new URIImpl(uri + "_" + field.getName() ) );
								
								for (int count = 0; count < Array.getLength(oo);) {
									final Object o = Array.get(oo, count);
									count = this.processLoop(uri, field, count, 
											o, subject, predicate, asCollection);
								}
							} else
							// If we have a collection of things, we'll output
							// them as an RDF linked-list.
							if( obj instanceof Collection<?> ) 
							{
								isCollective = true;
								
								object = this.getObjectURI( obj, 
										new URIImpl(uri + "_" + field.getName() ) );
								
								if( this.outputClassNames )
								{
									final Statement t = new StatementImpl(
											new URIImpl(object.stringValue()), 
											new URIImpl(RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME),
											new LiteralImpl(obj.getClass().getName()));
									this.addTriple(t);
								}
								
								int count = 1;
								for (final Object o : (Collection<?>) obj)
									count = this.processLoop(uri, field, count, 
											o, subject, predicate, asCollection);
							} else {
								// Try to serialise the object.
								// The serialize function will try to get the object
								// URI if this one here isn't any good.
								object = new URIImpl(uri + "_" + field.getName());
								object = this.serializeAux(oo, object.stringValue());
							}
						}

						// We don't need to add this triple if the triples are
						// are collection that's been output separately
						if( !isCollective || (isCollective && asCollection ) )
						{
							// Create a triple and send it to the serializer
							final Statement t = new StatementImpl( subject, 
									predicate, object);
							this.addTriple(t);
						}
					}
				}
			} catch (final Exception e) {
				System.out.println("Error reflecting " + field);
				e.printStackTrace();
			}
		}
		
		return subject;
	}

	/**
	 * Returns a predicate name for the given field.
	 * 
	 * @param field
	 *            The field
	 * @param uri
	 *            The URI of the object
	 * @return A predicate URI, either generated from the @Predicate annotation
	 *         or from the field name
	 */
	private URIImpl getPredicateName(final Field field, final String uri) {
		// Get the predicate annotation, if there is one
		final Predicate predicateAnnotation = field.getAnnotation(Predicate.class);

		URIImpl predicate = null;
		if (predicateAnnotation != null) {
			// Create a predicate URI for this predicate
			predicate = new URIImpl(predicateAnnotation.value());
		}
		// Null predicate annotation?
		else {
			// Try to create a predicate for the unannotated field
			if (this.autoPredicate)
				predicate = new URIImpl(uri + "_has" + field.getName().substring(0, 1).toUpperCase()
						+ field.getName().substring(1));
		}

		return predicate;
	}

	/**
	 * A method that's called during the processing of a list of items to write
	 * a single item.
	 * 
	 * @param objectURI
	 *            The uri of the current object which contains the list
	 * @param field
	 *            The field being processed
	 * @param listCounter
	 *            The current counter in the list (1-based index)
	 * @param listItemObject
	 *            The object to be serialised
	 * @param subject The original subject of the list
	 * @param predicate The original predicate of the list
	 * @param asCollection Whether to output as a collection or individual triples
	 * @return the next counter in the list
	 */
	private int processLoop(final String objectURI, final Field field, 
			final int listCounter, final Object listItemObject, 
			final URIImpl subject, final URIImpl predicate, 
			final boolean asCollection) 
	{
		// The URI of the list object
		final URIImpl defaultURI = new URIImpl(objectURI + "_" + field.getName() ); 
		final URIImpl u = this.getObjectURI( listItemObject, defaultURI );
		final boolean usingDefault = u == defaultURI;
		
		if( !asCollection )
		{
			StatementImpl s; 
			final Value ooo;
			if ((ooo = this.checkPrimitive(listItemObject)) != null) 
			{
				// If it's primitive, we simply output as a triple
				s = new StatementImpl( subject, predicate, ooo );
			}
			else
			{
				// Serialise the object into a subgraph
				final URI uu = this.serializeAux(listItemObject, u.toString() + 
						(usingDefault?"_"+listCounter:""));				
				
				// Output the triple as a standard triple that links to the
				// subgraph of the object
				s = new StatementImpl( subject, predicate, uu );
			}
			
			this.addTriple(s);
			return listCounter + 1;
		}
		
		// We're outputting as a collection
		// so the predicate becomes the rdf:_n counter predicate
		final URIImpl p = new URIImpl(RDF.NAMESPACE + "_" + listCounter);

		// If we're dealing with the first item in the list, we'll
		// put the RDF type (sequence) into the output
		if (listCounter == 1)
			this.addTriple(new StatementImpl( u, RDF.TYPE, RDF.SEQ));

		// If the values in the collection are primitives
		// we will simply add them in a list, otherwise
		// we'll attempt to serialise them separately.
		Value ooo;
		if ((ooo = this.checkPrimitive(listItemObject)) != null) {
			// If it's primitive, we simply output
			// as a sequence item: (:object rdf:_n primitive)
			final StatementImpl s = new StatementImpl( u, p, ooo);
			this.addTriple(s);
		} else {
			// If it's a complex field, we'll output the sequence
			// item as a URI to the subgraph, then we'll create
			// the subgraph by recursing.
			final URI uu = this.serializeAux(listItemObject, u.toString() + (usingDefault?"_"+listCounter:""));
			final StatementImpl s = new StatementImpl(u, p, uu);
			this.addTriple(s);
		}

		return listCounter + 1;
	}

	/**
	 * Checks whether the given object is a primitive type and, if so, will
	 * return a Node that encodes it. Otherwise NULL is returned.
	 * 
	 * @param o
	 *            The object to check
	 * @return a Node or NULL
	 */
	private Value checkPrimitive(final Object o) {
		if (o instanceof String)
			return new LiteralImpl(o.toString());

		if (o instanceof Integer)
			return new ValueFactoryImpl().createLiteral((Integer) o);

		if (o instanceof Float)
			return new ValueFactoryImpl().createLiteral((Float) o);

		if (o instanceof Double)
			return new ValueFactoryImpl().createLiteral((Double) o);

		if (o instanceof URI || o instanceof URL)
			return new URIImpl(o.toString());

		return null;
	}

	/**
	 * Returns a list of declared fields from the whole object tree.
	 * 
	 * @param o
	 *            The object
	 * @return A list of fields
	 */
	private List<Field> getAllFields(final Object o) {
		final ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> objectToGetFieldsFrom = o.getClass();
		do {
			fields.addAll(Arrays.asList(objectToGetFieldsFrom.getDeclaredFields()));
			objectToGetFieldsFrom = objectToGetFieldsFrom.getSuperclass();
		} while (!objectToGetFieldsFrom.getSimpleName().equals("Object"));

		return fields;
	}

	/**
	 * Set whether to output class names as triples.
	 * 
	 * @param tf
	 *            TRUE to output class name triples.
	 */
	public void setOutputClassNames(final boolean tf) {
		this.outputClassNames = tf;
	}

	/**
	 * Set whether to attempt to output all fields from the objects, not just
	 * those annotated with {@link Predicate}.
	 * 
	 * @param tf
	 *            TRUE to attempt to find predicates for all members.
	 */
	public void setAutoPredicate(final boolean tf) {
		this.autoPredicate = tf;
	}

	/**
	 * Unserializes an object from the given RDF string (with the given format)
	 * into the given object.
	 * 
	 * @param <T>
	 *            Type of object being unserialised
	 * 
	 * @param objectToUnserialize
	 *            The object to populate
	 * @param objectRootURI
	 *            The URI that gives the root of the object graph
	 * @param rdf
	 *            The RDF string
	 * @param rdfFormat
	 *            The format of the RDF in the string
	 * @return The populated object or NULL if an error occurs
	 */
	public <T> T unserialize(final T objectToUnserialize, final String objectRootURI, final String rdf, final RDFFormat rdfFormat) {
		try {
			// We'll read the RDF into a memory store. So create that store
			// here.
			final Repository repo = new SailRepository(new MemoryStore());
			repo.initialize();

			// Read the RDF into the store
			final RepositoryConnection connection = repo.getConnection();
			final StringReader sr = new StringReader(rdf);
			final String graphURI = "http://onto.arcomem.eu/tmp/";
			connection.add(sr, graphURI, rdfFormat);

			return this.unserialize(objectToUnserialize, objectRootURI, repo);
		} catch (final RepositoryException e) {
			e.printStackTrace();
			return null;
		} catch (final RDFParseException e) {
			e.printStackTrace();
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Unserializes an object from an RDF graph that is rooted at the given URI.
	 * 
	 * @param <T>
	 *            Type of object being unserialised
	 * 
	 * @param objectToUnserialize
	 *            The object to populate
	 * @param objectRootURI
	 *            The URI that gives the root of the object graph
	 * @param repo
	 *            The repository storing the RDF graph
	 * @return The populated object or NULL if an error occurs
	 */
	public <T> T unserialize(final T objectToUnserialize, final String objectRootURI, final Repository repo) {
		// Can't do anything if the object is null
		if (objectToUnserialize == null) {
			System.err.println("Unserialize error: given object is null");
			return null;
		}

		try {
			final RepositoryConnection connection = repo.getConnection();

			// Get the fields of the object's class
			final Field[] fields = objectToUnserialize.getClass().getFields();

			// Loop through the fields
			for (final Field field : fields) {
				final URIImpl predicateName = this.getPredicateName(field, objectRootURI);

				if (predicateName != null) {
					// Query the RDF graph for the triples that represent this
					// field in the graph. If there are more than one, the first
					// will
					// be used.
					try {
						final String queryString = "SELECT ?o WHERE {<" + objectRootURI + "> <" + predicateName
								+ "> ?o.}";
						final TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
						final TupleQueryResult result = tupleQuery.evaluate();
						
						// We only want the first result
						if (result.hasNext()) {
							try {
								final BindingSet bindingSet = result.next();
								final Value objectValue = bindingSet.getValue("o");
								
								// We have a value for the field. Now what we do
								// with it depends on the field itself.
								field.set(objectToUnserialize, this.getFieldValue(
										field.getGenericType(), objectValue, repo, 
										field, objectRootURI ));
							} catch (final IllegalArgumentException e) {
								e.printStackTrace();
							} catch (final IllegalAccessException e) {
								e.printStackTrace();
							}
						} else {
							// RDF Graph did not have a value for the field
						}
					} catch (final MalformedQueryException e) {
						e.printStackTrace();
					} catch (final QueryEvaluationException e) {
						e.printStackTrace();
					}
				}
			}

			connection.close();
		} catch (final RepositoryException e) {
			e.printStackTrace();
		} catch (final SecurityException e) {
			e.printStackTrace();
		}

		return objectToUnserialize;
	}

	/**
	 * Set the field in the given object to the given value, if possible.
	 * 
	 * @param field
	 *            The field.
	 * @param value
	 *            The RDF value object for the field.
	 * @param repo
	 *            The RDF Graph repository in use.
	 */
	private Object getFieldValue(final Type fieldType, final Value value, 
			final Repository repo, final Field field, final String subjectURI ) {
		try {
			if (fieldType.equals(String.class)) {
				return value.stringValue();
			} else if (fieldType.equals(java.net.URI.class)) {
				try {
					return new java.net.URI(value.toString());
				} catch (final URISyntaxException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals(URL.class)) {
				try {
					return new URL(value.toString());
				} catch (final MalformedURLException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
				return Integer.parseInt(value.stringValue());
			} else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
				return Double.parseDouble(value.stringValue());
			} else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
				return Float.parseFloat(value.stringValue());
			} else {
				// The object is not a default type that we understand.
				// So what we must do is try to instantiate the object,
				// then attempt to deserialize that object, then set the field
				// in this object.
				try {
					String listURI = value.stringValue(); 
					
					// Try and look up the className predicate of the object.
					Type type = this.getObjectClass( listURI, repo );
					if( type == null )
						type = this.getObjectClass( 
							listURI = subjectURI + "_" + field.getName(), repo );

					// Attempt to instantiate the new object.
					// This may fail if the object does not have a
					// default or accessible constructor.
					final Object newInstance = ((Class<?>) type).newInstance();
					
					final URIImpl predicateName = this.getPredicateName( field, listURI );

					// If we have a collection object, then we can do something
					// a bit different here. We know it's a collection, so we
					// simply iterate through the sequence getting each item in
					// turn and deserialize it.
					if (newInstance instanceof Collection) {
						@SuppressWarnings("unchecked")
						final Collection<Object> collection = (Collection<Object>) newInstance;

						final Object[] seq = this.getSequenceObjects(
								listURI,
								repo, 
								((ParameterizedType)fieldType).getActualTypeArguments()[0], 
								field,
								subjectURI, predicateName.stringValue() );
						
						if( seq != null )
							for (final Object o : seq)
								collection.add(o);

						return collection;
					} else
					// Same goes for if it's an array.
					if (((Class<?>) type).isArray()) {
						final Object[] seq = this.getSequenceObjects(listURI,
								repo, ((Class<?>) type).getComponentType(), field,
								subjectURI, predicateName.stringValue() );
						return seq;
					}
					// If we don't know what it is, we'll treat it as
					// an unknown (RDF) serializable object and recurse
					else {
						// Now recurse the unserialization down the object tree,
						// by attempting to unserialize the given object.
						this.unserialize(newInstance, listURI, repo);
					}

					return newInstance;
				} catch (final InstantiationException e) {
					e.printStackTrace();
				}
			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
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
	private Object[] getSequenceObjects(final String sequenceURI, 
			final Repository repo, final Type fieldType, final Field field,
			final String subject, final String predicate ) 
	{
		// Before we retrieve the objects from the sequence, we'll first
		// double check that it really is a sequence. If it's not (it's a 
		// collection of unordered triples), then we'll treat it differently.
		try
		{
			final RepositoryConnection c = repo.getConnection();
			final String queryString = "ASK {<"+sequenceURI+"> <"+RDF.TYPE+"> <"+RDF.SEQ+">}";
			final BooleanQuery query = c.prepareBooleanQuery( QueryLanguage.SPARQL, queryString );
			if( !query.evaluate() )
			{
				return this.getUnorderedObjects( sequenceURI, repo, 
						fieldType, field, subject, predicate );
			}
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

		
		// This will be the output of the method - an array of
		// all the objects in order. May contain nulls.
		Object[] sequence = null;

		// We'll get all the results into this map to start with.
		// It maps an index (in the sequence) to the binding set from the query
		final HashMap<Integer, BindingSet> tmpMap = new HashMap<Integer, BindingSet>();

		try {
			int max = -1;

			final RepositoryConnection c = repo.getConnection();

			final String queryString = "SELECT ?p ?o WHERE {<" + sequenceURI + "> ?p ?o} ORDER BY DESC(?p)";
			final TupleQuery tupleQuery = c.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			final TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				try {
					final BindingSet bs = result.next();

					// If the predicate is a sequence number, then we parse the
					// integer into the index variable. If it's not a
					// NumberFormatException is thrown (and caught)
					final int index = Integer.parseInt(bs.getValue("p").stringValue()
							.substring("http://www.w3.org/1999/02/22-rdf-syntax-ns#_".length()));

					// Just be sure we're doing something sensible.
					if (index >= 0) {
						// Stick it in the map.
						tmpMap.put(index, bs);

						// Store the maximum index
						max = Math.max(index, max);
					}
				} catch (final NumberFormatException e) {
					// If we get a NFE then it's probably not a sequence number.
				} catch (final StringIndexOutOfBoundsException e) {
					// If we get a SOOBE then it's probably because the
					// predicate
					// is not a sequence number.
				}
			}

			// So we've processed and stored all the results. Now we need to
			// make sure our output array is big enough for all the results.
			sequence = new Object[max];

			// Now loop through all the values and poke them into the array.
			// Note that we convert the indices to 0-based (RDF.Seq are
			// 1-based indices).
			for (final int i : tmpMap.keySet())
				sequence[i - 1] = this.getFieldValue(fieldType, 
						tmpMap.get(i).getValue("o"), repo, field, sequenceURI );
		} catch (final RepositoryException e) {
			e.printStackTrace();
		} catch (final MalformedQueryException e) {
			e.printStackTrace();
		} catch (final QueryEvaluationException e) {
			e.printStackTrace();
		}

		return sequence;
	}

	/**
	 * 	Returns a list of unserialized objects that were unserialized from
	 * 	an unorder list of triples in RDF
	 *	@param sequenceURI The URI of the triples
	 *	@param repo The repository
	 *	@param fieldType The field type
	 *	@param field The field
	 *	@return
	 */
	private Object[] getUnorderedObjects( final String sequenceURI, final Repository repo,
			final Type fieldType, final Field field, final String subjectURI, 
			final String predicate )
	{
		try
		{
			final RepositoryConnection c = repo.getConnection();
			final String queryString = "SELECT ?o WHERE {<"+subjectURI+"> <"+predicate+"> ?o}";
			final TupleQuery tupleQuery = c.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			final TupleQueryResult result = tupleQuery.evaluate();

			final ArrayList<Object> objs = new ArrayList<Object>();
			while( result.hasNext() ) 
			{
				final BindingSet bs = result.next();
				final Value oo = bs.getBinding( "o" ).getValue();
				objs.add( this.getFieldValue( fieldType, oo, repo, field, sequenceURI ) );
			}
			return objs.toArray( new Object[0] );
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
	 * @param objectURI
	 *            The URI of the object in the repo
	 * @param repo
	 *            The RDF repository
	 * @return A class object.
	 */
	private Type getObjectClass(final String objectURI, final Repository repo ) 
	{
		String queryString = null;
		try {
			final RepositoryConnection c = repo.getConnection();

			queryString = "SELECT ?o WHERE {<" + objectURI + "> <" 
					+ RDFSerializer.RDF_OPENIMAJ_P_CLASSNAME
					+ "> ?o.}";
			final TupleQuery tupleQuery = c.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			final TupleQueryResult result = tupleQuery.evaluate();

			System.out.println( queryString );
			
			// We'll look at all the results until we find a class we can
			// instantiate. Of course, we expect there to be only one in
			// reality.
			Class<?> clazz = null;
			boolean found = false;
			while (!found && result.hasNext()) {
				final Value value = result.next().getValue("o");

				try {
					// Try to find the class with the given name
					clazz = Class.forName(value.stringValue());

					// If the above succeeds, then we are done
					found = true;
				} catch (final ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			// Close the repo connection.
			c.close();

			// Return the class if we have one.
			if (clazz != null)
				return clazz;
			
		} catch (final RepositoryException e) {
			System.out.println( "Processing: "+queryString );
			e.printStackTrace();
		} catch (final MalformedQueryException e) {
			System.out.println( "Processing: "+queryString );
			e.printStackTrace();
		} catch (final QueryEvaluationException e) {
			System.out.println( "Processing: "+queryString );
			e.printStackTrace();
		}

		// Can't determine a class from the repository? Then we'll fall back
		// to the field's type.
		return null;
	}

	/**
	 * 	Returns a URI for the given object. If it cannot determine one, it
	 * 	will return the default URI.
	 * 
	 *	@param obj The object
	 *	@param defaultURI A default value for the URI
	 *	@return A URI for the object
	 */
	public URIImpl getObjectURI( final Object obj, final URIImpl defaultURI )
	{
		// Check whether the object has a getURI() method. If so, then
		// what we'll do, is we'll call the getURI() method to retrieve the
		// URI of the object and use that as the subject URI instead of the
		// uri that's passed in via the method parameters.
		try
		{
			final Method method = obj.getClass().getMethod( "getURI" );

			// We'll call the method and use the toString() method to
			// get the URI as a string. We'll instantiate a new URIImpl with it.
			final URIImpl subject = new URIImpl( 
				method.invoke( obj, (Object[])null ).toString() );
			
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
	 * 	Checks whether the field value is a special field. If so, it will output
	 * 	it using a separate device than the main serialization loop. Otherwise
	 * 	the method returns FALSE and the main loop continues.
	 * 
	 *	@param fieldValue the value of the field
	 *	@param field The field definition
	 *	@return
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
				for( final Object o : (Collection<?>)fieldValue )
				{
					if( o instanceof Statement )
						this.addTriple( (Statement)o );
				}
			}
			return true;	// stop the main loop processing this field
		}
		else
		// If the field is a relation list, process each in turn
		if( field.getAnnotation( RelationList.class ) != null )
		{
			if( fieldValue instanceof Collection )
			{
				int count = 0;
				for( final Object o : (Collection<?>)fieldValue )
				{
					if( o instanceof IndependentPair<?,?> )
					{
						final IndependentPair<?,?> ip = (IndependentPair<?,?>)o;
						
						Value ooo;
						if( (ooo = this.checkPrimitive( ip.getSecondObject() )) != null )
							this.addTriple( new StatementImpl( subjectURI,
									new URIImpl( ip.getFirstObject().toString() ), ooo ) );
						else
						{
							final URI subjU = this.serializeAux( ip.getSecondObject(), 
								subjectURI + "_" + field.getName() + "_" + count++ );
							this.addTriple( new StatementImpl( subjectURI,
									new URIImpl( ip.getFirstObject().toString() ), subjU ) );
						}
					}
					else
						this.serializeAux( o, subjectURI + "_" + field.getName() 
							+ "_" + count++ );
				}
			}
			return true;	// stop the main loop processing this field
		}
		
		return false;	// continue on the main loop
	}
	
	/**
	 * Adds a single triple to some RDF serializer.
	 * 
	 * @param t
	 *            The triple to add
	 */
	public void addTriple(final Statement t) {
		// Default implementation does nothing. Subclasses should override
		// this method and do something useful with created triples.
		// This method is not abstract just so users can create this object
		// for unserialization.
	}
}
