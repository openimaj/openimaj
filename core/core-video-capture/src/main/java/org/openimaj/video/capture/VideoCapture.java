package org.openimaj.video.capture;

import java.io.IOException;
import java.util.List;


import org.bridj.Pointer;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

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
	 * @throws IOException if no webcam is found
	 */
	public VideoCapture(int width, int height) throws IOException {
		fps = 25;
		grabber = new OpenIMAJGrabber();
		if(!startSession(width, height))
			throw new IOException("No webcams found!");
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

	/**
	 * Test main method.
	 * Lists the available devices, and then 
	 * opens the first and second capture devices
	 * if they are available and displays their video.
	 * @param args ignored.
	 */
	public static void main(String [] args) {
		List<Device> devices = VideoCapture.getVideoDevices();
		System.out.println(devices);

		if (devices.size() > 0) {
			VideoCapture grabber1 = new VideoCapture(640, 480, devices.get(0));
			VideoDisplay<MBFImage> disp1 = VideoDisplay.createVideoDisplay(grabber1);

			if (devices.size() > 1) {
				VideoCapture grabber2 = new VideoCapture(320, 240, devices.get(1));
				VideoDisplay<MBFImage> disp2 = VideoDisplay.createVideoDisplay(grabber2);
				disp2.getScreen().setLocation(disp1.getScreen().getWidth(), disp2.getScreen().getLocation().y);
			}
		}
	}
}
