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
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.ImageRenderer;

/**
 * 	A component renderer that will draw into the given image the principle
 * 	axes of the connected component. 
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 *  @param <T> The type of image into which to draw the axes
 */
public class AxisRenderer<T> extends AbstractRenderer<T> 
{
	/** The length to draw the major axis line */
	int majorSize = 100;
	
	/** The length to draw the minor axis line */
	int minorSize = 50;
	
	/**
	 * 	Default constructor that takes an image to draw into and a colour
	 * 	in which to draw the axes.
	 * 
	 *  @param image The image to draw into
	 *  @param colour The colour to draw the axes
	 */
	public AxisRenderer(Image<T,?> image, T colour) {
		super(image, colour);
	}
	
	/**
	 * 	Constructor that creates an image of the given size and a colour
	 * 	in which to draw the axes.
	 * @param width The width of the image to create
	 * @param height The height of the image to create
	 * @param colour The colour in which to draw the axes
	 */
	public AxisRenderer(int width, int height, T colour) {
		super(width, height, colour);
	}
	
	/**
	 * 	Draws the principle axes of the connected component into the image.
	 * 
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor#process(org.openimaj.image.pixel.ConnectedComponent)
	 */
	@Override
	public void process(ConnectedComponent cc) {
		Pixel cp = cc.calculateCentroidPixel();
		double theta = cc.calculateDirection();
		
		ImageRenderer<T, ?> r = image.createRenderer();
		
		r.drawLine(cp.x, cp.y, theta, majorSize, colour);
		r.drawLine(cp.x, cp.y, theta, -majorSize, colour);
		r.drawLine(cp.x, cp.y, (Math.PI/2.0)+theta, minorSize, colour);
		r.drawLine(cp.x, cp.y, (Math.PI/2.0)+theta, -minorSize, colour);
	}
}
