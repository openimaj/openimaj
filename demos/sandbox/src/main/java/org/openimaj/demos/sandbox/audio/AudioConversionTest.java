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
package org.openimaj.demos.sandbox.audio;

import java.io.File;

import org.openimaj.audio.AudioEventAdapter;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.SampleChunk;
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

							@Override
							public void beforePlay( SampleChunk sc )
							{
							}

							@Override
							public void afterPlay( AudioPlayer ap,
									SampleChunk sc )
							{
							}
						} );
						ap3.run();
						System.out.println( "3----------------------------------------- ");
					}

					@Override
					public void beforePlay( SampleChunk sc )
					{
					}

					@Override
					public void afterPlay( AudioPlayer ap, SampleChunk sc )
					{
					}
				} );
				ap2.run();
				System.out.println( "2----------------------------------------- ");
			}

			@Override
			public void beforePlay( SampleChunk sc )
			{
			}

			@Override
			public void afterPlay( AudioPlayer ap, SampleChunk sc )
			{
			}
		} );
		ap.run();
		System.out.println( "1----------------------------------------- ");
	}
}
