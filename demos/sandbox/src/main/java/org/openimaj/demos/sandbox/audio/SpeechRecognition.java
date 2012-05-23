/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.IOException;
import java.net.URL;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

/**
 * 	Basic Sphinx demo (from their webpage). No OpenIMAJ integration yet.
 * 	Also: doesn't work!
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 * 	@version $Author$, $Revision$, $Date$
 * 	@created 23 May 2012
 */
public class SpeechRecognition
{
	/**
	 * @param args
	 * @throws PropertyException 
	 * @throws IOException 
	 * @throws InstantiationException 
	 */
	public static void main( String[] args ) 
		throws IOException, PropertyException, InstantiationException
	{
		URL configFile = SpeechRecognition.class.getResource( 
    		"/org/openimaj/demos/sandbox/audio/sphinx-config.xml" );
		System.out.println( configFile );
		
		if( configFile == null )
		{
			System.err.println( "Cannot find config file" );
			System.exit(1);
		}
		
		// Load the configuration
		ConfigurationManager cm = new ConfigurationManager( configFile );

		// allocate the recognizer
		System.out.println( "Loading..." );
		Recognizer recognizer = (Recognizer)cm.lookup( "recognizer" );
		recognizer.allocate();

		// Start the microphone or exit if the program if this is not possible
		Microphone microphone = (Microphone)cm.lookup( "microphone" );
		if( !microphone.startRecording() )
		{
			System.out.println( "Cannot start microphone." );
			recognizer.deallocate();
			System.exit( 1 );
		}

		// loop the recognition until the programm exits.
		while( true )
		{
			System.out.println( "Start speaking. Press Ctrl-C to quit.\n" );

			Result result = recognizer.recognize();

			if( result != null )
			{
				String resultText = result.getBestResultNoFiller();
				System.out.println( "You said: " + resultText + '\n' );
			}
			else
			{
				System.out.println( "I can't hear what you said.\n" );
			}
		}
	}
}
