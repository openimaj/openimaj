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
package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point3d;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Concrete implementation of a model of a 3D rigid transform with only rotation
 * and translation allowed. Capable of least-squares estimate of model
 * parameters.
 * 
 * @see TransformUtilities#rigidMatrix(List)
 * 
 * @author Jonathon Hare
 * 
 */
public class RigidTransformModel3d extends AffineTransformModel3d
		implements MatrixTransformProvider
{
	/**
	 * Create a {@link RigidTransformModel3d}
	 */
	public RigidTransformModel3d()
	{
		super();
	}

	@Override
	public RigidTransformModel3d clone() {
		final RigidTransformModel3d atm = new RigidTransformModel3d();
		atm.transform = transform.copy();
		return atm;
	}

	@Override
	public boolean estimate(List<? extends IndependentPair<Point3d, Point3d>> data) {
		this.transform = TransformUtilities.rigidMatrix(data);

		try {
			transform.inverse();
		} catch (final RuntimeException e) {
			transform = Matrix.identity(4, 4);
		}
		return true;
	}

	@Override
	public int numItemsToEstimate() {
		return 6;
	}
}
