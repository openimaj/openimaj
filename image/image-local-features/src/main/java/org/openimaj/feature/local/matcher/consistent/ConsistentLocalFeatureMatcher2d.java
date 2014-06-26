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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

/**
 * Object to attempt to find a consistent geometric mapping of two sets of local
 * features according to a given model.
 * 
 * The initial matching is performed by a separate matcher that is provided in
 * the constructor. The list of initial matches is then passed to the
 * modelfitter which attempts to estimate the model and provide a list of inlier
 * pairs, which are then used to populate the consistent matches.
 * 
 * @author Jonathon Hare
 * @param <T>
 *            The type of LocalFeature
 * 
 */
public class ConsistentLocalFeatureMatcher2d<T extends LocalFeature<?, ?> & Point2d>
		implements
		ModelFittingLocalFeatureMatcher<T>
{
	protected LocalFeatureMatcher<T> innerMatcher;
	protected RobustModelFitting<Point2d, Point2d, ?> modelfit;
	protected List<Pair<T>> consistentMatches;

	/**
	 * Default constructor
	 * 
	 * @param innerMatcher
	 *            the internal matcher for getting seed matches
	 */
	public ConsistentLocalFeatureMatcher2d(LocalFeatureMatcher<T> innerMatcher) {
		this.innerMatcher = innerMatcher;

		modelfit = null;
		consistentMatches = new ArrayList<Pair<T>>();
	}

	/**
	 * Default constructor
	 * 
	 * @param innerMatcher
	 *            the internal matcher for getting seed matches
	 * @param fit
	 *            the points against which to test consistency
	 */
	public ConsistentLocalFeatureMatcher2d(LocalFeatureMatcher<T> innerMatcher,
			RobustModelFitting<Point2d, Point2d, ?> fit)
	{
		this(innerMatcher);

		modelfit = fit;
	}

	/**
	 * @return a list of consistent matching keypoints according to the
	 *         estimated model parameters.
	 */
	@Override
	public List<Pair<T>> getMatches() {
		return consistentMatches;
	}

	/**
	 * @return a list of all matches irrespective of whether they fit the model
	 */
	public List<Pair<T>> getAllMatches() {
		return innerMatcher.getMatches();
	}

	@Override
	public Model<Point2d, Point2d> getModel() {
		return modelfit.getModel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean findMatches(List<T> keys1)
	{
		// if we're gonna re-use the object, we need to reset everything!
		consistentMatches = new ArrayList<Pair<T>>();

		// find the initial matches using the inner matcher
		innerMatcher.findMatches(keys1);
		final List<Pair<T>> matches = innerMatcher.getMatches();

		if (matches.size() < modelfit.numItemsToEstimate()) {
			consistentMatches.clear();
			consistentMatches.addAll(matches);
			return false;
		}

		final List<Pair<Point2d>> li_p2d = new ArrayList<Pair<Point2d>>();
		for (final Pair<T> m : matches) {
			li_p2d.add(new Pair<Point2d>(m.firstObject(), m.secondObject()));
		}

		// fit the model
		final boolean didfit = modelfit.fitData(li_p2d);

		// get the inliers and build the list of consistent matches
		for (final IndependentPair<Point2d, Point2d> p : modelfit.getInliers()) {
			final Object op = p;
			consistentMatches.add((Pair<T>) op);
		}

		return didfit;
	}

	@Override
	public void setFittingModel(RobustModelFitting<Point2d, Point2d, ?> mf) {
		modelfit = mf;
	}

	@Override
	public void setModelFeatures(List<T> modelkeys) {
		innerMatcher.setModelFeatures(modelkeys);
	}
}
