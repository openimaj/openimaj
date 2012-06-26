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
package org.openimaj.feature.local.matcher.quantised;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.quantised.QuantisedLocalFeature;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.util.pair.Pair;


/**
 * Simple matcher for quantised features. Features match if they
 * have the same id.
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T> 
 *
 */
public class BasicQuantisedKeypointMatcher<T extends QuantisedLocalFeature<?>> implements LocalFeatureMatcher<T> {
	protected TIntObjectHashMap<List<T>> modelKeypoints;
	protected List <Pair<T>> matches;
	protected boolean allowMultiple;
	
	/**
	 * Match quantised keypoints
	 * @param allowMultiple
	 */
	public BasicQuantisedKeypointMatcher(boolean allowMultiple)
	{
		this.allowMultiple = allowMultiple;
	}
	
	@Override
	public List<Pair<T>> getMatches() {
		return matches;
	}
	
	@Override
	public boolean findMatches(List<T> keys1)
	{
		matches = new ArrayList<Pair<T>>();
		
	    for (T k : keys1) {
	    	if (modelKeypoints.contains(k.id)) {
	    		if (allowMultiple || modelKeypoints.get(k.id).size() == 1) {
	    			for (T match : modelKeypoints.get(k.id)) {
	    				matches.add(new Pair<T>(k, match));
	    			}
	    		}
	    	}
	    }
	    
	    return true;
	}

	@Override
	public void setModelFeatures(List<T> modelkeys) {
		modelKeypoints = new TIntObjectHashMap<List<T>>();
		for (T k : modelkeys) {
			if (!modelKeypoints.contains(k.id))
				modelKeypoints.put(k.id, new ArrayList<T>());
			modelKeypoints.get(k.id).add(k);
		}
	}
}
