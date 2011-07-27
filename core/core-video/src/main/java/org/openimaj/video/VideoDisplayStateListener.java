/**
 * 
 */
package org.openimaj.video;

/**
 *	A listener interface for objects that want to know about the state
 *	of a video display.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 27 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public interface VideoDisplayStateListener
{
	/**
	 * 	Called when the video display is stopped.
	 *	@param v The video display that was stopped.
	 */
	public void videoStopped( VideoDisplay<?> v );
	
	/**
	 * 	Called when the video display is set to play.
	 *	@param v The video display that was set to play.
	 */
	public void videoPlaying( VideoDisplay<?> v );
	
	/**
	 * 	Called when the video display is paused.
	 *	@param v The video display that was paused.
	 */
	public void videoPaused( VideoDisplay<?> v );
	
	/**
	 * 	Called when the video display's state changes.
	 * 
	 *	@param mode The mode the video display changed to
	 *	@param v The video display whose state changed.
	 */
	public void videoStateChanged( VideoDisplay.Mode mode, VideoDisplay<?> v );
}
