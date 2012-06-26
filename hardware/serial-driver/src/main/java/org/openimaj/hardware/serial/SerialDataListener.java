/**
 * 
 */
package org.openimaj.hardware.serial;

import java.util.EventListener;

/**
 * 	An event listener for objects that wish to be informed
 * 	of data arriving on a serial port.	
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
 */
public interface SerialDataListener extends EventListener
{
	/**
	 * 	Called when data is received on a serial port.
	 *  @param data The data that was received as a string.
	 */
	public void dataReceived( String data );
}
