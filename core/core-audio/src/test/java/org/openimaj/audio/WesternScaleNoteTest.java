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
package org.openimaj.audio;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.audio.util.WesternScaleNote;

/**
 *	Tests the conversion algorithms in the {@link WesternScaleNote} class.
 *	See the link for the source of the test values.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 *	@see "http://www.tonalsoft.com/pub/news/pitch-bend.aspx"
 */
public class WesternScaleNoteTest
{
	/**
	 *	Test the conversions
	 */
	@Test
	public void test()
	{
		final String[] notes = new String[] { "C", "C#", "D", "D#", "E", "F",
				"F#", "G", "G#", "A", "A#", "B", "C"
		};
		
		final double[] freqs = new double[] {
				261.6255653006, 277.1826309769, 293.6647679174,
				311.1269837221, 329.6275569129, 349.2282314330,
				369.9944227116, 391.9954359817, 415.3046975799,
				440.0000000000, 466.1637615181, 493.8833012561,
				523.2511306012
		};
		
		// Create a middle C
		for( int i = 60; i < 72; i++ )
		{
			final int j = i-60;
			final WesternScaleNote wsn = WesternScaleNote.createNote( i );
			
			System.out.println( wsn );
			
			Assert.assertEquals( i, wsn.noteNumber );
			Assert.assertEquals( notes[j], wsn.noteName );
			Assert.assertEquals( freqs[j], wsn.frequency, 0.1 );
		}
	}
}
