/**
 *
 */
package org.openimaj.vis.general;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <Q> The pixel type of the image you'll be drawing the axes to
 *  @created 3 Jun 2013
 */
public class AxesRenderer<Q>
{
	/** Whether to draw the x axis */
	private boolean drawXAxis = true;

	/** Whether to draw the y axis */
	private boolean drawYAxis = true;

	/** How far from the left of the image will the x axis start */
	private int axisPaddingLeft = 20;

	/** How far from the right of the image will the x axis stop */
	private int axisPaddingRight = 20;

	/** How far from the top of the image with the y axis start */
	private int axisPaddingTop = 20;

	/** How far from the bottom of the image will the y axis stop */
	private int axisPaddingBottom = 20;

	/** The x position of the y axis */
	private double yAxisPosition = 100;

	/** The y position of the x axis */
	private double xAxisPosition = 100;

	/** Whether to work out the ideal position for the axes */
	private boolean autoScaleAxes = true;

	/** The thickness of the x-axis */
	private int xAxisThickness = 3;

	/** The colour of the x-axis */
	private Q xAxisColour;

	/** The thickness of the y-axis */
	private int yAxisThickness = 3;

	/** The colour of the y-axis */
	private Q yAxisColour;

	/** The maximum value (in units) of the X axis */
	private double maxXValue = 1;

	/** The minimum value (in units) on the X axis */
	private double minXValue = -1;

	/** The maximum value (in units) on the Y axis */
	private double maxYValue = 1;

	/** The minimum value (in units) on the Y axis */
	private double minYValue = -1;

	/** The spacing (in units) of the ticks on the x axis */
	private double xMinorTickSpacing = 0.25;

	/** The spacing (in units) of the ticks on the y axis */
	private double yMinorTickSpacing = 0.25;

	/** Major tick marks on the x axis spacing */
	private double xMajorTickSpacing = 0.5;

	/** Major tick marks on the y axis spacing */
	private double yMajorTickSpacing = 0.5;

	/** The length of the minor ticks */
	private int minorTickLength = 5;

	/** The length of the major ticks */
	private int majorTickLength = 9;

	/** The colour of the minor ticks */
	private Q minorTickColour;

	/** The colour of the major ticks */
	private Q majorTickColour;

	/** The thickness of the major ticks on the axes */
	private int majorTickThickness = 3;

	/** The thickness of the minor tick marks on the axes */
	private int minorTickThickenss = 1;

	/** Whether to draw the X ticks */
	private boolean drawXTicks = true;

	/** Whether to draw labels on the x axis */
	private boolean drawXTickLabels = true;

	/** whether to draw the Y ticks */
	private boolean drawYTicks = true;

	/** Whether to draw labels on the y axis */
	private boolean drawYTickLabels = true;

	/** The font to use for the x tick labels */
	@SuppressWarnings( "rawtypes" )
	private Font xTickLabelFont = HersheyFont.ROMAN_DUPLEX;

	/** The size of the x tick labels */
	private int xTickLabelSize = 8;

	/** The colour of the x tick labels */
	private Q xTickLabelColour;

	/** The font to use for the y tick labels */
	@SuppressWarnings( "rawtypes" )
	private Font yTickLabelFont = HersheyFont.ROMAN_DUPLEX;

	/** The size of the y tick labels */
	private int yTickLabelSize = 8;

	/** The colour of the y tick labels */
	private Q yTickLabelColour;

	/** Whether to attempt to auto space the labels on both axes */
	private boolean autoSpaceLabels = true;

	/** Whether to attempt to auto space the ticks on both axes */
	private boolean autoSpaceTicks = false;

	/** If auto spacing, the minimum space (in pixels) between ticks */
	private int minTickSpacing = 10;

	/** The spacing between labels on the x axis (in units) */
	private double xLabelSpacing = 0.25;

	/** The spacing between labels on the y axis (in units) */
	private double yLabelSpacing = 0.25;

	/** The name of the x-axis */
	private String xAxisName = "x-axis";

	/** The name of the y-axis */
	private String yAxisName = "y-axis";

	/** Whether to draw the y-axis name */
	private boolean drawYAxisName = true;

