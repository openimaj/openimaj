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
package org.openimaj.image;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.ImageRenderer;

/**
 * A simple 2d plotter
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <Q> The pixel type that is processed
 * @param <I> The image type that is processed
 */
public class Plotter<Q, I extends Image<Q, I>> {
	protected Pixel penPosition = new Pixel(0, 0);
	protected Q penColour;
	protected int penThickness;
	protected I image;
	protected ImageRenderer<Q,I> renderer;
	
	/**
	 * Construct a plotter that writes to the given image.
	 * The pen colour is set to the value of the pixel at (0,0) 
	 * and the pen thickness is set to 1.
	 * 
	 * @param image the image
	 */
	public Plotter(I image) {
		this(image, image.getPixel(0, 0), 1);
	}
	
	/**
	 * Construct a plotter that writes to the given image with the
	 * given initial pen colour. The pen thickness is set to 1.
	 * 
	 * @param image the image
	 * @param colour the pen colour
	 */
	public Plotter(I image, Q colour) {
		this(image, colour, 1);
	}
	
	/**
	 * Construct a plotter that writes to the given image.
	 * 
	 * @param image the image
	 * @param colour the pen colour
	 * @param thickness the pen thickness
	 */
	public Plotter(I image, Q colour, int thickness) {
		this.image = image;
		this.renderer = image.createRenderer();
		penColour = colour;
		penThickness = thickness;
	}
	
	/**
	 * Move to a pixel
	 * @param p the pixel
	 */
	public void moveTo(Pixel p) {
		moveTo(p.x, p.y);
	}
	
	/**
	 * Move to a point
	 * @param x x-position
	 * @param y y-position
	 */
	public void moveTo(int x, int y) {
		penPosition.x = x;
		penPosition.y = y;
	}
	
	/**
	 * Draw a line from the current position to a pixel
	 * @param p the pixel to draw to
	 */
	public void lineTo(Pixel p) {
		lineTo(p.x, p.y);
	}
	
	/**
	 * Draw a line from the current position to a point
	 * @param x the x position
	 * @param y the y position
	 */
	public void lineTo(int x, int y) {
		renderer.drawLine(penPosition.x, penPosition.y, x, y, penThickness, penColour);
		moveTo(x, y);
	}

	/**
	 * @return the penPosition
	 */
	public Pixel getPenPosition() {
		return penPosition;
	}

	/**
	 * @param penPosition the penPosition to set
	 */
	public void setPenPosition(Pixel penPosition) {
		this.penPosition = penPosition;
	}

	/**
	 * @return the penColour
	 */
	public Q getPenColour() {
		return penColour;
	}

	/**
	 * @param penColour the penColour to set
	 */
	public void setPenColour(Q penColour) {
		this.penColour = penColour;
	}

	/**
	 * @return the penThickness
	 */
	public int getPenThickness() {
		return penThickness;
	}

	/**
	 * @param penThickness the penThickness to set
	 */
	public void setPenThickness(int penThickness) {
		this.penThickness = penThickness;
	}

	/**
	 * @return the image
	 */
	public I getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(I image) {
		this.image = image;
		this.renderer = image.createRenderer();
	}
}
