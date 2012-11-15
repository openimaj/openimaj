/**
 * 
 */
package org.openimaj.rdf.owl2java;

/**
 *	Top level class that all top-level superclasses generated from an ontology
 *	will inherit from. This allows us to add functionality to all generated
 *	classes in one single place.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 15 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class Something
{
	/** The URI of this instance */
	private String uri;
	
	/**
	 * 	Get the URI of this instance
	 *	@return The URI of this instance
	 */
	public String getURI()
	{
		return this.uri;
	}
	
	/**
	 * 	Set the URI of this instance.
	 *	@param uri The URI of this instance
	 */
	public void setURI( final String uri )
	{
		this.uri = uri;
	}
}
