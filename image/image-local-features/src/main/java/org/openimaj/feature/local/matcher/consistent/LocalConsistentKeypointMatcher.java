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

import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.knn.CoordinateKDTree;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

/**
 * Improved version of ConsistentKeypointMatcher. Much much faster! We use a
 * b-tree to implement scale space search & stop the matcher after a number of
 * iterations and attempt to find a model with what we have.
 * 
 * @author Jonathon Hare
 * @param <T>
 *            The type of keypoint
 * 
 */
public class LocalConsistentKeypointMatcher<T extends Keypoint> extends FastBasicKeypointMatcher<T>
		implements
		ModelFittingLocalFeatureMatcher<T>
{
	RobustModelFitting<Point2d, Point2d, ?> modelfit;
	List<Pair<T>> consistentMatches;
	Model<Point2d, Point2d> model;
	CoordinateKDTree<T> tree;

	Keypoint minDim, maxDim;

	/**
	 * Default constructor
	 * 
	 * @param threshold
	 *            threshold for determining matching keypoints
	 */
	public LocalConsistentKeypointMatcher(int threshold) {
		super(threshold);

		model = null;
		consistentMatches = new ArrayList<Pair<T>>();

		minDim = new Keypoint();
		maxDim = new Keypoint();
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
		return matches;
	}

	@Override
	public Model<Point2d, Point2d> getModel() {
		return model;
	}

	/*
	 * Given a pair of images and their keypoints, pick the first keypoint from
	 * one image and find its closest match in the second set of keypoints. Then
	 * write the result to a file.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean findMatches(List<T> keys1)
	{
		// if we're gonna re-use the object, we need to reset everything!
		model = null;
		matches = new ArrayList<Pair<T>>();
		consistentMatches = new ArrayList<Pair<T>>();
		//

		final List<Pair<Point2d>> li_p2d = new ArrayList<Pair<Point2d>>();

		final List<T> klist1 = new ArrayList<T>();
		tree.rangeSearch(klist1, minDim, maxDim);

		// Keypoint.KeypointStats(klist1);

		T initMatch = null;
		for (final T k : keys1) {
			// find a seed point
			initMatch = checkForMatch(k, klist1);

			if (initMatch != null) {
				break;
			}
		}

		// System.out.println("INIT: " + initMatch);

		if (initMatch == null) {
			System.out.println("no match found!");
			return false;
		}

		final Keypoint lbound = new Keypoint();
		final Keypoint ubound = new Keypoint();

		/*
		 * These parameters have been hardcoded, but in reality they probably
		 * shouldn't be. The values of +/-100 are just guestimates - a proper
		 * evaluation is needed to find optimal values (however +/-100 does seem
		 * to work well)
		 */
		lbound.x = initMatch.x - 100;
		lbound.y = initMatch.y - 100;
		lbound.scale = minDim.scale;
		ubound.x = initMatch.x + 100;
		ubound.y = initMatch.y + 100;
		ubound.scale = maxDim.scale;

		final List<T> klist = new ArrayList<T>();
		tree.rangeSearch(klist, lbound, ubound);

		for (final T k : keys1) {
			// find a seed point
			final T match = checkForMatch(k, klist);

			if (match != null) {
				li_p2d.add(new Pair<Point2d>(k, match));
				matches.add(new Pair<T>(k, match));

				// System.out.println(k.col+", "+k.row+", "+k.scale+"\t->\t"+match.col+", "+match.row+", "+match.scale);
				// System.out.format("%3.2f, %3.2f, %3.2f\t->\t%3.2f, %3.2f, %3.2f\n",
				// k.col, k.row, k.scale, match.col, match.row, match.scale);

				/*
				 * We could stop after a certain number of matches here...
				 * 
				 * Actually, we could stop, then restart if we were unable to
				 * find a good model...
				 */
				if (matches.size() >= 10)
					break;
			}
		}

		if (matches.size() < modelfit.numItemsToEstimate()) {
			System.out.println("Not enough matches to check consistency!");
			return false;
		}

		if (modelfit.fitData(li_p2d)) {
			model = modelfit.getModel();
			for (final IndependentPair<Point2d, Point2d> p : modelfit.getInliers()) {
				final Object op = p;
				consistentMatches.add((Pair<T>) op);
			}
		}
		return true;
	}

	@Override
	public void setFittingModel(RobustModelFitting<Point2d, Point2d, ?> mf) {
		modelfit = mf;
	}

	@Override
	public void setModelFeatures(List<T> map) {
		// build KDTree
		try {
			// System.out.println("building tree");
			tree = new CoordinateKDTree<T>();

			for (final T k : map) {
				tree.insert(k);

				if (k.x < minDim.x)
					minDim.x = k.x;
				if (k.y < minDim.y)
					minDim.y = k.y;
				if (k.scale < minDim.scale)
					minDim.scale = k.scale;

				if (k.x > maxDim.x)
					maxDim.x = k.x;
				if (k.y > maxDim.y)
					maxDim.y = k.y;
				if (k.scale > maxDim.scale)
					maxDim.scale = k.scale;
			}
			// System.out.println("done");
		} catch (final Exception e) {
			System.out.println(e);
		}
	}
}
