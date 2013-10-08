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
package org.openimaj.demos.sandbox.audio;

import java.util.List;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioMixer;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.Instrument;
import org.openimaj.audio.generation.PolyphonicSynthesizer;
import org.openimaj.audio.util.BasicMusicTimekeeper;
import org.openimaj.audio.util.MusicUtils;
import org.openimaj.time.Sequencer;
import org.openimaj.time.Sequencer.SequencedAction;
import org.openimaj.time.Sequencer.SequencerEvent;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class Tune
{
	/**
	 * 	A single voice channel in the polyphonic synth.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 12 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	private class SingleVoiceChannel
	{
		/** The notes being played */
		private List<IndependentPair<Integer, Double>> notes = null;

		public SingleVoiceChannel( final Sequencer sequencer,
				final Instrument instrument, final String notes,
				final long millisPerNote )
		{
			// Parse the notes into note numbers and lengths.
			this.notes = MusicUtils.parseABCNotes( notes );
			System.out.println( this.notes );

			// Accumulate the note lengths so they become absolute position
			// markers. We also multiply them by the number of ticks in each
			// beat, so that we get an absolute tick position for the note
			// in time.
			double o = 0;
			for( final IndependentPair<Integer,Double> note: this.notes )
			{
				final double tmp = note.getSecondObject();
				note.setSecondObject( o * millisPerNote );
				o += tmp;
			}

			// Add the notes as sequencer events
			int lastNote = 0;
			for( final IndependentPair<Integer,Double> note: this.notes )
			{
				final int ln = lastNote;
				sequencer.addEvent( new SequencerEvent(
						(long)note.secondObject().doubleValue(),
						new SequencedAction()
						{
							@Override
							public boolean performAction()
							{
								if( note.firstObject() != -1 )
								{
									instrument.noteOff( ln );
									instrument.noteOn( note.firstObject(), 0.5f );
								}
								else
									instrument.noteOff( ln );

								return true;
							}
						} ) );
				lastNote = note.firstObject();
			}
		}
	}

	/** The notes for each channel */
	private final String[] notes = {
			"CD4z", //DEFG-",
//			"E4z", //FGAB-",
//			",C4z", //,D,E,F,G-",
//			",,C4z" //,,E2,,G-"
	};

	/** Each of the characters in the note strings are this number of notes */
	private final double noteLength = 1/4d;

	/** Tempo of the tune playing */
	private final float tempo = 120;

	/** The timekeeper for all the music channels */
	private final BasicMusicTimekeeper timeKeeper = new BasicMusicTimekeeper();

	/** The sequencer to use */
	private final Sequencer sequencer;

	/**
	 * 	Create a new tune.
	 */
	public Tune()
	{
		// Set the speed of the tune
		this.timeKeeper.setBPM( this.tempo );

		// Number of milliseconds per music tick
		final long millisPerTick = MusicUtils.millisPerBeat(
				(float)this.timeKeeper.getBPM() ) /
				this.timeKeeper.getTicksPerBeat() * 10;

		System.out.println( millisPerTick );

		// Create the sequencer
		this.sequencer = new Sequencer( this.timeKeeper, millisPerTick );

		// The mixer that will mix all the synths' outputs
		final AudioMixer mixer = new AudioMixer( new AudioFormat( 16, 44.1, 1 ) );

		// Work out the number of milliseconds per note length
		// Assumption is that a beat lands on a quarter note.
		final long millisPerNote = (long)(MusicUtils.millisPerBeat(
				(float)this.timeKeeper.getBPM() ) *
				4d * this.noteLength);

		// Create a new voice.
		for( final String noteString: this.notes )
		{
			// Create a synth (start with making no noise)
			final PolyphonicSynthesizer synth = new PolyphonicSynthesizer( 5 );

			// Add it as a stream to the mixer
			mixer.addStream( synth, 0.1f );

			// Create a voice channel to run the synth
			new SingleVoiceChannel( this.sequencer, synth, noteString,
					 millisPerNote );
		}

		final AudioPlayer ap = new AudioPlayer( mixer );
		new Thread( ap ).start();

		// Start the timekeeper - the "music" should start
		new Thread( this.sequencer ).start();
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		new Tune();
	}
}
