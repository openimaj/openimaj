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
/**
 *
 */
package org.openimaj.hardware.compass;

import jssc.SerialPort;

import org.openimaj.hardware.serial.SerialDataListener;
import org.openimaj.hardware.serial.SerialDevice;

/**
 * This class is used to read data from the Ocean Server serial compass.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @created 13 Jul 2011
 */
public class CompassSerialReader implements Runnable
{
	/** The port name on which the compass is putting its data */
	private String portName = null;

	private CompassData latestData = null;

	/**
	 * Constructor that takes the serial port name on which the compass is
	 * putting its data.
	 *
	 * @param portName
	 *            The port name.
	 */
	public CompassSerialReader(String portName)
	{
		this.portName = portName;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			final String firstPort = portName;
			System.out.println("Opening " + firstPort);

			// This is the standard Ocean Server compass configuration
			final SerialDevice sd = new SerialDevice(firstPort, 19200,
					SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			sd.addSerialDataListener(new SerialDataListener()
			{
				@Override
				public void dataReceived(String data)
				{
					latestData = OS5000_0x01_Parser.parseLine(data.trim());
				}
			});
		} catch (final Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * @return The most recent compass data
	 */
	public CompassData getCompassData()
	{
		return latestData;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new CompassSerialReader("/dev/ttyUSB0").run();
	}
}
