/**
 * 
 */
package org.openimaj.hardware.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import org.bridj.Platform;

/**
 * 	Serial device driver. Uses RXTX library underneath. The native parts of
 * 	the RXTX library are published to the Maven repository as a JAR and
 * 	are extracted and the java.library.path property is flushed and reset.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
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
	
	// Static block that loads the native libraries
	static 
	{
		// The package in the JAR where we've stored the native libs
		String libprefix = "/org/openimaj/driver/serial/native/";

		// Try to find out if we've got a library to load
		String libraryResource = null;
		for( String s : getEmbeddedLibraryResource("rxtxSerial") ) 
		{
			URL r = SerialDevice.class.getResource( libprefix + s );
			if( r != null ) 
			{
				libraryResource = libprefix + s;
				break;
			}
		}

		// Stop if we can't find the library
		if( libraryResource == null ) 
			throw new RuntimeException("Unable to load platform library");

		String directory = null;
		try 
		{
			// Extract the library from the JAR and find the directory into which it was put
			// ... it's this we'll add as the library path
			File file = extractEmbeddedLibraryResource(libraryResource);
			directory = file.getAbsoluteFile().getParent();
		} 
		catch (IOException e) 
		{
			throw new RuntimeException("Error unpacking platform library");
		}

		// http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
		try
        {
	        // Flush the paths in the Classloader
	        System.setProperty( "java.library.path", directory );
	        
	        Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
	        fieldSysPath.setAccessible( true );
	        fieldSysPath.set( null, null );
        }
        catch( SecurityException e )
        {
	        e.printStackTrace();
        }
        catch( IllegalArgumentException e )
        {
	        e.printStackTrace();
        }
        catch( NoSuchFieldException e )
        {
	        e.printStackTrace();
        }
        catch( IllegalAccessException e )
        {
	        e.printStackTrace();
        }
	}

	/**
	 * 	Returns a singleton list containing the library name for
	 * 	the given platform.
	 *  
	 *  @param name The name of the library.
	 *  @return The library name in a singleton list.
	 */
	static Collection<String> getEmbeddedLibraryResource(String name) 
	{
		if( Platform.isWindows() )
			return Collections.singletonList((Platform.is64Bits() ? "win64/" : "win32/") + name + ".dll");
		
		if( Platform.isMacOSX() )
		{
			String generic = "darwin_universal/lib" + name + ".jnilib";
			
			if (Platform.isAmd64Arch())
					return Arrays.asList("darwin_x64/lib" + name + ".jnilib", generic);
			else	return Collections.singletonList(generic);
		}
		
		if( Platform.isLinux() )
			return Collections.singletonList(
					(Platform.is64Bits() ? "linux_x64/lib" : "linux_x86/lib") 
						+ name + ".so");

		throw new RuntimeException("Platform not supported ! " +
				"(os.name='" + System.getProperty("os.name") + 
				"', os.arch='" + System.getProperty("os.arch") + "')");
	}

	/**
	 * 	Copies the file from the resource into a temporary file.
	 * 
	 *  @param libraryResource The library resource to copy
	 *  @return The file where the resource was copied to.
	 *  @throws IOException If the file could not be read or written to.
	 */
	static File extractEmbeddedLibraryResource( String libraryResource ) 
		throws IOException 
	{
		File libdir = File.createTempFile( new File(libraryResource).getName(), null );
		libdir.delete();
		libdir.mkdir();
		libdir.deleteOnExit();
		
		File libFile = new File(libdir, new File(libraryResource).getName());
		libFile.deleteOnExit();
		
		InputStream in = SerialDevice.class.getResourceAsStream(libraryResource);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));
		
		int len;
		byte[] b = new byte[8196];
		while ((len = in.read(b)) > 0)
			out.write(b, 0, len);
		
		out.close();
		in.close();

		System.out.println( "Using library "+libraryResource );
		
		return libFile;
	}
	
	/**
	 * 	Constructor that takes the port name to connect to and the rate at which
	 * 	to connect. The data rate will be set to 19,200 with 8 data bits, 1 stop bit
	 * 	and no parity.
	 * 
	 *  @param portName The port name to connect to.
	 *  @throws Exception
	 */
	public SerialDevice( String portName ) throws Exception 
	{
		this( portName, 4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
	}

	/**
	 * 	Complete constructor that takes all the information required to connect to 
	 * 	a port.
	 * 
	 *  @param portName The port name to connect to.
	 *  @param dataRate The data rate to read from the port.
	 *  @param dataBits The number of data bits
	 *  @param stopBits The number of stop bits
	 *  @param parity The bit parity
	 *  @throws Exception 
	 */
	public SerialDevice( String portName, int dataRate, int dataBits, int stopBits, int parity )
		throws Exception
	{
		// Connect to the given port name with a 2 second timeout. 
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );
		CommPort commPort = portIdentifier.open( this.getClass().getName(), 2000 );

		// Set the serial port information
		serialPort = (SerialPort) commPort;
		serialPort.setSerialPortParams( dataRate, dataBits, stopBits, parity );

		// Get the input and output streams from and to the serial port.
		outputStream = serialPort.getOutputStream();		
		inputStream  = serialPort.getInputStream();
		
		// Set up our data listener
		regexParser  = new RegExParser( regex );
		serialReader = new SerialReader( inputStream, regexParser );
		serialReader.addSerialDataListener( this );
		serialPort.addEventListener( serialReader );
		serialPort.notifyOnDataAvailable(true);
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		serialPort.removeEventListener();
		serialPort.close();
	    super.finalize();
	}
	
	/**
	 *  Close the connection to the serial port.
	 */
	public void close()
	{
		serialPort.removeEventListener();
		serialPort.close();
	}
	
	/**
	 * 	Add the given {@link SerialDataListener} to the listener list.
	 *  @param sdl The {@link SerialDataListener} to add.
	 */
	public void addSerialDataListener( SerialDataListener sdl )
	{
		listeners.add( sdl );
	}
	
	/**
	 * 	Remove the given {@link SerialDataListener} from the listener list
	 *  @param sdl The {@link SerialDataListener} to remove.
	 */
	public void removeSerialDataListener( SerialDataListener sdl )
	{
		listeners.remove( sdl );
	}
	
	/**
	 * 	Fires the serial data event when data is received on the port.
	 *  @param data The data that was received
	 */
	protected void fireSerialDataEvent( String data )
	{
		for( SerialDataListener listener: listeners )
			listener.dataReceived( data );
	}

	/**
     *  Returns the regular expression being used to split incoming strings.
     *  @return the regular expression being used to split incoming strings.
     */
    public String getRegex()
    {
    	return regex;
    }

	/**
     *  Set the regular expression to use to split incoming strings.
     *  @param regex the regex to split incoming strings
     */
    public void setRegex( String regex )
    {
    	this.regex = regex;
    	this.regexParser.setRegEx( regex );
    }

	/**
     *  Returns the input stream for this device.
     *  @return the input stream
     */
    public InputStream getInputStream()
    {
    	return inputStream;
    }

	/**
     *  Returns the output stream for this device.
     *  @return the output stream
     */
    public OutputStream getOutputStream()
    {
    	return outputStream;
    }

    /**
     *  {@inheritDoc}
     *  @see org.openimaj.hardware.serial.SerialDataListener#dataReceived(java.lang.String)
     */
	@Override
	public void dataReceived( String data )
    {
		fireSerialDataEvent( data );
    }
	
	/**
	 * @return A HashSet containing the CommPortIdentifier for all serial ports
	 *         that are not currently being used.
	 */
	public static HashSet<CommPortIdentifier> getAvailableSerialPorts()
	{
		HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
		Enumeration<?> thePorts = CommPortIdentifier.getPortIdentifiers();
		while( thePorts.hasMoreElements() )
		{
			CommPortIdentifier com = (CommPortIdentifier)thePorts.nextElement();
			switch (com.getPortType())
			{
			case CommPortIdentifier.PORT_SERIAL:
				try
				{
					CommPort thePort = com.open( "CommUtil", 50 );
					thePort.close();
					h.add( com );
				}
				catch( PortInUseException e )
				{
					System.out.println( "Port, " + com.getName()
					        + ", is in use." );
				}
				catch( Exception e )
				{
					System.err.println( "Failed to open port " + com.getName() );
					e.printStackTrace();
				}
			}
		}
		return h;
	}
}
