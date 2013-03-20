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
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.Visualisation;

/**
 *	The {@link BarVisualisation} can be used to draw to an image a bar graph
 *	of any data set to an RGBA MBFImage.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class BarVisualisation extends Visualisation<double[]>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The colour of the background */
	private Float[] backgroundColour = new Float[]{0f,0f,0f,1f};

	/** The colour of the bar */
	private Float[] barColour = new Float[]{1f,0f,0f,1f};

	/** The colour to stroke the bar */
	private Float[] strokeColour = new Float[]{0f,0f,0f,1f};

	/** The colour of the text to draw */
	private Float[] textColour = new Float[]{1f,1f,1f,1f};

	/** The colour to stroke any text */
	private Float[] textStrokeColour = new Float[]{0f,0f,0f,1f};

	/** Colour of the axis */
	private Float[] axisColour = new Float[]{1f,1f,1f,1f};

	/** Width of the axis line */
	private int axisWidth = 1;

	/** Number of pixels to pad the base of the text */
	private final int textBasePad = 4;

	/** Whether to auto scale the vertical axis */
	private final boolean autoScale = true;

	/** The maximum value of the scale (if autoScale is false) */
	private final double maxValue = 1d;

	/** The minimum value of the scale (if autoScale if false) */
	private final double minValue = 0d;

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

	/** Whether to outline the text used to draw the values */
	private boolean outlineText = false;

	/** The size of the text to draw */
	private int textSize = 12;

	/** Whether to use a colour map or not */
	private boolean useColourMap = true;

	/** The colour map to use if useColourMap == true */
	private ColourMap colourMap = ColourMap.Autumn;

	/**
	 * 	Create a bar visualisation of the given size
	 *	@param width The width of the image
	 *	@param height The height of the image
	 */
	public BarVisualisation( final int width, final int height )
	{
		super( width, height );
	}

	/**
	 * 	Create a bar visualisation that will draw to the given
	 * 	image.
	 *
	 *	@param imageToDrawTo The image to draw to.
	 */
	public BarVisualisation( final MBFImage imageToDrawTo )
	{
		this.visImage = imageToDrawTo;
	}

	/**
	 * 	Overlay a bar visualisation on the given vis
	 *	@param v The visualisation to overlay
	 */
	public BarVisualisation( final Visualisation<?> v )
	{
		super( v );
	}

	/**
	 * 	Creates the given visualisation with the given data
	 *	@param width The width of the image
	 *	@param height The height of the image
	 *	@param data The data to visualise
	 */
	public BarVisualisation( final int width, final int height, final double[] data )
	{
		super( width, height );
		this.setData( data );
	}

	/**
	 * 	Plot the given data to the given image.
	 *	@param image The image to plot to
	 *	@param data The data to plot
	 */
	public static void plotBars( final MBFImage image, final double[] data )
	{
		new BarVisualisation( image ).plotBars( data );
	}

	/**
	 * 	Plot the given data to the bar image.
	 *	@param data The data to plot.
	 */
	public void plotBars( final double[] data )
	{
		// Set the background
		this.visImage.fill( this.getBackgroundColour() );

		final int w = this.visImage.getWidth();
		final int h = this.visImage.getHeight();

		// Find min and max values from the data
		double max = this.maxValue;
		if( this.autoScale )
			max = ArrayUtils.maxValue( data );
		double min = this.minValue;
		if( this.autoScale )
			min = ArrayUtils.minValue( data );

		// Find the maximum value that occurs on one or t'other
		// side of the main axis
		final double largestAxisValue = Math.max( Math.abs(max), Math.abs( min ) );

		// Work out the scalars for the values to fit within the window
		double yscale = (largestAxisValue == max ? this.axisLocation :
				(h-this.axisLocation) )/largestAxisValue;

		// The width of each of the bars
		final double xscale = w/(double)data.length;

		// Position of the axis - either fixed or moving to fit best
		if( !this.fixAxis )
				this.axisLocation = min * yscale;
		else	yscale = Math.min( (h-this.axisLocation)/min, (h-this.axisLocation)/max );

		// Now draw the bars
		for( int i = 0; i < data.length; i++ )
		{
			// Position on the x-axis
			final int x = (int)(i*xscale);

			// The value (negative as we're drawing from the bottom of the window)
			double s = -data[i] * yscale;

			// This is used to ensure we draw the rectangle from the top-left each time.
			double offset = 0;

			// Get the bar colour. We'll get the colour map colour if we're doing that,
			// or if we've fixed bar colours use those.
			Float[] c = this.getBarColour();
			if( this.useColourMap )
				c = this.colourMap.apply( (float)(Math.abs(data[i]) / largestAxisValue) );
			if( this.useIndividualBarColours )
				c = this.barColours[i%this.barColours.length];

			// If we need to draw the rectangle below the axis, we need to draw
			// from a different position, so we update the offset and height of bar
			if( s < 0 )
			{
				offset = -s;
				s = -s;
			}

			// Draw the filled rectangle, and then stroke it.
			this.visImage.drawShapeFilled( new Rectangle( x, (int)(h-s+this.axisLocation+offset),
					(int)xscale, (int)s ), c );
			this.visImage.drawShape( new Rectangle( x, (int)(h-s+this.axisLocation+offset),
					(int)xscale, (int)s ), this.getStrokeColour() );

			// If we're to draw the bar's value, do that here.
			if( this.drawValue )
			{
				// We'll draw the bar's value
				final String text = ""+data[i];

				// Find the width and height of the text to draw
				final HersheyFont f = HersheyFont.TIMES_BOLD;
				final Rectangle r = f.createStyle( this.visImage.createRenderer() )
					.getRenderer( this.visImage.createRenderer() )
					.getBounds( text, f.createStyle( this.visImage.createRenderer() ) );

				// Work out where to put the text
				int tx = (int)(x+xscale/2-r.width/2);
				final int ty = (int)(h-s+this.axisLocation+offset)-this.textBasePad;

				// Make sure the text will be drawn within the bounds of the image.
				if( tx < 0 )
					tx = 0;
				if( tx + r.width > this.getWidth() )
					tx = this.getWidth() - (int)r.width;

				// Stroke the text, if necessary
				if( this.isOutlineText() )
				{
					this.visImage.drawText( text, tx-1, ty-1, f, this.textSize, this.getTextStrokeColour() );
					this.visImage.drawText( text, tx+1, ty-1, f, this.textSize, this.getTextStrokeColour() );
					this.visImage.drawText( text, tx-1, ty+1, f, this.textSize, this.getTextStrokeColour() );
					this.visImage.drawText( text, tx+1, ty+1, f, this.textSize, this.getTextStrokeColour() );
				}

				// Fill the text
				this.visImage.drawText( text, tx, ty, f, this.textSize, this.getTextColour() );
			}
		}

		// Finally, draw the axis on top of everything.
		if( this.drawAxis )
		{
			this.visImage.drawLine( 0, (int)(h+this.axisLocation),
					this.getWidth(), (int)(h+this.axisLocation),
					this.axisWidth, this.getAxisColour() );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.Visualisation#update()
	 */
	@Override
	public void update()
	{
		if( this.data != null )
			this.plotBars( this.data );
	}

	/**
	 * 	Set the colours to use for each bar.
	 * 	@param colours The colours to use.
	 */
	public void setInvidiualBarColours( final Float[][] colours )
	{
		this.barColours = colours;
		this.useIndividualBarColours = true;
	}

	/**
	 * 	Sets whether values are drawn to the image.
	 *	@param tf TRUE to draw values
	 */
	public void setDrawValues( final boolean tf )
	{
		this.drawValue = tf;
	}

	/**
	 * 	Set the data from a float array.
	 *	@param data The data to set
	 */
	public void setData( final float[] data )
	{
		super.setData( ArrayUtils.floatToDouble( data ) );
	}

	/**
	 * 	Set the data from a long array.
	 *	@param data The data to set
	 */
	public void setData( final long[] data )
	{
		super.setData( ArrayUtils.longToDouble( data ) );
	}

	/**
	 * 	Fix the x-axis to the given position in pixels. Note that the
	 * 	position is given from the bottom of the visualisation window.
	 *
	 *	@param position The position in pixels
	 */
	public void fixAxis( final int position )
	{
		this.axisLocation = -position;
		this.fixAxis = true;
	}

	/**
	 * 	Allow the x-axis to move as best to fit the data
	 */
	public void floatAxis()
	{
		this.fixAxis = false;
	}

	/**
	 *	@return the outlineText
	 */
	public boolean isOutlineText()
	{
		return this.outlineText;
	}

	/**
	 *	@param outlineText the outlineText to set
	 */
	public void setOutlineText( final boolean outlineText )
	{
		this.outlineText = outlineText;
	}

	/**
	 *	@return the textSize
	 */
	public int getTextSize()
	{
		return this.textSize;
	}

	/**
	 *	@param textSize the textSize to set
	 */
	public void setTextSize( final int textSize )
	{
		this.textSize = textSize;
	}

	/**
	 * 	Whether to use a colour map and which one.
	 *	@param cp The colour map to use
	 */
	public void useColourMap( final ColourMap cp )
	{
		this.colourMap = cp;
		this.useColourMap = true;
	}

	/**
	 * 	Revert back to using a static colour rather than
	 * 	a colour map;
	 */
	public void useStaticColour()
	{
		this.useColourMap = false;
	}

	/**
	 *	@return the barColour
	 */
	public Float[] getBarColour()
	{
		return this.barColour;
	}

	/**
	 *	@return the strokeColour
	 */
	public Float[] getStrokeColour()
	{
		return this.strokeColour;
	}

	/**
	 *	@return the textColour
	 */
	public Float[] getTextColour()
	{
		return this.textColour;
	}

	/**
	 *	@return the textStrokeColour
	 */
	public Float[] getTextStrokeColour()
	{
		return this.textStrokeColour;
	}

	/**
	 *	@return the backgroundColour
	 */
	public Float[] getBackgroundColour()
	{
		return this.backgroundColour;
	}

	/**
	 *	@param backgroundColour the backgroundColour to set
	 */
	public void setBackgroundColour( final Float[] backgroundColour )
	{
		this.backgroundColour = backgroundColour;
	}

	/**
	 *	@param barColour the barColour to set
	 */
	public void setBarColour( final Float[] barColour )
	{
		this.barColour = barColour;
		this.useColourMap = false;
	}

	/**
	 *	@param strokeColour the strokeColour to set
	 */
	public void setStrokeColour( final Float[] strokeColour )
	{
		this.strokeColour = strokeColour;
	}

	/**
	 *	@param textColour the textColour to set
	 */
	public void setTextColour( final Float[] textColour )
	{
		this.textColour = textColour;
	}

	/**
	 *	@param textStrokeColour the textStrokeColour to set
	 */
	public void setTextStrokeColour( final Float[] textStrokeColour )
	{
		this.textStrokeColour = textStrokeColour;
	}

	/**
	 *	@return the axisColour
	 */
	public Float[] getAxisColour()
	{
		return this.axisColour;
	}

	/**
	 *	@param axisColour the axisColour to set
	 */
	public void setAxisColour( final Float[] axisColour )
	{
		this.axisColour = axisColour;
	}

	/**
	 * 	Get the width of the axis being drawn
	 *	@return The axis width
	 */
	public int getAxisWidth()
	{
		return this.axisWidth;
	}

	/**
	 * 	Set the axis width
	 *	@param axisWidth The new axis width
	 */
	public void setAxisWidth( final int axisWidth )
	{
		this.axisWidth = axisWidth;
	}
}
