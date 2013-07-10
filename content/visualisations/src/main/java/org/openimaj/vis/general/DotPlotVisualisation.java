/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.vis.general.DotPlotVisualisation.ColouredDot;

/**
 *	Plots blobs proportional to the size of the value. This can be used as a
 *	visualisation in itself or used as an {@link ItemPlotter} in other visualisations.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jun 2013
 */
public class DotPlotVisualisation extends XYPlotVisualisation<ColouredDot>
	implements ItemPlotter<ColouredDot,Float[],MBFImage>
{
	/**
	 * 	A dot with a specific size and colour.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 11 Jun 2013
	 */
	public static class ColouredDot
	{
		/** The size of the dot */
		public double size;

		/** The colour of the dot */
		public Float[] colour;

		/**
		 *	@param size
		 *	@param colour
		 */
		public ColouredDot( final double size, final Float[] colour )
		{
			this.size = size;
			this.colour = colour;
		}
	}

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 *	Default construcotr
	 */
	public DotPlotVisualisation()
	{
		super( null );
		this.setItemPlotter( this );
	}

	/**
	 * 	Constructor that takes the width and height of the visualisation
	 *
	 *	@param width The width of the visualisation in pixels
	 *	@param height The height of the visualisation in pixels
	 */
	public DotPlotVisualisation( final int width, final int height )
	{
		super( width, height, null );
		this.setItemPlotter( this );
	}

	/**
	 * 	Adds a default coloured dot with the given size (in red).
	 *	@param x The x location
	 *	@param y The y location
	 *	@param d The size
	 */
	public void addPoint( final double x, final double y, final double d )
	{
		super.addPoint( x, y, new ColouredDot( d, RGBColour.RED ) );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter#plotObject(org.openimaj.image.Image, org.openimaj.vis.general.XYPlotVisualisation.LocatedObject, org.openimaj.vis.general.AxesRenderer2D)
	 */
	@Override
	public void plotObject( final MBFImage visImage,
			final XYPlotVisualisation.LocatedObject<ColouredDot> object,
			final AxesRenderer2D<Float[],MBFImage> renderer )
	{
		System.out.println( "Object at "+object.x+","+object.y+" is plotted at "+
				renderer.calculatePosition( object.x, object.y ) );
		visImage.createRenderer().drawShapeFilled(
			new Circle( renderer.calculatePosition( object.x, object.y ),
				(float)(renderer.scaleDimensions( object.object.size, object.object.size )[0] ) ),
			object.object.colour );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter#renderRestarting()
	 */
	@Override
	public void renderRestarting()
	{
	}

	/**
	 * 	Main method to demonstrate the vis.
	 *	@param args command-line args (not used)
	 */
	public static void main( final String[] args )
	{
		final DotPlotVisualisation dpv = new DotPlotVisualisation( 1000, 600 );
		dpv.axesRenderer2D.setMaxXValue( 1 );
		dpv.axesRenderer2D.setMinXValue( -1 );
		dpv.axesRenderer2D.setMaxYValue( 1 );
		dpv.axesRenderer2D.setMinYValue( -1 );
		dpv.axesRenderer2D.setxMajorTickSpacing( 0.2 );
		dpv.axesRenderer2D.setxMinorTickSpacing( 0.1 );
		dpv.axesRenderer2D.setyMajorTickSpacing( 0.2 );
		dpv.axesRenderer2D.setyMinorTickSpacing( 0.1 );
		dpv.axesRenderer2D.setxAxisPosition( 300 );
		dpv.setAutoScaleAxes( false );

		for( int i = 0; i < 10; i++ )
			dpv.addPoint( (Math.random()-0.5)*2, (Math.random()-0.5)*2,
					Math.random()/10 );

		dpv.updateVis();
		dpv.showWindow( "Dot plot" );
	}
}
