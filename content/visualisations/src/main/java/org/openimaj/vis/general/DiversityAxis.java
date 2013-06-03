/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <O>
 *  @created 3 Jun 2013
 */
public class DiversityAxis<O> extends XYPlotVisualisation<O>
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * @param plotter
	 */
	public DiversityAxis( final ItemPlotter<O,Float[]> plotter )
	{
		super( plotter );
		this.init();
	}

	/**
	 *	@param w
	 *	@param h
	 * 	@param plotter
	 */
	public DiversityAxis( final int w, final int h, final ItemPlotter<O,Float[]> plotter )
	{
		super( w, h, plotter );
		this.init();
	}

	/**
	 * 	Initialise
	 */
	private void init()
	{
//		this.axesRenderer.setDrawYAxis( false );
		this.axesRenderer.setyLabelSpacing( 1 );
		this.axesRenderer.setMinYValue( 0 );
		this.axesRenderer.setAxisPaddingBottom( 50 );
	}

	/**
	 *
	 *	@param band The band in which the object is to be put
	 * 	@param position The position of the object on the axis
	 *	@param object The object
	 */
	public void addObject( final int band, final double position, final O object )
	{
		super.addPoint( position, band, object );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.XYPlotVisualisation#beforeAxesRender(org.openimaj.image.MBFImage, org.openimaj.vis.general.AxesRenderer)
	 */
	@Override
	public void beforeAxesRender( final MBFImage visImage, final AxesRenderer<Float[]> renderer )
	{
		int maxBand = 1;
		for( final XYPlotVisualisation.LocatedObject<O> s : this.data )
			maxBand = Math.max( maxBand, (int)s.y );

		System.out.println( "Size of data: "+this.data.size() );
		System.out.println( "Number of bands: "+maxBand );

		renderer.setMaxYValue( maxBand );
		renderer.precalc( visImage );

		final Float[][] cols = new Float[][]{ {0.4f,0.4f,0.4f}, {0.3f,0.3f,0.3f} };
		for( int b = 1; b <= maxBand; b++ )
		{
			final int topOfBand    = (int)renderer.calculatePosition( visImage, 0, b ).getY();
			final int bottomOfBand = (int)renderer.calculatePosition( visImage, 0, b-1 ).getY();
			System.out.println( "Band: "+b+" ("+(b%2)+"), top: "+topOfBand+", bottom: "+bottomOfBand );
			visImage.createRenderer().drawShapeFilled(
				new Rectangle( 0, topOfBand, visImage.getWidth(), bottomOfBand-topOfBand ),
				cols[b%2] );

			if( b == 1 )
				this.bandSizeKnown( bottomOfBand - topOfBand );
		}
	}

	/**
	 * 	Called once the band size has been calculated
	 *	@param bandSize The band size
	 */
	public void bandSizeKnown( final int bandSize )
	{
		// Can be overridden if you need to know this
	}
}
