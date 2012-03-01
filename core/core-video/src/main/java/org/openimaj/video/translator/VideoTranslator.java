/**
 * 
 */
package org.openimaj.video.translator;

import org.openimaj.image.Image;
import org.openimaj.video.Video;
import org.openimaj.video.processor.VideoProcessor;

/**
 *	A video translator is a video processor where the input and output
 *	frame types may be different. This means that no processing can take
 *	place in place but new frames must be returned.
 *	<p>	
 *	Although it overrides {@link VideoProcessor}, this processor must be
 *	used in chain mode - that is, it appears as a {@link Video} of the output
 *	type while taking a video of the input type.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 1 Mar 2012
 *	@version $Author$, $Revision$, $Date$
 *
 * 	@param <INPUT> 
 * 	@param <OUTPUT> 
 */
public abstract class VideoTranslator<INPUT extends Image<?,INPUT>,
	OUTPUT extends Image<?,OUTPUT>> 
	extends Video<OUTPUT>
{
	/** The input video */
	private Video<INPUT> video = null;
	
	/** The last processed frame */
	private OUTPUT currentFrame = null;
	
	/**
	 * 	Construct a new VideoTranslator that will translate
	 * 	the given input video.
	 *	@param in The input video.
	 */
	public VideoTranslator( Video<INPUT> in )
	{
		this.video = in;
	}
	
	@Override
	public double getFPS()
	{
		return video.getFPS();
	}
	
	@Override
	public OUTPUT getCurrentFrame()
	{
		return currentFrame;
	}

	@Override
	public int getWidth()
	{
		return video.getWidth();
	}

	@Override
	public int getHeight()
	{
		return video.getHeight();
	}

	@Override
	public long getTimeStamp()
	{
		return video.getTimeStamp();
	}

	@Override
	public boolean hasNextFrame()
	{
		return video.hasNextFrame();
	}

	@Override
	public long countFrames()
	{
		return video.countFrames();
	}

	@Override
	public void reset()
	{
		video.reset();
	}

	@Override
	public OUTPUT getNextFrame()
	{
		return currentFrame = translateFrame( video.getNextFrame() );
	}

	/**
	 * 	Translate the given input frame to the appropriate output frame.
	 * 
	 *	@param nextFrame The input frame.
	 *	@return The output frame
	 */
	public abstract OUTPUT translateFrame( INPUT nextFrame );
}
