/**
 *
 */
package org.openimaj.math.geometry.shape;


/**
 *	A rectangle in 3D space. (x,y,z) provide the position (of top left) there
 *	are rotations for each of the axes
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 1 Aug 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class Rectangle3D extends Rectangle
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The Z position */
	public float z = 0;

	/** The rotation in the x axis */
	public double xRotation = 0;

	/** The rotation in the x axis */
	public double yRotation = 0;

	/** The rotation in the x axis */
	public double zRotation = 0;

	@Override
	public String toString()
	{
		return String.format("Rectangle[x=%2.2f,y=%2.2f,z=%2.2f,width=%2.2f,height=%2.2f]", this.x, this.y, this.z, this.width, this.height);
	}
}
