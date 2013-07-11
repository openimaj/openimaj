/**
 *
 */
package org.openimaj.vis;


/**
 *	An interface for classes that can transform data units into renderer units
 *	based on the definition of the data axis in space.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <Q> Colour representation for the axis renderer
 * 	@param <D> The type of the data units
 * 	@param <U> The type of the converted units
 */
public interface DataUnitsTransformer<Q,D,U>
{
	/**
	 * 	Force a precalculation of the variables required for
	 * 	the pixel transformation.
	 */
	public void precalc();

	/**
	 * 	For a given coordinate in the units of the data, will calculate
	 * 	the render coordinates
	 * @param units The unit to convert to render coordinates
	 *
	 *	@return Render coordinates
	 */
	public U calculatePosition( D units );

	/**
	 *	Calculates the data unit coordinate at the given render coordinates
	 * @param position The data rendered position to convert
	 *
	 *	@return the data units on the given axis
	 */
	public D calculateUnitsAt( U position );

	/**
	 * 	Given a specific data dimension, calculates a render dimension.
	 *
	 *	@param dimension The units/data dimension
	 *	@return The scaled dimension
	 */
	public U scaleDimension( D dimension );
}
