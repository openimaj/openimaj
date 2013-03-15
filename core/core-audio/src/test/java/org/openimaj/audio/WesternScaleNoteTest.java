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
