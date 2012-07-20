/**
 * 
 */
package org.openimaj.vis.audio;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;

/**
 *	Implemented to draw short sample chunks to a window so that "live" display
 *	of audio waveform can be displayed.  If you prefer a display of the
 *	whole audio of a resource, use the {@link AudioWaveformPlotter}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioWaveform
{
	/** The image to which the waveform will be drawn */
	private MBFImage img = null;
	
	/** Whether to have a decay on the waveform */
	private boolean decay = false;
	
	/** The decay amount if decay is set to true */
	private float decayAmount = 0.3f;
	
	/** The title of the display window, if used */
	private String title = "Waveform";
	
	/** The colour to draw the waveform */
	private Float[] colour = RGBColour.WHITE;
	
	/**
	 * 
	 *	@param w
	 *	@param h
	 */
	public AudioWaveform( int w, int h )
	{
		img = new MBFImage( w, h, 3 );
	}
	
	/**
	 * 	Draw the given sample chunk into an image and returns that image.
	 * 	The image is reused, so if you want to keep it you must clone
	 * 	the image afterwards.
	 * 
	 * 	@param s The sample chunk to draw
	 * 	@return The image drawn
	 */
	public MBFImage drawWaveform( SampleChunk s )
	{
		// If decay is not set we simply wipe the image.
		if( !decay )
				img.zero();
		else	img.multiplyInplace( decayAmount );
		
		// Get our interface to the samples
		SampleBuffer sb = s.getSampleBuffer();
		
		final float scalar = img.getHeight() / Integer.MAX_VALUE;
		final int yOffset = img.getHeight()/2;
		for( int i = 1; i < sb.size()/s.getFormat().getNumChannels(); i++ )
		{
			img.drawLine( 
				i-1, (int)(sb.get( (i-1)*s.getFormat().getNumChannels() )*scalar+yOffset), 
				  i, (int)(sb.get( i*s.getFormat().getNumChannels() )*scalar+yOffset), 
				  colour );
		}
		
		return img;
	}
	
	/**
	 * 	Get the last drawn image.
	 *	@return The last drawn image.
	 */
	public MBFImage getWaveformImage()
	{
		return this.img;
	}
	
	/**
	 * 	Plots the waveform to an image and displays the image in a titled
	 * 	window.
	 * 
	 *	@param s The sample chunk to display
	 *	@return The waveform image being displayed
	 */
	public MBFImage displayWaveform( SampleChunk s )
	{		
		DisplayUtilities.displayName( drawWaveform( s ), title );
		return img;	
	}
}
