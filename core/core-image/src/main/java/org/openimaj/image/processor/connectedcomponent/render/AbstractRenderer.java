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

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;

/**
 * 	An abstract class that defines top-level methods for objects that can
 * 	render connected components into images.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 *  @param <T>  The type of image the rendered can write to
 */
public abstract class AbstractRenderer<T> implements ConnectedComponentProcessor 
{
	/** The image that the rendered will write to */
	protected Image<T,?> image;
	
	/** The colour the connected component will be written in */
	protected T colour;
	
	/**
	 * 	Default constructor that takes the image to write to and the
	 * 	colour to draw in.
	 * 
	 *  @param image The image to write to
	 *  @param colour The colour to write in
	 */
	public AbstractRenderer(Image<T,?> image, T colour) 
	{	
		this.image = image;
		this.colour = colour;
	}
	
	/**
	 *	Constructor that creates a new image into which to render the
	 *	connected components and a colour to draw in.
	 * @param width The width of the image to create
	 * @param height The height of the image to create
	 * @param colour The colour to draw in
	 */
	@SuppressWarnings("unchecked")
	public AbstractRenderer(int width, int height, T colour) 
	{
		if (Float.class.isAssignableFrom(colour.getClass())) {
			image = (Image<T,?>)(Object)new FImage(width, height);
		} else if (Float[].class.isAssignableFrom(colour.getClass())) {
			image = (Image<T,?>)(Object)new MBFImage(width, height, ((Float[])colour).length);
		} else {
			throw new IllegalArgumentException("Unknown/unsupported type");
		}
		this.colour = colour;
	}
	
	/**
	 * 	Returns the colour that the components will be drawn in.
	 * 
	 *  @return the colour that the components will be drawn in.
	 */
	public T getColour() {
		return colour;
	}
	
	/**
	 * 	Set the colour that the components will be drawn in.
	 * 
	 *  @param colour the colour to draw components.
	 */
	public void setColour(T colour) {
		this.colour = colour;
	}
	
	/**
	 * 	Returns the image that is being rendered on.
	 * 
	 *  @return The image that is being rendered on.
	 */
	public Image<T,?> getImage() {
		return image;
	}
}
