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
package org.openimaj.image.feature.local.keypoints.quantised;

import org.openimaj.feature.local.quantised.QuantisedLocalFeature;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointLocation;
import org.openimaj.math.geometry.point.Point2d;

import Jama.Matrix;

/**
 * A {@link QuantisedKeypoint} is a {@link QuantisedLocalFeature} with its
 * location described by a {@link KeypointLocation}. It is the quantised
 * equivalent to a {@link Keypoint}, with the feature vector replaced by a
 * single integer assignment.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class QuantisedKeypoint extends QuantisedLocalFeature<KeypointLocation> implements Point2d {
	/**
	 * Construct an empty {@link QuantisedKeypoint}, located at the origin with
	 * an id of 0.
	 */
	public QuantisedKeypoint() {
		super(new KeypointLocation(0, 0, 0, 0), 0);
	}

	/**
	 * Construct a {@link QuantisedKeypoint}, located at the given position with
	 * an id of 0.
	 *
	 * @param loc
	 *            the position
	 */
	public QuantisedKeypoint(KeypointLocation loc) {
		super(loc, 0);
	}

	/**
	 * Construct a {@link QuantisedKeypoint}, located at the given position and
	 * id.
	 *
	 * @param loc
	 *            the position
	 * @param id
	 *            the id
	 */
	public QuantisedKeypoint(KeypointLocation loc, int id) {
		super(loc, id);
	}

	@Override
	public Float getOrdinate(int dimension) {
		if (dimension == 0)
			return location.x;
		if (dimension == 1)
			return location.y;
		if (dimension == 2)
			return location.scale;
		return null;
	}

	@Override
	public void setOrdinate(int dimension, Number value) {
		if (dimension == 0)
			location.x = value.floatValue();
		if (dimension == 1)
			location.y = value.floatValue();
		if (dimension == 2)
			location.scale = value.floatValue();
	}

	@Override
	public int getDimensions() {
		return 3;
	}

	@Override
	public float getX() {
		return location.x;
	}

	@Override
	public float getY() {
		return location.y;
	}

	@Override
	public void setX(float x) {
		this.location.x = x;
	}

	@Override
	public void setY(float y) {
		this.location.y = y;
	}

	@Override
	public void copyFrom(Point2d p) {
		location.copyFrom(p);
	}

	@Override
	public Point2d copy() {
		return location.copy();
	}

	@Override
	public void translate(float x, float y) {
		location.translate(x, y);
	}

	@Override
	public Point2d transform(Matrix m) {
		return location.transform(m);
	}

	@Override
	public Point2d minus(Point2d a) {
		return location.minus(a);
	}

	@Override
	public void translate(Point2d v) {
		location.translate(v);
	}

}
