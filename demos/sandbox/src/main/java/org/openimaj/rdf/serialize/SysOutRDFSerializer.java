/**
 * 
 */
package org.openimaj.rdf.serialize;

import org.openrdf.model.Statement;

/**
 *	Test class that outputs triples to the sysout.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Sep 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class SysOutRDFSerializer extends RDFSerializer
{
	@Override
	public void addTriple( Statement t )
	{
		System.out.println( t );
	}
}
