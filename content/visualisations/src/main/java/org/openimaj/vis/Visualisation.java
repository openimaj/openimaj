/**
 *
 */
package org.openimaj.vis;

/**
 *	An API for objects that are able to provide visualisation for some data.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <T>
 *  @created 5 Jul 2013
 */
public interface Visualisation<T> extends VisualisationImageProvider
{
	/**
	 * 	Set the data to display in the visualisation.
	 *	@param data The data to display
	 */
	public void setData( T data );
}
