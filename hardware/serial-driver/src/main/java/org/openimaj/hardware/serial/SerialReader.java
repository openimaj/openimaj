/**
 * 
 */
package org.openimaj.hardware.serial;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;

/**
 * 	An RXTX event listener that receives data from the serial port, buffers
 * 	the data, parses the data then calls the listeners for every sentence parsed.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
 */
public class SerialReader implements SerialPortEventListener
{
	/** The input stream from the serial device */
	private InputStream inputStream = null;
	
	/** The parser being used for incoming data */
	private SerialDataParser parser = null;

	/** We use trove to buffer the incoming data */
	private TByteList buffer = new TByteArrayList();
	
	/** The maximum size of a buffer before parsing data */
	private int maxSize = 256;
	
	/** Listeners */
	private List<SerialDataListener> listeners = new ArrayList<SerialDataListener>();
	
	/**
	 *  Default constructor
	 *  @param in
	 *  @param parser 
	 */
	public SerialReader( InputStream in, SerialDataParser parser )
    {
		this.inputStream  = in;
		this.parser  = parser;
    }

	/**
	 *  {@inheritDoc}
	 *  @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
	 */
	@Override
	public void serialEvent( SerialPortEvent event )
	{
		try 
		{
			// Reads all the data from the serial port event (upto a maximum size)
			int data = 0;
			while( (data = inputStream.read()) > -1 && buffer.size() < maxSize ) 
				buffer.add( (byte)data );

			// Parse the data
			String dataString = new String( buffer.toArray(), 0, buffer.size() );
			String[] strings = parser.parse( dataString );
			
			// Keep the left-over parts of the string in the buffer
			String leftOvers = parser.getLeftOverString();
			if( leftOvers != null )
				buffer = buffer.subList( 
						buffer.size()-leftOvers.length(),
						buffer.size() );
			else	buffer.clear();
			
			// Let everyone know we have data!
			fireDataReceived( strings );
		} 
		catch ( IOException e ) 
		{
			// FIXME: RuntimeException? Seems a bit harsh.
			throw new RuntimeException(e);
		}
	}

	/**
	 * 	Add a serial data listener that will be informed of individual tokens
	 * 	that are parsed from the parser.
	 * 
	 *  @param listener The listener
	 */
	public void addSerialDataListener( SerialDataListener listener )
    {
		listeners.add( listener );
    }
	
	/**
	 * 	Remove the given listener from this reader.
	 * 
	 *  @param listener The listener
	 */
	public void removeSerialDataListener( SerialDataListener listener )
	{
		listeners.remove( listener );
	}
	
	/**
	 * 	Fire multiple events: one for each parsed string.
	 *  @param strings The strings parsed from the parser.
	 */
	protected void fireDataReceived( String[] strings )
	{
		for( String s : strings )
			for( SerialDataListener listener: listeners )
				listener.dataReceived( s );
	}
}
