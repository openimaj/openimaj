package org.openimaj.video;

import java.util.List;

import openimajgrabber.Device;
import openimajgrabber.DeviceList;
import openimajgrabber.OpenIMAJGrabber;

import org.bridj.Pointer;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

public class VideoGrabber extends Video<MBFImage> {
	OpenIMAJGrabber grabber;
	MBFImage frame;
	private int width;
	private int height;
	
	public VideoGrabber() {
		fps = 25;
		grabber = new OpenIMAJGrabber();
		startSession(640, 480);
	}
	
	public VideoGrabber(Device device) {
		fps = 25;
		grabber = new OpenIMAJGrabber();
		startSession(640, 480, device);
	}
	
	public static List<Device> getVideoDevices() {
		DeviceList list = OpenIMAJGrabber.getVideoDevices().get();
		
		return list.asArrayList();
	}
	
	public boolean startSession(int width, int height, Device device) {
		System.out.println("startSession()");
		if (grabber.startSession(width, height, Pointer.pointerTo(device))) {
			this.width = grabber.getWidth();
			this.height = grabber.getHeight();
			frame = new MBFImage(width, height, ColourSpace.RGB);
			
			return true;
		} 
		return false;
	}
	
	public boolean startSession(int width, int height) {
		System.out.println("startSession()");
		if (grabber.startSession(width, height)) {
			this.width = grabber.getWidth();
			this.height = grabber.getHeight();
			frame = new MBFImage(width, height, ColourSpace.RGB);
			
			return true;
		} 
		return false;
	}
	
	public void stopSession() {
		grabber.stopSession();
	}

	@Override
	public MBFImage getCurrentFrame() {
		System.out.println("getCurrentFrame()");
		return frame;
	}

	@Override
	public MBFImage getNextFrame() {
		System.out.println("getNextFrame()");
		grabber.nextFrame();
		
		Pointer<Byte> data = grabber.getImage();
		if (data == null) {
			return frame;
		}
		byte [] d = data.getBytes(width * height * 3);
		
		for (int i=0, y=0; y<height; y++) {
			for (int x=0; x<width; x++, i+=3) {
				int red = d[i+0] & 0xFF;
				int green = d[i+1] & 0xFF;
				int blue = d[i+2] & 0xFF;
				(frame.bands.get(0)).pixels[y][x] = red   / 255.0F;
				(frame.bands.get(1)).pixels[y][x] = green / 255.0F;
				(frame.bands.get(2)).pixels[y][x] = blue  / 255.0F;
			}
		}
		
		return frame;
	}
}
