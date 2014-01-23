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
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.VisualisationImpl;

/**
 * The basic bar visualisation that can be used to draw a bar graph of
 * any data set to an RGBA MBFImage.  This basic bar graph does not provide
 * any axes or controlled rendering. If you want that, use the {@link BarVisualisation}
 * which is more controllable. This one might be quicker as there's less render
 * hierarchy.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 26 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class BarVisualisationBasic extends VisualisationImpl<double[]>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The colour of the background */
	private Float[] backgroundColour = new Float[]
	{ 0f, 0f, 0f, 1f };

	/** The colour of the bar */
	private Float[] barColour = new Float[]
	{ 1f, 0f, 0f, 1f };

	/** The colour to stroke the bar */
	private Float[] strokeColour = new Float[]
	{ 0f, 0f, 0f, 1f };

	/** The colour of the text to draw */
	private Float[] textColour = new Float[]
	{ 1f, 1f, 1f, 1f };

	/** The colour to stroke any text */
	private Float[] textStrokeColour = new Float[]
	{ 0f, 0f, 0f, 1f };

	/** Colour of the axis */
	private Float[] axisColour = new Float[]
	{ 1f, 1f, 1f, 1f };

	/** Width of the axis line */
	private int axisWidth = 1;

	/** Number of pixels to pad the base of the text */
	private final int textBasePad = 4;

	/** Whether to auto scale the vertical axis */
	private boolean autoScale = true;

	/** The maximum value of the scale (if autoScale is false) */
	private double maxValue = 1d;

	/** The minimum value of the scale (if autoScale if false) */
	private double minValue = 0d;

	/** Whether to draw the value of the bar in each bar */
	private boolean drawValue = false;

	/** Whether to use individual colours for each bar */
	private boolean useIndividualBarColours = false;

	/** The colours of the bars is useIndividualBarColours is true */
	private Float[][] barColours = null;

	/** Whether to draw the main axis */
	private final boolean drawAxis = true;

	/** Whether or not to fix the axis */
	private boolean fixAxis = false;

	/** The location of the fixed axis, if it is to be fixed */
	private double axisLocation = 100;

	/**
	 * If the minimum value > 0 (or the max < 0), then whether the make the axis
	 * visible
	 */
	private boolean axisAlwaysVisible = true;

	/** Whether to outline the text used to draw the values */
	private boolean outlineText = false;

	/** The size of the text to draw */
	private int textSize = 12;

	/** Whether to use a colour map or not */
	private boolean useColourMap = true;

	/** The colour map to use if useColourMap == true */
	private ColourMap colourMap = ColourMap.Autumn;

	/** The scalar being used to plot the data */
	private double yscale = 0;

	/** The range of the data being viewed */
	private double axisRangeY = 0;

	private double dataRange;

	private StrokeColourProvider<Float[]> strokeColourProvider = new StrokeColourProvider<Float[]>() {

		@Override
		public Float[] getStrokeColour(final int row) {
			return BarVisualisationBasic.this.strokeColour;
		}
	};

	/**
	 * Create a bar visualisation of the given size
	 *
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 */
	public BarVisualisationBasic(final int width, final int height)
	{
		super(width, height);
	}

	/**
	 * Create a bar visualisation that will draw to the given image.
	 *
	 * @param imageToDrawTo
	 *            The image to draw to.
	 */
	public BarVisualisationBasic(final MBFImage imageToDrawTo)
	{
		this.visImage = imageToDrawTo;
	}

	/**
	 * Overlay a bar visualisation on the given vis
	 *
	 * @param v
	 *            The visualisation to overlay
	 */
	public BarVisualisationBasic(final VisualisationImpl<?> v)
	{
		super(v);
	}

	/**
	 * Creates the given visualisation with the given data
	 *
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 * @param data
	 *            The data to visualise
	 */
	public BarVisualisationBasic(final int width, final int height, final double[] data)
	{
		super(width, height);
		this.setData(data);
	}

	/**
	 * Plot the given data to the given image.
	 *
	 * @param image
	 *            The image to plot to
	 * @param data
	 *            The data to plot
	 */
	public static void plotBars(final MBFImage image, final double[] data)
	{
		new BarVisualisationBasic(image).plotBars(data);
	}

	/**
	 * Plot the given data to the bar image.
	 *
	 * @param data
	 *            The data to plot.
	 */
	public void plotBars(final double[] data)
	{
		// Set the background
		this.visImage.fill(this.getBackgroundColour());

		final int w = this.visImage.getWidth();
		final int h = this.visImage.getHeight();

		synchronized (data)
		{
			// Find min and max values from the data
			double max = this.maxValue;
			if (this.autoScale)
				max = ArrayUtils.maxValue(data);
			double min = this.minValue;
			if (this.autoScale)
				min = ArrayUtils.minValue(data);

			// Find the maximum value that occurs on one or t'other
			// side of the main axis
			final double largestAxisValue = Math.max(Math.abs(max), Math.abs(min));

			// The range displayed on the axis.
			this.dataRange = max - min;

			// Work out the scalars for the values to fit within the window - in
			// pixels per unit
			this.yscale = h / this.dataRange;
			if (this.fixAxis)
			{
				// Recalculate the yscale to fit the fixed axis
				this.yscale = Math.min((h - this.axisLocation) / Math.abs(min), this.axisLocation / Math.abs(max));
			}
			// Position of the axis - if it's fixed we need to alter
			// the yscale or we calculate where it fits.
			else
			{
				this.axisLocation = h + min * this.yscale;

				if (this.axisAlwaysVisible && this.axisLocation < 0)
				{
					this.axisLocation = 0;
					this.yscale = h / Math.abs(min);
				}
				else if (this.axisAlwaysVisible && this.axisLocation > h)
				{
					this.axisLocation = h;
					this.yscale = h / max;
				}
			}

			// Calculate the visible axis range
			this.axisRangeY = this.getValueAt(0, 0).getY() - this.getValueAt(0, h).getY();

			// The width of each of the bars
			final double barWidth = w / (double) data.length;

			// Now draw the bars
			synchronized (this.visImage)
			{
				for (int i = 0; i < data.length; i++)
				{
					// Position on the x-axis
					final int x = (int) (i * barWidth);

					// The size of the bar (negative as we're drawing from the
					// bottom of the window)
					double barHeight = -data[i] * this.yscale;

					// This is used to ensure we draw the rectangle from its
					// top-left each time.
					double offset = 0;

					// Get the bar colour. We'll get the colour map colour if
					// we're
					// doing that,
					// or if we've fixed bar colours use those.
					Float[] c = this.getBarColour();
					if (this.useColourMap)
						c = this.colourMap.apply((float) (Math.abs(data[i]) / largestAxisValue));
					if (this.useIndividualBarColours)
						c = this.barColours[i % this.barColours.length];

					// If we need to draw the rectangle above the axis (a
					// positive
					// value
					// makes barHeight negative), we need to draw from above the
					// axis,
					// down to the axis.
					if (barHeight < 0)
						barHeight = offset = -barHeight;

					// Create the shape for the bar
					final int rectPosition = (int) (this.axisLocation - offset);
					final Rectangle barRect = new Rectangle(x, rectPosition, (int) barWidth, (int) barHeight);

					// Draw the filled rectangle, and then stroke it.
					this.visImage.drawShapeFilled(barRect, c);

					if (barWidth > 3)
						this.visImage.drawShape(barRect, this.getStrokeColour(i));

					// If we're to draw the bar's value, do that here.
					if (this.drawValue)
					{
						// We'll draw the bar's value
						final String text = "" + data[i];

						// Find the width and height of the text to draw
						final HersheyFont f = HersheyFont.TIMES_BOLD;
						final Rectangle r = f.createStyle(this.visImage.createRenderer()).getRenderer(
								this.visImage.createRenderer())
								.getSize(text, f.createStyle(this.visImage.createRenderer()));

						// Work out where to put the text
						int tx = (int) (x + barWidth / 2 - r.width / 2);
						final int ty = (int) (this.axisLocation - offset) - this.textBasePad;

						// Make sure the text will be drawn within the bounds of
						// the
						// image.
						if (tx < 0)
							tx = 0;
						if (tx + r.width > this.getWidth())
							tx = this.getWidth() - (int) r.width;

						// Stroke the text, if necessary
						if (this.isOutlineText())
						{
							this.visImage.drawText(text, tx - 1, ty - 1, f, this.textSize, this.getTextStrokeColour());
							this.visImage.drawText(text, tx + 1, ty - 1, f, this.textSize, this.getTextStrokeColour());
							this.visImage.drawText(text, tx - 1, ty + 1, f, this.textSize, this.getTextStrokeColour());
							this.visImage.drawText(text, tx + 1, ty + 1, f, this.textSize, this.getTextStrokeColour());
						}

						// Fill the text
						this.visImage.drawText(text, tx, ty, f, this.textSize, this.getTextColour());
					}
				}

				// Finally, draw the axis on top of everything.
				if (this.drawAxis)
				{
					this.visImage.drawLine(0, (int) (h + this.axisLocation), this.getWidth(),
							(int) (h + this.axisLocation), this.axisWidth, this.getAxisColour());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.VisualisationImpl#update()
	 */
	@Override
	public void update()
	{
		if (this.data != null)
			this.plotBars(this.data);
	}

	/**
	 * Set the colours to use for each bar.
	 *
	 * @param colours
	 *            The colours to use.
	 */
	public void setInvidiualBarColours(final Float[][] colours)
	{
		this.barColours = colours;
		this.useIndividualBarColours = true;
	}

	/**
	 * Sets whether values are drawn to the image.
	 *
	 * @param tf
	 *            TRUE to draw values
	 */
	public void setDrawValues(final boolean tf)
	{
		this.drawValue = tf;
	}

	/**
	 * Set the data from a float array.
	 *
	 * @param data
	 *            The data to set
	 */
	public void setData(final float[] data)
	{
		super.setData(ArrayUtils.convertToDouble(data));
	}

	/**
	 * Set the data from a long array.
	 *
	 * @param data
	 *            The data to set
	 */
	public void setData(final long[] data)
	{
		super.setData(ArrayUtils.convertToDouble(data));
	}

	/**
	 * Fix the x-axis to the given position in pixels. Note that the position is
	 * given from the bottom of the visualisation window.
	 *
	 * @param position
	 *            The position in pixels
	 */
	public void fixAxis(final int position)
	{
		this.axisLocation = -position;
		this.fixAxis = true;
	}

	/**
	 * Allow the x-axis to move as best to fit the data
	 */
	public void floatAxis()
	{
		this.fixAxis = false;
	}

	/**
	 * @return the outlineText
	 */
	public boolean isOutlineText()
	{
		return this.outlineText;
	}

	/**
	 * @param outlineText
	 *            the outlineText to set
	 */
	public void setOutlineText(final boolean outlineText)
	{
		this.outlineText = outlineText;
	}

	/**
	 * @return the textSize
	 */
	public int getTextSize()
	{
		return this.textSize;
	}

	/**
	 * @param textSize
	 *            the textSize to set
	 */
	public void setTextSize(final int textSize)
	{
		this.textSize = textSize;
	}

	/**
	 * Whether to use a colour map and which one.
	 *
	 * @param cp
	 *            The colour map to use
	 */
	public void useColourMap(final ColourMap cp)
	{
		this.colourMap = cp;
		this.useColourMap = true;
	}

	/**
	 * Revert back to using a static colour rather than a colour map;
	 */
	public void useStaticColour()
	{
		this.useColourMap = false;
	}

	/**
	 * @return the barColour
	 */
	public Float[] getBarColour()
	{
		return this.barColour;
	}

	/**
	 * @param row the row
	 * @return the strokeColour
	 */
	public Float[] getStrokeColour(final int row)
	{
		return this.strokeColourProvider.getStrokeColour(row);
	}

	/**
	 * @param prov
	 */
	public void setStrokeProvider(final StrokeColourProvider<Float[]> prov){
		this.strokeColourProvider = prov;
	}

	/**
	 * @return the textColour
	 */
	public Float[] getTextColour()
	{
		return this.textColour;
	}

	/**
	 * @return the textStrokeColour
	 */
	public Float[] getTextStrokeColour()
	{
		return this.textStrokeColour;
	}

	/**
	 * @return the backgroundColour
	 */
	public Float[] getBackgroundColour()
	{
		return this.backgroundColour;
	}

	/**
	 * @param backgroundColour
	 *            the backgroundColour to set
	 */
	public void setBackgroundColour(final Float[] backgroundColour)
	{
		this.backgroundColour = backgroundColour;
	}

	/**
	 * @param barColour
	 *            the barColour to set
	 */
	public void setBarColour(final Float[] barColour)
	{
		this.barColour = barColour;
		this.useColourMap = false;
	}

	/**
	 * @param strokeColour
	 *            the strokeColour to set
	 */
	public void setStrokeColour(final Float[] strokeColour)
	{
		this.strokeColour = strokeColour;
	}

	/**
	 * @param textColour
	 *            the textColour to set
	 */
	public void setTextColour(final Float[] textColour)
	{
		this.textColour = textColour;
	}

	/**
	 * @param textStrokeColour
	 *            the textStrokeColour to set
	 */
	public void setTextStrokeColour(final Float[] textStrokeColour)
	{
		this.textStrokeColour = textStrokeColour;
	}

	/**
	 * @return the axisColour
	 */
	public Float[] getAxisColour()
	{
		return this.axisColour;
	}

	/**
	 * @param axisColour
	 *            the axisColour to set
	 */
	public void setAxisColour(final Float[] axisColour)
	{
		this.axisColour = axisColour;
	}

	/**
	 * Get the width of the axis being drawn
	 *
	 * @return The axis width
	 */
	public int getAxisWidth()
	{
		return this.axisWidth;
	}

	/**
	 * Set the axis width
	 *
	 * @param axisWidth
	 *            The new axis width
	 */
	public void setAxisWidth(final int axisWidth)
	{
		this.axisWidth = axisWidth;
	}

	/**
	 * Returns whether the bars are auto scaling
	 *
	 * @return TRUE if auto scaling
	 */
	public boolean isAutoScale()
	{
		return this.autoScale;
	}

	/**
	 * Set whether the bars should auto scale to fit all values within the vis.
	 *
	 * @param autoScale
	 *            TRUE to auto scale the values
	 */
	public void setAutoScale(final boolean autoScale)
	{
		this.autoScale = autoScale;
	}

	/**
	 * Get the maximum value for the scaling
	 *
	 * @return The maximum value
	 */
	public double getMaxValue()
	{
		return this.maxValue;
	}

	/**
	 * Set the maximum value (in units) for the bars. Automatically sets the
	 * autoScaling to FALSE.
	 *
	 * @param maxValue
	 *            Set the maximum value to use
	 */
	public void setMaxValue(final double maxValue)
	{
		this.maxValue = maxValue;
		this.autoScale = false;
	}

	/**
	 * Get the minimum value in use.
	 *
	 * @return The minimum value
	 */
	public double getMinValue()
	{
		return this.minValue;
	}

	/**
	 * Set the minimum value (in units) to use to plot the bars. Automatically
	 * sets the auto scaling to FALSE.
	 *
	 * @param minValue
	 *            the minimum value
	 */
	public void setMinValue(final double minValue)
	{
		this.minValue = minValue;
		this.autoScale = false;
	}

	/**
	 * Whether the axis is always visible
	 *
	 * @return TRUE if the axis is always visible
	 */
	public boolean isAxisAlwaysVisible()
	{
		return this.axisAlwaysVisible;
	}

	/**
	 * Set whether the axis should always be visible. If the minimum value is >
	 * 0 or maximum value < 0, then the axis will be made visible (either at the
	 * bottom or the top of the viewport respectively) if this is TRUE. This has
	 * no effect if the axis is fixed and set to a point outside the viewport.
	 *
	 * @param axisAlwaysVisible
	 *            TRUE to make the axis always visible
	 */
	public void setAxisAlwaysVisible(final boolean axisAlwaysVisible)
	{
		this.axisAlwaysVisible = axisAlwaysVisible;
	}

	/**
	 * Returns the last calculated axis location
	 *
	 * @return the axisLocation The axis location
	 */
	public double getAxisLocation()
	{
		return this.axisLocation;
	}

	/**
	 * Set the axis location. Automatically fixes the axis location
	 *
	 * @param axisLocation
	 *            the axisLocation to set
	 */
	public void setAxisLocation(final double axisLocation)
	{
		this.axisLocation = axisLocation;
		this.fixAxis = true;
	}

	/**
	 * Returns whether the axis is fixed or not.
	 *
	 * @return the fixAxis TRUE if the axis is fixed; FALSE otherwise
	 */
	public boolean isFixAxis()
	{
		return this.fixAxis;
	}

	/**
	 * Set whether the axis should be fixed.
	 *
	 * @param fixAxis
	 *            TRUE to fix the axis; FALSE to allow it to float
	 */
	public void setFixAxis(final boolean fixAxis)
	{
		this.fixAxis = fixAxis;
	}

	/**
	 * The y-scale being used to plot the data.
	 *
	 * @return the yscale The y-scale
	 */
	public double getYscale()
	{
		return this.yscale;
	}

	/**
	 * The data range being displayed.
	 *
	 * @return the axisRangeY
	 */
	public double getAxisRangeY()
	{
		return this.axisRangeY;
	}

	/**
	 * Returns the units value at the given pixel coordinate.
	 *
	 * @param x
	 *            The x pixel coordinate
	 * @param y
	 *            The y pixel coordinate
	 * @return The cartesian unit coordinate
	 */
	public Point2d getValueAt(final int x, final int y)
	{
		return new Point2dImpl(x * this.data.length / this.getWidth(), (float) ((this.axisLocation - y) / this.yscale));
	}

	/**
	 * 	Shows a basic bar visualisation.
	 *	@param args The bar visualisation.
	 */
	public static void main( final String[] args )
	{
		final int nPoints = 10;

		final double[] data = new double[nPoints];
		for( int i = 0; i < nPoints; i++ )
			data[i] = nPoints*(Math.random()*2-1);

		final BarVisualisationBasic bv = new BarVisualisationBasic( 1000, 600 );
		bv.setData( data );
		bv.showWindow( "Bar Visualisation Demo" );
	}
}
