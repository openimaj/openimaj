/**
 * 
 */
package org.openimaj.audio;

import org.openimaj.audio.util.AudioUtils;

/**
 *	Represents an audio device and its capabilities. Use
 *	{@link AudioUtils#getDevices()} to get a list of supported devices.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 26 Nov 2011
 */
public class AudioDevice
{
	/** The audio device name */
	public String displayName;
	
	/** The name of the device */
	public String deviceName;
	
	/**
	 * 	Create a new audio device.
	 * 
	 *	@param deviceName The device name
	 *	@param displayName The display name
	 */
	public AudioDevice( String deviceName, String displayName )
	{
		this.deviceName = deviceName;
		this.displayName = displayName;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return deviceName+" ("+displayName+")";
	}
}
