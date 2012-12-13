/**
 *
 */
package org.openimaj.audio;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.audio.features.MFCC;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCCTest
{

	/**
	 *	@throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 *
	 */
	@Test
	public void test()
	{
		final MFCC mfcc = new MFCC( 40, 20, 16000 );

		final double[] dummyData = new double[1024];
		for( int i = 0; i < 1024; i++ )
			dummyData[i] = i;

		mfcc.calculateMFCC( dummyData, new AudioFormat( 8, 44.1, 1 ) );
	}
}
