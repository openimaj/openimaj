/**
 * 
 */
package org.openimaj.rdf.serialize;

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

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.vocabulary.XSD;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Sep 2012
 *	@version $Author$, $Revision$, $Date$
 */

public abstract class RDFSerializer
{
	/** Whether to try to create predicates for unannotated fields */
	protected boolean autoPredicate = true;
	
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
						if( autoPredicate )
							predicate = new URIImpl( uri+"_has"+
									field.getName().substring(0,1).toUpperCase()+
									field.getName().substring(1) );
					}
					
					// Check whether we've got something we're going
					// to actually output.
					if( predicate != null )
					{
						Value object;
						if( (object = checkPrimitive( oo )) == null )
						{
							Object obj = oo;
							
							// If oo is an array, we'll wrap it in an array list
							// so that we can deal with it as a colleciton below.
							if( oo.getClass().isArray() )
								obj = new ArrayList<Object>( 
										(Collection<?>)
										Arrays.asList( (Object[])oo ) );
							
							// If we have a collection of things, we'll output
							// them as an RDF linked-list.
							if( obj instanceof Collection<?> )
							{
								object = new URIImpl( uri+"_"+field.getName() );
								int count = 1;
								for( Object o : (Collection<?>)obj )
								{
									String u = uri+"_"+field.getName();
									
									// If the values in the collection are primitives
									// we will simply add them in a list, otherwise
									// we'll attempt to serialise them separately.
									Value ooo;
									if( (ooo = checkPrimitive( o )) == null )
									{
										serialize( o, u+"_"+count );
									}
									else
									{
										URIImpl p = new URIImpl( 
												"http://example.com/hasItem" );
										StatementImpl s = new StatementImpl( 
											new URIImpl(u), p, ooo );
										addTriple( s );
									}
									
									count++;
								}
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
	 *	Adds a single triple to some RDF serializer. 
	 *	@param t The triple to add
	 */
	public abstract void addTriple( Statement t );
}
