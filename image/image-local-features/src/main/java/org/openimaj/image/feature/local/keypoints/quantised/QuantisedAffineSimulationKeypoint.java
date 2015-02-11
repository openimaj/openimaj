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
import org.openimaj.image.feature.local.affine.ASIFT;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint.AffineSimulationKeypointLocation;

/**
 * A {@link QuantisedLocalFeature} with a location described by an
 * {@link AffineSimulationKeypointLocation}. This can be used for representing
 * quantised local features extracted in combination with the {@link ASIFT}
 * algorithm.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class QuantisedAffineSimulationKeypoint extends QuantisedLocalFeature<AffineSimulationKeypointLocation> {
	/**
	 * Construct an empty {@link QuantisedAffineSimulationKeypoint}, located at
	 * the origin with an id of 0.
	 */
	public QuantisedAffineSimulationKeypoint() {
		super(new AffineSimulationKeypointLocation(), 0);
	}

	/**
	 * Construct a {@link QuantisedAffineSimulationKeypoint}, located at the
	 * origin with the given id.
	 *
	 * @param id
	 *            the id
	 */
	public QuantisedAffineSimulationKeypoint(int id) {
		super(new AffineSimulationKeypointLocation(), id);
	}

	/**
	 * Construct a {@link QuantisedAffineSimulationKeypoint}, located at the
	 * given position and id.
	 *
	 * @param location
	 *            the position
	 * @param id
	 *            the id
	 */
	public QuantisedAffineSimulationKeypoint(AffineSimulationKeypointLocation location, int id) {
		super(location, id);
	}
}
