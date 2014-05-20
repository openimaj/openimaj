package org.openimaj.image.contour;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
}
