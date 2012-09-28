/**
 * 
 */
package org.openimaj.vis.general;

import java.awt.Font;

import org.openimaj.image.MBFImage;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.general.GeneralFontRenderer;
import org.openimaj.image.typography.general.GeneralFontStyle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.Visualisation;

/**
 *	The {@link BarVisualisation} can be used to draw to an image a bar graph
 *	of any data set.
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
	private final Float[] backgroundColour = new Float[]{0f,0f,0f,0.75f};

	/** The colour of the bar */
	private final Float[] barColour = new Float[]{1f,0f,0f,1f};

	/** The colour to stroke the bar */
	private final Float[] strokeColour = new Float[]{0f,0f,0f,1f};

	/** The colour of the text to draw */
	private final Float[] textColour = new Float[]{1f,1f,1f,1f};

	/** The colour to stroke any text */
	private final Float[] textStrokeColour = new Float[]{0f,0f,0f,1f};

	/** Colour of the axis */
	private final Float[] axisColour = new Float[]{1f,1f,1f,1f};

	/** Width of the axis line */
	private final int axisWidth = 1;

	/** Number of pixels to pad the base of the text */
	private final int textBasePad = 4;

	/** The font to draw any text in */
	private final GeneralFont font = new GeneralFont( "Arial", Font.BOLD );

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
		this.visImage.fill( this.backgroundColour );

		final int w = this.visImage.getWidth();
		final int h = this.visImage.getHeight();

		// Find min and max
		double max = this.maxValue;
		if( this.autoScale )
			max = ArrayUtils.maxValue( data );
		double min = this.minValue;
		if( this.autoScale )
			min = ArrayUtils.minValue( data );

		// Work out the scalars
		final double yscale = h/(max-min);
		final double xscale = w/(double)data.length;

		// Position of the axis
		final double z = min * yscale;

		for( int i = 0; i < data.length; i++ )
		{
			final int x = (int)(i*xscale);
			double s = data[i] * yscale;
			double offset = 0;
			Float[] c = this.barColour;

			if( s < 0 )
			{
				offset = -s;
				s = Math.abs( s );
			}

			if( this.useIndividualBarColours )
				c = this.barColours[i%this.barColours.length];

			this.visImage.drawShapeFilled( new Rectangle( x, (int)(h-s+z+offset),
					(int)xscale, (int)s ), c );
			this.visImage.drawShape( new Rectangle( x, (int)(h-s+z+offset),
					(int)xscale, (int)s ), this.strokeColour );

			if( this.drawValue )
			{
				final String text = ""+data[i];

				// Work out the text size
				final GeneralFontStyle<Float[]> fs = new GeneralFontStyle<Float[]>(
						this.font, this.visImage.createRenderer(), false );
				fs.setFontSize( 30 );
				final Rectangle r = new GeneralFontRenderer<Float[]>().getBounds( text, fs );

				// Work out where to put the text
				final int tx = (int)(x+xscale/2-r.width/2);
				int ty = (int)(h-s+z+offset)-this.textBasePad;
				if( ty - r.height < 0 )
					ty = (int)r.height + this.textBasePad;

				// Fill the text
				fs.setColour( this.textColour );
				this.visImage.drawText( text, tx, ty, fs );

				// Stroke the text
				fs.setOutline( true );
				fs.setColour( this.textStrokeColour );
				this.visImage.drawText( text, tx, ty, fs );
			}
		}

		if( this.drawAxis )
		{
			this.visImage.drawLine( 0, (int)(h+z), this.getWidth(), (int)(h+z), this.axisWidth, this.axisColour );
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
}
