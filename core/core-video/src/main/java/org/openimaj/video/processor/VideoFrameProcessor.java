/**
 * 
 */
package org.openimaj.video.processor;

import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.video.Video;

/**
 *	This class is a {@link VideoProcessor} that uses an {@link ImageProcessor}
 *	for processing frames of a video.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 27 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 *
 *	@param I The image type that this processor will process
 *	@param X The video type that this processor will process
 */
public class VideoFrameProcessor<I extends Image<?,I>> 
	extends VideoProcessor<I>
{
	/** The processor that will be used to process frames */
	private ImageProcessor<I> processor = null;

	/**
	 * 	Non-chainable constructor
	 *	@param processor the processor to use
	 */
	public VideoFrameProcessor( ImageProcessor<I> processor )
	{
		this.processor = processor;
	}

	/**
	 * 	Chainable constructor.
	 *	@param video The video to process
	 */
	public VideoFrameProcessor( Video<I> video, ImageProcessor<I> processor )
	{
		super( video );
		this.processor = processor;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
	public I processFrame( I frame )
	{
		return frame.process( this.processor );
	}
}
