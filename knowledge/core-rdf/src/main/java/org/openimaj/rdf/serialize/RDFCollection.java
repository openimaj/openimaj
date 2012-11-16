/**
 * 
 */
package org.openimaj.rdf.serialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *	This annotation will force a collection object to be output as an 
 *	RDF collection (or sequence) rather than as individual triples.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 15 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RDFCollection
{
}
