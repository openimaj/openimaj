/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import org.openimaj.video.timecode.VideoTimecode;

/**
 *	Represents a shot boundary that is a fade shot boundary between
 *	two scenes. This occurs over a time duration so has both a start
 *	and end timecode.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 7 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class FadeShotBoundary extends ShotBoundary
{
	/** The timecode at the end of the fade */
	private VideoTimecode endTimecode;
	
	/**
	 * 	Create a new fade shot boundary giving the start
	 * 	of the fade as a starting point.
	 * 
	 *	@param b The boundary at the start of the fade.
	 */
	public FadeShotBoundary( ShotBoundary b )
	{
		super( b.timecode );
	}
	
	/**
	 * 	Get the timecode at the end of the fade.
	 *	@return The timecode at the end of the fade.
	 */
	public VideoTimecode getEndTimecode()
	{
		return this.endTimecode;
	}
	
	/**
	 * 	Get the timecode at the start of the fade.
	 *	@return The timecode at the start of the fade.
	 */
	public VideoTimecode getStartTimecode()
	{
		return super.timecode;
	}
	
	/**
	 * 	Set the timecode at the end of the fade. 
	 *	@param v The timecode of end of the fade
	 */
	public void setEndTimecode( VideoTimecode v )
	{
		this.endTimecode = v;
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processing.shotdetector.ShotBoundary#getTimecode()
	 */
	@Override
	public VideoTimecode getTimecode()
	{
		return this.getEndTimecode();
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processing.shotdetector.ShotBoundary#toString()
	 */
	@Override
	public String toString()
	{
		return "Fade "+getStartTimecode()+" -> "+getEndTimecode();
	}
}
