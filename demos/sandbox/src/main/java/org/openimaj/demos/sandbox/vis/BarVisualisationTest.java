/**
 * 
 */
package org.openimaj.demos.sandbox.vis;

import org.openimaj.vis.general.BarVisualisation;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 30 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class BarVisualisationTest
{
	/**
	 *	@param args
	 */
	public static void main( String[] args )
    {
	    double[] data1 = new double[]{ -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	    BarVisualisation bv = new BarVisualisation( 1000, 400 );
	    bv.setData( data1 );
	    bv.showWindow( "Data" );
	}
}
