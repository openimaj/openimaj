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

import gnu.trove.TIntProcedure;

import java.util.*;

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.math.geometry.point.*;
import org.openimaj.math.model.*;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.pair.Pair;



/**
 * Object to attempt to find a consistent geometric mapping
 * of two sets of keypoints according to a given geometric 
 * model
 * 
 * @author Jonathon Hare
 * @param <T> The type of keypoint
 *
 */
public class ConsistentKeypointMatcher<T extends Keypoint> extends FastBasicKeypointMatcher<T> implements ModelFittingLocalFeatureMatcher<T> {
	RobustModelFitting<Point2d, Point2d> modelfit;
	List<Pair<T>> consistentMatches;
	Model<Point2d, Point2d> model;
	int filterColinearThresh = 0;

	/**
	 * Default constructor
	 * @param threshold threshold for determining matching keypoints
	 * @param filterColinearThresh The threshold of the co-linear filter
	 */
	public ConsistentKeypointMatcher(int threshold, int filterColinearThresh) {
		super(threshold);

		modelfit = null;
		model = null;
		consistentMatches = new ArrayList<Pair<T>>();
		this.filterColinearThresh = filterColinearThresh;
	}

	/**
	 * Default constructor
	 * @param threshold threshold for determining matching keypoints
	 * @param fit the points against which to test consistency
	 * @param filterColinearThresh The threshold of the co-linear filter
	 */
	public ConsistentKeypointMatcher(int threshold, RobustModelFitting<Point2d, Point2d> fit, int filterColinearThresh) {
		this(threshold, filterColinearThresh);

		modelfit = fit;
	}

	/**
	 * Old constructor - immediatly tries to find model between two sets of keypoints
	 * @param keys1
	 * @param keys2
	 * @param fit
	 * @param threshold
	 */
	@Deprecated
	public ConsistentKeypointMatcher(List<T> keys1, List<T> keys2, RobustModelFitting<Point2d, Point2d> fit, int threshold) {
		this(threshold, 0);

		modelfit = fit;
		setModelKeypoints(keys2);
		findMatches(keys1);
	}

	/**
	 * @return a list of consistent matching keypoints according
	 * to the estimated model parameters.
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

	/* Given a pair of images and their keypoints, pick the first keypoint
	from one image and find its closest match in the second set of
	keypoints.  Then write the result to a file.
	 */
	@Override
	public boolean findMatches(List<T> keys1)
	{
		//if we're gonna re-use the object, we need to reset everything!
		model = null;
		matches = new ArrayList<Pair<T>>();
		consistentMatches = new ArrayList<Pair<T>>();
		//

		/* Match the keys in list keys1 to their best matches in keys2.
		 */
		for (T k : keys1) {
			T match = checkForMatch(k, modelKeypoints);  

			if (match != null) {
				matches.add(new Pair<T>(k, match));

				//System.out.println(k.col+", "+k.row+", "+k.scale+"\t->\t"+match.col+", "+match.row+", "+match.scale);
				//System.out.format("%3.2f, %3.2f, %3.2f\t->\t%3.2f, %3.2f, %3.2f\n", k.col, k.row, k.scale, match.col, match.row, match.scale);

				/*
				 * We could stop after a certain number of matches here...
				 * 
				 * Actually, we could stop, then restart if we were unable to find a good model...
				 */
				//if (matches.size() >= 10) break;
			}
		}

		if (filterColinearThresh >= 1) {
			matches = filterColinear(matches, 1);
		} else {
			//check for co-linear matches
			if (checkColinear()) {
				//System.out.println("Too many co-linear matches!");
				consistentMatches.clear();
				consistentMatches.addAll(matches);
				return false;
			}
		}
		
		List <Pair<Point2d>> li_p2d = new ArrayList<Pair<Point2d>>();
		for (Pair<T> m : matches) {
			li_p2d.add(new Pair<Point2d>(m.firstObject(), m.secondObject()));
		}
		
		if (matches.size() < modelfit.getModel().numItemsToEstimate()) {
			//System.out.println("Not enough matches to check consistency!");
			consistentMatches.clear();
			consistentMatches.addAll(matches);
			return false;
		}

		boolean didfit = modelfit.fitData(li_p2d);
		model = modelfit.getModel();
		modelfit.getInliers().forEach(new TIntProcedure(){
			@Override
			public boolean execute(int value) {
				consistentMatches.add(matches.get(value));
				return true;
			}
		});
		
		return didfit;
	}

	@Override
	public void setFittingModel(RobustModelFitting<Point2d, Point2d> mf) {
		modelfit = mf;
	}

	private boolean checkColinear() {
		Map<Keypoint, Integer> counts = new HashMap<Keypoint, Integer>();

		for (Pair<T> p : matches) {
			if (!counts.containsKey(p.secondObject())) {
				counts.put(p.secondObject(), 1);
			} else {
				int c = counts.get(p.secondObject());
				counts.put(p.secondObject(), c+1);
			}
		}

		if (counts.size() < 0.9 * matches.size()) return true;
		return false;
	}
	
	private static <T extends Keypoint> Map<T, Integer> getColinearCounts(List<Pair<T>> matches) {
		Map<T, Integer> counts = new HashMap<T, Integer>();

		for (Pair<T> p : matches) {
			if (!counts.containsKey(p.secondObject())) {
				counts.put(p.secondObject(), 1);
			} else {
				int c = counts.get(p.secondObject());
				counts.put(p.secondObject(), c+1);
			}
		}

		return counts;
	}

	/**
	 * @param <T>
	 * @param matches
	 * @param thresh
	 * @return the filtered set
	 */
	public static <T extends Keypoint> List<Pair<T>> filterColinear(List<Pair<T>> matches, int thresh) {
		List<Pair<T>> out = new ArrayList<Pair<T>>();
		Map<T, Integer> colin = getColinearCounts(matches);

		for (Pair<T> match : matches) {
			int n = colin.get(match.secondObject());

			if (n <= thresh) {
				out.add(match);
			}
		}

		return out;
	}
}
