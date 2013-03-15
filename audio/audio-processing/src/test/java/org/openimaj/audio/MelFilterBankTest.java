/**
 *
 */
package org.openimaj.audio;

import java.util.Arrays;

import org.junit.Test;
import org.openimaj.audio.filters.MelFilterBank;

import Jama.Matrix;

/**
 *	Tests for the {@link MelFilterBank}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 4 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class MelFilterBankTest
{
	/**
	 * 	Test for the asMatrix method. Prints out the matrix for testing
	 * 	against some Matlab code
	 */
	@Test
	public void testMatrix()
	{
		final MelFilterBank mfb = new MelFilterBank( 12, 300, 3700 );
		mfb.createFilterBank();
		mfb.setFilterAmplitude( 1 );
		final Matrix m = mfb.asMatrix( 513, 0, 44100 );
		System.out.println( "Matrix "+m.getRowDimension()+"x"+m.getColumnDimension() );
		System.out.println( Arrays.deepToString( m.getArray() ).replace( "], ", "],\n" ) );
	}
}
