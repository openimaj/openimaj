/**
 * 
 */
package org.openimaj.vis.audio;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.vis.Visualisation;

/**
 *	Implemented to draw short sample chunks to a window so that "live" display
 *	of audio waveform can be displayed.  If you prefer a display of the
 *	whole audio of a resource, use the {@link AudioWaveformPlotter}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioWaveform extends Visualisation<SampleChunk>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** Whether to have a decay on the waveform */
	private boolean decay = false;
	
	/** The decay amount if decay is set to true */
	private float decayAmount = 0.3f;
	
	/** The colour to draw the waveform */
	private Float[] colour = RGBColour.WHITE;
	
	/**
	 *	Create an audio waveform display of the given width and height 
	 *	@param w The width of the image
	 *	@param h The height of the image
	 */
	public AudioWaveform( int w, int h )
	{
		super( w, h );
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
				visImage.zero();
		else	visImage.multiplyInplace( decayAmount );
		
		// Get our interface to the samples
		SampleBuffer sb = s.getSampleBuffer();
		
		final float scalar = visImage.getHeight() / Integer.MAX_VALUE;
		final int yOffset = visImage.getHeight()/2;
		for( int i = 1; i < sb.size()/s.getFormat().getNumChannels(); i++ )
		{
			visImage.drawLine( 
				i-1, (int)(sb.get( (i-1)*s.getFormat().getNumChannels() )*scalar+yOffset), 
				  i, (int)(sb.get( i*s.getFormat().getNumChannels() )*scalar+yOffset), 
				  colour );
		}
		
		return visImage;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.Visualisation#update()
	 */
	@Override
	public void update()
	{
		this.drawWaveform( this.data );
	}
}
