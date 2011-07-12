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
package org.openimaj.video.capture;

import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

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
		//on 32 bit osx a deadlock seems to occur between the
		//initialisation of the native library and AWT. This
		//seems to fix it...
		VideoCapture.getVideoDevices();

		fps = 25;
		grabber = new OpenIMAJGrabber();
		if(!startSession(width, height, fps))
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
		startSession(width, height, 0, device);
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
	public VideoCapture(int width, int height, double fps, Device device) {
		this.fps = fps;
		grabber = new OpenIMAJGrabber();
		startSession(width, height, fps, device);
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

	protected synchronized boolean startSession(int width, int height, double reqFPS, Device device) {
		if (grabber.startSession(width, height, reqFPS, Pointer.pointerTo(device))) {
			this.width = grabber.getWidth();
			this.height = grabber.getHeight();
			frame = new MBFImage(width, height, ColourSpace.RGB);

			isStopped = false;
			return true;
		}
		return false;
	}

	protected synchronized boolean startSession(int width, int height, double reqFPS) {
		if (grabber.startSession(width, height, reqFPS)) {
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
		for (Device d : devices)
			System.out.println(d);

		if (devices.size() == 1) {
			VideoCapture grabber1 = new VideoCapture(960, 720, devices.get(0));
			VideoDisplay.createVideoDisplay(grabber1);
		} else {
			int w = 320;
			int h = 240;
			double rate = 25.0;
			
			for (int y=0, i=0; y<3; y++) {
				for (int x=0; x<3 && i<devices.size(); x++, i++) {
					if (devices.get(i).getNameStr().equals("FaceTime HD Camera (Built-in)"))
						continue;
					
					VideoCapture grabber2 = new VideoCapture(w, h, rate, devices.get(i));
					
//					MBFImage im = grabber2.getNextFrame();
//					DisplayUtilities.display(im).setLocation(320*x, 240 *y);
//					grabber2.stopCapture();
					
					VideoDisplay<MBFImage> disp = VideoDisplay.createVideoDisplay(grabber2);
					SwingUtilities.getRoot(disp.getScreen()).setLocation(320*x, 240 *y);
				}
			}
		}
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getWidth()
	 */
	@Override
	public int getWidth()
	{
		return width;
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getHeight()
	 */
	@Override
	public int getHeight()
	{
		return height;
	}
}
