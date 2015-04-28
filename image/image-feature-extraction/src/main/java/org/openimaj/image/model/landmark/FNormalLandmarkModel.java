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
package org.openimaj.image.model.landmark;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.image.pixel.statistics.FStatisticalPixelProfileModel;
import org.openimaj.image.pixel.statistics.PixelProfileModel;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.util.pair.ObjectFloatPair;

/**
 * An {@link FNormalLandmarkModel} is a landmark represented by the 
 * surface normal line of a point (which is usually part of a 
 * {@link PointList} in an {@link FImage} connected by {@link PointListConnections}). 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FNormalLandmarkModel implements LandmarkModel<FImage> {
	/**
	 * A factory for producing {@link FNormalLandmarkModel}s
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class Factory implements LandmarkModelFactory<FImage> {
		private PointListConnections connections;
		private float normalLength;
		private int numSearchSamples;
		private FLineSampler sampler;
		private int numModelSamples;

		/**
		 * Default constructor.
		 * @param connections connections between points.
		 * @param sampler sampler for sampling along normals 
		 * @param numModelSamples number of samples for the model
		 * @param numSearchSamples number of samples for search; must be bigger than numModelSamples
		 * @param normalLength length of the normal in intrinsic scale units
		 */
		public Factory(PointListConnections connections, FLineSampler sampler, int numModelSamples, int numSearchSamples, float normalLength) {
			this.connections = connections;
			this.sampler = sampler;
			this.numModelSamples = numModelSamples;
			this.normalLength = normalLength;
			this.numSearchSamples = numSearchSamples;
		}

		@Override
		public FNormalLandmarkModel createLandmarkModel() {
			return new FNormalLandmarkModel(connections, sampler, numModelSamples, numSearchSamples, normalLength);
		}
		
		@Override
		public FNormalLandmarkModel createLandmarkModel(float scaleFactor) {
			return new FNormalLandmarkModel(connections, sampler, numModelSamples, numSearchSamples, scaleFactor * normalLength);
		}
	}

	private PointListConnections connections;
	private PixelProfileModel<FImage> model;
	private float normalLength;
	private int numModelSamples;
	private int numSearchSamples;

	/**
	 * Default constructor.
	 * 
	 * @param connections connections between points.
	 * @param sampler sampler for sampling along normals 
	 * @param numModelSamples number of samples for the model
	 * @param numSearchSamples number of samples for search; must be bigger than numModelSamples
	 * @param normalLength length of the normal in intrinsic scale units
	 */
	public FNormalLandmarkModel(PointListConnections connections, FLineSampler sampler, int numModelSamples, int numSearchSamples, float normalLength) {
		this.connections = connections;
		this.model = new FStatisticalPixelProfileModel(numModelSamples, sampler);
		this.normalLength = normalLength;
		this.numModelSamples = numModelSamples;
		this.numSearchSamples = numSearchSamples;
	}

	@Override
	public void updateModel(FImage image, Point2d point, PointList pointList) {
		float lineScale = normalLength * pointList.computeIntrinsicScale();
		Line2d line = connections.calculateNormalLine(point, pointList, lineScale);
		
		model.updateModel(image, line);
	}

	@Override
	public float computeCost(FImage image, Point2d point, PointList pointList) {
		float lineScale = normalLength * pointList.computeIntrinsicScale();
		Line2d line = connections.calculateNormalLine(point, pointList, lineScale);

		return model.computeCost(image, line);
	}

	@Override
	public ObjectFloatPair<Point2d> updatePosition(FImage image, Point2d initial, PointList pointList) {
		float scale = numSearchSamples * normalLength * pointList.computeIntrinsicScale() / (float) numModelSamples;
		Line2d line = connections.calculateNormalLine(initial, pointList, scale);
		
		Point2d newBest = model.computeNewBest(image, line, numSearchSamples);
		float distance = model.computeMovementDistance(image, line, numSearchSamples, newBest);
		
		return new ObjectFloatPair<Point2d>(newBest, distance);
	}
}
