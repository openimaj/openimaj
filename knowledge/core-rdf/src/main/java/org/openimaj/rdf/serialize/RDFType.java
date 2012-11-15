/**
 * 
 */
package org.openimaj.rdf.serialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *	An annotation that can be used to specify how a class can be serialized
 *	to a type in RDF. 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 15 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RDFType
{
	/** The URI of the RDF Type */ 
	public String value();
}
