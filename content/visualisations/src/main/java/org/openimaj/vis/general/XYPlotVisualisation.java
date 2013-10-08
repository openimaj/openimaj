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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.vis.VisualisationImpl;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;

/**
 * Abstract visualisation for plotting X,Y items. Uses the {@link AxesRenderer2D}
 * to determine the scale of the visualisation.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @param <O> The type of object to be visualised
 * @created 3 Jun 2013
 */
public class XYPlotVisualisation<O> extends VisualisationImpl<List<LocatedObject<O>>>
{
	/**
	 * Class that locates an object.
	 *
	 * @param <O> The object type
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jun 2013
	 */
	public static class LocatedObject<O>
	{
		/** The x position */
		public double x;

		/** The y position */
		public double y;

		/** The object */
		public O object;

		/**
		 * Create a located object
		 *
		 * @param x data point x location
		 * @param y data point y location
		 * @param object The object
		 */
		public LocatedObject( final double x, final double y, final O object )
		{
			this.x = x;
			this.y = y;
			this.object = object;
		}
	}

	/** */
	private static final long serialVersionUID = 1L;

	/** The renderer for the axes */
	protected final AxesRenderer2D<Float[], MBFImage> axesRenderer2D =
			new AxesRenderer2D<Float[], MBFImage>();

	/** Whether to render the axes on top of the data rather than underneath */
	private boolean renderAxesLast = false;

	/** The item plotter to use */
	protected ItemPlotter<O, Float[], MBFImage> plotter;

	/** Whether to scale the axes to fit the data */
	private boolean autoScaleAxes = true;

	/** Whether to auto position the x axis - if false you'll have to do it yourself */
	private boolean autoPositionXAxis = true;

	/**
	 * Default constructor
	 *
	 * @param plotter The item plotter to use
	 */
	public XYPlotVisualisation( final ItemPlotter<O, Float[], MBFImage> plotter )
	{
		this.plotter = plotter;
		this.init();
	}

	/**
	 * Constructor that provides the width and height of the visualisation.
	 *
	 * @param width Width of the vis in pixels
	 * @param height Height of the vis in pixels
	 * @param plotter The item plotter to use
	 */
	public XYPlotVisualisation( final int width, final int height, final ItemPlotter<O, Float[], MBFImage> plotter )
	{
		super( width, height );
		this.plotter = plotter;
		this.init();
	}

	/**
	 * Constructor that provides the width and height of the visualisation and a null plotter
	 *
	 * @param width Width of the vis in pixels
	 * @param height Height of the vis in pixels
	 */
	public XYPlotVisualisation( final int width, final int height )
	{
		super( width, height );
		this.init();
	}

