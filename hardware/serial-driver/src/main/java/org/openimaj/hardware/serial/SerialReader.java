/**
 *
 */
package org.openimaj.hardware.serial;

import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;

/**
 * An event listener that receives data from the serial port, buffers the data,
 * parses the data then calls the listeners for every sentence parsed.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @created 12 Jul 2011
 */
public class SerialReader implements SerialPortEventListener, Closeable
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

	private boolean closed = false;

	/**
	 * Default constructor
	 *
	 * @param in
	 * @param parser
	 */
	public SerialReader(InputStream in, SerialDataParser parser)
	{
		this.inputStream = in;
		this.parser = parser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialEvent(SerialPortEvent event)
	{
		if (closed)
			return;

		if (event != null && !event.isRXCHAR())
			return;

		try
		{
			// Reads all the data from the serial port event (upto a maximum
			// size)
			int data = 0;
			while (buffer.size() < maxSize && (data = inputStream.read()) > -1)
				buffer.add((byte) data);

			// Parse the data
			final String dataString = new String(buffer.toArray(), 0, buffer.size());
			final String[] strings = parser.parse(dataString);
			final String leftOvers = parser.getLeftOverString();

			// If we've got to the end of the stream, we'll simply fire the
			// events
			// for the strings that are parsed and the left overs.
			if (data == -1)
			{
				if (strings.length > 0)
					fireDataReceived(strings);
				if (leftOvers.length() > 0)
					fireDataReceived(new String[] { leftOvers });
				buffer.clear();
			}
			else
			{
				// Keep the left-over parts of the string in the buffer
				if (leftOvers != null)
					buffer = buffer.subList(
							buffer.size() - leftOvers.length(),
							buffer.size());
				else
					buffer.clear();

				// Let everyone know we have data!
				fireDataReceived(strings);
			}
		} catch (final IOException e)
		{
			// FIXME: RuntimeException? Seems a bit harsh.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a serial data listener that will be informed of individual tokens
	 * that are parsed from the parser.
	 *
	 * @param listener
	 *            The listener
	 */
	public void addSerialDataListener(SerialDataListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove the given listener from this reader.
	 *
	 * @param listener
	 *            The listener
	 */
	public void removeSerialDataListener(SerialDataListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Fire multiple events: one for each parsed string.
	 *
	 * @param strings
	 *            The strings parsed from the parser.
	 */
	protected void fireDataReceived(String[] strings)
	{
		for (final String s : strings)
			for (final SerialDataListener listener : listeners)
				listener.dataReceived(s);
	}

	/**
	 * Set the size of the buffer to use. The buffer size must be larger than
	 * any expected data item that you are wanting to parse. If your sentences
	 * can be up to 128 bytes, then the buffer should be at least 128 bytes. It
	 * can be larger.
	 *
	 * @param maxSize
	 *            The size of the buffer to use.
	 */
	public void setMaxBufferSize(int maxSize)
	{
		this.maxSize = maxSize;
	}

	@Override
	public void close() throws IOException {
		this.closed = true;
	}
}
