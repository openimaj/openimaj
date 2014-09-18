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
package org.openimaj.hardware.kinect;

import java.util.ArrayList;
import java.util.List;

import org.bridj.Pointer;
import org.bridj.ValuedEnum;
import org.openimaj.hardware.kinect.freenect.freenect_raw_tilt_state;
import org.openimaj.hardware.kinect.freenect.freenect_registration;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_context;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_led_options;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_tilt_status_code;
import org.openimaj.image.FImage;
import org.openimaj.video.VideoDisplay;

/**
 * The event thread for Freenect
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
class EventThread extends Thread {
	private volatile boolean alive = true;

	public EventThread() {
		setDaemon(true);
		setName("FreenectEventThread");
	}

	public void kill() {
		this.alive = false;
	}

	@Override
	public void run() {
		while (alive) {
			libfreenectLibrary.freenect_process_events(KinectController.CONTEXT);
		}
	}
};

/**
 * The OpenIMAJ Kinect Interface.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class KinectController {
	private static final int DEPTH_X_RES = 640;
	private static final int DEPTH_Y_RES = 480;
	protected volatile static Pointer<freenect_context> CONTEXT;
	protected volatile static EventThread EVENT_THREAD;
	protected volatile static List<KinectController> ACTIVE_CONTROLLERS = new ArrayList<KinectController>();

	protected Pointer<freenect_device> device;

	/**
	 * The RGB or IR video stream
	 */
	public KinectStream<?> videoStream;

	/**
	 * The depth stream
	 */
	public KinectDepthStream depthStream;

	/**
	 * Construct with given device
	 * 
	 * @param deviceId
	 *            the device id
	 * @throws KinectException
	 */
	public KinectController(int deviceId) throws KinectException {
		this(deviceId, false, false);
	}

	/**
	 * Construct with the first device in the given mode.
	 * 
	 * @param irmode
	 *            if true then the camera is set to IR mode; otherwise its in
	 *            RGB mode
	 * @throws KinectException
	 */
	public KinectController(boolean irmode) throws KinectException {
		this(0, irmode, false);
	}

	/**
	 * Default constructor. Uses the first device in RGB mode.
	 * 
	 * @throws KinectException
	 */
	public KinectController() throws KinectException {
		this(0, false, false);
	}

	/**
	 * Construct with the given device and mode.
	 * 
	 * @param deviceId
	 *            the device identifier. 0 for the first one.
	 * @param irmode
	 *            whether to use infra-red mode or rgb mode.
	 * @param registeredDepthMode
	 *            whether to register the depth image. If true, depth
	 *            measurements are in millimeters.
	 * @throws KinectException
	 */
	public KinectController(int deviceId, boolean irmode, boolean registeredDepthMode) throws KinectException {
		// init the context and start thread if necessary
		init();

		final int cd = connectedDevices();
		if (deviceId >= cd || deviceId < 0) {
			throw new IllegalArgumentException("Invalid device id");
		}

		ACTIVE_CONTROLLERS.add(this);

		// init device
		final Pointer<Pointer<freenect_device>> devicePtr = Pointer.pointerToPointer(null);
		libfreenectLibrary.freenect_open_device(CONTEXT, devicePtr, deviceId);
		device = devicePtr.get();

		// setup listeners
		if (irmode)
			videoStream = new KinectIRVideoStream(this);
		else
			videoStream = new KinectRGBVideoStream(this);
		depthStream = new KinectDepthStream(this, registeredDepthMode);
	}

	@Override
	public void finalize() {
		close();
	}

	/**
	 * Init the freenect library. This only has to be done once.
	 * 
	 * @throws KinectException
	 */
	private static synchronized void init() throws KinectException {
		if (KinectController.CONTEXT == null) {
			final Pointer<Pointer<freenect_context>> ctxPointer = Pointer.pointerToPointer(null);
			libfreenectLibrary.freenect_init(ctxPointer, Pointer.NULL);

			if (ctxPointer == null)
				throw new KinectException("Unable to initialise libfreenect.");

			CONTEXT = ctxPointer.get();

			if (CONTEXT == null)
				throw new KinectException("Unable to initialise libfreenect.");

			if (libfreenectLibrary.freenect_num_devices(CONTEXT) == 0) {
				libfreenectLibrary.freenect_shutdown(CONTEXT);
				throw new KinectException("Unable to initialise libfreenect; No devices found.");
			}

			EVENT_THREAD = new EventThread();
			EVENT_THREAD.start();

			// turn off the devices on shutdown
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public synchronized void run() {
					shutdownFreenect();
				}
			});
		}
	}

	/**
	 * Completely shutdown the context. This turns off all cameras. The context
	 * will be restarted upon creation of a new KinectController.
	 */
	public synchronized static void shutdownFreenect() {
		while (ACTIVE_CONTROLLERS.size() > 0) {
			ACTIVE_CONTROLLERS.get(0).close();
		}

		if (EVENT_THREAD != null) {
			EVENT_THREAD.kill();
		}

		if (CONTEXT != null)
			libfreenectLibrary.freenect_shutdown(CONTEXT);

		CONTEXT = null;
		EVENT_THREAD = null;
	}

	/**
	 * Switch the main camera between InfraRed and RGB modes.
	 * 
	 * @param irmode
	 *            if true, then switches to IR mode, otherwise switches to RGB
	 *            mode.
	 */
	public void setIRMode(boolean irmode) {
		if (device == null)
			return;

		if (irmode) {
			if (!(videoStream instanceof KinectIRVideoStream)) {
				videoStream.stop();
				videoStream = new KinectIRVideoStream(this);
			}
		} else {
			if (!(videoStream instanceof KinectRGBVideoStream)) {
				videoStream.stop();
				videoStream = new KinectRGBVideoStream(this);
			}
		}
	}

	/**
	 * Set whether depth should be registered
	 * 
	 * @param rdepth
	 *            if true, then switches to depth registered mode, otherwise
	 *            depth is not registered
	 */
	public void setRegisteredDepth(boolean rdepth) {
		if (device == null)
			return;

		if (depthStream.registered != rdepth) {
			depthStream.stop();
			depthStream = new KinectDepthStream(this, rdepth);
		}
	}

	/**
	 * Get the number of connected devices.
	 * 
	 * @return the number of devices connected.
	 * @throws KinectException
	 */
	public static synchronized int connectedDevices() throws KinectException {
		init();
		return libfreenectLibrary.freenect_num_devices(CONTEXT);
	}

	/**
	 * Close the device. Closing an already closed device has no effect.
	 */
	public synchronized void close() {
		if (device == null)
			return;

		videoStream.stop();
		depthStream.stop();
		libfreenectLibrary.freenect_close_device(device);
		device = null;

		ACTIVE_CONTROLLERS.remove(this);
	}

	/**
	 * Get the current angle in degrees
	 * 
	 * @return the angle or 0 if the device is closed
	 */
	public synchronized double getTilt() {
		if (device == null)
			return 0;

		libfreenectLibrary.freenect_update_tilt_state(device);
		final Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		return libfreenectLibrary.freenect_get_tilt_degs(state);
	}

	/**
	 * Set the tilt to the given angle in degrees
	 * 
	 * @param angle
	 *            the angle
	 */
	public synchronized void setTilt(double angle) {
		if (device == null)
			return;

		if (angle < -30)
			angle = -30;
		if (angle > 30)
			angle = 30;
		libfreenectLibrary.freenect_set_tilt_degs(device, angle);
	}

	/**
	 * Determine the current tilt status of the device
	 * 
	 * @return the tilt status or null if the device is closed
	 */
	public synchronized KinectTiltStatus getTiltStatus() {
		if (device == null)
			return null;

		libfreenectLibrary.freenect_update_tilt_state(device);
		final Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		final ValuedEnum<freenect_tilt_status_code> v = libfreenectLibrary.freenect_get_tilt_status(state);

		for (final freenect_tilt_status_code c : freenect_tilt_status_code.values())
			if (c.value == v.value())
				return KinectTiltStatus.valueOf(c.name());

		return null;
	}

	/**
	 * Set the device status LEDs
	 * 
	 * @param option
	 *            the LED status to set
	 */
	public synchronized void setLED(KinectLEDMode option) {
		if (device == null)
			return;

		final ValuedEnum<freenect_led_options> o = freenect_led_options.valueOf(option.name());
		libfreenectLibrary.freenect_set_led(device, o);
	}

	/**
	 * Sets the LEDs to blink red and yellow for five seconds before resetting
	 * to green.
	 */
	public synchronized void identify() {
		if (device == null)
			return;

		setLED(KinectLEDMode.LED_BLINK_RED_YELLOW);
		try {
			Thread.sleep(5000);
		} catch (final InterruptedException e) {
		}
		setLED(KinectLEDMode.LED_GREEN);
	}

	/**
	 * Get the current acceleration values from the device
	 * 
	 * @return the acceleration or null if the device is closed
	 */
	public synchronized KinectAcceleration getAcceleration() {
		if (device == null)
			return null;

		final Pointer<Double> px = Pointer.pointerToDouble(0);
		final Pointer<Double> py = Pointer.pointerToDouble(0);
		final Pointer<Double> pz = Pointer.pointerToDouble(0);

		libfreenectLibrary.freenect_update_tilt_state(device);
		final Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		libfreenectLibrary.freenect_get_mks_accel(state, px, py, pz);

		return new KinectAcceleration(px.getDouble(), py.getDouble(), pz.getDouble());
	}

	/**
	 * Compute the world coordinates from the pixel location and registered
	 * depth.
	 * 
	 * @param x
	 *            pixel x-ordinate
	 * @param y
	 *            pixel y-ordinate
	 * @param depth
	 *            the depth
	 * @return the (x,y,z) coordinate in world space
	 */
	public double[] cameraToWorld(int x, int y, int depth) {
		final Pointer<Double> wx = Pointer.allocateDouble();
		final Pointer<Double> wy = Pointer.allocateDouble();
		libfreenectLibrary.freenect_camera_to_world(device, x, y, depth, wx, wy);

		return new double[] { wx.get(), wy.get(), depth };
	}

	/**
	 * Compute the scaling factor for computing world coordinates.
	 * 
	 * @see #cameraToWorld(int, int, int, double)
	 * 
	 * @return the scaling factor
	 */
	public double computeScalingFactor() {
		// Struct-by-value isn't currently working in bridj. When it is
		// we can do:
		// freenect_registration regInfo =
		// libfreenectLibrary.freenect_copy_registration(device);
		// double ref_pix_size =
		// regInfo.zero_plane_info().reference_pixel_size();
		// double ref_distance = regInfo.zero_plane_info().reference_distance();
		// return 2 * ref_pix_size / ref_distance;

		// for now we can work around by calculating the factor from a projected
		// point
		final int x = (DEPTH_X_RES / 2) + 1;
		final double[] xyz = cameraToWorld(x, 0, 1000);
		return xyz[0] / 1000.0;
	}

	/**
	 * Compute the world coordinates from the pixel location and registered
	 * depth. This method requires you pre-compute the constant scaling factor
	 * using {@link #computeScalingFactor()}, but it should be faster than using
	 * {@link #cameraToWorld(int, int, int)}.
	 * 
	 * @param x
	 *            pixel x-ordinate
	 * @param y
	 *            pixel y-ordinate
	 * @param depth
	 *            the depth
	 * @param factor
	 *            the scaling factor
	 * @return the (x,y,z) coordinate in world space
	 */
	public float[] cameraToWorld(int x, int y, int depth, double factor) {
		final float[] pt = new float[3];
		pt[0] = (float) ((x - DEPTH_X_RES / 2) * factor * depth);
		pt[1] = (float) ((y - DEPTH_Y_RES / 2) * factor * depth);
		pt[2] = depth;
		return pt;
	}

	/**
	 * Compute the world coordinates from the pixel location and registered
	 * depth. This method requires you pre-compute the constant scaling factor
	 * using {@link #computeScalingFactor()}, but it should be faster than using
	 * {@link #cameraToWorld(int, int, int)}.
	 * 
	 * This method is the same as {@link #cameraToWorld(int, int, int, double)},
	 * but reuses the point array for efficiency.
	 * 
	 * @param x
	 *            pixel x-ordinate
	 * @param y
	 *            pixel y-ordinate
	 * @param depth
	 *            the depth
	 * @param factor
	 * @param pt
	 *            the point to write to
	 * @return the (x,y,z) coordinate in world space
	 */
	public float[] cameraToWorld(int x, int y, int depth, double factor, float[] pt) {
		pt[0] = (float) ((x - DEPTH_X_RES / 2) * factor * depth);
		pt[1] = (float) ((y - DEPTH_Y_RES / 2) * factor * depth);
		pt[2] = depth;
		return pt;
	}

	/**
	 * Compute the world coordinates from the pixel locations in the given
	 * registered depth image. This method basically gives you a fully
	 * registered point cloud.
	 * 
	 * @param regDepth
	 *            the registered depth image
	 * @param startx
	 *            the starting x-ordinate in the image
	 * @param stopx
	 *            the stopping x-ordinate in the image
	 * @param stepx
	 *            the number of pixels in the x direction to skip between
	 *            samples
	 * @param starty
	 *            the starting y-ordinate in the image
	 * @param stopy
	 *            the stopping y-ordinate in the image
	 * @param stepy
	 *            the number of pixels in the y direction to skip between
	 *            samples
	 * @return an array of 3d vectors
	 */
	public float[][] cameraToWorld(FImage regDepth, int startx, int stopx, int stepx, int starty, int stopy, int stepy) {
		final freenect_registration regInfo = libfreenectLibrary.freenect_copy_registration(device);
		final double ref_pix_size = regInfo.zero_plane_info().reference_pixel_size();
		final double ref_distance = regInfo.zero_plane_info().reference_distance();

		final int nx = (stopx - startx) / stepx;
		final int ny = (stopy - starty) / stepy;
		final float[][] points = new float[nx * ny][3];
		final float[][] depths = regDepth.pixels;
		final double factor = 2 * ref_pix_size / ref_distance;

		for (int i = 0, y = starty; y < stopy; y += stepy) {
			for (int x = startx; x < stopx; x += stepx) {
				final float depth = depths[y][x];

				points[i][0] = (float) ((x - DEPTH_X_RES / 2) * factor * depth);
				points[i][1] = (float) ((y - DEPTH_Y_RES / 2) * factor * depth);
				points[i][2] = depth;
			}
		}

		return points;
	}

	/**
	 * Test
	 * 
	 * @param args
	 * @throws KinectException
	 */
	public static void main(String[] args) throws KinectException {
		VideoDisplay.createVideoDisplay(new KinectController(0).videoStream);
	}
}
