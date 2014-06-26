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
package org.openimaj.feature.local.matcher.consistent;

import java.util.List;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.RobustModelFitting;

/**
 * Interface for classes able to match local features within the constraints of
 * a mathematical model between pairs of 2d points (i.e. affine transform,
 * homography, etc).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 */
public interface ModelFittingLocalFeatureMatcher<T extends LocalFeature<?, ?> /*
																			 * &
																			 * Point2d
																			 */> extends LocalFeatureMatcher<T> {
	/**
	 * Set the object which robustly attempts to fit matches to the model
	 * 
	 * @param mf
	 *            fitting model
	 */
	public void setFittingModel(RobustModelFitting<Point2d, Point2d, ?> mf);

	/**
	 * Attempt to find matches between the model features from the database, and
	 * given query features and learn the parameters of the underlying model
	 * that links the two sets of features.
	 * 
	 * @param queryfeatures
	 *            features from the query
	 */
	@Override
	public boolean findMatches(List<T> queryfeatures);

	/**
	 * Get the model that has been learned. Do this after finding matches!
	 * 
	 * @return the model found between the model features and object features
	 *         after a findMatches operation.
	 */
	public Model<Point2d, Point2d> getModel();
}
