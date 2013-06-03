/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Circle;

/**
 *	Plots blobs proportional to the size of the value. This can be used as a
 *	visualisation in itself or used as an {@link ItemPlotter} in other visualisations.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jun 2013
 */
public class DotPlotVisualisation extends XYPlotVisualisation<Double>
	implements ItemPlotter<Double,Float[]>
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public DotPlotVisualisation()
	{
		super( null );
		this.setItemPlotter( this );
	}

	/**
	 *	@param i
	 *	@param j
	 */
	public DotPlotVisualisation( final int i, final int j )
	{
		super( i, j, null );
		this.setItemPlotter( this );
	}


	@Override
	public void plotObject( final MBFImage visImage,
			final XYPlotVisualisation.LocatedObject<Double> object,
			final AxesRenderer<Float[]> renderer )
	{
		visImage.createRenderer().drawShapeFilled(
				new Circle( renderer.calculatePosition( visImage,
						object.x, object.y ),
						(float)(object.object.doubleValue() * renderer.getxUnitSizePx()) ),
				RGBColour.RED );
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		final DotPlotVisualisation dpv = new DotPlotVisualisation( 1000, 600 );

		for( int i = 0; i < 10; i++ )
			dpv.addPoint( (Math.random()-0.5)*2, (Math.random()-0.5)*2,
					Math.random()/10 );

		dpv.updateVis();

		DisplayUtilities.display( dpv.visImage );
	}
}
