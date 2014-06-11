package org.openimaj.image.contour;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A contour or hierarchical set of contours within an image.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Contour extends Polygon {
	/**
	 * The type of contour
	 */
	public ContourType type;
	/**
	 * sub contours
	 */
	public List<Contour> children = new ArrayList<Contour>();
	/**
	 * The parent contour, might be null
	 */
	public Contour parent;

	/**
	 * where the contour starts
	 */
	public Pixel start;
	Rectangle rect = null;

	/**
	 * Construct a contour with the given type and start at the origin
	 * 
	 * @param type
	 *            the type
	 */
	public Contour(ContourType type) {
		this.type = type;
		this.start = new Pixel(0, 0);
	}

	/**
	 * Construct a contour with the given type and start pixel
	 * 
	 * @param type
	 *            the type
	 * @param x
	 *            the x-ordinate of the start pixel
	 * @param y
	 *            the y-ordinate of the start pixel
	 */
	public Contour(ContourType type, int x, int y) {
		this.type = type;
		this.start = new Pixel(x, y);
	}

	/**
	 * Construct a contour with the given type and start pixel
	 * 
	 * @param type
	 *            the type
	 * @param p
	 *            the coordinate of the start pixel
	 */
	public Contour(ContourType type, Pixel p) {
		this.type = type;
		this.start = p;
	}

	/**
	 * Construct a contour with the given starting coordinate and a
	 * <code>null</code> type.
	 * 
	 * @param x
	 *            the x-ordinate of the start pixel
	 * @param y
	 *            the y-ordinate of the start pixel
	 */
	public Contour(int x, int y) {
		this.type = null;
		this.start = new Pixel(x, y);
	}

	protected void setParent(Contour bp) {
		this.parent = bp;
		bp.children.add(this);
	}

	@Override
	public String toString() {
		final StringWriter contour = new StringWriter();
		final PrintWriter pw = new PrintWriter(contour);
		pw.println(String.format("[%s] start: %s %s", this.type, this.start, this.points));
		for (final Contour child : this.children) {
			pw.print(child);
		}
		pw.flush();
		return contour.toString();
	}

	/**
	 * Complete this contour by computing it's bounding box
	 */
	public void finish() {
		this.rect = this.calculateRegularBoundingBox();
	}

	private class ContourIterator implements Iterator<Contour> {
		final List<Contour> toProcess = new ArrayList<Contour>();

		ContourIterator() {
			toProcess.add(Contour.this);
		}

		@Override
		public boolean hasNext() {
			return !toProcess.isEmpty();
		}

		@Override
		public Contour next() {
			final Contour next = toProcess.remove(toProcess.size() - 1);
			toProcess.addAll(next.children);
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Removal not supported");
		}
	}

	/**
	 * Get an iterator over the complete set of contours (including the root
	 * itself)
	 * 
	 * @return the iterator
	 */
	public Iterator<Contour> contourIterator() {
		return new ContourIterator();
	}

	/**
	 * Get an {@link Iterable} over all the contours belonging to this root
	 * (including the root itself)
	 * 
	 * @return the iterable
	 */
	public Iterable<Contour> contourIterable() {
		return new Iterable<Contour>() {
			@Override
			public Iterator<Contour> iterator() {
				return contourIterator();
			}
		};
	}

	@Override
	public boolean isHole() {
		return type != null && type.equals(ContourType.HOLE);
	}
}
