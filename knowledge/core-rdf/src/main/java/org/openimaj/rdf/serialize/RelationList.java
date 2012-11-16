package org.openimaj.rdf.serialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *	An annotation that marks a member of a class that is a Collection<IndependentPair<URI,Object>>
 *	object to be treated specially during serialization. When it is encountered,
 *	the relations stored in the list will be added to the graph without validation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 16 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RelationList
{

}
