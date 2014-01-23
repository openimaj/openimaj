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
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.image.typography.hershey.HersheyFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.general.BarVisualisation.Bar;

/**
 * The {@link BarVisualisation} can be used to draw to an image a bar graph of
 * any data set to an RGBA MBFImage.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 26 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class BarVisualisation extends XYPlotVisualisation<Bar>
		implements ItemPlotter<Bar, Float[], MBFImage>
{
	/**
	 * Represents a single bar to draw.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 6 Aug 2013
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class Bar
	{
		/** The value (height) of the bar */
		public double value;

		/** Data units where the bar is drawn */
		public double startX;

		/** Data units where the bar drawing ends */
		public double endX;

		/** The colour of the bar */
		public Float[] colour;

		/** The stroke colour */
		public Float[] strokeColour = RGBColour.BLACK;

		/**
		 * Default constructor
		 * 
		 * @param value
		 *            Height of the bar
		 * @param startX
		 *            units of the start of the bar
		 * @param endX
		 *            units at the end of the bar
		 * @param colour
		 *            The colour of the bar
		 */
		public Bar(final double value, final double startX, final double endX, final Float[] colour)
		{
			this.value = value;
			this.startX = startX;
			this.endX = endX;
			this.colour = colour;
		}

		@Override
		public String toString()
		{
			return "Bar[" + this.startX + " to " + this.endX + " = " + this.value + "]";
		}
	}

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
	private int textBasePad = 4;

	/** Whether to auto scale the vertical axis */
	private boolean autoScale = true;

	/** The maximum value of the scale (if autoScale is false) */
	// private double maxValue = 1d;

	/** The minimum value of the scale (if autoScale if false) */
	// private double minValue = 0d;

	/** Whether to draw the value of the bar in each bar */
	private boolean drawValue = false;

	/** Whether to use individual colours for each bar */
	private final boolean useIndividualBarColours = false;

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
	private final double yscale = 0;

	/** The range of the data being viewed */
	// private final double axisRangeY = 0;

	/**
	 * Whether to use a fixed bar width. If so barWidth gives the size in data
	 * units
	 */
	private boolean useFixedBarWidth = true;

	/** The width of each bar that's drawn */
	private double barWidth = 1;

	/**
	 * Whether to centre the bars on the values (rather than between the values)
	 */
	private boolean centreBarsOnValues = false;

	/** The label transformer used for the data point values */
	private LabelTransformer transformer = null;

	private StrokeColourProvider<Float[]> strokeColourProvider = new StrokeColourProvider<Float[]>() {

		@Override
		public Float[] getStrokeColour(final int row) {
			return BarVisualisation.this.strokeColour;
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
	public BarVisualisation(final int width, final int height)
	{
		super(width, height);
		super.setItemPlotter(this);
		super.setRenderAxesLast(false);
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
	public BarVisualisation(final int width, final int height, final double[] data)
	{
		this(width, height);
		this.setData(data);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.general.ItemPlotter#renderRestarting()
	 */
	@Override
	public void renderRestarting()
	{
	}

	/**
	 * Plots a single bar into the visualisation.
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.general.ItemPlotter#plotObject(org.openimaj.image.Image,
	 *      org.openimaj.vis.general.XYPlotVisualisation.LocatedObject,
	 *      org.openimaj.vis.general.AxesRenderer2D)
	 */
	@Override
	public void plotObject(final MBFImage visImage, final LocatedObject<Bar> object,
			final AxesRenderer2D<Float[], MBFImage> renderer)
	{
		// Position on the x-axis
		final int[] p = this.axesRenderer2D.calculatePosition(new double[] { object.x, object.y });

		// The position of the x axis and the start of the bar
		final int[] z = this.axesRenderer2D.calculatePosition(new double[] { object.object.startX, 0 });

		// The position of the end of the bar (y is ignored)
		final int[] p2 = this.axesRenderer2D.calculatePosition(new double[] { object.object.endX, 0 });

		// The width of the bar in pixels
		final int barWidth = p2[0] - z[0];

		// The height of the bar
		int barHeight = z[1] - p[1];
		int y = p[1];

		if (barHeight < 0) {
			barHeight = -barHeight;
			y = z[1];
		}

		// The rectangle delimiting the bar
		final Rectangle rect = new Rectangle(p[0], y, barWidth, barHeight);

		// Work out what colour it should be
		Float[] c = this.getBarColour();
		if (this.useColourMap)
			c = this.colourMap.apply((float) (Math.abs(object.y) / this.axesRenderer2D.getMaxYValue()));
		if (this.useIndividualBarColours)
			c = object.object.colour;

		// Draw the bar
		visImage.drawShapeFilled(rect, c);

		// Stroke the bar
		if (object.object.strokeColour != null)
			visImage.drawShape(rect, object.object.strokeColour);

		// If we're to draw the value, do that here.
		if (this.drawValue)
		{
			// We'll draw the bar's value
			String text;
			if (this.transformer != null)
				text = this.transformer.transform(object.y);
			else
				text = "" + object.y;

			// Find the width and height of the text to draw
			final HersheyFont f = HersheyFont.TIMES_BOLD;
			final HersheyFontStyle<Float[]> style = f.createStyle(this.visImage.createRenderer());
			style.setFontSize(this.textSize);
			final Rectangle r = f.createStyle(this.visImage.createRenderer())
					.getRenderer(this.visImage.createRenderer())
					.getSize(text, style);

			// Work out where to put the text
			// tx is the centre of the bar minus half the text bounds
			int tx = (int) (z[0] + barWidth / 2 - r.width / 2);

			// ty is the top of the bar minus a small padding
			final int ty = (int) ((object.y >= 0 ? rect.y : rect.y + rect.height + r.height) -
					(object.y >= 0 ? this.textBasePad : -this.textBasePad));

			// Make sure the text will be drawn within the bounds of the image.
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
		this.setData(ArrayUtils.convertToDouble(data));
	}

	/**
	 * Set the data from a long array.
	 * 
	 * @param data
	 *            The data to set
	 */
	public void setData(final long[] data)
	{
		this.setData(ArrayUtils.convertToDouble(data));
	}

	/**
	 * Set the data to a double array
	 * 
	 * @param data
	 *            The data
	 */
	public void setData(final double[] data)
	{
		super.data.clear();

		if (this.useFixedBarWidth)
			for (int i = 0; i < data.length; i++)
				super.data.add(new LocatedObject<Bar>(i, data[i],
						new Bar(data[i], i, i + this.barWidth, RGBColour.RED)));
		else
			for (int i = 0; i < data.length; i++)
				super.data.add(new LocatedObject<Bar>(i, data[i],
						new Bar(data[i], i, i + this.barWidth, RGBColour.RED)));

		super.validateData();
		this.axesRenderer2D.setMaxXValue(data.length);

		// Force the axis to be zero if the axis always visible flag is set
		if (axisAlwaysVisible)
			if (getMinValue() > 0)
				setMinValue(0);
			else if (getMaxValue() < 0)
				setMaxValue(0);

		super.updateVis();
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
	 * @param row
	 *            the row
	 * @return the strokeColour
	 */
	public Float[] getStrokeColour(final int row)
	{
		return this.strokeColourProvider.getStrokeColour(row);
	}

	/**
	 * @param prov
	 */
	public void setStrokeProvider(final StrokeColourProvider<Float[]> prov) {
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
		return axesRenderer2D.getyAxisConfig().getMaxValue();
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
		axesRenderer2D.getyAxisConfig().setMaxValue(maxValue);
		this.autoScale = false;
	}

	/**
	 * Get the minimum value in use.
	 * 
	 * @return The minimum value
	 */
	public double getMinValue()
	{
		return axesRenderer2D.getyAxisConfig().getMinValue();
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
		axesRenderer2D.getyAxisConfig().setMinValue(minValue);
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
		return axesRenderer2D.getyAxisRenderer().calculatePosition(0d)[1];
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
		return axesRenderer2D.getyAxisRenderer().scaleDimension(1d)[0];
	}

	/**
	 * The data range being displayed.
	 * 
	 * @return the axisRangeY
	 */
	public double getAxisRangeY()
	{
		return axesRenderer2D.getyAxisConfig().getMaxValue() -
				axesRenderer2D.getyAxisConfig().getMinValue();
	}

	/**
	 * The data range being displayed.
	 * 
	 * @return the axisRangeX
	 */
	public double getAxisRangeX()
	{
		return axesRenderer2D.getxAxisConfig().getMaxValue() -
				axesRenderer2D.getxAxisConfig().getMinValue();
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
		return new Point2dImpl(x * this.data.size() / this.getWidth(),
				(float) ((this.axisLocation - y) / this.yscale));
	}

	/**
	 * @return the barWidth
	 */
	public double getBarWidth()
	{
		return this.barWidth;
	}

	/**
	 * @param barWidth
	 *            the barWidth to set
	 */
	public void setBarWidth(final double barWidth)
	{
		this.barWidth = barWidth;
	}

	/**
	 * @return the useFixedBarWidth
	 */
	public boolean isUseFixedBarWidth()
	{
		return this.useFixedBarWidth;
	}

	/**
	 * @param useFixedBarWidth
	 *            the useFixedBarWidth to set
	 */
	public void setUseFixedBarWidth(final boolean useFixedBarWidth)
	{
		this.useFixedBarWidth = useFixedBarWidth;
	}

	/**
	 * @return the centreBarsOnValues
	 */
	public boolean isCentreBarsOnValues()
	{
		return this.centreBarsOnValues;
	}

	/**
	 * @param centreBarsOnValues
	 *            the centreBarsOnValues to set
	 */
	public void setCentreBarsOnValues(final boolean centreBarsOnValues)
	{
		this.centreBarsOnValues = centreBarsOnValues;
	}

	/**
	 * @return the textBasePad
	 */
	public int getTextBasePad()
	{
		return this.textBasePad;
	}

	/**
	 * @param textBasePad
	 *            the textBasePad to set
	 */
	public void setTextBasePad(final int textBasePad)
	{
		this.textBasePad = textBasePad;
	}

	/**
	 * @return the transformer
	 */
	public LabelTransformer getTransformer()
	{
		return this.transformer;
	}

	/**
	 * @param transformer
	 *            the transformer to set
	 */
	public void setTransformer(final LabelTransformer transformer)
	{
		this.transformer = transformer;
	}

	/**
	 * Shows a basic bar visualisation.
	 * 
	 * @param args
	 *            The bar visualisation.
	 */
	public static void main(final String[] args)
	{
		final int nPoints = 10;

		final double[] data = new double[nPoints];
		for (int i = 0; i < nPoints; i++)
			data[i] = nPoints * (Math.random() * 2 - 1);

		final BarVisualisation bv = new BarVisualisation(1000, 600);
		bv.setDrawValues(true);
		bv.setData(data);
		bv.setTransformer(new LabelTransformer()
		{
			@Override
			public String transform(final double value)
			{
				return String.format("%2.2f", value);
			}
		});
		bv.showWindow("Bar Visualisation Demo");
	}
}
