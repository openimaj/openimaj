/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.audio.AudioWaveformPlotter;

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
	 * @param args
	 * @throws PropertyException
	 * @throws IOException
	 * @throws InstantiationException
	 */
	public static void main( String[] args ) throws IOException,
	        PropertyException, InstantiationException
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
		URL audioFileURL = SpeechRecognition.class
		        .getResource( "/org/openimaj/demos/sandbox/audio/WelcomeToTheNews.wav" );

		// Check whether the audio file exists
		if( audioFileURL != null )
		{
			// Get a display of the audio waveform
			XuggleAudio xa = new XuggleAudio( audioFileURL );
			AudioWaveformPlotter awp = new AudioWaveformPlotter();
			MBFImage awi = awp.plotAudioWaveformImage( xa, 1000, 300,
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
			dataSource.setAudioFile( audioFileURL );

			// Start recognising words from the audio file
			Pattern p = Pattern.compile( "(\\w+)\\(([0-9.]+),([0-9.]+)\\)" );
			Result result = null;
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
					
					String word = matcher.group(1);
					double s = Double.parseDouble( matcher.group(2) ) * 1000;
					double e = Double.parseDouble( matcher.group(3) ) * 1000;
					
					int x = (int)(s/awp.millisecondsInView*1000);
					int x2 = (int)(e/awp.millisecondsInView*1000);
					
					System.out.println( word +" @ "+ x);
					
					img.drawLine( x, 320, x2, 320, RGBColour.YELLOW );
					img.drawLine( x, 318, x, 322, RGBColour.GREEN );
					img.drawLine( x2, 318, x2, 322, RGBColour.RED );
					img.drawText( word, x, 
							350, new GeneralFont("Courier", Font.PLAIN, 24), 
							1, RGBColour.WHITE  );
				}
			}

			DisplayUtilities.displayName( img, "waveform" );
		}
		else
		{
			System.err.println( "The audio file " + audioFileURL
			        + " could not be found" );
		}
	}
}
