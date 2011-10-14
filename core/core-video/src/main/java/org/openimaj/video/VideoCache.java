/**
 * 
 */
package org.openimaj.video;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.video.timecode.VideoTimecode;

/**
 *	This class represents a cache of video material. It is also able to
 *	build the cache for you with the static methods.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 14 Oct 2011
 *
 *	@param <I> The type of video frames to be cached
 */
public class VideoCache<I extends Image<?,I>> extends VideoWriter<I>
{
	/** The cached frames */
	private List<I> frames = new ArrayList<I>();
	
	/**
	 *	Create a video cache for frames of the given size and for a video
	 *	of the given frame rate.
	 * 
	 *	@param width The width of the video frames
	 *	@param height The height of the video frames
	 *	@param frameRate The frame rate of the video
	 */
	public VideoCache( int width, int height, double frameRate )
	{
		super( width, height, frameRate );
	}

	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoWriter#addFrame(org.openimaj.image.Image)
	 */
	@Override
	public void addFrame( I frame )
	{
		frames.add( frame );
	}
	
	/**
	 * 	Returns an {@link ArrayBackedVideo} for the frames in this cache.
	 *	@return An {@link ArrayBackedVideo}
	 */
	@SuppressWarnings( "unchecked" )
	public ArrayBackedVideo<I> getArrayBackedVideo()
	{
		return new ArrayBackedVideo<I>( (I[])frames.toArray(), frameRate );
	}
	
	/**
	 * 	Returns the number of frames that have been cached.
	 *	@return The number of frames that have been cached.
	 */
	public int getNumberOfFrames()
	{
		return frames.size();
	}
	
	/**
	 * 	Returns the frame at the given index.
	 *	@param i The index to get the frame from
	 *	@return The frame at the given index
	 */
	public I getFrame( int i )
	{
		return frames.get(i);
	}
	
	/**
	 * 	Clears the cache.
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processor.VideoProcessor#reset()
	 */
	public void reset()
	{
		frames.clear();
	}

	/**
	 * 	Cache the whole of the given video.
	 * 
	 *	@param video The video to cache
	 *	@return A {@link VideoCache}
	 */
	public static <I extends Image<?,I>> VideoCache<I> cacheVideo( Video<I> video )
	{
		VideoCache<I> vc = new VideoCache<I>( video.getWidth(), 
				video.getHeight(), video.getFPS() );
		video.reset();
		while( video.hasNextFrame() )
			vc.addFrame( video.getNextFrame().clone() );
		return vc;
	}
	
	/**
	 * 	Cache the given time range from the given video.
	 * 
	 *	@param <I> The type of the video frames
	 *	@param video The video to cache
	 *	@param start The start of the video to cache
	 *	@param end The end of the video to cache
	 *	@return A {@link VideoCache}
	 */
	public static <I extends Image<?,I>> VideoCache<I> cacheVideo( Video<I> video,
			VideoTimecode start, VideoTimecode end )
	{
		VideoCache<I> vc = new VideoCache<I>( video.getWidth(), 
				video.getHeight(), video.getFPS() );
		video.setCurrentFrameIndex( start.getFrameNumber() );
		while( video.hasNextFrame() && video.getCurrentFrameIndex() < end.getFrameNumber() )
			vc.addFrame( video.getNextFrame().clone() );
		return vc;
	}
}
