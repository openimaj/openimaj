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
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
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
		return noteName+octaveNumber;
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
	public final static int nStepsBetween( WesternScaleNote note1, 
			WesternScaleNote note2 )
	{
		return note1.noteNumber - note2.noteNumber;
	}
	
	/**
	 * 	Given a note name and octave, returns a {@link WesternScaleNote} from
	 * 	which other information can be garnered.
	 * 
	 *	@param noteName The name of the note
	 *	@param octaveNumber The octave of the note.
	 *	@return A new WesternScaleNote
	 */
	public final static WesternScaleNote createNote( String noteName, 
			int octaveNumber )
	{
		WesternScaleNote n = new WesternScaleNote();
		n.noteName = noteName;
		n.octaveNumber = octaveNumber;
		n.noteNumber = Arrays.asList( noteNames ).indexOf( noteName ) + 
			(octaveNumber+1)*12;
		
		if( noteName.equals("A") && octaveNumber == 4 )
				n.frequency = WesternScaleNote.tuningOfA;
		else	n.frequency = (float)(tuningOfA * Math.pow( twelfthRootOfTwo, 
					nStepsBetween( WesternScaleNote.createNote("A",4), n ) ));
					  
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
	public final static WesternScaleNote createNote( float frequency )
	{
		double f = frequency/tuningOfA;
		int noteNum = (int)(69 + 12*(Math.log(f)/Math.log(2)));
		return WesternScaleNote.createNote( noteNum );
	}

	/**
	 * 	Given a note number, returns a {@link WesternScaleNote} from which other
	 * 	information can be garnered.
	 * 
	 *	@param noteNumber A note number
	 *	@return A {@link WesternScaleNote}
	 */
	public final static WesternScaleNote createNote( int noteNumber )
	{
		WesternScaleNote n = new WesternScaleNote();
		n.noteNumber = noteNumber;
		n.octaveNumber = noteNumber/12 -1;
		n.noteName = noteNames[ noteNumber%12 ];

		if( n.noteName.equals("A") && n.octaveNumber == 4 )
				n.frequency = WesternScaleNote.tuningOfA;
		else	n.frequency = (float)(tuningOfA * Math.pow( twelfthRootOfTwo, 
					nStepsBetween( WesternScaleNote.createNote("A",4), n ) ));
				  
		return n;
	}
}
