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

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.iterator.IterableIterator;

/**
 * A {@link QuadtreeSampler} provides an easy way of extracting sample patches
 * from an image or other domain in both spatial and scale directions. The
 * sampling regions returned by the sampler form a quadtree. At the first level
 * there is a single sample region covering the entire region; at the second
 * level, there are 4 non-overlapping sample regions, and so on.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class QuadtreeSampler implements Iterable<Rectangle> {
	Rectangle base;
	int nLevels;

	/**
	 * Construct the sampler with the given parameters
	 * 
	 * @param r
	 *            the size of the initial sample region on the first level
	 * @param nLevels
	 *            the number of levels to sample over
	 */
	public QuadtreeSampler(Rectangle r, int nLevels)
	{
		this.base = r;
		this.nLevels = nLevels;
	}

	/**
	 * Get all the sampling regions grouped by level
	 * 
	 * @return the sampling regions grouped by level
	 */
	public List<List<Rectangle>> levelRectangles() {
		final List<List<Rectangle>> rects = new ArrayList<List<Rectangle>>();

		for (final RectangleSampler rs : IterableIterator.in(levelIterator())) {
			rects.add(rs.allRectangles());
		}
		return rects;
	}

	/**
	 * Get all the sampling regions
	 * 
	 * @return all the sampling regions
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
			Iterator<RectangleSampler> levelIter = levelIterator();
			Iterator<Rectangle> rectIter = levelIter.hasNext() ? levelIter.next().iterator() : null;

			@Override
			public boolean hasNext() {
				if (rectIter != null && rectIter.hasNext())
					return true;

				return levelIter.hasNext();
			}

			@Override
			public Rectangle next() {
				if (!rectIter.hasNext()) {
					rectIter = levelIter.next().iterator();
				}

				return rectIter.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal is not supported!");
			}

		};
	}

	/**
	 * Create an iterator over each of the levels in the pyramid. Each level is
	 * represented by a {@link RectangleSampler} that describes the individual
	 * rectangles on a level.
	 * 
	 * @return an iterator over the levels.
	 */
	public Iterator<RectangleSampler> levelIterator() {
		return new Iterator<RectangleSampler>() {
			int level = 0;

			@Override
			public boolean hasNext() {
				return level < nLevels;
			}

			@Override
			public RectangleSampler next() {
				if (level >= nLevels)
					throw new NoSuchElementException();

				final float sz = (float) Math.pow(2, level);
				final float dx = base.width / sz;
				final float dy = base.height / sz;

				final RectangleSampler r = new RectangleSampler(base, dx, dy, dx, dy);
				level++;

				return r;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal is not supported!");
			}
		};
	}
}
