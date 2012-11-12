package org.openimaj.image.pixel.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.iterator.IterableIterator;

public class RectanglePyramidSampler implements Iterable<Rectangle> {
	Rectangle base;
	int nLevels;

	public RectanglePyramidSampler(Rectangle r, int nLevels)
	{
		this.base = r;
		this.nLevels = nLevels;
	}

	public List<List<Rectangle>> levelRectangles() {
		final List<List<Rectangle>> rects = new ArrayList<List<Rectangle>>();

		for (final RectangleSampler rs : IterableIterator.in(levelIterator())) {
			rects.add(rs.allRectangles());
		}
		return rects;
	}

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

	public static void main(String[] args) {
		final Rectangle r = new Rectangle(0, 0, 4, 4);
		final RectanglePyramidSampler s = new RectanglePyramidSampler(r, 3);

		// for (final RectangleSampler rs :
		// IterableIterator.in(s.levelIterator())) {
		// for (final Rectangle rr : rs) {
		// System.out.println(rr);
		// }
		// }

		for (final Rectangle rr : s) {
			System.out.println(rr);
		}
	}
}
