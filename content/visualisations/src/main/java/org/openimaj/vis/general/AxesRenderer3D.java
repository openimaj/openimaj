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

import javax.media.opengl.GLAutoDrawable;

import org.openimaj.vis.DataUnitsTransformer;

/**
 *	A class for generating X, Y and Z axes in a 3D visualisation and providing
 *	data unit transformations.
 *
 *	// TODO: Need to add getters/setters ***
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 10 Jul 2013
 */
public class AxesRenderer3D implements DataUnitsTransformer<float[], double[], double[]>
{
	/** The x axis renderer */
	private final AxisRenderer3D xAxisRenderer = new AxisRenderer3D();

	/** The y axis renderer */
	private final AxisRenderer3D yAxisRenderer = new AxisRenderer3D();

	/** The z axis renderer */
	private final AxisRenderer3D zAxisRenderer = new AxisRenderer3D();

	/**
	 *
	 */
	public AxesRenderer3D()
	{
		this.xAxisRenderer.setGridDirection( -1 );
		this.yAxisRenderer.getConfig().setOrientation( new double[] { 90, 0, 0, 1, 180, 1, 0, 0 } );
		this.zAxisRenderer.getConfig().setOrientation( new double[] { 90, 0, 1, 0 } );
		this.yAxisRenderer.getConfig().getRenderingConfig().setNameOrientation(
				new double[] { 90, 0, 0, 1 } );
		this.yAxisRenderer.getConfig().getRenderingConfig().setNameDirection( -1 );
		this.zAxisRenderer.getConfig().getRenderingConfig().setNameOrientation(
				new double[] { 90, 0, 1, 0, -90, 1, 0, 0 } );
		this.zAxisRenderer.getConfig().getRenderingConfig().setNameDirection( -1 );
		this.xAxisRenderer.getConfig().setName( "x-axis" );
		this.yAxisRenderer.getConfig().setName( "y-axis" );
		this.zAxisRenderer.getConfig().setName( "z-axis" );

		this.setAxisThickness( 4 );
		this.setDrawMajorGrid( false );
		this.setDrawMinorGrid( false );
	}

	/**
	 *	@param b
	 */
	public void setDrawMajorGrid( final boolean b )
	{
		this.xAxisRenderer.getConfig().getRenderingConfig().setDrawMajorGrid( b );
		this.yAxisRenderer.getConfig().getRenderingConfig().setDrawMajorGrid( b );
		this.zAxisRenderer.getConfig().getRenderingConfig().setDrawMajorGrid( b );
	}

	/**
	 *	@param b
	 */
	public void setDrawMinorGrid( final boolean b )
	{
		this.xAxisRenderer.getConfig().getRenderingConfig().setDrawMinorGrid( b );
		this.yAxisRenderer.getConfig().getRenderingConfig().setDrawMinorGrid( b );
		this.zAxisRenderer.getConfig().getRenderingConfig().setDrawMinorGrid( b );
	}

	/**
	 * 	Set the maximum x value
	 *	@param d The new maximum
	 */
	public void setMaxXValue( final double d )
	{
		this.xAxisRenderer.getConfig().setMaxValue( d );
	}

	/**
	 * 	Set the maximum y value
	 *	@param d The new maximum
	 */
	public void setMaxYValue( final double d )
	{
		this.yAxisRenderer.getConfig().setMaxValue( d );
	}

	/**
	 * 	Set the maximum z value
	 *	@param d The new maximum
	 */
	public void setMaxZValue( final double d )
	{
		this.zAxisRenderer.getConfig().setMaxValue( d );
	}

	/**
	 * 	Set the Minimum x value
	 *	@param d The new Minimum
	 */
	public void setMinXValue( final double d )
	{
		this.xAxisRenderer.getConfig().setMinValue( d );
	}

	/**
	 * 	Set the Minimum y value
	 *	@param d The new Minimum
	 */
	public void setMinYValue( final double d )
	{
		this.yAxisRenderer.getConfig().setMinValue( d );
	}

	/**
	 * 	Set the Minimum z value
	 *	@param d The new Minimum
	 */
	public void setMinZValue( final double d )
	{
		this.zAxisRenderer.getConfig().setMinValue( d );
	}

	/**
	 *
	 *	@param minX
	 *	@param maxX
	 *	@param minY
	 *	@param maxY
	 *	@param minZ
	 *	@param maxZ
	 */
	public void setAxesRanges( final double minX, final double maxX, final double minY,
			final double maxY, final double minZ, final double maxZ )
	{
		this.setMinXValue( minX );
		this.setMaxXValue( maxX );
		this.setMinYValue( minY );
		this.setMaxYValue( maxY );
		this.setMinZValue( minZ );
		this.setMaxZValue( maxZ );
	}

	/**
	 *	@param glad
	 */
	public void renderAxis( final GLAutoDrawable glad )
	{
		this.xAxisRenderer.setGLAD( glad );
		this.yAxisRenderer.setGLAD( glad );
		this.zAxisRenderer.setGLAD( glad );

		this.xAxisRenderer.renderAxis();
		this.yAxisRenderer.renderAxis();
		this.zAxisRenderer.renderAxis();
	}

	/**
	 *	@param d
	 */
	public void setAxisThickness( final double d )
	{
		this.xAxisRenderer.getConfig().getRenderingConfig().setThickness( d );
	}

	/**
	 *	@param glad
	 */
	public void setGLAutoDrawable( final GLAutoDrawable glad )
	{

	}

	@Override
	public void precalc()
	{
	}

	@Override
	public double[] calculatePosition( final double[] units )
	{
		return new double[] {
			this.xAxisRenderer.calculatePosition( units[0] ),
			this.yAxisRenderer.calculatePosition( units[1] ),
			this.zAxisRenderer.calculatePosition( units[2] )
		};
	}

	@Override
	public double[] calculateUnitsAt( final double[] position )
	{
		return new double[] {
				this.xAxisRenderer.calculateUnitsAt( position[0] ),
				this.yAxisRenderer.calculateUnitsAt( position[1] ),
				this.zAxisRenderer.calculateUnitsAt( position[2] )
			};
	}

	@Override
	public double[] scaleDimension( final double[] dimension )
	{
		return new double[]
		{
				this.xAxisRenderer.scaleDimension( dimension[0] ),
				this.yAxisRenderer.scaleDimension( dimension[1] ),
				this.zAxisRenderer.scaleDimension( dimension[2] ),
		};
	}

	/**
	 *	@return the xAxisRenderer
	 */
	public AxisRenderer3D getxAxisRenderer()
	{
		return this.xAxisRenderer;
	}

	/**
	 *	@return the yAxisRenderer
	 */
	public AxisRenderer3D getyAxisRenderer()
	{
		return this.yAxisRenderer;
	}

	/**
	 *	@return the zAxisRenderer
	 */
	public AxisRenderer3D getzAxisRenderer()
	{
		return this.zAxisRenderer;
	}
}