	/** Whether to draw the x-axis name */
	private boolean drawXAxisName = true;

	/** The font to use for the x axis name */
	@SuppressWarnings( "rawtypes" )
	private Font xAxisNameFont = HersheyFont.FUTURA_MEDIUM;

	/** The size of the font to use for the x axis name */
	private int xAxisNameSize = 15;

	/** The colour to draw the x axis name */
	private Q xAxisNameColour;

	/** The font to use for the x axis name */
	@SuppressWarnings( "rawtypes" )
	private Font yAxisNameFont = HersheyFont.FUTURA_MEDIUM;

	/** The size of the font to use for the x axis name */
	private int yAxisNameSize = 15;

	/** The colour to draw the x axis name */
	private Q yAxisNameColour;

	/** The calculated size of each unit in the x direction */
	private double xUnitSizePx;

	/** The calculated size of each unit in the y direction */
	private double yUnitSizePx;

	/**
	 * 	Default constructor
	 */
	public AxesRenderer()
	{
	}

	/**
	 * 	Can be called to force the precalculation of some of the variables.
	 * 	@param image The image
	 */
	public <I extends Image<Q,I>> void precalc( final Image<Q, ? extends Image<Q,I>> image )
	{
		// Get the dimensions of the image to draw to
		final int w = image.getWidth();
		final int h = image.getHeight();

		// Find the pixel spacing to use
		final double xRange = this.maxXValue - this.minXValue;
		this.xUnitSizePx = (w-this.axisPaddingLeft-this.axisPaddingRight) / xRange;
		final double yRange = this.maxYValue - this.minYValue;
		this.yUnitSizePx = (h-this.axisPaddingBottom-this.axisPaddingTop) / yRange;

		// Pixel position of where the axes crossing happens
		this.yAxisPosition = this.axisPaddingLeft - this.minXValue*this.xUnitSizePx;
		this.xAxisPosition = h - this.axisPaddingBottom + this.minYValue*this.yUnitSizePx;
	}

