/**
 * 
 */
package org.openimaj.hardware.gps;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.gps.NMEAParser;
import org.openimaj.hardware.gps.NMEAMessage;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 12 Jul 2011
 */
public class NMEAParserTest
{
	private NMEAParser parser = null;
	
	@Before
	public void setup()
	{
		parser = new NMEAParser();
	}
	
	@Test
	public void testSentenceParsing()
	{
		String test = "$GPGGA,141543.000,5056.1979,N,00123.7854,W,1,05,3.0,84.0,M,4.6,M,,0000*79";
		List<NMEAMessage> msg = parser.parseString( test );
		
		for( NMEAMessage m : msg )
			System.out.println( "Got message: "+m );

		Assert.assertTrue( msg.size() == 1 );
		
	}
}
