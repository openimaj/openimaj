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
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.vis.DataUnitsTransformer;

import Jama.Matrix;

/**
 *	TODO: javadoc
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <Q> The pixel type of the image you'll be drawing the axes to
 * 	@param <I> The type of image being drawn to
 *  @created 3 Jun 2013
 */
public class AxesRenderer2D<Q,I extends Image<Q,I>>
	implements DataUnitsTransformer<Q,double[],int[]>
{
	/** The axis renderer we'll use to render the x axis */
	private final AxisRenderer2D<Q> xAxisRenderer = new AxisRenderer2D<Q>();

	/** The axis renderer we'll use to render the y axis */
	private final AxisRenderer2D<Q> yAxisRenderer = new AxisRenderer2D<Q>();

	/** The configuration for the xAxis */
	private AxisConfig<Q> xAxisConfig = this.xAxisRenderer.getConfig();

	/** The configuration for the yAxis */
	private AxisConfig<Q> yAxisConfig = this.yAxisRenderer.getConfig();

	/** How far from the left of the image will the x axis start */
	private int axisPaddingLeft = 20;

	/** How far from the right of the image will the x axis stop */
	private int axisPaddingRight = 20;

	/** How far from the top of the image with the y axis start */
	private int axisPaddingTop = 20;

	/** How far from the bottom of the image will the y axis stop */
	private int axisPaddingBottom = 20;

	/** Whether to work out the ideal position for the axes */
	private boolean autoScaleAxes = false;

	private Matrix dataTransformMatrix;

	/**
	 * 	Default constructor
	 */
	public AxesRenderer2D()
	{
		this.xAxisConfig.setOrientation( new double[] { 0 } );
		this.yAxisConfig.setOrientation( new double[] {
				this.xAxisConfig.getOrientation()[0]-Math.PI/2d } );
	}

	/**
	 * 	Set the orientation of the x axis
	 *	@param rads angle in radians
	 */
	public void setOrientation( final double rads )
	{
		this.xAxisConfig.setOrientation( new double[] { rads } );
		this.yAxisConfig.setOrientation( new double[] {
				this.xAxisConfig.getOrientation()[0]-Math.PI/2d } );

	}

	/**
	 * 	Render the axis to the given image
	 *	@param image The image to draw the axes to
	 */
	public void renderAxis( final I image )
	{
		// Set the image to draw upon
		this.setImage( image );

		this.xAxisRenderer.renderAxis();
		this.yAxisRenderer.renderAxis();

//
//		// Create the renderer to draw to the image
//		final ImageRenderer<Q, ? extends Image<Q, I>> ir = image.createRenderer( RenderHints.ANTI_ALIASED );
//
//		// Get the dimensions of the image to draw to
//		final int w = image.getWidth();
//		final int h = image.getHeight();
//
//		// Find the pixel spacing to use
//		final double xRange = this.maxXValue - this.minXValue;
//		this.xUnitSizePx = (w-this.axisPaddingLeft-this.axisPaddingRight) / xRange;
//		final double yRange = this.maxYValue - this.minYValue;
//		this.yUnitSizePx = (h-this.axisPaddingBottom-this.axisPaddingTop) / yRange;
//
//		// Pixel position of where the axes crossing happens
//		this.yAxisConfig.setLocation( new double[] {this.axisPaddingLeft - this.minXValue*this.xUnitSizePx,0} );
//		this.xAxisPosition = h - this.axisPaddingBottom + this.minYValue*this.yUnitSizePx;
//
//		// Draw the x-axis minor ticks
//		if( this.drawXAxis && this.drawXTicks )
//		{
//			for( double v = this.minXValue; v <= this.maxXValue; v += this.xMinorTickSpacing )
//			{
//				if( this.drawMinorTickGrid )
//					ir.drawLine(
//							(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//							0,
//							(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//							image.getHeight(),
//							this.minorGridThickness,
//							this.minorGridColour );
//
//				ir.drawLine(
//					(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//					(int)(this.xAxisPosition-this.minorTickLength),
//					(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//					(int)(this.xAxisPosition+this.minorTickLength),
//					this.minorTickThickness,
//					this.minorTickColour );
//			}
//		}
//
//		// Draw the x-axis major ticks
//		if( this.drawXAxis && this.drawXTicks )
//		{
//			for( double v = this.minXValue; v <= this.maxXValue; v += this.xMajorTickSpacing )
//			{
//				if( this.drawMajorTickGrid )
//					ir.drawLine(
//							(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//							0,
//							(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//							image.getHeight(),
//							this.majorGridThickness,
//							this.majorGridColour );
//
//				ir.drawLine(
//					(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//					(int)(this.xAxisPosition-this.majorTickLength),
//					(int)(this.yAxisConfig.getLocation()[0] + v * this.xUnitSizePx),
//					(int)(this.xAxisPosition+this.majorTickLength),
//					this.majorTickThickness,
//					this.majorTickColour );
//			}
//		}
//
//		// Draw the x tick labels
//		double maxXLabelPosition = 0;
//		if( this.drawXAxis && this.drawXTickLabels )
//		{
//			final int yPos = (int)(this.xAxisPosition + this.xTickLabelSize + this.majorTickLength
//					+ this.xTickLabelSize/2 );
//
//			@SuppressWarnings( "rawtypes" )
//			final FontStyle s = this.xTickLabelFont.createStyle( ir );
//			s.setFontSize( this.xTickLabelSize );
//
//			@SuppressWarnings( "rawtypes" )
//			final FontRenderer r = this.xTickLabelFont.getRenderer( ir );
//
//			for( double v = this.minXValue; v <= this.maxXValue; v += this.xLabelSpacing )
//			{
//				String text = ""+v;
//				if( this.xAxisLabelTransformer != null )
//					text = this.xAxisLabelTransformer.transform( v );
//
//				final float fw = r.getBounds( text, s ).width;
//				final int xPos = (int)(this.yAxisConfig.getLocation()[0] + v*this.xUnitSizePx - fw/2);
//				ir.drawText( text, xPos, yPos, this.xTickLabelFont,
//						this.xTickLabelSize, this.xTickLabelColour );
//			}
//			maxXLabelPosition = yPos;
//		}
//
//		// Draw the y-axis ticks
//		if( this.drawYAxis && this.drawYTicks )
//		{
//			for( double v = this.minYValue; v <= this.maxYValue; v += this.yMinorTickSpacing )
//			{
//				if( this.drawMinorTickGrid )
//					ir.drawLine(
//							0,
//							(int)(this.xAxisPosition - v * this.yUnitSizePx),
//							image.getWidth(),
//							(int)(this.xAxisPosition - v * this.yUnitSizePx),
//							this.minorGridThickness,
//							this.minorGridColour );
//
//				ir.drawLine(
//					(int)(this.yAxisConfig.getLocation()[0]-this.minorTickLength),
//					(int)(this.xAxisPosition - v * this.yUnitSizePx),
//					(int)(this.yAxisConfig.getLocation()[0]+this.minorTickLength),
//					(int)(this.xAxisPosition - v * this.yUnitSizePx),
//					this.minorTickThickness,
//					this.minorTickColour );
//			}
//		}
//
//		// Draw the y-axis ticks
//		if( this.drawYAxis && this.drawYTicks )
//		{
//			for( double v = this.minYValue; v <= this.maxYValue; v += this.yMajorTickSpacing )
//			{
//				if( this.drawMajorTickGrid )
//					ir.drawLine(
//							0,
//							(int)(this.xAxisPosition - v * this.yUnitSizePx),
//							image.getWidth(),
//							(int)(this.xAxisPosition - v * this.yUnitSizePx),
//							this.majorGridThickness,
//							this.majorGridColour );
//
//				ir.drawLine(
//					(int)(this.yAxisPosition-this.majorTickLength),
//					(int)(this.xAxisPosition - v * this.yUnitSizePx),
//					(int)(this.yAxisPosition+this.majorTickLength),
//					(int)(this.xAxisPosition - v * this.yUnitSizePx),
//					this.majorTickThickness,
//					this.majorTickColour );
//			}
//		}
//
//		// Draw the x tick labels
//		double minYLabelPosition = this.yAxisPosition;
//		if( this.drawYAxis && this.drawYTickLabels )
//		{
//			@SuppressWarnings( "rawtypes" )
//			final FontStyle s = this.yTickLabelFont.createStyle( ir );
//			s.setFontSize( this.yTickLabelSize );
//
//			@SuppressWarnings( "rawtypes" )
//			final FontRenderer r = this.yTickLabelFont.getRenderer( ir );
//
//			for( double v = this.minYValue; v <= this.maxYValue; v += this.yLabelSpacing )
//			{
//				String text = ""+v;
//				if( this.yAxisLabelTransformer != null )
//					text = this.yAxisLabelTransformer.transform( v );
//
//				final float fw = r.getBounds( text, s ).width;
//				final int xPos = (int)(this.yAxisPosition - fw - this.majorTickLength
//						- this.yTickLabelSize/2 );	// Last part is just a bit of padding
//				final int yPos = (int)(this.xAxisPosition - v*this.yUnitSizePx + this.yTickLabelSize/2 );
//				ir.drawText( text, xPos, yPos, this.yTickLabelFont,
//						this.yTickLabelSize, this.yTickLabelColour );
//				minYLabelPosition = Math.min( xPos, minYLabelPosition );
//			}
//		}
//
//		// Draw the X-axis
//		if( this.drawXAxis )
//			ir.drawLine( this.axisPaddingLeft, (int)this.xAxisPosition, w-this.axisPaddingRight,
//				(int)this.xAxisPosition, this.xAxisThickness, this.xAxisColour );
//
//		// Draw the Y-axis
//		if( this.drawYAxis )
//			ir.drawLine( (int)this.yAxisPosition, this.axisPaddingTop, (int)this.yAxisPosition,
//				h-this.axisPaddingBottom, this.yAxisThickness, this.yAxisColour );
//
//		// Draw the X-axis label
//		if( this.drawXAxis && this.drawXAxisName )
//			ir.drawText( this.xAxisName, this.axisPaddingLeft,
//					(int)(maxXLabelPosition + this.xAxisNameSize),
//					this.xAxisNameFont,
//					this.xAxisNameSize, this.xAxisNameColour );
//
//		// Draw the Y-axis label
//		if( this.drawYAxis && this.drawYAxisName )
//		{
//			@SuppressWarnings( "rawtypes" )
//			final FontStyle s = this.yAxisNameFont.createStyle( ir );
//			s.setFontSize( this.yAxisNameSize );
//
//			final float fw = this.yAxisNameFont.getRenderer( ir ).getBounds(
//					this.yAxisName, s ).width;
//
//			ir.drawText( this.yAxisName, (int)(minYLabelPosition - fw),
//					this.yAxisNameSize + this.axisPaddingTop, this.yAxisNameFont,
//					this.yAxisNameSize, this.yAxisNameColour );
//		}
	}

	/**
	 * 	For a given coordinate in the units of the data, will calculate
	 * 	the pixel position.
	 *
	 * 	@param image The image in which the axes were drawn
	 *	@param x The x position
	 *	@param y The y position
	 *	@return The pixel position
	 */
//	@Override
//	public Point2d calculatePosition(
//			final I image, final double x, final double y )
//	{
//		return new Point2dImpl( (float)(this.yAxisPosition + x*this.xUnitSizePx),
//				(float)(this.xAxisPosition - y*this.yUnitSizePx) );
//	}

	/**
	 *	@param drawXAxis the drawXAxis to set
	 */
	public void setDrawXAxis( final boolean drawXAxis )
	{
		this.xAxisConfig.getRenderingConfig().setRenderAxis( drawXAxis );
	}

	/**
	 *	@param drawYAxis the drawYAxis to set
	 */
	public void setDrawYAxis( final boolean drawYAxis )
	{
		this.yAxisConfig.getRenderingConfig().setRenderAxis( drawYAxis );
	}

	/**
	 *	@return the axisPaddingLeft
	 */
	public int getAxisPaddingLeft()
	{
		return this.axisPaddingLeft;
	}

	/**
	 *	@param axisPaddingLeft the axisPaddingLeft to set
	 */
	public void setAxisPaddingLeft( final int axisPaddingLeft )
	{
		this.axisPaddingLeft = axisPaddingLeft;
	}

	/**
	 *	@return the axisPaddingRight
	 */
	public int getAxisPaddingRight()
	{
		return this.axisPaddingRight;
	}

	/**
	 *	@param axisPaddingRight the axisPaddingRight to set
	 */
	public void setAxisPaddingRight( final int axisPaddingRight )
	{
		this.axisPaddingRight = axisPaddingRight;
	}

	/**
	 *	@return the axisPaddingTop
	 */
	public int getAxisPaddingTop()
	{
		return this.axisPaddingTop;
	}

	/**
	 *	@param axisPaddingTop the axisPaddingTop to set
	 */
	public void setAxisPaddingTop( final int axisPaddingTop )
	{
		this.axisPaddingTop = axisPaddingTop;
	}

	/**
	 *	@return the axisPaddingBottom
	 */
	public int getAxisPaddingBottom()
	{
		return this.axisPaddingBottom;
	}

	/**
	 *	@param axisPaddingBottom the axisPaddingBottom to set
	 */
	public void setAxisPaddingBottom( final int axisPaddingBottom )
	{
		this.axisPaddingBottom = axisPaddingBottom;
	}

	/**
	 *	@param xAxisPosition the xAxisPosition to set
	 */
	public void setxAxisPosition( final double xAxisPosition )
	{
		this.xAxisConfig.setLocation( new double[] { this.axisPaddingLeft, xAxisPosition } );
	}

	/**
	 * 	The y position of the x axis.
	 *	@return the y position.
	 */
	public double getxAxisPosition()
	{
		return this.xAxisConfig.getLocation()[1];
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
	 *	@param xAxisThickness the xAxisThickness to set
	 */
	public void setxAxisThickness( final int xAxisThickness )
	{
		this.xAxisConfig.getRenderingConfig().setThickness( xAxisThickness );
	}

	/**
	 *	@param xAxisColour the xAxisColour to set
	 */
	public void setxAxisColour( final Q xAxisColour )
	{
		this.xAxisConfig.getRenderingConfig().setColour( xAxisColour );
	}

	/**
	 *	@param yAxisThickness the yAxisThickness to set
	 */
	public void setyAxisThickness( final int yAxisThickness )
	{
		this.yAxisConfig.getRenderingConfig().setThickness( yAxisThickness );
	}

	/**
	 *	@param yAxisColour the yAxisColour to set
	 */
	public void setyAxisColour( final Q yAxisColour )
	{
		this.yAxisConfig.getRenderingConfig().setColour( yAxisColour );
	}

	/**
	 *	@param maxXValue the maxXValue to set
	 */
	public void setMaxXValue( final double maxXValue )
	{
		this.xAxisConfig.setMaxValue( this.xAxisRenderer.nearestHigherMajorTick( maxXValue ) );
	}

	/**
	 *	@param minXValue the minXValue to set
	 */
	public void setMinXValue( final double minXValue )
	{
		this.xAxisConfig.setMinValue( this.xAxisRenderer.nearestLowerMajorTick( minXValue ) );
	}

	/**
	 *	@param maxYValue the maxYValue to set
	 */
	public void setMaxYValue( final double maxYValue )
	{
		this.yAxisConfig.setMaxValue( this.yAxisRenderer.nearestHigherMajorTick( maxYValue ) );
	}

	/**
	 * 	Returns the maximum value of the y axis
	 *	@return The y axis maximum value
	 */
	public double getMaxYValue()
	{
		return this.yAxisConfig.getMaxValue();
	}

	/**
	 *	@param minYValue the minYValue to set
	 */
	public void setMinYValue( final double minYValue )
	{
		this.yAxisConfig.setMinValue( this.yAxisRenderer.nearestLowerMajorTick( minYValue ) );
	}

	/**
	 *	@param xMinorTickSpacing the xMinorTickSpacing to set
	 */
	public void setxMinorTickSpacing( final double xMinorTickSpacing )
	{
		this.xAxisConfig.getRenderingConfig().setMinorTickSpacing( xMinorTickSpacing );
	}

	/**
	 *	@param yMinorTickSpacing the yMinorTickSpacing to set
	 */
	public void setyMinorTickSpacing( final double yMinorTickSpacing )
	{
		this.yAxisConfig.getRenderingConfig().setMinorTickSpacing( yMinorTickSpacing );
	}

	/**
	 *	@param xMajorTickSpacing the xMajorTickSpacing to set
	 */
	public void setxMajorTickSpacing( final double xMajorTickSpacing )
	{
		this.xAxisConfig.getRenderingConfig().setMajorTickSpacing( xMajorTickSpacing );
	}

	/**
	 *	@param yMajorTickSpacing the yMajorTickSpacing to set
	 */
	public void setyMajorTickSpacing( final double yMajorTickSpacing )
	{
		this.yAxisConfig.getRenderingConfig().setMajorTickSpacing( yMajorTickSpacing );
	}

	/**
	 *	@param minorTickLength the minorTickLength to set
	 */
	public void setMinorTickLength( final int minorTickLength )
	{
		this.yAxisConfig.getRenderingConfig().setMinorTickLength( minorTickLength );
		this.xAxisConfig.getRenderingConfig().setMinorTickLength( minorTickLength );
	}

	/**
	 *	@param majorTickLength the majorTickLength to set
	 */
	public void setMajorTickLength( final int majorTickLength )
	{
		this.yAxisConfig.getRenderingConfig().setMajorTickLength( majorTickLength );
		this.xAxisConfig.getRenderingConfig().setMajorTickLength( majorTickLength );
	}

	/**
	 *	@param minorTickColour the minorTickColour to set
	 */
	public void setMinorTickColour( final Q minorTickColour )
	{
		this.yAxisConfig.getRenderingConfig().setMinorTickColour( minorTickColour );
		this.xAxisConfig.getRenderingConfig().setMinorTickColour( minorTickColour );
	}

	/**
	 *	@param majorTickColour the majorTickColour to set
	 */
	public void setMajorTickColour( final Q majorTickColour )
	{
		this.yAxisConfig.getRenderingConfig().setMajorTickColour( majorTickColour );
		this.xAxisConfig.getRenderingConfig().setMajorTickColour( majorTickColour );
	}

	/**
	 *	@param majorTickThickness the majorTickThickness to set
	 */
	public void setMajorTickThickness( final int majorTickThickness )
	{
		this.yAxisConfig.getRenderingConfig().setMajorTickThickness( majorTickThickness );
		this.xAxisConfig.getRenderingConfig().setMajorTickThickness( majorTickThickness );
	}

	/**
	 *	@param minorTickThickness the minorTickThickenss to set
	 */
	public void setMinorTickThickenss( final int minorTickThickness )
	{
		this.yAxisConfig.getRenderingConfig().setMinorTickThickness( minorTickThickness );
		this.xAxisConfig.getRenderingConfig().setMinorTickThickness( minorTickThickness );
	}

	/**
	 *	@param drawXTickLabels the drawXTickLabels to set
	 */
	public void setDrawXTickLabels( final boolean drawXTickLabels )
	{
		this.xAxisConfig.getRenderingConfig().setDrawMajorTickLabels( drawXTickLabels );
		this.xAxisConfig.getRenderingConfig().setDrawMinorTickLabels( drawXTickLabels );
	}

	/**
	 *	@param drawYTickLabels the drawYTickLabels to set
	 */
	public void setDrawYTickLabels( final boolean drawYTickLabels )
	{
		this.yAxisConfig.getRenderingConfig().setDrawMajorTickLabels( drawYTickLabels );
		this.yAxisConfig.getRenderingConfig().setDrawMinorTickLabels( drawYTickLabels );
	}

	/**
	 *	@param xTickLabelSize the xTickLabelSize to set
	 */
	public void setxTickLabelSize( final int xTickLabelSize )
	{
//		this.xTickLabelSize = xTickLabelSize;
		// TODO:
	}

	/**
	 *	@param xTickLabelColour the xTickLabelColour to set
	 */
	public void setxTickLabelColour( final Q xTickLabelColour )
	{
//		this.xTickLabelColour = xTickLabelColour;
		// TODO:
	}

	/**
	 *	@param yTickLabelSize the yTickLabelSize to set
	 */
	public void setyTickLabelSize( final int yTickLabelSize )
	{
//		yAxisConfig.getRenderingConfig().setMajorTickLabelFont( majorTickLabelFont );
		// TODO:
	}

	/**
	 *	@param yTickLabelColour the yTickLabelColour to set
	 */
	public void setyTickLabelColour( final Q yTickLabelColour )
	{
//		this.yTickLabelColour = yTickLabelColour;
//		yAxisConfig.getRenderingConfig().getMajor
		// TODO:
	}

	/**
	 *	@param autoSpaceLabels the autoSpaceLabels to set
	 */
	public void setAutoSpaceLabels( final boolean autoSpaceLabels )
	{
//		this.autoSpaceLabels = autoSpaceLabels;
		// TODO:
	}

	/**
	 *	@param autoSpaceTicks the autoSpaceTicks to set
	 */
	public void setAutoSpaceTicks( final boolean autoSpaceTicks )
	{
//		this.autoSpaceTicks = autoSpaceTicks;
		// TODO:
	}

	/**
	 *	@param minTickSpacing the minTickSpacing to set
	 */
	public void setMinTickSpacing( final int minTickSpacing )
	{
		// TODO:
	}

	/**
	 *	@param xLabelSpacing the xLabelSpacing to set
	 */
	public void setxLabelSpacing( final double xLabelSpacing )
	{
//		xAxisConfig.getRenderingConfig().setMajor
		// TODO:
	}

	/**
	 *	@param yLabelSpacing the yLabelSpacing to set
	 */
	public void setyLabelSpacing( final double yLabelSpacing )
	{
//		this.yLabelSpacing = yLabelSpacing;
		// TODO:
	}

	/**
	 *	@param xAxisName the xAxisName to set
	 */
	public void setxAxisName( final String xAxisName )
	{
		this.xAxisConfig.setName( xAxisName );
	}

	/**
	 *	@param yAxisName the yAxisName to set
	 */
	public void setyAxisName( final String yAxisName )
	{
		this.yAxisConfig.setName( yAxisName );
	}

	/**
	 *	@param drawYAxisName the drawYAxisName to set
	 */
	public void setDrawYAxisName( final boolean drawYAxisName )
	{
//		this.drawYAxisName = drawYAxisName;
		// TODO:
	}

	/**
	 *	@param drawXAxisName the drawXAxisName to set
	 */
	public void setDrawXAxisName( final boolean drawXAxisName )
	{
		// xAxisConfig.getRenderingConfig()
		// TODO:
	}

	/**
	 *	@param xAxisNameSize the xAxisNameSize to set
	 */
	public void setxAxisNameSize( final int xAxisNameSize )
	{
		this.xAxisConfig.getRenderingConfig().setNameSize( xAxisNameSize );
	}

	/**
	 *	@param xAxisNameColour the xAxisNameColour to set
	 */
	public void setxAxisNameColour( final Q xAxisNameColour )
	{
		this.xAxisConfig.getRenderingConfig().setNameColour( xAxisNameColour );
	}

	/**
	 *	@param yAxisNameSize the yAxisNameSize to set
	 */
	public void setyAxisNameSize( final int yAxisNameSize )
	{
		this.yAxisConfig.getRenderingConfig().setNameSize( yAxisNameSize );
	}

	/**
	 *	@param yAxisNameColour the yAxisNameColour to set
	 */
	public void setyAxisNameColour( final Q yAxisNameColour )
	{
		this.yAxisConfig.getRenderingConfig().setNameColour( yAxisNameColour );
	}

	/**
	 *	@param drawXTicks the drawXTicks to set
	 */
	public void setDrawXTicks( final boolean drawXTicks )
	{
		this.xAxisConfig.getRenderingConfig().setDrawMajorTicks( drawXTicks );
		this.xAxisConfig.getRenderingConfig().setDrawMinorTicks( drawXTicks );
	}

	/**
	 *	@param drawYTicks the drawYTicks to set
	 */
	public void setDrawYTicks( final boolean drawYTicks )
	{
		this.yAxisConfig.getRenderingConfig().setDrawMajorTicks( drawYTicks );
		this.yAxisConfig.getRenderingConfig().setDrawMinorTicks( drawYTicks );
	}

	/**
	 *	@param xAxisLabelTransformer the xAxisLabelTransformer to set
	 */
	public void setxAxisLabelTransformer( final LabelTransformer xAxisLabelTransformer )
	{
		this.xAxisConfig.getRenderingConfig().setLabelTransformer( xAxisLabelTransformer );
	}

	/**
	 *	@param yAxisLabelTransformer the yAxisLabelTransformer to set
	 */
	public void setyAxisLabelTransformer( final LabelTransformer yAxisLabelTransformer )
	{
		this.yAxisConfig.getRenderingConfig().setLabelTransformer( yAxisLabelTransformer );
	}

	/**
	 *	@param drawMinorTickGrid the drawMinorTickGrid to set
	 */
	public void setDrawMinorTickGrid( final boolean drawMinorTickGrid )
	{
		this.xAxisConfig.getRenderingConfig().setDrawMinorGrid( drawMinorTickGrid );
		this.yAxisConfig.getRenderingConfig().setDrawMinorGrid( drawMinorTickGrid );
	}

	/**
	 *	@param drawMajorTickGrid the drawMajorTickGrid to set
	 */
	public void setDrawMajorTickGrid( final boolean drawMajorTickGrid )
	{
		this.xAxisConfig.getRenderingConfig().setDrawMajorGrid( drawMajorTickGrid );
		this.yAxisConfig.getRenderingConfig().setDrawMajorGrid( drawMajorTickGrid );
	}

	/**
	 *	@param minorGridColour the minorGridColour to set
	 */
	public void setMinorGridColour( final Q minorGridColour )
	{
		this.xAxisConfig.getRenderingConfig().setMinorGridColour( minorGridColour );
		this.yAxisConfig.getRenderingConfig().setMinorGridColour( minorGridColour );
	}

	/**
	 *	@param majorGridColour the majorGridColour to set
	 */
	public void setMajorGridColour( final Q majorGridColour )
	{
		this.xAxisConfig.getRenderingConfig().setMajorGridColour( majorGridColour );
		this.yAxisConfig.getRenderingConfig().setMajorGridColour( majorGridColour );
	}

	/**
	 *	@param majorGridThickness the majorGridThickness to set
	 */
	public void setMajorGridThickness( final int majorGridThickness )
	{
		this.xAxisConfig.getRenderingConfig().setMajorGridThickness( majorGridThickness );
		this.yAxisConfig.getRenderingConfig().setMajorGridThickness( majorGridThickness );
	}

	/**
	 *	@param minorGridThickness the minorGridThickness to set
	 */
	public void setMinorGridThickness( final int minorGridThickness )
	{
		this.xAxisConfig.getRenderingConfig().setMinorGridThickness( minorGridThickness );
		this.yAxisConfig.getRenderingConfig().setMinorGridThickness( minorGridThickness );
	}

	/**
	 * 	Returns a data pixel transformer that is based on this axes renderer but has its values
	 * 	offset by the given number of pixels.
	 *
	 * 	// TODO: This method doesn't work at the moment
	 *
	 *	@param xOffset The x location
	 *	@param yOffset The y location
	 *	@return A new data pixel transformer
	 */
	public DataUnitsTransformer<Q,double[],int[]> getRelativePixelTransformer(
			final int xOffset, final int yOffset )
	{
		// TODO: This needs completing
		return new DataUnitsTransformer<Q,double[],int[]>()
		{
			@Override
			public void precalc()
			{
				// TODO Auto-generated method stub
			}

			@Override
			public int[] calculatePosition( final double[] units )
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public double[] calculateUnitsAt( final int[] position )
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int[] scaleDimension( final double[] dimension )
			{
				// TODO Auto-generated method stub
				return null;
			}

//			@Override
//			public void precalc( final double[] dim )
//			{
//				AxesRenderer2D.this.precalc( dim );
//			}
//
//			@Override
//			public Point2d calculatePosition( final double[] dim, final double x, final double y )
//			{
//				// Calculate the position of the data
//				final Point2d p = AxesRenderer2D.this.calculatePosition( dim, x, y );
//
//				// Then translate it back by the offset
//				p.translate( -xOffset, -yOffset );
//				return p;
//			}
//
//			@Override
//			public double[] calculateUnitsAt( final double[] dim, int x, int y )
//			{
//				// Translate the pixel to the original coordinates
//				x += xOffset;
//				y += yOffset;
//				// Now calculate the value
//				return AxesRenderer2D.this.calculateUnitsAt( dim, x, y );
//			}
		};
	}

	/**
	 *	@return the xAxisConfig
	 */
	public AxisConfig<Q> getxAxisConfig()
	{
		return this.xAxisConfig;
	}

	/**
	 *	@param xAxisConfig the xAxisConfig to set
	 */
	public void setxAxisConfig( final AxisConfig<Q> xAxisConfig )
	{
		this.xAxisConfig = xAxisConfig;
	}

	/**
	 *	@return the yAxisConfig
	 */
	public AxisConfig<Q> getyAxisConfig()
	{
		return this.yAxisConfig;
	}

	/**
	 *	@param yAxisConfig the yAxisConfig to set
	 */
	public void setyAxisConfig( final AxisConfig<Q> yAxisConfig )
	{
		this.yAxisConfig = yAxisConfig;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.DataUnitsTransformer#calculatePosition(java.lang.Object)
	 */
	@Override
	public int[] calculatePosition( final double[] units )
	{
		Point2dImpl p = new Point2dImpl( (float)units[0], (float)units[1] );
		p = p.transform( this.dataTransformMatrix );
		return new int[] { Math.round(p.getX()), Math.round(p.getY()) };

//		final double[] dx = this.xAxisRenderer.calculatePosition( units[0] );
//		final double[] dy = this.yAxisRenderer.calculatePosition( units[1] );
//
//		System.out.println( "units[0] = "+units[0]+" --> "+Arrays.toString( dx ) );
//		System.out.println( "units[1] = "+units[1]+" --> "+Arrays.toString( dy ) );
//
//		return new int[] { (int) dx[0], (int)dy[1] };
//				//(int)(dx[0]+dy[0]), (int)(dx[1]+dy[1]) };
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.DataUnitsTransformer#calculateUnitsAt(java.lang.Object)
	 */
	@Override
	public double[] calculateUnitsAt( final int[] position )
	{
		return new double[] {
				this.xAxisRenderer.calculateUnitsAt(
						new double[] {position[0],position[1]} ),
				this.yAxisRenderer.calculateUnitsAt(
						new double[] {position[0],position[1]} )
		};
	}

	/**
	 * 	Given two dimensions, returns the dimensions scaled to the appropriate sizes.
	 *	@param xs The x dimension
	 *	@param ys The y dimension
	 *	@return The scaled dimensions
	 */
	public double[] scaleDimensions( final double xs, final double ys )
	{
		return new double[]{
			xs * this.xAxisRenderer.getCurrentScale(),
			ys * this.yAxisRenderer.getCurrentScale()
		};
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.DataUnitsTransformer#scaleDimension(java.lang.Object)
	 */
	@Override
	public int[] scaleDimension( final double[] dimension )
	{
		return new int[]
		{
			(int)(dimension[0] * this.xAxisRenderer.getCurrentScale()),
			(int)(dimension[1] * this.yAxisRenderer.getCurrentScale())
		};
	}

	/**
	 * 	Helper function to calulate the render position of a data unit
	 *	@param x The data unit x
	 *	@param y The data unit y
	 *	@return The render position
	 */
	public Point2d calculatePosition( final double x, final double y )
	{
		final int[] p = this.calculatePosition( new double[] {x, y} );
		return new Point2dImpl( p[0], p[1] );
	}

	/**
	 * 	Config may be null as it is not used.
	 *
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.DataUnitsTransformer#precalc()
	 */
	@Override
	synchronized public void precalc()
	{
		// Set the position of the x axis
		final double[] xLoc = this.xAxisConfig.getLocation();
		xLoc[0] = this.axisPaddingLeft;
		xLoc[1] = this.getxAxisPosition();
		this.xAxisRenderer.precalc();

		// Calculate a data transform matrix for working out the position of data
		this.dataTransformMatrix = Matrix.identity( 3, 3 );
		this.dataTransformMatrix = TransformUtilities.translateMatrix(
				-this.xAxisConfig.getMinValue(), 0 ).times( this.dataTransformMatrix );
		this.dataTransformMatrix = TransformUtilities.scaleMatrix(
				this.xAxisRenderer.getCurrentScale(), -this.yAxisRenderer.getCurrentScale() )
				.times( this.dataTransformMatrix );
		this.dataTransformMatrix = TransformUtilities.rotationMatrix(
				this.xAxisConfig.getOrientation()[0] ).times( this.dataTransformMatrix );
		this.dataTransformMatrix = TransformUtilities.translateMatrix( xLoc[0], xLoc[1] )
				.times( this.dataTransformMatrix );

		// Re-seat the y axis
		final double[] yLoc = this.yAxisConfig.getLocation();
		final Point2d dd = this.calculatePosition( 0, this.yAxisConfig.getMinValue() );
		yLoc[0] = dd.getX();
		yLoc[1] = dd.getY();
		this.yAxisRenderer.precalc();
	}

	/**
	 *	@param image the image to set
	 */
	public void setImage( final I image )
	{
		synchronized( this )
		{
			this.xAxisRenderer.setImage( image );
			this.yAxisRenderer.setImage( image );

			this.xAxisRenderer.setAxisLength(
				image.getWidth() - this.axisPaddingLeft - this.axisPaddingRight );
			this.yAxisRenderer.setAxisLength(
				image.getHeight() - this.axisPaddingBottom - this.axisPaddingTop );
		}
	}

	/**
	 *	@return the xAxisRenderer
	 */
	public AxisRenderer2D<Q> getxAxisRenderer()
	{
		return this.xAxisRenderer;
	}

	/**
	 *	@return the yAxisRenderer
	 */
	public AxisRenderer2D<Q> getyAxisRenderer()
	{
		return this.yAxisRenderer;
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		final MBFImage visImage = new MBFImage( 1000, 600, 3 );
		final AxesRenderer2D<Float[],MBFImage> ar = new AxesRenderer2D<Float[],MBFImage>();

		ar.setxAxisColour( RGBColour.WHITE );
		ar.setyAxisColour( RGBColour.WHITE );
		ar.setMajorTickColour( RGBColour.WHITE );
		ar.setMinorTickColour( RGBColour.GRAY );
		ar.setxTickLabelColour( RGBColour.GRAY );
		ar.setyTickLabelColour( RGBColour.GRAY );
		ar.setxAxisNameColour( RGBColour.WHITE );
		ar.setyAxisNameColour( RGBColour.WHITE );

		ar.setxAxisPosition( 100 );
		ar.setMinXValue( -1 );
		ar.setMaxXValue( 1 );
		ar.setMinYValue( -5 );
		ar.setMaxYValue( 1 );
		ar.setDrawYAxis( true );
		ar.setDrawXAxis( true );
		ar.setDrawXAxisName( true );
		ar.setDrawYAxisName( true );
		ar.setDrawXTickLabels( true );
		ar.setMinorTickLength( 5 );
		ar.setMajorTickLength( 7 );
		ar.setMajorTickThickness( 3 );
		ar.setMinorTickThickenss( 1 );
		ar.setxMinorTickSpacing( 0.2 );
		ar.setxMajorTickSpacing( 1 );
		ar.setxLabelSpacing( 0.25 );
//		ar.setxTickLabelFont( new GeneralFont( "Arial", java.awt.Font.PLAIN ) );
		ar.setxTickLabelSize( 14 );
		ar.setyMinorTickSpacing( 0.1 );
		ar.setyMajorTickSpacing( 1 );
		ar.setyLabelSpacing( 0.5 );
//		ar.setyTickLabelFont = new GeneralFont( "Arial", java.awt.Font.PLAIN );
		ar.setyTickLabelSize( 14 );
		ar.setxAxisName( "Stuff" );
//		ar.setxAxisNameFont = new GeneralFont( "Times New Roman", java.awt.Font.PLAIN );
		ar.setxAxisNameSize( 25 );
		ar.setyAxisName( "Things" );
//		ar.setyAxisNameFont = new GeneralFont( "Times New Roman", java.awt.Font.PLAIN );
		ar.setyAxisNameSize( 25 );

		ar.precalc();
		ar.renderAxis( visImage );
		DisplayUtilities.display( visImage );
	}
}
