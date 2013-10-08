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
import org.openimaj.image.colour.ColourMap;
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

	/** A colour map to use */
	private ColourMap colourMap = ColourMap.Autumn;

	/** Colour map range */
	private double colourMapMin = -1;

	/** Colour map range */
	private double colourMapMax = 1;

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
		Float[] c = RGBColour.RED;
		if( this.colourMap != null )
			c = this.colourMap.apply( (float)((d-this.colourMapMin)/(this.colourMapMax-this.colourMapMin)) );
		super.addPoint( x, y, new ColouredDot( d, c ) );
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
//		System.out.println( "Object at "+object.x+","+object.y+" is plotted at "+
//				renderer.calculatePosition( object.x, object.y ) );
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
	 *	@return the colourMap
	 */
	public ColourMap getColourMap()
	{
		return this.colourMap;
	}

	/**
	 *	@param colourMap the colourMap to set
	 */
	public void setColourMap( final ColourMap colourMap )
	{
		this.colourMap = colourMap;
	}

	/**
	 *	@return the colourMapRange
	 */
	public double getColourMapMin()
	{
		return this.colourMapMin;
	}

	/**
	 *	@param colourMapRange the colourMapRange to set
	 */
	public void setColourMapMin( final double colourMapRange )
	{
		this.colourMapMin = colourMapRange;
	}

	/**
	 *	@return the colourMapMax
	 */
	public double getColourMapMax()
	{
		return this.colourMapMax;
	}

	/**
	 *	@param colourMapMax the colourMapMax to set
	 */
	public void setColourMapMax( final double colourMapMax )
	{
		this.colourMapMax = colourMapMax;
	}

	/**
	 *	@param min
	 *	@param max
	 */
	public void setColourMapRange( final double min, final double max )
	{
		this.colourMapMin = min;
		this.colourMapMax = max;
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
		dpv.setColourMapRange( 0, 0.2 );

		for( int i = 0; i < 10; i++ )
			dpv.addPoint( (Math.random()-0.5)*2, (Math.random()-0.5)*2,
					i/50d );

		dpv.updateVis();
		dpv.showWindow( "Dot plot" );
	}
}
