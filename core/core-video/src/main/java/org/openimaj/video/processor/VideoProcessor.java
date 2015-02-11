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
package org.openimaj.video.processor;

import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.video.Video;

/**
 * A super class for classes which are able to process videos. This class can be
 * used in two ways: one as a stand-alone video processor and another as a
 * chainable video processor.
 * <p>
 * As a stand-alone processor it may be used as such:
 * {@code
 * 		MyVideoProcessor vp = new MyVideoProcessor();
 * 		vp.process( myVideo );
 * 	}
 * <p>
 * As a chainable processor it may be used as so:
 * {@code
 * 		MyVideoProcessor vp = new MyVideoProcessor( myVideo );
 * 		MyOtherProcessor op = new MyOtherProcessor( vp );
 * 		AnotherProcessor ap = new AnotherProcessor( op );
 * 		ap.process();
 * 	}
 * <p>
 * If any of the chain-based functions are called when the video has not been
 * set, an {@link UnsupportedOperationException} is thrown.
 * <p>
 * In the same way that {@link ImageProcessor}s are expected to change the image
 * content, video processors should change the video frame content in place,
 * returning new altered frames. If you do not need to do this then use the
 * VideoAnalyser.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of {@link Image}
 * @created 1 Jun 2011
 */
public abstract class VideoProcessor<T extends Image<?, T>>
extends Video<T>
{
	/** The video in a video processing chain */
	private Video<T> video = null;

	/** A buffer of the current frame */
	private T currentFrame = null;

	/**
	 * Default constructor for using the video processor in an ad-hoc manner.
	 */
	public VideoProcessor()
	{
	}

	/**
	 * Constructor for creating a video processor which is chainable.
	 *
	 * @param v
	 *            The video to process
	 */
	public VideoProcessor(Video<T> v)
	{
		this.video = v;
	}

	/**
	 * Process a frame in this video. The processor must determine itself what
	 * is to be done with the frame that is processed. It is suggest that
	 * subclass processors add listeners for processed frames if they are
	 * required. The implementation must process the frame in-place and the
	 * frame should be returned.
	 *
	 * @param frame
	 *            The frame to process.
	 * @return the processed frame
	 */
	public abstract T processFrame(T frame);

	/**
	 * A hook for subclasses to be called when processing for the video has
	 * completed to clean up after themselves.
	 */
	public void processingComplete()
	{
		// No implementation
	}

	/**
	 * Process the given video using this processor.
	 *
	 * @param video
	 *            The video to process.
	 */
	public void process(Video<T> video)
	{
		T frame = null;
		while ((frame = video.getNextFrame()) != null)
			processFrame(frame);
		processingComplete();
	}

	/**
	 * This is a sugar function that will call {@link #process(Video)} with the
	 * current video (for chainable processors).
	 */
	public void process()
	{
		if (this.video == null)
			throw new UnsupportedOperationException(
					"Chain method called on non-chainable processor");
		process(this.video);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getWidth()
	 */
	@Override
	public int getWidth()
	{
		if (this.video == null)
			throw new UnsupportedOperationException(
					"Chain method called on non-chainable processor");
		return this.video.getWidth();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getHeight()
	 */
	@Override
	public int getHeight()
	{
		if (this.video == null)
			throw new UnsupportedOperationException(
					"Chain method called on non-chainable processor");
		return this.video.getHeight();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getNextFrame()
	 */
	@Override
	public T getNextFrame()
	{
		if (this.video == null)
			throw new UnsupportedOperationException(
					"Chain method called on non-chainable processor");
		currentFrame = this.video.getNextFrame();
		if (currentFrame == null)
			return null;
		return processFrame(currentFrame);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#hasNextFrame()
	 */
	@Override
	public boolean hasNextFrame()
	{
		if (this.video == null)
			throw new UnsupportedOperationException("Chain method called on non-chainable processor");
		return this.video.hasNextFrame();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#hasNextFrame()
	 */
	@Override
	public long countFrames()
	{
		if (this.video == null)
			throw new UnsupportedOperationException("Chain method called on non-chainable processor");
		return this.video.countFrames();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getCurrentFrame()
	 */
	@Override
	public T getCurrentFrame()
	{
		if (this.video == null)
			throw new UnsupportedOperationException(
					"Chain method called on non-chainable processor");

		if (this.currentFrame == null)
			this.currentFrame = processFrame(getNextFrame());

		return currentFrame;
	}

	/**
	 * No implementation.
	 */
	@Override
	public void reset()
	{
		// No implementation
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getTimeStamp()
	 */
	@Override
	public long getTimeStamp()
	{
		return this.video.getTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
		return this.video.getFPS();
	}

}
