package org.openimaj.feature.local.matcher;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.util.pair.Pair;

/**
 * Matcher that uses minimum Euclidean distance to find matches.
 * Model and object are compared both ways. Matches that are
 * oneway are rejected.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public class BasicTwoWayMatcher <T extends LocalFeature<?>> implements LocalFeatureMatcher<T> {
	protected List<T> modelKeypoints;
	protected List <Pair<T>> matches;
	
	@Override
	public void setModelFeatures(List<T> modelkeys) {
		this.modelKeypoints = modelkeys;		
	}

	/**
	 * This searches through the keypoints in klist for the two closest
	 * matches to key.  If the closest is less than 0.6 times distance to
	 * second closest, then return the closest match.  Otherwise, return
	 * NULL.
	 */
	protected T findMatch(T query, List<T> features)
	{
	    double distsq = Double.MAX_VALUE;
	    T minkey = null;
	    
	    //find two closest matches
	    for (T target : features) {
	    	double dsq = target.getFeatureVector().asDoubleFV().compare(query.getFeatureVector().asDoubleFV(), DoubleFVComparison.EUCLIDEAN);
	        
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
		
		for (T query : queryfeatures) {
			T modeltarget = findMatch(query, modelKeypoints);
			T querytarget = findMatch(modeltarget, queryfeatures);
			
			if (querytarget == query) {
				matches.add(new Pair<T>(query, modeltarget));
			}
		}
		
		return false;
	}

	@Override
	public List<Pair<T>> getMatches() {
		return matches;
	}
}
