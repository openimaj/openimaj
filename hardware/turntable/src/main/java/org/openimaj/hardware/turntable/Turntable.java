package org.openimaj.hardware.turntable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import gnu.io.SerialPort;

import org.openimaj.hardware.serial.SerialDevice;


/**
 * A simple controller for our serially connected electronic turntable.
 * 
 * Send NNNNNA0 to rotate anticlockwise by NNNNN increments (360/24000th of a degree)
 * Send NNNNNC0 to rotate clockwise by NNNNN increments (360/24000th of a degree)
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class Turntable {
	protected final static int TICKS_PER_REVOLUTION = 24000;
	protected final static double TICKS_PER_DEGREE = TICKS_PER_REVOLUTION / 360.0;
	protected final static double TICKS_PER_RADIAN = TICKS_PER_REVOLUTION / (2.0 * Math.PI);

	protected int currentAngleTicks = 0;
	protected SerialDevice turntableDevice;

	public Turntable(String port) throws Exception {
		turntableDevice = new SerialDevice( port, 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
	}

	public double getCurrentAngleDegrees() {
		return currentAngleTicks * TICKS_PER_DEGREE;
	}

	public double getCurrentAngleRadians() {
		return currentAngleTicks * TICKS_PER_RADIAN;
	}

	public void rotateToRadians() {

	}

	public void rotateToDegrees(double degrees) throws IOException {
		double current = getCurrentAngleDegrees();
		double delta = degrees - current;
		sendCommand((int)Math.rint(delta * TICKS_PER_DEGREE));
	}

	public void rotateByRadians(double rads) throws IOException {
		sendCommand((int)Math.rint(rads * TICKS_PER_RADIAN));
	}

	public void rotateByDegrees(double degrees) throws IOException {
		sendCommand((int)Math.rint(degrees * TICKS_PER_DEGREE));
	}

	protected void sendCommand(int ticks) throws IOException {
		if (ticks < 0) {
			sendCommand(Math.abs(ticks), false);
		} else {
			sendCommand(ticks, true);
		}
	}
	
	protected void sendCommand(int ticks, boolean cw) throws IOException {
		String dir = cw ? "C" : "A";
		
		try {	
			String cmd = ticks + dir + "0\n";
			System.out.println("Sending command: " + cmd);
			turntableDevice.getOutputStream().write(cmd.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		turntableDevice.close();
	}
	
	public static void main( String[] args ) throws Exception {
		System.out.println("Starting Turntable");
		Turntable t = new Turntable("/dev/tty.usbserial-FTCXE2RA");
		t.rotateByDegrees(20);
		System.out.println("Done");
	}
}
