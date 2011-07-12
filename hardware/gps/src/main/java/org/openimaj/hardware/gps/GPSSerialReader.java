package org.openimaj.hardware.gps;
/**
 * 
 */


import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.util.HashSet;
import java.util.List;

import org.openimaj.hardware.gps.NMEAParser;
import org.openimaj.hardware.gps.NMEAMessage;
import org.openimaj.hardware.serial.SerialDataListener;
import org.openimaj.hardware.serial.SerialDevice;

/**
 * 	This class reads GPS data from a serial port and makes available
 * 	the latest location information through its getters.
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 * 	@version $Author$, $Revision$, $Date$
 * 	@created 11 Jul 2011
 */
public class GPSSerialReader implements Runnable
{
	private double lng = 0;
	private double lat = 0;
	private int nSats = 0;
	
	public GPSSerialReader()
	{
	}

	public void run()
	{
		try
        {
	        HashSet<CommPortIdentifier> p = SerialDevice.getAvailableSerialPorts();
	        
	        System.out.println( "Available ports: ");
	        for( CommPortIdentifier port : p )
	        	System.out.println( "    - "+port.getName() );
	        
	        String firstPort = "/dev/ttyUSB0"; //p.iterator().next().getName();
	        System.out.println( "Opening "+firstPort );
	        
	        // This is the standard GPS configuration
	        SerialDevice sd = new SerialDevice( firstPort, 4800, SerialPort.DATABITS_8, 
	        		SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
	        
	        sd.addSerialDataListener( new SerialDataListener()
			{
	        	private NMEAParser parser = new NMEAParser();
	        	
				public void dataReceived( String data )
				{
					// System.out.println( "Data: '"+data.trim()+"'" );
					List<NMEAMessage> m = parser.parseString( data );
					
					if( m.size() > 0 )
					{
						System.out.println( m );
						
						for( NMEAMessage mm : m )
						{
							if( mm.get("lat") != null )
								lat = (Double)mm.get("lat");
							if( mm.get("lng") != null )
								lng = (Double)mm.get("lng");
							if( mm.get("numberOfSatellites") != null )
								nSats = (Integer)mm.get("numberOfSatellites");
						}
					}
				}
			});
        }
        catch( Exception e )
        {
	        e.printStackTrace();
        }		
	}
	
	public double getLatitude()
	{
		return lat;
	}
	
	public double getLongitude()
	{
		return lng;
	}
	
	public int getNumberOfSatellites()
	{
		return nSats;
	}
	
    static public void main( String[] args )
    {
    	new GPSSerialReader().run();
    }
}
