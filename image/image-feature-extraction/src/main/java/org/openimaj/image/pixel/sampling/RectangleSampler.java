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
package org.openimaj.image.pixel.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.iterator.IterableIterator;

/**
 * A {@link RectangleSampler} provides an easy way to generate a sliding window
 * of rectangle over an image or other domain.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class RectangleSampler implements Iterable<Rectangle> {
	float minx;
	float maxx;
	float miny;
	float maxy;
	float stepx;
	float stepy;
	float width;
	float height;

	public RectangleSampler(float minx, float maxx, float miny, float maxy, float stepx, float stepy, float width,
			float height)
	{
		setBounds(minx, maxx, miny, maxy);
		this.stepx = stepx;
		this.stepy = stepy;
		this.width = width;
		this.height = height;
	}

	public RectangleSampler(Rectangle bounds, float stepx, float stepy, float width, float height)
	{
		setBounds(bounds);
		this.stepx = stepx;
		this.stepy = stepy;
		this.width = width;
		this.height = height;
	}

	public RectangleSampler(Image<?, ?> img, float stepx, float stepy, float width, float height)
	{
		setBounds(img);
		this.stepx = stepx;
		this.stepy = stepy;
		this.width = width;
		this.height = height;
	}

	public void setBounds(float minx, float maxx, float miny, float maxy) {
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
	}

	public void setBounds(Rectangle r) {
		if (r == null)
			return;

		this.minx = r.x;
		this.maxx = r.x + r.width;
		this.miny = r.y;
		this.maxy = r.y + r.height;
	}

	public void setBounds(Image<?, ?> img) {
		if (img == null)
			return;

		setBounds(img.getBounds());
	}

	/**
	 * Get a list of all the rectangles that can be produced by this sampler
	 * 
	 * @return all the rectangles
	 */
	public List<Rectangle> allRectangles() {
		final List<Rectangle> list = new ArrayList<Rectangle>();

		for (final Rectangle r : this)
			list.add(r);

		return list;
	}

	@Override
	public Iterator<Rectangle> iterator() {
		return new Iterator<Rectangle>() {
			float x = minx;
			float y = miny;

			@Override
			public boolean hasNext() {
				if (x + width <= maxx && y + height <= maxy)
					return true;

				return false;
			}

			@Override
			public Rectangle next() {
				if (y + height > maxy)
					throw new NoSuchElementException();

				float nextX = x + stepx;
				float nextY = y;
				if (nextX + width > maxx) {
					nextX = minx;
					nextY += stepy;
				}

				final Rectangle r = new Rectangle(x, y, width, height);

				x = nextX;
				y = nextY;

				return r;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal is not supported!");
			}
		};
	}

	/**
	 * Create an iterator to extract sub-images from an image based on the
	 * rectangles defined by this sampler.
	 * 
	 * @param image
	 *            the image to extract from
	 * @return an iterator over the extracted sub-images.
	 */
	public <I extends Image<?, I>> Iterator<I> subImageIterator(final I image) {
		return new Iterator<I>() {
			Iterator<Rectangle> inner = iterator();

			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}

			@Override
			public I next() {
				final Rectangle r = inner.next();

				return image.extractROI(r);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal is not supported!");
			}
		};
	}

	public static void main(String[] args) {
		final FImage img = new FImage(300, 300);

		final RectangleSampler s = new RectangleSampler(img, 100, 100, 100, 100);
		for (final Rectangle r : s) {
			System.out.println(r);
			img.drawShape(r, 1f);
		}

		for (final FImage i : IterableIterator.in(s.subImageIterator(img)))
			System.out.println(i.width);

		DisplayUtilities.display(img);
	}
}
