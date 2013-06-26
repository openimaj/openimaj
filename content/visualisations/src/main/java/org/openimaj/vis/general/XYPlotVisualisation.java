/**
 *
 */
package org.openimaj.vis.general;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.vis.Visualisation;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;

/**
 * Abstract visualisation for plotting X,Y items. Uses the {@link AxesRenderer}
 * to determine the scale of the visualisation.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @param <O> The type of object to be visualised
 * @created 3 Jun 2013
 */
public class XYPlotVisualisation<O> extends Visualisation<List<LocatedObject<O>>>
{
	/**
	 * Class that locates an object.
	 *
	 * @param <O> The object type
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jun 2013
	 */
	protected static class LocatedObject<O>
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
	protected final AxesRenderer<Float[], MBFImage> axesRenderer = new AxesRenderer<Float[], MBFImage>();

	/** Whether to render the axes on top of the data rather than underneath */
	private final boolean renderAxesLast = false;

	/** The item plotter to use */
	protected ItemPlotter<O, Float[], MBFImage> plotter;

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
	 * Initialise
	 */
	private void init()
	{
		this.data = new ArrayList<LocatedObject<O>>();

		this.axesRenderer.setxAxisColour( RGBColour.WHITE );
		this.axesRenderer.setyAxisColour( RGBColour.WHITE );
		this.axesRenderer.setMajorTickColour( RGBColour.WHITE );
		this.axesRenderer.setMinorTickColour( RGBColour.GRAY );
		this.axesRenderer.setxTickLabelColour( RGBColour.GRAY );
		this.axesRenderer.setyTickLabelColour( RGBColour.GRAY );
		this.axesRenderer.setxAxisNameColour( RGBColour.WHITE );
		this.axesRenderer.setyAxisNameColour( RGBColour.WHITE );
		this.axesRenderer.setMajorGridColour( new Float[]{.5f,.5f,.5f,1f} );
		this.axesRenderer.setMinorGridColour( new Float[]{.5f,.5f,.5f,1f} );
		this.axesRenderer.setDrawMajorTickGrid( true );
		this.axesRenderer.setDrawMinorTickGrid( true );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.Visualisation#update()
	 */
	@Override
	public void update()
	{
		this.axesRenderer.precalc( this.visImage );
		this.beforeAxesRender( this.visImage, this.axesRenderer );

		if( !this.renderAxesLast ) this.axesRenderer.renderAxis( this.visImage );

		// Tell the plotter we're about to start rendering items,
		// then loop over the items plotting them
		this.plotter.renderRestarting();
		synchronized( this.data )
		{
			for( final LocatedObject<O> o : this.data )
				this.plotter.plotObject( this.visImage, o, this.axesRenderer );

		}

		if( this.renderAxesLast ) this.axesRenderer.renderAxis( this.visImage );
	}

	/**
	 * A method that can be overridden to plot something prior to the axes being
	 * drawn.
	 *
	 * @param visImage The image to draw to
	 * @param renderer The axes renderer
	 */
	public void beforeAxesRender( final MBFImage visImage, final AxesRenderer<Float[], MBFImage> renderer )
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
	public AxesRenderer<Float[], MBFImage> getAxesRenderer()
	{
		return this.axesRenderer;
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
}
