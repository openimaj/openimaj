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
package org.openimaj.demos.sandbox.audio;

import java.util.Arrays;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.generation.Oscillator;
import org.openimaj.audio.generation.Synthesizer;
import org.openimaj.audio.generation.Synthesizer.FMOptions;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.vis.audio.AudioFramePlot;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 20 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class FMSynthTest
{
	/**
	 * @param args
	 */
	public static void main( final String args[] )
	{
		final Synthesizer s = new Synthesizer();
		s.setOscillatorType( new Oscillator.FrequencyModulatedOscillator() );
		s.setFrequency( 220 );
		s.setGain( (int)(Integer.MAX_VALUE * 0.1) );
		s.setAttack( 0 );
		s.setDecay( 0 );
		s.setSustain( 1f );
		s.setRelease( 0 );

		final FMOptions o = (FMOptions)s.getOscillator().getOptions();
		o.carrier = new Synthesizer();
		o.modulator = new Synthesizer();
		o.modulator.setFrequency( 88 );
		o.modulatorAmplitude *= 100;

//		final AudioPlayer ap = AudioPlayer.createAudioPlayer( s );
//		ap.run();

		AudioFramePlot.drawChart( 3, true, Arrays.asList(
			new IndependentPair<AudioStream,String>( s, "Output" ),
			new IndependentPair<AudioStream,String>( o.modulator, "Modulator" ),
			new IndependentPair<AudioStream,String>( o.carrier, "Carrier" ) )
		);

		try
		{
			Thread.sleep( 100000000 );
		}
		catch( final InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
