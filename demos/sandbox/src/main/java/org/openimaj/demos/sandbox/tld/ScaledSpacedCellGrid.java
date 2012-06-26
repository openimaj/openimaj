package org.openimaj.demos.sandbox.tld;

import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A {@link SpacedCellGrid} with a notion of the scale which generated it 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ScaledSpacedCellGrid extends SpacedCellGrid{

	private double scale;

	/**
	 * Calls {@link SpacedCellGrid#SpacedCellGrid(Rectangle, int, double, double, double, double)} and saves the scale also
	 * @param bounds
	 * @param padding
	 * @param cellwidth
	 * @param cellheight
	 * @param dx
	 * @param dy
	 * @param scale
	 */
	public ScaledSpacedCellGrid(Rectangle bounds, int padding,double cellwidth, double cellheight, double dx, double dy, double scale) {
		super(bounds, padding, cellwidth, cellheight, dx, dy);
		this.setScale(scale);
	}

	/**
	 * @param scale
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

}
