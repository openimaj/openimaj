/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import org.openimaj.audio.generation.Synthesizer;
import org.openimaj.vis.audio.AudioFramePlot;


/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class FixSynth
{
	/**
	 *	@param args
	 */
	public static void main( final String args[] )
	{
		final Synthesizer s = new Synthesizer();
		s.setAttack( 0 );
		s.setDecay( 0 );
		s.setRelease( 100 );
		s.noteOn( 60, 1f );
		s.noteOff();

		AudioFramePlot.drawChart( s );
	}
}
