package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Scanbeam tree
 */
public class ScanBeamTree
{
	public double y; /* Scanbeam node y value */

	public ScanBeamTree less; /* Pointer to nodes with lower y */

	public ScanBeamTree more; /* Pointer to nodes with higher y */

	public ScanBeamTree( double yvalue )
	{
		y = yvalue;
	}
}
