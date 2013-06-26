/**
 *
 */
package org.openimaj.vis;

/**
 *	An interface for objects that want to listen for animation events
 *	from an {@link AnimatedVisualisationProvider}. The object must implement the callback
 *	method that animated visualisations will call when they are ready
 *	to provide an update. It is not necessary that the provider actually
 *	produce the visualisation before calling this callback, simply that
 *	the animation is proceeding and should the listener wish they may
 *	call the {@link VisualisationImageProvider#updateVis()} method and
 *	then the {@link VisualisationImageProvider#getVisualisationImage()} method
 *	to retrieve the latest image.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 25 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface AnimatedVisualisationListener
{
	/**
	 * 	Called by the {@link AnimatedVisualisationProvider} when
	 * 	a new visualisation is available to drawn (but hasn't been
	 * 	drawn yet).
	 * 	@param avp The source of the event
	 */
	public void newVisualisationAvailable( AnimatedVisualisationProvider avp );
}
