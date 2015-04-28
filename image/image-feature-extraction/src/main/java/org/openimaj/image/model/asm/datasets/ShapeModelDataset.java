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
package org.openimaj.image.model.asm.datasets;

import java.util.List;

import org.openimaj.data.dataset.ListDataset;
import org.openimaj.image.Image;
import org.openimaj.image.model.asm.ActiveShapeModel;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.util.pair.IndependentPair;

/**
 * Dataset representing pairs of images and fixed size sets of points, together
 * with a set of connections between points which are valid across all
 * instances. Useful for training {@link PointDistributionModel}s and
 * {@link ActiveShapeModel}s.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            type of the images in the collection
 */
public interface ShapeModelDataset<IMAGE extends Image<?, IMAGE>>
extends
ListDataset<IndependentPair<PointList, IMAGE>>
{
	/**
	 * Get the connections between the points
	 *
	 * @return the connections
	 */
	public PointListConnections getConnections();

	/**
	 * Get the points for each instance
	 *
	 * @return the point list for each instance
	 */
	public List<PointList> getPointLists();

	/**
	 * Get the image for each instance
	 *
	 * @return the image for each instance
	 */
	public List<IMAGE> getImages();
}
