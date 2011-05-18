package org.openimaj.video;

import java.util.List;

import openimajgrabber.Device;
import openimajgrabber.DeviceList;
import openimajgrabber.OpenIMAJGrabber;

import org.bridj.Pointer;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 * VideoCapture is a type of {@link Video} that can capture
 * live video streams from a webcam or other video device.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class VideoCapture extends Video<MBFImage> {
	private OpenIMAJGrabber grabber;
	private MBFImage frame;
	private int width;
	private int height;
	private boolean isStopped = true;
	
	/**
	 * Construct a VideoCapture instance with the requested
	 * width and height. The default video device will be
	 * used. The actual height and width of the captured
	 * frames may not equal the requested size if the
	 * underlying platform-specific grabber is not able to
	 * honor the request. The actual size can be inspected
	 * through the {@link #getWidth()} and {@link getHeight()}
	 * methods.  
	 * 
	 * @param width the requested video width
	 * @param height the requested video height
	 */
	public VideoCapture(int width, int height) {
		fps = 25;
		grabber = new OpenIMAJGrabber();
		startSession(width, height);
	}
	
	/**
	 * Construct a VideoCapture instance with the requested
	 * width and height using the specified video device. 
	 * The actual height and width of the captured
	 * frames may not equal the requested size if the
	 * underlying platform-specific grabber is not able to
	 * honor the request. The actual size can be inspected
	 * through the {@link #getWidth()} and {@link getHeight()}
	 * methods.  
	 * 
	 * @param width the requested video width.
	 * @param height the requested video height.
	 * @param device the requested video device.
	 */
	public VideoCapture(int width, int height, Device device) {
		fps = 25;
		grabber = new OpenIMAJGrabber();
		startSession(width, height, device);
	}
	
	/**
	 * Get a list of all compatible video devices attached
	 * to the machine.
	 * @return a list of devices.
	 */
	public static List<Device> getVideoDevices() {
		OpenIMAJGrabber grabber = new OpenIMAJGrabber();
		DeviceList list = grabber.getVideoDevices().get();
		
		return list.asArrayList();
	}
	
	protected synchronized boolean startSession(int width, int height, Device device) {
		if (grabber.startSession(width, height, Pointer.pointerTo(device))) {
			this.width = grabber.getWidth();
			this.height = grabber.getHeight();
			frame = new MBFImage(width, height, ColourSpace.RGB);
			
			isStopped = false;
			return true;
		}
		return false;
	}
	
	protected synchronized boolean startSession(int width, int height) {
		if (grabber.startSession(width, height)) {
			this.width = grabber.getWidth();
			this.height = grabber.getHeight();
			frame = new MBFImage(width, height, ColourSpace.RGB);
			
			isStopped = false;
			return true;
		} 
		return false;
	}
	
	/**
	 * Stop the video capture system. Once stopped, it
	 * can only be started again by constructing a new
	 * instance of VideoCapture.
	 */
	public synchronized void stopCapture() {
		isStopped = true;
		grabber.stopSession();
	}

	@Override
	public MBFImage getCurrentFrame() {
		return frame;
	}

	@Override
	public synchronized MBFImage getNextFrame() {
		if (isStopped) return frame;
		
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
