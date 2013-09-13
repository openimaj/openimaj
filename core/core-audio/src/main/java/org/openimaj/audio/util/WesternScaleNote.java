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

import java.util.Arrays;

/**
 *	Represents a note in a standard equal-tempered Western scale. There
 *	are various static methods for creating notes from names, note numbers,
 *	and frequencies.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@see "http://www.musicdsp.org/showone.php?id=125"
 *	@created 27 Nov 2011
 */
public class WesternScaleNote
{
	/** The MIDI note number of the note */
	public int noteNumber;
	
	/** The note name; e.g. C, D, F#, etc. */
	public String noteName;
	
	/** The octave number. Note C4 is middle C */
	public int octaveNumber;
	
	/** The frequency of the note */
	public float frequency;
	
	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.noteName + this.octaveNumber + 
				" ("+this.frequency+"Hz, "+this.noteNumber+")";
//		return this.noteName+this.octaveNumber;
	}
	
	// ---------------------- Static stuff below here ------------------------ //
	/** The standing tuning is A=440Hz. Can change that here. */
	public static float tuningOfA = 440.0f;
	
	/** The twelfth root of 2 */
	public static final double twelfthRootOfTwo = 1.059463094359;
	
	/** The names of the notes in the Western scale */
	public static final String[] noteNames = new String[]
	          {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B" };
	
	/**
	 * 	Returns the number of half-steps between the two given notes.
	 * 
	 *	@param note1 The first note of the interval
	 *	@param note2 The second note of the interval
	 *	@return The number of half-steps between the two notes.
	 */
	public final static int nStepsBetween( final WesternScaleNote note1, 
			final WesternScaleNote note2 )
	{
		return note1.noteNumber - note2.noteNumber;
	}
	
	/**
	 * 	Converts a MIDI note number to a frequency.
	 *	@param noteNumber The note number
	 *	@return The frequency
	 */
	public final static float noteToFrequency( final int noteNumber )
	{
		return (float)(WesternScaleNote.tuningOfA * 
				Math.pow( 2, (noteNumber-69)/12d ));
	}
	
	/**
	 * 	Converts a frequency to the nearest note number
	 *	@param frequency The frequency
	 *	@return The note number
	 */
	public final static int frequencyToNote( final float frequency )
	{
		return (int)Math.round( 12 * Math.log( 
				frequency/WesternScaleNote.tuningOfA )/Math.log(2) )
				+69;
	}
	
	/**
	 * 	Given a note string, returns a new {@link WesternScaleNote} from
	 * 	which other information can be garnered. A note string is the note name
	 * 	followed by the octave; e.g. "D3" or "A#5".
	 *	@param noteString The note string
	 *	@return A {@link WesternScaleNote} or null if the note string is invalid
	 */
	public final static WesternScaleNote createNote( final String noteString )
	{
		// Options we have are nO, nAO, nOO, nAOO.

		// Find the longest matching start to the string
		String startNoteName = null;
		int length = 0;
		for( String noteName: noteNames )
		{
			if( noteString.startsWith( noteName ) && noteName.length() > length )
			{
				startNoteName = noteName;
				length = noteName.length();
			}
		}
		
		if( startNoteName == null ) return null;
		
		// Parse the octave value
		int octave = Integer.parseInt( noteString.substring( 
				startNoteName.length() ) );
		
		// Create a new note
		return WesternScaleNote.createNote( startNoteName, octave ); 
	}
	
	/**
	 * 	Given a note name and octave, returns a {@link WesternScaleNote} from
	 * 	which other information can be garnered.
	 * 
	 *	@param noteName The name of the note
	 *	@param octaveNumber The octave of the note.
	 *	@return A new WesternScaleNote
	 */
	public final static WesternScaleNote createNote( final String noteName, 
			final int octaveNumber )
	{
		final WesternScaleNote n = new WesternScaleNote();
		n.noteName = noteName;
		n.octaveNumber = octaveNumber;
		n.noteNumber = Arrays.asList( WesternScaleNote.noteNames ).indexOf( noteName ) + 
			(octaveNumber+1)*12;
		n.frequency = WesternScaleNote.noteToFrequency( n.noteNumber );
					  
		return n;
	}
	
	/**
	 * 	Create a {@link WesternScaleNote} given a frequency. It does this by
	 * 	converting the frequency in to a MIDI note number and using
	 * 	{@link WesternScaleNote#createNote(int)} which returns the note. For
	 * 	this reason, the value of the member {@link #frequency} in the returned
	 * 	note may not be the same as the given frequency. 
	 * 
	 *	@param frequency The frequency to convert to a note.
	 *	@return A {@link WesternScaleNote}
	 */
	public final static WesternScaleNote createNote( final float frequency )
	{
		final int noteNum = WesternScaleNote.frequencyToNote( frequency );
		return WesternScaleNote.createNote( noteNum );
	}

	/**
	 * 	Given a note number, returns a {@link WesternScaleNote} from which other
	 * 	information can be garnered.
	 * 
	 *	@param noteNumber A note number
	 *	@return A {@link WesternScaleNote}
	 */
	public final static WesternScaleNote createNote( final int noteNumber )
	{
		final WesternScaleNote n = new WesternScaleNote();
		n.noteNumber = noteNumber;
		n.octaveNumber = noteNumber/12 -1;
		n.noteName = WesternScaleNote.noteNames[ noteNumber%12 ];
		n.frequency = WesternScaleNote.noteToFrequency( noteNumber );
		
		return n;
	}

}
