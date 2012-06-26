package org.openimaj.demos.sandbox.tld;

/**
 * A grid is a set of points all representing the start of a rectangle with a fixed width and height.
 * Grids can be iterated to retrieve their top left and bottom right points
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class Grid implements Iterable<double[]>{
	/**
	 * the width of each grid cell
	 */
	public double cellwidth;
	/**
	 * the height of each grid cell
	 */
	public double cellheight;
	
	/**
	 * @return how many cells can be generatd by this grid
	 */
	public abstract int size();
	
	@Override
	public String toString() {
		return String.format("[%d %dx%d cells]",this.size(),(int)this.cellwidth,(int)this.cellheight);
	}
}