	/**
	 * 	Render the axis to the given image
	 *	@param image The image to draw the axes to
	 */
	@SuppressWarnings( "unchecked" )
	public <I extends Image<Q,I>> void renderAxis( final Image<Q, ? extends Image<Q,I>> image )
	{
		// Create the renderer to draw to the image
		final ImageRenderer<Q, ? extends Image<Q, I>> ir = image.createRenderer();

		// Get the dimensions of the image to draw to
		final int w = image.getWidth();
		final int h = image.getHeight();

		// Find the pixel spacing to use
		final double xRange = this.maxXValue - this.minXValue;
		this.xUnitSizePx = (w-this.axisPaddingLeft-this.axisPaddingRight) / xRange;
		final double yRange = this.maxYValue - this.minYValue;
		this.yUnitSizePx = (h-this.axisPaddingBottom-this.axisPaddingTop) / yRange;

		// Pixel position of where the axes crossing happens
		this.yAxisPosition = this.axisPaddingLeft - this.minXValue*this.xUnitSizePx;
		this.xAxisPosition = h - this.axisPaddingBottom + this.minYValue*this.yUnitSizePx;

		// Draw the x-axis minor ticks
		if( this.drawXAxis && this.drawXTicks )
			for( double v = this.minXValue; v <= this.maxXValue; v += this.xMinorTickSpacing )
				ir.drawLine(
					(int)(this.yAxisPosition + v * this.xUnitSizePx),
					(int)(this.xAxisPosition-this.minorTickLength),
					(int)(this.yAxisPosition + v * this.xUnitSizePx),
					(int)(this.xAxisPosition+this.minorTickLength),
					this.minorTickThickenss,
					this.minorTickColour );

		// Draw the x-axis major ticks
		if( this.drawXAxis && this.drawXTicks )
			for( double v = this.minXValue; v <= this.maxXValue; v += this.xMajorTickSpacing )
				ir.drawLine(
					(int)(this.yAxisPosition + v * this.xUnitSizePx),
					(int)(this.xAxisPosition-this.majorTickLength),
					(int)(this.yAxisPosition + v * this.xUnitSizePx),
					(int)(this.xAxisPosition+this.majorTickLength),
					this.majorTickThickness,
					this.majorTickColour );

		// Draw the x tick labels
		double maxXLabelPosition = 0;
		if( this.drawXAxis && this.drawXTickLabels )
		{
			final int yPos = (int)(this.xAxisPosition + this.xTickLabelSize + this.majorTickLength
					+ this.xTickLabelSize/2 );

			@SuppressWarnings( "rawtypes" )
			final FontStyle s = this.xTickLabelFont.createStyle( ir );
			s.setFontSize( this.xTickLabelSize );

			@SuppressWarnings( "rawtypes" )
			final FontRenderer r = this.xTickLabelFont.getRenderer( ir );

			for( double v = this.minXValue; v <= this.maxXValue; v += this.xLabelSpacing )
			{
				final String text = ""+v;
				final float fw = r.getBounds( text, s ).width;
				final int xPos = (int)(this.yAxisPosition + v*this.xUnitSizePx - fw/2);
				ir.drawText( text, xPos, yPos, this.xTickLabelFont,
						this.xTickLabelSize, this.xTickLabelColour );
			}
			maxXLabelPosition = yPos;
		}

		// Draw the y-axis ticks
		if( this.drawYAxis && this.drawYTicks )
			for( double v = this.minYValue; v <= this.maxYValue; v += this.yMinorTickSpacing )
				ir.drawLine(
					(int)(this.yAxisPosition-this.minorTickLength),
					(int)(this.xAxisPosition - v * this.yUnitSizePx),
					(int)(this.yAxisPosition+this.minorTickLength),
					(int)(this.xAxisPosition - v * this.yUnitSizePx),
					this.minorTickThickenss,
					this.minorTickColour );

		// Draw the y-axis ticks
		if( this.drawYAxis && this.drawYTicks )
			for( double v = this.minYValue; v <= this.maxYValue; v += this.yMajorTickSpacing )
				ir.drawLine(
					(int)(this.yAxisPosition-this.majorTickLength),
					(int)(this.xAxisPosition - v * this.yUnitSizePx),
					(int)(this.yAxisPosition+this.majorTickLength),
					(int)(this.xAxisPosition - v * this.yUnitSizePx),
					this.majorTickThickness,
					this.majorTickColour );

		// Draw the x tick labels
		double minYLabelPosition = this.yAxisPosition;
		if( this.drawYAxis && this.drawYTickLabels )
		{
			@SuppressWarnings( "rawtypes" )
			final FontStyle s = this.yTickLabelFont.createStyle( ir );
			s.setFontSize( this.yTickLabelSize );

			@SuppressWarnings( "rawtypes" )
			final FontRenderer r = this.yTickLabelFont.getRenderer( ir );

			for( double v = this.minYValue; v <= this.maxYValue; v += this.yLabelSpacing )
			{
				final String text = ""+v;
				final float fw = r.getBounds( text, s ).width;
				final int xPos = (int)(this.yAxisPosition - fw - this.majorTickLength
						- this.yTickLabelSize/2 );	// Last part is just a bit of padding
				final int yPos = (int)(this.xAxisPosition - v*this.yUnitSizePx + this.yTickLabelSize/2 );
				ir.drawText( text, xPos, yPos, this.yTickLabelFont,
						this.yTickLabelSize, this.yTickLabelColour );
				minYLabelPosition = Math.min( xPos, minYLabelPosition );
			}
		}

		// Draw the X-axis
		if( this.drawXAxis )
			ir.drawLine( this.axisPaddingLeft, (int)this.xAxisPosition, w-this.axisPaddingRight,
				(int)this.xAxisPosition, this.xAxisThickness, this.xAxisColour );

		// Draw the Y-axis
		if( this.drawYAxis )
			ir.drawLine( (int)this.yAxisPosition, this.axisPaddingTop, (int)this.yAxisPosition,
				h-this.axisPaddingBottom, this.yAxisThickness, this.yAxisColour );

		// Draw the X-axis label
		if( this.drawXAxis && this.drawXAxisName )
			ir.drawText( this.xAxisName, this.axisPaddingLeft,
					(int)(maxXLabelPosition + this.xAxisNameSize + this.majorTickLength),
					this.xAxisNameFont,
					this.xAxisNameSize, this.xAxisNameColour );

		// Draw the Y-axis label
		if( this.drawYAxis && this.drawYAxisName )
		{
			final float fw = this.yAxisNameFont.getRenderer( ir ).getBounds(
					this.yAxisName, this.yAxisNameFont.createStyle( ir ) ).width;
			ir.drawText( this.yAxisName, (int)(minYLabelPosition - fw),
					this.yAxisNameSize + this.axisPaddingTop, this.yAxisNameFont,
					this.yAxisNameSize, this.yAxisNameColour );
		}
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
	public <I extends Image<Q,I>> Point2d calculatePosition(
			final Image<Q, ? extends Image<Q,I>> image, final double x, final double y )
	{
		return new Point2dImpl( (float)(this.yAxisPosition + x*this.xUnitSizePx),
				(float)(this.xAxisPosition - y*this.yUnitSizePx) );
	}

	/**
	 *	Calculates the data unit coordinate at the given pixel position.
	 *
	 *	@param image The image in which the axes were drawn
	 *	@param x The x pixel position
	 *	@param y The y pixel position
	 *	@return the x and y unit coordinates in a double
	 */
	public <I extends Image<Q,I>> double[] calculateUnitsAt(
			final Image<Q, ? extends Image<Q,I>> image, final int x, final int y )
	{
		return new double[] { (x-this.yAxisPosition)/this.xUnitSizePx,
				(this.xAxisPosition-y)/this.yUnitSizePx };
	}

	/**
	 *	@return the drawXAxis
	 */
	public boolean isDrawXAxis()
	{
		return this.drawXAxis;
	}

	/**
	 *	@param drawXAxis the drawXAxis to set
	 */
	public void setDrawXAxis( final boolean drawXAxis )
	{
		this.drawXAxis = drawXAxis;
	}

	/**
	 *	@return the drawYAxis
	 */
	public boolean isDrawYAxis()
	{
		return this.drawYAxis;
	}

	/**
	 *	@param drawYAxis the drawYAxis to set
	 */
	public void setDrawYAxis( final boolean drawYAxis )
	{
		this.drawYAxis = drawYAxis;
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
	 *	@return the yAxisPosition
	 */
	public double getyAxisPosition()
	{
		return this.yAxisPosition;
	}

	/**
	 *	@param yAxisPosition the yAxisPosition to set
	 */
	public void setyAxisPosition( final double yAxisPosition )
	{
		this.yAxisPosition = yAxisPosition;
	}

	/**
	 *	@return the xAxisPosition
	 */
	public double getxAxisPosition()
	{
		return this.xAxisPosition;
	}

	/**
	 *	@param xAxisPosition the xAxisPosition to set
	 */
	public void setxAxisPosition( final double xAxisPosition )
	{
		this.xAxisPosition = xAxisPosition;
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
	 *	@return the xAxisThickness
	 */
	public int getxAxisThickness()
	{
		return this.xAxisThickness;
	}

	/**
	 *	@param xAxisThickness the xAxisThickness to set
	 */
	public void setxAxisThickness( final int xAxisThickness )
	{
		this.xAxisThickness = xAxisThickness;
	}

	/**
	 *	@return the xAxisColour
	 */
	public Q getxAxisColour()
	{
		return this.xAxisColour;
	}

	/**
	 *	@param xAxisColour the xAxisColour to set
	 */
	public void setxAxisColour( final Q xAxisColour )
	{
		this.xAxisColour = xAxisColour;
	}

	/**
	 *	@return the yAxisThickness
	 */
	public int getyAxisThickness()
	{
		return this.yAxisThickness;
	}

	/**
	 *	@param yAxisThickness the yAxisThickness to set
	 */
	public void setyAxisThickness( final int yAxisThickness )
	{
		this.yAxisThickness = yAxisThickness;
	}

	/**
	 *	@return the yAxisColour
	 */
	public Q getyAxisColour()
	{
		return this.yAxisColour;
	}

	/**
	 *	@param yAxisColour the yAxisColour to set
	 */
	public void setyAxisColour( final Q yAxisColour )
	{
		this.yAxisColour = yAxisColour;
	}

	/**
	 *	@return the maxXValue
	 */
	public double getMaxXValue()
	{
		return this.maxXValue;
	}

	/**
	 *	@param maxXValue the maxXValue to set
	 */
	public void setMaxXValue( final double maxXValue )
	{
		this.maxXValue = maxXValue;
	}

	/**
	 *	@return the minXValue
	 */
	public double getMinXValue()
	{
		return this.minXValue;
	}

	/**
	 *	@param minXValue the minXValue to set
	 */
	public void setMinXValue( final double minXValue )
	{
		this.minXValue = minXValue;
	}

	/**
	 *	@return the maxYValue
	 */
	public double getMaxYValue()
	{
		return this.maxYValue;
	}

	/**
	 *	@param maxYValue the maxYValue to set
	 */
	public void setMaxYValue( final double maxYValue )
	{
		this.maxYValue = maxYValue;
	}

	/**
	 *	@return the minYValue
	 */
	public double getMinYValue()
	{
		return this.minYValue;
	}

	/**
	 *	@param minYValue the minYValue to set
	 */
	public void setMinYValue( final double minYValue )
	{
		this.minYValue = minYValue;
	}

	/**
	 *	@return the xMinorTickSpacing
	 */
	public double getxMinorTickSpacing()
	{
		return this.xMinorTickSpacing;
	}

	/**
	 *	@param xMinorTickSpacing the xMinorTickSpacing to set
	 */
	public void setxMinorTickSpacing( final double xMinorTickSpacing )
	{
		this.xMinorTickSpacing = xMinorTickSpacing;
	}

	/**
	 *	@return the yMinorTickSpacing
	 */
	public double getyMinorTickSpacing()
	{
		return this.yMinorTickSpacing;
	}

	/**
	 *	@param yMinorTickSpacing the yMinorTickSpacing to set
	 */
	public void setyMinorTickSpacing( final double yMinorTickSpacing )
	{
		this.yMinorTickSpacing = yMinorTickSpacing;
	}

	/**
	 *	@return the xMajorTickSpacing
	 */
	public double getxMajorTickSpacing()
	{
		return this.xMajorTickSpacing;
	}

	/**
	 *	@param xMajorTickSpacing the xMajorTickSpacing to set
	 */
	public void setxMajorTickSpacing( final double xMajorTickSpacing )
	{
		this.xMajorTickSpacing = xMajorTickSpacing;
	}

	/**
	 *	@return the yMajorTickSpacing
	 */
	public double getyMajorTickSpacing()
	{
		return this.yMajorTickSpacing;
	}

	/**
	 *	@param yMajorTickSpacing the yMajorTickSpacing to set
	 */
	public void setyMajorTickSpacing( final double yMajorTickSpacing )
	{
		this.yMajorTickSpacing = yMajorTickSpacing;
	}

	/**
	 *	@return the minorTickLength
	 */
	public int getMinorTickLength()
	{
		return this.minorTickLength;
	}

	/**
	 *	@param minorTickLength the minorTickLength to set
	 */
	public void setMinorTickLength( final int minorTickLength )
	{
		this.minorTickLength = minorTickLength;
	}

	/**
	 *	@return the majorTickLength
	 */
	public int getMajorTickLength()
	{
		return this.majorTickLength;
	}

	/**
	 *	@param majorTickLength the majorTickLength to set
	 */
	public void setMajorTickLength( final int majorTickLength )
	{
		this.majorTickLength = majorTickLength;
	}

	/**
	 *	@return the minorTickColour
	 */
	public Q getMinorTickColour()
	{
		return this.minorTickColour;
	}

	/**
	 *	@param minorTickColour the minorTickColour to set
	 */
	public void setMinorTickColour( final Q minorTickColour )
	{
		this.minorTickColour = minorTickColour;
	}

	/**
	 *	@return the majorTickColour
	 */
	public Q getMajorTickColour()
	{
		return this.majorTickColour;
	}

	/**
	 *	@param majorTickColour the majorTickColour to set
	 */
	public void setMajorTickColour( final Q majorTickColour )
	{
		this.majorTickColour = majorTickColour;
	}

	/**
	 *	@return the majorTickThickness
	 */
	public int getMajorTickThickness()
	{
		return this.majorTickThickness;
	}

	/**
	 *	@param majorTickThickness the majorTickThickness to set
	 */
	public void setMajorTickThickness( final int majorTickThickness )
	{
		this.majorTickThickness = majorTickThickness;
	}

	/**
	 *	@return the minorTickThickenss
	 */
	public int getMinorTickThickenss()
	{
		return this.minorTickThickenss;
	}

	/**
	 *	@param minorTickThickenss the minorTickThickenss to set
	 */
	public void setMinorTickThickenss( final int minorTickThickenss )
	{
		this.minorTickThickenss = minorTickThickenss;
	}

	/**
	 *	@return the drawXTickLabels
	 */
	public boolean isDrawXTickLabels()
	{
		return this.drawXTickLabels;
	}

	/**
	 *	@param drawXTickLabels the drawXTickLabels to set
	 */
	public void setDrawXTickLabels( final boolean drawXTickLabels )
	{
		this.drawXTickLabels = drawXTickLabels;
	}

	/**
	 *	@return the drawYTickLabels
	 */
	public boolean isDrawYTickLabels()
	{
		return this.drawYTickLabels;
	}

	/**
	 *	@param drawYTickLabels the drawYTickLabels to set
	 */
	public void setDrawYTickLabels( final boolean drawYTickLabels )
	{
		this.drawYTickLabels = drawYTickLabels;
	}

	/**
	 *	@return the xTickLabelFont
	 */
	@SuppressWarnings( "rawtypes" )
	public Font getxTickLabelFont()
	{
		return this.xTickLabelFont;
	}

	/**
	 *	@param xTickLabelFont the xTickLabelFont to set
	 */
	@SuppressWarnings( "rawtypes" )
	public void setxTickLabelFont( final Font xTickLabelFont )
	{
		this.xTickLabelFont = xTickLabelFont;
	}

	/**
	 *	@return the xTickLabelSize
	 */
	public int getxTickLabelSize()
	{
		return this.xTickLabelSize;
	}

	/**
	 *	@param xTickLabelSize the xTickLabelSize to set
	 */
	public void setxTickLabelSize( final int xTickLabelSize )
	{
		this.xTickLabelSize = xTickLabelSize;
	}

	/**
	 *	@return the xTickLabelColour
	 */
	public Q getxTickLabelColour()
	{
		return this.xTickLabelColour;
	}

	/**
	 *	@param xTickLabelColour the xTickLabelColour to set
	 */
	public void setxTickLabelColour( final Q xTickLabelColour )
	{
		this.xTickLabelColour = xTickLabelColour;
	}

	/**
	 *	@return the yTickLabelFont
	 */
	@SuppressWarnings( "rawtypes" )
	public Font getyTickLabelFont()
	{
		return this.yTickLabelFont;
	}

	/**
	 *	@param yTickLabelFont the yTickLabelFont to set
	 */
	@SuppressWarnings( "rawtypes" )
	public void setyTickLabelFont( final Font yTickLabelFont )
	{
		this.yTickLabelFont = yTickLabelFont;
	}

	/**
	 *	@return the yTickLabelSize
	 */
	public int getyTickLabelSize()
	{
		return this.yTickLabelSize;
	}

	/**
	 *	@param yTickLabelSize the yTickLabelSize to set
	 */
	public void setyTickLabelSize( final int yTickLabelSize )
	{
		this.yTickLabelSize = yTickLabelSize;
	}

	/**
	 *	@return the yTickLabelColour
	 */
	public Q getyTickLabelColour()
	{
		return this.yTickLabelColour;
	}

	/**
	 *	@param yTickLabelColour the yTickLabelColour to set
	 */
	public void setyTickLabelColour( final Q yTickLabelColour )
	{
		this.yTickLabelColour = yTickLabelColour;
	}

	/**
	 *	@return the autoSpaceLabels
	 */
	public boolean isAutoSpaceLabels()
	{
		return this.autoSpaceLabels;
	}

	/**
	 *	@param autoSpaceLabels the autoSpaceLabels to set
	 */
	public void setAutoSpaceLabels( final boolean autoSpaceLabels )
	{
		this.autoSpaceLabels = autoSpaceLabels;
	}

	/**
	 *	@return the autoSpaceTicks
	 */
	public boolean isAutoSpaceTicks()
	{
		return this.autoSpaceTicks;
	}

	/**
	 *	@param autoSpaceTicks the autoSpaceTicks to set
	 */
	public void setAutoSpaceTicks( final boolean autoSpaceTicks )
	{
		this.autoSpaceTicks = autoSpaceTicks;
	}

	/**
	 *	@return the minTickSpacing
	 */
	public int getMinTickSpacing()
	{
		return this.minTickSpacing;
	}

	/**
	 *	@param minTickSpacing the minTickSpacing to set
	 */
	public void setMinTickSpacing( final int minTickSpacing )
	{
		this.minTickSpacing = minTickSpacing;
	}

	/**
	 *	@return the xLabelSpacing
	 */
	public double getxLabelSpacing()
	{
		return this.xLabelSpacing;
	}

	/**
	 *	@param xLabelSpacing the xLabelSpacing to set
	 */
	public void setxLabelSpacing( final double xLabelSpacing )
	{
		this.xLabelSpacing = xLabelSpacing;
	}

	/**
	 *	@return the yLabelSpacing
	 */
	public double getyLabelSpacing()
	{
		return this.yLabelSpacing;
	}

	/**
	 *	@param yLabelSpacing the yLabelSpacing to set
	 */
	public void setyLabelSpacing( final double yLabelSpacing )
	{
		this.yLabelSpacing = yLabelSpacing;
	}

	/**
	 *	@return the xAxisName
	 */
	public String getxAxisName()
	{
		return this.xAxisName;
	}

	/**
	 *	@param xAxisName the xAxisName to set
	 */
	public void setxAxisName( final String xAxisName )
	{
		this.xAxisName = xAxisName;
	}

	/**
	 *	@return the yAxisName
	 */
	public String getyAxisName()
	{
		return this.yAxisName;
	}

	/**
	 *	@param yAxisName the yAxisName to set
	 */
	public void setyAxisName( final String yAxisName )
	{
		this.yAxisName = yAxisName;
	}

	/**
	 *	@return the drawYAxisName
	 */
	public boolean isDrawYAxisName()
	{
		return this.drawYAxisName;
	}

	/**
	 *	@param drawYAxisName the drawYAxisName to set
	 */
	public void setDrawYAxisName( final boolean drawYAxisName )
	{
		this.drawYAxisName = drawYAxisName;
	}

	/**
	 *	@return the drawXAxisName
	 */
	public boolean isDrawXAxisName()
	{
		return this.drawXAxisName;
	}

	/**
	 *	@param drawXAxisName the drawXAxisName to set
	 */
	public void setDrawXAxisName( final boolean drawXAxisName )
	{
		this.drawXAxisName = drawXAxisName;
	}

	/**
	 *	@return the xAxisNameFont
	 */
	@SuppressWarnings( "rawtypes" )
	public Font getxAxisNameFont()
	{
		return this.xAxisNameFont;
	}

	/**
	 *	@param xAxisNameFont the xAxisNameFont to set
	 */
	@SuppressWarnings( "rawtypes" )
	public void setxAxisNameFont( final Font xAxisNameFont )
	{
		this.xAxisNameFont = xAxisNameFont;
	}

	/**
	 *	@return the xAxisNameSize
	 */
	public int getxAxisNameSize()
	{
		return this.xAxisNameSize;
	}

	/**
	 *	@param xAxisNameSize the xAxisNameSize to set
	 */
	public void setxAxisNameSize( final int xAxisNameSize )
	{
		this.xAxisNameSize = xAxisNameSize;
	}

	/**
	 *	@return the xAxisNameColour
	 */
	public Q getxAxisNameColour()
	{
		return this.xAxisNameColour;
	}

	/**
	 *	@param xAxisNameColour the xAxisNameColour to set
	 */
	public void setxAxisNameColour( final Q xAxisNameColour )
	{
		this.xAxisNameColour = xAxisNameColour;
	}

	/**
	 *	@return the yAxisNameFont
	 */
	@SuppressWarnings( "rawtypes" )
	public Font getyAxisNameFont()
	{
		return this.yAxisNameFont;
	}

	/**
	 *	@param yAxisNameFont the yAxisNameFont to set
	 */
	@SuppressWarnings( "rawtypes" )
	public void setyAxisNameFont( final Font yAxisNameFont )
	{
		this.yAxisNameFont = yAxisNameFont;
	}

	/**
	 *	@return the yAxisNameSize
	 */
	public int getyAxisNameSize()
	{
		return this.yAxisNameSize;
	}

	/**
	 *	@param yAxisNameSize the yAxisNameSize to set
	 */
	public void setyAxisNameSize( final int yAxisNameSize )
	{
		this.yAxisNameSize = yAxisNameSize;
	}

	/**
	 *	@return the yAxisNameColour
	 */
	public Q getyAxisNameColour()
	{
		return this.yAxisNameColour;
	}

	/**
	 *	@param yAxisNameColour the yAxisNameColour to set
	 */
	public void setyAxisNameColour( final Q yAxisNameColour )
	{
		this.yAxisNameColour = yAxisNameColour;
	}

	/**
	 *	@return the xUnitSizePx
	 */
	public double getxUnitSizePx()
	{
		return this.xUnitSizePx;
	}

	/**
	 *	@return the yUnitSizePx
	 */
	public double getyUnitSizePx()
	{
		return this.yUnitSizePx;
	}

	/**
	 *	@return the drawXTicks
	 */
	public boolean isDrawXTicks()
	{
		return this.drawXTicks;
	}

	/**
	 *	@return the drawYTicks
	 */
	public boolean isDrawYTicks()
	{
		return this.drawYTicks;
	}

	/**
	 *	@param drawXTicks the drawXTicks to set
	 */
	public void setDrawXTicks( final boolean drawXTicks )
	{
		this.drawXTicks = drawXTicks;
	}

	/**
	 *	@param drawYTicks the drawYTicks to set
	 */
	public void setDrawYTicks( final boolean drawYTicks )
	{
		this.drawYTicks = drawYTicks;
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		final MBFImage visImage = new MBFImage( 1000, 600, 3 );
		final AxesRenderer<Float[]> ar = new AxesRenderer<Float[]>();

		ar.xAxisColour = RGBColour.WHITE;
		ar.yAxisColour = RGBColour.WHITE;
		ar.majorTickColour = RGBColour.WHITE;
		ar.minorTickColour = RGBColour.GRAY;
		ar.xTickLabelColour = RGBColour.GRAY;
		ar.yTickLabelColour = RGBColour.GRAY;
		ar.xAxisNameColour = RGBColour.WHITE;
		ar.yAxisNameColour = RGBColour.WHITE;

		ar.minXValue = -1;
		ar.maxXValue = 1;
		ar.minYValue = -5;
		ar.maxYValue = 1;
		ar.drawYAxis = true;
		ar.drawXAxis = true;
		ar.drawXAxisName = true;
		ar.drawYAxisName = true;
		ar.drawXTickLabels = true;
		ar.minorTickLength = 5;
		ar.majorTickLength = 7;
		ar.majorTickThickness = 3;
		ar.minorTickThickenss = 1;
		ar.xMinorTickSpacing = 0.05;
		ar.xMajorTickSpacing = 0.25;
		ar.xLabelSpacing = 0.25;
		ar.xTickLabelFont = new GeneralFont( "Arial", java.awt.Font.PLAIN );
		ar.xTickLabelSize = 14;
		ar.yMinorTickSpacing = 0.25;
		ar.yMajorTickSpacing = 0.5;
		ar.yLabelSpacing = 0.5;
		ar.yTickLabelFont = new GeneralFont( "Arial", java.awt.Font.PLAIN );
		ar.yTickLabelSize = 14;
		ar.xAxisName = "Stuff";
		ar.xAxisNameFont = new GeneralFont( "Times New Roman", java.awt.Font.PLAIN );
		ar.xAxisNameSize = 25;
		ar.yAxisName = "Things";
		ar.yAxisNameFont = new GeneralFont( "Times New Roman", java.awt.Font.PLAIN );
		ar.yAxisNameSize = 25;

		ar.renderAxis( visImage );
		DisplayUtilities.display( visImage );
	}
}
