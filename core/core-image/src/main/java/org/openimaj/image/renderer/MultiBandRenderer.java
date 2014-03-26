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
package org.openimaj.image.renderer;

import org.openimaj.image.MultiBandImage;
import org.openimaj.image.SingleBandImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * Abstract base for {@link ImageRenderer}s that work on {@link MultiBandImage}
 * s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The pixel type
 * @param <I>
 *            The concrete subclass type
 * @param <S>
 *            The concrete subclass type of each band
 */
public abstract class MultiBandRenderer<T extends Comparable<T>, I extends MultiBandImage<T, I, S>, S extends SingleBandImage<T, S>>
		extends
		ImageRenderer<T[], I>
{
	/**
	 * Construct with given target image.
	 * 
	 * @param targetImage
	 *            the target image.
	 */
	public MultiBandRenderer(final I targetImage) {
		super(targetImage);
	}

	/**
	 * Construct with given target image and rendering hints.
	 * 
	 * @param targetImage
	 *            the target image.
	 * @param hints
	 *            the render hints
	 */
	public MultiBandRenderer(final I targetImage, final RenderHints hints) {
		super(targetImage, hints);
	}

	/**
	 * Draws the given single band image onto each band at the given position.
	 * Side-affects this image. The single band image must be of the same type
	 * as the bands within this image.
	 * 
	 * @param image
	 *            A {@link SingleBandImage} to draw
	 * @param x
	 *            The x-coordinate for the top-left of the drawn image
	 * @param y
	 *            The y-coordinate for the top-left of the drawn image
	 */
	public void drawImage(final S image, final int x, final int y) {
		for (final S band : this.targetImage.bands)
			band.createRenderer(this.hints).drawImage(image, x, y);
	}

	/**
	 * Draws the given single band image onto the specific band at the given
	 * position. Side-affects this image. The single band image must be of the
	 * same type as the bands within this image.
	 * 
	 * @param image
	 *            A {@link SingleBandImage} to draw
	 * @param band
	 *            The band onto which the image will be drawn
	 * @param x
	 *            The x-coordinate for the top-left of the drawn image
	 * @param y
	 *            The y-coordinate for the top-left of the drawn image
	 */
	public void drawImage(final S image, final int band, final int x, final int y) {
		this.targetImage.bands.get(band).createRenderer(this.hints).drawImage(image, x, y);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, double,
	 *      int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(final int x1, final int y1, final double theta, final int length, final int thickness, T[] grey)
	{
		grey = this.sanitise(grey);

		for (int i = 0; i < this.targetImage.bands.size(); i++) {
			this.targetImage.bands.get(i).createRenderer(this.hints).drawLine(x1, y1, theta, length, thickness, grey[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, int,
	 *      int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(final int x0, final int y0, final int x1, final int y1, final int thickness, T[] grey) {
		grey = this.sanitise(grey);

		for (int i = 0; i < this.targetImage.bands.size(); i++) {
			this.targetImage.bands.get(i).createRenderer(this.hints).drawLine(x0, y0, x1, y1, thickness, grey[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, int,
	 *      int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(final float x0, final float y0, final float x1, final float y1, final int thickness, T[] grey) {
		grey = this.sanitise(grey);

		for (int i = 0; i < this.targetImage.bands.size(); i++) {
			this.targetImage.bands.get(i).createRenderer(this.hints).drawLine(x0, y0, x1, y1, thickness, grey[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.renderer.ImageRenderer#drawPoint(org.openimaj.math.geometry.point.Point2d,
	 *      java.lang.Object, int)
	 */
	@Override
	public void drawPoint(final Point2d p, T[] col, final int size) {
		col = this.sanitise(col);
		for (int i = 0; i < this.targetImage.bands.size(); i++)
			this.targetImage.bands.get(i).createRenderer(this.hints).drawPoint(p, col[i], size);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.renderer.ImageRenderer#drawPolygon(org.openimaj.math.geometry.shape.Polygon,
	 *      int, java.lang.Object)
	 */
	@Override
	public void drawPolygon(final Polygon p, final int thickness, T[] grey) {

		grey = this.sanitise(grey);

		for (int i = 0; i < this.targetImage.bands.size(); i++) {
			this.targetImage.bands.get(i).createRenderer(this.hints).drawPolygon(p, thickness, grey[i]);
		}
	}
}
