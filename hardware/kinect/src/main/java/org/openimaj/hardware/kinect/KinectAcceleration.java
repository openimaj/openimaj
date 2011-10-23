package org.openimaj.hardware.kinect;

/**
 * The accelerometer state of the Kinect
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class KinectAcceleration {
	public double x;
	public double y;
	public double z;
	
	public KinectAcceleration(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String toString() {
		return String.format("Acceleration(x: %1.3f, y: %1.3f, z: %1.3f)", x ,y ,z);
	}
}
