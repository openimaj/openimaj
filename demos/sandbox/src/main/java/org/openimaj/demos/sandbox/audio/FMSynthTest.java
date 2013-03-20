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
	 *
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
