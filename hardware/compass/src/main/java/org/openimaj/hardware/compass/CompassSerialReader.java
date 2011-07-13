/**
 * 
 */
package org.openimaj.hardware.compass;

import gnu.io.SerialPort;

import org.openimaj.hardware.serial.SerialDataListener;
import org.openimaj.hardware.serial.SerialDevice;

/**
 * 	This class is used to read data from the Ocean Server serial compass.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Jul 2011
 */
public class CompassSerialReader implements Runnable
{
	/** The port name on which the compass is putting its data */
	private String portName = null;
	
	/**
	 * 	Constructor that takes the serial port name on which the
	 * 	compass is putting its data.
	 * 
	 *  @param portName The port name.
	 */
	public CompassSerialReader( String portName )
    {
		this.portName = portName;
    }
	
	/**
	 *  @inheritDoc
	 *  @see java.lang.Runnable#run()
	 */
	@Override
    public void run()
    {
		try
        {
	        String firstPort = portName;
	        System.out.println( "Opening "+firstPort );
	        
	        // This is the standard Ocean Server compass configuration
	        SerialDevice sd = new SerialDevice( firstPort, 19200, 
	        		SerialPort.DATABITS_8, SerialPort.STOPBITS_1, 
	        		SerialPort.PARITY_NONE );
	        
	        sd.addSerialDataListener( new SerialDataListener()
			{
				public void dataReceived( String data )
				{
					// System.out.println( "Data: '"+data.trim()+"'" );
					
					CompassData cd = OS5000_0x01_Parser.parseLine( data.trim() );
					System.out.println( cd );
				}
			});
        }
        catch( Exception e )
        {
	        e.printStackTrace();
        }		
		
    }

	/**
	 *  @param args
	 */
	public static void main( String[] args )
	{
		new CompassSerialReader("/dev/ttyUSB0").run();
	}
}
