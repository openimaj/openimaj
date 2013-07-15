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
package org.openimaj.feature.local.matcher;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.util.pair.Pair;

/**
 * Matcher that uses minimum Euclidean distance to find matches. Model and
 * object are compared both ways. Matches that are oneway are rejected, as are
 * one->many matches.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public class BasicTwoWayMatcher<T extends LocalFeature<?, ?>> implements LocalFeatureMatcher<T> {
	protected List<T> modelKeypoints;
	protected List<Pair<T>> matches;

	@Override
	public void setModelFeatures(List<T> modelkeys) {
		this.modelKeypoints = modelkeys;
	}

	/**
	 * This searches through the keypoints in klist for the closest match to
	 * key.
	 */
	protected T findMatch(T query, List<T> features)
	{
		double distsq = Double.MAX_VALUE;
		T minkey = null;

		// find closest match
		for (final T target : features) {
			final double dsq = target.getFeatureVector().asDoubleFV()
					.compare(query.getFeatureVector().asDoubleFV(), DoubleFVComparison.EUCLIDEAN);

			if (dsq < distsq) {
				distsq = dsq;
				minkey = target;
			}
		}

		return minkey;
	}

	@Override
	public boolean findMatches(List<T> queryfeatures) {
		matches = new ArrayList<Pair<T>>();

		final TObjectIntHashMap<T> targets = new TObjectIntHashMap<T>();

		for (final T query : queryfeatures) {
			final T modeltarget = findMatch(query, modelKeypoints);
			if (modeltarget == null)
				continue;

			final T querytarget = findMatch(modeltarget, queryfeatures);

			if (querytarget == query) {
				matches.add(new Pair<T>(query, modeltarget));
				targets.adjustOrPutValue(modeltarget, 1, 1);
			}
		}

		final ArrayList<Pair<T>> matchesToRemove = new ArrayList<Pair<T>>();
		targets.forEachEntry(new TObjectIntProcedure<T>() {
			@Override
			public boolean execute(T a, int b) {
				if (b > 1) {
					for (final Pair<T> p : matches) {
						if (p.secondObject() == a)
							matchesToRemove.add(p);
					}
				}
				return true;
			}
		});

		matches.removeAll(matchesToRemove);

		return matches.size() > 0;
	}

	@Override
	public List<Pair<T>> getMatches() {
		return matches;
	}
}
