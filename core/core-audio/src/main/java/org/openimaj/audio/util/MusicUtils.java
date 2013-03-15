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
package org.openimaj.audio.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 *	Static utility methods that are related to music (more semantic
 *	than audio utilities).
 *	<p>	
 *	For working with notes, see {@link WesternScaleNote}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 27 Nov 2011
 */
public class MusicUtils
{
	/**
	 * 	Given a beats-per-minute rate, returns the number of milliseconds
	 * 	in a single beat.
	 * 
	 *	@param bpm Beats per minute
	 *	@return Number of milliseconds per beat.
	 */
	public static int millisPerBeat( final float bpm )
	{
		return (int)(60000f/bpm);
	}
	
	/**
	 * 	Parses the music notation from ABC files (without the header). Note number
	 * 	-1 signifies a rest.
	 * 
	 *	@param abc The abc note string
	 *	@return An list of note numbers and lengths
	 */
	public static List<IndependentPair<Integer,Double>> 
		parseABCNotes( final String abc )
	{
		final List<IndependentPair<Integer, Double>> ret = 
				new ArrayList<IndependentPair<Integer,Double>>();
		
		// Start with middle-C and alter from there
		int nn = 60;
		int length = 1;
		IndependentPair<Integer,Double> lastNote = null;
		
		for( int i = 0; i < abc.length(); i++ )
		{
			char c = abc.charAt( i );
			
			switch( c )
			{
				case '_': nn -= 1; break;
				case '=': nn -= 0; break;
				case '^': nn += 1; break;
				case ',': nn -= 12; break;
				case '\'': nn += 12; break;
				case '/': 
					if( lastNote != null )
						lastNote.setSecondObject( lastNote.getSecondObject()/2d );
					break;
				case '-':
				case 'z':
					nn = -1; break;
				default: 
				break; 
			}
			
			if( Character.isDigit( c ) && lastNote != null )
				lastNote.setSecondObject( lastNote.getSecondObject() * (c-48) );
			
			// If it's a note name...
			if( Character.isLetter( c ) && "abcdefgABCDEFG".contains( ""+c ) )
			{
				// If it's lower case it's the upper octave
				if( Character.isLowerCase( c ) )
					nn += 12;
				
				// Convert to upper case and get the note offset
				c = Character.toUpperCase( c );
				nn += Arrays.asList( WesternScaleNote.noteNames ).indexOf( ""+c );
				
				// Add it to the list
				ret.add( lastNote = new IndependentPair<Integer, Double>( nn, 
						(double)length ) );
				
				// Reset our variables for the next note
				nn = 60;
				length = 1;
			}
			else
			if( nn == -1 )
				ret.add( new IndependentPair<Integer, Double>( -1, (double)length ) );
		}
		
		return ret;
	}
}
