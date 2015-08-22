/**
 *
 */
package org.openimaj.hardware.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

/**
 * Serial device driver. Uses RXTX library underneath. The native parts of the
 * RXTX library are published to the Maven repository as a JAR and are extracted
 * and the java.library.path property is flushed and reset.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @created 12 Jul 2011
 */
public class SerialDevice implements SerialDataListener
{
	/** The RXTX serial port we'll be reading from */
	private SerialPort serialPort = null;

	/** Listeners for data events coming from the serial port */
	private List<SerialDataListener> listeners = new ArrayList<SerialDataListener>();

	/** The regular expression used to split incoming data for the listeners */
	private String regex = "\n";

	/** The input stream for the port */
	private InputStream inputStream = null;

	/** The output stream for the port */
	private OutputStream outputStream = null;

	/** The serial reader used to buffer and parse incoming data */
	private SerialReader serialReader = null;

	/** The parser being used to parse incoming data */
	private RegExParser regexParser = null;

	/**
	 * Constructor that takes the port name to connect to and the rate at which
	 * to connect. The data rate will be set to 19,200 with 8 data bits, 1 stop
	 * bit and no parity.
	 *
	 * @param portName
	 *            The port name to connect to.
	 * @throws Exception
	 */
	public SerialDevice(String portName) throws Exception
	{
		this(portName, 4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	}

	/**
	 * Complete constructor that takes all the information required to connect
	 * to a port.
	 *
	 * @param portName
	 *            The port name to connect to.
	 * @param dataRate
	 *            The data rate to read from the port.
	 * @param dataBits
	 *            The number of data bits
	 * @param stopBits
	 *            The number of stop bits
	 * @param parity
	 *            The bit parity
	 * @throws Exception
	 */
	public SerialDevice(String portName, int dataRate, int dataBits, int stopBits, int parity)
			throws Exception
	{
		// Set the serial port information
		serialPort = new SerialPort(portName);
		serialPort.openPort();
		serialPort.setParams(dataRate, dataBits, stopBits, parity);

		// Get the input and output streams from and to the serial port.
		outputStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				try {
					serialPort.writeByte((byte) b);
				} catch (final SerialPortException e) {
					throw new IOException(e);
				}
			}

			@Override
			public void write(byte[] b) throws IOException {
				try {
					serialPort.writeBytes(b);
				} catch (final SerialPortException e) {
					throw new IOException(e);
				}
			}
		};
		inputStream = new InputStream() {
			@Override
			public int read() throws IOException {
				while (true) {
					try {
						if (!serialPort.isOpened())
							return -1;

						return serialPort.readBytes(1, 100)[0];
					} catch (final SerialPortTimeoutException e) {
						// ignore and try again
					} catch (final SerialPortException e) {
						if (e.getMessage().contains("Port not opened"))
							return -1;
						throw new IOException(e);
					}
				}
			}
		};

		// Set up our data listener
		regexParser = new RegExParser(regex);
		serialReader = new SerialReader(inputStream, regexParser);
		serialReader.addSerialDataListener(this);
		serialPort.addEventListener(serialReader, SerialPort.MASK_RXCHAR);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		if (serialPort.isOpened()) {
			serialReader.close();
			serialPort.removeEventListener();
			serialPort.closePort();
		}
		super.finalize();
	}

	/**
	 * Close the connection to the serial port.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		try {
			serialReader.close();
			serialPort.removeEventListener();
			serialPort.closePort();
		} catch (final SerialPortException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Add the given {@link SerialDataListener} to the listener list.
	 *
	 * @param sdl
	 *            The {@link SerialDataListener} to add.
	 */
	public void addSerialDataListener(SerialDataListener sdl)
	{
		listeners.add(sdl);
	}

	/**
	 * Remove the given {@link SerialDataListener} from the listener list
	 *
	 * @param sdl
	 *            The {@link SerialDataListener} to remove.
	 */
	public void removeSerialDataListener(SerialDataListener sdl)
	{
		listeners.remove(sdl);
	}

	/**
	 * Fires the serial data event when data is received on the port.
	 *
	 * @param data
	 *            The data that was received
	 */
	protected void fireSerialDataEvent(String data)
	{
		for (final SerialDataListener listener : listeners)
			listener.dataReceived(data);
	}

	/**
	 * Returns the regular expression being used to split incoming strings.
	 *
	 * @return the regular expression being used to split incoming strings.
	 */
	public String getRegex()
	{
		return regex;
	}

	/**
	 * Set the regular expression to use to split incoming strings.
	 *
	 * @param regex
	 *            the regex to split incoming strings
	 */
	public void setRegex(String regex)
	{
		this.regex = regex;
		this.regexParser.setRegEx(regex);
	}

	/**
	 * Returns the input stream for this device.
	 *
	 * @return the input stream
	 */
	public InputStream getInputStream()
	{
		return inputStream;
	}

	/**
	 * Returns the output stream for this device.
	 *
	 * @return the output stream
	 */
	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.hardware.serial.SerialDataListener#dataReceived(java.lang.String)
	 */
	@Override
	public void dataReceived(String data)
	{
		fireSerialDataEvent(data);
	}

	/**
	 * @return A HashSet containing the identifier for all serial ports
	 */
	public static HashSet<String> getSerialPorts()
	{
		final HashSet<String> ports = new HashSet<String>();
		for (final String s : SerialPortList.getPortNames()) {
			ports.add(s);
		}
		return ports;
	}
}
