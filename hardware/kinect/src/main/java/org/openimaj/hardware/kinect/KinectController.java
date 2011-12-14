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
	protected static Pointer<freenect_context> CONTEXT;
	protected static EventThread EVENT_THREAD;
	protected static List<KinectController> INITIALISED_CONTROLLERS = new ArrayList<KinectController>();
	
	protected Pointer<freenect_device> device;
	public KinectStream<?> videoStream;
	public KinectDepthStream depthStream;

	public KinectController(int deviceId) {
		this(deviceId, false);
	}

	public KinectController(boolean irmode) {
		this(0, irmode);
	}

	public KinectController() {
		this(0, false);
	}

	/**
	 * Construct with the given device and mode.
	 * 
	 * @param deviceId the device identifier. 0 for the first one.
	 * @param irmode whether to use infra-red mode or rgb mode.
	 */
	public KinectController(int deviceId, boolean irmode) {
		// init the context and start thread if necessary
		if (!init()) {
			throw new RuntimeException("Unable to initialise libfreenect.");
		}

		int cd = connectedDevices();
		if (cd == 0) {
			throw new IllegalArgumentException("No devices found");
		}
		if (deviceId >= cd || deviceId < 0) {
			throw new IllegalArgumentException("Invalid device id");
		}

		//init device
		@SuppressWarnings("unchecked")
		Pointer<Pointer<freenect_device>> devicePtr = Pointer.pointerToPointer(Pointer.NULL);
		libfreenectLibrary.freenect_open_device(CONTEXT, devicePtr, deviceId);
		device = devicePtr.get();
		INITIALISED_CONTROLLERS.add(this);

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
	 */
	private static synchronized boolean init() {
		if (KinectController.CONTEXT == null) {
			@SuppressWarnings("unchecked")
			Pointer<Pointer<freenect_context>> ctxPointer = Pointer.pointerToPointer(Pointer.NULL);
			libfreenectLibrary.freenect_init(ctxPointer, Pointer.NULL);
			
			if (ctxPointer == null)
				return false;
			
			CONTEXT = ctxPointer.get();
			
			if (CONTEXT == null)
				return false;
			
			EVENT_THREAD = new EventThread();
			EVENT_THREAD.start();
			
			//turn off the devices on shutdown
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public synchronized void run() {
					while (INITIALISED_CONTROLLERS.size() > 0) {
						INITIALISED_CONTROLLERS.get(0).close();
					}
					
					if (EVENT_THREAD != null)
						EVENT_THREAD.kill();
					
					if (CONTEXT != null)
						libfreenectLibrary.freenect_shutdown(CONTEXT);
				}
			});
		}
		return true;
	}

	/**
	 * Switch the main camera between InfraRed and RGB modes. 
	 * @param irmode if true, then switches to IR mode, otherwise switches to RGB mode.
	 */
	public void setIRMode(boolean irmode) {
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
	 */
	public static synchronized int connectedDevices() {
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
		INITIALISED_CONTROLLERS.remove(this);
		libfreenectLibrary.freenect_close_device(device);
		device = null;
	}

	/**
	 * Get the current angle in degrees
	 * @return the angle
	 */
	public synchronized double getTilt() {
		libfreenectLibrary.freenect_update_tilt_state(device);
		Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		return libfreenectLibrary.freenect_get_tilt_degs(state);
	}

	/**
	 * Set the tilt to the given angle in degrees
	 * @param angle the angle
	 */
	public synchronized void setTilt(double angle) {
		if (angle < -30) angle = -30;
		if (angle > 30) angle = 30;
		libfreenectLibrary.freenect_set_tilt_degs(device, angle);		
	}

	/**
	 * Determine the current tilt status of the device
	 * @return the tilt status
	 */
	public synchronized KinectTiltStatus getTiltStatus() {
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
		ValuedEnum<freenect_led_options> o = freenect_led_options.valueOf(option.name());
		libfreenectLibrary.freenect_set_led(device, o);		
	}

	/**
	 * Sets the LEDs to blink red and yellow for five seconds
	 * before resetting to green.
	 */
	public synchronized void identify() {
		setLED(KinectLEDMode.LED_BLINK_RED_YELLOW);
		try { Thread.sleep(5000); } catch (InterruptedException e) { }
		setLED(KinectLEDMode.LED_GREEN);
	}

	/**
	 * Get the current acceleration values from the device
	 * @return the acceleration
	 */
	public synchronized KinectAcceleration getAcceleration() {
		Pointer<Double> px = Pointer.pointerToDouble(0);
		Pointer<Double> py = Pointer.pointerToDouble(0);
		Pointer<Double> pz = Pointer.pointerToDouble(0);

		libfreenectLibrary.freenect_update_tilt_state(device);
		Pointer<freenect_raw_tilt_state> state = libfreenectLibrary.freenect_get_tilt_state(device);
		libfreenectLibrary.freenect_get_mks_accel(state, px, py, pz);

		return new KinectAcceleration(px.getDouble(), py.getDouble(), pz.getDouble());
	}
	
	public static void main(String[] args) {
		VideoDisplay.createVideoDisplay(new KinectController(0).videoStream);
	}
}
