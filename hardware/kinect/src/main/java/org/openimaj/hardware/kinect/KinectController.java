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
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_context;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_device;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_led_options;
import org.openimaj.hardware.kinect.freenect.libfreenectLibrary.freenect_tilt_status_code;
import org.openimaj.video.VideoDisplay;

/**
 * The event thread for Freenect
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class KinectController {
	protected volatile static Pointer<freenect_context> CONTEXT;
	protected volatile static EventThread EVENT_THREAD;
	protected volatile static List<KinectController> ACTIVE_CONTROLLERS = new ArrayList<KinectController>();

	protected Pointer<freenect_device> device;
	public KinectStream<?> videoStream;
	public KinectDepthStream depthStream;

	public KinectController(int deviceId) throws KinectException {
		this(deviceId, false);
	}

	public KinectController(boolean irmode) throws KinectException {
		this(0, irmode);
	}

	public KinectController() throws KinectException {
		this(0, false);
	}

	/**
	 * Construct with the given device and mode.
	 * 
	 * @param deviceId the device identifier. 0 for the first one.
	 * @param irmode whether to use infra-red mode or rgb mode.
	 * @throws KinectException 
	 */
	public KinectController(int deviceId, boolean irmode) throws KinectException {
		// init the context and start thread if necessary
		init();

		int cd = connectedDevices();
		if (cd == 0) {
			throw new IllegalArgumentException("No devices found");
		}
		if (deviceId >= cd || deviceId < 0) {
			throw new IllegalArgumentException("Invalid device id");
		}
		
		ACTIVE_CONTROLLERS.add(this);

		//init device
		@SuppressWarnings("unchecked")
		Pointer<Pointer<freenect_device>> devicePtr = Pointer.pointerToPointer(Pointer.NULL);
		libfreenectLibrary.freenect_open_device(CONTEXT, devicePtr, deviceId);
		device = devicePtr.get();

		//setup listeners
		if (irmode)
			videoStream = new KinectIRVideoStream(this);
		else
			videoStream = new KinectRGBVideoStream(this);
		depthStream = new KinectDepthStream(this);
	}

	@Override
	public void finalize() {
		close();
	}

	/**
	 * Init the freenect library. This only has to be done once.
	 * @throws KinectException 
	 */
	private static synchronized void init() throws KinectException {
		if (KinectController.CONTEXT == null) {
			@SuppressWarnings("unchecked")
			Pointer<Pointer<freenect_context>> ctxPointer = Pointer.pointerToPointer(Pointer.NULL);
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

			//turn off the devices on shutdown
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public synchronized void run() {
					shutdownFreenect();
				}
			});
		}
	}
	
	/**
	 * Completely shutdown the context. This turns
	 * off all cameras. The context will be restarted
	 * upon creation of a new KinectController.
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
	 * @param irmode if true, then switches to IR mode, otherwise switches to RGB mode.
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
	 * Get the number of connected devices.
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
	 * @return the angle or 0 if the device is closed
	 */
	public synchronized double getTilt() {
		if (device == null)
			return 0;
		
		libfreenectLibrary.freenect_update_tilt_state(device);
		Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		return libfreenectLibrary.freenect_get_tilt_degs(state);
	}

	/**
	 * Set the tilt to the given angle in degrees
	 * @param angle the angle
	 */
	public synchronized void setTilt(double angle) {
		if (device == null)
			return;
		
		if (angle < -30) angle = -30;
		if (angle > 30) angle = 30;
		libfreenectLibrary.freenect_set_tilt_degs(device, angle);		
	}

	/**
	 * Determine the current tilt status of the device
	 * @return the tilt status or null if the device is closed
	 */
	public synchronized KinectTiltStatus getTiltStatus() {
		if (device == null)
			return null;
		
		libfreenectLibrary.freenect_update_tilt_state(device);
		Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		ValuedEnum<freenect_tilt_status_code> v = libfreenectLibrary.freenect_get_tilt_status(state);

		for (freenect_tilt_status_code c : freenect_tilt_status_code.values())
			if (c.value == v.value()) return KinectTiltStatus.valueOf(c.name()); 

		return null;
	}

	/**
	 * Set the device status LEDs
	 * @param option the LED status to set
	 */
	public synchronized void setLED(KinectLEDMode option) {
		if (device == null)
			return;
		
		ValuedEnum<freenect_led_options> o = freenect_led_options.valueOf(option.name());
		libfreenectLibrary.freenect_set_led(device, o);		
	}

	/**
	 * Sets the LEDs to blink red and yellow for five seconds
	 * before resetting to green.
	 */
	public synchronized void identify() {
		if (device == null)
			return;
		
		setLED(KinectLEDMode.LED_BLINK_RED_YELLOW);
		try { Thread.sleep(5000); } catch (InterruptedException e) { }
		setLED(KinectLEDMode.LED_GREEN);
	}

	/**
	 * Get the current acceleration values from the device
	 * @return the acceleration or null if the device is closed
	 */
	public synchronized KinectAcceleration getAcceleration() {
		if (device == null)
			return null;
		
		Pointer<Double> px = Pointer.pointerToDouble(0);
		Pointer<Double> py = Pointer.pointerToDouble(0);
		Pointer<Double> pz = Pointer.pointerToDouble(0);

		libfreenectLibrary.freenect_update_tilt_state(device);
		Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		libfreenectLibrary.freenect_get_mks_accel(state, px, py, pz);

		return new KinectAcceleration(px.getDouble(), py.getDouble(), pz.getDouble());
	}

	public static void main(String[] args) throws KinectException {
		VideoDisplay.createVideoDisplay(new KinectController(0).videoStream);
	}
}
