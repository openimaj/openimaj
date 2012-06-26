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
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
