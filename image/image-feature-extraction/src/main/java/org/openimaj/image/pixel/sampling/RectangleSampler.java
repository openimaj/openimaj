package org.openimaj.image.pixel.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;

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
	public List<Rectangle> getAllRectangles() {
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
				final float nextX = x + stepx;
				if (nextX + width < maxx)
					return true;

				final float nextY = y + stepy;
				if (nextY + height < maxy)
					return true;

				return false;
			}

			@Override
			public Rectangle next() {
				float nextX = x + stepx;
				float nextY = y;
				if (nextX + width >= maxx) {
					nextX = minx;
					nextY += stepy;
				}

				if (nextY + height >= maxy)
					return null;

				x = nextX;
				y = nextY;

				return new Rectangle(x, y, width, height);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal is not supported!");
			}
		};
	}
}
