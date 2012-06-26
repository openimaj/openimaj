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
package org.openimaj.image.processor.connectedcomponent.render;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.ConnectedComponent;

/**
 * 	Renders a connected component's centroid into the given image.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 *  @param <T> The image type into which this renderer can draw
 */
public class CentroidRenderer<T> extends AbstractRenderer<T> 
{
	/**
	 * 	Default constructor that takes the image to draw into
	 * 	and the colour to draw the centroid.
	 * 
	 *  @param image the image to draw into
	 *  @param colour The colour to draw the centroid.
	 */
	public CentroidRenderer(Image<T,?> image, T colour) {
		super(image, colour);
	}
	
	/**
	 * 	Constructor that creates the image to draw into
	 * 	and takes the colour to draw the centroid.
	 * @param width the width of the image to create
	 * @param height the height of the image to create
	 * @param colour The colour to draw the centroid.
	 */
	public CentroidRenderer(int width, int height, T colour) {
		super(width, height, colour);
	}
	
	/**
	 * 	Draws the component's centroid into the image as a small 5 pixel square
	 * 	centred on the centroid.
	 * 
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor#process(org.openimaj.image.pixel.ConnectedComponent)
	 */
	@Override
	public void process(ConnectedComponent cc) {
		double [] centroid = cc.calculateCentroid();
		for (int i=-5; i<=5; i++) {
			int y = (int)(Math.round(centroid[1]));
			int x = (int)(Math.round(centroid[0]));
			
			if (i+x>0 && i+x<image.getWidth()) image.setPixel(x+i, y, colour);
			if (i+y>0 && i+y<image.getHeight()) image.setPixel(x, y+i, colour);
		}
	}
}
