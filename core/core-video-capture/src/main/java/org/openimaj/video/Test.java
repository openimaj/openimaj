package org.openimaj.video;

import java.util.List;

import openimajgrabber.Device;

import org.openimaj.image.MBFImage;

public class Test {
	public static void main(String [] args) {
		List<Device> devices = VideoCapture.getVideoDevices();
		System.out.println(devices);

//		VideoGrabber grabber = new VideoGrabber();
//		VideoDisplay.createVideoDisplay(grabber);
		
		VideoCapture grabber1 = new VideoCapture(640, 480, devices.get(0));
		VideoDisplay<MBFImage> disp1 = VideoDisplay.createVideoDisplay(grabber1);
		
		if (devices.size() > 1) {
			VideoCapture grabber2 = new VideoCapture(320, 240, devices.get(1));
			VideoDisplay<MBFImage> disp2 = VideoDisplay.createVideoDisplay(grabber2);
			disp2.getScreen().setLocation(disp1.getScreen().getWidth(), disp2.getScreen().getLocation().y);
		}
	}
}
