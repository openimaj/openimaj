/**
 * 
 */
package org.openimaj.demos;

import java.io.File;
import java.nio.ShortBuffer;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 * 	Utilises an audio processor to plot the audio waveform.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 9 Jun 2011
 */
public class AudioWaveformPlotter
{
	public static void main( String[] args )
    {
	    final XuggleAudio a = new XuggleAudio( 
	    //		new File( "src/test/resources/06041609-rttr-16k-news18-rttr-16k.mpg") );
	    		new File( "src/test/resources/fma.flv") );
	    // AudioPlayer ap = AudioPlayer.createAudioPlayer( a );
	    
	    final int w = 1000;
	    final int h = 400;
	    final int nSamplesPerPixel = 5000; // TODO: This is currently fixed
	    final int channelSize = h/a.getFormat().getNumChannels();
	    final double ampScalar = (double)channelSize / Short.MAX_VALUE / 2;
	    
	    System.out.println( "Samples per pixel: "+nSamplesPerPixel );
	    System.out.println( "Channel height: "+channelSize );
	    System.out.println( "Amplitude scalar: "+ampScalar );
	    
	    final MBFImage m = new MBFImage( w, h, 3 );
	    AudioProcessor aap = new AudioProcessor()
		{	
	    	private int offset = 0;
	    	private int nSampleToDraw = 0;
	    	
			@Override
			public SampleChunk process( SampleChunk samples )
			{
				if( offset > w ) return null;

				int nChans = a.getFormat().getNumChannels();
				int nSamples = samples.getSamples().length/nChans;
				
				// If the sample we need to draw is within this chunk...
				if( nSampleToDraw < nSamples )
				{
					ShortBuffer b = samples.getSamplesAsByteBuffer().asShortBuffer();
					
					int xx = 0;
					for( int x = nSampleToDraw; x < nSamples; x += nSamplesPerPixel  )
					{
						for( int c = 0; c < a.getFormat().getNumChannels(); c++ )
						{
								m.drawLine( 
									(int)(xx+offset), 
									(int)(channelSize*c+channelSize/2),
									(int)(xx+offset), 
									(int)(channelSize*c+channelSize/2 +
										b.get(x+c)*ampScalar), RGBColour.WHITE );
								m.drawPoint( new Point2dImpl( (int)(xx+offset), 
									(int)(channelSize*c+channelSize/2 +
										b.get(x+c)*ampScalar) ), RGBColour.RED, 1 );
						}
						
						xx++;
					}
					
					offset += xx;
					nSampleToDraw += nSamplesPerPixel;
				}
				else	nSampleToDraw -= nSamples;
				
				return samples;
			}
		};
		aap.process( a );
		DisplayUtilities.display( m );
    }
}
