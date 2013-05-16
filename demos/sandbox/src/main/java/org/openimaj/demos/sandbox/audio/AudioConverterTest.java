/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.conversion.AudioConverter;
import org.openimaj.audio.generation.Synthesizer;
import org.openimaj.vis.audio.AudioFramePlot;

/**
 *	Basic test of the {@link AudioConverter} class.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 May 2013
 */
public class AudioConverterTest
{
	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		final Synthesizer synth = new Synthesizer();
		final AudioFormat o = new AudioFormat( 8, 22.05, 1 );

		System.out.println( "Converting from "+synth.getFormat()+" to "+o );

		final AudioConverter ac = new AudioConverter( synth, o );

		AudioFramePlot.drawChart( ac );
	}
}
