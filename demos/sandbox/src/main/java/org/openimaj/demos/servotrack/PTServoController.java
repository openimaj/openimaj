package org.openimaj.demos.servotrack;

import gnu.io.SerialPort;

import org.openimaj.hardware.serial.SerialDevice;

public class PTServoController {
	private SerialDevice device;
	private int currentTilt = 90;
	private int currentPan = 90;

	public PTServoController(String dev) throws Exception {
		device = new SerialDevice(dev, 9600, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		this.setPan(currentPan);
		this.setTilt(currentTilt);
	}

	public void setTilt(int angle) {
		if (angle > 0 && angle < 180) {
			currentTilt = angle;
			sendCommand("t", currentTilt);
		}
	}

	public void setPan(int angle) {
		if (angle > 0 && angle < 180) {
			currentPan = angle;
			sendCommand("p", currentPan);
		}
	}

	public void changePanBy(int angle) {
		setPan(angle + currentPan);
	}

	public void changeTiltBy(int angle) {
		setTilt(angle + currentTilt);
	}

	public int getTilt() {
		return currentTilt;
	}

	public int getPan() {
		return currentPan;
	}

	private void sendCommand(String servo, int angle) {
		try {
			device.getOutputStream().write((servo + " " + angle + "\n").getBytes("US-ASCII"));
			Thread.sleep(60);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		final PTServoController controller = new PTServoController("/dev/tty.usbmodemfd121");

		for (int i = 0; i < 100; i++) {
			controller.setPan(i);
			controller.setTilt(60 + i);
		}

		System.exit(0);
	}
}
