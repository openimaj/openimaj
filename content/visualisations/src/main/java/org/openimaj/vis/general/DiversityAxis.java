/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *	A horizontal axis on which items can be plotted and grouped into bands.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <O> The type of item to plot on the axis
 *  @created 3 Jun 2013
 */
public class DiversityAxis<O> extends XYPlotVisualisation<O>
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Default constructor
	 * 	@param plotter The plotter to use for items
	 */
	public DiversityAxis( final ItemPlotter<O,Float[],MBFImage> plotter )
	{
		super( plotter );
		this.init();
	}

	/**
	 * 	Default constructor that takes the width and height of the visualisation
	 *
	 *	@param w The width of the axis visualisation
	 *	@param h The height of the axis visualisation
	 * 	@param plotter The plotter to use for items
	 */
	public DiversityAxis( final int w, final int h,
			final ItemPlotter<O,Float[],MBFImage> plotter )
	{
		super( w, h, plotter );
		this.init();
	}

	/**
	 * 	Initialise
	 */
	private void init()
	{
		this.axesRenderer2D.setDrawYTicks( false );
		this.axesRenderer2D.setDrawYTickLabels( false );
		this.axesRenderer2D.setyLabelSpacing( 1 );
		this.axesRenderer2D.setMinYValue( 0 );
		this.axesRenderer2D.setAxisPaddingBottom( 50 );
		this.axesRenderer2D.setyAxisName( "" );
	}

	/**
	 * 	Set the name of the diversity axis.
	 *	@param name The name of the diversity axis
	 */
	public void setDiversityAxisName( final String name )
	{
		this.axesRenderer2D.setxAxisName( name );
	}

	/**
	 *	Add an object to the axis at the given position in the given band.
	 *	Note that the band is 1-based.
	 *
	 *	@param band The band in which the object is to be put (1-based)
	 * 	@param position The position of the object on the axis
	 *	@param object The object
	 */
	public void addObject( final int band, final double position, final O object )
	{
		super.addPoint( position, band, object );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.XYPlotVisualisation#beforeAxesRender(org.openimaj.image.MBFImage, org.openimaj.vis.general.AxesRenderer2D)
	 */
	@Override
	public void beforeAxesRender( final MBFImage visImage,
			final AxesRenderer2D<Float[],MBFImage> renderer )
	{
		int maxBand = 1;
		for( final XYPlotVisualisation.LocatedObject<O> s : this.data )
			maxBand = Math.max( maxBand, (int)s.y );

		renderer.setMaxYValue( maxBand );
		renderer.setImage( visImage );
		renderer.precalc( );

		final Float[][] cols = new Float[][]{ {0.4f,0.4f,0.4f}, {0.3f,0.3f,0.3f} };
		for( int b = 1; b <= maxBand; b++ )
		{
			final int topOfBand    = (int)renderer.calculatePosition( 0, b ).getY();
			final int bottomOfBand = (int)renderer.calculatePosition( 0, b-1 ).getY();
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
		// - for example, to let the item plotter know the size of the bands
	}
}
