package org.openimaj.video;

import java.util.List;

import openimajgrabber.Device;

import org.openimaj.image.MBFImage;

public class Test {
	public static void main(String [] args) {
		List<Device> devices = VideoGrabber.getVideoDevices();
		
		VideoGrabber grabber1 = new VideoGrabber(devices.get(0));
		VideoDisplay<MBFImage> disp1 = VideoDisplay.createVideoDisplay(grabber1);
		
		if (devices.size() > 1) {
			VideoGrabber grabber2 = new VideoGrabber(devices.get(1));
			VideoDisplay<MBFImage> disp2 = VideoDisplay.createVideoDisplay(grabber2);
			disp2.getScreen().setLocation(disp1.getScreen().getWidth(), disp2.getScreen().getLocation().y);
		}
	}
}
