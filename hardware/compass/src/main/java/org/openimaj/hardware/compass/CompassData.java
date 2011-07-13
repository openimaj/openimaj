package org.openimaj.hardware.compass;

/**
 * 	A structure for storing compass data.
 * 
 * 	@author Jon Hare <dpd@ecs.soton.ac.uk>
 * 	@version $Author$, $Revision$, $Date$
 * 	@created 13 Jul 2011
 */
public class CompassData
{
	public double compass;
	public double pitch;
	public double roll;
	public double temperature;
	public double mx;
	public double my;
	public double mz;
	public double ax;
	public double ay;
	public double az;

	public String toString()
	{
		return String.format( "compass=%3.1f, pitch=%2.1f, roll=%2.1f",
		        compass, pitch, roll );
	}
}
