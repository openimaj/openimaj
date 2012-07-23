/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The history of a set of tracked features through time
 * 
 * @author Stan Birchfield
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FeatureHistory {
	Map<Integer, List<List<Feature>>> history = new TreeMap<Integer, List<List<Feature>>>();
	
	/**
	 * The feature history. Each element is a list over time.
	 */
	public List<Feature> [] currentState;
	
	/**
	 * Default constructor with given number of features
	 * @param nFeatures
	 */
	@SuppressWarnings("unchecked")
	public FeatureHistory(int nFeatures) {
		currentState = new List[nFeatures];
	}
	
	/**
	 * Record a list of features at a given time
	 * @param fl
	 * @param frame
	 */
	public void record(FeatureList fl, int frame) {
		for (int i=0; i<fl.features.length; i++) {
			Feature f = fl.features[i];
			
			if (f.val>=0) {
				//was tracked
				if (currentState[i] == null) {
					List<Feature> ff = new ArrayList<Feature>();
					List<List<Feature>> hist;
					if (history.containsKey(frame)) {
						hist = history.get(frame);
					} else {
						hist = new ArrayList<List<Feature>>();
						history.put(frame, hist);
					}
					hist.add(ff);
					currentState[i] = ff;
				}
				if (currentState[i].size() == 0 || !currentState[i].get(currentState[i].size()-1).equals(f))
					currentState[i].add(f.clone());
			} else {
				//was lost
				currentState[i] = null;
			}
		}
	}
	
	@Override
	public String toString() {
		String s = "FeatureHistory[\n";
		for (int startframe : history.keySet()) {
			List<List<Feature>> tracks = history.get(startframe);
			s += "Starting frame: " + startframe + ":\n";
			for (int i=0; i<tracks.size(); i++) {
				s += "\t" + i +" " + tracks.get(i) + "\n";
			}
		}
		return s+"]";
	}
}
