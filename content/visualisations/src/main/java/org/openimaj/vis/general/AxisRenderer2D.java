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

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.vis.DataUnitsTransformer;

import Jama.Matrix;


/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <Q>
 *  @created 9 Jul 2013
 */
public class AxisRenderer2D<Q> extends AxisRenderer<Q>
	implements DataUnitsTransformer<Q,Double,double[]>
{
	/** The image we're drawing to */
	private Image<Q,?> image;

	/** Desired size of the axis in pixels */
	protected double axisLength = 100;

	/** The data point transform we've calculated */
	private Matrix transform;

	/** The axis line we've calculated */
	private Line2d axisLine;

	/** Units to pixels scaling */
	private double currentScale = 1;

	/**
	 *	Default constructor
	 */
	public AxisRenderer2D()
	{
	}

	/**
	 * 	Constructor to set the config
	 *	@param conf The config
	 */
	public AxisRenderer2D( final AxisConfig<Q> conf )
	{
		this.config = conf;
	}

	@Override
	public double[] scaleDimension( final Double dimension )
	{
		return new double[] { dimension * this.getCurrentScale() };
	}

	@Override
	public void precalc()
	{
		// Create an axis line between the min and max value
		this.axisLine = new Line2d( (float)this.config.getMinValue(), 0,
				(float)this.config.getMaxValue(), 0 );

		// Transform to (0,0) in data units
		this.transform = TransformUtilities.translateMatrix( -this.config.getMinValue(), 0 );

		// Scale to pixels
		this.currentScale = this.axisLength / (this.config.getMaxValue() - this.config.getMinValue());
		this.transform = TransformUtilities.scaleMatrix( (float)this.currentScale, 1 )
				.times(this.transform);

		// Rotate to the preferred orientation
		this.transform = TransformUtilities.rotationMatrix( this.config.getOrientation()[0] )
				.times(this.transform);

		// Translate to the preferred position
		this.transform = TransformUtilities.translateMatrix(
				this.config.getLocation()[0], this.config.getLocation()[1] )
				.times( this.transform );

		// Transform the axis
		this.axisLine = this.axisLine.transform( this.transform );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.AxisRenderer#drawAxis(org.openimaj.vis.general.AxisConfig)
	 */
	@Override
	public void drawAxis( final AxisConfig<Q> config )
	{
		if( this.axisLine == null ) this.precalc( );

		this.image.drawLine( this.axisLine,
				(int)config.getRenderingConfig().getThickness(),
				config.getRenderingConfig().getColour() );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.AxisRenderer#drawAxisLabel(org.openimaj.vis.general.AxisConfig)
	 */
	@Override
	public void drawAxisLabel( final AxisConfig<Q> config )
	{
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.AxisRenderer#drawMajorTick(double, org.openimaj.vis.general.AxisConfig)
	 */
	@Override
	public void drawMajorTick( final double location, final AxisConfig<Q> config )
	{
		if( this.transform == null ) this.precalc( );

		final float x = (float)location;
		final float y = (float)config.getRenderingConfig().getMajorTickLength();
		Line2d l = new Line2d( x, -y, x, y );
		l = l.transform( this.transform );
		this.image.drawLine( l, (int)config.getRenderingConfig().getMajorTickThickness(),
				config.getRenderingConfig().getMajorTickColour() );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.AxisRenderer#drawMajorTickGridline(double, org.openimaj.vis.general.AxisConfig)
	 */
	@Override
	public void drawMajorTickGridline( final double location, final AxisConfig<Q> config )
	{
		if( this.transform == null ) this.precalc( );

		final float x = (float)location;
		final float y = this.image.getHeight()*2;
		Line2d l = new Line2d( x, -y, x, y );
		l = l.transform( this.transform );
		this.image.drawLine( l, (int)config.getRenderingConfig().getMajorGridThickness(),
				config.getRenderingConfig().getMajorGridColour() );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.AxisRenderer#drawMinorTick(double, org.openimaj.vis.general.AxisConfig)
	 */
	@Override
	public void drawMinorTick( final double location, final AxisConfig<Q> config )
	{
		if( this.transform == null ) this.precalc( );

		final float x = (float)location;
		final float y = (float)config.getRenderingConfig().getMinorTickLength();
		Line2d l = new Line2d( x, -y, x, y );
		l = l.transform( this.transform );
		this.image.drawLine( l, (int)config.getRenderingConfig().getMinorTickThickness(),
				config.getRenderingConfig().getMinorTickColour() );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.AxisRenderer#drawMinorTickGridline(double, org.openimaj.vis.general.AxisConfig)
	 */
	@Override
	public void drawMinorTickGridline( final double location, final AxisConfig<Q> config )
	{
		if( this.transform == null ) this.precalc( );

		final float x = (float)location;
		final float y = this.image.getHeight()*2;
		Line2d l = new Line2d( x, -y, x, y );
		l = l.transform( this.transform );
		this.image.drawLine( l, (int)config.getRenderingConfig().getMinorGridThickness(),
				config.getRenderingConfig().getMinorGridColour() );
	}

	/**
	 *	@return the axisLength
	 */
	public double getAxisLength()
	{
		return this.axisLength;
	}

	/**
	 *	@param axisLength the axisLength to set
	 */
	public void setAxisLength( final double axisLength )
	{
		this.axisLength = axisLength;
		this.precalc();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.DataUnitsTransformer#calculatePosition(java.lang.Object)
	 */
	@Override
	public double[] calculatePosition( final Double units )
	{
		if( this.transform == null ) this.precalc();

		final Point2d p = new Point2dImpl( units.floatValue(), 0f );
		final Point2d p2 = p.transform( this.transform );
		return new double[] {p2.getX(), p2.getY()};
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.DataUnitsTransformer#calculateUnitsAt(java.lang.Object)
	 */
	@Override
	public Double calculateUnitsAt( final double[] position )
	{
		if( this.transform == null ) this.precalc( );

		final Point2d p = new Point2dImpl( (float)position[0], (float)position[1] );
		final Point2d p2 = p.transform( this.transform.inverse() );
		return new Double(p2.getX());
	}

	/**
	 *	@return the image
	 */
	public Image<Q, ?> getImage()
	{
		return this.image;
	}

	/**
	 *	@param image the image to set
	 */
	public void setImage( final Image<Q, ?> image )
	{
		this.image = image;
	}

	/**
	 *	@return the currentScale
	 */
	public double getCurrentScale()
	{
		return this.currentScale;
	}

	/**
	 *	@return the config
	 */
	@Override
	public AxisConfig<Q> getConfig()
	{
		return this.config;
	}

	/**
	 *	@param config the config to set
	 */
	@Override
	public void setConfig( final AxisConfig<Q> config )
	{
		this.config = config;
	}

	/**
	 * 	Simple test
	 *	@param args command-line args (not used)
	 */
	public static void main( final String[] args )
	{
		// Create the image to draw into
		final MBFImage img = new MBFImage( 400, 400, 3 );

		// Create the configuration for our axis
		final AxisConfig<Float[]> conf = new AxisConfig<Float[]>();
		conf.setLocation( new double[]{ 20, 200 } );
		conf.setOrientation( new double[] {0/(360/(2*Math.PI))} );
		conf.setMaxValue( 10 );
		conf.setMinValue( 5 );
		conf.getRenderingConfig().setMajorTickSpacing( 1 );
		conf.getRenderingConfig().setMinorTickSpacing( 0.5d );
		conf.getRenderingConfig().setColour( RGBColour.WHITE );
		conf.getRenderingConfig().setMajorTickColour( RGBColour.WHITE );
		conf.getRenderingConfig().setMinorTickColour( RGBColour.WHITE );
		conf.getRenderingConfig().setMinorGridColour( RGBColour.GRAY  );
		conf.getRenderingConfig().setMajorGridColour( RGBColour.GRAY );
		conf.getRenderingConfig().setDrawMajorGrid( true );
		conf.getRenderingConfig().setDrawMinorGrid( true );

		// Create the axis renderer for the image
		final AxisRenderer2D<Float[]> r = new AxisRenderer2D<Float[]>( conf );
		r.setAxisLength( 360 );
		r.setImage( img );

		r.renderAxis();

		DisplayUtilities.display( img );
	}
}
