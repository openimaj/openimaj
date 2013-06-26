/**
 *
 */
package org.openimaj.vis;

/**
 *	An interface for visualisation objects that are able to provide animated
 *	visualisations. They must implement the methods to allow them to accept
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface AnimatedVisualisationProvider extends VisualisationImageProvider
{
	/**
	 * 	Add a visualisation listener to be informed of animation events.
	 *	@param avl The listener to add
	 */
	public void addAnimatedVisualisationListener( AnimatedVisualisationListener avl );

	/**
	 * 	Remove a given listener from being informed of animation events.
	 *	@param avl The listener to remove
	 */
	public void removeAnimatedVisualisationListener( AnimatedVisualisationListener avl );
}
