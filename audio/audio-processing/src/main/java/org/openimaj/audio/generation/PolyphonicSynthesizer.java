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
package org.openimaj.audio.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioMixer;
import org.openimaj.audio.Instrument;

/**
 *	A class that uses a pool of synthesizers for creating a polyphonic
 *	synthesizer.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class PolyphonicSynthesizer extends AudioMixer implements Instrument
{
	/** The synth channels */
	private Map<Integer,Synthesizer> playingSynths = new HashMap<Integer, Synthesizer>();
	
	/** A set of playingSynths that we can call upon to make sounds */
	private final Stack<Synthesizer> voicePool = new Stack<Synthesizer>();
	
	/**
	 * 	Constructor
	 * 	@param nPolyphony Number of voices polyphony allowed 
	 */
	public PolyphonicSynthesizer( final int nPolyphony )
	{
		super( new AudioFormat( 16, 44.1, 1 ) );
		this.playingSynths = new HashMap<Integer, Synthesizer>();
		
		for( int i = 0; i < nPolyphony; i++ )
		{
			final Synthesizer s = new Synthesizer(); 
			this.voicePool.add( s );
			// super.addStream( s, 1f );
		}
	}
	
	/**
	 * 	Turn the given note on.
	 *	@param noteNumber
	 */
	@Override
	public void noteOn( final int noteNumber, final double velocity )
	{
		System.out.println( "Playing: "+this.playingSynths );
		System.out.println( "Pool:    "+this.voicePool );
		
		// Check if a synth is already playing a note with the given note
		// number. If so, we'll just retrigger the note.
		Synthesizer s = this.playingSynths.get( noteNumber );
		if( s == null )
		{
			if( this.voicePool.size() > 0 )
				s = this.voicePool.pop();
		}

		System.out.println( "Using synth "+s );
		
		// If we've got an existing or new synth...
		if( s != null )
		{
			super.addStream( s, 1f );
			
			// Push it into the playing synths map
			this.playingSynths.put( noteNumber, s );
			
			// Make it play the right note
			s.noteOn( noteNumber, velocity );

			final Synthesizer ss = s;
			
			// Catch when it's finished playing and pop it back on the pool
			s.addSynthesizerListener( new SynthesizerListener()
			{
				@Override
				public void synthQuiet()
				{
					System.out.println( "Synth quiet "+ss );
					final Synthesizer synth = PolyphonicSynthesizer.this.
							playingSynths.remove( noteNumber );
					PolyphonicSynthesizer.this.voicePool.push( synth );
					synth.removeSynthesizerListener( this );
//					PolyphonicSynthesizer.super.removeStream( synth );
				}
			} );
		}
	}
	
	/**
	 * 	Turn the given note off
	 *	@param noteNumber
	 */
	@Override
	public void noteOff( final int noteNumber )
	{
		final Synthesizer s = this.playingSynths.get( noteNumber );
		if( s != null )
			s.noteOff();
	}
	
	/**
	 * 	The number of synths in use.
	 *	@return The number of synths in use.
	 */
	public int getChannelsInUse()
	{
		return this.playingSynths.keySet().size();
	}
}
