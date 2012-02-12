package org.openimaj.content.slideshow;

import java.io.IOException;
import java.util.List;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

/**
 * Utility class for getting a preferred camera.
 * 
 * Set the PREFERRED_DEVICE_NAME to the name of the device,
 * then the other methods will return that device/capture object.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class CameraSelector {
	/**
	 * The name of the preferred device
	 */
	public static String PREFERRED_DEVICE_NAME = "Logitech Camera";
	
	/**
	 * @return the preferred device
	 * @throws IOException
	 */
	public static Device getPreferredVideoCaptureDevice() throws IOException {
		List<Device> devices = VideoCapture.getVideoDevices();
		
		for (Device d : devices) {
			if (d.getNameStr().contains(PREFERRED_DEVICE_NAME)) { 
				return d;
			}
		}
		
		if (devices.size() > 0)
			return devices.get(0);
		
		return null;
	}
	
	/**
	 * Get a {@link VideoCapture} representing the preferred device.
	 * @param width the desired capture width
	 * @param height the desired capture height
	 * @return the VideoCapture for the preferred device
	 * @throws IOException
	 */
	public static VideoCapture getPreferredVideoCapture(int width, int height) throws IOException {
		return new VideoCapture(width, height, getPreferredVideoCaptureDevice());
	}
}
