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
import org.openimaj.vis.audio.AudioWaveformPlotter;

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
	public static AudioStream getStream( AudioStream as )
	{
		// Effect chain:
		//
		//		-> Mono
		//		-> Band-pass filter (LPF + HPF)
		//		-> Sample rate to 16KHz
		//		-> Bit rate to 8-bit
		//
		
		MultichannelToMonoProcessor m2m2 = new MultichannelToMonoProcessor( as );
		
		double fc = 1000; // mid-point 1000Hz
		double q = 1600;  // HPF @ 200Hz, LPF @ 1800Hz			
		EQFilter lpf = new EQFilter( m2m2, EQType.LPF, fc+q/2 );
		EQFilter hpf = new EQFilter( lpf, EQType.HPF, fc-q/2 );
		
		SampleRateConverter src2 = new SampleRateConverter( hpf, 
				SampleRateConversionAlgorithm.LINEAR_INTERPOLATION,
				new AudioFormat( m2m2.getFormat().getNBits(),
								 16, m2m2.getFormat().getNumChannels() ) );
		
		BitDepthConverter xa2 = new BitDepthConverter( src2, 
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
	public static void main( String[] args ) throws IOException,
	        PropertyException, InstantiationException, InterruptedException
	{
		URL configFile = SpeechRecognition.class
		        .getResource( "/org/openimaj/demos/sandbox/audio/sphinx-config-hub4.xml" );

		// Check the configuration file exists
		if( configFile == null )
		{
			System.err.println( "Cannot find config file" );
			System.exit( 1 );
		}

		// Get the audio file input
		// URL audioFileURL = new URL( "http://www.moviewavs.com/0058349934/WAVS/Movies/Juno/experimenting.wav" );
		File audioFileURL = new File( "videoplayback.mp4" );

		try
		{
			List<Rectangle> boundingBoxes = new ArrayList<Rectangle>();
			
			System.out.println( audioFileURL );
			
			// Get a display of the audio waveform
			XuggleAudio xuggle = new XuggleAudio( audioFileURL );
			AudioWaveformPlotter awp = new AudioWaveformPlotter( getStream( xuggle ) );
			MBFImage awi = awp.plotAudioWaveformImage( 1000, 300,
			        new Float[]
			        { 0f, 0f, 0f, 1f }, new Float[]
			        { 1f, 1f, 1f, 1f } );

			System.out.println( awp.millisecondsInView );

			MBFImage img = new MBFImage( 1000, 400, 3 );
			img.drawImage( awi, 0, 0 );
			DisplayUtilities.displayName( img, "waveform" );
			
			// Load the configuration
			ConfigurationManager cm = new ConfigurationManager( configFile );

			// Allocate the recognizer
			System.out.println( "Loading..." );
			Recognizer recognizer = (Recognizer)cm.lookup( "recognizer" );
			recognizer.allocate();

			// Configure the audio input for the recognizer
			OpenIMAJAudioFileDataSource dataSource = (OpenIMAJAudioFileDataSource)cm
			        .lookup( "audioFileDataSource" );
			XuggleAudio xuggle2 = new XuggleAudio( audioFileURL );
			dataSource.setAudioStream( getStream( xuggle2 ) );
			
			// Play the audio
//			XuggleAudio xuggleToPlay = new XuggleAudio( audioFileURL );
//			AudioPlayer ap = AudioPlayer.createAudioPlayer(	getStream( xuggleToPlay ) );
//			ap.run();

			// The font to plot the words
			GeneralFont font = new GeneralFont("Courier", Font.PLAIN, 24);
			FontStyle<GeneralFont, Float[]> fontStyle = font.createStyle( awi.createRenderer() );
			
			// Start recognising words from the audio file
			Pattern p = Pattern.compile( "([A-Za-z0-9'_]+)\\(([0-9.]+),([0-9.]+)\\)" );
			Result result = null;
			StringBuffer sb = new StringBuffer();
			while( (result = recognizer.recognize()) != null )
			{
				String resultText = result.getTimedBestResult( false, true );
				System.out.println( resultText );

				Matcher matcher = p.matcher( resultText );
				while( matcher.find() )
				{
					System.out.println( "Word:  " + matcher.group( 1 ) );
					System.out.println( "Start: " + matcher.group( 2 ) );
					System.out.println( "End:   " + matcher.group( 3 ) );

					// Parse the word and timings from the result
					String word = matcher.group(1);
					double s = Double.parseDouble( matcher.group(2) ) * 1000;
					double e = Double.parseDouble( matcher.group(3) ) * 1000;
					sb.append( word+" " );

					// Get the bounds of the word polygon
					Rectangle bounds = font.getRenderer( 
							awi.createRenderer() ).getBounds( 
									word, fontStyle );

					// Determine the pixel coordinate of the start and end times
					int startX = (int)(s/awp.millisecondsInView*1000);
					int endX   = (int)(e/awp.millisecondsInView*1000);
					
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
						for( Rectangle r : boundingBoxes )
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
					img.drawText( word, startX, y, font, 1, RGBColour.WHITE  );
					
					// Store the bounding box
					boundingBoxes.add( bounds );
				}
			}

			DisplayUtilities.displayName( img, "waveform" );
			System.out.println( "=======================================" );
			System.out.println( "Text: \n"+sb.toString() );
			System.out.println( "=======================================" );
		}
		catch( NumberFormatException e )
		{
			e.printStackTrace();
		}
		catch( IllegalStateException e )
		{
			e.printStackTrace();
		}
	}
}
