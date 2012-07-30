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
	private Float[] backgroundColour = new Float[]{0f,0f,0f,0.75f};
	
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
	private int textBasePad = 4;

	/** The font to draw any text in */
	private GeneralFont font = new GeneralFont( "Arial", Font.BOLD, 30 );
	
	/** Whether to auto scale the vertical axis */
	private boolean autoScale = true;
	
	/** The maximum value of the scale (if autoScale is false) */
	private double maxValue = 1d;
	
	/** The minimum value of the scale (if autoScale if false) */
	private double minValue = 0d;
	
	/** Whether to draw the value of the bar in each bar */
	private boolean drawValue = true;
	
	/** Whether to use individual colours for each bar */ 
	private boolean useIndividualBarColours = false;
	
	/** The colours of the bars is useIndividualBarColours is true */
	private Float[][] barColours = null;
	
	/** Whether to draw the main axis */
	private boolean drawAxis = true;
	
	/**
	 * 	Create a bar visualisation of the given size
	 *	@param width The width of the image
	 *	@param height The height of the image
	 */
	public BarVisualisation( int width, int height )
	{
		super( width, height );
	}
	
	/**
	 * 	Create a bar visualisation that will draw to the given
	 * 	image.
	 * 
	 *	@param imageToDrawTo The image to draw to.
	 */
	public BarVisualisation( MBFImage imageToDrawTo )
	{
		super( imageToDrawTo );
	}
	
	/**
	 * 	Plot the given data to the given image.
	 *	@param image The image to plot to
	 *	@param data The data to plot
	 */
	public static void plotBars( MBFImage image, double[] data )
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
		visImage.fill( backgroundColour );
		
		final int w = visImage.getWidth();
		final int h = visImage.getHeight();

		// Find min and max
		double max = maxValue;
		if( autoScale )
			max = ArrayUtils.maxValue( data );
		double min = minValue;
		if( autoScale )
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
			Float[] c = barColour;
			
			if( s < 0 )
			{
				offset = -s;
				s = Math.abs( s );
			}
			
			if( useIndividualBarColours )
				c = barColours[i%barColours.length];
			
			visImage.drawShapeFilled( new Rectangle( x, (int)(h-s+z+offset), 
					(int)xscale, (int)s ), c );
			visImage.drawShape( new Rectangle( x, (int)(h-s+z+offset), 
					(int)xscale, (int)s ), strokeColour );
			
			if( drawValue )
			{
				String text = ""+data[i];

				// Work out the text size
				GeneralFontStyle<Float[]> fs = new GeneralFontStyle<Float[]>( 
						font, visImage.createRenderer(), false );
				Rectangle r = new GeneralFontRenderer<Float[]>().getBounds( text, fs );
				
				// Work out where to put the text
				int tx = (int)(x+xscale/2-r.width/2);
				int ty = (int)(h-s+z+offset)-textBasePad;
				if( ty - r.height < 0 )
					ty = (int)r.height + textBasePad;
				
				// Fill the text
				fs.setColour( textColour );
				visImage.drawText( text, tx, ty, fs );

				// Stroke the text
				fs.setOutline( true );
				fs.setColour( textStrokeColour );
				visImage.drawText( text, tx, ty, fs );
			}
		}
		
		if( drawAxis )
		{
			visImage.drawLine( 0, (int)(h+z), getWidth(), (int)(h+z), axisWidth, axisColour );
		}
		
		repaint();
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
	public void setInvidiualBarColours( Float[][] colours )
	{
		this.barColours = colours;
		this.useIndividualBarColours = true;
	}
	
	/**
	 * 	Sets whether values are drawn to the image.
	 *	@param tf TRUE to draw values
	 */
	public void setDrawValues( boolean tf )
	{
		this.drawValue = tf;
	}
}
 