/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.IOException;
import java.net.URL;

import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

/**
 * 	Basic Sphinx demo (from their webpage). No OpenIMAJ integration yet.
 * 
 * 	The Sphinx system generates huge amounts of temporary objects when
 * 	reading in the large dictionary which rather overwhelms the garbage
 * 	collector.  If it becomes really slow, or plain crashes, then try adding
 * 	the VM argument <code> -XX:+UseConcMarkSweepGC </code> which enforces a
 * 	different collection policy for the GC. That sometimes helps. You will 
 * 	likely have to supply extra heap space too (and probably quite a lot too:
 * 	<code>-Xmx4G</code>).
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

		// Check the configuration file exists
		if( configFile == null )
		{
			System.err.println( "Cannot find config file" );
			System.exit(1);
		}
		
		// Load the configuration
		ConfigurationManager cm = new ConfigurationManager( configFile );

		// Allocate the recognizer
		System.out.println( "Loading..." );
		Recognizer recognizer = (Recognizer)cm.lookup( "recognizer" );
		recognizer.allocate();

        // Configure the audio input for the recognizer
        OpenIMAJAudioFileDataSource dataSource = (OpenIMAJAudioFileDataSource) 
        		cm.lookup("audioFileDataSource");
        dataSource.setAudioFile( SpeechRecognition.class.getResource( 
        		"/org/openimaj/demos/sandbox/audio/Welcome To The news.wav" ) );

        // Start recognising words from the audio file
        Result result = null;
        while( (result = recognizer.recognize()) != null ) 
        {
            String resultText = result.getTimedBestResult( false, true );
            System.out.println(resultText);
        }
	}
}
