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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.Instrument;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.generation.Oscillator.SineOscillator;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.util.WesternScaleNote;

/**
 * 	Really really basic synthesizer. Useful for doing tests by running the
 * 	synth as an audio source through filters or whatever.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 *	@created 2 May 2012
 */
public class Synthesizer extends AudioStream implements Instrument
{
	/**
	 * 	Interface for options.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static interface OscillatorOptions
	{
	}

	/**
	 * 	Options class for FM synthesis. Note that the carrier options are set by the
	 * 	FM synth and will be overridden.  The modulator options can be controlled
	 * 	by the user.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Feb 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class FMOptions implements OscillatorOptions
	{
		/** The carrier signal */
		public Synthesizer carrier;

		/** The modulator signal */
		public Synthesizer modulator;

		/** The modulator amplitude */
		public double modulatorAmplitude = 1000d / Integer.MAX_VALUE;
	}

	/** The current time position of the synth */
	private double currentTime = 0;

	/** This is a cached version of the current time * 1000 as it's used lots of places */
	private double currentTimeMS = 0;

	/** The oscillator used to generate the wave */
	private Oscillator oscillator = new SineOscillator();

	/** Default sample chunk length is 1024 bytes */
	private final int sampleChunkLength = 1024;

	/** Default frequency is the standard A4 (440Hz) tuning pitch */
	private double frequency = 440;

	/** The gain of the synth */
	private int gain = Integer.MAX_VALUE;

	/** The time the last note on was detected */
	private double noteOnTime = 0;

	/** Whether to generate a note or not */
	private boolean noteOn = true;

	/** Determines whether a note off has been detected. */
	private boolean noteOff = false;

	/** The time the note off was detected */
	private double noteOffTime = 0;

	/** Envelope parameter - attack */
	private long attack = 0;

	/** Envelope parameter - decay */
	private long decay = 0;

	/** Envelope parameter - sustain */
	private float sustain = 1f;

	/** Envelope parameter - release */
	private long release = 0;

	/** The current value on the envelope */
	private float currentADSRValue = 0;

	/** The phase the envelope is in */
	private char envelopePhase = 'N';

	/** Listeners for synthesizer events */
	private final List<SynthesizerListener> listeners =
			new ArrayList<SynthesizerListener>();

	/**
	 *
	 */
	public Synthesizer()
    {
		this.setFormat( new AudioFormat( 16, 44.1, 1 ) );
    }

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
    public SampleChunk nextSampleChunk()
    {
		final Oscillator o = this.oscillator;
		if( !this.noteOn )
			return null;
//			o = Oscillator.NONE;

	    final SampleChunk x = o.getSampleChunk( this.sampleChunkLength,
	    		this.currentTime, this.frequency, this.gain, this.format );

	    this.applyADSREnvelope( x.getSampleBuffer() );

	    this.currentTime += x.getSampleBuffer().size() /
	    	(this.format.getSampleRateKHz()*1000d);
	    this.currentTimeMS = this.currentTime * 1000d;

	    return x;
    }

	/**
	 * 	Set the frequency at which the synth will generate tones.
	 *  @param f The frequency
	 */
	public void setFrequency( final double f )
	{
		this.frequency = f;
	}

	/**
	 * 	Set the gain at which the synth will generate tones
	 *	@param gain The gain
	 */
	public void setGain( final int gain )
	{
		this.gain = gain;
	}

	/**
	 * 	Set the type of oscillator used to generate tones.
	 *  @param t The type of oscillator.
	 */
	public void setOscillatorType( final Oscillator t )
	{
		this.oscillator = t;
	}

	/**
	 * 	Returns the oscillator in use.
	 * 	@return the oscillator instance.
	 */
	public Oscillator getOscillator()
	{
		return this.oscillator;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
    public void reset()
    {
		this.currentTime = 0;
    }

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		return -1;
	}

	/**
	 * 	Note on.
	 */
	public void noteOn()
	{
		this.noteOn = true;
		this.noteOff = false;
		this.noteOnTime = this.currentTimeMS;
	}

	/**
	 *	Note off
	 */
	public void noteOff()
	{
		this.noteOff  = true;
		this.noteOffTime = this.currentTimeMS;
	}

	/**
	 * 	Returns whether the synth is playing a note or not.
	 *	@return TRUE if the synth is currently playing a note.
	 */
	public boolean isNoteOn()
	{
		return this.noteOn;
	}

	/**
	 * 	Applies the ADSR gain envelope to the samples generated by the
	 * 	generator.
	 * 	@param sb The sample buffer to affect
	 */
	public void applyADSREnvelope( final SampleBuffer sb )
	{
		// The current time in the envelope at the start of processing this buffer
		double currentms = this.currentTimeMS - this.noteOnTime;

		// The time of each sample in ms
		final double sampleTime = 1d / sb.getFormat().getSampleRateKHz();

		// Loop through the sample buffer
		for( int i = 0; i < sb.size(); i++ )
		{
			// If the note is set to off and the ADSR value dips to zero,
			// then we can say the note is now definitively off.
			if( this.noteOff && this.currentADSRValue == 0 && this.envelopePhase == 'R' )
			{
				this.noteOn = false;
				this.fireSynthQuiet();
				this.noteOff = false;
			}

			// Outside the attack phase?
			if( currentms > this.attack )
			{
				// Outside the decay phase?
				if( currentms > this.decay+this.attack )
				{
					if( !this.noteOff )
					{
						// In the sustain phase.
						this.currentADSRValue = this.sustain;
						this.envelopePhase = 'S';
					}
					else
					{
						// in the release phase.
						this.currentADSRValue = this.sustain * Math.max( 0f,
							(float)(1d-((currentms - this.noteOffTime) /
									this.release)) );
						this.envelopePhase = 'R';
					}
				}
				else
				{
					// In the decay phase
					this.currentADSRValue = Math.max( this.sustain, 1f - (float)
						((1f - this.sustain) *
								(currentms-this.attack) / this.decay ) );
					this.envelopePhase = 'D';
				}
			}
			else
			{
				// In the attack phase
				this.currentADSRValue = Math.min( 1f,
						(float)(currentms/this.attack) );
				this.envelopePhase = 'A';
			}

			sb.set( i, sb.get(i) * this.currentADSRValue );

			currentms += sampleTime;

//			System.out.println( currentms +" : "+this.currentADSRValue+" ("+this.envelopePhase+")" );
		}

//		System.out.println( this.envelopePhase +" : "+this.currentADSRValue );
	}

	/**
	 * 	Returns the phase of the ADSR envelope
	 *	@return the envelope phase.
	 */
	public char getEnvelopePhase()
	{
		return this.envelopePhase;
	}

	/**
	 * 	Get the ADSR attack time in milliseconds
	 *	@return the attack time in milliseconds
	 */
	public long getAttack()
	{
		return this.attack;
	}

	/**
	 * 	Set the ADSR attack time in milliseconds
	 *	@param attack the attack time in milliseconds
	 */
	public void setAttack( final long attack )
	{
		this.attack = attack;
	}

	/**
	 * 	Get the ADSR decay time in milliseconds
	 *	@return the decay time in milliseconds
	 */
	public long getDecay()
	{
		return this.decay;
	}

	/**
	 * 	Set the ADSR decay time in milliseconds
	 *	@param decay the decay time in milliseconds
	 */
	public void setDecay( final long decay )
	{
		this.decay = decay;
	}

	/**
	 * 	Get the ADSR sustain gain
	 *	@return the sustain gain
	 */
	public float getSustain()
	{
		return this.sustain;
	}

	/**
	 * 	Set the ADSR sustain gain
	 *	@param sustain the sustain gain
	 */
	public void setSustain( final float sustain )
	{
		this.sustain = sustain;
	}

	/**
	 * 	Get the release time in milliseconds
	 *	@return the release time in milliseconds
	 */
	public long getRelease()
	{
		return this.release;
	}

	/**
	 * 	Set the ADSR release time in milliseconds
	 *	@param release the release time in milliseconds
	 */
	public void setRelease( final long release )
	{
		this.release = release;
	}

	/**
	 * 	Add a synth listener to this synth.
	 *	@param sl The synth listener
	 */
	public void addSynthesizerListener( final SynthesizerListener sl )
	{
		this.listeners.add( sl );
	}

	/**
	 * 	remove the synth listener from this synth.
	 *	@param sl The synth listener to remove
	 */
	public void removeSynthesizerListener( final SynthesizerListener sl )
	{
		this.listeners.remove( sl );
	}

	/**
	 * 	Fired when the synth finished the release phase.
	 */
	protected void fireSynthQuiet()
	{
		final ArrayList<SynthesizerListener> l = new ArrayList<SynthesizerListener>( this.listeners );
		for( final SynthesizerListener sl : l )
			sl.synthQuiet();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.Instrument#noteOn(int, double)
	 */
	@Override
	public void noteOn( final int noteNumber, final double velocity )
	{
		System.out.println( "Note on "+noteNumber );
		this.setGain( (int)(velocity * Integer.MAX_VALUE) );
		this.setFrequency( WesternScaleNote.noteToFrequency(noteNumber) );
		this.noteOn();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.Instrument#noteOff(int)
	 */
	@Override
	public void noteOff( final int noteNumber )
	{
		System.out.println( "Note off "+noteNumber );
		this.noteOff();
	}
}
