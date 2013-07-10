/**
 *
 */
package org.openimaj.vis.general;

import org.openimaj.vis.general.AxisConfig.AxisRenderingConfig;

/**
 *	A general axis renderer that can be used for rendering an axis into a visualisation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <Q> The pixel colour representation
 *  @created 9 Jul 2013
 */
public abstract class AxisRenderer<Q>
{
	/**
	 * 	Draw the major axis
	 *	@param config The axis configuration
	 */
	public abstract void drawAxis( AxisConfig<Q> config );

	/**
	 * 	Draw the axis label
	 *	@param config The axis configuration
	 */
	public abstract void drawAxisLabel( AxisConfig<Q> config );

	/**
	 * 	Draw a tick on the axis at the given location
	 *	@param location The location (in data units)
	 *	@param config The config
	 */
	public abstract void drawMajorTick( double location, AxisConfig<Q> config );

	/**
	 * 	Draw a major grid line on the vis
	 *	@param location The location (in data units)
	 *	@param config The config
	 */
	public abstract void drawMajorTickGridline( double location, AxisConfig<Q> config );

	/**
	 * 	Draw a minor tick on the axis
	 *	@param location The location (in data units)
	 *	@param config The configuration
	 */
	public abstract void drawMinorTick( double location, AxisConfig<Q> config );

	/**
	 * 	Draw a minor grid on the vis
	 *	@param location The location (in data units)
	 *	@param config The axis configuration
	 */
	public abstract void drawMinorTickGridline( double location, AxisConfig<Q> config );

	/** The axis configuration */
	protected AxisConfig<Q> config = new AxisConfig<Q>();

	/**
	 * 	Find the nearest lower major tick mark to the given value.
	 *	@param value The given value
	 *	@return The nearest lower major tick mark
	 */
	public double nearestLowerMajorTick( final double value )
	{
		final double ts = this.config.getRenderingConfig().getMajorTickSpacing();
		return Math.floor( value/ts ) * ts;
	}

	/**
	 * 	Find the nearest higher major tick mark to the given value.
	 *	@param value The given value
	 *	@return The nearest higher major tick mark
	 */
	public double nearestHigherMajorTick( final double value )
	{
		final double ts = this.config.getRenderingConfig().getMajorTickSpacing();
		return Math.ceil( value/ts ) * ts;
	}

	/**
	 * 	Perform any precalculations that will allow the axes to
	 * 	be drawn faster, or to allow the data transformer to work.
	 */
	public void precalc( )
	{
		// No implementation. Override if you need to.
	}

	/**
	 */
	public void renderAxis()
	{
		final AxisRenderingConfig<Q> rc = this.config.getRenderingConfig();

		double min = this.config.getMinValue();
		double max = this.config.getMaxValue();

		if( min > max ) { final double t = min; min = max; max = t; }

		if( rc.isRenderAxis() )
		{
			if( rc.isDrawMajorTicks() || rc.isDrawMinorTicks() )
			{
				if( rc.getMinorTickSpacing() > 0 )
					for( double u = min; u <= max; u += rc.getMinorTickSpacing() )
					{
						if( rc.isDrawMinorGrid() )
							this.drawMinorTickGridline( u, this.config );
						this.drawMinorTick( u, this.config );
					}

				if( rc.getMajorTickSpacing() > 0 )
					for( double u = min; u <= max; u += rc.getMajorTickSpacing() )
					{
						if( rc.isDrawMajorGrid() )
							this.drawMajorTickGridline( u, this.config );
						this.drawMajorTick( u, this.config );
					}
			}

			this.drawAxis( this.config );
			this.drawAxisLabel( this.config );
		}
	}

	/**
	 *	@return the config
	 */
	public AxisConfig<Q> getConfig()
	{
		return this.config;
	}

	/**
	 *	@param config the config to set
	 */
	public void setConfig( final AxisConfig<Q> config )
	{
		this.config = config;
	}
}
