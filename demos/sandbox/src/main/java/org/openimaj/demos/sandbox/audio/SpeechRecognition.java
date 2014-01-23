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

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.conversion.BitDepthConverter;
import org.openimaj.audio.conversion.BitDepthConverter.BitDepthConversionAlgorithm;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.audio.conversion.SampleRateConverter;
import org.openimaj.audio.conversion.SampleRateConverter.SampleRateConversionAlgorithm;
import org.openimaj.audio.filters.EQFilter;
import org.openimaj.audio.filters.EQFilter.EQType;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioOverviewVisualisation;

import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

/**
 * Basic Sphinx demo (from their webpage). Uses the OpenIMAJ audio file data
 * source to link OpenIMAJ audio engine to Sphinx.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @created 23 May 2012
 */
public class SpeechRecognition
{
	/**
	 * 	Returns the affected audio stream.
	 *	@param as The audio stream to affect
	 *	@return The affected audio stream
	 */
	public static AudioStream getStream( final AudioStream as )
	{
		// Effect chain:
		//
		//		-> Mono
		//		-> Band-pass filter (LPF + HPF)
		//		-> Sample rate to 16KHz
		//		-> Bit rate to 8-bit
		//

		final MultichannelToMonoProcessor m2m2 = new MultichannelToMonoProcessor( as );

		final double fc = 1000; // mid-point 1000Hz
		final double q = 1600;  // HPF @ 200Hz, LPF @ 1800Hz
		final EQFilter lpf = new EQFilter( m2m2, EQType.LPF, fc+q/2 );
		final EQFilter hpf = new EQFilter( lpf, EQType.HPF, fc-q/2 );

		final SampleRateConverter src2 = new SampleRateConverter( hpf,
				SampleRateConversionAlgorithm.LINEAR_INTERPOLATION,
				new AudioFormat( m2m2.getFormat().getNBits(),
						16, m2m2.getFormat().getNumChannels() ) );

		final BitDepthConverter xa2 = new BitDepthConverter( src2,
				BitDepthConversionAlgorithm.NEAREST,
				new AudioFormat( 8, src2.getFormat().getSampleRateKHz(),
						src2.getFormat().getNumChannels() ) );

		return xa2;
	}

	/**
	 * @param args
	 * @throws PropertyException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws InterruptedException
	 */
	public static void main( final String[] args ) throws IOException,
	PropertyException, InstantiationException, InterruptedException
	{
		final URL configFile = SpeechRecognition.class
				.getResource( "/org/openimaj/demos/sandbox/audio/sphinx-config-hub4.xml" );

		// Check the configuration file exists
		if( configFile == null )
		{
			System.err.println( "Cannot find config file" );
			System.exit( 1 );
		}

		// Get the audio file input
		// URL audioFileURL = new URL( "http://www.moviewavs.com/0058349934/WAVS/Movies/Juno/experimenting.wav" );
		final File audioFileURL = new File( "videoplayback.mp4" );

		try
		{
			final List<Rectangle> boundingBoxes = new ArrayList<Rectangle>();

			System.out.println( audioFileURL );

			// Get a display of the audio waveform
			final XuggleAudio xuggle = new XuggleAudio( audioFileURL );
			final AudioOverviewVisualisation awp = new AudioOverviewVisualisation( SpeechRecognition.getStream( xuggle ) );
			final MBFImage awi = awp.plotAudioWaveformImage( 1000, 300,
					new Float[]
							{ 0f, 0f, 0f, 1f }, new Float[]
									{ 1f, 1f, 1f, 1f } );

			System.out.println( awp.millisecondsInView );

			final MBFImage img = new MBFImage( 1000, 400, 3 );
			img.drawImage( awi, 0, 0 );
			DisplayUtilities.displayName( img, "waveform" );

			// Load the configuration
			final ConfigurationManager cm = new ConfigurationManager( configFile );

			// Allocate the recognizer
			System.out.println( "Loading..." );
			final Recognizer recognizer = (Recognizer)cm.lookup( "recognizer" );
			recognizer.allocate();

			// Configure the audio input for the recognizer
			final OpenIMAJAudioFileDataSource dataSource = (OpenIMAJAudioFileDataSource)cm
					.lookup( "audioFileDataSource" );
			final XuggleAudio xuggle2 = new XuggleAudio( audioFileURL );
			dataSource.setAudioStream( SpeechRecognition.getStream( xuggle2 ) );

			// Play the audio
			//			XuggleAudio xuggleToPlay = new XuggleAudio( audioFileURL );
			//			AudioPlayer ap = AudioPlayer.createAudioPlayer(	getStream( xuggleToPlay ) );
			//			ap.run();

			// The font to plot the words
			final GeneralFont font = new GeneralFont("Courier", Font.PLAIN );
			final FontStyle<Float[]> fontStyle = font.createStyle( awi.createRenderer() );

			// Start recognising words from the audio file
			final Pattern p = Pattern.compile( "([A-Za-z0-9'_]+)\\(([0-9.]+),([0-9.]+)\\)" );
			Result result = null;
			final StringBuffer sb = new StringBuffer();
			while( (result = recognizer.recognize()) != null )
			{
				final String resultText = result.getTimedBestResult( false, true );
				System.out.println( resultText );

				final Matcher matcher = p.matcher( resultText );
				while( matcher.find() )
				{
					System.out.println( "Word:  " + matcher.group( 1 ) );
					System.out.println( "Start: " + matcher.group( 2 ) );
					System.out.println( "End:   " + matcher.group( 3 ) );

					// Parse the word and timings from the result
					final String word = matcher.group(1);
					final double s = Double.parseDouble( matcher.group(2) ) * 1000;
					final double e = Double.parseDouble( matcher.group(3) ) * 1000;
					sb.append( word+" " );

					// Get the bounds of the word polygon
					final Rectangle bounds = font.getRenderer(
							awi.createRenderer() ).getSize(
									word, fontStyle );

					// Determine the pixel coordinate of the start and end times
					final int startX = (int)(s/awp.millisecondsInView*1000);
					final int endX   = (int)(e/awp.millisecondsInView*1000);

					// Draw bars showing the range of the word
					img.drawLine( startX, 320, endX, 320, RGBColour.YELLOW );
					img.drawLine( startX, 318, startX, 322, RGBColour.GREEN );
					img.drawLine( endX, 318, endX, 322, RGBColour.RED );

					int y = 350;
					bounds.translate( startX, y );
					boolean noIntersection = true;
					do
					{
						noIntersection = true;
						for( final Rectangle r : boundingBoxes )
							if( r.isOverlapping( bounds ) )
							{ noIntersection = false; break; }

						if( !noIntersection )
							bounds.translate( 0, bounds.height );
					} while( !noIntersection );
					y = (int)bounds.y;

					// Draw the word
					img.drawLine( startX, 322, startX, (int)(y+bounds.height),
							new Float[]{0.4f,0.4f,0.4f} );
					img.drawLine( startX, (int)(y+bounds.height), startX+8,
							(int)(y+bounds.height), new Float[]{0.4f,0.4f,0.4f} );
					img.drawText( word, startX, y, font, 24, RGBColour.WHITE  );

					// Store the bounding box
					boundingBoxes.add( bounds );
				}
			}

			DisplayUtilities.displayName( img, "waveform" );
			System.out.println( "=======================================" );
			System.out.println( "Text: \n"+sb.toString() );
			System.out.println( "=======================================" );
		}
		catch( final NumberFormatException e )
		{
			e.printStackTrace();
		}
		catch( final IllegalStateException e )
		{
			e.printStackTrace();
		}
	}
}
