/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.File;

import org.openimaj.audio.AudioEventAdapter;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.conversion.BitDepthConverter;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.conversion.BitDepthConverter.BitDepthConversionAlgorithm;
import org.openimaj.audio.conversion.SampleRateConverter;
import org.openimaj.audio.conversion.SampleRateConverter.SampleRateConversionAlgorithm;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 20 Jun 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioConversionTest
{
	/**
	 *	@param args
	 */
	public static void main( String[] args )
	{
		final File f = new File( "videoplayback.3gp" );
		
		// ================================================================= //
		// Original Sample
		// ================================================================= //
		XuggleAudio x1 = new XuggleAudio( f );		
		System.out.println( "Input Audio Format: "+x1.getFormat() );
		
		System.out.println( "Playing original audio... "+x1.getFormat() );
		AudioPlayer ap = new AudioPlayer( x1 );
		ap.addAudioEventListener( new AudioEventAdapter()
		{
			@Override
			public void audioEnded()
			{
				
				// ================================================================= //
				// Sample rate conversion to 16KHz
				// ================================================================= //
				XuggleAudio x2 = new XuggleAudio( f );
				SampleRateConverter src = new SampleRateConverter( x2, 
					SampleRateConversionAlgorithm.LINEAR_INTERPOLATION,
					new AudioFormat( x2.getFormat().getNBits(), 11.025, 
							x2.getFormat().getNumChannels() ) );
				System.out.println( "Playing audio "+src.getFormat() );
				AudioPlayer ap2 = new AudioPlayer( src );				
				ap2.addAudioEventListener( new AudioEventAdapter()
				{
					@Override
					public void audioEnded() 
					{
						// ================================================================= //
						// Sample rate conversion to 16KHz
						// Bit depth conversion to 8bit
						// ================================================================= //
						XuggleAudio x3 = new XuggleAudio( f );
						SampleRateConverter src2 = new SampleRateConverter( x3, 
							SampleRateConversionAlgorithm.LINEAR_INTERPOLATION,
							new AudioFormat( x3.getFormat().getNBits(), 11.025, 
									x3.getFormat().getNumChannels() ) );
						BitDepthConverter bd1 = new BitDepthConverter( src2, 
							BitDepthConversionAlgorithm.NEAREST, 
							new AudioFormat( 8, src2.getFormat().getSampleRateKHz(),
								src2.getFormat().getNumChannels() ) );
						System.out.println( "Playing audio "+src2.getFormat() );
						AudioPlayer ap3 = new AudioPlayer( bd1 );
						ap3.addAudioEventListener( new AudioEventAdapter()
						{
							@Override
							public void audioEnded() 
							{
								// ================================================================= //
								// Sample rate conversion to 16KHz
								// Bit depth conversion to 8bit
								// Channel reduced to mono
								// ================================================================= //
								XuggleAudio x4 = new XuggleAudio( f );
								SampleRateConverter src3 = new SampleRateConverter( x4, 
									SampleRateConversionAlgorithm.LINEAR_INTERPOLATION,
									new AudioFormat( x4.getFormat().getNBits(), 16, 
											x4.getFormat().getNumChannels() ) );
								BitDepthConverter bd2 = new BitDepthConverter( src3, 
									BitDepthConversionAlgorithm.NEAREST, 
									new AudioFormat( 8, src3.getFormat().getSampleRateKHz(),
										src3.getFormat().getNumChannels() ) );
								MultichannelToMonoProcessor mc = new MultichannelToMonoProcessor( bd2 );
								System.out.println( "Playing audio "+src3.getFormat() );
								AudioPlayer ap4 = new AudioPlayer( mc );
								ap4.run();
								System.out.println( "4----------------------------------------- ");							
							}
						} );
						ap3.run();
						System.out.println( "3----------------------------------------- ");
					}
				} );
				ap2.run();
				System.out.println( "2----------------------------------------- ");
			}
		} );
		ap.run();
		System.out.println( "1----------------------------------------- ");
	}
}
