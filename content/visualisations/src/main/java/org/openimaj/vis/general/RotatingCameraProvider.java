/**
 *
 */
package org.openimaj.vis.general;



/**
 *	This provides an implementation of the camera position provider interface
 *	that points a camera at a specific point and rotates the camera around the
 *	X, Y and Z planes with specific speeds at a specific radius.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Jul 2013
 */
public class RotatingCameraProvider implements CameraPositionProvider
{
	/** The lookAt x coordinate */
	private final float x;

	/** The lookAt y coordinate */
	private final float y;

	/** The lookAt z coordinate */
	private final float z;

	/** The speed of the x oscillation */
	private final float xS;

	/** The speed of the y oscillation */
	private final float yS;

	/** The speed of the z oscillation */
	private final float zS;

	/** The radius of the oscillation */
	private final float radius;

	/** The time the provider was initialised */
	private long startTime = 0;

	/** The position of the camera x coordinate */
	private final float xPos;

	/** The position of the camera y coordinate */
	private final float yPos;

	/** The position of the camera z coordinate */
	private final float zPos;

	/**
	 * 	Rotating camera provider looking at x, y, z with the given radius.
	 *
	 * 	@param xPos The initial position of the camera x coordinate
	 * 	@param yPos The initial position of the camera y coordinate
	 * 	@param zPos The initial position of the camera z coordinate
	 *	@param x The x The look-at point of the camera x coordinate
	 *	@param y The y The look-at point of the camera y coordinate
	 *	@param z The z The look-at point of the camera z coordinate
	 *	@param xS The x speed The x oscillation speed
	 *	@param yS The y speed The y oscillation speed
	 *	@param zS The z speed The z oscillation speed
	 *	@param radius The amplitude of all the osciallitions
	 */
	public RotatingCameraProvider(
			final float xPos, final float yPos, final float zPos,
			final float x, final float y, final float z,
			final float xS, final float yS, final float zS,
			final float radius )
	{
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
		this.x = x;
		this.y = y;
		this.z = z;
		this.xS = yS;
		this.yS = yS;
		this.zS = zS;
		this.radius = radius;
		this.startTime = System.currentTimeMillis();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.CameraPositionProvider#getCameraPosition()
	 */
	@Override
	public float[] getCameraPosition()
	{
		final long currentTime = System.currentTimeMillis();
		final long diffTime = currentTime - this.startTime;

		final float xPos = (float)(this.xPos + this.radius * Math.sin( diffTime * this.xS ));
		final float yPos = (float)(this.yPos + this.radius * Math.cos( diffTime * this.yS ));
		final float zPos = (float)(this.zPos + this.radius * Math.sin( diffTime * this.zS ));

		final float[] f = new float[] {xPos, yPos, zPos, this.x, this.y, this.z, 0, 1, 0};
		return f;
	}
}
