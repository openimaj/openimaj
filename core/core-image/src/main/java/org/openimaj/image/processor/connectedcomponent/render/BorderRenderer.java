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

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;

/**
 * 	Draws the border of the connected component into the given image.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 *  @param <T> The type of image to draw into
 */
public class BorderRenderer<T> extends AbstractRenderer<T> 
{
	/** The connect mode to use to get the boundary */
	ConnectMode mode;
	
	/**
	 * 	Default constructor that takes the image to draw into, the colour
	 * 	to draw the boundary and the connect mode to use to extract the
	 * 	boundary.
	 * 
	 *  @param image The image to draw into.
	 *  @param colour The colour to use to draw the boundary
	 *  @param mode The {@link ConnectMode} to use to extract the boundary.
	 */
	public BorderRenderer(Image<T,?> image, T colour, ConnectMode mode) {
		super(image, colour);
		this.mode = mode;
	}
	
	/**
	 * 	Constructor that creates the image to draw into, and takes the colour
	 * 	to draw the boundary and the connect mode to use to extract the
	 * 	boundary.
	 * @param width The width of the image to create
	 * @param height The height of the image to create
	 * @param colour The colour to use to draw the boundary
	 * @param mode The {@link ConnectMode} to use to extract the boundary.
	 */
	public BorderRenderer(int width, int height, T colour, ConnectMode mode) {
		super(width, height, colour);
		this.mode = mode;
	}
	
	/**
	 * 	Draws the boundary of the connected component into the image.	
	 * 
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor#process(org.openimaj.image.pixel.ConnectedComponent)
	 */
	@Override
	public void process(ConnectedComponent cc) {
		List<Pixel> pset = cc.getInnerBoundary(mode);

		for (Pixel p : pset) {
			image.setPixel(p.x, p.y, colour);
		}
	}
}
