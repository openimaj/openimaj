package org.openimaj.video.tracking.klt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FeatureHistory {
	Map<Integer, List<List<Feature>>> history = new TreeMap<Integer, List<List<Feature>>>();
	
	public List<Feature> [] currentState;
	
	@SuppressWarnings("unchecked")
	public FeatureHistory(int nFeatures) {
		currentState = new List[nFeatures];
	}
	
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
