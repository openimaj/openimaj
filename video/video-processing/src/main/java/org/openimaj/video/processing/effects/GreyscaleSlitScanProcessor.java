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
package org.openimaj.video.processing.effects;

import java.util.LinkedList;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.Video;
import org.openimaj.video.processor.VideoProcessor;

/**
 * {@link VideoProcessor} that produces a slit-scan effect based on the time-map
 * in a greyscale image.
 *
 * 	@author Sina Samangooei (ss@ecs.soton.ac.uk)
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class GreyscaleSlitScanProcessor extends VideoProcessor<MBFImage>
{
	/** The list of previous images */
	private final LinkedList<MBFImage> cache = new LinkedList<MBFImage>();

	/** The kernel to blur with */
	private final float[] blurKern = FGaussianConvolve.makeKernel( 0.5f );

	/** The number of images in the cache -> more = slower */
	private int cacheSize = 240;

	/** The timemap image */
	private FImage timemapImage = null;

	/** First time through we'll always fix the timemap */
	private boolean needToFixTimemap = true;

	/**
	 * 	Default constructor for using the video processor in an ad-hoc manner. Uses a default
	 * 	cache size of 240 steps.
	 *
	 * 	@param timemap The time map image
	 */
	public GreyscaleSlitScanProcessor( final FImage timemap )
	{
		this( timemap, 240 );
	}

	/**
	 * Default constructor for using the video processor in an ad-hoc manner.
	 *
	 * @param timemap The time map image
	 * @param cacheSize The number of frames to retain for creating the slitscan
	 *            effect
	 */
	public GreyscaleSlitScanProcessor( final FImage timemap, final int cacheSize )
	{
		this.cacheSize = cacheSize;
		this.timemapImage = timemap;
	}

	/**
	 * Constructor for creating a video processor which is chainable.
	 *
	 * @param video The video to process
	 * @param timemap the time map
	 * @param cacheSize The number of frames to retain for creating the slitscan
	 *            effect
	 */
	public GreyscaleSlitScanProcessor( final Video<MBFImage> video, final FImage timemap, final int cacheSize )
	{
		super( video );
		this.cacheSize = cacheSize;
		this.timemapImage = timemap;
	}

	/**
	 * Default constructor for using the video processor in an ad-hoc manner.
	 *
	 * @param cacheSize
	 *            The number of frames to retain for creating the slitscan
	 *            effect
	 */
	public GreyscaleSlitScanProcessor( final int cacheSize )
	{
		this.cacheSize = cacheSize;
	}

	/**
	 * Constructor for creating a video processor which is chainable.
	 *
	 * @param video The video to process
	 * @param cacheSize The number of frames to retain for creating the slitscan
	 *            effect
	 */
	public GreyscaleSlitScanProcessor( final Video<MBFImage> video, final int cacheSize )
	{
		super( video );
		this.cacheSize = cacheSize;
	}

	@Override
	public MBFImage processFrame( final MBFImage frame )
	{
		this.addToCache( frame );

		if( this.timemapImage == null || this.timemapImage.getWidth() != frame.getWidth() ||
				this.timemapImage.getHeight() != frame.getHeight() )
			this.needToFixTimemap = true;

		if( this.needToFixTimemap )
			this.fixTimemapImage( frame.getWidth(), frame.getHeight() );

		final int height = frame.getHeight();
		final int width = frame.getWidth();

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				int index = (int)this.timemapImage.pixels[y][x];
				if( index >= this.cache.size() )
					index = this.cache.size()-1;

				final MBFImage cacheImage = this.cache.get( index );
				frame.setPixel( x, y, cacheImage.getPixel(x,y) );
			}
		}

		for( final FImage f : frame.bands )
		{
			FImageConvolveSeparable.convolveVertical( f, this.blurKern );
		}

		if( this.cache.size() >= this.cacheSize ) this.cache.removeLast();

		return frame;
	}

	/**
	 * 	Fixes the timemap image to be the same width/height as the given value.
	 * 	Will generate a default one if the image is null
	 *
	 *	@param width The width to resize the timemap to
	 *	@param height The height to resize the timemap to
	 */
	private void fixTimemapImage( final int width, final int height )
    {
		// Resize the timemap
		this.timemapImage = ResizeProcessor.resample( this.timemapImage, width, height );

		// Resample the colours to the number of cache sizes.
		for( int y = 0; y < this.timemapImage.getHeight(); y++ )
			for( int x = 0; x < this.timemapImage.getWidth(); x++ )
				this.timemapImage.pixels[y][x] = (float)
					(Math.floor( this.timemapImage.pixels[y][x] * this.cacheSize));
		this.needToFixTimemap = false;
    }

	private void addToCache( final MBFImage frame )
	{
		final MBFImage f = frame.clone();
		this.cache.addFirst( f );
	}
}
