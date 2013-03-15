/**
 * 
 */
package org.openimaj.audio;

/**
 *	Interface for instruments that can play music.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface Instrument
{
	/**
	 * 	Play a note on the instrument.
	 *	@param noteNumber The MIDI note number
	 *	@param velocity The velocity (0-1)
	 */
	public void noteOn( int noteNumber, double velocity );
	
	/**
	 * 	Turn off the given note.
	 *	@param noteNumber The note to turn off.
	 */
	public void noteOff( int noteNumber );
}
