package org.openimaj.rdf.serialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for members of classes that can be serialized as relationships
 * in an RDF graph.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 11 Sep 2012
 * @version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Predicate {
	/** The URI of the predicate */
	public String value();
}
