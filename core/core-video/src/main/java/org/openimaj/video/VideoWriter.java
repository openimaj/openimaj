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

import org.openimaj.image.Image;
import org.openimaj.video.processor.VideoProcessor;

/**
 *	An abstract class which classes can override to provide
 *	video writing capabilities.
 *	<p>
 *	The {@link #addFrame(Image)} method must be overridden by implementing
 *	subclasses to actually perform the encoding and writing of the video frame.
 *	<p>
 *	The class is an extension of a video processor which allows the writer
 *	to act in a processing chain. The implementation of the {@link VideoProcessor}
 *	interface calls the {@link #addFrame(Image)} method for every frame that
 *	is passed in the processor.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 27 Jul 2011
 *	
 * 	@param <T> Type of {@link Image} 
 */
public abstract class VideoWriter<T extends Image<?,T>> 
	extends VideoProcessor<T>
{
	/** The frame rate at which written videos will be replayed */
	protected double frameRate = 25;
	
	/** The width of the video frames in pixels */
	protected int width = 720;
	
	/** The height of the video frames in pixels */
	protected int height = 576;
	
	/**
	 * 	Default constructor that takes the frame rate at which the written
	 * 	video will be replayed.
	 * 
	 * 	@param width The width of the video frames in pixels
	 * 	@param height The height of the video frames in pixels
	 *	@param frameRate The frame rate in frames per second
	 */
	public VideoWriter( int width, int height, double frameRate )
	{
		this.width = width;
		this.height = height;
		this.frameRate = frameRate;
	}
	
	/**
	 * 	Add a frame to the video stream.
	 *	@param frame The frame to add to the video stream
	 */
	public abstract void addFrame( T frame );
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
	public T processFrame( T frame )
	{
		if( frame.getWidth() == this.width && frame.getHeight() == this.height )
		{
			this.addFrame( frame );
			return frame;
		}
		
		throw new RuntimeException( 
				"Frame width and height ("+frame.getWidth()+"x"+
				frame.getHeight()+") does not match the video width and height ("+
				this.width+"x"+this.height+")" );
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getTimeStamp()
	 */
	@Override
    public long getTimeStamp()
    {
	    return (long)(getCurrentFrameIndex() / this.frameRate)*1000;
    }
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
		return this.frameRate;
	}
}
