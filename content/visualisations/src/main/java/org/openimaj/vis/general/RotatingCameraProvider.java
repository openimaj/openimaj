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

	/** The radius of the X oscillation */
	private final float radiusX;

	/** The radius of the Y oscillation */
	private final float radiusY;

	/** The radius of the Z oscillation */
	private final float radiusZ;

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
		this.radiusX = this.radiusY = this.radiusZ = radius;
		this.startTime = System.currentTimeMillis();
	}

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
	 * 	@param radiusX The amplitude of the X osciallitions
	 * 	@param radiusY The amplitude of the Y osciallitions
	 * 	@param radiusZ The amplitude of the Z osciallitions
	 */
	public RotatingCameraProvider(
			final float xPos, final float yPos, final float zPos,
			final float x, final float y, final float z,
			final float xS, final float yS, final float zS,
			final float radiusX, final float radiusY, final float radiusZ )
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
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.radiusZ = radiusZ;
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

		final float xPos = (float)(this.xPos + this.radiusX * Math.sin( diffTime * this.xS ));
		final float yPos = (float)(this.yPos + this.radiusY * Math.cos( diffTime * this.yS ));
		final float zPos = (float)(this.zPos + this.radiusZ * Math.sin( diffTime * this.zS ));

		final float[] f = new float[] {xPos, yPos, zPos, this.x, this.y, this.z, 0, 1, 0};
		return f;
	}
}
