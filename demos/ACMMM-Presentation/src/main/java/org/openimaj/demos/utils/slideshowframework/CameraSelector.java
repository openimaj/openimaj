package org.openimaj.demos.utils.slideshowframework;

import java.io.IOException;
import java.util.List;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

public class CameraSelector {
	public static Device getPreferredVideoCaptureDevice() throws IOException {
		List<Device> devices = VideoCapture.getVideoDevices();
		
		for (Device d : devices) {
			if (d.getNameStr().contains("Logitech Camera")) { 
				return d;
			}
		}
		
		if (devices.size() > 0)
			return devices.get(0);
		
		return null;
	}
	
	public static VideoCapture getPreferredVideoCapture(int width, int height) throws IOException {
		return new VideoCapture(width, height, getPreferredVideoCaptureDevice());
	}
}
