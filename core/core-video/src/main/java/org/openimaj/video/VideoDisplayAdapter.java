/**
 * 
 */
package org.openimaj.video;

import org.openimaj.image.Image;
import org.openimaj.video.VideoDisplay.Mode;

/**
 *	An adapter for classes that want to listen to certain events that are
 *	generated from a video display. This adapter implements both the
 *	{@link VideoDisplayListener} and the {@link VideoDisplayStateListener}.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 27 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class VideoDisplayAdapter<T extends Image<?,T>> 
	implements VideoDisplayStateListener, VideoDisplayListener<T>
{

	/** 
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoStopped(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoStopped( VideoDisplay<?> v )
	{
	}

	/** 
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoPlaying(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoPlaying( VideoDisplay<?> v )
	{
	}

	/** 
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoPaused(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoPaused( VideoDisplay<?> v )
	{
	}

	/** 
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoStateChanged(org.openimaj.video.VideoDisplay.Mode, org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoStateChanged( Mode mode, VideoDisplay<?> v )
	{
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate( VideoDisplay<T> display )
	{
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate( T frame )
	{
	}
}
