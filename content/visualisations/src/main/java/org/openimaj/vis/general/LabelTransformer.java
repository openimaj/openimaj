/**
 *
 */
package org.openimaj.vis.general;

/**
 *	Used by the {@link AxesRenderer2D} to write labels onto the axes.
 *	The label transformer should take a value and return a string.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface LabelTransformer
{
	/**
	 * 	Transform the given value into a different label.
	 *	@param value The value to transform
	 *	@return The label
	 */
	public String transform( double value );
}
