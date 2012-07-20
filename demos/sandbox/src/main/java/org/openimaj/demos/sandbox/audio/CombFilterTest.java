/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 27 Apr 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class CombFilterTest
{
	/**
	 * 	Main
	 *	@param args CLAs
	 */
	public static void main( String[] args )
	{
		XuggleAudio xa = new XuggleAudio( CombFilterTest.class
				.getResource("Devaney's_Goat_Solo_Tinwhistle.wav") );
		
		System.out.println( xa.getFormat() );
	}
}
