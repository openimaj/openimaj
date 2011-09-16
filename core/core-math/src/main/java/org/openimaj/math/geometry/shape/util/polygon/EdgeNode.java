package org.openimaj.math.geometry.shape.util.polygon;

import java.awt.geom.Point2D;

public class EdgeNode
{
	public Point2D.Double vertex = new Point2D.Double(); /*
												 * Piggy-backed contour vertex
												 * data
												 */

	public Point2D.Double bot = new Point2D.Double(); /* Edge lower (x, y) coordinate */

	public Point2D.Double top = new Point2D.Double(); /* Edge upper (x, y) coordinate */

	public double xb; /* Scanbeam bottom x coordinate */

	public double xt; /* Scanbeam top x coordinate */

	public double dx; /* Change in x for a unit y increase */

	public int type; /* PolygonUtils / subject edge flag */

	public int[][] bundle = new int[2][2]; /* Bundle edge flags */

	public int[] bside = new int[2]; /* Bundle left / right indicators */

	public BundleState[] bstate = new BundleState[2]; /* Edge bundle state */

	public PolygonNode[] outp = new PolygonNode[2]; /* Output polygon / tristrip pointer */

	public EdgeNode prev; /* Previous edge in the AET */

	public EdgeNode next; /* Next edge in the AET */

	// EdgeNode pred; /* Edge connected at the lower end */
	public EdgeNode succ; /* Edge connected at the upper end */

	public EdgeNode next_bound; /* Pointer to next bound in LMT */
}
