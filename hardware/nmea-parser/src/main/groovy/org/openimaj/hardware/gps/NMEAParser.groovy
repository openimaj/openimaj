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
package org.openimaj.hardware.gps;

import org.openimaj.hardware.gps.NMEASentenceType;


/**
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
 */
@SuppressWarnings("all")
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
