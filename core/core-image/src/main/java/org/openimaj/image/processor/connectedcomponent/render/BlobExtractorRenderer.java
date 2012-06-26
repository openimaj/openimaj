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
package org.openimaj.image.processor.connectedcomponent.render;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;

/**
 * 	Renders the pixels from one image into the given image based on
 * 	the connected components.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *  @param <T>  The type of image the rendered can write to
 */
public class BlobExtractorRenderer<T> extends AbstractRenderer<T>
{
	/** The image the pixels are being copied from */
	private Image<T,?> copyImage;
	
	/**
	 * 	Default constructor that takes the image to draw into and the image to
	 * 	copy from.	
	 * 
	 *  @param image The image to draw into
	 *  @param copyImage The image to copy pixels from
	 */
	public BlobExtractorRenderer( Image<T,?> image, Image<T,?> copyImage ) 
	{
		super( image, null );
		this.copyImage = copyImage;
	}
	
	/**
	 * 	Default constructor that takes the image to draw into and the image to
	 * 	copy from.	
	 * @param width The width of the image to create 
	 * @param height The height of the image to create
	 * @param img The image to copy pixels from
	 */
	public BlobExtractorRenderer( int width, int height, Image<T,?> img) 
	{
		super( width, height, null );
		copyImage = img;
	}

	/**
	 * 	Copies pixels that are within the connected component from the copyImage
	 * 	into the write image.
	 * 
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor#process(org.openimaj.image.pixel.ConnectedComponent)
	 */
	@Override
	public void process( ConnectedComponent cc ) 
	{
		for( Pixel p : cc.getPixels() ) 
		{
			image.setPixel( p.x, p.y, copyImage.getPixel( p.x, p.y ) );
		}
	}
}
