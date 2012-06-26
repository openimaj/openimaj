/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hardware.gps;

import gnu.io.SerialPort;

import java.util.List;

import org.openimaj.hardware.serial.SerialDataListener;
import org.openimaj.hardware.serial.SerialDevice;

/**
 * 	This class reads GPS data from a serial port and makes available
 * 	the latest location information through its getters.
 * 
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	
 * 	@created 11 Jul 2011
 */
public class GPSSerialReader implements Runnable
{
	private double lng = 0;
	private double lat = 0;
	private int nSats = 0;
	private String portName = null; 
	
	/**
	 * 	Constructor that takes the name of the serial port onto which
	 * 	the GPS is transmitting data.
	 * 
	 * 
	 *  @param portName The name of the port
	 */
	public GPSSerialReader( String portName)
	{
		this.portName = portName;
	}

	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
        {
	        String firstPort = portName;
	        System.out.println( "Opening "+firstPort );
	        
	        // This is the standard GPS configuration
	        SerialDevice sd = new SerialDevice( firstPort, 4800, SerialPort.DATABITS_8, 
	        		SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
	        
	        sd.addSerialDataListener( new SerialDataListener()
			{
	        	private NMEAParser parser = new NMEAParser();
	        	
				@Override
				public void dataReceived( String data )
				{
					System.out.println( "Data: '"+data.trim()+"'" );
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
	
	/**
	 * @return The latitude
	 */
	public double getLatitude()
	{
		return lat;
	}
	
	/**
	 * @return The longitude
	 */
	public double getLongitude()
	{
		return lng;
	}
	
	/**
	 * @return The number of satellites
	 */
	public int getNumberOfSatellites()
	{
		return nSats;
	}
	
    /**
     * Test
     * @param args
     */
    static public void main( String[] args )
    {
    	if (args.length == 1) {
    		new GPSSerialReader(args[0]).run();
    	} else {
    		new GPSSerialReader("/dev/ttyUSB0").run();
    	}
    }
}