	/**
	 * Initialise
	 */
	private void init()
	{
		this.data = new ArrayList<LocatedObject<O>>();

		// Set up a load of defaults for the axes renderer
		this.axesRenderer2D.setxAxisColour( RGBColour.WHITE );
		this.axesRenderer2D.setyAxisColour( RGBColour.WHITE );
		this.axesRenderer2D.setMajorTickColour( RGBColour.WHITE );
		this.axesRenderer2D.setMinorTickColour( RGBColour.GRAY );
		this.axesRenderer2D.setxTickLabelColour( RGBColour.GRAY );
		this.axesRenderer2D.setyTickLabelColour( RGBColour.GRAY );
		this.axesRenderer2D.setxAxisNameColour( RGBColour.WHITE );
		this.axesRenderer2D.setyAxisNameColour( RGBColour.WHITE );
		this.axesRenderer2D.setMajorGridColour( new Float[]{.5f,.5f,.5f,1f} );
		this.axesRenderer2D.setMinorGridColour( new Float[]{.5f,.5f,.5f,1f} );
		this.axesRenderer2D.setDrawMajorTickGrid( true );
		this.axesRenderer2D.setDrawMinorTickGrid( true );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.VisualisationImpl#update()
	 */
	@Override
	public void update()
	{
		// Tell the axes renderer where we're drawing the axes to.
		this.axesRenderer2D.setImage( this.visImage );

		// If we're going to auto position the axes we need to determine
		// where in the display the x-axis will be positioned and set the
		// axes renderer x axis position.
		if( this.autoPositionXAxis )
		{
			synchronized( this.axesRenderer2D )
			{
				// Note, this might not work very well, if the axes are rotated.
				final double xAxisPosition = this.axesRenderer2D.getAxisPaddingTop() +
					this.axesRenderer2D.getyAxisConfig().getMaxValue() *
					this.axesRenderer2D.getyAxisRenderer().getAxisLength() /
					(this.axesRenderer2D.getyAxisConfig().getMaxValue()
							- this.axesRenderer2D.getyAxisConfig().getMinValue());
				
				System.out.println( "Setting x position: "+xAxisPosition );
				
				this.axesRenderer2D.setxAxisPosition( xAxisPosition );
			}
		}

		// Recalculate any sizes needed for the axes
		synchronized( this.axesRenderer2D )
		{
			this.axesRenderer2D.precalc();
		}

		// Call the beforeAxesRender callback so the vis can draw anything it need sto
		this.beforeAxesRender( this.visImage, this.axesRenderer2D );

		// Render the axes if we're to do it below the plot
		if( !this.renderAxesLast ) this.axesRenderer2D.renderAxis( this.visImage );

		// Tell the plotter we're about to start rendering items,
		// then loop over the items plotting them
		if( this.plotter != null )
		{
			// Tell the plotter we're starting to render
			this.plotter.renderRestarting();

			// Render each data point using the plotter
			synchronized( this.data )
			{
				for( final LocatedObject<O> o : this.data )
					this.plotter.plotObject( this.visImage, o, this.axesRenderer2D );
			}
		}

		// Render the axes if we're to do it on top of the plot
		if( this.renderAxesLast ) this.axesRenderer2D.renderAxis( this.visImage );
	}

	/**
	 * A method that can be overridden to plot something prior to the axes being
	 * drawn.
	 *
	 * @param visImage The image to draw to
	 * @param renderer The axes renderer
	 */
	public void beforeAxesRender( final MBFImage visImage, final AxesRenderer2D<Float[], MBFImage> renderer )
	{
		// No implementation by default
	}

	/**
	 * Add an object to the plot
	 *
	 * @param x x location of data point
	 * @param y y location of data point
	 * @param object The object
	 */
	public void addPoint( final double x, final double y, final O object )
	{
		this.data.add( new LocatedObject<O>( x, y, object ) );
		this.validateData();
	}

	/**
	 * Remove a specific object
	 *
	 * @param object The object
	 */
	public void removePoint( final O object )
	{
		LocatedObject<O> toRemove = null;
		for( final LocatedObject<O> o : this.data )
		{
			if( o.object.equals( object ) )
			{
				toRemove = o;
				break;
			}
		}

		if( toRemove != null ) this.data.remove( toRemove );
		this.validateData();
	}

	/**
	 * Set the plotter
	 *
	 * @param plotter The plotter
	 */
	public void setItemPlotter( final ItemPlotter<O, Float[], MBFImage> plotter )
	{
		this.plotter = plotter;
	}

	/**
	 * Provides access to the underlying axes renderer so that various changes
	 * can be made to the visualisation.
	 *
	 * @return The axes renderer.
	 */
	public AxesRenderer2D<Float[], MBFImage> getAxesRenderer()
	{
		return this.axesRenderer2D;
	}

	/**
	 * Clear the data list.
	 */
	public void clearData()
	{
		synchronized( this.data )
		{
			this.data.clear();
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImpl#setData(java.lang.Object)
	 */
	@Override
	public void setData( final List<LocatedObject<O>> data )
	{
		super.setData( data );
		this.validateData();
	}

	/**
	 * 	Set up the min/max of the axes based on the data.
	 */
	protected void validateData()
	{
		if( this.autoScaleAxes && this.data.size() > 0 )
		{
			double minX = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			for( final LocatedObject<O> o : this.data)
			{
				minX = Math.min( minX, o.x );
				maxX = Math.max( maxX, o.x );
				minY = Math.min( minY, o.y );
				maxY = Math.max( maxY, o.y );				
			}

			this.axesRenderer2D.setMaxXValue( maxX );
			this.axesRenderer2D.setMinXValue( minX );
			this.axesRenderer2D.setMaxYValue( maxY );
			this.axesRenderer2D.setMinYValue( minY );
			this.axesRenderer2D.precalc();

//			System.out.println( "XYPlotVis.validateData(): max x: "+maxX+
//					", min x: "+minX+", max y: "+maxY+", min y: "+minY );
		}
	}

	/**
	 *	@return the autoScaleAxes
	 */
	public boolean isAutoScaleAxes()
	{
		return this.autoScaleAxes;
	}

	/**
	 *	@param autoScaleAxes the autoScaleAxes to set
	 */
	public void setAutoScaleAxes( final boolean autoScaleAxes )
	{
		this.autoScaleAxes = autoScaleAxes;
	}

	/**
	 *	@return the autoPositionXAxis
	 */
	public boolean isAutoPositionXAxis()
	{
		return this.autoPositionXAxis;
	}

	/**
	 *	@param autoPositionXAxis the autoPositionXAxis to set
	 */
	public void setAutoPositionXAxis( final boolean autoPositionXAxis )
	{
		this.autoPositionXAxis = autoPositionXAxis;
	}

	/**
	 *	@return the renderAxesLast
	 */
	public boolean isRenderAxesLast()
	{
		return this.renderAxesLast;
	}

	/**
	 *	@param renderAxesLast the renderAxesLast to set
	 */
	public void setRenderAxesLast( final boolean renderAxesLast )
	{
		this.renderAxesLast = renderAxesLast;
	}
}
