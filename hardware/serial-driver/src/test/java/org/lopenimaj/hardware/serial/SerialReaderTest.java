/**
 * 
 */
package org.lopenimaj.hardware.serial;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.hardware.serial.SerialDataListener;
import org.openimaj.hardware.serial.SerialDataParser;
import org.openimaj.hardware.serial.SerialReader;

/**
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 26 Sep 2012
 * @version $Author$, $Revision$, $Date$
 */
public class SerialReaderTest
{
	/**
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testSerialReader() throws UnsupportedEncodingException
	{
		// This is the data stream from our simulated serial port.
		byte[] bytes = new byte[] {43, 49, 10, 43, 50, 10, 43, 51, 10, 43, 52};
		InputStream is = new ByteArrayInputStream( bytes );
		
		// This is the parser we'll use
		SerialDataParser parser = new SerialDataParser()
		{
			private String leftOvers;
			
			@Override
			public String[] parse( String data )
			{
				Pattern p = Pattern.compile( "(.*)\n" );
				Matcher m = p.matcher( data );
				int count = 0;
				ArrayList<String> splits = new ArrayList<String>();
				while( m.find() )
				{
					splits.add( m.group(1) );
					count += m.group().length();
				}
				
				leftOvers = data.substring( count );
				String[] dataSplit = splits.toArray( new String[splits.size()] );
				
				return dataSplit;
			}
			
			@Override
			public String getLeftOverString()
			{
				return leftOvers;
			}
		};
		
		// Construct a serial reader that reads from the string above
		SerialReader sr = new SerialReader( is, parser  );
		sr.setMaxBufferSize( 7 );

		// We'll print out the data the parser is parsing.
		final ArrayList<String> dataItems = new ArrayList<String>();
		sr.addSerialDataListener( new SerialDataListener()
		{
			private int count = 0;
			
			@Override
			public void dataReceived( String data )
			{
				System.out.println( count+++" : '"+data+"'" );
				dataItems.add( data );
			}
		} );
		
		// The event isn't actually used. The serial reader reads from
		// the InputStream that's passed into the constructor.
		
		// First n bytes
		sr.serialEvent( null );
		
		// Next n bytes
		sr.serialEvent( null );

		// Next n bytes
		sr.serialEvent( null );

		// Next n bytes
		sr.serialEvent( null );

		// We should have a list containing {'+1','+2','+3','+4'}
		Assert.assertEquals( 4, dataItems.size() );
		Assert.assertEquals( "+1", dataItems.get( 0 ) );
		Assert.assertEquals( "+2", dataItems.get( 1 ) );
		Assert.assertEquals( "+3", dataItems.get( 2 ) );
		Assert.assertEquals( "+4", dataItems.get( 3 ) );
	}
}
