/**
 * 
 */
package org.openimaj.hardware.gps;

import org.openimaj.hardware.gps.NMEASentenceType;


/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 12 Jul 2011
 */
public class NMEAParser
{	
	public NMEAParser()
	{
	}
	
	public List<NMEAMessage> parseString( String data )
	{
		def messages = [];
		
		// Go through all lines splitting by comma
		data.splitEachLine(",")
		{ tokens ->
			
			try
			{
				// tokens.each { println "token: ${it}" }
				
				// Get the device identifier
				if( tokens[0][0] as String == '$' )
				{
					// Get the message type...
					def sentenceType = tokens[0][3..5] as NMEASentenceType;
					// println "message: "+sentenceType;
					
					// Create the appropriate message based on message type
					NMEAMessage message = sentenceType( tokens );
	
					if( message != null )
					{	
						// Store the device identifier into the message
						message.deviceIdentifier = tokens[0][1..2];
						// println "device : "+message.deviceIdentifier;				
		
						// Store the message original string
						message.string = tokens.join(",");
						
						// System.out.println( "Message: "+message );
						
						messages << message;
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		return messages;
	}
}
