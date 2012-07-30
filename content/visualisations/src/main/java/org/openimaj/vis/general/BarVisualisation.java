/**
 * 
 */
package org.openimaj.vis.general;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.vis.Visualisation;

/**
 *	The {@link BarVisualisation} can be used to draw to an image a bar graph
 *	of any data set.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class BarVisualisation extends Visualisation<float[]>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The colour of the bar */
	private Float[] barColour = RGBColour.RED;
	
	/** The colour to stroke the bar */
	private Float[] strokeColour = RGBColour.BLACK;
	
	/** Whether to auto scale the vertical axis */
	private boolean autoScale = false;
	
	/** The maximum value of the scale (if autoScale is false) */
	private double maxValue = 1d;
	
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
	public static void plotBars( MBFImage image, float[] data )
	{
		new BarVisualisation( image ).plotBars( data );
	}
	
	/**
	 * 	Plot the given data to the bar image.
	 *	@param data The data to plot.
	 */
	public void plotBars( final float[] data )
	{
		final int w = visImage.getWidth();
		final int h = visImage.getHeight();

		// Work out the maximum value
		double max = maxValue;
		if( autoScale )
			for( float f : data )
				if( f > max ) max = f;
		
		// Work out the scalars
		final double yscale = h/max;
		final double xscale = w/(double)data.length;
		
		for( int i = 0; i < data.length; i++ )
		{
			final double s = data[i] * yscale;
			final int x = (int)(i*xscale);
			visImage.drawShapeFilled( new Rectangle( x, (int)(h-s), 
					(int)xscale, (int)s ), barColour );
			visImage.drawShape( new Rectangle( x, (int)(h-s), 
					(int)xscale, (int)s ), strokeColour );
		}
		
		repaint();
	}

	@Override
	public void update()
	{
		if( this.data != null )
			this.plotBars( this.data );
	}
}
 