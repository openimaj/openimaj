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
package org.openimaj.hardware.turntable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jssc.SerialPort;

import org.openimaj.hardware.serial.SerialDevice;

/**
 * A simple controller for our serially connected electronic turntable.
 *
 * Send NNNNNA0 to rotate anticlockwise by NNNNN increments (360/24000th of a
 * degree) Send NNNNNC0 to rotate clockwise by NNNNN increments (360/24000th of
 * a degree)
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Turntable {
	protected final static int TICKS_PER_REVOLUTION = 24000;
	protected final static double TICKS_PER_DEGREE = TICKS_PER_REVOLUTION / 360.0;
	protected final static double TICKS_PER_RADIAN = TICKS_PER_REVOLUTION / (2.0 * Math.PI);

	protected int currentAngleTicks = 0;
	protected SerialDevice turntableDevice;

	/**
	 * Default constructor. Opens a connection to the turntable on the given
	 * port.
	 *
	 * @param port
	 *            The port
	 * @throws Exception
	 */
	public Turntable(String port) throws Exception {
		turntableDevice = new SerialDevice(port, 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
	}

	/**
	 * Get the current absolute angle in degrees (relative to the position at
	 * initialisation)
	 *
	 * @return the absolute angle in degrees
	 */
	public double getCurrentAngleDegrees() {
		return currentAngleTicks / TICKS_PER_DEGREE;
	}

	/**
	 * Get the current absolute angle in radians (relative to the position at
	 * initialisation)
	 *
	 * @return the absolute angle in radians
	 */
	public double getCurrentAngleRadians() {
		return currentAngleTicks / TICKS_PER_RADIAN;
	}

	/**
	 * Rotate the turntable to the given absolute angle in radians (relative to
	 * the position at initialisation). The turntable will take the shortest
	 * path to the requested position.
	 *
	 * @param rads
	 *            the angle in radians
	 * @throws IOException
	 */
	public void rotateToRadians(double rads) throws IOException {
		rotateToDegrees(rads * 180 / Math.PI);
	}

	/**
	 * Rotate the turntable to the given absolute angle in degrees (relative to
	 * the position at initialisation). The turntable will take the shortest
	 * path to the requested position.
	 *
	 * @param degrees
	 *            the angle in degrees
	 * @throws IOException
	 */
	public void rotateToDegrees(double degrees) throws IOException {
		final double current = getCurrentAngleDegrees();
		double delta = degrees - current;

		if (delta > 180)
			delta = 360 - delta;
		if (delta < -180)
			delta = 360 + delta;

		sendCommand((int) Math.rint(delta * TICKS_PER_DEGREE));
	}

	/**
	 * Rotate the turntable by the given angle in radians. Positive angles are
	 * clockwise, negative anticlockwise.
	 *
	 * @param rads
	 *            the angle in radians
	 * @throws IOException
	 */
	public void rotateByRadians(double rads) throws IOException {
		sendCommand((int) Math.rint(rads * TICKS_PER_RADIAN));
	}

	/**
	 * Rotate the turntable by the given angle in degrees. Positive angles are
	 * clockwise, negative anticlockwise.
	 *
	 * @param degrees
	 *            the angle in degrees
	 * @throws IOException
	 */
	public void rotateByDegrees(double degrees) throws IOException {
		sendCommand((int) Math.rint(degrees * TICKS_PER_DEGREE));
	}

	protected void sendCommand(int ticks) throws IOException {
		if (ticks < 0) {
			sendCommand(Math.abs(ticks), false);
		} else {
			sendCommand(ticks, true);
		}
	}

	protected void sendCommand(int ticks, boolean cw) throws IOException {
		final String dir = cw ? "C" : "A";

		if (cw)
			currentAngleTicks += ticks;
		else
			currentAngleTicks -= ticks;

		if (currentAngleTicks > TICKS_PER_REVOLUTION / 2)
			currentAngleTicks = TICKS_PER_REVOLUTION - currentAngleTicks;
		if (currentAngleTicks < -TICKS_PER_REVOLUTION / 2)
			currentAngleTicks = TICKS_PER_REVOLUTION + currentAngleTicks;

		try {
			final String cmd = ticks + dir + "0\n";
			turntableDevice.getOutputStream().write(cmd.getBytes("US-ASCII"));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the connection to the turntable.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		turntableDevice.close();
	}

	/**
	 * Test the turntable
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Initializing Turntable");
		System.out
		.println("the command \"r 10\" will rotate the turntable to 10 degrees CW relative to the starting point");
		System.out
		.println("the command \"i -10\" will rotate the turntable to 10 degrees AW relative to the current point");

		// Turntable t = new Turntable("/dev/tty.usbserial-FTCXE2RA");
		final Turntable t = new Turntable("/dev/tty.usbserial");

		System.out.println("Turntable is ready");
		System.out.println("Current absolute angle is " + t.getCurrentAngleDegrees() + " degrees");

		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String s;
		while ((s = br.readLine()) != null) {
			try {
				final String[] parts = s.split("\\s");

				if (parts[0].equals("q"))
					break;

				final double ang = Double.parseDouble(parts[1]);
				if (parts[0].equals("i"))
					t.rotateByDegrees(ang);
				else if (parts[0].equals("r"))
					t.rotateToDegrees(ang);
				else
					throw new Exception();

				System.out.println("Rotating to absolute angle of " + t.getCurrentAngleDegrees() + " degrees");
			} catch (final Throwable throwable) {
				System.out.println("invalid command");
			}
		}

		System.out.println("Done");
		System.exit(0);
	}
}
