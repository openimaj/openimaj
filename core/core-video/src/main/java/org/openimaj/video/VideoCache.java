/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
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
	 *	{@inheritDoc}
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
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processor.VideoProcessor#reset()
	 */
	@Override
	public void reset()
	{
		frames.clear();
	}

	/**
	 * 	Cache the whole of the given video.
	 *  @param <I> Type of {@link Image} 
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
		while( video.hasNextFrame() && 
			   video.getCurrentFrameIndex() < end.getFrameNumber() )
			vc.addFrame( video.getNextFrame().clone() );
		return vc;
	}
}
